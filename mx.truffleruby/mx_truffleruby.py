# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

import glob
import os
from os.path import join

import mx
import mx_unittest

_suite = mx.suite('truffleruby')
rubyDists = ['RUBY', 'RUBY-TEST']

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
    doc_files = (glob.glob(join(_suite.dir, 'doc', 'legal', '*')) +
        glob.glob(join(_suite.dir, 'doc', 'user', '*')) +
        glob.glob(join(_suite.dir, '*.md')))

    def getResults(self):
        return [join(_suite.dir, f) for f in self.doc_files]

# Commands

def ruby_tck(args):
    mx_unittest.unittest(['--verbose', '--suite', 'truffleruby'])

def deploy_binary_if_truffle_head(args):
    """If the active branch is 'truffle-head', deploy binaries for the primary suite to remote maven repository."""
    primary_branch = 'truffle-head'
    active_branch = mx.VC.get_vc(_suite.dir).active_branch(_suite.dir)
    if active_branch == primary_branch:
        return mx.command_function('deploy-binary')(args)
    else:
        mx.log('The active branch is "%s". Binaries are deployed only if the active branch is "%s".' % (active_branch, primary_branch))
        return 0

def ruby_testdownstream(args):
    os.environ['CI'] = 'true'

    mx.command_function('build')([])
    mx.run(['ruby', 'tool/jt.rb', 'test', 'fast'])


mx.update_commands(_suite, {
    'rubytck': [ruby_tck, ''],
    'deploy-binary-if-truffle-head': [deploy_binary_if_truffle_head, ''],
    'ruby_testdownstream': [ruby_testdownstream, '']
})
