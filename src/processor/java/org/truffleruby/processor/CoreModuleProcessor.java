/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.processor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

@SupportedAnnotationTypes("org.truffleruby.builtins.CoreModule")
public class CoreModuleProcessor extends AbstractProcessor {

    private static final String SUFFIX = "Builtins";
    private static final Set<String> KEYWORDS;
    static {
        KEYWORDS = new HashSet<>();
        KEYWORDS.addAll(Arrays.asList(
                "alias",
                "and",
                "begin",
                "break",
                "case",
                "class",
                "def",
                "defined?",
                "do",
                "else",
                "elsif",
                "end",
                "ensure",
                "false",
                "for",
                "if",
                "in",
                "module",
                "next",
                "nil",
                "not",
                "or",
                "redo",
                "rescue",
                "retry",
                "return",
                "self",
                "super",
                "then",
                "true",
                "undef",
                "unless",
                "until",
                "when",
                "while",
                "yield"));
    }

    private final Set<String> processed = new HashSet<>();
    private TypeMirror virtualFrameType;
    private TypeMirror objectType;
    private TypeMirror rubyNodeType;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        virtualFrameType = processingEnv
                .getElementUtils()
                .getTypeElement("com.oracle.truffle.api.frame.VirtualFrame")
                .asType();
        objectType = processingEnv
                .getElementUtils()
                .getTypeElement("java.lang.Object")
                .asType();
        rubyNodeType = processingEnv
                .getElementUtils()
                .getTypeElement("org.truffleruby.language.RubyNode")
                .asType();

        if (!annotations.isEmpty()) {
            for (Element element : roundEnvironment.getElementsAnnotatedWith(CoreModule.class)) {
                try {
                    processCoreModule((TypeElement) element);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, e.getClass() + " " + e.getMessage(), element);
                }
            }
        }

        return true;
    }

    private void processCoreModule(TypeElement coreModuleElement) throws IOException {
        final CoreModule coreModule = coreModuleElement.getAnnotation(CoreModule.class);

        final PackageElement packageElement = (PackageElement) coreModuleElement.getEnclosingElement();
        final String packageName = packageElement.getQualifiedName().toString();

        final String qualifiedName = coreModuleElement.getQualifiedName().toString();
        if (!processed.add(qualifiedName)) {
            // Already processed, do nothing. This seems an Eclipse bug.
            return;
        }

        final JavaFileObject output = processingEnv
                .getFiler()
                .createSourceFile(qualifiedName + SUFFIX, coreModuleElement);
        final FileObject rubyFile = processingEnv.getFiler().createResource(
                StandardLocation.SOURCE_OUTPUT,
                "core_module_stubs",
                coreModule.value().replace("::", "/") + ".rb",
                (Element[]) null);


        try (PrintStream stream = new PrintStream(output.openOutputStream(), true, "UTF-8")) {
            try (PrintStream rubyStream = new PrintStream(rubyFile.openOutputStream(), true, "UTF-8")) {

                final List<? extends Element> enclosedElements = coreModuleElement.getEnclosedElements();
                final boolean anyCoreMethod = anyCoreMethod(enclosedElements);

                stream.println("package " + packageName + ";");
                stream.println();
                stream.println("import org.truffleruby.builtins.CoreMethodNodeManager;");
                stream.println("import org.truffleruby.builtins.PrimitiveManager;");
                if (anyCoreMethod) {
                    stream.println("import org.truffleruby.language.Visibility;");
                }
                stream.println();
                stream.println("public class " + coreModuleElement.getSimpleName() + SUFFIX + " {");
                stream.println();
                stream.println(
                        "    public static void setup(CoreMethodNodeManager coreMethodManager, PrimitiveManager primitiveManager) {");

                rubyStream.println("raise 'this file is a stub file for development and should never be loaded'");
                rubyStream.println();
                rubyStream.println((coreModule.isClass() ? "class" : "module") + " " + coreModule.value());
                rubyStream.println();

                final StringBuilder rubyPrimitives = new StringBuilder();

                for (Element e : enclosedElements) {
                    if (e instanceof TypeElement) {
                        final TypeElement klass = (TypeElement) e;

                        final CoreMethod coreMethod = klass.getAnnotation(CoreMethod.class);
                        if (coreMethod != null) {
                            if (coreMethod.optional() > 0 || coreMethod.needsBlock()) {
                                checkAmbiguousOptionalArguments(coreMethod, klass);
                            }
                            // Do not use needsSelf=true in module functions, it is either the module/class or the instance.
                            // Usage of needsSelf is quite rare for singleton methods (except constructors).
                            boolean needsSelf = coreMethod.constructor() ||
                                    (!coreMethod.isModuleFunction() && !coreMethod.onSingleton() &&
                                            coreMethod.needsSelf());

                            checkLowerFixnumArguments(coreMethod.lowerFixnum(), klass, needsSelf);
                            processCoreMethod(stream, rubyStream, coreModuleElement, coreModule, klass, coreMethod);
                        }

                        final Primitive primitive = e.getAnnotation(Primitive.class);
                        if (primitive != null) {
                            processPrimitive(stream, rubyPrimitives, coreModuleElement, klass, primitive);
                            checkLowerFixnumArguments(primitive.lowerFixnum(), klass, primitive.needsSelf());
                        }
                    }
                }

                stream.println("    }");
                stream.println();
                stream.println("}");

                rubyStream.println("end");
                rubyStream.println();

                rubyStream.println("module TrufflePrimitive");
                rubyStream.print(rubyPrimitives);
                rubyStream.println("end");
                rubyStream.println();
            }
        }
    }

    private void checkLowerFixnumArguments(int[] lowerFixnum, TypeElement klass, boolean needsSelf) {
        byte[] lowerArgs = null;

        List<ExecutableElement> specializationMethods = new ArrayList<>();

        TypeElement klassIt = klass;
        while (true) {
            for (Element el : klassIt.getEnclosedElements()) {
                if (!(el instanceof ExecutableElement)) {
                    continue; // we are interested only in executable elements
                }

                final ExecutableElement specializationMethod = (ExecutableElement) el;

                Specialization specializationAnnotation = specializationMethod.getAnnotation(Specialization.class);
                if (specializationAnnotation == null) {
                    continue; // we are interested only in Specialization methods
                }

                specializationMethods.add(specializationMethod);
            }

            // TODO (pitr-ch 15-Nov-2019): did not we need to find superclasses somewhere else as well ?
            klassIt = processingEnv.getElementUtils().getTypeElement(klassIt.getSuperclass().toString());
            if (processingEnv.getTypeUtils().isSameType(klassIt.asType(), rubyNodeType)) {
                break;
            }
        }

        for (ExecutableElement specializationMethod : specializationMethods) {
            List<? extends VariableElement> parameters = specializationMethod.getParameters();
            int skip = needsSelf ? 1 : 0;

            if (parameters.size() > 0 &&
                    processingEnv.getTypeUtils().isSameType(parameters.get(0).asType(), virtualFrameType)) {
                skip++;
            }

            int end = parameters.size();
            for (int i = end - 1; i >= skip; i--) {
                boolean cached = parameters.get(i).getAnnotation(Cached.class) != null;
                if (cached) {
                    end--;
                } else {
                    break;
                }
            }

            if (lowerArgs == null) {
                if (end < skip) {
                    processingEnv.getMessager().printMessage(
                            Kind.ERROR,
                            "should have needsSelf = false",
                            specializationMethod);
                    continue;
                }
                lowerArgs = new byte[end - skip];
            } else {
                assert lowerArgs.length == end - skip;
            }

            for (int i = skip; i < end; i++) {
                TypeKind argumentType = parameters.get(i).asType().getKind();
                if (argumentType == TypeKind.INT) {
                    lowerArgs[i - skip] |= 0b01;
                } else if (argumentType == TypeKind.LONG) {
                    lowerArgs[i - skip] |= 0b10;
                }
            }
        }

        if (lowerArgs == null) {
            processingEnv.getMessager().printMessage(
                    Kind.ERROR,
                    "could not find specializations (lowerArgs == null)",
                    klass);
            return;
        }

        // Verify against the lowerFixnum annotation
        for (int i = 0; i < lowerArgs.length; i++) {
            boolean shouldLower = lowerArgs[i] == 0b01; // int without long
            if (shouldLower && !contains(lowerFixnum, i + 1)) {
                processingEnv.getMessager().printMessage(
                        Kind.ERROR,
                        "should use lowerFixnum for argument " + (i + 1),
                        klass);
            }
        }
    }

    private static boolean contains(int[] array, int value) {
        for (int n = 0; n < array.length; n++) {
            if (array[n] == value) {
                return true;
            }
        }
        return false;
    }

    private void checkAmbiguousOptionalArguments(CoreMethod coreMethod, TypeElement klass) {
        for (Element el : klass.getEnclosedElements()) {
            if (!(el instanceof ExecutableElement)) {
                continue; // we are interested only in executable elements
            }

            final ExecutableElement specializationMethod = (ExecutableElement) el;

            Specialization specializationAnnotation = specializationMethod.getAnnotation(Specialization.class);
            if (specializationAnnotation == null) {
                continue; // we are interested only in Specialization methods
            }

            List<? extends VariableElement> parameters = specializationMethod.getParameters();

            int n = parameters.size() - 1;
            // Ignore all the @Cached methods from our consideration.
            while (n >= 0 && parameters.get(n).getAnnotation(Cached.class) != null) {
                n--;
            }

            if (coreMethod.needsBlock()) {
                if (n < 0) {
                    processingEnv.getMessager().printMessage(
                            Kind.ERROR,
                            "invalid block method parameter position for",
                            specializationMethod);
                    continue;
                }
                isParameterUnguarded(specializationAnnotation, parameters.get(n));
                n--; // Ignore block argument.
            }

            if (coreMethod.rest()) {
                if (n < 0) {
                    processingEnv.getMessager().printMessage(
                            Kind.ERROR,
                            "missing rest method parameter",
                            specializationMethod);
                    continue;
                }

                if (parameters.get(n).asType().getKind() != TypeKind.ARRAY) {
                    processingEnv.getMessager().printMessage(
                            Kind.ERROR,
                            "rest method parameter is not array",
                            parameters.get(n));
                    continue;
                }
                n--; // ignore final Object[] argument
            }

            for (int i = 0; i < coreMethod.optional(); i++, n--) {
                if (n < 0) {
                    processingEnv.getMessager().printMessage(
                            Kind.ERROR,
                            "invalid optional parameter count for",
                            specializationMethod);
                    continue;
                }
                isParameterUnguarded(specializationAnnotation, parameters.get(n));
            }
        }
    }

    private void isParameterUnguarded(Specialization specializationAnnotation, VariableElement parameter) {
        String name = parameter.getSimpleName().toString();

        // A specialization will only be called if the types of the arguments match its declared parameter
        // types. So a specialization with a declared optional parameter of type NotProvided will only be
        // called if that argument is not supplied. Similarly a specialization with a DynamicObject optional
        // parameter will only be called if the value has been supplied.
        //
        // Since Object is the super type of NotProvided any optional parameter declaration of type Object
        // must have additional guards to check whether this specialization should be called, or must make
        // it clear in the parameter name (by using unused or maybe prefix) that it may not have been
        // provided or is not used.

        if (processingEnv.getTypeUtils().isSameType(parameter.asType(), objectType) &&
                !name.startsWith("unused") &&
                !name.startsWith("maybe") &&
                !isGuarded(name, specializationAnnotation.guards())) {
            processingEnv.getMessager().printMessage(
                    Kind.ERROR,
                    "Since Object is the super type of NotProvided any optional parameter declaration of type Object " +
                            "must have additional guards to check whether this specialization should be called, " +
                            "or must make it clear in the parameter name (by using unused or maybe prefix) " +
                            "that it may not have been provided or is not used.",
                    parameter);
        }

    }

    private static boolean isGuarded(String name, String[] guards) {
        for (String guard : guards) {
            if (guard.equals("wasProvided(" + name + ")") ||
                    guard.equals("wasNotProvided(" + name + ")") ||
                    guard.equals("isNil(" + name + ")")) {
                return true;
            }
        }
        return false;
    }

    private void processPrimitive(
            PrintStream stream,
            StringBuilder rubyPrimitives,
            TypeElement element,
            TypeElement klass,
            Primitive primitive) {
        List<String> argumentNames = getArgumentNames(klass, primitive.argumentNames(), false, -1);

        final String nodeFactory = nodeFactoryName(element, klass);
        stream.println("        primitiveManager.addLazyPrimitive(" +
                quote(primitive.name()) + ", " + quote(nodeFactory) + ");");

        final StringJoiner arguments = new StringJoiner(", ");
        for (String argument : argumentNames) {
            arguments.add(argument);
        }

        rubyPrimitives
                .append("  def self.")
                .append(primitive.name())
                .append("(")
                .append(arguments)
                .append(")")
                .append('\n');
        rubyPrimitives.append("    # language=java").append('\n');
        rubyPrimitives
                .append("    /** @see ")
                .append(klass.getQualifiedName().toString())
                .append(" */")
                .append('\n');
        rubyPrimitives.append("  end").append('\n');
        rubyPrimitives.append('\n');
    }

    private void processCoreMethod(
            PrintStream stream,
            PrintStream rubyStream,
            TypeElement element,
            CoreModule coreModule,
            TypeElement klass,
            CoreMethod coreMethod) {
        final StringJoiner names = new StringJoiner(", ");
        for (String name : coreMethod.names()) {
            names.add(quote(name));
        }
        // final String className = klass.getQualifiedName().toString();
        final String nodeFactory = nodeFactoryName(element, klass);
        final boolean onSingleton = coreMethod.onSingleton() || coreMethod.constructor();
        stream.println("        coreMethodManager.addLazyCoreMethod(" + quote(nodeFactory) + ",");
        stream.println("                " +
                quote(coreModule.value()) + ", " +
                coreModule.isClass() + ", " +
                "Visibility." + coreMethod.visibility().name() + ", " +
                coreMethod.isModuleFunction() + ", " +
                onSingleton + ", " +
                coreMethod.neverSplit() + ", " +
                coreMethod.required() + ", " +
                coreMethod.optional() + ", " +
                coreMethod.rest() + ", " +
                (coreMethod.keywordAsOptional().isEmpty()
                        ? "null"
                        : quote(coreMethod.keywordAsOptional())) +
                ", " +
                names + ");");

        final boolean hasSelfArgument = !coreMethod.onSingleton() && !coreMethod.constructor() &&
                !coreMethod.isModuleFunction() && coreMethod.needsSelf();

        int numberOfArguments = getNumberOfArguments(coreMethod);
        String[] argumentNamesFromAnnotation = coreMethod.argumentNames();
        final List<String> argumentNames = getArgumentNames(
                klass,
                argumentNamesFromAnnotation,
                hasSelfArgument,
                numberOfArguments);

        if (argumentNames.isEmpty() && numberOfArguments > 0) {
            processingEnv.getMessager().printMessage(
                    Kind.ERROR,
                    "Did not find argument names. If the class has inherited Specializations use org.truffleruby.builtins.CoreMethod.argumentNames",
                    klass);

            for (int i = 0; i < coreMethod.required(); i++) {
                argumentNames.add("req" + (i + 1));
            }
            for (int i = 0; i < coreMethod.optional(); i++) {
                argumentNames.add("opt" + (i + 1));
            }
            if (coreMethod.rest()) {
                argumentNames.add("args");
            }
            if (coreMethod.needsBlock()) {
                argumentNames.add("block");
            }
        }

        int index = 0;

        final StringJoiner args = new StringJoiner(", ");
        for (int i = 0; i < coreMethod.required(); i++) {
            args.add(argumentNames.get(index));
            index++;
        }
        for (int i = 0; i < coreMethod.optional(); i++) {
            args.add(argumentNames.get(index) + " = nil");
            index++;
        }
        if (coreMethod.rest()) {
            args.add("*" + argumentNames.get(index));
            index++;
        }
        if (!coreMethod.keywordAsOptional().isEmpty()) {
            // TODO (pitr-ch 03-Oct-2019): check interaction with names, or remove it
            args.add(coreMethod.keywordAsOptional() + ": :unknown_default_value");
        }
        if (coreMethod.needsBlock()) {
            args.add("&" + argumentNames.get(index));
        }

        rubyStream.println("  def " + (onSingleton ? "self." : "") + coreMethod.names()[0] + "(" + args + ")");
        rubyStream.println("    # language=java");
        rubyStream.println("    /** @see " + klass.getQualifiedName().toString() + " */");
        rubyStream.println("  end");

        for (int i = 1; i < coreMethod.names().length; i++) {
            rubyStream.println("  alias_method :" + coreMethod.names()[i] + ", :" + coreMethod.names()[0]);
        }
        if (coreMethod.isModuleFunction()) {
            rubyStream.println("  module_function :" + coreMethod.names()[0]);
        }
        rubyStream.println();
    }

    private List<String> getArgumentNames(
            TypeElement klass, String[] argumentNamesFromAnnotation, boolean hasSelfArgument, int numberOfArguments) {

        List<String> argumentNames;
        if (argumentNamesFromAnnotation.length == 0) {
            argumentNames = getArgumentNamesFromSpecializations(klass, hasSelfArgument);
        } else {
            if (argumentNamesFromAnnotation.length != numberOfArguments && numberOfArguments >= 0) {
                processingEnv.getMessager().printMessage(
                        Kind.ERROR,
                        "The size of argumentNames does not match declared number of arguments.",
                        klass);
                argumentNames = new ArrayList<>();
            } else {
                argumentNames = Arrays.asList(argumentNamesFromAnnotation);
            }
        }
        return argumentNames;
    }

    private int getNumberOfArguments(CoreMethod coreMethod) {
        return coreMethod.required() + coreMethod.optional() + (coreMethod.rest() ? 1 : 0) +
                (coreMethod.needsBlock() ? 1 : 0);
    }

    private List<String> getArgumentNamesFromSpecializations(TypeElement klass, boolean hasSelfArgument) {
        List<String> argumentNames = new ArrayList<>();
        List<VariableElement> argumentElements = new ArrayList<>();

        for (Element el : klass.getEnclosedElements()) {
            if (!(el instanceof ExecutableElement)) {
                continue; // we are interested only in executable elements
            }

            final ExecutableElement executableElement = (ExecutableElement) el;

            if (executableElement.getAnnotation(Specialization.class) == null) {
                continue; // we are interested only in Specialization methods
            }

            boolean addingArguments = argumentNames.isEmpty();

            int index = 0;
            boolean skippedSelf = false;
            for (VariableElement parameter : executableElement.getParameters()) {
                if (!parameter.getAnnotationMirrors().isEmpty()) {
                    continue; // we ignore arguments having annotations like @Cached
                }

                if (processingEnv.getTypeUtils().isSameType(parameter.asType(), virtualFrameType)) {
                    continue;
                }

                if (hasSelfArgument && !skippedSelf) {
                    skippedSelf = true;
                    continue;
                }

                String nameCanBeKeyword = parameter
                        .getSimpleName()
                        .toString()
                        .replaceAll("(.)(\\p{Upper})", "$1_$2")
                        .toLowerCase()
                        .replaceAll("^(maybe|unused)_", "");
                String name = KEYWORDS.contains(nameCanBeKeyword) ? nameCanBeKeyword + "_" : nameCanBeKeyword;


                if (addingArguments) {
                    argumentNames.add(name);
                    argumentElements.add(parameter);
                } else {
                    if (!argumentNames.get(index).equals(name)) {
                        processingEnv.getMessager().printMessage(
                                Kind.ERROR,
                                "The argument does not match with the first occurrence of this argument which was '" +
                                        argumentElements.get(index).getSimpleName() +
                                        "' (translated to Ruby as '" + argumentNames.get(index) + "').",
                                parameter);
                    }
                }
                index++;
            }
        }

        return argumentNames;
    }

    private boolean anyCoreMethod(List<? extends Element> enclosedElements) {
        for (Element e : enclosedElements) {
            if (e instanceof TypeElement && e.getAnnotation(CoreMethod.class) != null) {
                return true;
            }
        }
        return false;
    }

    private String nodeFactoryName(TypeElement element, TypeElement klass) {
        return element.getQualifiedName() + "Factory$" + klass.getSimpleName() + "Factory";
    }

    private static String quote(String str) {
        return '"' + str + '"';
    }

}
