# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

from __future__ import print_function

import os
import pipes
from os.path import join, exists, basename
import re
import shutil
import sys

import mx
import mx_util
import mx_gate
import mx_sdk
import mx_sdk_vm
import mx_sdk_vm_impl
import mx_subst
import mx_spotbugs

# re-export custom mx project classes, so they can be used from suite.py
from mx_sdk_shaded import ShadedLibraryProject # pylint: disable=unused-import

# Fail early and clearly when trying to build with a too old JDK
jdk = mx.get_jdk(mx.JavaCompliance('11+'), 'building TruffleRuby which requires JDK 11 or newer')
if mx_sdk_vm.base_jdk_version() < 11:
    mx.abort('Building TruffleRuby requires JDK 11 or newer')

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

# From org.truffleruby.shared.Platform
def get_cruby_arch():
    arch = mx.get_arch()
    if arch == 'amd64':
        return 'x86_64'
    elif arch == 'aarch64':
        if mx.is_darwin():
            # CRuby makes exception for macOS and uses `arm64` instead of `aarch64`
            return 'arm64'
        else:
            return 'aarch64'
    else:
        raise Exception("Unknown platform " + arch)

mx_subst.results_substitutions.register_no_arg('cruby_arch', get_cruby_arch)

def get_truffleruby_abi_version():
    path = join(root, 'lib/cext/include/truffleruby/truffleruby-abi-version.h')
    with open(path, "r") as f:
        contents = f.read()
    m = re.search('"(.+)"', contents)
    return m.group(1)

mx_subst.results_substitutions.register_no_arg('truffleruby_abi_version', get_truffleruby_abi_version)

# Utilities

class VerboseMx:
    def __enter__(self):
        self.verbose = mx.get_opts().verbose
        mx.get_opts().verbose = True

    def __exit__(self, exc_type, exc_value, traceback):
        mx.get_opts().verbose = self.verbose

# Project classes

class TruffleRubyBootstrapLauncherProject(mx.Project):
    def __init__(self, suite, name, deps, workingSets, theLicense, **kwArgs):
        super(TruffleRubyBootstrapLauncherProject, self).__init__(suite, name, subDir=None, srcDirs=[], deps=deps, workingSets=workingSets, d=suite.dir, theLicense=theLicense, **kwArgs)

    def launchers(self):
        result = join(self.get_output_root(), 'miniruby')
        yield result, 'TOOL', 'miniruby'

    def getArchivableResults(self, use_relpath=True, single=False):
        for result, _, prefixed in self.launchers():
            yield result, prefixed

    def getBuildTask(self, args):
        return TruffleRubyBootstrapLauncherBuildTask(self, args, 1)


class TruffleRubyBootstrapLauncherBuildTask(mx.BuildTask):
    def __str__(self):
        return "Generating " + self.subject.name

    def newestOutput(self):
        return mx.TimeStampFile.newest([result for result, _, _ in self.subject.launchers()])

    def needsBuild(self, newestInput):
        sup = super(TruffleRubyBootstrapLauncherBuildTask, self).needsBuild(newestInput)
        if sup[0]:
            return sup

        for result, _, _ in self.subject.launchers():
            if not exists(result):
                return True, result + ' does not exist'
            with open(result, "r") as f:
                on_disk = f.read()
            if on_disk != self.contents(result):
                return True, 'command line changed for ' + basename(result)

        return False, 'up to date'

    def build(self):
        mx_util.ensure_dir_exists(self.subject.get_output_root())
        for result, _, _ in self.subject.launchers():
            with open(result, "w") as f:
                f.write(self.contents(result))
            os.chmod(result, 0o755)

    def clean(self, forBuild=False):
        if exists(self.subject.get_output_root()):
            mx.rmtree(self.subject.get_output_root())

    def contents(self, result):
        classpath_deps = [dep for dep in self.subject.buildDependencies if isinstance(dep, mx.ClasspathDependency)]
        jvm_args = [pipes.quote(arg) for arg in mx.get_runtime_jvm_args(classpath_deps)]

        debug_args = mx.java_debug_args()
        jvm_args.extend(debug_args)
        if debug_args:
            jvm_args.extend(['-ea', '-esa'])

        bootstrap_home = mx.distribution('TRUFFLERUBY_BOOTSTRAP_HOME').get_output()
        jvm_args.append('-Dorg.graalvm.language.ruby.home=' + bootstrap_home)

        jvm_args.append('-Dtruffleruby.repository=' + root)

        main_class = 'org.truffleruby.launcher.RubyLauncher'
        ruby_options = [
            '--experimental-options',
            '--building-core-cexts', # This lets the process know it's miniruby
            '--launcher=' + result,
            '--disable-gems',
            '--disable-rubyopt',
        ]
        trufflerubyopt = os.environ.get("TRUFFLERUBYOPT")
        if trufflerubyopt and '--cexts-sulong' in trufflerubyopt:
            ruby_options.append('--cexts-sulong')

        command = [jdk.java] + jvm_args + ['-m', 'org.graalvm.ruby.launcher/' + main_class] + ruby_options + ['"$@"']
        return "#!/usr/bin/env bash\n" + "exec " + " ".join(command) + "\n"


class YARPNativeProject(mx.NativeProject):
    def __init__(self, suite, name, deps, workingSets, output=None, **kwArgs):
        path = join(root, kwArgs.pop('dir'))
        super(YARPNativeProject, self).__init__(
            suite, name, subDir=None, srcDirs=[path], deps=deps, workingSets=workingSets,
            results=kwArgs.pop('results'),
            output=path, d=path, vpath=False, **kwArgs)

# Commands

def jt(*args):
    mx.log("\n$ " + ' '.join(['jt'] + list(args)) + "\n")
    mx.run([join(root, 'bin/jt')] + list(args))

def build_truffleruby(args):
    mx.command_function('sversions')([])
    jt('build')

def ruby_check_heap_dump(input_args, out=None):
    print("mx ruby_check_heap_dump " + " ".join(input_args))
    dists = ['TRUFFLERUBY', 'TRUFFLE_NFI', 'SULONG_NATIVE', 'TRUFFLERUBY-TEST-INTERNAL']
    mx.command_function('build')(['--dependencies', ','.join(dists)])
    args = input_args
    args.insert(0, "--experimental-options")
    vm_args, truffleruby_args = mx.extract_VM_args(args, useDoubleDash=True, defaultAllVMArgs=False)
    vm_args += mx.get_runtime_jvm_args(dists)
    # vm_args.append("-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y")
    vm_args.append("org.truffleruby.test.internal.LeakTest")
    out = mx.OutputCapture() if out is None else out
    retval = mx.run_java(vm_args + truffleruby_args, jdk=jdk, nonZeroIsFatal=False, out=out)
    if retval == 0:
        print("PASSED")
        print(out.data)
    elif os.environ.get("CI") and "--keep-dump" not in input_args:
        # rerun once with heap dumping enabled
        out = mx.OutputCapture()
        ruby_check_heap_dump(["--keep-dump"] + input_args, out=out)
        path = out.data.strip().partition("Dump file:")[2].strip()
        if path:
            save_path = os.path.join(root, "dumps", "leak_test")
            try:
                os.makedirs(save_path)
            except OSError:
                pass
            dest = shutil.copy(path, save_path) # pylint: disable=assignment-from-no-return
            print("Heapdump file kept in " + dest)
            raise Exception("heap dump check failed")
    else:
        print("FAILED")
        print(out.data)
        raise Exception("heap dump check failed")

def ruby_run_ruby(args):
    """run TruffleRuby in $(mx standalone-home --type=jvm ruby), needs an env including a ruby standalone. Use bin/jt for more control and shortcuts"""
    standalone_home = mx_sdk_vm_impl.standalone_home('ruby', is_jvm=True)
    ruby = join(standalone_home, 'bin/ruby')
    os.execlp(ruby, ruby, *args)

def ruby_run_specs(ruby, args):
    with VerboseMx():
        jt('--use', ruby, 'test', 'specs', *args)

def ruby_jacoco_args(args):
    mx_gate.add_jacoco_whitelisted_packages(['org.truffleruby'])
    print(' '.join(mx_gate.get_jacoco_agent_args('append')))

def ruby_testdownstream_hello(args):
    """Run a minimal Hello World test"""
    build_truffleruby([])
    jt('ruby', '-e', 'puts "Hello Ruby!"')

def ruby_testdownstream_aot(args):
    """Run tests for the native image"""
    if len(args) > 3:
        mx.abort("Incorrect argument count: mx ruby_testdownstream_aot <aot_bin> [<format>] [<build_type>]")

    aot_bin = args[0]
    fast = ['--excl-tag', 'slow']

    ruby_run_specs(aot_bin, [])

    # Run "jt test fast --native :truffle" to catch slow specs in Truffle which only apply to native
    ruby_run_specs(aot_bin, fast + [':truffle'])

def ruby_spotbugs(args):
    """Run SpotBugs with custom options to detect more issues"""
    # SpotBugs needs all Java projects to be built
    # GR-52408: mx.command_function('build')(['--no-native']) should be enough but it fails
    mx.command_function('build')([])

    filters = join(root, 'mx.truffleruby', 'spotbugs-filters.xml')
    spotbugsArgs = ['-textui', '-low', '-longBugCodes', '-include', filters]
    if mx.is_interactive():
        spotbugsArgs.append('-progress')
    sys.exit(mx_spotbugs.spotbugs(args, spotbugsArgs))

def verify_ci(args):
    """Verify CI configuration"""
    mx.verify_ci(args, mx.suite('truffle'), _suite, ['common.json', 'ci/common.jsonnet'])

def ruby_maven_deploy_public(args):
    mx.command_function('build')([])
    licenses = ['EPL-2.0', 'PSF-License', 'GPLv2-CPE', 'ICU,GPLv2', 'BSD-simplified', 'BSD-new', 'UPL', 'MIT']
    mx_sdk.maven_deploy_public(args, licenses=licenses, deploy_snapshots=False)

def ruby_maven_deploy_public_repo_dir(args):
    print(mx_sdk.maven_deploy_public_repo_dir())

mx_sdk_vm.register_graalvm_component(mx_sdk_vm.GraalVmLanguage(
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
    stability="experimental",
))

# Only keep what we need from the Sulong home.
# Exclude the toolchain launchers, we don't need them, and they would not work in the native standalone.
# Excluding "native/bin" or "native/bin/*" does not work so we have to list them.
toolchain_launchers = ['binutil', 'clang', 'clang++', 'clang-cl', 'flang', 'ld']
sulong_home_excludes = [f"native/bin/graalvm-native-{launcher}" for launcher in toolchain_launchers] + \
    ['native/cmake', 'native/include', 'native/lib/*++*']
standalone_dependencies_common = {
    'LLVM Runtime Core': ('lib/sulong', []),
    'LLVM Runtime Native': ('lib/sulong', sulong_home_excludes),
}

mx_sdk_vm.register_graalvm_component(mx_sdk_vm.GraalVmLanguage(
    suite=_suite,
    name='TruffleRuby',
    short_name='rby',
    dir_name='ruby',
    standalone_dir_name='truffleruby-community-<version>-<graalvm_os>-<arch>',
    standalone_dir_name_enterprise='truffleruby-<version>-<graalvm_os>-<arch>',
    license_files=[],
    third_party_license_files=[],
    dependencies=['rbyl', 'Truffle', 'Truffle NFI', 'LLVM Runtime Native', 'TRegex'],  # Use short name for license to select by priority
    standalone_dependencies={**standalone_dependencies_common, **{
        'TruffleRuby license files': ('', []),
    }},
    standalone_dependencies_enterprise={**standalone_dependencies_common, **{
        'LLVM Runtime Enterprise': ('lib/sulong', []),
        'LLVM Runtime Native Enterprise': ('lib/sulong', []),
        'TruffleRuby license files EE': ('', []),
        'GraalVM enterprise license files': ('', ['LICENSE.txt', 'GRAALVM-README.md']),
    }},
    truffle_jars=[
        # Distributions
        'truffleruby:TRUFFLERUBY',
        'truffleruby:TRUFFLERUBY-SHARED',
        'truffleruby:TRUFFLERUBY-ANNOTATIONS',
        'sdk:JLINE3',
        # Library distributions
        'truffle:TRUFFLE_JCODINGS',
        'truffleruby:TRUFFLERUBY_JONI',
    ],
    support_distributions=[
        'truffleruby:TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_AGNOSTIC',
        'truffleruby:TRUFFLERUBY_GRAALVM_SUPPORT_PLATFORM_SPECIFIC',
        'truffleruby:TRUFFLERUBY_GRAALVM_SUPPORT_NO_NI_RESOURCES',
    ],
    provided_executables=[
        'bin/bundle',
        'bin/bundler',
        'bin/erb',
        'bin/gem',
        'bin/irb',
        'bin/racc',
        'bin/rake',
        'bin/rbs',
        'bin/rdbg',
        'bin/rdoc',
        'bin/ri',
    ],
    library_configs=[
        mx_sdk_vm.LanguageLibraryConfig(
            destination='lib/<lib:rubyvm>',
            launchers=['bin/<exe:ruby>', 'bin/<exe:truffleruby>', 'bin/<exe:truffleruby-polyglot-get>'],
            jar_distributions=['truffleruby:TRUFFLERUBY-LAUNCHER', 'sdk:MAVEN_DOWNLOADER'],
            main_class='org.truffleruby.launcher.RubyLauncher',
            build_args=[
                '-H:+DetectUserDirectoriesInImageHeap',
            ],
            # G1 is only supported on linux currently
            build_args_enterprise=(['--gc=G1', '-H:-ProtectionKeys'] if (mx.get_os() == 'linux' and 'NATIVE_IMAGE_AUXILIARY_ENGINE_CACHE' not in os.environ) else []),
            language='ruby',
            option_vars=[
                'RUBYOPT',
                'TRUFFLERUBYOPT'
            ]
        )
    ],
    stability="experimental",
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
    'ruby_check_heap_dump': [ruby_check_heap_dump, ''],
    'ruby_testdownstream_aot': [ruby_testdownstream_aot, 'aot_bin'],
    'ruby_testdownstream_hello': [ruby_testdownstream_hello, ''],
    'ruby_spotbugs': [ruby_spotbugs, ''],
    'verify-ci': [verify_ci, '[options]'],
    'ruby_jacoco_args': [ruby_jacoco_args, ''],
    'ruby_maven_deploy_public': [ruby_maven_deploy_public, ''],
    'ruby_maven_deploy_public_repo_dir': [ruby_maven_deploy_public_repo_dir, ''],
})
