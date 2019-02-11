# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.
# OTHER DEALINGS IN THE SOFTWARE.

require_relative '../ruby/spec_helper'

guard -> { TruffleRuby.native? } do
  describe "The pre-initialized context" do
    it "can be used to run specs, as otherwise this spec run is meaningless" do
      Truffle::Boot.was_preinitialized?.should == true
    end

    it "is not used if -Xpreinit=false is passed" do
      out = ruby_exe("p Truffle::Boot.was_preinitialized?", options: "-Xpreinit=false")
      out.should == "false\n"
    end

    it "is used when passing no incompatible options" do
      out = ruby_exe("p Truffle::Boot.was_preinitialized?", options: "--log.ruby.level=FINE", args: "2>&1")
      out.should include("patchContext()")
      out.should_not include("createContext()")
      out.should_not include("initializeContext()")
      out.should include("\ntrue\n")
    end

    it "is not used when passing incompatible options" do
      no_native = "-Xpatching=false --disable-gems"
      out = ruby_exe("p Truffle::Boot.was_preinitialized?", options: "--log.ruby.level=FINE #{no_native}", args: "2>&1")
      out.should include("patchContext()")
      out.should include("not reusing pre-initialized context: loading patching is false")
      out.should include("finalizeContext()")
      out.should include("disposeContext()")
      out.should include("createContext()")
      out.should include("initializeContext()")
      out.should include("\nfalse\n")
    end

    it "is not used when the home is unset but was set at build time" do
      code = "p [Truffle::Boot.ruby_home, Truffle::Boot.was_preinitialized?]"
      out = ruby_exe(code, options: "--log.ruby.level=FINE -Xno_home_provided=true", args: "2>&1")
      out.should include("[nil, false]\n")
      out.should include("not reusing pre-initialized context: Ruby home is unset")
    end

    it "is used when $VERBOSE changes" do
      code = "p [$VERBOSE, Truffle::Boot.was_preinitialized?]"
      # -w/-W in RUBYOPT overrides -w on the command-line, like in MRI,
      # so unset the RUBYOPT and TRUFFLERUBYOPT env vars
      env = { "RUBYOPT" => nil, "TRUFFLERUBYOPT" => nil }
      ruby_exe(code, env: env).should == "[false, true]\n"
      ruby_exe(code, options: "-w", env: env).should == "[true, true]\n"
      ruby_exe(code, options: "-W0", env: env).should == "[nil, true]\n"
      ruby_exe(code, options: "-v", env: env).should include "[true, true]\n"
    end

    it "is used with --disable-gems so startup with --disable-gems is not slower" do
      code = "p [defined?(Gem), Truffle::Boot.was_preinitialized?]"
      ruby_exe(code, options: "--disable-gems").should == "[nil, true]\n"
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

    it "uses the runtime value of $TZ" do
      with_timezone("America/New_York") do
        Time.local(2001, 1, 1).zone.should == "EST"
        env = { "TZ" => "Pacific/Honolulu" }
        ruby_exe("print Time.local(2001, 1, 1).zone", env: env).should == "HST"
      end
    end
  end
end
