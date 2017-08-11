/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.processor;

import java.io.IOException;
import java.io.PrintStream;
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
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.Primitive;

@SupportedAnnotationTypes("org.truffleruby.builtins.CoreClass")
public class TruffleRubyProcessor extends AbstractProcessor {

    private static final String SUFFIX = "Builtins";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (!annotations.isEmpty()) {
            for (Element element : roundEnvironment.getElementsAnnotatedWith(CoreClass.class)) {
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
        final CoreClass coreClass = element.getAnnotation(CoreClass.class);

        final PackageElement packageElement = (PackageElement) element.getEnclosingElement();
        final String packageName = packageElement.getQualifiedName().toString();

        final JavaFileObject output = processingEnv.getFiler().createSourceFile(element.getQualifiedName() + SUFFIX, element);

        try (PrintStream stream = new PrintStream(output.openOutputStream(), true, "UTF-8")) {
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
            stream.println("    public static void setup(CoreMethodNodeManager coreMethodManager, PrimitiveManager primitiveManager) {");

            for (Element e : enclosedElements) {
                if (e instanceof TypeElement) {
                    final TypeElement klass = (TypeElement) e;

                    final CoreMethod method = e.getAnnotation(CoreMethod.class);
                    if (method != null) {
                        final StringJoiner names = new StringJoiner(", ");
                        for (String name : method.names()) {
                            names.add(quote(name));
                        }
                        // final String className = klass.getQualifiedName().toString();
                        final String nodeFactory = nodeFactoryName(element, klass);
                        final boolean onSingleton = method.onSingleton() || method.constructor();
                        stream.println("        coreMethodManager.addLazyCoreMethod(" + quote(nodeFactory) + ",");
                        stream.println("                " +
                                quote(coreClass.value()) + ", " +
                                "Visibility." + method.visibility().name() + ", " +
                                method.isModuleFunction() + ", " +
                                onSingleton + ", " +
                                method.required() + ", " +
                                method.optional() + ", " +
                                method.rest() + ", " +
                                names + ");");
                    }

                    final Primitive primitive = e.getAnnotation(Primitive.class);
                    if (primitive != null) {
                        // final String className = klass.getQualifiedName().toString();
                        final String nodeFactory = nodeFactoryName(element, klass);
                        stream.println("        primitiveManager.addLazyPrimitive(" +
                                quote(primitive.name()) + ", " + quote(nodeFactory) + ");");
                    }
                }
            }
            stream.println("    }");
            stream.println();
            stream.println("}");
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
