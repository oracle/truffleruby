# Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

Psych = Truffle::Psych

require 'psych/visitors'

module Psych

  def self.libyaml_version
    # TODO CS 23-Sep-15 hardcoded this for now - uses resources to read
    [1, 14, 0]
  end

  class ClassLoader
    def path2class(path)
      eval("::#{path}")
    end
  end

  module Visitors
    class ToRuby
      def build_exception(klass, mesg)
        klass.new(mesg)
      end
    end

    class YAMLTree
      def private_iv_get(target, prop)
        target.instance_variable_get("@#{prop}")
      end
    end
  end

  class Parser
    def parse(yaml, path = nil)
      tainted = yaml.tainted? || yaml.is_a?(IO)
      if !(String === yaml) && yaml.respond_to?(:read)
        # TODO (eregon, 19 Jan. 2018): is it worth using a streaming adapter?
        yaml = yaml.read
      else
        yaml = StringValue(yaml)
      end

      if path.nil? && yaml.respond_to?(:path)
        path = yaml.path
      end

      parser = Truffle.invoke_primitive(:psych_create_parser, yaml)

      loop do
        begin
          event = get_event(parser)
        rescue RuntimeError => e
          if info = parse_exception_info(e)
            raise Psych::SyntaxError.new(path, *info)
          else
            raise e
          end
        end

        if event?(event, :StreamStart)
          @handler.start_stream(ANY)
        elsif event?(event, :DocumentStart)
          tags = []
          version, explicit = doc_start_info(event) do |key, value|
            key.taint if tainted
            value.taint if tainted
            tags << [key, value]
          end
          @handler.start_document(version, tags, !explicit)
        elsif event?(event, :DocumentEnd)
          explicit = doc_end_explicit?(event)
          @handler.end_document(!explicit)
        elsif event?(event, :Alias)
          alias_name = alias_anchor(event)
          alias_name.taint if tainted
          @handler.alias(alias_name)
        elsif event?(event, :Scalar)
          value, anchor, tag, plain_implicit, quoted_implicit, style = scalar_info(event)
          value.taint if tainted
          anchor.taint if tainted
          tag.taint if tainted
          @handler.scalar(value, anchor, tag, plain_implicit, quoted_implicit, style)
        elsif event?(event, :SequenceStart)
          anchor, tag, implicit, style = seq_start_info(event)
          anchor.taint if tainted
          tag.taint if tainted
          @handler.start_sequence(anchor, tag, implicit, style)
        elsif event?(event, :SequenceEnd)
          @handler.end_sequence
        elsif event?(event, :MappingStart)
          anchor, tag, implicit, style = mapping_start_info(event)
          anchor.taint if tainted
          tag.taint if tainted
          @handler.start_mapping(anchor, tag, implicit, style)
        elsif event?(event, :MappingEnd)
          @handler.end_mapping
        elsif event?(event, :StreamEnd)
          @handler.end_stream
          break
        end
      end
    end
  end

end

require 'psych/syntax_error.rb'
