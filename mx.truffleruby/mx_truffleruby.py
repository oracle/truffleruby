# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

import glob
import os
from os.path import exists, join
import shutil
import sys

import mx
import mx_unittest

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

class TruffleRubyDocsProject(ArchiveProject):
    doc_files = (glob.glob(join(root, 'doc', 'legal', '*')) +
        glob.glob(join(root, 'doc', 'user', '*')) +
        glob.glob(join(root, '*.md')))

    def getResults(self):
        return [join(root, f) for f in self.doc_files]

class TruffleRubyLauncherProject(ArchiveProject):
    def getBuildTask(self, args):
        return TruffleRubyLauncherBuildTask(self, args, 1)

    def getResults(self):
        return ArchiveProject.getResults(self)


class TruffleRubyLauncherBuildTask(mx.ArchivableBuildTask):
    def __init__(self, *args):
        mx.ArchivableBuildTask.__init__(self, *args)
        self.launcher = join(root, 'bin', 'truffleruby')
        self.binary = join(root, 'tool', 'native_launcher_darwin')

    def needsBuild(self, newestInput):
        if sys.platform.startswith('darwin'):
            return (mx.TimeStampFile(self.launcher).isOlderThan(self.binary), self.launcher)
        else:
            return (not exists(self.launcher), self.launcher)

    def build(self):
        if sys.platform.startswith('darwin'):
            shutil.copy(self.binary, self.launcher)
        else:
            os.symlink("truffleruby.sh", self.launcher)

    def clean(self, forBuild=False):
        if exists(self.launcher):
            os.remove(self.launcher)

# Commands

def jt(*args):
    mx.log("\n$ " + ' '.join(['jt'] + list(args)) + "\n")
    mx.run(['ruby', join(root, 'tool/jt.rb')] + list(args))

def build_truffleruby():
    mx.command_function('sversions')([])
    mx.command_function('build')([])

def run_unittest(*args):
    mx_unittest.unittest(['-Dpolyglot.ruby.home='+root, '--verbose', '--suite', 'truffleruby'] + list(args))

def ruby_tck(args):
    for var in ['GEM_HOME', 'GEM_PATH', 'GEM_ROOT']:
        if var in os.environ:
            del os.environ[var]
    if args:
        run_unittest(*args)
    else:
        # Run the TCK in isolation
        run_unittest('--blacklist', 'mx.truffleruby/tck.blacklist')
        run_unittest('RubyTckTest')

def deploy_binary_if_master(args):
    """If the active branch is 'master', deploy binaries for the primary suite to remote maven repository."""
    primary_branch = 'master'
    active_branch = mx.VC.get_vc(root).active_branch(root)
    if active_branch == primary_branch:
        return mx.command_function('deploy-binary')(args)
    else:
        mx.log('The active branch is "%s". Binaries are deployed only if the active branch is "%s".' % (active_branch, primary_branch))
        return 0

def download_binary_suite(args):
    if len(args) == 1:
        name, version = args[0], None
    else:
        name, version = args

    # Add to MX_BINARY_SUITES dynamically
    mx._binary_suites.append(name)

    suite = _suite.import_suite(name=name, version=version, urlinfos=[
        mx.SuiteImportURLInfo('https://curio.ssw.jku.at/nexus/content/repositories/snapshots', 'binary', mx.vc_system('binary'))
    ], kind=None)

def ruby_run_specs(launcher, format, args):
    mx.run([launcher, 'spec/mspec/bin/mspec', 'run', '--config', 'spec/truffle.mspec', '--format', format, '--excl-tag', 'fails'] + args)

def ruby_testdownstream(args):
    build_truffleruby()
    ruby_run_specs('bin/truffleruby', 'specdoc', ['--excl-tag', 'slow'])

def ruby_testdownstream_hello(args):
    build_truffleruby()
    mx.run(['bin/truffleruby', '-e', 'puts "Hello Ruby!"'])

def ruby_testdownstream_aot(args):
    if len(args) > 3:
        mx.abort("Incorrect argument count: mx ruby_testdownstream_aot <aot_bin> [<format>] [<build_type>]")

    aot_bin = args[0]
    format = args[1] if len(args) >= 2 else 'dot'
    debug_build = args[2] == 'debug' if len(args) >= 3 else False

    mspec_args = [
        '--excl-tag', 'graalvm',
        '--excl-tag', 'aot',
        '-t', aot_bin,
        '-T-XX:YoungGenerationSize=2G', '-T-XX:OldGenerationSize=4G',
        '-T-Xhome=' + root
    ]

    if debug_build:
        mspec_args.append(':language')

    ruby_run_specs(aot_bin, format, mspec_args)

def ruby_testdownstream_sulong(args):
    build_truffleruby()
    # Ensure Sulong is available
    mx.suite('sulong')

    jt('test', 'cexts')
    jt('test', 'specs', ':capi')
    jt('test', 'specs', ':openssl')
    jt('test', 'mri', '--openssl')
    jt('test', 'mri', '--syslog')
    jt('test', 'mri', '--cext')
    jt('test', 'bundle')

mx.update_commands(_suite, {
    'rubytck': [ruby_tck, ''],
    'deploy-binary-if-master': [deploy_binary_if_master, ''],
    'ruby_download_binary_suite': [download_binary_suite, 'name', 'revision'],
    'ruby_testdownstream': [ruby_testdownstream, ''],
    'ruby_testdownstream_aot': [ruby_testdownstream_aot, 'aot_bin'],
    'ruby_testdownstream_hello': [ruby_testdownstream_hello, ''],
    'ruby_testdownstream_sulong': [ruby_testdownstream_sulong, ''],
})
