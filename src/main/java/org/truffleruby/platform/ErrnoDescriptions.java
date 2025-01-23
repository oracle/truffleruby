/*
 * Copyright (c) 2017, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.platform;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ErrnoDescriptions {

    private static final Map<String, String> DESCRIPTIONS;

    @TruffleBoundary // Map
    public static String getDescription(String errnoName) {
        final String description = DESCRIPTIONS.get(errnoName);
        assert description != null;
        return description;
    }

    static {
        // From Linux, macOS, FreeBSD and OpenBSD error messages according to strerror()
        // Generated by tool/generate-errno-descriptions.rb
        final Map<String, String> map = new HashMap<>();
        map.put("E2BIG", "Argument list too long");
        map.put("EACCES", "Permission denied");
        map.put("EADDRINUSE", "Address already in use");
        map.put("EADDRNOTAVAIL", "Cannot assign requested address");
        map.put("EADV", "Advertise error");
        map.put("EAFNOSUPPORT", "Address family not supported by protocol");
        map.put("EAGAIN", "Resource temporarily unavailable");
        map.put("EALREADY", "Operation already in progress");
        map.put("EAUTH", "Authentication error");
        map.put("EBADARCH", "Bad CPU type in executable");
        map.put("EBADE", "Invalid exchange");
        map.put("EBADEXEC", "Bad executable (or shared library)");
        map.put("EBADF", "Bad file descriptor");
        map.put("EBADFD", "File descriptor in bad state");
        map.put("EBADMACHO", "Malformed Mach-o file");
        map.put("EBADMSG", "Bad message");
        map.put("EBADR", "Invalid request descriptor");
        map.put("EBADRPC", "RPC struct is bad");
        map.put("EBADRQC", "Invalid request code");
        map.put("EBADSLT", "Invalid slot");
        map.put("EBFONT", "Bad font file format");
        map.put("EBUSY", "Device or resource busy");
        map.put("ECANCELED", "Operation canceled");
        map.put("ECAPMODE", "Not permitted in capability mode");
        map.put("ECHILD", "No child processes");
        map.put("ECHRNG", "Channel number out of range");
        map.put("ECOMM", "Communication error on send");
        map.put("ECONNABORTED", "Software caused connection abort");
        map.put("ECONNREFUSED", "Connection refused");
        map.put("ECONNRESET", "Connection reset by peer");
        map.put("EDEADLK", "Resource deadlock avoided");
        map.put("EDEADLOCK", "Resource deadlock avoided");
        map.put("EDESTADDRREQ", "Destination address required");
        map.put("EDEVERR", "Device error");
        map.put("EDOM", "Numerical argument out of domain");
        map.put("EDOOFUS", "Programming error");
        map.put("EDOTDOT", "RFS specific error");
        map.put("EDQUOT", "Disk quota exceeded");
        map.put("EEXIST", "File exists");
        map.put("EFAULT", "Bad address");
        map.put("EFBIG", "File too large");
        map.put("EFTYPE", "Inappropriate file type or format");
        map.put("EHOSTDOWN", "Host is down");
        map.put("EHOSTUNREACH", "No route to host");
        map.put("EHWPOISON", "Memory page has hardware error");
        map.put("EIDRM", "Identifier removed");
        map.put("EILSEQ", "Invalid or incomplete multibyte or wide character");
        map.put("EINPROGRESS", "Operation now in progress");
        map.put("EINTR", "Interrupted system call");
        map.put("EINVAL", "Invalid argument");
        map.put("EIO", "Input/output error");
        map.put("EIPSEC", "IPsec processing failure");
        map.put("EISCONN", "Transport endpoint is already connected");
        map.put("EISDIR", "Is a directory");
        map.put("EISNAM", "Is a named type file");
        map.put("EKEYEXPIRED", "Key has expired");
        map.put("EKEYREJECTED", "Key was rejected by service");
        map.put("EKEYREVOKED", "Key has been revoked");
        map.put("EL2HLT", "Level 2 halted");
        map.put("EL2NSYNC", "Level 2 not synchronized");
        map.put("EL3HLT", "Level 3 halted");
        map.put("EL3RST", "Level 3 reset");
        map.put("ELAST", "Interface output queue is full");
        map.put("ELIBACC", "Can not access a needed shared library");
        map.put("ELIBBAD", "Accessing a corrupted shared library");
        map.put("ELIBEXEC", "Cannot exec a shared library directly");
        map.put("ELIBMAX", "Attempting to link in too many shared libraries");
        map.put("ELIBSCN", ".lib section in a.out corrupted");
        map.put("ELNRNG", "Link number out of range");
        map.put("ELOOP", "Too many levels of symbolic links");
        map.put("EMEDIUMTYPE", "Wrong medium type");
        map.put("EMFILE", "Too many open files");
        map.put("EMLINK", "Too many links");
        map.put("EMSGSIZE", "Message too long");
        map.put("EMULTIHOP", "Multihop attempted");
        map.put("ENAMETOOLONG", "File name too long");
        map.put("ENAVAIL", "No XENIX semaphores available");
        map.put("ENEEDAUTH", "Need authenticator");
        map.put("ENETDOWN", "Network is down");
        map.put("ENETRESET", "Network dropped connection on reset");
        map.put("ENETUNREACH", "Network is unreachable");
        map.put("ENFILE", "Too many open files in system");
        map.put("ENOANO", "No anode");
        map.put("ENOATTR", "Attribute not found");
        map.put("ENOBUFS", "No buffer space available");
        map.put("ENOCSI", "No CSI structure available");
        map.put("ENODATA", "No data available");
        map.put("ENODEV", "No such device");
        map.put("ENOENT", "No such file or directory");
        map.put("ENOEXEC", "Exec format error");
        map.put("ENOKEY", "Required key not available");
        map.put("ENOLCK", "No locks available");
        map.put("ENOLINK", "Link has been severed");
        map.put("ENOMEDIUM", "No medium found");
        map.put("ENOMEM", "Cannot allocate memory");
        map.put("ENOMSG", "No message of desired type");
        map.put("ENONET", "Machine is not on the network");
        map.put("ENOPKG", "Package not installed");
        map.put("ENOPOLICY", "Policy not found");
        map.put("ENOPROTOOPT", "Protocol not available");
        map.put("ENOSPC", "No space left on device");
        map.put("ENOSR", "Out of streams resources");
        map.put("ENOSTR", "Device not a stream");
        map.put("ENOSYS", "Function not implemented");
        map.put("ENOTBLK", "Block device required");
        map.put("ENOTCAPABLE", "Capabilities insufficient");
        map.put("ENOTCONN", "Transport endpoint is not connected");
        map.put("ENOTDIR", "Not a directory");
        map.put("ENOTEMPTY", "Directory not empty");
        map.put("ENOTNAM", "Not a XENIX named type file");
        map.put("ENOTRECOVERABLE", "State not recoverable");
        map.put("ENOTSOCK", "Socket operation on non-socket");
        map.put("ENOTSUP", "Operation not supported");
        map.put("ENOTTY", "Inappropriate ioctl for device");
        map.put("ENOTUNIQ", "Name not unique on network");
        map.put("ENXIO", "No such device or address");
        map.put("EOPNOTSUPP", "Operation not supported");
        map.put("EOVERFLOW", "Value too large for defined data type");
        map.put("EOWNERDEAD", "Owner died");
        map.put("EPERM", "Operation not permitted");
        map.put("EPFNOSUPPORT", "Protocol family not supported");
        map.put("EPIPE", "Broken pipe");
        map.put("EPROCLIM", "Too many processes");
        map.put("EPROCUNAVAIL", "Bad procedure for program");
        map.put("EPROGMISMATCH", "Program version wrong");
        map.put("EPROGUNAVAIL", "RPC prog. not avail");
        map.put("EPROTO", "Protocol error");
        map.put("EPROTONOSUPPORT", "Protocol not supported");
        map.put("EPROTOTYPE", "Protocol wrong type for socket");
        map.put("EPWROFF", "Device power is off");
        map.put("EQFULL", "Interface output queue is full");
        map.put("ERANGE", "Numerical result out of range");
        map.put("EREMCHG", "Remote address changed");
        map.put("EREMOTE", "Object is remote");
        map.put("EREMOTEIO", "Remote I/O error");
        map.put("ERESTART", "Interrupted system call should be restarted");
        map.put("ERFKILL", "Operation not possible due to RF-kill");
        map.put("EROFS", "Read-only file system");
        map.put("ERPCMISMATCH", "RPC version wrong");
        map.put("ESHLIBVERS", "Shared library version mismatch");
        map.put("ESHUTDOWN", "Cannot send after transport endpoint shutdown");
        map.put("ESOCKTNOSUPPORT", "Socket type not supported");
        map.put("ESPIPE", "Illegal seek");
        map.put("ESRCH", "No such process");
        map.put("ESRMNT", "Srmount error");
        map.put("ESTALE", "Stale file handle");
        map.put("ESTRPIPE", "Streams pipe error");
        map.put("ETIME", "Timer expired");
        map.put("ETIMEDOUT", "Connection timed out");
        map.put("ETOOMANYREFS", "Too many references: cannot splice");
        map.put("ETXTBSY", "Text file busy");
        map.put("EUCLEAN", "Structure needs cleaning");
        map.put("EUNATCH", "Protocol driver not attached");
        map.put("EUSERS", "Too many users");
        map.put("EWOULDBLOCK", "Resource temporarily unavailable");
        map.put("EXDEV", "Invalid cross-device link");
        map.put("EXFULL", "Exchange full");

        DESCRIPTIONS = Collections.unmodifiableMap(map);
    }

}
