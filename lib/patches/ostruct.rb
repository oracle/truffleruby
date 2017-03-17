class OpenStruct

  # This is copied from the original OpenStruct definition, but modified to
  # check if @table is nil before trying to read from it. OpenStruct aliases
  # `allocate` to `new`, which normally would ensure @table is initialized.
  # However, we're unable to accurately retarget `allocate` and thus must
  # retain the original definition, which leaves @table uninitialized.
  def respond_to_missing?(mid, include_private = false)
    mname = mid.to_s.chomp("=").to_sym
    (@table && @table.key?(mname)) || super
  end

end