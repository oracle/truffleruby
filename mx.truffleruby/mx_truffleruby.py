# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

import os
from os.path import join

import mx
import mx_sdk
import mx_subst

if 'RUBY_BENCHMARKS' in os.environ:
    import mx_truffleruby_benchmark  # pylint: disable=unused-import

_suite = mx.suite('truffleruby')
root = _suite.dir

def add_ext_suffix(name):
    """
    Adds the platform specific C extension suffix to a name
    """
    if mx.is_darwin():
        return name + '.bundle'
    else:
        return name + '.so'

mx_subst.results_substitutions.register_with_arg('extsuffix', add_ext_suffix)

# Utilities

class VerboseMx:
    def __enter__(self):
        self.verbose = mx.get_opts().verbose
        mx.get_opts().verbose = True

    def __exit__(self, exc_type, exc_value, traceback):
        mx.get_opts().verbose = self.verbose

# Commands

def jt(*args):
    mx.log("\n$ " + ' '.join(['jt'] + list(args)) + "\n")
    mx.run(['ruby', join(root, 'tool/jt.rb')] + list(args))

def build_truffleruby(args):
    mx.command_function('sversions')([])
    jt('build', '--no-sforceimports', '--no-ee-checkout')

def miniruby_for_building_cexts(args):
    jvm_args = mx.get_runtime_jvm_args(['TRUFFLERUBY', 'TRUFFLERUBY-LAUNCHER', 'SULONG'])
    mx_binary = join(mx._mx_home, 'mx')
    options = [
        '--experimental-options',
        '--building-core-cexts',
        '--home=' + root,
        '--launcher=' + mx_binary + ' -p ' + root + ' miniruby_for_building_cexts',
        '--disable-gems',
        '--disable-rubyopt',
    ]
    mx.run_java(jvm_args + ['org.truffleruby.launcher.RubyLauncher'] + options + args)

def ruby_run_ruby(args):
    """run TruffleRuby (through tool/jt.rb)"""

    jt = join(root, 'tool/jt.rb')
    os.execlp(jt, jt, "ruby", *args)

def ruby_run_specs(args):
    with VerboseMx():
        jt('test', 'specs', *args)

def ruby_testdownstream_hello(args):
    """Run a minimal Hello World test"""
    build_truffleruby([])
    jt('ruby', '-e', 'puts "Hello Ruby!"')

def ruby_testdownstream_aot(args):
    """Run tests for the native image"""
    if len(args) > 3:
        mx.abort("Incorrect argument count: mx ruby_testdownstream_aot <aot_bin> [<format>] [<build_type>]")

    aot_bin = args[0]
    spec_format = args[1] if len(args) >= 2 else 'dot'

    fast = ['--excl-tag', 'slow']
    mspec_args = ['--format', spec_format, '--excl-tag', 'ci']

    os.environ['RUBY_BIN'] = aot_bin
    ruby_run_specs(mspec_args)

    # Run "jt test fast --native :truffle" to catch slow specs in Truffle which only apply to native
    ruby_run_specs(fast + mspec_args + [':truffle'])

def ruby_testdownstream_sulong(args):
    """Run C extension tests"""
    build_truffleruby([])
    # Ensure Sulong is available
    mx.suite('sulong')

    jt('test', 'specs', ':capi')
    jt('test', 'specs', ':truffle_capi')
    jt('test', 'specs', ':library_cext')
    jt('test', 'mri', '--all-sulong')
    jt('test', 'cexts')
    jt('test', 'bundle')

mx_sdk.register_graalvm_component(mx_sdk.GraalVmLanguage(
    suite=_suite,
    name='TruffleRuby license files',
    short_name='rbyl',
    dir_name='ruby',
    license_files=['LICENSE_TRUFFLERUBY.txt'],
    third_party_license_files=['3rd_party_licenses_truffleruby.txt'],
    dependencies=[],
    truffle_jars=[],
    support_distributions=[
        'truffleruby:TRUFFLERUBY_GRAALVM_LICENSES',
    ],
    priority=5,
))

mx_sdk.register_graalvm_component(mx_sdk.GraalVmLanguage(
    suite=_suite,
    name='TruffleRuby',
    short_name='rby',
    dir_name='ruby',
    standalone_dir_name='truffleruby-<version>-<graalvm_os>-<arch>',
    license_files=[],
    third_party_license_files=[],
    dependencies=['rbyl', 'Truffle', 'Truffle NFI', 'Sulong', 'LLVM.org toolchain', 'GraalVM Chrome Inspector', 'GraalVM Profiler', 'GraalVM Coverage', 'VisualVM'],
    standalone_dependencies={
        'Sulong': ('lib/sulong', ['bin/<exe:lli>']),
        'LLVM.org toolchain': ('lib/llvm-toolchain', []),
    },
    truffle_jars=[
        'truffleruby:TRUFFLERUBY',
        'truffleruby:TRUFFLERUBY-SHARED',
        'truffleruby:TRUFFLERUBY-ANNOTATIONS'
    ],
    boot_jars=[
        'truffleruby:TRUFFLERUBY-SERVICES'
    ],
    support_distributions=[
        'truffleruby:TRUFFLERUBY_GRAALVM_SUPPORT',
    ],
    provided_executables=[
        'bin/bundle',
        'bin/bundler',
        'bin/gem',
        'bin/irb',
        'bin/rake',
        'bin/rdoc',
        'bin/ri',
    ],
    launcher_configs=[
        mx_sdk.LanguageLauncherConfig(
            destination='bin/<exe:truffleruby>',
            jar_distributions=['truffleruby:TRUFFLERUBY-LAUNCHER'],
            main_class='org.truffleruby.launcher.RubyLauncher',
            build_args=[
                '--language:llvm',
            ],
            language='ruby',
            links=['bin/<exe:ruby>'],
        )
    ],
    post_install_msg="""
IMPORTANT NOTE:
---------------
The Ruby openssl C extension needs to be recompiled on your system to work with the installed libssl.
First, make sure TruffleRuby's dependencies are installed, which are described at:
  https://github.com/oracle/truffleruby/blob/master/README.md#dependencies
Then run the following command:
        ${graalvm_languages_dir}/ruby/lib/truffle/post_install_hook.sh""",
))

mx.update_commands(_suite, {
    'ruby': [ruby_run_ruby, ''],
    'build_truffleruby': [build_truffleruby, ''],
    'miniruby_for_building_cexts': [miniruby_for_building_cexts, ''],
    'ruby_testdownstream_aot': [ruby_testdownstream_aot, 'aot_bin'],
    'ruby_testdownstream_hello': [ruby_testdownstream_hello, ''],
    'ruby_testdownstream_sulong': [ruby_testdownstream_sulong, ''],
})
