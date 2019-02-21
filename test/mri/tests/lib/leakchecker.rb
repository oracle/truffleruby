# frozen_string_literal: false

raise "LeakChecker should not be used on TruffleRuby as ObjectSpace.each_object is slow"
