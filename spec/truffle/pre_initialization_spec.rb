# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../ruby/spec_helper'

guard -> { Truffle.native? } do
  describe "The pre-initialized context" do
    it "can be used to run specs, as otherwise this spec run is meaningless" do
      Truffle::Boot.was_preinitialized?.should == true
    end

    it "is not used if -Xpreinit=false is passed" do
      out = ruby_exe("p Truffle::Boot.was_preinitialized?", options: "-Xpreinit=false")
      out.should == "false\n"
    end

    it "is used when passing no incompatible options" do
      out = ruby_exe("p Truffle::Boot.was_preinitialized?", options: "-Xlog=fine", args: "2>&1")
      out.should include("patchContext()")
      out.should_not include("createContext()")
      out.should_not include("initializeContext()")
      out.should include("\ntrue\n")
    end

    it "is not used when passing incompatible options" do
      no_native = "-Xplatform.native=false -Xpolyglot.stdio=true -Xsync.stdio=true --disable-gems"
      out = ruby_exe("p Truffle::Boot.was_preinitialized?", options: "-Xlog=fine #{no_native}", args: "2>&1")
      out.should include("patchContext()")
      out.should include("not reusing pre-initialized context: -Xplatform.native is false")
      out.should include("finalizeContext()")
      out.should include("disposeContext()")
      out.should include("createContext()")
      out.should include("initializeContext()")
      out.should include("\nfalse\n")
    end

    it "is not used when the home is unset but was set at build time" do
      subdir = tmp("subdir")
      moved_ruby = "#{subdir}/pre-init-spec-ruby"
      mkdir_p subdir
      cp RbConfig.ruby, moved_ruby
      begin
        File.chmod(0755, moved_ruby)
        _, *flags = *ruby_exe
        flags.delete_if { |arg| arg.start_with?("-Xhome") }
        args = [moved_ruby, *flags]
        code = "p [Truffle::Boot.ruby_home, Truffle::Boot.was_preinitialized?]"
        out = `#{args.join(' ')} -Xlog=fine -e #{code.inspect} 2>&1`
      ensure
        rm_r subdir
      end
      out.should include("[nil, false]\n")
      out.should include("not reusing pre-initialized context: Ruby home is unset")
    end

    it "is used when $VERBOSE changes" do
      code = "p [$VERBOSE, Truffle::Boot.was_preinitialized?]"
      ruby_exe(code).should == "[false, true]\n"
      ruby_exe(code, options: "-w").should == "[true, true]\n"
      ruby_exe(code, options: "-W0").should == "[nil, true]\n"
      ruby_exe(code, options: "-v").should include "[true, true]\n"
    end

    it "picks up new environment variables" do
      var = "TR_PRE_INIT_NEW_VAR"
      ENV[var] = "true"
      begin
        ruby_exe("print ENV['#{var}']").should == "true"
      ensure
        ENV.delete var
      end
    end
  end
end
