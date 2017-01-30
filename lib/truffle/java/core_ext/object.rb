###### BEGIN LICENSE BLOCK ######
# Version: EPL 1.0/GPL 2.0/LGPL 2.1
#
# The contents of this file are subject to the Common Public
# License Version 1.0 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a copy of
# the License at http://www.eclipse.org/legal/cpl-v10.html
#
# Software distributed under the License is distributed on an "AS
# IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
# implied. See the License for the specific language governing
# rights and limitations under the License.
#
# Alternatively, the contents of this file may be used under the terms of
# either of the GNU General Public License Version 2 or later (the "GPL"),
# or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
# in which case the provisions of the GPL or the LGPL are applicable instead
# of those above. If you wish to allow use of your version of this file only
# under the terms of either the GPL or the LGPL, and not to allow others to
# use your version of this file under the terms of the EPL, indicate your
# decision by deleting the provisions above and replace them with the notice
# and other provisions required by the GPL or the LGPL. If you do not delete
# the provisions above, a recipient may use your version of this file under
# the terms of any one of the EPL, the GPL or the LGPL.
###### END LICENSE BLOCK ######

class Object
  
  # Import one or many Java classes as follows:
  #
  #   java_import java.lang.System
  #   java_import java.lang.System, java.lang.Thread
  #   java_import [java.lang.System, java.lang.Thread]
  #
  # @!visibility public
  def java_import(*import_classes)
    import_classes = import_classes.flatten
    
    import_classes.map do |import_class|
      case import_class
      when String
        cc = java.lang.Character
        valid_name = import_class.split(".").all? do |frag|
          cc.java_identifier_start? frag[0].ord and
          frag.each_char.all? {|c| cc.java_identifier_part? c.ord }
        end
        unless valid_name
          raise ArgumentError.new "not a valid Java identifier: #{import_class}"
        end
        # pull in the class
        raise ArgumentError.new "must use jvm-style name: #{import_class}" if import_class.include? "::"
        import_class = JavaUtilities.get_proxy_class(import_class)
      when Module
        if import_class.respond_to? "java_class"
          # ok, it's a proxy
        else
          raise ArgumentError.new "not a Java class or interface: #{import_class}"
        end
      else
        raise ArgumentError.new "invalid Java class or interface: #{import_class}"
      end

      java_class = import_class.java_class
      class_name = java_class.simple_name

      if block_given?
        package = java_class.package

        # package can be nil if it's default or no package was defined by the classloader
        if package
          package_name = package.name
        elsif java_class.canonical_name =~ /(.*)\.[^.]$/
          package_name = $1
        else
          package_name = ""
        end

        constant = yield(package_name, class_name)
      else
        constant = class_name

        # Inner classes are separated with $, get last element
        if constant =~ /\$([^$])$/
          constant = $1
        end
      end

      unless constant =~ /^[A-Z].*/
        raise ArgumentError.new "cannot import class `" + java_class.name + "' as `" + constant + "'"
      end

      # JRUBY-3453: Make import not complain if Java already has already imported the specific Java class
      # If no constant is defined, or the constant is not already set to the java_import, assign it
      eval_str = "if !defined?(#{constant}) || #{constant} != import_class; #{constant} = import_class; end"
      if Module === self
        class_eval(eval_str, __FILE__, __LINE__)
      else
        eval(eval_str, binding, __FILE__, __LINE__)
      end

      import_class
    end
  end
  private :java_import

  # @private
  def handle_different_imports(*args, &block)
    if args.first.respond_to?(:java_class)
      java_import(*args, &block)
    else
      other_import(*args, &block)
    end
  end
  
  unless respond_to?(:import)
    alias :import :java_import
  end
end
