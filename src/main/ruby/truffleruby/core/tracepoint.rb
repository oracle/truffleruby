# frozen_string_literal: true

# Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class TracePoint
  def self.trace(*events, &handler)
    TracePoint.new(*events, &handler).tap(&:enable)
  end

  def initialize(*events, &handler)
    events = [:line] if events.empty?
    events = events.map { |event| Truffle::Type.coerce_to event, Symbol, :to_sym }
    events.each do |event|
      case event
      when :line, :class, :never
        # Supported event, keep it and pass it below
      else
        raise ArgumentError, "unknown event: #{event}"
      end
    end

    raise ArgumentError, 'must be called with a block' unless handler

    Primitive.tracepoint_initialize self, events.uniq, handler
  end

  def inspect
    if enabled?
      "#<TracePoint:#{event} #{path}:#{lineno}>"
    else
      '#<TracePoint:disabled>'
    end
  end
end
