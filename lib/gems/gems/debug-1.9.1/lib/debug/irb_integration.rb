# frozen_string_literal: true

require 'irb'

module DEBUGGER__
  module IrbPatch
    def evaluate(line, line_no)
      SESSION.send(:restart_all_threads)
      super
      # This is to communicate with the test framework so it can feed the next input
      puts "INTERNAL_INFO: {}" if ENV['RUBY_DEBUG_TEST_UI'] == 'terminal'
    ensure
      SESSION.send(:stop_all_threads)
    end
  end

  class ThreadClient
    def activate_irb_integration
      IRB.setup(location, argv: [])
      workspace = IRB::WorkSpace.new(current_frame&.binding || TOPLEVEL_BINDING)
      irb = IRB::Irb.new(workspace)
      IRB.conf[:MAIN_CONTEXT] = irb.context
      IRB::Debug.setup(irb)
      IRB::Context.prepend(IrbPatch)
    end
  end
end
