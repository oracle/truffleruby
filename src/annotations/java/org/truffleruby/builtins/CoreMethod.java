/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import org.truffleruby.language.Visibility;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CoreMethod {

    String[] names();

    Visibility visibility() default Visibility.PUBLIC;

    /**
     * Defines the method on the singleton class. {@link #needsSelf() needsSelf} is always false. See
     * {@link #constructor() constructor} if you need self.
     */
    boolean onSingleton() default false;

    /**
     * Like {@link #onSingleton() onSingleton} but with {@link #needsSelf() needsSelf} always true.
     */
    boolean constructor() default false;

    /**
     * Defines the method as public on the singleton class and as a private instance method.
     * {@link #needsSelf() needsSelf} is always false as it could be either a module or any receiver.
     */
    boolean isModuleFunction() default false;

    boolean needsSelf() default true;

    int required() default 0;

    int optional() default 0;

    /**
     * Declares that a keyword argument with this name will be passed into the method as if it was an
     * extra trailing positional optional argument.
     */
    String keywordAsOptional() default "";

    boolean rest() default false;

    boolean needsBlock() default false;

    /**
     * Try to lower argument <code>i</code> (starting at 1) to an int if its value is a long.
     * Use 0 for <code>self</code>.
     */
    int[] lowerFixnum() default {};

    /**
     * Raise an error if self is frozen.
     */
    boolean raiseIfFrozenSelf() default false;

    /**
     * Taint the result if argument <code>i</code> (starting at 1) is tainted.
     * Use 0 for <code>self</code>.
     */
    int taintFrom() default -1;

    UnsupportedOperationBehavior unsupportedOperationBehavior() default UnsupportedOperationBehavior.TYPE_ERROR;

    boolean returnsEnumeratorIfNoBlock() default false;

    /**
     * Method to call to determine the size of the returned Enumerator. Implies
     * {@link #returnsEnumeratorIfNoBlock()}.
     */
    String enumeratorSize() default "";

    /**
     * Disallow method splitting for this CoreMethod. Useful for methods not specializing on their
     * arguments and just calling a TruffleBoundary method.
     */
    boolean neverSplit() default false;

}
