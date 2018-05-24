suite = {
    "mxversion": "5.156.0",
    "name": "truffleruby",

    "imports": {
        "suites": [
            {
                "name": "truffle",
                "subdir": True,
                "version": "7d0f63af89a80ab94b8d4f6a0d4c3f9ef1d4b881",
                "urls": [
                    {"url": "https://github.com/graalvm/graal.git", "kind": "git"},
                    {"url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind": "binary"},
                ]
            },
            {
                "name": "sulong",
                "version": "cd88a1cb441971fb710e41fc7a5f4ac7a70794fd",
                "urls": [
                    {"url": "https://github.com/graalvm/sulong.git", "kind": "git"},
                    {"url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind": "binary"},
                ]
            },
        ],
    },

    "licenses": {
        "EPL-1.0": {
            "name": "Eclipse Public License 1.0",
            "url": "https://opensource.org/licenses/EPL-1.0",
        },
        "BSD-simplified": {
            "name": "Simplified BSD License (2-clause BSD license)",
            "url": "http://opensource.org/licenses/BSD-2-Clause"
        },
        "MIT": {
            "name": "MIT License",
            "url": "http://opensource.org/licenses/MIT"
        },
    },

    "repositories": {
        "truffleruby-binary-snapshots": {
            "url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots",
            "licenses": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-simplified",   # MRI
                "BSD-new",          # Rubinius, FFI
                "MIT",              # JCodings, minitest, did_you_mean, rake
            ]
        },
    },

    "libraries": {

        # ------------- Libraries -------------

        "JONI": {
            "maven": {
                "groupId": "org.jruby.joni",
                "artifactId": "joni",
                "version": "2.1.16"
            },
            "sha1": "b5c07f6aa52f93fe592dd9545b6b528e1a52c54d",
            "sourceSha1": "ab3c291fa9aa4caee512b6624b4d2302d91a7230",
            "license": [
                "MIT",              # Joni
            ],
        },

        "JCODINGS": {
            "maven": {
                "groupId": "org.jruby.jcodings",
                "artifactId": "jcodings",
                "version": "1.0.30"
            },
            "sha1": "1238908f91735a4201a2adabfc1026c84dc16598",
            "sourceSha1": "21c4b05215a0ee50d8791d3d01269b880f09852d",
            "license": [
                "MIT",              # JCodings
            ],
        },
    },

    "externalProjects": {
        "truffleruby-root": {
            "type": "ruby",
            "path": '.',
            "source": [
                "lib/json",
                "lib/mri",
                "lib/truffle",
            ],
            "load_path": ["src/main/ruby/core"],
            "test": ["spec", "test"],
            "excluded": [
                "bench",
                "dumps",
                "logo",
                "mxbuild",
                "truffleruby-gem-test-pack",
                "lib/ruby",
                "test/truffle/ecosystem/blog",
                "test/truffle/ecosystem/hello-world",
                "test/truffle/ecosystem/rails-app",
            ]
        },
    },

    "projects": {

        # ------------- Projects -------------

        "truffleruby-annotations": {
            "dir": "src/annotations",
            "sourceDirs": ["java"],
            "javaCompliance": "1.8",
            "workingSets": "TruffleRuby",
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "truffleruby-shared": {
            "dir": "src/shared",
            "sourceDirs": ["java"],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "sdk:GRAAL_SDK",
            ],
            "annotationProcessors": [
                "TRUFFLERUBY-PROCESSOR",
            ],
            "javaCompliance": "1.8",
            "workingSets": "TruffleRuby",
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "truffleruby-processor": {
            "dir": "src/processor",
            "sourceDirs": ["java"],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
            ],
            "javaCompliance": "1.8",
            "workingSets": "TruffleRuby",
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "truffleruby": {
            "dir": "src/main",
            "sourceDirs": ["java"],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "truffle:TRUFFLE_API",
                "truffle:JLINE",
                "JONI",
                "JCODINGS",
            ],
            "annotationProcessors": [
                "truffle:TRUFFLE_DSL_PROCESSOR",
                "TRUFFLERUBY-PROCESSOR",
            ],
            "javaCompliance": "1.8",
            "checkstyle" : "truffleruby",
            "workingSets": "TruffleRuby",
            "findbugsIgnoresGenerated" : True,
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
                "BSD-simplified",   # MRI
                "MIT",              # Joni, JCodings
            ],
            "externalProjects": {
                "ruby-core" : {
                    "type": "ruby",
                    "path": "ruby",
                    "source": ["core", "post-boot"],
                    "load_path": ["core"]
                }
            }
        },

        "truffleruby-launcher": {
            "dir": "src/launcher",
            "sourceDirs": ["java"],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "sdk:GRAAL_SDK",
                "sdk:LAUNCHER_COMMON",
            ],
            "javaCompliance": "1.8",
            "workingSets": "TruffleRuby",
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "truffleruby-core": {
            "class": "ArchiveProject",
            "outputDir": "src/main/ruby",
            "prefix": "truffleruby",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
            ],
        },

        "truffleruby-test": {
            "dir": "src/test",
            "sourceDirs": ["java"],
            "dependencies": [
                "truffleruby",
                "truffle:TRUFFLE_TCK",
                "mx:JUNIT",
            ],
            "javaCompliance": "1.8",
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "truffleruby-test-ruby": {
            "class": "ArchiveProject",
            "outputDir": "src/test/ruby",
            "prefix": "src/test/ruby",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "truffleruby-cext": {
            "native": True,
            "dir": "src/main/c",
            "buildDependencies": [
                "TRUFFLERUBY", # We need truffleruby.jar to run extconf.rb
                "truffleruby-bin", # bin/truffleruby
                "truffleruby-sulong-libs", # polyglot.h
            ],
            "output": ".",
            "results": [], # Empty results as they overlap with truffleruby-lib
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-simplified",   # MRI
            ],
        },

        # Copy the files from SULONG_LIBS to lib/cext/sulong-libs.
        # Used by native images, which need a relative path from the Ruby home
        # to these libraries to pass to Sulong so it can find them outside GraalVM.
        "truffleruby-sulong-libs": {
            "class": "TruffleRubySulongLibsProject",
            "outputDir": "lib/cext/sulong-libs",
            "prefix": "lib/cext/sulong-libs",
            "buildDependencies": [
                "sulong:SULONG_LIBS",
            ],
        },

        "truffleruby-lib": {
            "class": "ArchiveProject",
            "dependencies": [
                "truffleruby-cext",
                "truffleruby-sulong-libs",
            ],
            "outputDir": "lib",
            "prefix": "lib",
            "license": [
                "EPL-1.0",
                "MIT",              # minitest, did_you_mean, rake
                "BSD-simplified",   # MRI
                "BSD-new",          # Rubinius, FFI and RubySL
            ],
        },

        "truffleruby-bin": {
            "class": "TruffleRubyLauncherProject",
            "buildDependencies": [
                "TRUFFLERUBY",
                "TRUFFLERUBY-LAUNCHER",
                "sulong:SULONG",
            ],
            "outputDir": "bin",
            "prefix": "bin",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "truffleruby-doc": {
            "class": "TruffleRubyDocsProject",
            "outputDir": "",
            "prefix": "",
        },

        "truffleruby-specs": {
            "class": "ArchiveProject",
            "prefix": "spec",
            "outputDir": "spec",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "MIT",              # Ruby Specs
            ],
        },
    },

    "distributions": {

        # ------------- Distributions -------------

        "TRUFFLERUBY-ANNOTATIONS": {
            "dependencies": [
                "truffleruby-annotations"
            ],
            "description": "TruffleRuby Annotations",
            "license": ["EPL-1.0"]
        },

        # Required to share code between the launcher and the rest,
        # since the rest cannot depend on the launcher and the shared code cannot be there.
        # This code is loaded twice in different classloaders, therefore any created instances should not be passed around.
        "TRUFFLERUBY-SHARED": {
            "dependencies": [
                "truffleruby-shared"
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "sdk:GRAAL_SDK",
            ],
            "description": "TruffleRuby Shared constants and predicates",
            "license": ["EPL-1.0"]
        },

        "TRUFFLERUBY-PROCESSOR": {
            "dependencies": [
                "truffleruby-processor"
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
            ],
            "description": "TruffleRuby Annotation Processor",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "TRUFFLERUBY": {
            "mainClass": "org.truffleruby.launcher.RubyLauncher",
            "dependencies": [
                "truffleruby",
                "truffleruby-core",
            ],
            "distDependencies": [
                "truffle:TRUFFLE_API",
                "truffle:TRUFFLE_NFI",
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
            ],
            "description": "TruffleRuby",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
                "BSD-simplified",   # MRI
                "MIT",              # Joni, JCodings
            ],
        },

        "TRUFFLERUBY-LAUNCHER": {
            "dependencies": [
                "truffleruby-launcher"
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "sdk:GRAAL_SDK",
                "sdk:LAUNCHER_COMMON",
            ],
            "description": "TruffleRuby Launcher",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        # Set of extra files to extract to run Ruby
        "TRUFFLERUBY-ZIP": {
            "native": True, # Not Java
            "relpath": True,
            "platformDependent": True, # truffleruby-cext, truffleruby-bin
            "dependencies": [
                "truffleruby-bin",
                "truffleruby-lib",
                "truffleruby-doc",
            ],
            "description": "TruffleRuby libraries, documentation, bin directory",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "MIT",              # minitest, did_you_mean, rake
                "BSD-simplified",   # MRI
                "BSD-new",          # Rubinius, FFI
            ],
        },

        "TRUFFLERUBY_GRAALVM_SUPPORT" : {
            "native": True,
            "platformDependent": True,
            "description" : "TruffleRuby support distribution for the GraalVM",
            "dependencies" : [
                "truffleruby-cext",
            ],
            "layout" : {
                "./" : [
                    "file:lib",  # contains some results from truffleruby-cext
                    "file:CHANGELOG.md",
                    "file:mx.truffleruby/GraalCE_Ruby_license_3rd_party_license.txt",
                    "file:README.md",
                    "file:mx.truffleruby/native-image.properties",
                ],
                "bin/" : [
                    "file:bin/gem",
                    "file:bin/irb",
                    "file:bin/rake",
                    "file:bin/rdoc",
                    "file:bin/ri",
                    "file:bin/testrb",
                ],
                "doc/" : [
                    "file:doc/legal",
                    "file:doc/user",
                ],
                "src/main/c/openssl/": [
                    "file:src/main/c/openssl/deprecation.rb",
                    "file:src/main/c/openssl/extconf.rb",
                    "file:src/main/c/openssl/*.c",
                    {
                        "source_type": "file",
                        "path": "src/main/c/openssl/*.h",
                        "exclude": ["src/main/c/openssl/extconf.h"]
                    },
                ],
            },
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "MIT",              # minitest, did_you_mean, rake
                "BSD-simplified",   # MRI
                "BSD-new",          # Rubinius, FFI
            ],
        },

        "TRUFFLERUBY-TEST": {
            "dependencies": [
                "truffleruby-test",
                "truffleruby-test-ruby",
            ],
            "exclude": [
                "mx:HAMCREST",
                "mx:JUNIT"
            ],
            "distDependencies": [
                "TRUFFLERUBY",
                "truffle:TRUFFLE_TCK"
            ],
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "TRUFFLERUBY-SPECS": {
            "native": True, # Not Java
            "relpath": True,
            "dependencies": [
                "truffleruby-specs",
            ],
            "description": "TruffleRuby spec files from ruby/spec",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "MIT",              # Ruby Specs
            ],
        },
    },
}
