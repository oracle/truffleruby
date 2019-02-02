# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

import glob
import os
from os.path import exists, join, dirname, basename, isdir
import shutil
import sys

import mx
import mx_subst
import mx_unittest
import mx_sdk

if 'RUBY_BENCHMARKS' in os.environ:
    import mx_truffleruby_benchmark

_suite = mx.suite('truffleruby')
root = _suite.dir

# Project classes

class ArchiveProject(mx.ArchivableProject):
    def __init__(self, suite, name, deps, workingSets, theLicense, **args):
        mx.ArchivableProject.__init__(self, suite, name, deps, workingSets, theLicense)
        assert 'prefix' in args
        assert 'outputDir' in args

    def output_dir(self):
        return join(self.dir, self.outputDir)

    def archive_prefix(self):
        return self.prefix

    def getResults(self):
        return mx.ArchivableProject.walk(self.output_dir())


class TruffleRubySulongLibsProject(ArchiveProject):
    def getBuildTask(self, args):
        return TruffleRubySulongLibsBuildTask(self, args, 1)

    def getResults(self):
        # Empty results as they overlap with truffleruby-lib
        return []

class TruffleRubySulongLibsBuildTask(mx.ArchivableBuildTask):
    def __init__(self, *args):
        mx.ArchivableBuildTask.__init__(self, *args)
        self.sulong_libs_under_home = join(root, self.subject.outputDir)
        self.sulong_include_dir = join(root, 'lib', 'cext', 'include', 'sulong')

    def needsBuild(self, newestInput):
        for header in self.headers_target_files():
            if not exists(header):
                return (True, header + " does not exist")

        if not exists(self.sulong_libs_under_home):
            return (True, self.sulong_libs_under_home + " does not exist")

        return mx.ArchivableBuildTask.needsBuild(self, newestInput)

    def split_headers_libraries(self):
        sulong_libs = mx_subst.path_substitutions.substitute('<path:SULONG_LIBS>')
        files = glob.glob(join(sulong_libs, '*'))
        headers = glob.glob(join(sulong_libs, '*.h'))
        libraries = [f for f in files if f not in headers]
        return headers, libraries

    def headers_target_files(self):
        headers, _libraries = self.split_headers_libraries()
        return [join(self.sulong_include_dir, basename(header)) for header in headers]

    def build(self):
        headers, libraries = self.split_headers_libraries()

        for header in headers:
            shutil.copy(header, self.sulong_include_dir)

        if not isdir(self.sulong_libs_under_home):
            os.mkdir(self.sulong_libs_under_home)
        for library in libraries:
            shutil.copy(library, self.sulong_libs_under_home)

    def clean(self, forBuild=False):
        for header in self.headers_target_files():
            if exists(header):
                os.remove(header)

        if exists(self.sulong_libs_under_home):
            shutil.rmtree(self.sulong_libs_under_home)

class TruffleRubyLauncherProject(ArchiveProject):
    def getBuildTask(self, args):
        return TruffleRubyLauncherBuildTask(self, args, 1)

    def getResults(self):
        return []

class TruffleRubyLauncherBuildTask(mx.ArchivableBuildTask):
    def __init__(self, *args):
        mx.ArchivableBuildTask.__init__(self, *args)

        self.jvm_args_file = join(root, 'mxbuild', 'jvm_args.sh')
        self.mx_env = join(_suite.mxDir, 'env')
        self.suite_py = _suite.suite_py()
        self.mx_truffleruby = join(_suite.mxDir, 'mx_truffleruby.py')

        self.launcher = join(root, 'bin', 'truffleruby')
        self.binary = join(root, 'tool', 'native_launcher_darwin')

    def needsBuild(self, newestInput):
        if not exists(self.jvm_args_file):
            return (True, self.jvm_args_file + " does not exist")

        jvm_args = mx.TimeStampFile(self.jvm_args_file)
        # Depends on mx.truffleruby/env which can change which suites are binary
        if exists(self.mx_env) and jvm_args.isOlderThan(self.mx_env):
            return (True, self.jvm_args_file + " is older than " + self.mx_env)
        # and on suite.py which can change dependencies and the classpath
        if jvm_args.isOlderThan(self.suite_py):
            return (True, self.jvm_args_file + " is older than " + self.suite_py)
        # and on this file which can change the list of distributions for the classpath
        if jvm_args.isOlderThan(self.mx_truffleruby):
            return (True, self.jvm_args_file + " is older than " + self.mx_truffleruby)

        if sys.platform.startswith('darwin'):
            return (mx.TimeStampFile(self.launcher).isOlderThan(self.binary), self.launcher)
        else:
            return (not exists(self.launcher), self.launcher)

    def build(self):
        self.store_jvm_args()

        if sys.platform.startswith('darwin'):
            shutil.copy(self.binary, self.launcher)
        else:
            os.symlink("truffleruby.sh", self.launcher)

    def clean(self, forBuild=False):
        if os.path.lexists(self.launcher):
            os.remove(self.launcher)

    def store_jvm_args(self):
        distributions = [dep.name for dep in self.subject.buildDependencies]
        jvm_args = mx.get_runtime_jvm_args(distributions)
        properties = []
        while jvm_args:
            arg = jvm_args.pop(0)
            if arg == '-cp':
                classpath = self.relativize(jvm_args.pop(0)).split(':')
            else:
                properties.append(self.relativize(arg))

        sulong_libs = mx_subst.path_substitutions.substitute('-Dpolyglot.llvm.libraryPath=<path:SULONG_LIBS>')
        properties.append(self.relativize(sulong_libs))

        boot_dists = ['GRAAL_SDK', 'TRUFFLE_API', 'LAUNCHER_COMMON']
        bootclasspath = self.relativize(mx.classpath(boot_dists)).split(':')
        for jar in bootclasspath:
            classpath.remove(jar)

        with open(self.jvm_args_file, 'w') as f:
            f.write('bootclasspath=\\\n' + ':\\\n'.join(bootclasspath) + '\n\n')
            f.write('classpath=\\\n' + ':\\\n'.join(classpath) + '\n\n')
            f.write('properties=(\n"' + '"\n"'.join(properties) + '"\n)\n')

    def relativize(self, path):
        return path.replace(_suite.dir, "$root").replace(dirname(_suite.dir), "$root_parent")

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

def build_truffleruby(args = []):
    # Only build the necessary distributions to avoid building Sulong tests
    truffleruby_dists = [
        'TRUFFLERUBY',
        'TRUFFLERUBY-LAUNCHER',
        'TRUFFLERUBY_GRAALVM_SUPPORT',
        'TRUFFLERUBY-TEST',
        'TRUFFLERUBY-SPECS',
    ]
    mx.command_function('sversions')([])
    mx.command_function('build')([
        '--force-javac', '--warning-as-error', '--force-deprecation-as-warning',
        '-A-Xmaxerrs', '-A1000', # show more than default 100 errors not to hide actual errors under many missing symbols
        '--dependencies', ','.join(truffleruby_dists)])

def run_unittest(*args):
    mx_unittest.unittest(['-Dpolyglot.ruby.home='+root, '--verbose', '--suite', 'truffleruby'] + list(args))

def ruby_deploy_binaries(args):
    """Deploy a binary suite for truffleruby"""
    assert len(args) == 0
    deploy_binary = mx.command_function('deploy-binary')
    # Deploy platform-independent distributions only on Linux to avoid duplicates
    if sys.platform.startswith('linux'):
        return deploy_binary(['--skip-existing', 'truffleruby-binary-snapshots'])
    else:
        return deploy_binary(['--skip-existing', '--platform-dependent', 'truffleruby-binary-snapshots'])

def download_binary_suite(args):
    """Download a binary suite at the given revision"""
    if len(args) == 1:
        name, version = args[0], ''
    else:
        name, version = args

    if len(version) == 0:
        version = None
    elif version == 'truffle':
        version = mx.suite('truffle').version()

    # Add to MX_BINARY_SUITES dynamically, make sure to not use a source suite
    if not mx._binary_suites:
        mx._binary_suites = []
    mx._binary_suites.append(name)

    # For Graal's mx_post_parse_cmd_line()
    mx.get_opts().vm_prefix = None
    # Do not check JAVA_HOME within Graal's suite yet
    os.environ['JVMCI_VERSION_CHECK'] = 'ignore'

    suite = _suite.import_suite(name=name, version=version, urlinfos=[
        mx.SuiteImportURLInfo('https://curio.ssw.jku.at/nexus/content/repositories/snapshots', 'binary', mx.vc_system('binary'))
    ], kind=None)

def ruby_run_ruby(args):
    """run TruffleRuby (through tool/jt.rb)"""

    jt = join(root, 'tool/jt.rb')
    os.execlp(jt, jt, "ruby", *args)

def ruby_run_specs(launcher, format, args):
    with VerboseMx():
        mx.run([launcher, 'spec/mspec/bin/mspec', 'run', '--config', 'spec/truffle.mspec', '--format', format, '--excl-tag', 'fails'] + args, cwd=root)

def ruby_testdownstream(args):
    """Run fast specs"""
    build_truffleruby()
    ruby_run_specs('bin/truffleruby', 'specdoc', ['--excl-tag', 'slow'] + args)

def ruby_testdownstream_hello(args):
    """Run a minimal Hello World test"""
    build_truffleruby()
    with VerboseMx():
        mx.run(['bin/truffleruby', '-e', 'puts "Hello Ruby!"'], cwd=root)

def ruby_testdownstream_aot(args):
    """Run tests for the native image"""
    if len(args) > 3:
        mx.abort("Incorrect argument count: mx ruby_testdownstream_aot <aot_bin> [<format>] [<build_type>]")

    aot_bin = args[0]
    format = args[1] if len(args) >= 2 else 'dot'
    debug_build = args[2] == 'debug' if len(args) >= 3 else False

    fast = ['--excl-tag', 'slow']
    mspec_args = [
        '--excl-tag', 'ci',
        '--excl-tag', 'graalvm',
        '--excl-tag', 'aot',
        '-t', aot_bin
    ]

    ruby_run_specs(aot_bin, format, mspec_args)

    # Run "jt test fast --native :truffle" to catch slow specs in Truffle which only apply to native
    ruby_run_specs(aot_bin, format, fast + mspec_args + [':truffle'])

def ruby_testdownstream_sulong(args):
    """Run C extension tests"""
    build_truffleruby()
    # Ensure Sulong is available
    mx.suite('sulong')

    jt('test', 'specs', ':capi')
    jt('test', 'specs', ':truffle_capi')
    jt('test', 'specs', ':library_cext')
    jt('test', 'mri', '--cext')
    jt('test', 'cexts')
    jt('test', 'bundle')

mx_sdk.register_graalvm_component(mx_sdk.GraalVmLanguage(
    suite=_suite,
    name='TruffleRuby',
    short_name='rby',
    dir_name='ruby',
    license_files=['LICENSE_TRUFFLERUBY.md'],
    third_party_license_files=['3rd_party_licenses_truffleruby.txt'],
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
        'bin/gem',
        'bin/irb',
        'bin/rake',
        'bin/rdoc',
        'bin/ri',
        'bin/testrb',
    ],
    launcher_configs=[
        mx_sdk.LanguageLauncherConfig(
            destination='bin/<exe:truffleruby>',
            jar_distributions=['truffleruby:TRUFFLERUBY-LAUNCHER'],
            main_class='org.truffleruby.launcher.RubyLauncher',
            build_args=[
                '--language:llvm',
                '--language:ruby',
            ],
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
        ${graalvm_home}/jre/languages/ruby/lib/truffle/post_install_hook.sh""",
))

mx.update_commands(_suite, {
    'ruby': [ruby_run_ruby, ''],
    'build_truffleruby': [build_truffleruby, ''],
    'ruby_deploy_binaries': [ruby_deploy_binaries, ''],
    'ruby_download_binary_suite': [download_binary_suite, 'name [revision]'],
    'ruby_testdownstream': [ruby_testdownstream, ''],
    'ruby_testdownstream_aot': [ruby_testdownstream_aot, 'aot_bin'],
    'ruby_testdownstream_hello': [ruby_testdownstream_hello, ''],
    'ruby_testdownstream_sulong': [ruby_testdownstream_sulong, ''],
})
