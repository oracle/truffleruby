suite = {
    "mxversion": "5.156.0",
    "name": "truffleruby",

    "imports": {
        "suites": [
            { # Import only the tools suite which depends on truffle, to avoid duplicating import versions.
              # We want tools to be reliably available with TruffleRuby, even with "mx build", so this is a static import.
                "name": "tools",
                "subdir": True,
                # version must always be equal to the version of the "sulong" import below
                "version": "aacc6652e247841b2bfa9bdba308021049c2e215",
                "urls": [
                    {"url": "https://github.com/oracle/graal.git", "kind": "git"},
                    {"url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind": "binary"},
                ]
            },
            {
                "name": "sulong",
                "subdir": True,
                # version must always be equal to the version of the "tools" import above
                "version": "aacc6652e247841b2bfa9bdba308021049c2e215",
                "urls": [
                    {"url": "https://github.com/oracle/graal.git", "kind": "git"},
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
                "version": "2.1.25"
            },
            "sha1": "5dbb09787a9b8780737b71fbf942235ef59051b9",
            "sourceSha1": "505a09064f6e2209616f38724f6d97d8d889aa92",
            "license": [
                "MIT",              # Joni
            ],
        },

        "JCODINGS": {
            "maven": {
                "groupId": "org.jruby.jcodings",
                "artifactId": "jcodings",
                "version": "1.0.40"
            },
            "sha1": "2838952e91baa37ac73ed817451268a193ba440a",
            "sourceSha1": "0ed89e096c83d540acac00d6ee3ea935b4c905ff",
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

        "org.truffleruby.annotations": {
            "dir": "src/annotations",
            "sourceDirs": ["java"],
            "javaCompliance": "1.8",
            "workingSets": "TruffleRuby",
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "org.truffleruby.shared": {
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

        "org.truffleruby.processor": {
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

        "org.truffleruby.services": {
            "dir": "src/services",
            "sourceDirs": ["java"],
            "dependencies": [
                "sdk:GRAAL_SDK",
            ],
            "javaCompliance": "1.8",
            "workingSets": "TruffleRuby",
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "org.truffleruby": {
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
            "checkstyle" : "org.truffleruby",
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

        "org.truffleruby.launcher": {
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

        "org.truffleruby.core": {
            "class": "ArchiveProject",
            "outputDir": "src/main/ruby",
            "prefix": "truffleruby",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
            ],
        },

        "org.truffleruby.test": {
            "dir": "src/test",
            "sourceDirs": ["java"],
            "dependencies": [
                "org.truffleruby",
                "org.truffleruby.services",
                "truffle:TRUFFLE_TCK",
                "mx:JUNIT",
            ],
            "javaCompliance": "1.8",
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "org.truffleruby.test-ruby": {
            "class": "ArchiveProject",
            "outputDir": "src/test/ruby",
            "prefix": "src/test/ruby",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "org.truffleruby.cext": {
            "native": True,
            "dir": "src/main/c",
            "buildDependencies": [
                "TRUFFLERUBY", # We need truffleruby.jar to run extconf.rb
                "org.truffleruby.bin", # bin/truffleruby
                "org.truffleruby.sulong-libs", # polyglot.h
            ],
            "output": ".",
            "results": [], # Empty results as they overlap with org.truffleruby.lib
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-simplified",   # MRI
            ],
        },

        # Copy the files from SULONG_LIBS to lib/cext/sulong-libs.
        # Used by native images, which need a relative path from the Ruby home
        # to these libraries to pass to Sulong so it can find them outside GraalVM.
        "org.truffleruby.sulong-libs": {
            "class": "TruffleRubySulongLibsProject",
            "outputDir": "lib/cext/sulong-libs",
            "prefix": "lib/cext/sulong-libs",
            "buildDependencies": [
                "sulong:SULONG_LIBS",
            ],
        },

        "org.truffleruby.lib": {
            "class": "ArchiveProject",
            "dependencies": [
                "org.truffleruby.cext",
                "org.truffleruby.sulong-libs",
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

        "org.truffleruby.bin": {
            "class": "TruffleRubyLauncherProject",
            "buildDependencies": [
                "TRUFFLERUBY",
                "TRUFFLERUBY-LAUNCHER",
                "sulong:SULONG",
                "tools:CHROMEINSPECTOR",
                "tools:TRUFFLE_PROFILER",
            ],
            "outputDir": "bin",
            "prefix": "bin",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "org.truffleruby.doc": {
            "class": "TruffleRubyDocsProject",
            "outputDir": "",
            "prefix": "",
        },

        "org.truffleruby.specs": {
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
                "org.truffleruby.annotations"
            ],
            "description": "TruffleRuby Annotations",
            "license": ["EPL-1.0"]
        },

        # Required to share code between the launcher and the rest,
        # since the rest cannot depend on the launcher and the shared code cannot be there.
        # This code is loaded twice in different classloaders, therefore any created instances should not be passed around.
        "TRUFFLERUBY-SHARED": {
            "dependencies": [
                "org.truffleruby.shared"
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
                "org.truffleruby.processor"
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
            ],
            "description": "TruffleRuby Annotation Processor",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
            ],
        },

        "TRUFFLERUBY-SERVICES": {
            "dependencies": [
                "org.truffleruby.services"
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
            ],
            "description": "TruffleRuby services",
            "license": ["EPL-1.0"]
        },

        "TRUFFLERUBY": {
            "mainClass": "org.truffleruby.launcher.RubyLauncher",
            "dependencies": [
                "org.truffleruby",
                "org.truffleruby.core",
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
                "org.truffleruby.launcher"
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "truffleruby:TRUFFLERUBY-SERVICES",     # For the file type detection service
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
            "platformDependent": True, # org.truffleruby.cext, org.truffleruby.bin
            "dependencies": [
                "org.truffleruby.bin",
                "org.truffleruby.lib",
                "org.truffleruby.doc",
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
                "org.truffleruby.cext",
            ],
            "layout" : {
                "./" : [
                    "file:lib",  # contains some results from org.truffleruby.cext
                    "file:CHANGELOG.md",
                    "file:README.md",
                    "file:mx.truffleruby/native-image.properties",
                ],
                "LICENSE_TRUFFLERUBY.md" : "file:LICENCE.md",
                "3rd_party_licenses_truffleruby.txt" : "file:3rd_party_licenses.txt",
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
                "org.truffleruby.test",
                "org.truffleruby.test-ruby",
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
                "org.truffleruby.specs",
            ],
            "description": "TruffleRuby spec files from ruby/spec",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "MIT",              # Ruby Specs
            ],
        },
    },
}
