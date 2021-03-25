path = File.expand_path('..', __FILE__)
kernel = (class << ::Kernel; self; end)
[kernel, ::Kernel].each do |k|
  if k.private_method_defined?(:gem_original_require)
    private_require = k.private_method_defined?(:require)
    k.send(:remove_method, :require)
    k.send(:define_method, :require, k.instance_method(:gem_original_require))
    k.send(:private, :require) if private_require
  end
end
$:.unshift File.expand_path("#{path}/../gems/ruby2_keywords-0.0.4/lib")
$:.unshift File.expand_path("#{path}/../gems/mustermann-1.1.1/lib")
$:.unshift File.expand_path("#{path}/../gems/rack-2.2.3/lib")
$:.unshift File.expand_path("#{path}/../gems/rack-protection-2.1.0/lib")
$:.unshift File.expand_path("#{path}/../gems/tilt-2.0.10/lib")
$:.unshift File.expand_path("#{path}/../gems/sinatra-2.1.0/lib")
