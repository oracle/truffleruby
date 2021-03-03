# frozen_string_literal: true

# Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

# Copyright (c) 2007-2015, Evan Phoenix and contributors
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of Rubinius nor the names of its contributors
#   may be used to endorse or promote products derived from this software
#   without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

module GC
  def self.run(force)
    start
  end

  def self.start(full_mark: true, immediate_sweep: true)
    Primitive.gc_start()
  end

  # Totally fake.
  def self.stress
    @stress_level ||= false
  end

  # Totally fake.
  def self.stress=(flag)
    @stress_level = Primitive.as_boolean(flag)
  end

  # Totally fake.
  @enabled = true

  def self.enable
    # We don't support disable, so sure! enabled!
    ret = !@enabled
    @enabled = true
    ret
  end

  # Totally fake.
  def self.disable
    # Treat this like a request that we don't honor.
    ret = !@enabled
    @enabled = false
    ret
  end

  def garbage_collect
    GC.start
  end

  def self.stat(option = nil)
    time, count, minor_count, major_count, unknown_count, committed, used, memory_pool_names, memory_pool_info = Primitive.gc_stat()

    # Initialize stat for statistics that come from memory pools, and populate it with some final stats
    stat = {
      count: count,
      time: time,
      minor_gc_count: minor_count,
      major_gc_count: major_count,
      unknown_count: unknown_count, # if nonzero, major or minor count needs to be updated for this GC case
      committed: 0,
      init: 0,
      max: 0,
      used: 0,
      peak_committed: 0,
      peak_init: 0,
      peak_max: 0,
      peak_used: 0,
      last_committed: 0,
      last_init: 0,
      last_max: 0,
      last_used: 0,
      heap_available_slots: committed, # should be the same as the calculated committed
      heap_live_slots: used, # should be the same as the calculated used
      heap_free_slots: committed - used,
    }

    memory_pool_names.each_with_index do |memory_pool_name, i|
      # Populate memory pool specific stats
      info = memory_pool_info[i]
      if info
        stat[memory_pool_name] = data = {
          committed: info[0],
          init: info[1],
          max: info[2],
          used: info[3],
          peak_committed: info[4],
          peak_init: info[5],
          peak_max: info[6],
          peak_used: info[7],
          last_committed: info[8],
          last_init: info[9],
          last_max: info[10],
          last_used: info[11],
        }

        # Calculate stats across memory pools
        data.each_pair do |key, value|
          stat[key] += value
        end
      end
    end

    return stat unless option

    if stat[option]
      stat[option]
    else
      0
    end
  end

  module Profiler
    @enabled = true
    @since   = 0

    def self.clear
      @since = GC.time
      nil
    end

    def self.disable
      # Treat this like a request that we don't honor.
      ret = !@enabled
      @enabled = false
      ret
    end

    def self.enable
      # We don't support disable, so sure! enabled!
      ret = !@enabled
      @enabled = true
      ret
    end

    def self.enabled?
      @enabled
    end

    def self.report(out = $stdout)
      out.write result
    end

    def self.result
      <<-OUT
Complete process runtime statistics
===================================
Collections: #{GC.count}
Total time (ms): #{GC.time}
      OUT
    end

    def self.total_time
      (GC.time - @since) / 1000.0
    end
  end
end
