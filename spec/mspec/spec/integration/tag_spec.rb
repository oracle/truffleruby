# encoding: utf-8
require 'spec_helper'

describe "Running mspec tag" do
  before :all do
    FileUtils.rm_rf 'spec/fixtures/tags'
  end

  after :all do
    FileUtils.rm_rf 'spec/fixtures/tags'
  end

  it "tags the failing specs" do
    fixtures = "spec/fixtures"
    cwd = Dir.pwd

    cmd = "bin/mspec tag"
    cmd << " -B #{fixtures}/config.mspec"
    cmd << " --add fails --fail"
    cmd << " #{fixtures}/tagging_spec.rb"
    out = `#{cmd}`
    out = out.sub(RUBY_DESCRIPTION, "RUBY_DESCRIPTION")
    out = out.gsub(/\d\.\d{6}/, "D.DDDDDD")

    out.should == <<EOS
RUBY_DESCRIPTION
.FF
TagAction: specs tagged with 'fails':

Tag#me errors
Tag#me érròrs in unicode


1)
Tag#me errors FAILED
Expected 1
 to equal 2

#{cwd}/spec/fixtures/tagging_spec.rb:9:in `block (2 levels) in <top (required)>'
#{cwd}/spec/fixtures/tagging_spec.rb:3:in `<top (required)>'
#{cwd}/bin/mspec-tag:7:in `<main>'

2)
Tag#me érròrs in unicode FAILED
Expected 1
 to equal 2

#{cwd}/spec/fixtures/tagging_spec.rb:13:in `block (2 levels) in <top (required)>'
#{cwd}/spec/fixtures/tagging_spec.rb:3:in `<top (required)>'
#{cwd}/bin/mspec-tag:7:in `<main>'

Finished in D.DDDDDD seconds

1 file, 3 examples, 3 expectations, 2 failures, 0 errors, 0 tagged
EOS
  end

  it "does not run already tagged specs" do
    fixtures = "spec/fixtures"
    cwd = Dir.pwd

    cmd = "bin/mspec run"
    cmd << " -B #{fixtures}/config.mspec"
    cmd << " --excl-tag fails"
    cmd << " #{fixtures}/tagging_spec.rb"
    out = `#{cmd}`
    out = out.sub(RUBY_DESCRIPTION, "RUBY_DESCRIPTION")
    out = out.gsub(/\d\.\d{6}/, "D.DDDDDD")

    out.should == <<EOS
RUBY_DESCRIPTION
.

Finished in D.DDDDDD seconds

1 file, 3 examples, 1 expectation, 0 failures, 0 errors, 2 tagged
EOS
  end
end
