package org.truffleruby.utils;

/** Exception to be thrown in code paths that should never be reached. */
public final class UnreachableCodeException extends RuntimeException {

    public static final long serialVersionUID = 0;

    public UnreachableCodeException() {
    }

    public UnreachableCodeException(String msg) {
        super(msg);
    }
}
