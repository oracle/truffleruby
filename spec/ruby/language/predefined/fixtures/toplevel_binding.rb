foo = 1
require_relative 'toplevel_binding_required'
eval('baz = 3')
p TOPLEVEL_BINDING.local_variables
