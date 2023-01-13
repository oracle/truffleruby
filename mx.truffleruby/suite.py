suite = {
    "mxversion": "6.11.3",
    "name": "truffleruby",

    "imports": {
        "suites": [
            {
                "name": "regex",
                "subdir": True,
                "version": "f9c33591c9d79610625fda5ccee1a2eace88daa0",
                "urls": [
                    {"url": "https://github.com/oracle/graal.git", "kind": "git"},
                    {"url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind": "binary"},
                ]
            },
            {
                "name": "sulong",
                "subdir": True,
                "version": "f9c33591c9d79610625fda5ccee1a2eace88daa0",
                "urls": [
                    {"url": "https://github.com/oracle/graal.git", "kind": "git"},
                    {"url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind": "binary"},
                ]
            },
        ],
    },

    "licenses": {
        "EPL-2.0": {
            "name": "Eclipse Public License 2.0",
            "url": "https://opensource.org/licenses/EPL-2.0",
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
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
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
                "version": "2.1.41"
            },
            "digest": "sha512:88728bb3b6cf8b385e3a4639981ab8a54e977291cbb1d8b60fddd3a3381d9d5f356faa46b6b15b9ccf6ed4a2a42552152a254bfeab60f8b2ddac4959300212ac",
            "sourceDigest": "sha512:8502efcc9ab4d31aa92eb9c279f02b9cb1ab2d9c65660635a84d9ef6e773b05ebae026043ed7749a41dc0ee8b5f822d2e88ba44b3f6e244b145d61f2095e8d77",
            "license": ["MIT"],
        },

        "JCODINGS": {
            "maven": {
                "groupId": "org.jruby.jcodings",
                "artifactId": "jcodings",
                "version": "1.0.55"
            },
            "digest": "sha512:791c1442ca0aad9132f5d2d641f9bacf5b461e11fa2f8d2010af0de77ee2629a330c739004699bb987e331798e1eea06b5b6b15b7e7dd6d41f3322edce805d6b",
            "sourceDigest": "sha512:8935def3d670972d1d82a981fe3fbd999abffac07350c46419f1e542c4529f00c949e3eef442321fce23941b648cb09de157114ec751c0ef85c7ccfa6b719445",
            "license": ["MIT"],
        },

        "NETBEANS-LIB-PROFILER": {
            "moduleName": "org.netbeans.modules.org-netbeans-lib-profiler",
            "digest": "sha512:1de81a0340c0266b41ba774600346ac977910663016a0afa22859cf1eb9d9e507de4f66e3f51d5bd9575b1d1f083765ecb9b30c4d43adb201f68b83257e8a17d",
            "sourceDigest": "sha512:92c50b8832e3a9afc93f9eaacdfc79cdf2487a74a9f5cf93c54bed50e904ef70ac6504018558d7183f074132c37fe57b21bc1e662a71c74c4201b75cdc5f8947",
            "maven": {
              "groupId": "org.netbeans.modules",
              "artifactId": "org-netbeans-lib-profiler",
              "version": "RELEASE120-1",
            },
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
            "load_path": ["src/main/ruby/truffleruby/core"],
            "test": ["spec", "test"],
            "excluded": [
                "dumps",
                "logo",
                "mxbuild",
                "profiles",
                ".ext",
                "truffleruby-gem-test-pack",
                "lib/json/java",
                "lib/ruby",
                "test/truffle/ecosystem/blog6",
                "test/truffle/ecosystem/hello-world",
                "test/truffle/ecosystem/rails-app",
                "test/truffle/offline",
                "tool/docker",
                "rubyspec_temp",
            ]
        },
    },

    "projects": {

        # ------------- Projects -------------

        "org.truffleruby.annotations": {
            "dir": "src/annotations",
            "sourceDirs": ["java"],
            "jacoco": "include",
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "checkPackagePrefix": "false",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.shared": {
            "dir": "src/shared",
            "sourceDirs": ["java"],
            "requires": ["java.management"],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "sdk:GRAAL_SDK",
            ],
            "annotationProcessors": [
                "TRUFFLERUBY-PROCESSOR",
            ],
            "jacoco": "include",
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.processor": {
            "dir": "src/processor",
            "sourceDirs": ["java"],
            "requires": ["java.compiler"],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffle:TRUFFLE_API",
            ],
            "jacoco": "include",
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.services": {
            "dir": "src/services",
            "sourceDirs": ["java"],
            "requires": ["java.scripting"],
            "dependencies": [
                "sdk:GRAAL_SDK",
            ],
            "jacoco": "include",
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.rubysignal": {
            "dir": "src/main/c/rubysignal",
            "native": "shared_lib",
            "deliverable": "rubysignal",
            "buildDependencies": [
                "org.truffleruby", # for the generated JNI header file
            ],
            "use_jdk_headers": True, # the generated JNI header includes jni.h
            "cflags": ["-g", "-Wall", "-Werror", "-pthread"],
            "ldflags": ["-pthread"],
        },

        "org.truffleruby": {
            "dir": "src/main",
            "sourceDirs": ["java"],
            "jniHeaders": True,
            "requires": [
                "java.logging",
                "java.management",
                "jdk.management",
                "jdk.unsupported", # sun.misc.Signal
            ],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "truffle:TRUFFLE_API",
                "truffle:TRUFFLE_NFI",
                "sdk:JLINE3",
                "regex:TREGEX",
                "sulong:SULONG_API",
                "JONI",
                "JCODINGS",
            ],
            "annotationProcessors": [
                "truffle:TRUFFLE_DSL_PROCESSOR",
                "TRUFFLERUBY-PROCESSOR",
            ],
            "jacoco": "include",
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "findbugsIgnoresGenerated": True,
            "license": [
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
                "BSD-simplified",   # MRI
                "MIT",              # Joni, JCodings
            ],
        },

        "org.truffleruby.ruby": {
            "dir": "src/main/ruby",
            "sourceDirs": ["."],
            "javaCompliance": "17+",
            "license": [
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
            ],
            "externalProjects": {
                "core-library": {
                    "type": "ruby",
                    "path": "truffleruby",
                    "source": ["core", "post-boot"],
                    "load_path": ["core"]
                }
            }
        },

        "org.truffleruby.launcher": {
            "dir": "src/launcher",
            "sourceDirs": ["java"],
            "requires": ["java.logging"],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "sdk:GRAAL_SDK",
                "sdk:LAUNCHER_COMMON",
            ],
            "jacoco": "include",
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.test": {
            "dir": "src/test",
            "sourceDirs": ["java", "ruby"],
            "requires": ["java.scripting", "java.management", "jdk.management"],
            "dependencies": [
                "org.truffleruby",
                "org.truffleruby.services",
                "mx:JUNIT",
                "NETBEANS-LIB-PROFILER",
                "sdk:LAUNCHER_COMMON"
            ],
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.tck": {
            "testProject": True,
            "dir": "src/tck",
            "sourceDirs": ["java", "ruby"],
            "dependencies": ["truffle:TRUFFLE_TCK"],
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.bootstrap.launcher": {
            "class": "TruffleRubyBootstrapLauncherProject",
            "buildDependencies": [
                "TRUFFLERUBY", # We need this jar to run extconf.rb
                "TRUFFLERUBY-LAUNCHER", # We need this jar to run extconf.rb
                "sulong:SULONG_NATIVE", # We need this jar to find the toolchain with Toolchain#getToolPath
            ],
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.cext": {
            "native": True,
            "dir": "src/main/c",
            "buildDependencies": [
                "sulong:SULONG_BOOTSTRAP_TOOLCHAIN", # graalvm-native-clang
                "sulong:SULONG_HOME", # polyglot.h
                "TRUFFLERUBY-BOOTSTRAP-LAUNCHER",
            ],
            "buildEnv": {
                "TRUFFLERUBY_BOOTSTRAP_LAUNCHER": "<path:TRUFFLERUBY-BOOTSTRAP-LAUNCHER>/miniruby",
                "GRAALVM_TOOLCHAIN_CC": "<toolchainGetToolPath:native,CC>",
            },
            "output": ".",
            "results": [
                "src/main/c/spawn-helper/spawn-helper",
                "src/main/c/truffleposix/<lib:truffleposix>",
                "src/main/c/cext/<lib:truffleruby>",
                "src/main/c/bigdecimal/<extsuffix:bigdecimal>",
                "src/main/c/date/<extsuffix:date_core>",
                "src/main/c/etc/<extsuffix:etc>",
                "src/main/c/io-console/<extsuffix:console>",
                "src/main/c/nkf/<extsuffix:nkf>",
                "src/main/c/openssl/<extsuffix:openssl>",
                "src/main/c/psych/<extsuffix:psych>",
                "src/main/c/rbconfig-sizeof/<extsuffix:sizeof>",
                "src/main/c/ripper/<extsuffix:ripper>",
                "src/main/c/syslog/<extsuffix:syslog>",
                "src/main/c/zlib/<extsuffix:zlib>",
            ],
            "license": [
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-simplified",   # MRI
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
            "license": ["EPL-2.0"]
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
            "license": ["EPL-2.0"]
        },

        "TRUFFLERUBY-PROCESSOR": {
            "dependencies": [
                "org.truffleruby.processor"
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffle:TRUFFLE_API",
            ],
            "description": "TruffleRuby Annotation Processor",
            "license": ["EPL-2.0"],
        },

        "TRUFFLERUBY-SERVICES": {
            "moduleInfo": {
                "name": "org.truffleruby.services",
                "exports": ["org.truffleruby.services.scriptengine"],
            },
            "dependencies": [
                "org.truffleruby.services"
            ],
            "distDependencies": [
                "sdk:GRAAL_SDK",
            ],
            "description": "TruffleRuby services",
            "license": ["EPL-2.0"]
        },

        "TRUFFLERUBY": {
            "mainClass": "org.truffleruby.launcher.RubyLauncher",
            "dependencies": [
                "org.truffleruby",
                "org.truffleruby.ruby",
            ],
            "distDependencies": [
                "regex:TREGEX",
                "truffle:TRUFFLE_API",
                "truffle:TRUFFLE_NFI",
                "sulong:SULONG_API",
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
            ],
            "description": "TruffleRuby",
            "license": [
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
                "BSD-simplified",   # MRI
                "MIT",              # Joni, JCodings
            ],
        },

        "TRUFFLERUBY-BOOTSTRAP-LAUNCHER": {
            "native": True,
            "layout": {
                "./": "dependency:org.truffleruby.bootstrap.launcher/*",
            },
            "description": "TruffleRuby Bootstrap Launcher to build core C extensions",
            "license": ["EPL-2.0"],
        },

        "TRUFFLERUBY-LAUNCHER": {
            "dependencies": [
                "org.truffleruby.launcher"
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "sdk:GRAAL_SDK",
                "sdk:LAUNCHER_COMMON",
            ],
            "description": "TruffleRuby Launcher",
            "license": ["EPL-2.0"],
        },

        "TRUFFLERUBY_GRAALVM_SUPPORT": {
            "fileListPurpose": 'native-image-resources',
            "native": True,
            "platformDependent": True,
            "description": "TruffleRuby support distribution for the GraalVM",
            "layout": {
                "lib/": [
                    "file:lib/json",
                    "file:lib/mri",
                    "file:lib/patches",
                    "file:lib/truffle",
                ],
                "lib/cext/": [
                    "file:lib/cext/*.rb",
                    "file:lib/cext/ABI_version.txt",
                    "dependency:org.truffleruby.cext/src/main/c/truffleposix/<lib:truffleposix>",
                    "dependency:org.truffleruby.cext/src/main/c/cext/<lib:truffleruby>",
                    "dependency:org.truffleruby.rubysignal",
                ],
                "lib/cext/include/": [
                    "file:lib/cext/include/*",
                ],
                "lib/gems/": [
                    {
                        "source_type": "file",
                        "path": "lib/gems/*",
                        "exclude": [
                            "lib/gems/gems/debug-*/ext",
                            "lib/gems/gems/rbs-*/ext",
                        ],
                    },
                ],
                "lib/mri/": [
                    "dependency:org.truffleruby.cext/src/main/c/bigdecimal/<extsuffix:bigdecimal>",
                    "dependency:org.truffleruby.cext/src/main/c/date/<extsuffix:date_core>",
                    "dependency:org.truffleruby.cext/src/main/c/etc/<extsuffix:etc>",
                    "dependency:org.truffleruby.cext/src/main/c/nkf/<extsuffix:nkf>",
                    "dependency:org.truffleruby.cext/src/main/c/openssl/<extsuffix:openssl>",
                    "dependency:org.truffleruby.cext/src/main/c/psych/<extsuffix:psych>",
                    "dependency:org.truffleruby.cext/src/main/c/ripper/<extsuffix:ripper>",
                    "dependency:org.truffleruby.cext/src/main/c/syslog/<extsuffix:syslog>",
                    "dependency:org.truffleruby.cext/src/main/c/zlib/<extsuffix:zlib>",
                ],
                "lib/mri/io/": [
                    "dependency:org.truffleruby.cext/src/main/c/io-console/<extsuffix:console>",
                ],
                "lib/mri/rbconfig/": [
                    "dependency:org.truffleruby.cext/src/main/c/rbconfig-sizeof/<extsuffix:sizeof>",
                ],
                "lib/truffle/": [
                    "dependency:org.truffleruby.cext/src/main/c/spawn-helper/spawn-helper",
                ],
            },
            "license": [
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "MIT",              # minitest, did_you_mean, rake
                "BSD-simplified",   # MRI
                "BSD-new",          # Rubinius, FFI
            ],
        },

        "TRUFFLERUBY_GRAALVM_SUPPORT_NO_NI_RESOURCES": {
            "native": True,
            "platformDependent": True,
            "description": "TruffleRuby support distribution for the GraalVM, the contents is not included as native image resources.",
            "layout": {
                "./": [
                    "file:CHANGELOG.md",
                    "file:README.md",
                    "file:mx.truffleruby/native-image.properties",
                ],
                "bin/": [
                    "file:exe/*",
                ],
                "doc/": [
                    "file:doc/user",
                    "file:doc/legal",
                ],
                "logo/": [
                    "file:logo/ATTRIBUTION.md",
                    "file:logo/LICENSE.txt",
                ],
                "logo/png/": [
                    "file:logo/png/truffleruby_logo_horizontal_medium.png",
                ],
                "src/main/c/openssl/": [
                    "file:src/main/c/openssl/extconf.rb",
                    "file:src/main/c/openssl/*.c",
                    "file:src/main/c/openssl/ossl*.h",
                    "file:src/main/c/openssl/openssl_missing.h",
                ],
            },
        },

        "TRUFFLERUBY_GRAALVM_LICENSES": {
            "fileListPurpose": 'native-image-resources',
            "native": True,
            "platformDependent": True,
            "description": "TruffleRuby support distribution for the GraalVM license files",
            "layout": {
                "LICENSE_TRUFFLERUBY.txt": "file:LICENCE.md",
                "3rd_party_licenses_truffleruby.txt": "file:3rd_party_licenses.txt",
            },
        },

        "TRUFFLERUBY-TEST": {
            "testDistribution": True,
            "dependencies": [
                "org.truffleruby.test",
            ],
            "exclude": [
                "mx:HAMCREST",
                "mx:JUNIT"
            ],
            "distDependencies": [
                "NETBEANS-LIB-PROFILER",
                "sdk:LAUNCHER_COMMON",
                "TRUFFLERUBY",
                "TRUFFLERUBY-SERVICES",
                "TRUFFLERUBY_GRAALVM_SUPPORT",
            ],
            "javaProperties": {
                "org.graalvm.language.ruby.home": "<path:TRUFFLERUBY_GRAALVM_SUPPORT>"
            },
            "license": ["EPL-2.0"],
        },

        "TRUFFLERUBY-TCK": {
            "testDistribution": True,
            "dependencies": ["org.truffleruby.tck"],
            "distDependencies": ["truffle:TRUFFLE_TCK"],
            "javaProperties": {
                "org.graalvm.language.ruby.home": "<path:TRUFFLERUBY_GRAALVM_SUPPORT>"
            },
            "license": ["EPL-2.0"],
        },
    },
}
