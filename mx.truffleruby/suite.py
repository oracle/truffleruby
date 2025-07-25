suite = {
    "mxversion": "7.45.0",
    "name": "truffleruby",
    "version": "26.0.0",
    "release": False,
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
                "version": "5de6a234eeb153ebe0f6f0185bb9426fba57a0dd",
                "urls": [
                    {"url": "https://github.com/oracle/graal.git", "kind": "git"},
                    {"url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind": "binary"},
                ]
            },
            {
                "name": "sulong",
                "subdir": True,
                "version": "5de6a234eeb153ebe0f6f0185bb9426fba57a0dd",
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
                "EPL-2.0",          # JRuby (we choose EPL out of EPL,GPL,LGPL)
                "BSD-simplified",   # MRI
                "BSD-new",          # Rubinius, FFI
                "MIT",              # JCodings, minitest, did_you_mean, rake
            ]
        },
    },

    "libraries": {

        # ------------- Libraries -------------

        "JONI": {
            "moduleName": "org.jruby.joni",
            "maven": {
                "groupId": "org.jruby.joni",
                "artifactId": "joni",
                "version": "2.2.6"
            },
            "digest": "sha512:b62204f42d976a3c69bb6df006c0ee0fb2c1babd3943e12672db02cb861ab0728f68d5b0e29866d4247d5e9f3e52bf90ea15b74eef43a5a0e344666b0121055a",
            "sourceDigest": "sha512:9229253a37f8e5c5abe682f6889c65caaa17f85db6e40fa61d2d9f6f6d96bf4f2c11f80eb2a794739414ff6b60d4e5d471f1749952a89aea1bed1a415cf8acf8",
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
            "path": ".",
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
                "sdk:NATIVEIMAGE",
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

        "org.truffleruby.signal": {
            "dir": "src/signal",
            "sourceDirs": ["java"],
            "jniHeaders": True,
            "javaCompliance": "17+",
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "license": ["EPL-2.0"],
        },

        "org.truffleruby.librubysignal": {
            "dir": "src/main/c/rubysignal",
            "native": "shared_lib",
            "deliverable": "rubysignal",
            "buildDependencies": [
                "org.truffleruby.signal", # for the generated JNI header file
            ],
            "use_jdk_headers": True, # the generated JNI header includes jni.h
            "cflags": ["-g", "-O3", "-Wall", "-Werror", "-pthread"],
            "ldflags": ["-pthread"],
        },

        "org.truffleruby.libtruffleposix": {
            "dir": "src/main/c/truffleposix",
            "native": "shared_lib",
            "deliverable": "truffleposix",
            "buildDependencies": [
                "TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_AGNOSTIC",
            ],
            "cflags": ["-g", "-O3", "-std=c99", "-Wall", "-Werror", "-pthread", "-I<path:TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_AGNOSTIC>/lib/cext/include"],
            "ldflags": ["-pthread"],
            "os": {
                "linux": {
                    "ldlibs": ["-lrt"],
                },
                "<others>": {
                },
            },
        },

        "org.truffleruby.spawnhelper": {
            "dir": "src/main/c/spawn-helper",
            "native": "executable",
            "deliverable": "spawn-helper",
            "cflags": ["-g", "-O3", "-std=c99", "-Wall", "-Werror"],
        },

        "org.prism.libprism": {
            "class": "YARPNativeProject",
            "dir": "src/main/c/yarp",
            # "makeTarget": "all-no-debug", # Can use this to build without asserts
            "results": ["build/libprism.a"],
            "description": "YARP used as a static library with only semantics fields"
        },

        "org.prism.libprism.for.gem": {
            "class": "YARPNativeProject",
            "dir": "src/main/c/prism-gem",
            # "makeTarget": "all-no-debug", # Can use this to build without asserts
            "results": ["build/<lib:prism>"],
            "description": "YARP used as a dynamic library with all fields"
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
            "cflags": ["-g", "-O3", "-Wall", "-Werror", "-pthread", "-I<path:org.prism.libprism>/include"],
            "ldflags": ["-pthread"],
            "ldlibs": ["<path:org.prism.libprism>/build/libprism.a"],
            "description": "JNI bindings for YARP"
        },

        "org.truffleruby": {
            "dir": "src/main",
            "sourceDirs": ["java"],
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
                "sdk:COLLECTIONS",
                "sdk:NATIVEIMAGE",
                "sdk:POLYGLOT",
                # Library distributions, keep in sync with truffle_jars in mx_truffleruby.py
                "truffle:TRUFFLE_JCODINGS",
                "truffleruby:TRUFFLERUBY_JONI",
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
            "spotbugsIgnoresGenerated": True,
            "license": [
                "EPL-2.0",          # JRuby (we choose EPL out of EPL,GPL,LGPL)
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
                "EPL-2.0",          # JRuby (we choose EPL out of EPL,GPL,LGPL)
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
                "sdk:NATIVEIMAGE",
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
            "buildDependencies": [ # These are used to build the module path
                "TRUFFLERUBY", # We need this jar to run extconf.rb
                "TRUFFLERUBY-LAUNCHER", # We need this jar to run extconf.rb
                "sulong:SULONG_NATIVE", # We need this jar to find the toolchain with Toolchain#getToolPath
                "TRUFFLERUBY_BOOTSTRAP_HOME", # libyarpbindings.so, librubysignal.so
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
                "src/main/c/debug/<extsuffix:debug>",
                "src/main/c/rbs/<extsuffix:rbs_extension>",
            ],
            "license": [
                "EPL-2.0",          # JRuby (we choose EPL out of EPL,GPL,LGPL)
                "BSD-simplified",   # MRI
            ],
        },

        "org.graalvm.shadowed.org.joni": {
            # Shadowed JONI library (org.jruby.joni:joni)
            "dir": "src/shadowed/joni",
            "sourceDirs": ["java"],
            "javaCompliance": "17+",
            "spotbugsIgnoresGenerated": True,
            "dependencies": [
                "truffle:TRUFFLE_JCODINGS",
            ],
            "shadedDependencies": [
                "truffleruby:JONI",
            ],
            "class": "ShadedLibraryProject",
            "shade": {
                "packages": {
                    "org.joni": "org.graalvm.shadowed.org.joni",
                    "org.jcodings": "org.graalvm.shadowed.org.jcodings",
                },
                "exclude": [
                    "META-INF/MANIFEST.MF",
                    "META-INF/maven/org.jruby.joni/joni/*", # pom.xml, pom.properties
                    "module-info.java",
                    "org/joni/bench/*.java",
                ],
            },
            "description": "JOni library shadowed for TruffleRuby.",
            # We need to force javac because the generated sources in this project produce warnings in JDT.
            "forceJavac": "true",
            "javac.lint.overrides": "none",
            "jacoco": "exclude",
        },

        "truffleruby_licenses": {
            "class": "StandaloneLicenses",
            "community_license_file": "LICENCE.md",
            "community_3rd_party_license_file": "3rd_party_licenses.txt",
        },

        "truffleruby_thin_launcher": {
            "class": "ThinLauncherProject",
            "mainClass": "org.truffleruby.launcher.RubyLauncher",
            "jar_distributions": ["truffleruby:TRUFFLERUBY-LAUNCHER"],
            "option_vars": [
                "RUBYOPT",
                "TRUFFLERUBYOPT",
            ],
            "relative_home_paths": {
                "ruby": "..",
                "llvm": "../lib/sulong",
            },
            "relative_jre_path": "../jvm",
            "relative_module_path": "../modules",
            "relative_extracted_lib_paths": {
                "truffle.attach.library": "../jvmlibs/<lib:truffleattach>",
                "truffle.nfi.library": "../jvmlibs/<lib:trufflenfi>",
            },
            "liblang_relpath": "../lib/<lib:rubyvm>",
        },

        "librubyvm": {
            "class": "LanguageLibraryProject",
            "dependencies": [
                "TRUFFLERUBY_STANDALONE_DEPENDENCIES",
                # LLVM_NATIVE_POM is intentionally not used as that would include SULONG_NATIVE_RESOURCES,
                # which would copy the resources in the image, regardless of IncludeLanguageResources,
                # see com.oracle.truffle.llvm.nativemode.resources.NativeResourceFeature.
            ],
            "buildDependencies": [
                "TRUFFLERUBY_STANDALONE_COMMON",
            ],
            "build_args": [
                # From mx.truffleruby/native-image.properties
                "-Dpolyglot.image-build-time.PreinitializeContexts=ruby",
                "-Dorg.graalvm.language.ruby.home=<path:TRUFFLERUBY_STANDALONE_COMMON>",
                # Configure launcher
                "-Dorg.graalvm.launcher.class=org.truffleruby.launcher.RubyLauncher",
            ],
            "dynamicBuildArgs": "librubyvm_build_args",
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
                    "org.truffleruby.signal to org.graalvm.ruby, org.graalvm.ruby.launcher",
                ],
            },
            "useModulePath": True,
            "dependencies": [
                "org.truffleruby.shared",
                "org.truffleruby.signal",
            ],
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-ANNOTATIONS",
                "sdk:NATIVEIMAGE",
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
                "sdk:COLLECTIONS",
                "sdk:NATIVEIMAGE",
                "sdk:POLYGLOT",
                # Library distributions, keep in sync with truffle_jars in mx_truffleruby.py
                "truffle:TRUFFLE_JCODINGS",
                "truffleruby:TRUFFLERUBY_JONI",
                # runtime-only dependencies
                "truffle:TRUFFLE_NFI_LIBFFI",
                "truffle:TRUFFLE_NFI_PANAMA",
                "sulong:SULONG_NATIVE",
            ],
            "description": "TruffleRuby, a high-performance embeddable Ruby implementation. This artifact includes the core language runtime without standard libraries. It is not recommended to depend on this artifact directly. Instead, use \'org.graalvm.polyglot:ruby\' to ensure all dependencies are pulled in correctly.", # pylint: disable=line-too-long
            "license": [
                "EPL-2.0",          # JRuby (we choose EPL out of EPL,GPL,LGPL)
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

        "RUBY_POM": {
            "type": "pom",
            "runtimeDependencies": [
                "TRUFFLERUBY",
                "TRUFFLERUBY-RESOURCES",
                "truffle:TRUFFLE_RUNTIME",
                "sulong:LLVM_NATIVE_POM",
            ],
            "description": "TruffleRuby (GraalVM Ruby)",
            "maven": {
                "artifactId": "ruby",
                "tag": ["default", "public"],
            },
            "license": [
                "EPL-2.0",          # JRuby (we choose EPL out of EPL,GPL,LGPL)
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
                "sdk:NATIVEIMAGE",
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
                "EPL-2.0",          # JRuby (we choose EPL out of EPL,GPL,LGPL)
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

        "TRUFFLERUBY_BOOTSTRAP_HOME": {
            "description": "TruffleRuby bootstrap home used by a minimal TruffleRuby to run extconf.rb of default & bundled gems C extensions",
            "native": True,
            "platformDependent": True,
            "layout": {
                "lib/": [
                    "file:lib/json",
                    "file:lib/mri",
                    "file:lib/patches",
                    "file:lib/truffle",
                    "dependency:org.truffleruby.yarp.bindings",
                ],
                "lib/cext/": [
                    "file:lib/cext/*.rb",
                    "dependency:org.truffleruby.librubysignal",
                    "dependency:org.truffleruby.libtruffleposix",
                ],
                "lib/cext/include/": [
                    "file:lib/cext/include/*",
                ],
                "lib/truffle/": [
                    "dependency:org.truffleruby.spawnhelper",
                ],
            },
            "maven": False,
        },

        "TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_AGNOSTIC": {
            "description": "Platform-agnostic TruffleRuby home files",
            "fileListPurpose": "native-image-resources",
            "native": True,
            "platformDependent": False,
            "layout": {
                "lib/": [
                    "file:lib/json",
                    "file:lib/gems",
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
                "lib/prism/": [
                    "file:src/main/c/prism-gem/include",
                ],
            },
            "license": [
                "EPL-2.0",          # JRuby (we choose EPL out of EPL,GPL,LGPL)
                "MIT",              # minitest, did_you_mean, rake
                "BSD-simplified",   # MRI
                "BSD-new",          # Rubinius, FFI
            ],
            "maven": False,
        },

        "TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_SPECIFIC": {
            "description": "Platform-specific TruffleRuby home files",
            "fileListPurpose": "native-image-resources",
            "native": True,
            "platformDependent": True,
            "layout": {
                "lib/": [
                    "dependency:org.truffleruby.yarp.bindings",
                ],
                "lib/prism/": [
                    "dependency:org.prism.libprism.for.gem/build/<lib:prism>",
                ],
                "lib/cext/": [
                    "dependency:org.truffleruby.librubysignal",
                    "dependency:org.truffleruby.libtruffleposix",
                    "dependency:org.truffleruby.cext/src/main/c/cext/<lib:truffleruby>",
                    "dependency:org.truffleruby.cext/src/main/c/cext-trampoline/<lib:trufflerubytrampoline>",
                ],
                # Create the complete files to let RubyGems know the gems are fully built and can be activated
                "lib/gems/extensions/<cruby_arch>-<os>/<truffleruby_abi_version>/debug-1.9.2/gem.build_complete": "string:",
                "lib/gems/extensions/<cruby_arch>-<os>/<truffleruby_abi_version>/racc-1.7.3/gem.build_complete": "string:", # actually we do not build the C extension because the pure-Ruby fallback is enough
                "lib/gems/extensions/<cruby_arch>-<os>/<truffleruby_abi_version>/rbs-3.4.0/gem.build_complete": "string:",
                "lib/gems/gems/debug-1.9.2/lib/debug/": [
                    "dependency:org.truffleruby.cext/src/main/c/debug/<extsuffix:debug>",
                ],
                "lib/gems/gems/rbs-3.4.0/lib/": [
                    "dependency:org.truffleruby.cext/src/main/c/rbs/<extsuffix:rbs_extension>",
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
                    "dependency:org.truffleruby.spawnhelper",
                ],
            },
            "license": [
                "BSD-simplified",   # MRI
            ],
            "maven": False,
        },

        "TRUFFLERUBY_GRAALVM_SUPPORT_NO_NI_RESOURCES": {
            "description": "TruffleRuby support distribution for the GraalVM, the contents is not included as native image resources.",
            "native": True,
            "platformDependent": True,
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

        "TRUFFLERUBY_STANDALONE_DEPENDENCIES": {
            "description": "TruffleRuby standalone dependencies",
            "class": "DynamicPOMDistribution",
            "distDependencies": [
                "truffleruby:TRUFFLERUBY-LAUNCHER",
                "truffleruby:TRUFFLERUBY",
                "sdk:TOOLS_FOR_STANDALONE",
            ],
            "dynamicDistDependencies": "truffleruby_standalone_deps",
            "maven": False,
        },

        "TRUFFLERUBY_STANDALONE_COMMON": {
            "description": "Common layout for Native and JVM standalones",
            "type": "dir",
            "platformDependent": True,
            "platforms": "local",
            "layout": {
                "./": [
                    "extracted-dependency:TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_AGNOSTIC",
                    "extracted-dependency:TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_SPECIFIC",
                    "extracted-dependency:TRUFFLERUBY_GRAALVM_SUPPORT_NO_NI_RESOURCES",
                    "dependency:truffleruby_licenses/*",
                ],
                "bin/ruby": "dependency:truffleruby_thin_launcher",
                "bin/truffleruby": "dependency:truffleruby_thin_launcher",
                "bin/truffleruby-polyglot-get": "dependency:truffleruby_thin_launcher",
                "lib/sulong/": [
                    "extracted-dependency:sulong:SULONG_CORE_HOME",
                    "extracted-dependency:sulong:SULONG_GRAALVM_DOCS",
                    {
                        "source_type": "extracted-dependency",
                        "dependency": "sulong:SULONG_BITCODE_HOME",
                        "path": "*",
                        "exclude": [
                            "native/lib/*++*",
                        ],
                    },
                    {
                        "source_type": "extracted-dependency",
                        "dependency": "sulong:SULONG_NATIVE_HOME",
                        "path": "*",
                        "exclude": [
                            "native/cmake",
                            "native/include",
                            "native/lib/*++*",
                            "native/share",
                        ],
                    },
                ],
                "release": "dependency:sdk:STANDALONE_JAVA_HOME/release",
            },
        },

        "TRUFFLERUBY_NATIVE_STANDALONE": {
            "description": "TruffleRuby Native standalone",
            "type": "dir",
            "platformDependent": True,
            "platforms": "local",
            "layout": {
                "./": [
                    "dependency:TRUFFLERUBY_STANDALONE_COMMON/*",
                ],
                "lib/": "dependency:librubyvm",
            },
        },

        "TRUFFLERUBY_JVM_STANDALONE": {
            "description": "TruffleRuby JVM standalone",
            "type": "dir",
            "platformDependent": True,
            "platforms": "local",
            "layout": {
                "./": [
                    "dependency:TRUFFLERUBY_STANDALONE_COMMON/*",
                ],
                "jvm/": {
                    "source_type": "dependency",
                    "dependency": "sdk:STANDALONE_JAVA_HOME",
                    "path": "*",
                    "exclude": [
                        # Native Image-related
                        "bin/native-image*",
                        "lib/static",
                        "lib/svm",
                        "lib/<lib:native-image-agent>",
                        "lib/<lib:native-image-diagnostics-agent>",
                        # Unnecessary and big
                        "lib/src.zip",
                        "jmods",
                    ],
                },
                "jvmlibs/": [
                    "extracted-dependency:truffle:TRUFFLE_ATTACH_GRAALVM_SUPPORT",
                    "extracted-dependency:truffle:TRUFFLE_NFI_NATIVE_GRAALVM_SUPPORT",
                ],
                "modules/": [
                    "classpath-dependencies:TRUFFLERUBY_STANDALONE_DEPENDENCIES",
                ],
            },
        },

        "TRUFFLERUBY_NATIVE_STANDALONE_RELEASE_ARCHIVE": {
            "class": "DeliverableStandaloneArchive",
            "platformDependent": True,
            "standalone_dist": "TRUFFLERUBY_NATIVE_STANDALONE",
            "community_archive_name": "truffleruby-community",
            "enterprise_archive_name": "truffleruby",
            "language_id": "ruby",
        },

        "TRUFFLERUBY_JVM_STANDALONE_RELEASE_ARCHIVE": {
            "class": "DeliverableStandaloneArchive",
            "platformDependent": True,
            "standalone_dist": "TRUFFLERUBY_JVM_STANDALONE",
            "community_archive_name": "truffleruby-community-jvm",
            "enterprise_archive_name": "truffleruby-jvm",
            "language_id": "ruby",
        },

        "TRUFFLERUBY_GRAALVM_LICENSES": {
            "fileListPurpose": "native-image-resources",
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
            "unittestConfig": "truffleruby",
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
            "unittestConfig": "truffleruby",
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
                "TRUFFLERUBY-RESOURCES",
            ],
            "description": "Truffle TCK provider for Ruby language.",
            "license": ["EPL-2.0"],
            "maven": {
                "artifactId": "ruby-truffle-tck",
                "tag": ["default", "public"],
            },
            "noMavenJavadoc": True,
        },

        "TRUFFLERUBY_JONI": {
            # JONI library shadowed for TruffleRuby.
            "moduleInfo": {
                "name": "org.graalvm.shadowed.joni",
                "requires": [
                    "org.graalvm.shadowed.jcodings",
                ],
                "exports": [
                    "org.graalvm.shadowed.org.joni to org.graalvm.ruby",
                    "org.graalvm.shadowed.org.joni.constants to org.graalvm.ruby",
                    "org.graalvm.shadowed.org.joni.exception to org.graalvm.ruby",
                ],
            },
            "javaCompliance": "17+",
            "dependencies": [
                "org.graalvm.shadowed.org.joni",
            ],
            "distDependencies": [
                "truffle:TRUFFLE_JCODINGS",
            ],
            "description": "JOni module shadowed for TruffleRuby.",
            "license": ["MIT"],
            "maven": {
                "groupId": "org.graalvm.shadowed",
                "artifactId": "joni",
                "tag": ["default", "public"],
            },
            "allowsJavadocWarnings": True,
            "compress": True,
        },
    },
}
