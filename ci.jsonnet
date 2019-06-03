# File is formatted with
# `jsonnet fmt --indent 2 --max-blank-lines 2 --sort-imports --string-style d --comment-style h -i ci.jsonnet`

# The Visual Studio Code is recommended for editing jsonnet files.
# It has a 'jsonnet' plugin with a output preview. Alternatively Atom can be used.
#
# Formatter is part of `jsonnet` command, it can be installed with
# `brew install jsonnet` on mac or with http://linuxbrew.sh/ on a Linux machine
# with the same command. Or it can be downloaded from
# https://github.com/google/jsonnet/releases and compiled.

# CONFIGURATION
local overlay = "b6998e9380084ade695dbb160e8406bdd6441a93";

# For debugging: generated builds will be restricted to those listed in
# the array. No restriction is applied when it is empty.
local restrict_builds_to = [];

# Import support functions, they can be replaced with identity functions
# and it would still work.
local utils = import "utils.libsonnet";

# All builds are composed **directly** from **independent disjunct composable**
# jsonnet objects defined in here. Use `+:` to make the objects or arrays
# stored in fields composable, see http://jsonnet.org/docs/tutorial.html#oo.
# All used objects used to compose a build are listed
# where build is defined, there are no other objects in the middle.
local part_definitions = {
  local jt = function(args) [["ruby", "tool/jt.rb"] + args],

  use: {
    common: {
      environment+: {
        path+:: [],
        java_opts+:: ["-Xmx2G"],
        TRUFFLERUBY_CI: "true",
        RUBY_BENCHMARKS: "true",
        JAVA_OPTS: std.join(" ", self.java_opts),
        PATH: std.join(":", self.path + ["$PATH"]),
      },

      packages+: {
        "pip:ninja_syntax": "==1.7.2", # Required by NFI and mx
      },

      setup+: [
        # We don't want to proxy any internet access
        ["unset", "ANT_OPTS", "FTP_PROXY", "ftp_proxy", "GRADLE_OPTS",
          "HTTPS_PROXY", "https_proxy", "HTTP_PROXY", "http_proxy",
          "MAVEN_OPTS", "no_proxy"]
      ],
    },

    svm: {
      downloads+: {
        GDB: { name: "gdb", version: "7.11.1", platformspecific: true },
      },

      environment+: {
        GDB_BIN: "$GDB/bin/gdb",
        HOST_VM: "svm",
      },
    },

    maven: {
      downloads+: {
        MAVEN_HOME: { name: "maven", version: "3.3.9" },
      },

      environment+: {
        path+:: ["$MAVEN_HOME/bin"],
      },
    },

    build: {
      setup+: [
        ["mx", "sversions"],
      ] + self.before_build + jt(["build"]) + self.after_build,
    },

    truffleruby: {
      "$.benchmark.server":: { options: [] },
      environment+: {
        # Using jruby for compatibility with existing benchmark results.
        GUEST_VM: "jruby",
        GUEST_VM_CONFIG: "truffle",
      },
    },

    truffleruby_cexts: {
      is_after+:: ["$.use.truffleruby"],
      environment+: {
        # to differentiate running without (chunky_png) and with cexts (oily_png).
        GUEST_VM_CONFIG+: "-cexts",
      },
    },

    gem_test_pack: {
      setup+: jt(["gem-test-pack"]),
    },

    mri: {
      "$.benchmark.server":: { options: ["--", "--no-core-load-path"] },
      downloads+: {
        MRI_HOME: { name: "ruby", version: "2.3.3" },
      },

      environment+: {
        HOST_VM: "mri",
        HOST_VM_CONFIG: "default",
        GUEST_VM: "mri",
        GUEST_VM_CONFIG: "default",
        RUBY_BIN: "$MRI_HOME/bin/ruby",
        JT_BENCHMARK_RUBY: "$MRI_HOME/bin/ruby",
      },
    },

    jruby: {
      "$.benchmark.server":: { options: ["--", "--no-core-load-path"] },
      downloads+: {
        JRUBY_HOME: { name: "jruby", version: "9.1.12.0" },
      },

      environment+: {
        HOST_VM: "server",
        HOST_VM_CONFIG: "default",
        GUEST_VM: "jruby",
        GUEST_VM_CONFIG: "indy",
        RUBY_BIN: "$JRUBY_HOME/bin/jruby",
        JT_BENCHMARK_RUBY: "$JRUBY_HOME/bin/jruby",
        JRUBY_OPTS: "-Xcompile.invokedynamic=true",
      },
    },
  },

  graal: {
    core: {
      is_after+:: ["$.use.build"],
      setup+: jt(["build", "--graal"]),

      environment+: {
        HOST_VM: "server",
        HOST_VM_CONFIG: "graal-core",
      },
    },

    none: {
      environment+: {
        HOST_VM: "server",
        HOST_VM_CONFIG: "default",
      },
    },

    enterprise: {
      is_after+:: ["$.use.build"],

      clone::
      local url = ["mx", "urlrewrite", "https://github.com/graalvm/graal-enterprise.git"],
            repo = "../graal-enterprise",
            suite_file = "graal-enterprise/mx.graal-enterprise/suite.py",
            # Find the latest merge commit of a pull request in the graal repo, equal or older than our graal import.
            merge_commit_in_graal = ["git", "-C", "../graal", "log", "--pretty=%H", "--grep=PullRequest:", "--merges", "-n1"] + jt(["truffle_version"]),
            # Find the commit importing that version of graal in graal-enterprise by looking at the suite file.
            # The suite file is automatically updated on every graal PR merged.
            graal_enterprise_commit = ["git", "-C", repo, "log", "--pretty=%H", "--grep=PullRequest:", "--reverse", "-m", "-S", merge_commit_in_graal, suite_file, "|", "head", "-1"];
      [
        ["git", "clone", url, repo],
        ["git", "-C", repo, "checkout", graal_enterprise_commit],
      ],

      setup+: self.clone + jt(["build", "--dy", "/graal-enterprise"]),

      environment+: {
        HOST_VM: "server",
        HOST_VM_CONFIG: "graal-enterprise",
      },
    },

    without_om: {
      is_after+:: ["$.graal.enterprise", "$.use.common"],
      environment+: {
        HOST_VM_CONFIG+: "-no-om",
        java_opts+::
          ["-Dtruffle.object.LayoutFactory=com.oracle.truffle.object.basic.DefaultLayoutFactory"],
      },
    },
  },

  svm: {
    core: {
      is_after+:: ["$.use.build"],

      svm_suite:: "/substratevm",

      environment+: {
        HOST_VM_CONFIG: "graal-core",
        VM_SUITE_HOME: "$BUILD_DIR/graal/vm",
      },
    },

    enterprise: {
      is_after+:: ["$.use.build"],

      svm_suite:: "/substratevm-enterprise",

      setup+: $.graal.enterprise.clone,

      environment+: {
        HOST_VM_CONFIG: "graal-enterprise",
        VM_SUITE_HOME: "$BUILD_DIR/graal-enterprise/vm-enterprise",
      },
    },

    gate: {
      # The same as job "gate-vm-native-truffleruby-tip" in
      # https://github.com/oracle/graal/blob/master/vm/ci_common/common.hocon
      local build = self,
      run+: [
        ["cd", "$VM_SUITE_HOME"],
      ] + self.before_build + [
        [
          "mx",
          "--dynamicimports",
          self.svm_suite + ",truffleruby",
          "--disable-polyglot",
          "--disable-libpolyglot",
          "--force-bash-launchers=lli,native-image",
          "gate",
          "--no-warning-as-error",
          "--tags",
          build["$.svm.gate"].tags,
        ],
      ] + self.after_build,
    },

    build_image: {
      local vm_suite_mx_flags = [
        "--dynamicimports",
        self.svm_suite + ",truffleruby",
        "--disable-polyglot",
        "--disable-libpolyglot",
        "--force-bash-launchers=lli,native-image",
      ],
      local vm_build = ["mx"] + vm_suite_mx_flags + ["build"],
      local vm_dist_home = ["mx"] + vm_suite_mx_flags + ["graalvm-home"],

      setup+: [
        ["cd", "$VM_SUITE_HOME"],
      ] + self.before_build + [
        # aot-build.log is used for the build-stats metrics
        vm_build + ["|", "tee", "../../main/aot-build.log"],
      ] + self.after_build + [
        ["set-export", "VM_DIST_HOME", vm_dist_home],
        ["set-export", "AOT_BIN", "$VM_DIST_HOME/jre/languages/ruby/bin/truffleruby"],
        ["set-export", "JT_BENCHMARK_RUBY", "$AOT_BIN"],
        ["cd", "../../main"],
      ],
    },
  },

  jdk: {
    labsjdk8: {
      downloads+: {
        JAVA_HOME: {
          name: "oraclejdk",
          version: "8u212-jvmci-20-b01",
          platformspecific: true,
        },
      },

      environment+: {
        path+:: ["$JAVA_HOME/bin"],
      },
    },

    openjdk8: {
      downloads+: {
        JAVA_HOME: {
          name: "openjdk",
          version: "8u212-jvmci-20-b01",
          platformspecific: true,
        },
      },

      environment+: {
        path+:: ["$JAVA_HOME/bin"],
      },
    },
  },

  platform: {
    linux: {
      before_build:: [],
      after_build:: [],
      "$.run.specs":: { test_spec_options: ["-Gci"] },
      "$.cap":: {
        normal_machine: ["linux", "amd64"],
        bench_machine: ["x52"] + self.normal_machine + ["no_frequency_scaling"],
      },
      packages+: {
        git: ">=1.8.3",
        mercurial: ">=3.2.4",
        ruby: ">=2.0.0",
        llvm: "==3.8",
      },
    },
    darwin: {
      # Only set LLVM bin on PATH during "mx build" as Sulong needs it.
      # Unset it for everything else to test how end-user might run TruffleRuby.
      before_build:: [
        ["set-export", "PATH_WITHOUT_LLVM", "$PATH"],
        ["set-export", "PATH", "/usr/local/opt/llvm@4/bin:$PATH"],
      ],
      after_build:: [
        ["set-export", "PATH", "$PATH_WITHOUT_LLVM"],
      ],
      "$.run.specs":: { test_spec_options: ["-GdarwinCI"] },
      "$.cap":: {
        normal_machine: ["darwin_mojave", "amd64"],
      },
      environment+: {
        LANG: "en_US.UTF-8",
      },
    },
  },

  cap: {
    gate: {
      capabilities+: self["$.cap"].normal_machine,
      targets+: ["gate"],
      notify_emails: false,
    },
    bench: { capabilities+: self["$.cap"].bench_machine },
    x52_18_override: {
      is_after+:: ["$.cap.bench"],
      capabilities: if std.count(super.capabilities, "x52") > 0
      then std.map(function(c) if c == "x52" then "x52_18" else c,
                   super.capabilities)
      else error "trying to override x52 but it is missing",
    },
    daily: { targets+: ["bench", "daily"] },
    weekly: { targets+: ["weekly"] },
    manual: {
      capabilities+: self["$.cap"].normal_machine,
      targets: [],
    },
  },

  run: {
    test_unit_tck_specs: {
      run+: [
        ["mx", "unittest", "org.truffleruby"],
        ["mx", "tck"],
      ] + jt(["test", "specs"] + self["$.run.specs"].test_spec_options),
      # + jt(["test", "specs", ":next"]) disabled as it's currently empty and MSpec doesn't support empty sets of files
    },

    test_fast: {
      run+: jt(["test", "fast"]),
    },

    lint: {
      packages+: {
        ruby: ">=2.1.0",
        "pip:pylint": "==1.9.0",
      },
      run+: [
        ["mx", "sversions"],
      ] + self.before_build + [
        ["mx", "build", "--warning-as-error", "--force-deprecation-as-warning"],
      ] + jt(["lint"]) + self.after_build,
    },

    test_basictest: { run+: jt(["test", "basictest"]) },
    test_mri: { run+: jt(["test", "mri", "--no-sulong", "--", "-j4"]) },
    test_integration: { run+: jt(["test", "integration"]) },
    test_gems: { run+: jt(["test", "gems"]) },
    test_ecosystem: { run+: jt(["test", "ecosystem"]) },
    test_compiler: { run+: jt(["test", "compiler"]) },

    test_cexts: {
      is_after+:: ["$.use.common"],
      run+: [
        ["mx", "--dynamicimports", "/sulong", "ruby_testdownstream_sulong"],
      ],
    },

    test_make_standalone_distribution: {
      run+: [
        ["tool/make-standalone-distribution.sh"],
      ],
    },
  },

  benchmark: {
    local post_process =
      [["tool/post-process-results-json.rb", "bench-results.json", "bench-results-processed.json"]],
    local upload_results =
      [["bench-uploader.py", "bench-results-processed.json"]],
    local post_process_and_upload_results =
      post_process + upload_results +
      [["tool/fail-if-any-failed.rb", "bench-results-processed.json"]],
    local post_process_and_upload_results_wait =
      post_process + upload_results +
      [["tool/fail-if-any-failed.rb", "bench-results-processed.json", "--wait"]],
    local mx_benchmark = function(bench)
      [["mx", "benchmark"] + if std.type(bench) == "string" then [bench] else bench],

    local benchmark = function(benchs)
      if std.type(benchs) == "string" then
        error "benchs must be an array"
      else
        std.join(post_process_and_upload_results_wait, std.map(mx_benchmark, benchs)) +
        post_process_and_upload_results,

    runner: {
      local benchmarks_to_run = self.benchmarks,
      run+: std.join(
              post_process_and_upload_results_wait,
              std.map(mx_benchmark, benchmarks_to_run)
            ) +
            post_process_and_upload_results,
    },

    interpreter_metrics: {
      benchmarks+:: [
        "allocation:hello",
        "minheap:hello",
        "time:hello"
      ],
    },

    compiler_metrics: {
      # Run all metrics benchmarks: hello and compile-mandelbrot
      benchmarks+:: [
        "allocation",
        "minheap",
        "time",
      ]
    },

    # TODO not compose-able, it would had be broken up to 2 builds
    run_svm_metrics: {
      local run_benchs = benchmark([
        "instructions",
        ["time", "--", "--native"],
        "maxrss",
      ]),

      run: [
        ["set-export", "GUEST_VM_CONFIG", "default"],
      ] + run_benchs + [
        ["set-export", "GUEST_VM_CONFIG", "no-rubygems"],
        ["set-export", "TRUFFLERUBYOPT", "--disable-gems"],
      ] + run_benchs,
    },

    svm_build_stats: { benchmarks+:: ["build-stats"] },

    classic: { benchmarks+:: ["classic"] },
    chunky: { benchmarks+:: ["chunky"] },
    psd: { benchmarks+:: ["psd"] },
    asciidoctor: { benchmarks+:: ["asciidoctor"] },
    other_extra: { benchmarks+:: ["savina", "micro"] },
    other: { benchmarks+:: ["image-demo", "optcarrot", "synthetic"] },

    server: {
      local build = self,
      packages+: {
        "apache/ab": ">=2.3",
      },
      benchmarks+:: [["server"] + build["$.benchmark.server"].options],
    },

    cext_chunky: {
      environment+: {
        TRUFFLERUBYOPT: "--experimental-options --cexts-log-load",
        USE_CEXTS: "true",
      },
      setup+:
        jt(["cextc", "bench/chunky_png/oily_png"]) + jt(["cextc", "bench/psd.rb/psd_native"]),
      benchmarks+:: ["chunky"],
    },
  },
};

# composition_environment inherits from part_definitions all building blocks
# (parts) can be addressed using jsonnet root syntax e.g. $.use.maven
local composition_environment = utils.add_inclusion_tracking(part_definitions, "$", false) {

  test_builds:
    {
      "ruby-lint": $.platform.linux + $.cap.gate + $.jdk.labsjdk8 + $.use.common + $.run.lint + { timelimit: "30:00" },
    } +

    {
      local linux_gate = $.platform.linux + $.cap.gate + $.jdk.labsjdk8 + $.use.common + $.use.build + { timelimit: "01:00:00" },

      "ruby-test-specs-linux":       linux_gate + $.run.test_unit_tck_specs + $.run.test_basictest + { timelimit: "35:00" },
      "ruby-test-fast-linux":        linux_gate + $.run.test_fast + { timelimit: "30:00" }, # To catch missing slow tags
      "ruby-test-mri-linux":         linux_gate + $.run.test_mri + { timelimit: "30:00" },
      "ruby-test-integration-linux": linux_gate + $.run.test_integration,
      "ruby-test-cexts-linux":       linux_gate + $.use.gem_test_pack + $.run.test_cexts,
      "ruby-test-gems-linux":        linux_gate + $.use.gem_test_pack + $.run.test_gems,
      "ruby-test-ecosystem-linux":   linux_gate + $.use.gem_test_pack + $.run.test_ecosystem,
      "ruby-test-standalone-linux":  linux_gate + $.run.test_make_standalone_distribution + { timelimit: "40:00" },

      "ruby-test-compiler-graal-core": linux_gate + $.use.truffleruby + $.graal.core + $.run.test_compiler,
      # TODO was commented out, needs to be rewritten?
      # {name: "ruby-test-compiler-graal-enterprise"} + linux_gate + $.graal_enterprise + {run: jt(["test", "compiler"])},
    } +

    {
      local darwin_gate = $.platform.darwin + $.cap.gate + $.jdk.labsjdk8 + $.use.common + $.use.build + { timelimit: "01:00:00" },

      "ruby-test-specs-darwin": darwin_gate + $.run.test_unit_tck_specs + $.run.test_basictest + { timelimit: "50:00" },
      "ruby-test-mri-darwin":   darwin_gate + $.run.test_mri,
      "ruby-test-cexts-darwin": darwin_gate + $.use.gem_test_pack + $.run.test_cexts,
      "ruby-test-gems-darwin":  darwin_gate + $.use.gem_test_pack + $.run.test_gems,
    } +

    local svm_test_shared = $.jdk.labsjdk8 + $.use.common + $.use.svm + $.cap.gate + $.svm.gate;
    {
      local shared = $.use.build + $.svm.core + {
        "$.svm.gate":: {
          tags: "build,ruby"
        },
        environment+: {
          EXTRA_IMAGE_BUILDER_ARGUMENTS: "-H:GreyToBlackObjectVisitorDiagnosticHistory=16", # GR-9912
        },
        timelimit: "01:00:00",
      },

      "ruby-test-svm-graal-core-linux": $.platform.linux + svm_test_shared + shared,
      "ruby-test-svm-graal-core-darwin": $.platform.darwin + svm_test_shared + shared,
    } + {
      local shared = $.use.build + $.svm.enterprise + { timelimit: "01:15:00" },

      "ruby-test-svm-graal-enterprise-linux": $.platform.linux + svm_test_shared + shared + {
        "$.svm.gate":: {
          tags: "build,ruby_product"
        },
        environment+: {
          EXTRA_IMAGE_BUILDER_ARGUMENTS: "-H:+VerifyGraalGraphs -H:+VerifyPhases -H:+AOTPriorityInline -H:+VerifyDeoptimizationEntryPoints",
        },
      },
      "ruby-test-svm-graal-enterprise-darwin": $.platform.darwin + svm_test_shared + shared + {
        "$.svm.gate":: {
          tags: "build,darwin_ruby"
        },
        # No extra verifications, this CI job is already slow
      },
    },

  local other_rubies = {
    mri: $.use.mri + $.cap.bench + $.cap.weekly,
    jruby: $.use.jruby + $.cap.bench + $.cap.weekly,
  },
  local graal_configurations = {
    local shared = $.use.truffleruby + $.use.build + $.cap.daily + $.cap.bench,

    "graal-core": shared + $.graal.core,
    "graal-enterprise": shared + $.graal.enterprise,
    "graal-enterprise-no-om": shared + $.graal.enterprise + $.graal.without_om,
  },
  local svm_configurations = {
    local shared = $.cap.bench + $.cap.daily + $.use.truffleruby + $.use.build + $.use.svm,

    "svm-graal-core": shared + $.svm.core + $.svm.build_image,
    "svm-graal-enterprise": shared + $.svm.enterprise + $.svm.build_image,
  },

  bench_builds:
    {
      local shared = $.platform.linux + $.jdk.labsjdk8 + $.use.common +
                     $.benchmark.runner + $.benchmark.compiler_metrics + { timelimit: "00:50:00" },

      "ruby-metrics-compiler-graal-core": shared + graal_configurations["graal-core"],
      "ruby-metrics-compiler-graal-enterprise": shared + graal_configurations["graal-enterprise"],
      "ruby-metrics-compiler-graal-enterprise-no-om": shared + graal_configurations["graal-enterprise-no-om"],
    } +

    {
      local shared = $.platform.linux + $.jdk.labsjdk8 + $.use.common +
                     $.benchmark.runner + $.benchmark.svm_build_stats + { timelimit: "02:00:00" },
      # TODO this 2 jobs have GUEST_VM_CONFIG: 'default' instead of 'truffle', why?
      local guest_vm_override = { environment+: { GUEST_VM_CONFIG: "default" } },

      "ruby-build-stats-svm-graal-core": shared + svm_configurations["svm-graal-core"] + guest_vm_override,
      "ruby-build-stats-svm-graal-enterprise": shared + svm_configurations["svm-graal-enterprise"] + guest_vm_override,
    } +

    {
      local shared = $.platform.linux + $.jdk.labsjdk8 + $.use.common +
                     $.benchmark.run_svm_metrics + { timelimit: "00:30:00" },

      "ruby-metrics-svm-graal-core": shared + svm_configurations["svm-graal-core"] + $.cap.x52_18_override,
      "ruby-metrics-svm-graal-enterprise": shared + svm_configurations["svm-graal-enterprise"] + $.cap.x52_18_override,
    } +

    {
      local shared = $.platform.linux + $.jdk.labsjdk8 + $.use.common +
                     $.benchmark.runner + $.benchmark.classic,

      "ruby-benchmarks-classic-mri": shared + other_rubies.mri + { timelimit: "00:35:00" },
      "ruby-benchmarks-classic-jruby": shared + other_rubies.jruby + { timelimit: "00:35:00" },
      "ruby-benchmarks-classic-graal-core": shared + graal_configurations["graal-core"] + { timelimit: "00:35:00" },
      "ruby-benchmarks-classic-graal-enterprise": shared + graal_configurations["graal-enterprise"] + { timelimit: "00:35:00" },
      "ruby-benchmarks-classic-graal-enterprise-no-om": shared + graal_configurations["graal-enterprise-no-om"] + { timelimit: "00:35:00" },
      "ruby-benchmarks-classic-svm-graal-core": shared + svm_configurations["svm-graal-core"] + { timelimit: "01:10:00" },
      "ruby-benchmarks-classic-svm-graal-enterprise": shared + svm_configurations["svm-graal-enterprise"] + { timelimit: "01:10:00" },
    } +

    {
      local shared = $.platform.linux + $.jdk.labsjdk8 + $.use.common,

      local chunky = $.benchmark.runner + $.benchmark.chunky + { timelimit: "01:00:00" },
      "ruby-benchmarks-chunky-mri": shared + chunky + other_rubies.mri,
      "ruby-benchmarks-chunky-jruby": shared + chunky + other_rubies.jruby,
      "ruby-benchmarks-chunky-graal-core": shared + chunky + graal_configurations["graal-core"],
      "ruby-benchmarks-chunky-graal-enterprise": shared + chunky + graal_configurations["graal-enterprise"],
      "ruby-benchmarks-chunky-graal-enterprise-no-om": shared + chunky + graal_configurations["graal-enterprise-no-om"],
      local psd = $.benchmark.runner + $.benchmark.psd + { timelimit: "02:00:00" },
      "ruby-benchmarks-psd-mri": shared + psd + other_rubies.mri,
      "ruby-benchmarks-psd-jruby": shared + psd + other_rubies.jruby,
      "ruby-benchmarks-psd-graal-core": shared + psd + graal_configurations["graal-core"],
      "ruby-benchmarks-psd-graal-enterprise": shared + psd + graal_configurations["graal-enterprise"],
      "ruby-benchmarks-psd-graal-enterprise-no-om": shared + psd + graal_configurations["graal-enterprise-no-om"],
      "ruby-benchmarks-psd-svm-graal-core": shared + psd + svm_configurations["svm-graal-core"],
      "ruby-benchmarks-psd-svm-graal-enterprise": shared + psd + svm_configurations["svm-graal-enterprise"],
      local asciidoctor = $.benchmark.runner + $.benchmark.asciidoctor + { timelimit: "00:55:00" },
      "ruby-benchmarks-asciidoctor-mri": shared + asciidoctor + other_rubies.mri,
      "ruby-benchmarks-asciidoctor-jruby": shared + asciidoctor + other_rubies.jruby,
      "ruby-benchmarks-asciidoctor-graal-core": shared + asciidoctor + graal_configurations["graal-core"],
      "ruby-benchmarks-asciidoctor-graal-enterprise": shared + asciidoctor + graal_configurations["graal-enterprise"],
      "ruby-benchmarks-asciidoctor-graal-enterprise-no-om": shared + asciidoctor + graal_configurations["graal-enterprise-no-om"],
      "ruby-benchmarks-asciidoctor-svm-graal-core": shared + asciidoctor + svm_configurations["svm-graal-core"],
      "ruby-benchmarks-asciidoctor-svm-graal-enterprise": shared + asciidoctor + svm_configurations["svm-graal-enterprise"],
      local other = $.benchmark.runner + $.benchmark.other + $.benchmark.other_extra + { timelimit: "00:40:00" },
      local svm_other = $.benchmark.runner + $.benchmark.other + { timelimit: "01:00:00" },
      "ruby-benchmarks-other-mri": shared + other + other_rubies.mri,
      "ruby-benchmarks-other-jruby": shared + other + other_rubies.jruby,
      "ruby-benchmarks-other-graal-core": shared + other + graal_configurations["graal-core"],
      "ruby-benchmarks-other-graal-enterprise": shared + other + graal_configurations["graal-enterprise"],
      "ruby-benchmarks-other-graal-enterprise-no-om": shared + other + graal_configurations["graal-enterprise-no-om"],
      "ruby-benchmarks-other-svm-graal-core": shared + svm_other + svm_configurations["svm-graal-core"],
      "ruby-benchmarks-other-svm-graal-enterprise": shared + svm_other + svm_configurations["svm-graal-enterprise"],
    } +

    {
      local shared = $.platform.linux + $.jdk.labsjdk8 + $.use.common +
                     $.benchmark.runner + $.benchmark.server +
                     { timelimit: "00:20:00" },

      "ruby-benchmarks-server-mri": shared + other_rubies.mri,
      "ruby-benchmarks-server-jruby": shared + other_rubies.jruby,
      "ruby-benchmarks-server-graal-core": shared + graal_configurations["graal-core"],
      "ruby-benchmarks-server-graal-enterprise": shared + graal_configurations["graal-enterprise"],
      "ruby-benchmarks-server-graal-enterprise-no-om": shared + graal_configurations["graal-enterprise-no-om"],
    } +

    {
      "ruby-metrics-truffle":
        $.platform.linux + $.jdk.labsjdk8 + $.use.common + $.use.build +
        $.use.truffleruby + $.graal.none +
        $.cap.bench + $.cap.daily +
        $.benchmark.runner + $.benchmark.interpreter_metrics +
        { timelimit: "00:25:00" },
    } +

    {
      "ruby-benchmarks-cext":
        $.platform.linux + $.jdk.labsjdk8 + $.use.common +
        $.use.truffleruby + $.use.truffleruby_cexts +
        $.use.build + $.graal.core + $.use.gem_test_pack +
        $.cap.bench + $.cap.daily +
        $.benchmark.runner + $.benchmark.cext_chunky +
        { timelimit: "02:00:00" },
    },

  builds:
    local all_builds = $.test_builds + $.bench_builds;
    utils.check_builds(
      restrict_builds_to,
      # Move name inside into `name` field
      # and ensure timelimit is present
      [
        all_builds[k] {
          name: k,
          timelimit: if std.objectHas(all_builds[k], "timelimit")
          then all_builds[k].timelimit
          else error "Missing timelimit in " + k + " build.",
        }
        for k in std.objectFields(all_builds)
      ]
    ),
};

{
  overlay: overlay,
  builds: composition_environment.builds,
}

/**

# Additional notes

## Structure

- All builds are composed from disjunct parts.
- Part is just an jsonnet object which includes a part of a build declaration
  so fields like setup, run, etc.
- All parts are defined in part_definitions in groups like
  platform, graal, etc.
  - They are always nested one level deep therefore
    $.<group_name>.<part_name>
- Each part is included in a build at most once. A helper function is
  checking it and will raise an error otherwise with a message
  `Parts ["$.use.maven", "$.use.maven"] are used more than once in build: ruby-metrics-svm-graal-core. ...`
- All parts have to be disjoint and distinct.
- All parts have to be compose-able with each other. Therefore all fields
  like setup or run should end with '+' to avoid overriding.
  - An example of nested compose-able field (e.g. PATH in an environment)
    can be found in `$.use.common.environment`, look for `path::` and `PATH:`

### Exceptions

- In few cases values in a part (A) depend on values provided in another
  part (O), therefore (A) needs (O) to also be used in a build.
  - The inter part dependencies should be kept to minimum.
  - Use name of the part (A) for the field in (O).
  - See `$.platform` parts for an example.
  - If (A) is used without (O) an error is risen that a field
    (e.g. '$.run.deploy_and_spec') is missing which makes it easy to look up
    which inter dependency is broken.
- Few parts depend on othering with other parts, in this case
  they have `is_after+:: ['$.a_group.a_name']` (or `is_before`) which ensures
  the required part are included in correct order. If the dependecy can be
  omitted use `is_after_optional+::` instead.
  See $.use.truffleruby_cexts, $.graal.enterprise

## How to edit

- The composition of builds is intentionally kept very flat.
- All parts included in the build are listed where the build is being defined.
- Since parts do not inherit from each other or use each others' fields
  there is no need to track down what is composed of what.
- Each manifested build has included_parts field with names of all
  the included parts, which makes it clear what the build is
  using and doing.
  - The included parts is also stored in environment variable
    PARTS_INCLUDED_IN_BUILD therefore printed in each build log.
- The structure rules exist to simplify thinking about the build, to find
  out the composition of any build one needs to only investigate all named
  parts in the definition, where the name tells its placement in
  part_definitions object. Nothing else is in the build.
- When a part is edited, it can be easily looked up where it's used just by
  using its full name (e.g. $.run.deploy_and_spec). It's used nowhere else.

 */
