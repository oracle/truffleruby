suite = {
    "mxversion": "6.44.0",
    "name": "truffleruby",
    "groupId": "org.graalvm.ruby",
    "url": "https://www.graalvm.org/ruby/",
    "developer": {
        "name": "GraalVM Development",
        "email": "graalvm-dev@oss.oracle.com",
        "organization": "Oracle Corporation",
        "organizationUrl": "http://www.graalvm.org/",
    },
    "scm": {
        "url": "https://github.com/oracle/truffleruby",
        "read": "https://github.com/oracle/truffleruby.git",
        "write": "git@github.com:oracle/truffleruby.git",
    },

    "imports": {
        "suites": [
            {
                "name": "regex",
                "subdir": True,
                "version": "20b34173b7e15c334d79f0a22ee01889d235748b",
                "urls": [
                    {"url": "https://github.com/oracle/graal.git", "kind": "git"},
                    {"url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind": "binary"},
                ]
            },
            {
                "name": "sulong",
                "subdir": True,
                "version": "20b34173b7e15c334d79f0a22ee01889d235748b",
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

        "JCODINGS": {
            "moduleName": "org.jruby.jcodings",
            "maven": {
                "groupId": "org.jruby.jcodings",
                "artifactId": "jcodings",
                "version": "1.0.58"
            },
            "digest": "sha512:625210aa07d1e08bf2f5fdc9da6c491a4e5a56e7db297cba1aa73636670ac1d62f3fd763716ef6ede862456b17169272ed9c8461d07100f95262163dc9c18ef8",
            "sourceDigest": "sha512:d0f883f658310f7ad091aea08df28f1f5fe12080d6cb266cd91aec7e34cda1d57736d32618e8632b329854367d6e4d5fc91b5eb8ac9b823b26113fae3f75f50c",
            "license": ["MIT"],
        },

        "JONI": {
            "moduleName": "org.jruby.joni",
            "maven": {
                "groupId": "org.jruby.joni",
                "artifactId": "joni",
                "version": "2.1.44"
            },
            "digest": "sha512:59b901333d93b8eaf0c28a2d269d159e6806701902505113148c4df6b2ade973e97f9b888b8b5b4dff4d3c667b4594eeee8133f4260e7f1f3d9af1a031b9cab4",
            "sourceDigest": "sha512:773051c47893799ecc0b916b80c4dedac0efe2988743d455ce8529460aed2d7664d4b12cd51e449a8b20ac0c37e32ead89d72f6acf4f6a780268b55fdd67e3cf",
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
                "tool/docker",
                "rubyspec_temp",
            ]
        },
    },

    "projects": {

        # ------------- Projects -------------

        "org.prism": {
            "dir": "src/yarp",
            "sourceDirs": ["java"],
            "jniHeaders": True,
            "jacoco": "include",
            "javaCompliance": "8+",
            "workingSets": "TruffleRuby",
            "license": ["MIT"],
        },

        "org.truffleruby.annotations": {
            "dir": "src/annotations",
            "sourceDirs": ["java"],
            "jacoco": "include",
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.shared": {
            "dir": "src/shared",
            "sourceDirs": ["java"],
            "requires": ["java.management"],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "sdk:POLYGLOT",
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

        "org.truffleruby.resources": {
            "dir": "src/resources",
            "sourceDirs": ["java"],
            "dependencies": [
                "truffle:TRUFFLE_API",
            ],
            "annotationProcessors": [
                "truffle:TRUFFLE_DSL_PROCESSOR",
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

        "org.prism.libprism": {
            "class": "YARPNativeProject",
            "dir": "src/main/c/yarp",
            # "makeTarget": "all-no-debug", # Can use this to build without asserts
            "results": ["build/libprism.a"],
            "description": "YARP used as a static library"
        },

        "org.truffleruby.yarp.bindings": {
            "dir": "src/main/c/yarp_bindings",
            "native": "shared_lib",
            "deliverable": "yarpbindings",
            "buildDependencies": [
                "org.prism.libprism", # libprism.a
                "org.prism", # for the generated JNI header file
            ],
            "use_jdk_headers": True, # the generated JNI header includes jni.h
            "cflags": ["-g", "-Wall", "-Werror", "-pthread", "-I<path:org.prism.libprism>/include"],
            "ldflags": ["-pthread"],
            "ldlibs": ["<path:org.prism.libprism>/build/libprism.a"],
            "description": "JNI bindings for YARP"
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
                # Projects
                "org.prism",
                # Distributions, keep in sync with TRUFFLERUBY.distDependencies
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "truffle:TRUFFLE_API",
                "truffle:TRUFFLE_NFI",
                "regex:TREGEX",
                "sulong:SULONG_API",
                "sulong:SULONG_NFI",
                "sdk:JLINE3",
                # Libraries, keep in sync with TRUFFLERUBY.exclude and truffle_jars (in mx_truffleruby.py)
                "truffleruby:JCODINGS",
                "truffleruby:JONI",
            ],
            "annotationProcessors": [
                "truffle:TRUFFLE_DSL_PROCESSOR",
                "TRUFFLERUBY-PROCESSOR",
            ],
            "jacoco": "include",
            "javaCompliance": "17+",
            "checkstyleVersion": "10.7.0",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "findbugsIgnoresGenerated": True,
            "license": [
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
                "BSD-simplified",   # MRI
                "MIT",              # Joni, JCodings, YARP
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
            "requires": ["java.logging", "java.xml"],
            "dependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "sdk:POLYGLOT",
                "sdk:LAUNCHER_COMMON",
                "sdk:MAVEN_DOWNLOADER",
            ],
            "jacoco": "include",
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.test.embedding": {
            "testProject": True,
            "dir": "src/test-embedding",
            "sourceDirs": ["java"],
            "requires": ["java.scripting"],
            "dependencies": [
                # Distributions
                "sdk:POLYGLOT",
                # Libraries
                "mx:JUNIT",
            ],
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.test.internal": {
            "testProject": True,
            "dir": "src/test-internal",
            "sourceDirs": ["java", "ruby"],
            "requires": ["java.management", "jdk.management"],
            "dependencies": [
                # Distributions
                "sdk:LAUNCHER_COMMON",
                "TRUFFLERUBY",
                # Libraries
                "mx:JUNIT",
                "truffleruby:NETBEANS-LIB-PROFILER",
            ],
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.tck": {
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
                "truffle:TRUFFLE_NFI_NATIVE", # trufflenfi.h
                "TRUFFLERUBY-BOOTSTRAP-LAUNCHER",
            ],
            "buildEnv": {
                "TRUFFLERUBY_BOOTSTRAP_LAUNCHER": "<path:TRUFFLERUBY-BOOTSTRAP-LAUNCHER>/miniruby",
                "GRAALVM_TOOLCHAIN_CC": "<toolchainGetToolPath:native,CC>",
                "TRUFFLE_NFI_NATIVE_INCLUDE": "<path:truffle:TRUFFLE_NFI_NATIVE>/include",
            },
            "output": ".",
            "results": [
                "src/main/c/spawn-helper/spawn-helper",
                "src/main/c/truffleposix/<lib:truffleposix>",
                "src/main/c/cext/<lib:truffleruby>",
                "src/main/c/cext-trampoline/<lib:trufflerubytrampoline>",
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
                "lib/gems/gems/debug-1.7.1/lib/debug/<extsuffix:debug>",
                "lib/gems/gems/rbs-2.8.2/lib/<extsuffix:rbs_extension>",
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
            "moduleInfo": {
                "name": "org.graalvm.ruby.annotations",
                "exports": [
                    "org.truffleruby.annotations to org.graalvm.ruby",
                ],
            },
            "useModulePath": True,
            "dependencies": [
                "org.truffleruby.annotations"
            ],
            "description": "TruffleRuby Annotations",
            "license": ["EPL-2.0"],
            "maven": {
                "artifactId": "ruby-annotations",
                "tag": ["default", "public"],
            },
            "noMavenJavadoc": True,
        },

        # Required to share code between the launcher and the rest,
        # since the rest cannot depend on the launcher and the shared code cannot be there.
        # This code is loaded twice in different classloaders, therefore any created instances should not be passed around.
        "TRUFFLERUBY-SHARED": {
            "moduleInfo": {
                "name": "org.graalvm.ruby.shared",
                "exports": [
                    "org.truffleruby.shared to org.graalvm.ruby, org.graalvm.ruby.launcher",
                    "org.truffleruby.shared.options to org.graalvm.ruby, org.graalvm.ruby.launcher",
                ],
            },
            "useModulePath": True,
            "dependencies": [
                "org.truffleruby.shared"
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "sdk:POLYGLOT",
            ],
            "description": "TruffleRuby Shared constants and predicates",
            "license": ["EPL-2.0"],
            "maven": {
                "artifactId": "ruby-shared",
                "tag": ["default", "public"],
            },
            "noMavenJavadoc": True,
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
            "maven": False,
        },

        "TRUFFLERUBY": {
            "moduleInfo": {
                "name": "org.graalvm.ruby",
            },
            "useModulePath": True,
            "dependencies": [
                "org.truffleruby",
                "org.truffleruby.ruby",
            ],
            "distDependencies": [ # Keep in sync with org.truffleruby dependencies
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "truffle:TRUFFLE_API",
                "truffle:TRUFFLE_NFI",
                "regex:TREGEX",
                "sulong:SULONG_API",
                "sulong:SULONG_NFI",
                "sdk:JLINE3",
                # runtime-only dependencies
                "truffle:TRUFFLE_NFI_LIBFFI",
                "sulong:SULONG_NATIVE",
            ],
            "exclude": [ # Keep in sync with org.truffleruby dependencies and truffle_jars in mx_truffleruby.py
                "truffleruby:JCODINGS",
                "truffleruby:JONI",
            ],
            "description": "Core module of Ruby on Truffle",
            "license": [
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
                "BSD-simplified",   # MRI
                "MIT",              # Joni, JCodings
            ],
            "maven": {
                "artifactId": "ruby-language",
                "tag": ["default", "public"],
            },
            "noMavenJavadoc": True,
        },

        "RUBY_COMMUNITY": {
            "type": "pom",
            "runtimeDependencies": [
                "TRUFFLERUBY",
                "TRUFFLERUBY-RESOURCES",
                "truffle:TRUFFLE_RUNTIME",
                "sulong:LLVM_NATIVE_COMMUNITY",
            ],
            "description": "TruffleRuby (GraalVM Ruby)",
            "maven": {
                "groupId": "org.graalvm.polyglot",
                "artifactId": "ruby-community",
                "tag": ["default", "public"],
            },
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
            "maven": False,
        },

        "TRUFFLERUBY-LAUNCHER": {
            "moduleInfo": {
                "name": "org.graalvm.ruby.launcher",
                "exports": [
                    "org.truffleruby.launcher to org.graalvm.launcher",
                ],
            },
            "useModulePath": True,
            "dependencies": [
                "org.truffleruby.launcher"
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "truffleruby:TRUFFLERUBY-SHARED",
                "sdk:POLYGLOT",
                "sdk:LAUNCHER_COMMON",
                "sdk:MAVEN_DOWNLOADER",
            ],
            "description": "TruffleRuby Launcher",
            "license": ["EPL-2.0"],
            "maven": False,
        },

        "TRUFFLERUBY-RESOURCES": {
            "description": "TruffleRuby runtime resources",
            "platformDependent": True,
            "moduleInfo": {
                "name": "org.graalvm.ruby.resources",
            },
            "useModulePath": True,
            "dependencies": [
                "org.truffleruby.resources",
                "TRUFFLERUBY_RESOURCES_PLATFORM_AGNOSTIC",
                "TRUFFLERUBY_RESOURCES_PLATFORM_SPECIFIC",
            ],
            "distDependencies": [
                "truffle:TRUFFLE_API",
            ],
            "requires": [
                "java.base",
            ],
            "license": [
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "MIT",              # minitest, did_you_mean, rake
                "BSD-simplified",   # MRI
                "BSD-new",          # Rubinius, FFI
            ],
            "compress": True,
            "maven": {
                "artifactId": "ruby-resources",
                "tag": ["default", "public"],
            },
        },

        "TRUFFLERUBY_RESOURCES_PLATFORM_AGNOSTIC": {
            "description": "Platform-agnostic resources for TruffleRuby home",
            "type": "dir",
            "platformDependent": False,
            "hashEntry": "META-INF/resources/ruby/ruby-home/common/sha256",
            "fileListEntry": "META-INF/resources/ruby/ruby-home/common/file-list",
            "layout": {
                "META-INF/resources/ruby/ruby-home/common/": "extracted-dependency:TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_AGNOSTIC",
            },
            "maven": False,
        },

        "TRUFFLERUBY_RESOURCES_PLATFORM_SPECIFIC": {
            "description": "Platform-specific resources for TruffleRuby home",
            "type": "dir",
            "platformDependent": True,
            "hashEntry": "META-INF/resources/ruby/ruby-home/<os>/<arch>/sha256",
            "fileListEntry": "META-INF/resources/ruby/ruby-home/<os>/<arch>/file-list",
            "layout": {
                "META-INF/resources/ruby/ruby-home/<os>/<arch>/": "extracted-dependency:TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_SPECIFIC",
            },
            "platforms": [
                "linux-amd64",
                "linux-aarch64",
                "darwin-amd64",
                "darwin-aarch64",
            ],
            "maven": False,
        },

        "TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_AGNOSTIC": {
            "description": "Platform-agnostic TruffleRuby home files",
            "fileListPurpose": 'native-image-resources',
            "native": True,
            "platformDependent": False,
            # The project org.truffleruby.cext touches lib/gems/gems/debug-1.7.1/ext
            # and lib/gems/extensions/$ARCH-$OS/$ABI/rbs-2.8.2/gem.build_complete.
            # This causes this layout distribution to be rebuilt even though nothing changes in the result.
            # To avoid that we force org.truffleruby.cext to complete first.
            "dependencies": ["org.truffleruby.cext"],
            "layout": {
                "lib/": [
                    "file:lib/json",
                    "file:lib/mri",
                    "file:lib/patches",
                    "file:lib/truffle",
                ],
                "lib/cext/": [
                    "file:lib/cext/*.rb",
                ],
                "lib/cext/include/": [
                    "file:lib/cext/include/*",
                ],
                "lib/gems/": [
                    {
                        "source_type": "file",
                        "path": "lib/gems/*",
                        "exclude": [
                            # The debug and rbs gems have native extensions.
                            # Do not ship ext/ as it includes an unnecessary copy of the .so and intermediate files.
                            # The .so in lib/ are copied in the platform-specific distribution.
                            # <extsuffix:...> does not work in exclude, so use .* here (.{so,bundle} does not work either).
                            "lib/gems/extensions",
                            "lib/gems/gems/debug-1.7.1/ext",
                            "lib/gems/gems/debug-1.7.1/lib/debug/debug.*",
                            "lib/gems/gems/rbs-2.8.2/ext",
                            "lib/gems/gems/rbs-2.8.2/lib/rbs_extension.*",
                        ],
                    },
                ],
            },
            "license": [
                "EPL-2.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "MIT",              # minitest, did_you_mean, rake
                "BSD-simplified",   # MRI
                "BSD-new",          # Rubinius, FFI
            ],
            "maven": False,
        },

        "TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_SPECIFIC": {
            "description": "Platform-specific TruffleRuby home files",
            "fileListPurpose": 'native-image-resources',
            "native": True,
            "platformDependent": True,
            "layout": {
                "lib/": [
                    "dependency:org.truffleruby.yarp.bindings",
                ],
                "lib/cext/": [
                    "dependency:org.truffleruby.cext/src/main/c/truffleposix/<lib:truffleposix>",
                    "dependency:org.truffleruby.cext/src/main/c/cext/<lib:truffleruby>",
                    "dependency:org.truffleruby.cext/src/main/c/cext-trampoline/<lib:trufflerubytrampoline>",
                    "dependency:org.truffleruby.rubysignal",
                ],
                # The platform-specific files from debug and rbs, see comment above
                "lib/gems/": "file:lib/gems/extensions",
                "lib/gems/gems/debug-1.7.1/lib/debug/": "dependency:org.truffleruby.cext/lib/gems/gems/debug-1.7.1/lib/debug/<extsuffix:debug>",
                "lib/gems/gems/rbs-2.8.2/lib/": "dependency:org.truffleruby.cext/lib/gems/gems/rbs-2.8.2/lib/<extsuffix:rbs_extension>",
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
                "BSD-simplified",   # MRI
            ],
            "maven": False,
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
                "src/main/c/psych/": [
                    "file:src/main/c/psych/extconf.rb",
                    "file:src/main/c/psych/*.c",
                    "file:src/main/c/psych/psych*.h",
                ],
            },
            "maven": False,
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

        "TRUFFLERUBY-TEST-EMBEDDING": {
            "testDistribution": True,
            "dependencies": [
                "org.truffleruby.test.embedding",
            ],
            "distDependencies": [
                "sdk:POLYGLOT",
                # runtime-only dependencies
                "TRUFFLERUBY",
                "TRUFFLERUBY-RESOURCES",
            ],
            "exclude": [
                "mx:HAMCREST",
                "mx:JUNIT",
            ],
            "unittestConfig": "none",
            "javaProperties": {
                "polyglot.engine.WarnInterpreterOnly": "false",
            },
            "license": ["EPL-2.0"],
            "maven": False,
        },

        "TRUFFLERUBY-TEST-INTERNAL": {
            "testDistribution": True,
            "dependencies": [
                "org.truffleruby.test.internal",
            ],
            "distDependencies": [
                "sdk:LAUNCHER_COMMON",
                "TRUFFLERUBY",
                # runtime-only dependencies
                "TRUFFLERUBY-RESOURCES"
            ],
            "exclude": [
                "mx:HAMCREST",
                "mx:JUNIT",
                "truffleruby:NETBEANS-LIB-PROFILER",
            ],
            "unittestConfig": "none",
            "javaProperties": {
                "polyglot.engine.WarnInterpreterOnly": "false",
                "polyglotimpl.DisableClassPathIsolation": "true",
            },
            "license": ["EPL-2.0"],
            "maven": False,
        },

        "TRUFFLERUBY-TCK": {
            "dependencies": ["org.truffleruby.tck"],
            "distDependencies": [
                "truffle:TRUFFLE_TCK",
               # runtime-only dependencies
                "TRUFFLERUBY",
            ],
            "description" : "Truffle TCK provider for Ruby language.",
            "license": ["EPL-2.0"],
            "maven": {
                "artifactId": "ruby-truffle-tck",
                "tag": ["default", "public"],
            },
            "noMavenJavadoc": True,
        },
    },
}
