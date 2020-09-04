/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.CompilerDirectives;

public enum DispatchConfiguration {
    PUBLIC(false, true, MissingBehavior.CALL_METHOD_MISSING, DispatchAction.CALL_METHOD),
    PRIVATE(true, false, MissingBehavior.CALL_METHOD_MISSING, DispatchAction.CALL_METHOD),
    PROTECTED(false, false, MissingBehavior.CALL_METHOD_MISSING, DispatchAction.CALL_METHOD),
    PUBLIC_RETURN_MISSING(false, true, MissingBehavior.RETURN_MISSING, DispatchAction.CALL_METHOD),
    PRIVATE_RETURN_MISSING(true, false, MissingBehavior.RETURN_MISSING, DispatchAction.CALL_METHOD),
    PUBLIC_DOES_RESPOND(false, true, MissingBehavior.RETURN_MISSING, DispatchAction.RESPOND_TO_METHOD),
    PRIVATE_DOES_RESPOND(true, false, MissingBehavior.RETURN_MISSING, DispatchAction.RESPOND_TO_METHOD);

    public final boolean ignoreVisibility;
    public final boolean onlyLookupPublic;
    public final MissingBehavior missingBehavior;
    public final DispatchAction dispatchAction;

    DispatchConfiguration(
            boolean ignoreVisibility,
            boolean onlyLookupPublic,
            MissingBehavior missingBehavior,
            DispatchAction dispatchAction) {
        this.ignoreVisibility = ignoreVisibility;
        this.onlyLookupPublic = onlyLookupPublic;
        this.missingBehavior = missingBehavior;
        this.dispatchAction = dispatchAction;
    }

    public static DispatchConfiguration from(boolean ignoreVisibility, boolean onlyLookupPublic) {
        if (ignoreVisibility) {
            if (onlyLookupPublic) {
                throw CompilerDirectives.shouldNotReachHere();
            }
            return PRIVATE;
        }

        return onlyLookupPublic ? PUBLIC : PROTECTED;
    }
}
