class Socket < BasicSocket
  module Constants
    all_valid = RubySL::Socket.constant_pairs

    all_valid.each {|name, value| const_set name, Integer(value) }

    # MRI compat. socket is a pretty screwed up API. All the constants in Constants
    # must also be directly accessible on Socket itself. This means it's not enough
    # to include Constants into Socket, because Socket#const_defined? must be able
    # to see constants like AF_INET6 directly on Socket, but #const_defined? doesn't
    # check inherited constants. O_o
    #
    all_valid.each {|name, value| Socket.const_set name, Integer(value) }


    afamilies = all_valid.to_a.select { |name,| name =~ /^AF_/ }
    afamilies.map! {|name, value| [value.to_i, name] }

    pfamilies = all_valid.to_a.select { |name,| name =~ /^PF_/ }
    pfamilies.map! {|name, value| [value.to_i, name] }

    AF_TO_FAMILY = Hash[*afamilies.flatten]
    PF_TO_FAMILY = Hash[*pfamilies.flatten]

    # MRI defines these constants manually, thus our FFI generators don't pick
    # them up.
    # Truffle: added unless const_defined? to avoid warnings
    EAI_ADDRFAMILY = 1 unless const_defined?(:EAI_ADDRFAMILY)
    EAI_NODATA = 7 unless const_defined?(:EAI_NODATA)
    IPPORT_USERRESERVED = 5000 unless const_defined?(:IPPORT_USERRESERVED)

    # This constant is hidden behind a #ifdef __GNU on Linux, meaning it won't
    # be available when using clang.
    unless const_defined?(:SCM_CREDENTIALS)
      SCM_CREDENTIALS = 2
    end
  end

  [:EAI_ADDRFAMILY, :EAI_NODATA, :IPPORT_USERRESERVED, :SCM_CREDENTIALS].each do |const|
    # Truffle: added unless const_defined? to avoid warnings
    const_set(const, Constants.const_get(const)) unless const_defined?(const)
  end
end
