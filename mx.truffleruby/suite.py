suite = {
    "mxversion": "5.210.3",
    "name": "truffleruby",

    "imports": {
        "suites": [
            { # Import only the tools suite which depends on truffle, to avoid duplicating import versions.
              # We want tools to be reliably available with TruffleRuby, even with "mx build", so this is a static import.
                "name": "tools",
                "subdir": True,
                # version must always be equal to the version of the "sulong" import below
                "version": "5dc4d8b4f772e90475a00c2093846cf11b2fd480",
                "urls": [
                    {"url": "https://github.com/oracle/graal.git", "kind": "git"},
                    {"url": "https://curio.ssw.jku.at/nexus/content/repositories/snapshots", "kind": "binary"},
                ]
            },
            {
                "name": "sulong",
                "subdir": True,
                # version must always be equal to the version of the "tools" import above
                "version": "5dc4d8b4f772e90475a00c2093846cf11b2fd480",
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
                ".ext",
                "truffleruby-gem-test-pack",
                "lib/json/java",
                "lib/ruby",
                "test/truffle/ecosystem/blog",
                "test/truffle/ecosystem/hello-world",
                "test/truffle/ecosystem/rails-app",
                "test/truffle/offline"
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
            "license": ["EPL-1.0"],
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
            "license": ["EPL-1.0"],
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
            "license": ["EPL-1.0"],
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
            "license": ["EPL-1.0"],
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
            "checkstyle": "org.truffleruby",
            "workingSets": "TruffleRuby",
            "findbugsIgnoresGenerated": True,
            "checkPackagePrefix": "false",
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
                "BSD-new",          # Rubinius
                "BSD-simplified",   # MRI
                "MIT",              # Joni, JCodings
            ],
            "externalProjects": {
                "ruby-core": {
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
            "license": ["EPL-1.0"],
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
            "license": ["EPL-1.0"],
        },

        "org.truffleruby.test-ruby": {
            "class": "ArchiveProject",
            "outputDir": "src/test/ruby",
            "prefix": "src/test/ruby",
            "license": ["EPL-1.0"],
        },

        "org.truffleruby.cext": {
            "native": True,
            "dir": "src/main/c",
            "buildDependencies": [
                "TRUFFLERUBY", # We need this jar to run extconf.rb
                "TRUFFLERUBY-LAUNCHER", # We need this jar to run extconf.rb
                "truffle:TRUFFLE_NFI_NATIVE", # trufflenfi.h
                "sulong:SULONG_HOME", # polyglot.h
            ],
            "buildEnv": {
              "NFI_HEADERS_DIR": "<path:truffle:TRUFFLE_NFI_NATIVE>/include",
              "SULONG_HEADERS_DIR": "<path:SULONG_HOME>/include",
              "SULONG_POLYGLOT_H": "<path:SULONG_HOME>/include/polyglot.h",
            },
            "output": ".",
            "results": [
                "src/main/c/truffleposix/<lib:truffleposix>",
                "src/main/c/sulongmock/sulongmock.o",
                "src/main/c/cext/ruby.o",
                "src/main/c/cext/ruby.su",
                "src/main/c/etc/etc.su",
                "src/main/c/nkf/nkf.su",
                "src/main/c/openssl/openssl.su",
                "src/main/c/psych/psych.su",
                "src/main/c/rbconfig-sizeof/sizeof.su",
                "src/main/c/syslog/syslog.su",
                "src/main/c/zlib/zlib.su",
            ],
            "license": [
                "EPL-1.0",          # JRuby (we're choosing EPL out of EPL,GPL,LGPL)
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
            "license": ["EPL-1.0"],
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
            "license": ["EPL-1.0"],
        },

        "TRUFFLERUBY_GRAALVM_SUPPORT": {
            "native": True,
            "platformDependent": True,
            "description": "TruffleRuby support distribution for the GraalVM",
            "layout": {
                "./": [
                    "file:CHANGELOG.md",
                    "file:README.md",
                    "file:mx.truffleruby/native-image.properties",
                ],
                "bin/": [
                    "file:bin/bundle",
                    "file:bin/bundler",
                    "file:bin/gem",
                    "file:bin/irb",
                    "file:bin/rake",
                    "file:bin/rdoc",
                    "file:bin/ri",
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
                "lib/": [
                    "file:lib/json",
                    "file:lib/mri",
                    "file:lib/patches",
                    "file:lib/truffle",
                ],
                "lib/cext/": [
                    "file:lib/cext/patches",
                    "file:lib/cext/*.rb",
                    "dependency:org.truffleruby.cext/src/main/c/truffleposix/<lib:truffleposix>",
                    "dependency:org.truffleruby.cext/src/main/c/sulongmock/sulongmock.o",
                    "dependency:org.truffleruby.cext/src/main/c/cext/ruby.o",
                    "dependency:org.truffleruby.cext/src/main/c/cext/ruby.su",
                ],
                "lib/cext/include/": [
                    "file:lib/cext/include/ccan",
                    "file:lib/cext/include/ruby",
                    "file:lib/cext/include/truffleruby",
                    "file:lib/cext/include/*.h",
                ],
                "lib/cext/include/sulong/": [
                    "link:../../sulong-libs/include/polyglot.h",
                ],
                "lib/cext/sulong-libs/": [
                    "extracted-dependency:sulong:SULONG_HOME/*",
                ],
                "lib/mri/": [
                    "dependency:org.truffleruby.cext/src/main/c/etc/etc.su",
                    "dependency:org.truffleruby.cext/src/main/c/nkf/nkf.su",
                    "dependency:org.truffleruby.cext/src/main/c/openssl/openssl.su",
                    "dependency:org.truffleruby.cext/src/main/c/psych/psych.su",
                    "dependency:org.truffleruby.cext/src/main/c/syslog/syslog.su",
                    "dependency:org.truffleruby.cext/src/main/c/zlib/zlib.su",
                ],
                "lib/mri/rbconfig/": [
                    "dependency:org.truffleruby.cext/src/main/c/rbconfig-sizeof/sizeof.su",
                ],
                "lib/ruby/gems/2.6.0/": [
                    "file:lib/ruby/gems/2.6.0/truffleruby_gem_dir_marker.txt",
                ],
                "lib/ruby/gems/2.6.0/gems/": [
                    "file:lib/ruby/gems/2.6.0/gems/did_you_mean-1.3.0",
                    "file:lib/ruby/gems/2.6.0/gems/minitest-5.11.3",
                    "file:lib/ruby/gems/2.6.0/gems/net-telnet-0.2.0",
                    "file:lib/ruby/gems/2.6.0/gems/power_assert-1.1.3",
                    "file:lib/ruby/gems/2.6.0/gems/rake-12.3.2",
                    "file:lib/ruby/gems/2.6.0/gems/test-unit-3.2.9",
                    "file:lib/ruby/gems/2.6.0/gems/xmlrpc-0.3.0",
                ],
                "lib/ruby/gems/2.6.0/specifications/": [
                    "file:lib/ruby/gems/2.6.0/specifications/default",
                    "file:lib/ruby/gems/2.6.0/specifications/did_you_mean-1.3.0.gemspec",
                    "file:lib/ruby/gems/2.6.0/specifications/minitest-5.11.3.gemspec",
                    "file:lib/ruby/gems/2.6.0/specifications/net-telnet-0.2.0.gemspec",
                    "file:lib/ruby/gems/2.6.0/specifications/power_assert-1.1.3.gemspec",
                    "file:lib/ruby/gems/2.6.0/specifications/rake-12.3.2.gemspec",
                    "file:lib/ruby/gems/2.6.0/specifications/test-unit-3.2.9.gemspec",
                    "file:lib/ruby/gems/2.6.0/specifications/xmlrpc-0.3.0.gemspec",
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

        "TRUFFLERUBY_GRAALVM_LICENSES": {
            "native": True,
            "platformDependent": True,
            "description": "TruffleRuby support distribution for the GraalVM license files",
            "layout": {
                "LICENSE_TRUFFLERUBY.txt": "file:LICENCE.md",
                "3rd_party_licenses_truffleruby.txt": "file:3rd_party_licenses.txt",
            },
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
                "TRUFFLERUBY-SERVICES",
                "truffle:TRUFFLE_TCK"
            ],
            "license": ["EPL-1.0"],
        },
    },
}
