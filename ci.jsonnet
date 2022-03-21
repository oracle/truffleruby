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
local overlay = "ffb092a26e86b0b4c2ac33bdbc329d938b527942";

# For debugging: generated builds will be restricted to those listed in
# the array. No restriction is applied when it is empty.
local restrict_builds_to = [];

# Import support functions, they can be replaced with identity functions
# and it would still work.
local utils = import "utils.libsonnet";

local common = import "common.json";

# All builds are composed **directly** from **independent disjunct composable**
# jsonnet objects defined in here. Use `+:` to make the objects or arrays
# stored in fields composable, see http://jsonnet.org/docs/tutorial.html#oo.
# All used objects used to compose a build are listed
# where build is defined, there are no other objects in the middle.
local part_definitions = {
  local jt = function(args) [["bin/jt"] + args],
  local mri_path = function(version) "/cm/shared/apps/ruby/" + version + "/bin/ruby",
  local mri_version = "3.0.2",

  use: {
    common: {
      environment+: {
        path+:: [],
        TRUFFLERUBY_CI: "true",
        RUBY_BENCHMARKS: "true",
        MX_PYTHON_VERSION: "3",
        TRUFFLE_STRICT_OPTION_DEPRECATION: "true",
        PATH: std.join(":", self.path + ["$PATH"]),
      },

      packages+: {
        "pip:ninja_syntax": "==1.7.2",  # Required by NFI and mx
      },

      setup+: [
        # We don't want to proxy any internet access
        [
          "unset",
          "ANT_OPTS",
          "FTP_PROXY",
          "ftp_proxy",
          "GRADLE_OPTS",
          "HTTPS_PROXY",
          "https_proxy",
          "HTTP_PROXY",
          "http_proxy",
          "MAVEN_OPTS",
          "no_proxy",
        ],
        # Fail if any command part of the pipe fails
        ["set", "-o", "pipefail"],
        ["ruby", "--version"],
      ],

      logs+: [
        "*.log",
      ],

      catch_files+: [
        "Graal diagnostic output saved in (?P<filename>.+\\.zip)",
      ],

      mx_build_options:: [],
    },

    maven: {
      downloads+: { MAVEN_HOME: { name: "maven", version: "3.3.9" } },
      environment+: { path+:: ["$MAVEN_HOME/bin"] },
    },

    build: {
      setup+: [["mx", "sversions"]] +
              # aot-build.log is used for the build-stats metrics, in other cases it does no harm
              jt(["build", "--env", self.mx_env, "--"] + self.mx_build_options + ["|", "tee", "aot-build.log"]) +
              [
                # make sure jt always uses what was just built
                ["set-export", "RUBY_BIN", jt(["--use", self.mx_env, "--silent", "launcher"])[0]],
              ],
    },

    # Do not run this gate if only **/*.md files were changed
    skip_docs: {
      guard+: {
        excludes+: ["**.md"],
      },
    },

    truffleruby: {
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
      environment+: {
        HOST_VM: "mri",
        HOST_VM_CONFIG: "default",
        GUEST_VM: "mri",
        GUEST_VM_CONFIG: "default",
        RUBY_BIN: mri_path(mri_version),
      },
    },

    jruby: {
      downloads+: {
        JRUBY_HOME: { name: "jruby", version: "9.1.12.0" },
      },

      environment+: {
        HOST_VM: "jruby",
        HOST_VM_CONFIG: "default",
        GUEST_VM: "jruby",
        GUEST_VM_CONFIG: "indy",
        RUBY_BIN: "$JRUBY_HOME/bin/jruby",
        JRUBY_OPTS: "-Xcompile.invokedynamic=true",
      },
    },
    node: {
      downloads+: { NODE: { name: "node", version: "v10.15.2", platformspecific: true } },
      environment+: { path+:: ["$NODE/bin"] },
    },
    
    sqlite331: { packages+: { sqlite: ">=3.31" } },

    no_multi_tier: {
      environment+: {
        TRUFFLERUBYOPT+: " --experimental-options --engine.MultiTier=false",
      },
    },

    multi_tier: {
      environment+: {
        GUEST_VM_CONFIG+: "-multi-tier",
        TRUFFLERUBYOPT+: " --experimental-options --engine.MultiTier=true",
      },
    },

    three_threads: {
      environment+: {
        GUEST_VM_CONFIG+: "-3threads",
        TRUFFLERUBYOPT+: " --experimental-options --engine.CompilerThreads=3",
      },
    },

  },

  env: {
    jvm: {
      mx_env:: "jvm",
      environment+: {
        HOST_VM: "server",
        HOST_VM_CONFIG: "default",
      },
    },
    jvm_ce: {
      mx_env:: "jvm-ce",
      environment+: {
        HOST_VM: "server",
        HOST_VM_CONFIG: "graal-core",
      },
    },
    jvm_ee: {
      mx_env:: "jvm-ee",
      environment+: {
        HOST_VM: "server",
        HOST_VM_CONFIG: "graal-enterprise",
      },
    },
    local svm = {
      downloads+: {
        GDB: { name: "gdb", version: "7.11.1", platformspecific: true },
      },
      environment+: {
        GDB_BIN: "$GDB/bin/gdb",
        HOST_VM: "svm",
      },
    },
    native: {
      mx_env:: "native",
      environment+: {
        HOST_VM_CONFIG: "graal-core",
      },
    } + svm,
    native_ee: {
      mx_env:: "native-ee",
      environment+: {
        HOST_VM_CONFIG: "graal-enterprise",
      },
    } + svm,
  },

  jdk: {
    local with_path = { environment+: { path+:: ["$JAVA_HOME/bin"] } },

    v11: with_path {
      downloads+: {
        JAVA_HOME: common.jdks["labsjdk-ce-11"],
      },
    },

    v17: with_path {
      downloads+: {
        JAVA_HOME: common.jdks["labsjdk-ce-17"],
      },
    },
  },

  platform: {
    local linux_deps = common.sulong.deps.linux + {
      packages+: {
        git: ">=1.8.3",
        mercurial: ">=3.2.4",
        binutils: ">=2.30",
        ruby: "==" + mri_version,
      },
    },

    local darwin_deps = common.sulong.deps.darwin + {
      packages+: {
        ruby: "==3.0.2",
      },
    },

    linux: linux_deps + {
      platform_name:: "LinuxAMD64",
      platform: "linux",
      arch:: "amd64",
      "$.cap":: {
        normal_machine: ["linux", "amd64"],
        bench_machine: ["x52"] + self.normal_machine + ["no_frequency_scaling"],
      },
    },
    linux_arm64: linux_deps + {
      platform_name:: "LinuxARM64",
      platform: "linux",
      arch:: "aarch64",
      "$.cap":: {
        normal_machine: ["linux", "aarch64"],
      },
    },
    darwin: darwin_deps + {
      platform_name:: "DarwinAMD64",
      platform: "darwin",
      arch:: "amd64",
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
    bench: {
      capabilities+: self["$.cap"].bench_machine,
      targets+: ["bench"],
    },
    daily: { targets+: ["daily"] },
    weekly: { targets+: ["weekly"] },
    manual: {
      capabilities+: self["$.cap"].normal_machine,
      targets: [],
    },
  },

  run: {
    test_unit_tck: {
      run+: jt(["test", "unit", "--verbose"]) +
            jt(["test", "tck"])
    },

    test_specs: {
      run+: jt(["test", "specs", "--timeout", "180", ":all"]) +
            jt(["test", "specs", "--timeout", "180", ":tracepoint"]) +
            jt(["test", "specs", "--timeout", "180", ":next"]) +
            jt(["test", "basictest"]),
    },

    test_specs_mri: {
      environment+: {
        "CHECK_LEAKS": "true",
      },
      run+: jt(["-u", mri_path(mri_version), "mspec", "spec/ruby"]) +
            jt(["-u", mri_path("2.7.2"), "mspec", "spec/ruby"]) +
            jt(["-u", mri_path("2.6.6"), "mspec", "spec/ruby"]),
    },

    test_fast: {
      run+: jt(["test", "fast"]),
    },

    lint: {
      is_after:: ["$.use.build"],
      downloads+: common.downloads.eclipse.downloads,
      environment+: common.downloads.eclipse.environment,
      mx_build_options:: ["--jdt", "builtin", "--warning-as-error", "--force-deprecation-as-warning"],
      packages+: {
        "pip:pylint": "==1.9.0",
        "shellcheck": "==0.6.0",
      },
      run+: jt(["lint"]) + jt(["spotbugs"]),
    },

    test_mri: { run+: jt(["test", "mri", "--no-sulong", "--", "-j4"]) },
    test_integration: { run+: jt(["test", "integration"]) },
    test_gems: { run+: jt(["test", "gems"]) },
    test_compiler: { run+: jt(["test", "compiler"]) },
    test_ecosystem: {
      run+: [["node", "--version"]] + jt(["test", "ecosystem"]),
    },

    test_cexts: {
      is_after+:: ["$.use.common"],
      run+: [
        ["mx", "--dynamicimports", "/sulong", "ruby_testdownstream_sulong"],
      ] + jt(["test", "bundle"]),
    },

    testdownstream_aot: { run+: [["mx", "ruby_testdownstream_aot", "$RUBY_BIN"]] },

    test_make_standalone_distribution: {
      run+: [
        ["tool/make-standalone-distribution.sh"],
      ],
    },

    generate_native_config: {
      setup+: [
        ["mx", "sforceimports"], # clone the graal repo
        ["mx", "-p", "../graal/sulong", "build"],
        ["set-export", "TOOLCHAIN_PATH", ["mx", "-p", "../graal/sulong", "lli", "--print-toolchain-path"]],
      ],
      run+: [
        ["env",
          "LD_LIBRARY_PATH=$BUILD_DIR/graal/sulong/mxbuild/" + self.platform + "-" + self.arch + "/SULONG_HOME/native/lib:$LD_LIBRARY_PATH", # for finding libc++
          "PATH=$TOOLCHAIN_PATH:$PATH",
          "ruby", "tool/generate-native-config.rb"],
        ["cat", "src/main/java/org/truffleruby/platform/" + self.platform_name + "NativeConfiguration.java"],

        # Uses the system compiler as using the toolchain for this does not work on macOS
        ["tool/generate-config-header.sh"],
        ["cat", "lib/cext/include/truffleruby/config_" + self.platform + "_" + self.arch + ".h"],
      ],
    },

    check_native_config: {
      is_after+:: ["$.run.generate_native_config"],
      run+: jt(["check_native_configuration"]) + jt(["check_config_header"]),
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
      run+: benchmark(benchmarks_to_run),
    },

    interpreter_metrics: {
      benchmarks+:: [
        "allocation:hello",
        "minheap:hello",
        "time:hello",
      ],
    },

    compiler_metrics: {
      # Run all metrics benchmarks: hello and compile-mandelbrot
      benchmarks+:: [
        "allocation",
        "minheap",
        "time",
      ],
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
        ["set-export", "TRUFFLERUBYOPT", "$TRUFFLERUBYOPT --disable-gems"],
      ] + run_benchs,
    },

    svm_build_stats: { benchmarks+:: ["build-stats"] },

    classic: { benchmarks+:: ["classic"] },
    chunky: { benchmarks+:: ["chunky"] },
    psd: { benchmarks+:: ["psd"] },
    asciidoctor: { benchmarks+:: ["asciidoctor"] },
    micro: { benchmarks+:: ["micro"] },
    other_extra: { benchmarks+:: ["savina"] },
    other: { benchmarks+:: ["image-demo", "optcarrot", "synthetic", "rubykon", "liquid"] },
    warmup: {
      benchmarks+:: [
        ["--fork-count-file", "mx.truffleruby/warmup-fork-counts.json", "ruby-warmup:*"],
      ],
    },

    server: {
      packages+: {
        "apache/ab": ">=2.3",
      },
      setup+: [
        ["set-export", "GEM_TEST_PACK", jt(["gem-test-pack"])[0]],
      ],
      benchmarks+:: ["server"],
    },

    cext_chunky: {
      environment+: {
        TRUFFLERUBYOPT+: " --experimental-options --cexts-log-load",
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
      "ruby-lint": $.platform.linux + $.cap.gate + $.jdk.v11 + $.use.common + $.env.jvm + $.use.build + $.run.lint + { timelimit: "45:00" },
      # Run specs on CRuby to make sure new specs are compatible and have the needed version guards
      "ruby-test-specs-on-cruby": $.platform.linux + $.cap.gate + $.use.skip_docs + $.use.common + $.run.test_specs_mri + { timelimit: "45:00" },
    } +

    {
      local gate_no_build = $.cap.gate + $.use.skip_docs + $.use.common + { timelimit: "01:00:00" },
      local gate = gate_no_build + $.use.build,
      local native_config = $.run.generate_native_config + $.run.check_native_config,
      local native_tests = $.run.testdownstream_aot + $.run.test_integration + $.run.test_compiler,

      // Order: platform, jdk, mx_env. Keep aligned for an easy visual comparison.
      "ruby-test-specs-linux-11":    $.platform.linux  + $.jdk.v11 + $.env.jvm + gate_no_build + $.use.build + $.run.test_unit_tck + native_config + $.run.test_specs + { timelimit: "01:20:00" },
      "ruby-test-specs-linux-17":    $.platform.linux  + $.jdk.v17 + $.env.jvm + gate_no_build + $.use.build + $.run.test_unit_tck + native_config + $.run.test_specs + { timelimit: "01:20:00" },
      "ruby-test-specs-darwin-11":   $.platform.darwin + $.jdk.v11 + $.env.jvm + gate_no_build + $.use.build + $.run.test_unit_tck + native_config + $.run.test_specs + { timelimit: "01:40:00" },
      "ruby-test-specs-darwin-17":   $.platform.darwin + $.jdk.v17 + $.env.jvm + gate_no_build + $.use.build + $.run.test_unit_tck + native_config + $.run.test_specs + { timelimit: "01:40:00" },
      "ruby-test-fast-linux-arm64":  $.platform.linux_arm64 + $.jdk.v11 + $.env.jvm + gate + $.run.test_fast + native_config + { timelimit: "45:00" },
      "ruby-test-fast-linux":        $.platform.linux  + $.jdk.v11 + $.env.jvm + gate + $.run.test_fast + { timelimit: "45:00" },  # To catch missing slow tags
      "ruby-test-mri-linux":         $.platform.linux  + $.jdk.v11 + $.env.jvm + gate + $.run.test_mri + { timelimit: "01:10:00" },
      "ruby-test-mri-darwin":        $.platform.darwin + $.jdk.v11 + $.env.jvm + gate + $.run.test_mri + { timelimit: "01:30:00" },
      "ruby-test-integration-linux": $.platform.linux  + $.jdk.v11 + $.env.jvm + gate + $.run.test_integration,
      "ruby-test-cexts-linux":       $.platform.linux  + $.jdk.v11 + $.env.jvm + gate + $.use.gem_test_pack + $.use.sqlite331 + $.run.test_cexts,
      "ruby-test-cexts-darwin":      $.platform.darwin + $.jdk.v11 + $.env.jvm + gate + $.use.gem_test_pack + $.run.test_cexts + { timelimit: "01:20:00" },
      "ruby-test-gems-linux":        $.platform.linux  + $.jdk.v11 + $.env.jvm + gate + $.use.gem_test_pack + $.run.test_gems,
      "ruby-test-gems-darwin":       $.platform.darwin + $.jdk.v11 + $.env.jvm + gate + $.use.gem_test_pack + $.run.test_gems,
      "ruby-test-ecosystem-linux":   $.platform.linux  + $.jdk.v11 + $.env.jvm + gate + $.use.node + $.use.sqlite331 + $.use.gem_test_pack + $.run.test_ecosystem,
      "ruby-test-standalone-linux":  $.platform.linux  + $.jdk.v11+ gate_no_build + $.run.test_make_standalone_distribution,

      "ruby-test-compiler-graal-core-11":       $.platform.linux + $.jdk.v11 + $.env.jvm_ce + gate + $.use.truffleruby + $.run.test_compiler,
      "ruby-test-compiler-graal-core-17":       $.platform.linux + $.jdk.v17 + $.env.jvm_ce + gate + $.use.truffleruby + $.run.test_compiler,
      "ruby-test-compiler-graal-enterprise-11": $.platform.linux + $.jdk.v11 + $.env.jvm_ee + gate + $.use.truffleruby + $.run.test_compiler,
      "ruby-test-compiler-graal-enterprise-17": $.platform.linux + $.jdk.v17 + $.env.jvm_ee + gate + $.use.truffleruby + $.run.test_compiler,

      "ruby-test-svm-graal-core-linux-11":     $.platform.linux  + $.jdk.v11 + $.env.native    + gate + native_tests,
      "ruby-test-svm-graal-core-linux-17":     $.platform.linux  + $.jdk.v17 + $.env.native    + gate + native_tests,
      "ruby-test-svm-graal-core-darwin-11":    $.platform.darwin + $.jdk.v11 + $.env.native    + gate + native_tests,
      "ruby-test-svm-graal-core-darwin-17":    $.platform.darwin + $.jdk.v17 + $.env.native    + gate + native_tests,
      "ruby-test-svm-graal-enterprise-linux":  $.platform.linux  + $.jdk.v11 + $.env.native_ee + gate + native_tests,
      "ruby-test-svm-graal-enterprise-darwin": $.platform.darwin + $.jdk.v11 + $.env.native_ee + gate + native_tests,
    },

  local other_rubies = {
    mri: $.use.mri + $.cap.bench + $.cap.weekly,
    jruby: $.use.jruby + $.cap.bench + $.cap.weekly,
  },
  local graal_configurations = {
    local shared = $.cap.bench + $.cap.daily + $.use.truffleruby + $.use.build,

    "graal-core": shared + $.env.jvm_ce,
    "graal-enterprise": shared + $.env.jvm_ee,
  },
  local svm_configurations = {
    local shared = $.cap.bench + $.cap.daily + $.use.truffleruby + $.use.build,

    "svm-graal-core": shared + $.env.native,
    "svm-graal-enterprise": shared + $.env.native_ee,
  },

  bench_builds:
    {
      local shared = $.platform.linux + $.jdk.v11 + $.use.common +
                     $.benchmark.runner + $.benchmark.compiler_metrics + { timelimit: "01:15:00" },

      "ruby-metrics-compiler-graal-core": shared + graal_configurations["graal-core"] + $.use.no_multi_tier,
      "ruby-metrics-compiler-graal-core-multi-tier": shared + graal_configurations["graal-core"] + $.use.multi_tier,
      "ruby-metrics-compiler-graal-enterprise": shared + graal_configurations["graal-enterprise"] + $.use.no_multi_tier,
      "ruby-metrics-compiler-graal-enterprise-multi-tier": shared + graal_configurations["graal-enterprise"] + $.use.multi_tier,
    } +

    {
      local shared = $.platform.linux + $.jdk.v11 + $.use.common +
                     $.benchmark.runner + $.benchmark.svm_build_stats + { timelimit: "00:20:00" },
      # TODO this 2 jobs have GUEST_VM_CONFIG: 'default' instead of 'truffle', why?
      local guest_vm_override = { environment+: { GUEST_VM_CONFIG: "default" } },

      "ruby-build-stats-svm-graal-core": shared + svm_configurations["svm-graal-core"] + guest_vm_override,
      "ruby-build-stats-svm-graal-enterprise": shared + svm_configurations["svm-graal-enterprise"] + guest_vm_override,
    } +

    {
      local shared = $.platform.linux + $.jdk.v11 + $.use.common +
                     $.benchmark.run_svm_metrics + { timelimit: "00:45:00" },

      "ruby-metrics-svm-graal-core": shared + svm_configurations["svm-graal-core"] + $.use.no_multi_tier,
      "ruby-metrics-svm-graal-core-multi-tier": shared + svm_configurations["svm-graal-core"] + $.use.multi_tier,
      "ruby-metrics-svm-graal-enterprise": shared + svm_configurations["svm-graal-enterprise"] + $.use.no_multi_tier,
      "ruby-metrics-svm-graal-enterprise-multi-tier": shared + svm_configurations["svm-graal-enterprise"] + $.use.multi_tier,
    } +

    {
      local shared = $.platform.linux + $.jdk.v11 + $.use.common +
                     $.benchmark.runner + $.benchmark.classic,

      "ruby-benchmarks-classic-mri": shared + other_rubies.mri + { timelimit: "00:55:00" },
      "ruby-benchmarks-classic-jruby": shared + other_rubies.jruby + { timelimit: "00:55:00" },
      "ruby-benchmarks-classic-graal-core": shared + graal_configurations["graal-core"] + { timelimit: "00:55:00" } + $.use.no_multi_tier,
      "ruby-benchmarks-classic-graal-core-multi-tier": shared + graal_configurations["graal-core"] + { timelimit: "00:55:00" } + $.use.multi_tier,
      "ruby-benchmarks-classic-graal-enterprise": shared + graal_configurations["graal-enterprise"] + { timelimit: "00:55:00" } + $.use.no_multi_tier,
      "ruby-benchmarks-classic-graal-enterprise-multi-tier": shared + graal_configurations["graal-enterprise"] + { timelimit: "00:55:00" } + $.use.multi_tier,
      "ruby-benchmarks-classic-svm-graal-core": shared + svm_configurations["svm-graal-core"] + { timelimit: "01:45:00" } + $.use.no_multi_tier,
      "ruby-benchmarks-classic-svm-graal-core-multi-tier": shared + svm_configurations["svm-graal-core"] + { timelimit: "01:45:00" } + $.use.multi_tier,
      "ruby-benchmarks-classic-svm-graal-enterprise": shared + svm_configurations["svm-graal-enterprise"] + { timelimit: "01:45:00" } + $.use.no_multi_tier,
      "ruby-benchmarks-classic-svm-graal-enterprise-multi-tier": shared + svm_configurations["svm-graal-enterprise"] + { timelimit: "01:45:00" } + $.use.multi_tier,
    } +

    {
      local shared = $.platform.linux + $.jdk.v11 + $.use.common,

      local chunky = $.benchmark.runner + $.benchmark.chunky + { timelimit: "01:30:00" },
      "ruby-benchmarks-chunky-mri": shared + chunky + other_rubies.mri,
      "ruby-benchmarks-chunky-jruby": shared + chunky + other_rubies.jruby,
      "ruby-benchmarks-chunky-graal-core": shared + chunky + graal_configurations["graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-chunky-graal-core-multi-tier": shared + chunky + graal_configurations["graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-chunky-graal-enterprise": shared + chunky + graal_configurations["graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-chunky-graal-enterprise-multi-tier": shared + chunky + graal_configurations["graal-enterprise"] + $.use.multi_tier,
      "ruby-benchmarks-chunky-svm-graal-core": shared + chunky + svm_configurations["svm-graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-chunky-svm-graal-core-multi-tier": shared + chunky + svm_configurations["svm-graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-chunky-svm-graal-enterprise": shared + chunky + svm_configurations["svm-graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-chunky-svm-graal-enterprise-multi-tier": shared + chunky + svm_configurations["svm-graal-enterprise"] + $.use.multi_tier,
      local psd = $.benchmark.runner + $.benchmark.psd + { timelimit: "01:15:00" },
      "ruby-benchmarks-psd-mri": shared + psd + other_rubies.mri,
      "ruby-benchmarks-psd-jruby": shared + psd + other_rubies.jruby,
      "ruby-benchmarks-psd-graal-core": shared + psd + graal_configurations["graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-psd-graal-core-multi-tier": shared + psd + graal_configurations["graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-psd-graal-enterprise": shared + psd + graal_configurations["graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-psd-graal-enterprise-multi-tier": shared + psd + graal_configurations["graal-enterprise"] + $.use.multi_tier,
      "ruby-benchmarks-psd-svm-graal-core": shared + psd + svm_configurations["svm-graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-psd-svm-graal-core-multi-tier": shared + psd + svm_configurations["svm-graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-psd-svm-graal-enterprise": shared + psd + svm_configurations["svm-graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-psd-svm-graal-enterprise-multi-tier": shared + psd + svm_configurations["svm-graal-enterprise"] + $.use.multi_tier,
      local asciidoctor = $.benchmark.runner + $.benchmark.asciidoctor + { timelimit: "01:25:00" },
      "ruby-benchmarks-asciidoctor-mri": shared + asciidoctor + other_rubies.mri,
      "ruby-benchmarks-asciidoctor-jruby": shared + asciidoctor + other_rubies.jruby,
      "ruby-benchmarks-asciidoctor-graal-core": shared + asciidoctor + graal_configurations["graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-asciidoctor-graal-core-multi-tier": shared + asciidoctor + graal_configurations["graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-asciidoctor-graal-enterprise": shared + asciidoctor + graal_configurations["graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-asciidoctor-graal-enterprise-multi-tier": shared + asciidoctor + graal_configurations["graal-enterprise"] + $.use.multi_tier,
      "ruby-benchmarks-asciidoctor-svm-graal-core": shared + asciidoctor + svm_configurations["svm-graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-asciidoctor-svm-graal-core-multi-tier": shared + asciidoctor + svm_configurations["svm-graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-asciidoctor-svm-graal-enterprise": shared + asciidoctor + svm_configurations["svm-graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-asciidoctor-svm-graal-enterprise-multi-tier": shared + asciidoctor + svm_configurations["svm-graal-enterprise"] + $.use.multi_tier,
      local warmup = $.benchmark.runner + $.benchmark.warmup + { timelimit: "01:05:00" },
      "ruby-benchmarks-warmup-mri": shared + warmup + other_rubies.mri + { timelimit: "01:20:00" },
      "ruby-benchmarks-warmup-jruby": shared + warmup + other_rubies.jruby,
      "ruby-benchmarks-warmup-graal-core": shared + warmup + graal_configurations["graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-warmup-graal-core-3threads": shared + warmup + graal_configurations["graal-core"] + $.use.no_multi_tier + $.use.three_threads,
      "ruby-benchmarks-warmup-graal-core-multi-tier": shared + warmup + graal_configurations["graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-warmup-graal-core-multi-tier-3threads": shared + warmup + graal_configurations["graal-core"] + $.use.multi_tier + $.use.three_threads,
      "ruby-benchmarks-warmup-graal-enterprise": shared + warmup + graal_configurations["graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-warmup-graal-enterprise-3threads": shared + warmup + graal_configurations["graal-enterprise"] + $.use.no_multi_tier + $.use.three_threads,
      "ruby-benchmarks-warmup-graal-enterprise-multi-tier": shared + warmup + graal_configurations["graal-enterprise"] + $.use.multi_tier,
      "ruby-benchmarks-warmup-graal-enterprise-multi-tier-3threads": shared + warmup + graal_configurations["graal-enterprise"] + $.use.multi_tier + $.use.three_threads,
      "ruby-benchmarks-warmup-svm-graal-core": shared + warmup + svm_configurations["svm-graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-warmup-svm-graal-core-3threads": shared + warmup + svm_configurations["svm-graal-core"] + $.use.no_multi_tier + $.use.three_threads,
      "ruby-benchmarks-warmup-svm-graal-core-multi-tier": shared + warmup + svm_configurations["svm-graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-warmup-svm-graal-core-multi-tier-3threads": shared + warmup + svm_configurations["svm-graal-core"] + $.use.multi_tier + $.use.three_threads,
      "ruby-benchmarks-warmup-svm-graal-enterprise": shared + warmup + svm_configurations["svm-graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-warmup-svm-graal-enterprise-3threads": shared + warmup + svm_configurations["svm-graal-enterprise"] + $.use.no_multi_tier + $.use.three_threads,
      "ruby-benchmarks-warmup-svm-graal-enterprise-multi-tier": shared + warmup + svm_configurations["svm-graal-enterprise"] + $.use.multi_tier,
      "ruby-benchmarks-warmup-svm-graal-enterprise-multi-tier-3threads": shared + warmup + svm_configurations["svm-graal-enterprise"] + $.use.multi_tier + $.use.three_threads,

      local micro = $.benchmark.runner + $.benchmark.micro + { timelimit: "01:00:00" },
      "ruby-benchmarks-micro-mri": shared + micro + other_rubies.mri,
      "ruby-benchmarks-micro-jruby": shared + micro + other_rubies.jruby,
      "ruby-benchmarks-micro-graal-core": shared + micro + graal_configurations["graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-micro-graal-core-multi-tier": shared + micro + graal_configurations["graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-micro-graal-enterprise": shared + micro + graal_configurations["graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-micro-graal-enterprise-multi-tier": shared + micro + graal_configurations["graal-enterprise"] + $.use.multi_tier,
      "ruby-benchmarks-micro-svm-graal-core": shared + micro + svm_configurations["svm-graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-micro-svm-graal-core-multi-tier": shared + micro + svm_configurations["svm-graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-micro-svm-graal-enterprise": shared + micro + svm_configurations["svm-graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-micro-svm-graal-enterprise-multi-tier": shared + micro + svm_configurations["svm-graal-enterprise"] + $.use.multi_tier,

      local other = $.benchmark.runner + $.benchmark.other + $.benchmark.other_extra + { timelimit: "01:00:00" },
      local svm_other = $.benchmark.runner + $.benchmark.other + { timelimit: "01:30:00" },
      "ruby-benchmarks-other-mri": shared + other + other_rubies.mri,
      "ruby-benchmarks-other-jruby": shared + other + other_rubies.jruby,
      "ruby-benchmarks-other-graal-core": shared + other + graal_configurations["graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-other-graal-core-multi-tier": shared + other + graal_configurations["graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-other-graal-enterprise": shared + other + graal_configurations["graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-other-graal-enterprise-multi-tier": shared + other + graal_configurations["graal-enterprise"] + $.use.multi_tier,
      "ruby-benchmarks-other-svm-graal-core": shared + svm_other + svm_configurations["svm-graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-other-svm-graal-core-multi-tier": shared + svm_other + svm_configurations["svm-graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-other-svm-graal-enterprise": shared + svm_other + svm_configurations["svm-graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-other-svm-graal-enterprise-multi-tier": shared + svm_other + svm_configurations["svm-graal-enterprise"] + $.use.multi_tier,
    } +

    {
      local shared = $.platform.linux + $.jdk.v11 + $.use.common + $.use.gem_test_pack +
                     $.benchmark.runner + $.benchmark.server +
                     { timelimit: "00:30:00" },

      "ruby-benchmarks-server-mri": shared + other_rubies.mri,
      "ruby-benchmarks-server-jruby": shared + other_rubies.jruby,
      "ruby-benchmarks-server-graal-core": shared + graal_configurations["graal-core"] + $.use.no_multi_tier,
      "ruby-benchmarks-server-graal-core-multi-tier": shared + graal_configurations["graal-core"] + $.use.multi_tier,
      "ruby-benchmarks-server-graal-enterprise": shared + graal_configurations["graal-enterprise"] + $.use.no_multi_tier,
      "ruby-benchmarks-server-graal-enterprise-multi-tier": shared + graal_configurations["graal-enterprise"] + $.use.multi_tier,
    } +

    {
      "ruby-metrics-truffle":
        $.platform.linux + $.jdk.v11 + $.use.common + $.env.jvm + $.use.build +
        $.use.truffleruby +
        $.cap.bench + $.cap.daily +
        $.benchmark.runner + $.benchmark.interpreter_metrics +
        { timelimit: "00:40:00" },
    } +

    {
      "ruby-benchmarks-cext":
        $.platform.linux + $.jdk.v11 + $.use.common +
        $.use.truffleruby + $.use.truffleruby_cexts +
        $.env.jvm_ce + $.use.build + $.use.gem_test_pack +
        $.cap.bench + $.cap.daily +
        $.benchmark.runner + $.benchmark.cext_chunky +
        { timelimit: "00:40:00" },
    },

  manual_builds: {
    local shared = $.use.common + $.cap.manual + { timelimit: "15:00" },

    "ruby-generate-native-config-linux-amd64": $.platform.linux + $.jdk.v11 + shared + $.run.generate_native_config,
    "ruby-generate-native-config-linux-aarch64": $.platform.linux_arm64 + $.jdk.v11 + shared + $.run.generate_native_config,
    "ruby-generate-native-config-darwin-amd64": $.platform.darwin + $.jdk.v11 + shared + $.run.generate_native_config,
  },

  builds:
    local all_builds = $.test_builds + $.bench_builds + $.manual_builds;
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
  specVersion: "2",
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
  - If (A) is used without (O) an error is raised that a field
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

