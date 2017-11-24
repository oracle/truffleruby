# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

suite = {
    "mxversion": "5.128.1",
    "name": "truffleruby",

    "imports": {
        "suites": [
            {
                "name": "truffle",
                "subdir": True,
                "version": "d1bb9076f1fa6af71c60be140f980794596a75b4",
                "urls": [
                    {"url": "https://github.com/graalvm/graal.git", "kind": "git"},
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
        "Apache-2.0": {
            "name": "Apache License 2.0",
            "url": "https://opensource.org/licenses/Apache-2.0"
        },
        "GPLv2": {
            "name": "GNU General Public License, version 2",
            "url": "http://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html"
        },
        "zlib": {
            "name": "The zlib License",
            "url": "https://opensource.org/licenses/zlib"
        },
    },

    "repositories": {
        "truffleruby-binary-snapshots": {
            "url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots",
            "licenses": ["EPL-1.0", "BSD-simplified", "BSD-new", "MIT", "Apache-2.0", "GPLv2", "LGPLv21", "zlib"]
        },
    },

    "libraries": {

        # ------------- Libraries -------------

        "JNR_CONSTANTS": {
            "maven": {
                "groupId": "com.github.jnr",
                "artifactId": "jnr-constants",
                "version": "0.9.6"
            },
            "sha1": "84955256aa28919f12b6c7c9437ed65d814a3c0c",
            "sourceSha1": "5579ab41c687085e714fc330536ec4dda3350b08",
            "licence": "Apache-2.0"
        },

        "SNAKEYAML": {
            "maven": {
                "groupId": "org.yaml",
                "artifactId": "snakeyaml",
                "version": "1.17"
            },
            "sha1": "7a27ea250c5130b2922b86dea63cbb1cc10a660c",
            "sourceSha1": "63577e87886c76228db9f8a2c50ea43cde5072eb",
            "licence": "Apache-2.0"
        },

        "JONI": {
            "maven": {
                "groupId": "org.jruby.joni",
                "artifactId": "joni",
                "version": "2.1.12"
            },
            "sha1": "3ba3cb50e9a0b87b645cd412a8f7b7296c314aa1",
            "sourceSha1": "2fdcca5bd2c629c8c120d46cc1ed325c994ea3eb",
            "licence": "MIT"
        },

        "JCODINGS": {
            "maven": {
                "groupId": "org.jruby.jcodings",
                "artifactId": "jcodings",
                "version": "1.0.26"
            },
            "sha1": "179d05303c51e1e2b87c643147fac56c8437ccbb",
            "sourceSha1": "6b2043ee228ec5e2964d1cc5ada11d3ddbf8e9ff",
            "licence": "MIT"
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
            "license": ["EPL-1.0"],
        },

        "truffleruby-processor": {
            "dir": "src/processor",
            "sourceDirs": ["java"],
            "dependencies": [
                "truffleruby-annotations",
            ],
            "javaCompliance": "1.8",
            "workingSets": "TruffleRuby",
            "checkPackagePrefix": "false",
            "license": ["EPL-1.0"],
        },

        "truffleruby": {
            "dir": "src/main",
            "sourceDirs": ["java"],
            "dependencies": [
                "truffleruby-annotations",
                "truffleruby:TRUFFLERUBY-LAUNCHER",
                "truffle:TRUFFLE_API",
                "truffle:JLINE",
                "JNR_CONSTANTS",
                "SNAKEYAML",
                "JONI",
                "JCODINGS",
            ],
            "annotationProcessors": [
                "truffle:TRUFFLE_DSL_PROCESSOR",
                "TRUFFLERUBY-PROCESSOR",
            ],
            "javaCompliance": "1.8",
            "workingSets": "TruffleRuby",
            "findbugsIgnoresGenerated" : True,
            "checkPackagePrefix": "false",
            "license": ["EPL-1.0", "BSD-new", "BSD-simplified", "MIT", "Apache-2.0"],
        },

        "truffleruby-launcher": {
            "dir": "src/launcher",
            "sourceDirs": ["java"],
            "dependencies": [
                "sdk:GRAAL_SDK"
            ],
            "javaCompliance": "1.8",
            "workingSets": "TruffleRuby",
            "checkPackagePrefix": "false",
            "license": ["EPL-1.0"],
        },

        "truffleruby-core": {
            "class": "ArchiveProject",
            "outputDir": "src/main/ruby",
            "prefix": "truffleruby",
            "license": ["EPL-1.0", "BSD-new"],
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
            "license": "EPL-1.0",
        },

        "truffleruby-test-ruby": {
            "class": "ArchiveProject",
            "outputDir": "src/test/ruby",
            "prefix": "src/test/ruby",
            "license": "EPL-1.0",
        },

        "truffleruby-cext": {
            "native": True,
            "dir": "src/main/c",
            "buildDependencies": [
                "TRUFFLERUBY", # We need truffleruby.jar to run extconf.rb
                "truffleruby-bin", # And bin/truffleruby
            ],
            "output": ".",
            "results": [], # Empty results as they overlap with truffleruby-lib
            "license": ["EPL-1.0", "GPLv2", "LGPLv21", "BSD-simplified"],
        },

        "truffleruby-lib": {
            "class": "ArchiveProject",
            "dependencies": [
                "truffleruby-cext",
            ],
            "outputDir": "lib",
            "prefix": "lib",
            "license": ["EPL-1.0", "MIT", "BSD-simplified", "GPLv2", "LGPLv21", "zlib"],
        },

        "truffleruby-bin": {
            "class": "TruffleRubyLauncherProject",
            "outputDir": "bin",
            "prefix": "bin",
            "license": ["EPL-1.0", "GPLv2", "LGPLv21"],
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
            "license": ["EPL-1.0", "MIT"],
        },
    },

    "distributions": {

        # ------------- Distributions -------------

        "TRUFFLERUBY-PROCESSOR": {
            "dependencies": [
                "truffleruby-processor"
            ],
            "description": "TruffleRuby Annotation Processor",
            "license": ["EPL-1.0"]
        },

        "TRUFFLERUBY": {
            "mainClass": "org.truffleruby.Main",
            "dependencies": [
                "truffleruby",
                "truffleruby-core",
            ],
            "distDependencies": [
                "truffle:TRUFFLE_API",
                "truffle:TRUFFLE_NFI",
                "truffleruby:TRUFFLERUBY-LAUNCHER",
            ],
            "description": "TruffleRuby",
            "license": ["EPL-1.0", "BSD-new", "BSD-simplified", "MIT", "Apache-2.0"],
        },

        "TRUFFLERUBY-LAUNCHER": {
            "dependencies": [
                "truffleruby-launcher"
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
            ],
            "description": "TruffleRuby Launcher",
            "license": ["EPL-1.0"]
        },

        # Set of extra files to extract to run Ruby
        "TRUFFLERUBY-ZIP": {
            "native": True, # Not Java
            "relpath": True,
            "platformDependent": True, # truffleruby-cext
            "dependencies": [
                "truffleruby-bin",
                "truffleruby-lib",
                "truffleruby-doc",
            ],
            "description": "TruffleRuby libraries, documentation, bin directory",
            "license": ["EPL-1.0", "MIT", "BSD-simplified", "GPLv2", "LGPLv21", "zlib"],
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
            "license": "EPL-1.0",
        },

        "TRUFFLERUBY-SPECS": {
            "native": True, # Not Java
            "relpath": True,
            "dependencies": [
                "truffleruby-specs",
            ],
            "description": "TruffleRuby spec files from ruby/spec",
            "license": ["EPL-1.0", "MIT"],
        },
    },
}
