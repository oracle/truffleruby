/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.processor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.Primitive;

@SupportedAnnotationTypes("org.truffleruby.builtins.CoreModule")
public class CoreModuleProcessor extends AbstractProcessor {

    private static final String SUFFIX = "Builtins";

    private final Set<String> processed = new HashSet<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (!annotations.isEmpty()) {
            for (Element element : roundEnvironment.getElementsAnnotatedWith(CoreModule.class)) {
                try {
                    processCoreMethod((TypeElement) element);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, e.getClass() + " " + e.getMessage(), element);
                }
            }
        }

        return true;
    }

    private void processCoreMethod(TypeElement element) throws IOException {
        final CoreModule coreModule = element.getAnnotation(CoreModule.class);

        final PackageElement packageElement = (PackageElement) element.getEnclosingElement();
        final String packageName = packageElement.getQualifiedName().toString();

        final String qualifiedName = element.getQualifiedName().toString();
        if (!processed.add(qualifiedName)) {
            // Already processed, do nothing. This seems an Eclipse bug.
            return;
        }

        final JavaFileObject output = processingEnv.getFiler().createSourceFile(qualifiedName + SUFFIX, element);
        final FileObject rubyFile = processingEnv.getFiler().createResource(
                StandardLocation.SOURCE_OUTPUT,
                "core_class_stubs",
                coreModule.value().replace("::", "/") + ".rb",
                (Element[]) null);


        try (PrintStream stream = new PrintStream(output.openOutputStream(), true, "UTF-8")) {
            try (PrintStream rubyStream = new PrintStream(rubyFile.openOutputStream(), true, "UTF-8")) {

                final List<? extends Element> enclosedElements = element.getEnclosedElements();
                final boolean anyCoreMethod = anyCoreMethod(enclosedElements);

                stream.println("package " + packageName + ";");
                stream.println();
                stream.println("import org.truffleruby.builtins.CoreMethodNodeManager;");
                stream.println("import org.truffleruby.builtins.PrimitiveManager;");
                if (anyCoreMethod) {
                    stream.println("import org.truffleruby.language.Visibility;");
                }
                stream.println();
                stream.println("public class " + element.getSimpleName() + SUFFIX + " {");
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

                        final CoreMethod method = e.getAnnotation(CoreMethod.class);
                        if (method != null) {
                            processCoreMethod(stream, rubyStream, element, coreModule, klass, method);
                        }

                        final Primitive primitive = e.getAnnotation(Primitive.class);
                        if (primitive != null) {
                            // final String className = klass.getQualifiedName().toString();
                            final String nodeFactory = nodeFactoryName(element, klass);
                            stream.println("        primitiveManager.addLazyPrimitive(" +
                                    quote(primitive.name()) + ", " + quote(nodeFactory) + ");");

                            rubyPrimitives.append("  def self.").append(primitive.name()).append('\n');
                            rubyPrimitives.append("    # language=java").append('\n');
                            rubyPrimitives
                                    .append("    /** @see ")
                                    .append(klass.getQualifiedName().toString())
                                    .append(" */")
                                    .append('\n');
                            rubyPrimitives.append("  end").append('\n');
                            rubyPrimitives.append('\n');
                        }
                    }
                }

                stream.println("    }");
                stream.println();
                stream.println("}");

                rubyStream.println("end");
                rubyStream.println();

                // TODO (pitr-ch 28-Sep-2019): remove
                rubyStream.println("module TrufflePrimitive");
                rubyStream.print(rubyPrimitives);
                rubyStream.println("end");
                rubyStream.println();
            }
        }

    }

    private void processCoreMethod(
            PrintStream stream,
            PrintStream rubyStream,
            TypeElement element,
            CoreModule coreModule,
            TypeElement klass, CoreMethod method) {
        final StringJoiner names = new StringJoiner(", ");
        for (String name : method.names()) {
            names.add(quote(name));
        }
        // final String className = klass.getQualifiedName().toString();
        final String nodeFactory = nodeFactoryName(element, klass);
        final boolean onSingleton = method.onSingleton() || method.constructor();
        stream.println("        coreMethodManager.addLazyCoreMethod(" + quote(nodeFactory) + ",");
        stream.println("                " +
                quote(coreModule.value()) + ", " +
                coreModule.isClass() + ", " +
                "Visibility." + method.visibility().name() + ", " +
                method.isModuleFunction() + ", " +
                onSingleton + ", " +
                method.neverSplit() + ", " +
                method.required() + ", " +
                method.optional() + ", " +
                method.rest() + ", " +
                (method.keywordAsOptional().isEmpty()
                        ? "null"
                        : quote(method.keywordAsOptional())) +
                ", " +
                names + ");");

        final StringJoiner args = new StringJoiner(", ");
        for (int i = 0; i < method.required(); i++) {
            args.add("req" + (i + 1));
        }
        for (int i = 0; i < method.optional(); i++) {
            args.add("opt" + (i + 1) + "=nil");
        }
        if (method.rest()) {
            args.add("*args");
        }
        if (!method.keywordAsOptional().isEmpty()) {
            args.add(method.keywordAsOptional() + ": :unknown_default_value");
        }
        if (method.needsBlock()) {
            args.add("&block");
        }

        // keywordAsOptional is not handled since it's used only once

        rubyStream.println("  def " + (onSingleton ? "self." : "") + method.names()[0] + "(" + args + ")");
        rubyStream.println("    # language=java");
        rubyStream.println("    /** @see " + klass.getQualifiedName().toString() + " */");
        rubyStream.println("  end");
        rubyStream.println();

        boolean newLine = false;
        for (int i = 1; i < method.names().length; i++) {
            rubyStream.println("  alias_method :" + method.names()[i] + ", :" + method.names()[0]);
            newLine = true;
        }
        if (method.isModuleFunction()) {
            rubyStream.println("  module_function :" + method.names()[0]);
            newLine = true;
        }
        if (newLine) {
            rubyStream.println();
        }
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
