// File formatted with
// `jsonnet fmt --indent 2 --max-blank-lines 2 --sort-imports \
//  --string-style s --comment-style s`

// VSCODE is recomended for editting it has 'jsonnet' and 'jsonnet Formatter'
// plugins which makes it a functioning IDE for jsonnet.

local overlay = 'be5262d04726cbb2bf50fc08b32257692b40a046';

// For debugging: generated builds will be restricted to those listed in
// the array. No restriction is applied when it is empty.
local restrict_builds_to = [];
// Set to false to disable overlay application
local use_overlay = true;

// === Structure ===
//
// - All builds are composed from disjunct parts.
// - All parts are defined at the top in part_definitions in groups like
//   platform, graal, etc.
//    - The parts can be nested as needed, the last level is marked with field
//      `parts:: true`, currently there is always just one level, which should
//      probably be enough.
// - Each part is included in a build at most once, it will raise
//   an error otherwise.
// - All parts have to be disjoint and distinct.
// - All parts have to be compose-able with each other. Therefore all fields
//   should end with '+' to avoid overriding.
//   - For compose-able environment variables see `path::` and `PATH:` in
//     `$.use.common.environment`
// - Few values in a part (A) depend on values in other parts (O). The inter
//   part dependencies should be kept to minimum.
//   Use name of the part (A) for the field in (O).
//   See `$.platform` parts for an example.
// - Since parts do not inherit from each other, all parts included in the
//   build are listed where the build is being defined. (No need to track down
//   what is composed of what.)
// - Each manifested build has included_parts field with names of all
//   the included parts, which makes it clear what the build is
//   using and doing.
//   - The included parts is also stored in environment variable
//     PARTS_INCLUDED_IN_BUILD therefore printed at the top in each build log.
// - Very few parts depend on other parts already being included, in this case
//   they have `build_has_to_already_have+:: ['$.a_group.a_name']` which ensures
//   the required part are included before


// All helper functions of this CI definition
local ci = {

  ensure_it_has:: function(included_parts, part, name)
    if std.count(included_parts, part) == 0
    then error 'part ' + part + ' has to be included before ' + name +
               ' but has only ' + included_parts
    else true,

  // adds `included_parts+: $.a_group.part_name` fields into all parts
  // which then naturally track all included parts in build
  add_inclusion_tracking: function(prefix, parts, contains_parts)
    {
      local content = parts[k],
      local new_prefix = prefix + '.' + k,

      [k]: (
        if std.type(content) == 'object' && contains_parts
        then
          // process the part object
          content { included_parts+:
            // piggy back the build_has_to_already_have check here to avoid
            // extra field in the output
            local included_before_check =
              if std.objectHasAll(content, 'build_has_to_already_have')
              then
                std.foldl(
                  // make sure all required parts are already in there using super
                  function(r, p) r && ci.ensure_it_has(super.included_parts, p, new_prefix),
                  content.build_has_to_already_have,
                  true
                )
              else true;
            // if check is ok just add the part's name into included_parts
            if included_before_check then [new_prefix] }
        else
          // look recursively for parts
          if std.type(content) == 'object'
          then ci.add_inclusion_tracking(
            new_prefix,
            content,
            std.objectHasAll(content, 'parts') && content.parts
          )
          else content
      )
      for k in std.objectFields(parts)
    },

  // ensures that no part is included twice
  included_once_check: function(builds)
    std.map(
      function(build)
        local name = if std.objectHas(build, 'name') then build.name else build;
        // collect repeated parts
        local repeated = std.foldl(function(r, i)
                                     if std.count(build.included_parts, i) == 1
                                     then r else r + [i],
                                   build.included_parts,
                                   []);
        if std.length(repeated) == 0
        then build
        else error 'A parts ' + repeated +
                   ' are used more than once in build: ' + name +
                   '. See for duplicates: ' + build.included_parts,
      builds
    ),

  // restrict builds to a list given in restriction
  restrict_to: function(restriction, builds)
    if std.length(restriction) == 0
    then builds
    else std.filter(function(b) std.count(restriction, b.name) > 0, builds),

  // Combines objects holding partials in the given order producing
  // all possible variants:
  //  self.combine_partials([
  //    { a_: { d+: ['a1'] } },
  //    { b1_: { d+: ['b1'] },
  //      b2_: { d+: ['b2'] } },
  //    { c: { d+: ['c1']} },
  //  ])
  // Produces (in YAML syntax):
  //  a_b1_c:
  //    d:
  //      - a1
  //      - b1
  //      - c1
  //  a_b2_c:
  //    d:
  //      - a1
  //      - b2
  //      - c1
  combine_partials: function(partials)
    local init = partials[0];
    local collection = std.slice(partials, 1, std.length(partials), 1);
    std.foldl(
      function(results, partials)
        {
          [rk + pk]: { included_parts+: [] } + results[rk] + partials[pk]
          for rk in std.objectFields(results)
          for pk in std.objectFields(partials)
        },
      collection,
      init
    ),

  // try to read a field, if not present print error including the name
  // of the object
  debug_read: function(obj, field)
    if std.objectHasAll(obj, field)
    then obj[field]
    else error 'missing field: ' + field + ' in ' + obj.name,

  index: function(arr, obj)
    std.filter(function(i) obj == arr[i],
               std.range(0, std.length(arr) - 1)),
};

// Contains all part definitions used to compose into builds
local part_definitions = {
  local jt = function(args) [['ruby', 'tool/jt.rb'] + args],

  use: {
    parts:: true,

    common: {
      local build = self,
      environment+: {
        path+:: ['$JAVA_HOME/bin', '$MAVEN_HOME/bin', '$PATH'],
        java_opts+:: ['-Xmx2G'],
        CI: 'true',
        RUBY_BENCHMARKS: 'true',
        JAVA_OPTS: std.join(' ', self.java_opts),
        PATH: std.join(':', self.path),
      },

      setup+: [],
    },

    svm: {
      downloads+: {
        GDB: { name: 'gdb', version: '7.11.1', platformspecific: true },
      },

      environment+: {
        GDB_BIN: '$GDB/bin/gdb',
        HOST_VM: 'svm',
      },
    },

    maven: {
      downloads+: {
        MAVEN_HOME: { name: 'maven', version: '3.3.9' },
      },
    },

    build: {
      setup+: [['mx', 'build', '--force-javac', '--warning-as-error']],
    },

    sulong: {
      downloads+: {
        LIBGMP: {
          name: 'libgmp',
          version: '6.1.0',
          platformspecific: true,
        },
      },

      environment+: {
        CPPFLAGS: '-I$LIBGMP/include',
        LD_LIBRARY_PATH: '$LIBGMP/lib:$LLVM/lib:$LD_LIBRARY_PATH',
      },

      setup+: [
        ['git', 'clone', ['mx', 'urlrewrite', 'https://github.com/graalvm/sulong.git'], '../sulong'],
        ['cd', '../sulong'],
        ['mx', 'sversions'],
        ['mx', 'build'],
        ['cd', '../main'],
      ],
    },

    // TODO what is a purpose of this part, resp. its environment variables?
    truffleruby: {
      '$.benchmark.server':: { options: [] },
      environment+: {
        // TODO why do we still have jruby?
        GUEST_VM: 'jruby',
        GUEST_VM_CONFIG: 'truffle',
      },
    },

    truffleruby_cexts: {
      build_has_to_already_have+:: ['$.use.truffleruby'],
      environment+: {
        GUEST_VM_CONFIG+: '-cexts',
      },
    },

    gem_test_pack: {
      setup+: jt(['gem-test-pack']),
    },

    mri: {
      '$.benchmark.server':: { options: ['--', '--no-core-load-path'] },
      downloads+: {
        MRI_HOME: { name: 'ruby', version: '2.3.3' },
      },

      environment+: {
        HOST_VM: 'mri',
        HOST_VM_CONFIG: 'default',
        GUEST_VM: 'mri',
        GUEST_VM_CONFIG: 'default',
        RUBY_BIN: '$MRI_HOME/bin/ruby',
        JT_BENCHMARK_RUBY: '$MRI_HOME/bin/ruby',
      },
    },

    jruby: {
      '$.benchmark.server':: { options: ['--', '--no-core-load-path'] },
      downloads+: {
        JRUBY_HOME: { name: 'jruby', version: '9.1.12.0' },
      },

      environment+: {
        HOST_VM: 'server',
        HOST_VM_CONFIG: 'default',
        GUEST_VM: 'jruby',
        GUEST_VM_CONFIG: 'indy',
        RUBY_BIN: '$JRUBY_HOME/bin/jruby',
        JT_BENCHMARK_RUBY: '$JRUBY_HOME/bin/jruby',
        JRUBY_OPTS: '-Xcompile.invokedynamic=true',
      },
    },
  },

  graal: {
    parts:: true,

    core: {
      setup+: [
        ['cd', '../graal/compiler'],
        ['mx', 'sversions'],
        ['mx', 'build'],
        ['cd', '../../main'],
      ],

      environment+: {
        GRAAL_HOME: '../graal/compiler',
        HOST_VM: 'server',
        HOST_VM_CONFIG: 'graal-core',
      },
    },

    none: {
      environment+: {
        HOST_VM: 'server',
        HOST_VM_CONFIG: 'default',
        MX_NO_GRAAL: 'true',
      },
    },

    enterprise: {
      setup+: [
        [
          'git',
          'clone',
          ['mx', 'urlrewrite', 'https://github.com/graalvm/graal-enterprise.git'],
          '../graal-enterprise',
        ],
        ['cd', '../graal-enterprise/graal-enterprise'],
        ['mx', 'sforceimports'],
        ['mx', 'sversions'],
        ['mx', 'clean'],  // Workaround for NFI
        ['mx', 'build'],
        ['cd', '../../main'],
      ],

      environment+: {
        GRAAL_HOME: '$PWD/../graal-enterprise/graal-enterprise',
        HOST_VM: 'server',
        HOST_VM_CONFIG: 'graal-enterprise',
      },
    },

    without_om: {
      build_has_to_already_have+:: ['$.graal.enterprise', '$.use.common'],
      environment+: {
        HOST_VM_CONFIG+: '-no-om',
        java_opts+::
          ['-Dtruffle.object.LayoutFactory=com.oracle.truffle.object.basic.DefaultLayoutFactory'],
      },
    },
  },

  svm: {
    parts:: true,

    core: {
      build_has_to_already_have+:: ['$.use.build'],

      setup+: [
        ['cd', '../graal/substratevm'],
        ['mx', 'sforceimports'],
        ['mx', 'sversions'],
        ['cd', '../../main'],
      ],

      '$.svm.build_image':: {
        aot_bin: '$GRAAL_HOME/../substratevm/svmbuild/ruby',
      },

      environment+: {
        HOST_VM_CONFIG: 'graal-core',
        GRAAL_HOME: '$PWD/../graal/compiler',
        SVM_HOME: '$PWD/../graal/substratevm',
      },
    },

    enterprise: {
      build_has_to_already_have+:: ['$.use.build'],

      setup+: [
        [
          'git',
          'clone',
          ['mx', 'urlrewrite', 'https://github.com/graalvm/graal-enterprise.git'],
          '../graal-enterprise',
        ],
        ['cd', '../graal-enterprise/substratevm-enterprise'],
        ['mx', 'sforceimports'],
        ['mx', 'sversions'],
        ['cd', '../../main'],
      ],

      '$.svm.build_image':: {
        aot_bin: '$GRAAL_HOME/../substratevm-enterprise/svmbuild/ruby',
      },

      environment+: {
        HOST_VM_CONFIG: 'graal-enterprise',
        GRAAL_HOME: '$PWD/../graal-enterprise/graal-enterprise',
        SVM_HOME: '$PWD/../graal-enterprise/substratevm-enterprise',
      },
    },

    build_image: {
      setup+: [
        ['cd', '$SVM_HOME'],
        // Workaround for NFI when building with different Truffle versions
        ['mx', 'clean'],
        ['mx', 'build'],
        ['mx', 'image', '--', '-ruby', '>../../main/aot-build.log'],
        ['cd', '../../main'],
        ['cat', 'aot-build.log'],
      ],

      local build = self,
      environment+: {
        JT_BENCHMARK_RUBY: '$AOT_BIN',
        AOT_BIN: build['$.svm.build_image'].aot_bin,
        // so far there is no conflict buy it may become cumulative later
        TRUFFLERUBYOPT: '-Xhome=$PWD',
      },
    },
  },

  jdk: {
    parts:: true,

    labsjdk8: {
      downloads+: {
        JAVA_HOME: {
          name: 'labsjdk',
          version: '8u151-jvmci-0.39',
          platformspecific: true,
        },
      },
    },

    labsjdk9: {
      downloads+: {
        JAVA_HOME: {
          name: 'labsjdk',
          version: '9+181',
          platformspecific: true,
        },
      },
    },
  },

  platform: {
    parts:: true,

    linux: {
      local build = self,
      '$.run.deploy_and_spec':: { test_spec_options: ['-Gci'] },
      '$.cap':: {
        normal_machine: ['linux', 'amd64'],
        bench_machine: ['x52'] + self.normal_machine + ['no_frequency_scaling'],
      },
      packages+: {
        git: '>=1.8.3',
        mercurial: '>=3.2.4',
        ruby: '>=2.0.0',
        llvm: '==3.8',
      },
    },
    darwin: {
      '$.run.deploy_and_spec':: { test_spec_options: ['-GdarwinCI'] },
      '$.cap':: {
        normal_machine: ['darwin_sierra', 'amd64'],
      },
      environment+: {
        path+:: ['/usr/local/opt/llvm/bin'],
        LANG: 'en_US.UTF-8',
        // Homebrew does not put llvm on the PATH by default
        OPENSSL_PREFIX: '/usr/local/opt/openssl',
      },
    },
    solaris: {
      '$.run.deploy_and_spec':: { test_spec_options: [] },
      '$.cap':: {
        normal_machine: ['solaris', 'sparcv9'],
        bench_machine: ['m7_eighth', 'solaris'],
      },
      environment+: {
        // LLVM is currently not available on Solaris
        TRUFFLERUBY_CEXT_ENABLED: 'false',
      },
    },
  },

  cap: {
    parts:: true,

    gate: {
      capabilities+: self['$.cap'].normal_machine,
      targets+: ['gate', 'post-push'],
      environment+: {
        REPORT_GITHUB_STATUS: 'true',
      },
    },
    deploy: { targets+: ['deploy'] },
    fast_cpu: { capabilities+: ['x62'] },
    bench: { capabilities+: self['$.cap'].bench_machine },
    x52_18_override: {
      build_has_to_already_have+:: ['$.cap.bench'],
      capabilities: if std.count(super.capabilities, 'x52') > 0
      then std.map(function(c) if c == 'x52' then 'x52_18' else c,
                   super.capabilities)
      else error 'trying to override x52 but it is missing',
    },
    daily: { targets+: ['bench', 'daily'] },
    weekly: { targets+: ['weekly'] },
  },

  run: {
    parts:: true,

    deploy_and_spec: {
      local without_rewrites = function(commands)
        [
          ['export', 'PREV_MX_URLREWRITES=$MX_URLREWRITES'],
          ['unset', 'MX_URLREWRITES'],
        ] + commands + [
          ['export', 'MX_URLREWRITES=$PREV_MX_URLREWRITES'],
        ],
      local deploy_binaries_commands = [
        ['mx', 'deploy-binary-if-master-or-release'],
      ],
      local deploy_binaries_no_rewrites = without_rewrites(deploy_binaries_commands),
      local deploy_binaries = deploy_binaries_commands + deploy_binaries_no_rewrites,

      run+: deploy_binaries +
            jt(['test', 'specs'] + self['$.run.deploy_and_spec'].test_spec_options) +
            jt(['test', 'specs', ':ruby24']) +
            jt(['test', 'specs', ':ruby25']),
    },

    test_fast: {
      run+: jt(['test', 'fast']),
    },

    lint: {
      downloads+: {
        JDT: { name: 'ecj', version: '4.5.1', platformspecific: false },
      },
      packages+: {
        ruby: '>=2.1.0',
      },
      environment+: {
        // Truffle compiles with ECJ but does not run (GR-4720)
        TRUFFLERUBY_CEXT_ENABLED: 'false',
      },
      run+: [
        // Build with ECJ to get warnings
        ['mx', 'build', '--jdt', '$JDT', '--warning-as-error'],
      ] + jt(['lint']) + [
        ['mx', 'findbugs'],
      ],
    },

    test_mri: { run+: jt(['test', 'mri']) },
    test_integration: { run+: jt(['test', 'integration']) },
    test_gems: { run+: jt(['test', 'gems']) },
    test_ecosystem: { run+: jt(['test', 'ecosystem']) },
    test_bundle: { run+: jt(['test', 'bundle', '--no-sulong']) },
    test_compiler: { run+: jt(['test', 'compiler']) },

    test_cexts: {
      build_has_to_already_have+:: ['$.use.common'],
      environment+: {
        // TODO why is this option applied?
        java_opts+:: ['-Dgraal.TruffleCompileOnly=nothing'],
      },
      run+: [
        ['mx', '--dynamicimports', 'sulong', 'ruby_testdownstream_sulong'],
      ],
    },

    // TODO what does it test? That we run with graal which is part of java9?
    compiler_standalone: {
      build_has_to_already_have+:: ['$.jdk.labsjdk9'],
      run+: [
        [
          'bin/truffleruby',
          '-J-XX:+UnlockExperimentalVMOptions',
          '-J-XX:+EnableJVMCI',
          '-J--module-path=' + std.join(
            ':',
            [
              '../graal/sdk/mxbuild/modules/org.graalvm.graal_sdk.jar',
              '../graal/truffle/mxbuild/modules/com.oracle.truffle.truffle_api.jar',
            ]
          ),
          '-J--upgrade-module-path=../graal/compiler/mxbuild/modules/jdk.internal.vm.compiler.jar',
          '-e',
          "raise 'no graal' unless Truffle.graal?",
        ],
      ],
    },

    svm_gate: {
      local build = self,
      run+: [
        ['cd', '$SVM_HOME'],
        [
          'mx',
          '--strict-compliance',
          'gate',
          '-B--force-deprecation-as-warning',
          '--strict-mode',
          '--tags',
          build['$.run.svm_gate'].tags,
        ],
      ],
    },
  },

  benchmark: {
    parts:: true,

    local post_process = [
      // ["cat", "bench-results-processed.json"],
      ['tool/post-process-results-json.rb', 'bench-results.json', 'bench-results-processed.json'],
    ],
    local upload_results =
      [['bench-uploader.py', 'bench-results-processed.json']],
    local post_process_and_upload_results =
      post_process + upload_results +
      [['tool/fail-if-any-failed.rb', 'bench-results-processed.json']],
    local post_process_and_upload_results_wait =
      post_process + upload_results +
      [['tool/fail-if-any-failed.rb', 'bench-results-processed.json', '--wait']],
    local mx_benchmark = function(bench)
      [['mx', 'benchmark'] + if std.type(bench) == 'string' then [bench] else bench],

    local benchmark = function(benchs)
      if std.type(benchs) == 'string' then
        error 'benchs must be an array'
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

    metrics: {
      benchmarks+:: ['allocation', 'minheap', 'time'],
    },

    compiler_metrics: {
      benchmarks+:: [
        'allocation:compile-mandelbrot',
        'minheap:compile-mandelbrot',
        'time:compile-mandelbrot',
      ],
    },

    classic: { benchmarks+:: ['classic'] },
    chunky: { benchmarks+:: ['chunky'] },
    psd: { benchmarks+:: ['psd'] },
    asciidoctor: { benchmarks+:: ['asciidoctor'] },
    other_extra: { benchmarks+:: ['savina', 'micro'] },
    other: { benchmarks+:: ['image-demo', 'optcarrot', 'synthetic'] },

    server: {
      local build = self,
      packages+: {
        'apache/ab': '>=2.3',
      },
      benchmarks+:: [['server'] + build['$.benchmark.server'].options],
    },

    cext_chunky: {
      environment+: {
        TRUFFLERUBYOPT: '-Xcexts.log.load=true',
        USE_CEXTS: 'true',
      },
      setup+:
        jt(['cextc', 'bench/chunky_png/oily_png']) + jt(['cextc', 'bench/psd.rb/psd_native']),
      benchmarks+:: ['chunky'],
    },

    svm_build_stats: { benchmarks+:: ['build-stats'] },

    // TODO not compose-able, it would had be broken up to 2 builds
    run_svm_metrics: {
      local run_benchs = benchmark([
        'instructions',
        ['time', '--', '--native'],
        'maxrss',
      ]),

      run: [
        ['export', 'GUEST_VM_CONFIG=default'],
      ] + run_benchs + [
        ['export', 'GUEST_VM_CONFIG=no-rubygems'],
        ['export', 'TRUFFLERUBYOPT=--disable-gems'],
      ] + run_benchs,
    },

  },
};

local composition_environment = ci.add_inclusion_tracking('$', part_definitions, false) {

  partial_svm_test:: {
    local shared =
      $.use.maven + $.jdk.labsjdk8 + $.use.common +
      $.use.svm + $.cap.gate +
      $.run.svm_gate,
    linux:
      $.platform.linux + shared +
      { '$.run.svm_gate':: { tags: 'build,ruby_debug,ruby_product' } },
    darwin:
      $.platform.darwin + shared +
      { '$.run.svm_gate':: { tags: 'build,darwin_ruby' } },
  },

  test_builds: {
    local ruby_deploy_and_spec = $.use.maven + $.jdk.labsjdk8 +
                                 $.use.common + $.use.build +
                                 $.cap.deploy + $.cap.gate +
                                 $.run.deploy_and_spec,

    'ruby-deploy-and-specs-linux':
      $.platform.linux + ruby_deploy_and_spec,
    'ruby-deploy-and-specs-darwin':
      $.platform.darwin + ruby_deploy_and_spec,
    'ruby-deploy-and-specs-solaris':
      $.platform.solaris + ruby_deploy_and_spec,
  } + {
    'ruby-test-fast-java9-linux':
      $.platform.linux +
      $.jdk.labsjdk9 +
      $.use.common + $.use.build +
      $.cap.gate +
      $.run.test_fast,
  } + {
    local linux_gate = $.platform.linux + $.cap.gate + $.use.maven + $.jdk.labsjdk8 +
                       $.use.common,

    'ruby-lint':
      linux_gate + $.run.lint,
    'ruby-test-tck':
      linux_gate + $.use.build + { run+: [['mx', 'rubytck']] },
    'ruby-test-mri':
      $.cap.fast_cpu +
      linux_gate +
      $.use.build +
      $.use.sulong +  // OpenSSL is required to run RubyGems tests
      $.run.test_mri,
    'ruby-test-integration':
      linux_gate + $.use.build + $.use.sulong +
      $.run.test_integration,
    'ruby-test-cexts':
      linux_gate + $.use.build + $.use.sulong + $.use.gem_test_pack +
      $.run.test_cexts,
    'ruby-test-gems':
      linux_gate + $.use.build + $.use.gem_test_pack +
      $.run.test_gems,
    'ruby-test-bundle-no-sulong':
      linux_gate + $.use.build +
      $.run.test_bundle,
    'ruby-test-ecosystem':
      linux_gate + $.use.build + $.use.sulong + $.use.gem_test_pack +
      $.run.test_ecosystem,

    'ruby-test-compiler-graal-core':
      linux_gate + $.use.build + $.use.truffleruby + $.graal.core +
      $.run.test_compiler,
    // TODO was commented out, needs to be rewritten?
    // {name: "ruby-test-compiler-graal-enterprise"} + linux_gate + $.graal_enterprise + {run: jt(["test", "compiler"])},
    // {name: "ruby-test-compiler-graal-vm-snapshot"} + linux_gate + $.graal_vm_snapshot + {run: jt(["test", "compiler"])},
  } + {
    local shared = $.platform.linux + $.cap.gate + $.jdk.labsjdk9 +
                   $.use.common + $.use.build +
                   $.use.truffleruby + $.graal.core,
    'ruby-test-compiler-graal-core-java9': shared + $.run.test_compiler,
    'ruby-test-compiler-standalone-java9': shared + $.run.compiler_standalone,
  } + {
    ['ruby-test-svm-graal-core-' + k]:
      $.use.build + $.svm.core + $.partial_svm_test[k] +
      // Owerride tags provided by partials
      { '$.run.svm_gate':: { tags: 'build,ruby' } }
    for k in std.objectFields($.partial_svm_test)
  } + {
    ['ruby-test-svm-graal-enterprise-' + k]:
      $.use.build + $.svm.enterprise + $.partial_svm_test[k]
    for k in std.objectFields($.partial_svm_test)
  },

  partial_other_rubies:: {
    mri: $.use.mri + $.cap.bench + $.cap.weekly,
    jruby: $.use.jruby + $.cap.bench + $.cap.weekly,
  },

  partial_graal_builds:: {
    local shared = $.use.truffleruby + $.use.build + $.cap.daily + $.cap.bench,
    // TODO was commented out, needs to be rewritten?
    // { name: "no-graal",               caps: $.weekly_bench_caps, setup: $.no_graal,               kind: "graal"  },
    // { name: "graal-vm-snapshot",      caps: $.bench_caps,        setup: $.graal_vm_snapshot,      kind: "graal" },
    'graal-core': shared + $.graal.core,
    'graal-enterprise': shared + $.graal.enterprise,
    'graal-enterprise-no-om': shared + $.graal.enterprise + $.graal.without_om,
  },

  partial_svm_builds:: {
    local shared = $.cap.bench + $.cap.daily +
                   $.use.truffleruby +
                   $.use.build +
                   $.use.svm,

    'svm-graal-core': shared + $.svm.core + $.svm.build_image,
    'svm-graal-enterprise': shared + $.svm.enterprise + $.svm.build_image,
  },

  partial_solaris_bench:: {
    local shared = $.platform.solaris + $.use.maven + $.jdk.labsjdk8 + $.use.common + $.use.build +
                   $.use.truffleruby +
                   $.cap.bench + $.cap.daily,
    'graal-core-solaris': shared + $.graal.core,
    'graal-enterprise-solaris': shared + $.graal.enterprise,
  },

  bench_builds:
    {
      'ruby-metrics-truffle':
        $.platform.linux + $.use.maven + $.jdk.labsjdk8 + $.use.common + $.use.build +
        $.use.truffleruby + $.graal.none +
        $.cap.bench + $.cap.daily +
        $.benchmark.runner + $.benchmark.metrics,
    } +

    ci.combine_partials([
      { 'ruby-metrics-compiler-':
        $.platform.linux + $.use.maven + $.jdk.labsjdk8 + $.use.common +
        $.benchmark.runner + $.benchmark.compiler_metrics },
      $.partial_graal_builds,
    ]) +

    ci.combine_partials([
      { 'ruby-build-stats-':
        $.platform.linux + $.use.maven + $.jdk.labsjdk8 + $.use.common +
        $.benchmark.runner + $.benchmark.svm_build_stats },
      $.partial_svm_builds,
      // a workaround to replace empty field `'':` with `[empty_name]:`,
      // otherwise autoformatter deletes the key and the result is invalid
      local empty_name = '';
      // TODO this 2 jobs have GUEST_VM_CONFIG: 'default' instead of 'truffle', why?
      { [empty_name]: { environment+: { GUEST_VM_CONFIG: 'default' } } },
    ]) +

    {
      ['ruby-metrics-' + k]:
        $.platform.linux + $.use.maven + $.jdk.labsjdk8 + $.use.common +
        $.benchmark.run_svm_metrics +
        $.partial_svm_builds[k] +
        $.cap.x52_18_override
      for k in std.objectFields($.partial_svm_builds)
    } +

    ci.combine_partials([
      { 'ruby-benchmarks-classic-':
        $.platform.linux + $.use.maven + $.jdk.labsjdk8 + $.use.common +
        $.benchmark.runner + $.benchmark.classic },
      $.partial_other_rubies + $.partial_graal_builds + $.partial_svm_builds,
    ]) +

    ci.combine_partials([
      { 'ruby-benchmarks-classic-': $.benchmark.runner + $.benchmark.classic },
      $.partial_solaris_bench,
    ]) +

    ci.combine_partials([
      { 'ruby-benchmarks-': $.platform.linux + $.use.maven + $.jdk.labsjdk8 + $.use.common },
      ci.combine_partials([
        {
          'chunky-': $.benchmark.runner + $.benchmark.chunky,
          'psd-': $.benchmark.runner + $.benchmark.psd,
          'asciidoctor-': $.benchmark.runner + $.benchmark.asciidoctor,
        },
        $.partial_other_rubies + $.partial_graal_builds + $.partial_svm_builds,
      ]) +
      ci.combine_partials([
        { 'other-': $.benchmark.runner + $.benchmark.other + $.benchmark.other_extra },
        $.partial_other_rubies + $.partial_graal_builds,
      ]) +
      ci.combine_partials([
        { 'other-': $.benchmark.runner + $.benchmark.other },
        $.partial_svm_builds,
      ]),
    ]) +

    ci.combine_partials([
      { 'ruby-benchmarks-server-':
        $.platform.linux + $.use.maven + $.jdk.labsjdk8 + $.use.common +
        $.benchmark.runner + $.benchmark.server },
      $.partial_other_rubies + $.partial_graal_builds,
    ]) +

    {
      'ruby-benchmarks-cext':
        $.platform.linux + $.use.maven + $.jdk.labsjdk8 + $.use.common +
        // TODO why are these the only builds using $.use.truffleruby_cexts
        // and not just $.use.truffleruby? We have cexts enabled by default
        // don't we?
        $.use.truffleruby + $.use.truffleruby_cexts +
        // TODO build was previously called before sulong setup, which seems
        // wrong, was there a reason?
        $.graal.core + $.use.sulong + $.use.gem_test_pack + $.use.build +
        $.cap.bench + $.cap.daily +
        $.benchmark.runner + $.benchmark.cext_chunky,
    },

  timelimits: {
    'ruby-deploy-and-specs-darwin': '30:00',
    'ruby-deploy-and-specs-linux': '30:00',
    'ruby-deploy-and-specs-solaris': '30:00',
    'ruby-test-fast-java9-linux': '30:00',
    'ruby-lint': '30:00',
    'ruby-test-tck': '01:00:00',
    'ruby-test-mri': '01:00:00',
    'ruby-test-integration': '01:00:00',
    'ruby-test-cexts': '01:00:00',
    'ruby-test-gems': '01:00:00',
    'ruby-test-bundle-no-sulong': '01:00:00',
    'ruby-test-ecosystem': '01:00:00',
    'ruby-test-compiler-graal-core': '01:00:00',
    'ruby-test-compiler-graal-core-java9': '01:00:00',
    'ruby-test-compiler-standalone-java9': '01:00:00',
    'ruby-metrics-truffle': '00:25:00',
    'ruby-metrics-compiler-graal-core': '00:50:00',
    'ruby-metrics-compiler-graal-enterprise': '00:50:00',
    'ruby-metrics-compiler-graal-enterprise-no-om': '00:50:00',
    'ruby-build-stats-svm-graal-core': '02:00:00',
    'ruby-build-stats-svm-graal-enterprise': '02:00:00',
    'ruby-metrics-svm-graal-core': '00:30:00',
    'ruby-metrics-svm-graal-enterprise': '00:30:00',
    'ruby-benchmarks-classic-mri': '00:35:00',
    'ruby-benchmarks-classic-jruby': '00:35:00',
    'ruby-benchmarks-classic-graal-core': '00:35:00',
    'ruby-benchmarks-classic-graal-enterprise': '00:35:00',
    'ruby-benchmarks-classic-graal-enterprise-no-om': '00:35:00',
    'ruby-benchmarks-classic-svm-graal-core': '01:10:00',
    'ruby-benchmarks-classic-svm-graal-enterprise': '01:10:00',
    'ruby-benchmarks-classic-graal-core-solaris': '01:10:00',
    'ruby-benchmarks-classic-graal-enterprise-solaris': '01:10:00',
    'ruby-benchmarks-chunky-mri': '01:00:00',
    'ruby-benchmarks-chunky-jruby': '01:00:00',
    'ruby-benchmarks-chunky-graal-core': '01:00:00',
    'ruby-benchmarks-chunky-graal-enterprise': '01:00:00',
    'ruby-benchmarks-chunky-graal-enterprise-no-om': '01:00:00',
    'ruby-benchmarks-chunky-svm-graal-core': '01:00:00',
    'ruby-benchmarks-chunky-svm-graal-enterprise': '01:00:00',
    'ruby-benchmarks-psd-mri': '02:00:00',
    'ruby-benchmarks-psd-jruby': '02:00:00',
    'ruby-benchmarks-psd-graal-core': '02:00:00',
    'ruby-benchmarks-psd-graal-enterprise': '02:00:00',
    'ruby-benchmarks-psd-graal-enterprise-no-om': '02:00:00',
    'ruby-benchmarks-psd-svm-graal-core': '02:00:00',
    'ruby-benchmarks-psd-svm-graal-enterprise': '02:00:00',
    'ruby-benchmarks-asciidoctor-mri': '00:35:00',
    'ruby-benchmarks-asciidoctor-jruby': '00:35:00',
    'ruby-benchmarks-asciidoctor-graal-core': '00:35:00',
    'ruby-benchmarks-asciidoctor-graal-enterprise': '00:35:00',
    'ruby-benchmarks-asciidoctor-graal-enterprise-no-om': '00:35:00',
    'ruby-benchmarks-asciidoctor-svm-graal-core': '00:35:00',
    'ruby-benchmarks-asciidoctor-svm-graal-enterprise': '00:35:00',
    'ruby-benchmarks-other-mri': '00:40:00',
    'ruby-benchmarks-other-jruby': '00:40:00',
    'ruby-benchmarks-other-graal-core': '00:40:00',
    'ruby-benchmarks-other-graal-enterprise': '00:40:00',
    'ruby-benchmarks-other-graal-enterprise-no-om': '00:40:00',
    'ruby-benchmarks-other-svm-graal-core': '01:00:00',
    'ruby-benchmarks-other-svm-graal-enterprise': '01:00:00',
    'ruby-benchmarks-server-mri': '00:20:00',
    'ruby-benchmarks-server-jruby': '00:20:00',
    'ruby-benchmarks-server-graal-core': '00:20:00',
    'ruby-benchmarks-server-graal-enterprise': '00:20:00',
    'ruby-benchmarks-server-graal-enterprise-no-om': '00:20:00',
    'ruby-benchmarks-cext': '02:00:00',
    'ruby-test-svm-graal-core-linux': '01:00:00',
    'ruby-test-svm-graal-core-darwin': '01:00:00',
    'ruby-test-svm-graal-enterprise-linux': '01:00:00',
    'ruby-test-svm-graal-enterprise-darwin': '01:00:00',
  },

  builds:
    local all_builds = $.test_builds + $.bench_builds;
    ci.restrict_to(
      restrict_builds_to,
      ci.included_once_check([
        local build = all_builds[key];
        // Move name insinde into name field, add timelimit,
        // and add PARTS_INCLUDED_IN_BUILD env var
        build {
          name: key,
          timelimit: $.timelimits[key],
          environment+: {
            PARTS_INCLUDED_IN_BUILD: std.join(
              ', ',
              std.map(
                // strip $. so it's not tryed to be replaced as variable
                function(name) std.substr(name, 2, std.length(name) - 2),
                build.included_parts
              )
            ),
          },
          //std.substr(s, from, len)
        }
        for key in std.objectFields(all_builds)
      ])
    ),
};

{
  local no_overlay = '6f4eafb4da3b14be3593b07ed562d12caad9b64b',
  overlay: if use_overlay then overlay else no_overlay,

  builds: composition_environment.builds,
}
