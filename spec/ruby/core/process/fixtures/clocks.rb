module ProcessSpecs
  platform_is_not :windows, :solaris do
    CLOCK_CONSTANTS = Process.constants.select { |c|
      c.to_s.start_with?('CLOCK_') &&
      # These require CAP_WAKE_ALARM and are not documented in clock_gettime(),
      # they return EINVAL if the permission is not granted.
      c != :CLOCK_BOOTTIME_ALARM &&
      c != :CLOCK_REALTIME_ALARM
    }.map do |c|
      [c, Process.const_get(c)]
    end
  end
end
