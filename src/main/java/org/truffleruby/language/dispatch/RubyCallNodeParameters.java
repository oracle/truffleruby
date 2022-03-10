/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.ArgumentsDescriptor;

public class RubyCallNodeParameters {

    private final RubyNode receiver;
    private final String methodName;
    private final RubyNode block;
    private final ArgumentsDescriptor descriptor;
    private final RubyNode[] arguments;
    private final boolean isSplatted;
    private final boolean ignoreVisibility;
    private final boolean isVCall;
    private final boolean isSafeNavigation;
    private final boolean isAttrAssign;

    public RubyCallNodeParameters(
            RubyNode receiver,
            String methodName,
            RubyNode block,
            ArgumentsDescriptor descriptor,
            RubyNode[] arguments,
            boolean isSplatted,
            boolean ignoreVisibility) {
        this(receiver, methodName, block, descriptor, arguments, isSplatted, ignoreVisibility, false, false, false);
    }

    public RubyCallNodeParameters(
            RubyNode receiver,
            String methodName,
            RubyNode block,
            ArgumentsDescriptor descriptor,
            RubyNode[] arguments,
            boolean isSplatted,
            boolean ignoreVisibility,
            boolean isVCall,
            boolean isSafeNavigation,
            boolean isAttrAssign) {
        this.receiver = receiver;
        this.methodName = methodName;
        this.block = block;
        this.descriptor = descriptor;
        this.arguments = arguments;
        this.isSplatted = isSplatted;
        this.ignoreVisibility = ignoreVisibility;
        this.isVCall = isVCall;
        this.isSafeNavigation = isSafeNavigation;
        this.isAttrAssign = isAttrAssign;
    }

    public RubyCallNodeParameters withReceiverAndArguments(RubyNode receiver, RubyNode[] arguments, RubyNode block) {
        return new RubyCallNodeParameters(
                receiver,
                methodName,
                block,
                descriptor,
                arguments,
                isSplatted,
                ignoreVisibility,
                isVCall,
                isSafeNavigation,
                isAttrAssign);
    }

    public RubyCallNodeParameters withBlock(RubyNode block) {
        return withReceiverAndArguments(receiver, arguments, block);
    }

    public RubyNode getReceiver() {
        return receiver;
    }

    public String getMethodName() {
        return methodName;
    }

    public RubyNode getBlock() {
        return block;
    }

    public ArgumentsDescriptor getDescriptor() {
        return descriptor;
    }

    public RubyNode[] getArguments() {
        return arguments;
    }

    public boolean isSplatted() {
        return isSplatted;
    }

    public boolean isIgnoreVisibility() {
        return ignoreVisibility;
    }

    public boolean isVCall() {
        return isVCall;
    }

    public boolean isSafeNavigation() {
        return isSafeNavigation;
    }

    public boolean isAttrAssign() {
        return isAttrAssign;
    }

}
