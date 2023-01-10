# Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Gem
  module ExtraExecutablesInstaller
    extend self

    def install_hooks_for(extra_bin_dirs)
      install_hook   = -> tool { hook(extra_bin_dirs, tool, :generate_bin) }
      uninstall_hook = -> tool { hook(extra_bin_dirs, tool, :remove_executables, tool.spec) }
      Gem.post_install(&install_hook)
      Gem.pre_uninstall(&uninstall_hook)
      [install_hook, uninstall_hook]
    end

    # Currently unused, documents how to remove the hooks if necessary for testing or other cases.
    def remove_hooks(install_hook, uninstall_hook)
      [Gem.post_install_hooks.delete(install_hook),
       Gem.pre_uninstall_hooks.delete(uninstall_hook)]
    end

    private

    def with_bin_dir(obj, new_bin_dir)
      old_bin_dir = obj.instance_variable_get :@bin_dir
      obj.instance_variable_set :@bin_dir, new_bin_dir
      begin
        yield
      ensure
        obj.instance_variable_set :@bin_dir, old_bin_dir
      end
    end

    def hook(extra_bin_dirs, tool, method, *args)
      extra_bin_dirs.each do |extra_bin_dir|
        with_bin_dir(tool, extra_bin_dir) { tool.send method, *args }
      end
      true
    end

  end
end
