{
  overlay: "00590f090c94b77f0c0438e050dec81035d5c994",

  local jt = function(args)
    [
      ["ruby", "tool/jt.rb"] + args
    ],

  local labsjdk8 = {
    downloads: {
      JAVA_HOME: {
        name: "labsjdk",
        version: "8u141-jvmci-0.36",
        platformspecific: true
      }
    }
  },
  labsjdk8: labsjdk8,

  local labsjdk9 = {
    downloads: {
      JAVA_HOME: {
        name: "labsjdk",
        version: "9+181",
        platformspecific: true
      }
    }
  },
  labsjdk9: labsjdk9,

  local java_opts = "-Xmx2G",

  local common = {
    downloads: labsjdk8.downloads + {
      MAVEN_HOME: {name: "maven", version: "3.3.9"}
    },

    environment: {
      CI: "true",
      RUBY_BENCHMARKS: "true",
      JAVA_OPTS: java_opts,
      PATH: "$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"
    },

    prelude:: [],

    build:: [
      ["mx", "build", "--force-javac", "--warning-as-error"]
    ],

    setup: self.prelude + self.build,

    timelimit: "01:00:00"
  },
  common: common,

  common_linux: common + {
    packages+: {
      git:        ">=1.8.3",
      mercurial:  ">=3.2.4",
      ruby:       ">=2.0.0",
      llvm:       "==3.8"
    }
  },

  common_solaris: common + {
    environment+: {
      # LLVM is currently not available on Solaris
      TRUFFLERUBY_CEXT_ENABLED: "false"
    }
  },

  common_darwin: common + {
    environment+: {
      # Homebrew does not put llvm on the PATH by default
      PATH: "/usr/local/opt/llvm/bin:" + common.environment["PATH"],
      OPENSSL_PREFIX: "/usr/local/opt/openssl"
    }
  },

  local truffleruby = {
    environment+: {
      GUEST_VM: "jruby",
      GUEST_VM_CONFIG: "truffle",
    }
  },
  truffleruby: truffleruby,

  local truffleruby_cexts = {
    environment+: {
      GUEST_VM: "jruby",
      GUEST_VM_CONFIG: "truffle-cexts",
    }
  },

  local no_graal = {
    environment+: truffleruby.environment + {
      HOST_VM: "server",
      HOST_VM_CONFIG: "default",
      MX_NO_GRAAL: "true"
    }
  },

  graal_core: {
    downloads+: labsjdk8.downloads,

    setup: common.setup + [
      ["cd", "../graal/compiler"],
      ["mx", "sversions"],
      ["mx", "build"],
      ["cd", "../../main"]
    ],

    environment+: truffleruby.environment + {
      GRAAL_HOME: "../graal/compiler",
      HOST_VM: "server",
      HOST_VM_CONFIG: "graal-core"
    }
  },

  graal_enterprise: {
    downloads+: labsjdk8.downloads,

    setup: common.setup + [
      ["git", "clone", ["mx", "urlrewrite", "https://github.com/graalvm/graal-enterprise.git"], "../graal-enterprise"],
      ["cd", "../graal-enterprise/graal-enterprise"],
      ["mx", "sforceimports"],
      ["mx", "sversions"],
      ["mx", "clean"], # Workaround for NFI
      ["mx", "build"],
      ["cd", "../../main"]
    ],

    environment+: truffleruby.environment + {
      GRAAL_HOME: "$PWD/../graal-enterprise/graal-enterprise",
      HOST_VM: "server",
      HOST_VM_CONFIG: "graal-enterprise"
    }
  },

  graal_enterprise_no_om: $.graal_enterprise + {
    environment+: truffleruby.environment + {
      HOST_VM_CONFIG: "graal-enterprise-no-om",
      JAVA_OPTS: java_opts + " -Dtruffle.object.LayoutFactory=com.oracle.truffle.object.basic.DefaultLayoutFactory"
    }
  },

  sulong: {
    downloads+: labsjdk8.downloads + {
      LIBGMP: {
        name: "libgmp",
        version: "6.1.0",
        platformspecific: true
      }
    },

    environment+: {
      CPPFLAGS: "-I$LIBGMP/include",
      LD_LIBRARY_PATH: "$LIBGMP/lib:$LLVM/lib:$LD_LIBRARY_PATH",
    },

    setup: [
      ["git", "clone", ["mx", "urlrewrite", "https://github.com/graalvm/sulong.git"], "../sulong"],
      ["cd", "../sulong"],
      ["mx", "sversions"],
      ["mx", "build"],
      ["cd", "../main"],
    ]
  },

  graal_vm_snapshot: {
    downloads+: {
      GRAALVM_DIR: {
        name: "graalvm-snapshot",
        version: "latest",
        platformspecific: true
      }
    },

    environment+: {
      GRAALVM_BIN: "$GRAALVM_DIR/bin/java",
      HOST_VM: "server",
      HOST_VM_CONFIG: "graal-vm-snap"
    }
  },

  local gem_test_pack = jt(["gem-test-pack"]),

  jruby_benchmark: {
    setup: common.prelude,

    downloads+: {
      JRUBY_HOME: {name: "jruby", version: "9.1.12.0"}
    },

    environment+: {
      HOST_VM: "server",
      HOST_VM_CONFIG: "default",
      GUEST_VM: "jruby",
      GUEST_VM_CONFIG: "indy",
      RUBY_BIN: "$JRUBY_HOME/bin/jruby",
      JT_BENCHMARK_RUBY: "$JRUBY_HOME/bin/jruby",
      JRUBY_OPTS: "-Xcompile.invokedynamic=true"
    }
  },

  mri_benchmark: {
    setup: common.prelude,

    downloads+: {
      MRI_HOME: {name: "ruby", version: "2.3.3"}
    },

    environment+: {
      HOST_VM: "mri",
      HOST_VM_CONFIG: "default",
      GUEST_VM: "mri",
      GUEST_VM_CONFIG: "default",
      RUBY_BIN: "$MRI_HOME/bin/ruby",
      JT_BENCHMARK_RUBY: "$MRI_HOME/bin/ruby"
    }
  },

  # Capabilities

  gate_caps: {
    capabilities: ["linux", "amd64"],
    targets: ["gate", "post-push"],
    environment+: {
      REPORT_GITHUB_STATUS: "true"
    }
  },

  gate_caps_darwin: $.gate_caps + {
    capabilities: ["darwin_sierra", "amd64"]
  },

  gate_caps_solaris: $.gate_caps + {
    capabilities: ["solaris", "sparcv9"]
  },

  local linux_amd64_bench = ["linux", "amd64", "no_frequency_scaling"],

  bench_caps: {
    capabilities: ["x52"] + linux_amd64_bench,
    targets: ["bench", "post-push"],
    timelimit: "02:00:00",
    environment+: {
      REPORT_GITHUB_STATUS: "true"
    }
  },

  svm_bench_caps: $.bench_caps + {
    capabilities: ["x52_18"] + linux_amd64_bench
  },

  daily_bench_caps: {
    capabilities: $.bench_caps.capabilities,
    targets: ["bench", "daily"],
    timelimit: "02:00:00"
  },

  daily_bench_caps_solaris: $.daily_bench_caps + {
    capabilities: ["m7_eighth", "solaris"]
  },

  weekly_bench_caps: $.daily_bench_caps + {
    targets: ["weekly"],
  },

  # Metrics and benchmarks

  local post_process = [
    ["tool/post-process-results-json.rb", "bench-results.json", "bench-results-processed.json"],
    # ["cat", "bench-results-processed.json"],
  ],
  local upload_results = [
    ["bench-uploader.py", "bench-results-processed.json"],
  ],
  local post_process_and_upload_results = post_process + upload_results + [
    ["tool/fail-if-any-failed.rb", "bench-results-processed.json"]
  ],
  local post_process_and_upload_results_wait = post_process + upload_results + [
    ["tool/fail-if-any-failed.rb", "bench-results-processed.json", "--wait"]
  ],

  local mx_bench = function(bench)
    [
      ["mx", "benchmark"] + (if std.type(bench) == "string" then [bench] else bench)
    ],
  local benchmark = function(benchs)
    if std.type(benchs) == "string" then
      error "benchs must be an array"
    else
      std.join(post_process_and_upload_results_wait, std.map(mx_bench, benchs)) +
        post_process_and_upload_results,

  metrics: {
    run: benchmark(["allocation", "minheap", "time"]),
    timelimit: "00:25:00"
  },

  compiler_metrics: {
    run: benchmark([
      "allocation:compile-mandelbrot",
      "minheap:compile-mandelbrot",
      "time:compile-mandelbrot"]),
    timelimit: "00:50:00"
  },

  svm_build_stats: {
    environment+: {
      GUEST_VM_CONFIG: "default"
    },
    run: benchmark(["build-stats"])
  },

  svm_metrics: {
    local run_benchs = benchmark([
      "instructions",
      ["time", "--", "--aot"],
      "maxrss"]),

    run: [
      ["export", "GUEST_VM_CONFIG=default"]
    ] + run_benchs + [
      ["export", "GUEST_VM_CONFIG=no-rubygems"],
      ["export", "TRUFFLERUBYOPT=--disable-gems"]
    ] + run_benchs,

    timelimit: "00:30:00"
  },

  classic_benchmarks: {
    run: benchmark(["classic"]),
    timelimit: "00:35:00"
  },
  classic_benchmarks_solaris: $.classic_benchmarks + {
    timelimit: "01:10:00"
  },

  chunky_benchmarks: {
    run: benchmark(["chunky"]),
    timelimit: "01:00:00"
  },

  psd_benchmarks: {
    run: benchmark(["psd"]),
    timelimit: "02:00:00"
  },

  asciidoctor_benchmarks: {
    run: benchmark(["asciidoctor"]),
    timelimit: "00:35:00"
  },

  other_benchmarks: {
    run: benchmark([
      "image-demo",
      "optcarrot",
      "savina",
      "synthetic",
      "micro"]),
    timelimit: "00:40:00"
  },

  other_benchmarks_svm: {
    run: benchmark([
      "image-demo",
      "optcarrot",
      "synthetic"]),
    timelimit: "01:00:00"
  },

  optcarrot_warmup: {
    run: jt(["benchmark", "--stable", "--elapsed",
             "--time", "300", "bench/optcarrot/optcarrot.rb"]),
    timelimit: "00:40:00"
  },

  server_benchmarks: {
    packages+: {
      "apache/ab": ">=2.3"
    },
    run: benchmark(["server"]),
    timelimit: "00:20:00"
  },
  other_ruby_server_benchmarks: $.server_benchmarks {
    run: benchmark([["server", "--", "--no-core-load-path"]])
  },
  svm_server_benchmarks: $.server_benchmarks {
    run: benchmark([["server", "--", "--aot"]])
  },

  cext_benchmarks: $.sulong + $.graal_core + {
    environment+: {
      TRUFFLERUBYOPT: "-Xcexts.log.load=true",
      USE_CEXTS: "true"
    },
    setup: $.graal_core.setup + $.sulong.setup + gem_test_pack +
      jt(["cextc", "bench/chunky_png/oily_png"]) +
      jt(["cextc", "bench/psd.rb/psd_native"]),
    run: benchmark(["chunky"]),
    timelimit: "02:00:00"
  },

  # Tests

  test_gems: {
    setup: common.setup + gem_test_pack,
    run: jt(["test", "gems"])
  },

  test_ecosystem: $.sulong {
    setup: common.setup + $.sulong.setup + gem_test_pack,
    run: jt(["test", "ecosystem"])
  },

  test_cexts: $.sulong {
    environment+: {
      JAVA_OPTS: java_opts + " -Dgraal.TruffleCompileOnly=nothing",
    },
    setup: common.setup + $.sulong.setup + gem_test_pack,
    run: [
      ["mx", "--dynamicimports", "sulong", "ruby_testdownstream_sulong"]
    ]
  },

  local test_compilation_flags = {
    environment+: {
      JAVA_OPTS: java_opts + " -Dgraal.TraceTruffleCompilation=true -Dgraal.TruffleCompilationExceptionsAreFatal=true"
    }
  },

  local without_rewrites = function(commands)
    [
      ["export", "PREV_MX_URLREWRITES=$MX_URLREWRITES"],
      ["unset", "MX_URLREWRITES"]
    ] + commands + [
      ["export", "MX_URLREWRITES=$PREV_MX_URLREWRITES"]
    ],

  local deploy_binaries_commands = [
    ["mx", "deploy-binary-if-master", "--skip-existing", "truffleruby-binary-snapshots"]
  ],

  local deploy_binaries_no_rewrites = without_rewrites(deploy_binaries_commands),

  local deploy_binaries = {
    run: deploy_binaries_commands + deploy_binaries_no_rewrites,
    timelimit: "30:00"
  },

  test_fast: {
    run: jt(["test", "fast"]),
    timelimit: "30:00"
  },

  deploy_and_test_fast: {
    run: deploy_binaries.run +
      jt(["test", "fast"]),
    timelimit: "30:00"
  },

  deploy_and_test_fast_darwin: {
    run: deploy_binaries.run +
      jt(["test", "fast", "-GdarwinCI"]),
    timelimit: "30:00"
  },

  lint: {
    downloads+: {
      JDT: {name: "ecj", version: "4.5.1", platformspecific: false}
    },
    packages+: {
      ruby: ">=2.1.0"
    },
    environment+: {
      # Truffle compiles with ECJ but does not run (GR-4720)
      TRUFFLERUBY_CEXT_ENABLED: "false"
    },
    setup: common.prelude,
    run: [
      # Build with ECJ to get warnings
      ["mx", "build", "--jdt", "$JDT", "--warning-as-error"],
    ] + jt(["lint"]),
    timelimit: "30:00"
  },

  local linux_gate = $.common_linux + $.gate_caps,

  local specs_job = [
    { name: "command-line", args: [":command_line"] },
    { name: "language", args: [":language", ":security"] },
    { name: "core", args: ["-Gci", ":core"] },
    { name: "library", args: [":library"] },
    { name: "truffle", args: [":truffle"] },
  ],

  tests_jobs: [
    {name: "ruby-deploy-and-test-fast-linux"} + linux_gate + $.deploy_and_test_fast,
    {name: "ruby-deploy-and-test-fast-darwin"} + $.common_darwin + $.gate_caps_darwin + $.deploy_and_test_fast_darwin,
    {name: "ruby-deploy-and-test-fast-solaris"} + $.common_solaris + $.gate_caps_solaris + $.deploy_and_test_fast,

    {name: "ruby-test-fast-java9-linux"} + linux_gate + labsjdk9 + $.test_fast,

    {name: "ruby-lint"} + linux_gate + $.lint,
    {name: "ruby-test-tck"} + linux_gate + {run: [["mx", "rubytck"]]},
  ] + [
    {name: "ruby-test-specs-" + config.name} + linux_gate + {run: jt(["test", "specs"] + config.args)},
    for config in specs_job
  ] + [
    {name: "ruby-test-mri"} + linux_gate + {run: jt(["test", "mri"])},
    {name: "ruby-test-integration"} + linux_gate + {run: jt(["test", "integration"])},
    {name: "ruby-test-cexts"} + linux_gate + $.test_cexts,
    {name: "ruby-test-gems"} + linux_gate + $.test_gems,
    {name: "ruby-test-bundle-no-sulong"} + linux_gate + {run: jt(["test", "bundle", "--no-sulong"])},
    {name: "ruby-test-ecosystem"} + linux_gate + $.test_ecosystem,

    {name: "ruby-test-compiler-graal-core"} + linux_gate + $.graal_core + {run: jt(["test", "compiler"])},
    # {name: "ruby-test-compiler-graal-enterprise"} + linux_gate + $.graal_enterprise + {run: jt(["test", "compiler"])},
    # {name: "ruby-test-compiler-graal-vm-snapshot"} + linux_gate + $.graal_vm_snapshot + {run: jt(["test", "compiler"])},

    {name: "ruby-test-compiler-graal-core-java9"} + linux_gate + $.graal_core + labsjdk9 + {run: jt(["test", "compiler"])},
    {name: "ruby-test-compiler-standalone-java9"} + linux_gate + $.graal_core + labsjdk9 + {run: ["bin/truffleruby", "-J-XX:+UnlockExperimentalVMOptions", "-J-XX:+EnableJVMCI", "-J--module-path=../graal/sdk/mxbuild/modules/org.graalvm.graal_sdk.jar:../graal/truffle/mxbuild/modules/com.oracle.truffle.truffle_api.jar:../graal/compiler/mxbuild/modules/jdk.internal.vm.compiler.jar", "-J-Dgraal.TraceTruffleCompilation=true", "-e", "raise 'no graal' unless Truffle.graal?"]},
  ],

  local other_rubies = [
    { name: "mri",   caps: $.weekly_bench_caps, setup: $.mri_benchmark,   kind: "other" },
    { name: "jruby", caps: $.weekly_bench_caps, setup: $.jruby_benchmark, kind: "other" },
  ],

  local graal_configs = [
    # { name: "no-graal",               caps: $.weekly_bench_caps, setup: $.no_graal,               kind: "graal"  },
    { name: "graal-core",             caps: $.bench_caps,        setup: $.graal_core,             kind: "graal" },
    { name: "graal-enterprise",       caps: $.daily_bench_caps,  setup: $.graal_enterprise,       kind: "graal" },
    { name: "graal-enterprise-no-om", caps: $.daily_bench_caps,  setup: $.graal_enterprise_no_om, kind: "graal" },
    # { name: "graal-vm-snapshot",      caps: $.bench_caps,        setup: $.graal_vm_snapshot,      kind: "graal" },
  ],
  local bench_configs_no_svm = other_rubies + graal_configs,

  local solaris_bench_configs = [
    { name: "graal-core-solaris",       caps: $.daily_bench_caps_solaris, setup: $.graal_core },
    { name: "graal-enterprise-solaris", caps: $.daily_bench_caps_solaris, setup: $.graal_enterprise },
  ],

  local benchmark_sets = [
    { name: "chunky",       job: $.chunky_benchmarks },
    { name: "psd",          job: $.psd_benchmarks },
    { name: "asciidoctor",  job: $.asciidoctor_benchmarks },
    { name: "other",        job: $.other_benchmarks, svm: $.other_benchmarks_svm },
  ],

  generate_benchmarks_jobs:: function(svm_configs)
    local bench_configs = bench_configs_no_svm + svm_configs;

    local metrics_jobs = [
      {name: "ruby-metrics-truffle"} + $.common_linux + $.bench_caps + no_graal + $.metrics,
    ] + [
      {name: "ruby-metrics-compiler-" + config.name} + $.common_linux + config.caps + config.setup + $.compiler_metrics,
      for config in graal_configs
    ] + [
      {name: "ruby-build-stats-" + config.name} + $.common_linux + config.caps + config.setup + $.svm_build_stats,
      for config in svm_configs
    ] + [
      {name: "ruby-metrics-" + config.name} + $.common_linux + $.svm_bench_caps + config.setup + $.svm_metrics,
      for config in svm_configs
    ];

    local benchmarks_jobs = [
      {name: "ruby-benchmarks-classic-" + config.name} + $.common_linux + config.caps + config.setup + $.classic_benchmarks +
        {[if config.kind == "svm" then "timelimit"]: "01:10:00"},
      for config in bench_configs
    ] + [
      {name: "ruby-benchmarks-classic-" + config.name} + $.common_solaris + config.caps + config.setup + $.classic_benchmarks_solaris,
      for config in solaris_bench_configs
    ] + [
      {name: "ruby-benchmarks-" + bench.name + "-" + config.name} + $.common_linux + config.caps + config.setup +
        (if std.objectHas(bench, "svm") && config.kind == "svm" then bench.svm else bench.job),
      for bench in benchmark_sets
      for config in bench_configs
    ] + [
      {name: "ruby-benchmarks-server-" + config.name} + $.common_linux + config.caps + config.setup +
        (if config.kind == "other" then $.other_ruby_server_benchmarks else $.server_benchmarks),
      for config in bench_configs_no_svm
    ] + [
      # SVM currently fails these due to threading issue
      # {name: "ruby-benchmarks-server-" + config.name} + $.common_linux + config.caps + config.setup + $.svm_server_benchmarks,
      # for config in svm_configs
    ] + [
      {name: "ruby-benchmarks-cext"} + $.common_linux + $.daily_bench_caps + $.cext_benchmarks + truffleruby_cexts,
      # {name: "ruby-benchmarks-cext-mri"} + $.common_linux + $.weekly_bench_caps + $.cext_benchmarks + $.mri_benchmark,
    ];

    metrics_jobs + benchmarks_jobs,

  # To allow this file to resolve standalone
  builds: self.tests_jobs + self.generate_benchmarks_jobs([])
}
