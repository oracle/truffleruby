/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.processor;

import org.truffleruby.PopulateBuildInformation;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes("org.truffleruby.PopulateBuildInformation")
public class BuildInformationProcessor extends AbstractProcessor {

    private static final String SUFFIX = "Impl";

    private final Set<String> processed = new HashSet<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (!annotations.isEmpty()) {
            for (Element element : roundEnvironment.getElementsAnnotatedWith(PopulateBuildInformation.class)) {
                try {
                    processBuildInformation((TypeElement) element);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, e.getClass() + " " + e.getMessage(), element);
                }
            }
        }

        return true;
    }

    private void processBuildInformation(TypeElement element) throws IOException {
        final PackageElement packageElement = (PackageElement) element.getEnclosingElement();
        final String packageName = packageElement.getQualifiedName().toString();

        final String qualifiedName = element.getQualifiedName().toString();

        if (!processed.add(qualifiedName)) {
            // Already processed, do nothing. This seems an Eclipse bug.
            return;
        }

        final JavaFileObject output = processingEnv.getFiler().createSourceFile(qualifiedName + SUFFIX, element);

        try (PrintStream stream = new PrintStream(output.openOutputStream(), true, "UTF-8")) {
            stream.println("package " + packageName + ";");
            stream.println();
            stream.println("public class " + element.getSimpleName() + SUFFIX + " implements " + element.getSimpleName() + " {");
            stream.println();
            stream.println("    public static final " + element.getSimpleName() + " INSTANCE = new " + element.getSimpleName() + SUFFIX + "();");
            stream.println();

            for (Element e : element.getEnclosedElements()) {
                if (e instanceof ExecutableElement) {
                    final String value;

                    switch (e.getSimpleName().toString()) {
                        case "getRevision":
                            value = getRevision();
                            break;
                        case "getCompileDate":
                            value = getCompileDate();
                            break;
                        default:
                            throw new UnsupportedOperationException(e.getSimpleName() + " method not understood");
                    }

                    stream.println("    @Override");
                    stream.println("    public String " + e.getSimpleName() + "() {");
                    stream.println("        return \"" + value + "\";");
                    stream.println("    }");
                }
            }

            stream.println();
            stream.println("}");
        }
    }

    public String getRevision() {
        return "f00fc3fc33";
    }

    public String getCompileDate() {
        return "2018-01-07";
    }


}
