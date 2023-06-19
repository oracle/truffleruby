/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oracle.truffle.api.profiles.Profile;
import org.graalvm.collections.Pair;
import org.truffleruby.core.proc.ProcCallTargets;
import org.truffleruby.core.support.DetailedInspectingSupport;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.methods.CachedLazyCallTargetSupplier;
import org.truffleruby.language.methods.ModuleBodyDefinition;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.nodes.RootNode;

public abstract class TruffleASTPrinter {

    // Skip the following node attributes:
    // - "sourceCharIndex", "sourceLength" - are incorrect and will have correct values in YARP
    // - "RubyRootNode.bodyCopy" - is set if clone-uninitialized is forced
    private static final Set<String> attributesToIgnore = Set.of("sourceCharIndex", "sourceLength", "bodyCopy");

    public static String dump(RubyRootNode rootNode, String focusedNodeClassName, int index) {
        final Node node;
        try {
            @SuppressWarnings("unchecked")
            final var focusedNodeClass = (Class<? extends Node>) (Class.forName(focusedNodeClassName));
            List<? extends Node> nodes = NodeUtil.findAllNodeInstances(rootNode, focusedNodeClass);

            if (index >= nodes.size()) {
                String astString = NodeUtil.printTreeToString(rootNode);
                return "Cannot find index=" + index + " node of class " + focusedNodeClassName +
                        ". There are only " + nodes.size() + " of such nodes in the AST: \n" + astString;
            }

            node = nodes.get(index);
        } catch (ClassNotFoundException e) {
            String astString = NodeUtil.printTreeToString(rootNode);
            String message = "Cannot find a Java class for a focused node class name " +
                    focusedNodeClassName +
                    ", that is supposed to be used in the AST: \n" + astString;
            return message;
        }

        if (node == null) {
            String astString = NodeUtil.printTreeToString(rootNode);
            return "Cannot find " + focusedNodeClassName + " node in AST: \n" + astString;
        }

        try {
            StringBuilder out = new StringBuilder();
            printTree(out, node, 0);
            return out.toString();
        } catch (IllegalAccessException e) {
            return "Exception: " + e;
        }
    }

    private static void printTree(StringBuilder out, Node node, int level) throws IllegalAccessException {
        // A node has fields of two types:
        // - children nodes, part of AST
        // - its own attributes/properties - int, String, boolean, etc fields
        // So print them separately.
        final var attributes = getNodeAttributes(node);
        final var children = getNodeChildren(node);

        // instances of CallTarget class - either attributes or attribute's own field
        final var rootCallTargets = getNestedCallTargets(attributes);

        attributes.sort(Comparator.comparing(Pair::getLeft));
        children.sort(Comparator.comparing(Pair::getLeft));

        // node name
        printNewLine(out, level);
        out.append(nodeName(node));

        // node's non-AST fields (attributes)
        if (!attributes.isEmpty()) {
            printAttributes(attributes, out, level);
        }

        // node's AST fields (children nodes)
        if (!children.isEmpty()) {
            printChildren(children, out, level);
        }

        // call targets
        if (!rootCallTargets.isEmpty()) {
            printCallTargets(rootCallTargets, out, level);
        }
    }

    private static List<Pair<String, Object>> getNodeAttributes(Node node) {
        final TreeSet<String> generatedFieldNames = new TreeSet<>();

        if (node.getClass().getName().endsWith("Gen")) {
            var ownFieldNames = NodeUtil.collectFieldNames(node.getClass());
            @SuppressWarnings("unchecked")
            var parentFieldNames = NodeUtil.collectFieldNames((Class<? extends Node>) node.getClass().getSuperclass());
            generatedFieldNames.addAll(ownFieldNames);
            generatedFieldNames.removeAll(parentFieldNames);
        }

        var attributesMap = NodeUtil.collectNodeProperties(node);
        var attributes = new ArrayList<>(attributesMap.entrySet())
                .stream()
                .map(entry -> Pair.create(entry.getKey(), entry.getValue()))
                .map(pair -> {
                    if (pair.getLeft().startsWith("field.")) {
                        String name = pair.getLeft().substring("field.".length());
                        return Pair.create(name, pair.getRight());
                    } else {
                        return pair;
                    }
                })
                .filter(pair -> pair.getRight() != null)                        // hide numerous attributes that aren't initialized yet
                .filter(pair -> !attributesToIgnore.contains(pair.getLeft()))   // ignore some noisy attributes
                .filter(pair -> !generatedFieldNames.contains(pair.getLeft()))  // ignore attributes of generated -Gen classes
                .filter(pair -> !(pair.getRight() instanceof Profile))          // ignore ...Profile as far as they might be disabled/enabled that affects string representation
                .collect(Collectors.toList());

        return attributes;
    }

    private static List<Pair<String, Object>> getNodeChildren(Node node) {
        var childrenMapWithFlattenArrays = NodeUtil.collectNodeChildren(node);
        var childrenMap = new HashMap<String, Object>();
        for (var entry : childrenMapWithFlattenArrays.entrySet()) {
            if (entry.getKey().contains("[")) {
                String key = entry.getKey();
                String name = key.substring(0, key.indexOf("["));

                if (childrenMap.get(name) == null) {
                    var values = new ArrayList<>();
                    values.add(entry.getValue());
                    childrenMap.put(name, values);
                } else {
                    @SuppressWarnings("unchecked")
                    var values = (ArrayList<Object>) childrenMap.get(name);
                    values.add(entry.getValue());
                }
            } else {
                childrenMap.put(entry.getKey(), entry.getValue());
            }
        }

        var children = new ArrayList<>(childrenMap.entrySet())
                .stream()
                .map(entry -> Pair.create(entry.getKey(), entry.getValue()))
                .filter(pair -> pair.getRight() != null)    // hide child nodes that aren't initialized yet
                .collect(Collectors.toList());

        return children;
    }

    // instances of CallTarget class - either attributes or attribute's own field
    private static ArrayList<RootCallTarget> getNestedCallTargets(List<Pair<String, Object>> attributes) {
        final ArrayList<RootCallTarget> rootCallTargets = new ArrayList<>();

        for (var pair : attributes) {
            Object value = pair.getRight();

            // BlockDefinitionNode contains a ProcCallTargets field with callTargetForProc and callTargetForLambda fields
            if (value instanceof ProcCallTargets procsCallTargets) {
                if (procsCallTargets.hasCallTargetForProc()) {
                    rootCallTargets.add(procsCallTargets.getCallTargetForProc());
                }

                if (procsCallTargets.hasCallTargetForLambda()) {
                    rootCallTargets.add(procsCallTargets.getCallTargetForLambda());
                }
            }

            // ModuleBodyDefinition contains a CallTarget field
            if (value instanceof ModuleBodyDefinition moduleBodyDefinition) {
                rootCallTargets.add(moduleBodyDefinition.getCallTarget());
            }

            // LiteralMethodDefinitionNode contains a CachedLazyCallTargetSupplier field that contains CallTarget
            if (value instanceof CachedLazyCallTargetSupplier cachedLazyCallTargetSupplier) {
                final var rootCallTarget = cachedLazyCallTargetSupplier.get();
                rootCallTargets.add(rootCallTarget);
            }
        }

        return rootCallTargets;
    }

    private static void printAttributes(List<Pair<String, Object>> attributes, StringBuilder out, int level) {

        printNewLine(out, level + 1);
        out.append("attributes:");

        for (var pair : attributes) {
            printNewLine(out, level + 2);

            final String name = pair.getLeft();
            final Object value = pair.getRight();
            String string = valueOrArrayToString(value);

            // remove variable suffix when value is a custom class instance,
            // e.g. org.truffleruby.language.arguments.EmptyArgumentsDescriptor@359b650b
            // ignore class and instance variable names, e.g. values `@foo` or `@@bar`
            string = string.replaceAll("(?<!^|@)@[0-9a-f]+", "@...");

            // remove variable String representation of an instance of MethodTranslator,
            // e.g. "org.truffleruby.parser.MethodTranslator$$Lambda$839/0x00000008012ec000@..."
            // or "org.truffleruby.parser.MethodTranslator$$Lambda/0x00000008012d5c70@..."
            string = string.replaceAll(
                    "\\$\\$Lambda[^@]+@",
                    Matcher.quoteReplacement("$$Lambda$.../0x...@"));

            // remove column information for SourceSection - it's wrong in the current implementation
            // and will lead to noise in diff during switching to YARP (that provides accurate column information)
            // Example:
            //   SourceSection(source=<parse_ast> [1:1 - 1:5], index=0, length=5, characters=END {)
            string = string.replaceAll("\\[(\\d+):\\d+ - (\\d+):\\d+\\]", "[$1 - $2]");

            // avoid confusing 'emptyTString = '
            if (value instanceof String || string.isEmpty()) {
                string = "\"" + string + "\"";
            }

            out.append(name + " = " + string);
        }
    }

    private static void printChildren(List<Pair<String, Object>> children, StringBuilder out, int level)
            throws IllegalAccessException {
        printNewLine(out, level + 1);
        out.append("children:");

        for (var pair : children) {
            final Object value = pair.getRight();

            printNewLine(out, level + 2);
            out.append(pair.getLeft());

            if (value instanceof Node) {
                // single child node
                out.append(" ="); // a node is printed on the next line, so the terminating whitespace is excessive
                printTree(out, (Node) value, level + 3);
            } else if (value instanceof ArrayList) {
                // collection of children
                @SuppressWarnings("unchecked")
                var values = (ArrayList<Object>) value;

                out.append(" = [");

                for (Object child : values) {
                    printTree(out, (Node) child, level + 3);
                }

                printNewLine(out, level + 2);
                out.append("]");
            }
        }
    }

    private static void printCallTargets(ArrayList<RootCallTarget> rootCallTargets, StringBuilder out, int level)
            throws IllegalAccessException {
        // Don't print even title if there are no elements at all
        printNewLine(out, level + 1);
        out.append("call targets:");

        for (RootCallTarget callTarget : rootCallTargets) {
            RootNode rootNode = callTarget.getRootNode();
            printTree(out, rootNode, level + 2);
        }
    }

    private static void printNewLine(StringBuilder out, int level) {
        out.append("\n");
        for (int i = 0; i < level; i++) {
            out.append("    ");
        }
    }

    private static String nodeName(Node node) {
        return className(node.getClass());
    }

    private static String className(Class<?> clazz) {
        String name = clazz.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private static String valueOrArrayToString(Object value) {
        String valueString;

        if (value.getClass().isArray()) {
            Object[] elements = (Object[]) value;
            String[] strings = new String[elements.length];

            for (var i = 0; i < elements.length; i++) {
                Object element = elements[i];
                strings[i] = valueToString(element);
            }

            valueString = Stream.of(strings).collect(Collectors.joining(", ", "[", "]"));
        } else {
            valueString = valueToString(value);
        }

        return valueString;
    }

    private static String valueToString(Object value) {
        String string;

        if (value instanceof DetailedInspectingSupport) {
            string = ((DetailedInspectingSupport) value).toStringWithDetails();
        } else {
            string = value.toString();
        }

        return string;
    }

}
