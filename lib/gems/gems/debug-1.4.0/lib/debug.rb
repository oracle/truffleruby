# frozen_string_literal: true

require_relative 'debug/session'
DEBUGGER__::start no_sigint_hook: true, nonstop: true
