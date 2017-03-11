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

# Extensions to the standard Module package.
class Module
  private

  ##
  # Includes a Java package into this class/module. The Java classes in the
  # package will become available in this class/module, unless a constant
  # with the same name as a Java class is already defined.
  #
  def include_package(package)
    package = package.package_name if package.respond_to?(:package_name)

    if defined? @included_packages
      @included_packages << package
      return
    end

    @included_packages = [ package ]
    @java_aliases ||= {}

    def self.const_missing(constant)
      real_name = @java_aliases[constant] || constant

      java_class = nil
      last_error = nil

      @included_packages.each do |package|
        begin
          java_class = JavaUtilities.get_java_class("#{package}.#{real_name}")
        rescue NameError
          # we only rescue NameError, since other errors should bubble out
          last_error = $!
        end
        break if java_class
      end

      if java_class
        return JavaUtilities.create_proxy_class(constant, java_class, self)
      else
        # try to chain to super's const_missing
        begin
          return super
        rescue NameError
          # super didn't find anything either, raise our Java error
          raise NameError.new("#{constant} not found in packages #{@included_packages.join(', ')}; last error: #{last_error.message}")
        end
      end
    end
  end

  # Imports the package specified by +package_name+, first by trying to scan JAR resources
  # for the file in question, and failing that by adding a const_missing hook
  # to try that package when constants are missing.
  def import(package_name, &block)
    if package_name.respond_to?(:java_class) || (String === package_name && package_name.split(/\./).last =~ /^[A-Z]/)
      return super(package_name, &block)
    end
    include_package(package_name, &block)
  end

  def java_alias(new_id, old_id)
    (@java_aliases ||= {})[new_id] = old_id
  end

end
