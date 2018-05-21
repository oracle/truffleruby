# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module TruffleTool

  def self.exclude_rspec_examples(exclusions, ignore_missing: false)
    return if exclusions.nil?
    exclusions.each do |mod_name, tests|
      begin
        a_module = Object.const_get mod_name.to_s
      rescue NameError => e
        puts "Exclusion FAILED of module: #{mod_name}"
        if ignore_missing
          next
        else
          raise e
        end
      end

      Array(tests).each do |test|
        print "Excluding: #{a_module}##{test}"
        begin
          a_module.send :undef_method, test
          a_module.send :define_method, test do
            skip 'excluded test'
          end
        rescue NameError => e
          print ' (NOT FOUND)'
          raise e unless ignore_missing
        end
        puts
      end

    end
  end

end
