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

def build_truffleruby():
    mx.command_function('sversions')([])
    mx.command_function('build')([])

def ruby_tck(args):
    for var in ['GEM_HOME', 'GEM_PATH', 'GEM_ROOT']:
        if var in os.environ:
            del os.environ[var]
    mx_unittest.unittest(['-Dpolyglot.ruby.home='+root, '--verbose', '--suite', 'truffleruby'])

def deploy_binary_if_master(args):
    """If the active branch is 'master', deploy binaries for the primary suite to remote maven repository."""
    primary_branch = 'master'
    active_branch = mx.VC.get_vc(root).active_branch(root)
    if active_branch == primary_branch:
        return mx.command_function('deploy-binary')(args)
    else:
        mx.log('The active branch is "%s". Binaries are deployed only if the active branch is "%s".' % (active_branch, primary_branch))
        return 0

def ruby_run_specs(args):
    mx.run(['bin/truffleruby', 'spec/mspec/bin/mspec', 'run', '--config', 'spec/truffle.mspec', '--format', 'specdoc', '--excl-tag', 'fails'] + args)

def ruby_testdownstream(args):
    build_truffleruby()
    ruby_run_specs(['--excl-tag', 'slow'])

def ruby_testdownstream_hello(args):
    build_truffleruby()
    mx.run(['bin/truffleruby', '-e', 'puts "Hello Ruby!"'])


mx.update_commands(_suite, {
    'rubytck': [ruby_tck, ''],
    'deploy-binary-if-master': [deploy_binary_if_master, ''],
    'ruby_testdownstream': [ruby_testdownstream, ''],
    'ruby_testdownstream_hello': [ruby_testdownstream_hello, ''],
})
