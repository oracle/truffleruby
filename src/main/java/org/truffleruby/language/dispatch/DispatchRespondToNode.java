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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.methods.LookupMethodNodeGen;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.MetaClassNodeGen;

public class DispatchRespondToNode extends RubyBaseNode {

    // NOTE(norswap): cf. comment above static fields in DispatchNode to see why we need this field
    public static final DispatchConfiguration PUBLIC_DOES_RESPOND = DispatchConfiguration.PUBLIC_DOES_RESPOND;

    public static DispatchRespondToNode create(DispatchConfiguration config) {
        return new DispatchRespondToNode(config);
    }

    public static DispatchRespondToNode create() {
        return new DispatchRespondToNode(DispatchConfiguration.PRIVATE_DOES_RESPOND);
    }

    public static DispatchRespondToNode getUncached(DispatchConfiguration config) {
        return Uncached.UNCACHED_NODES[config.ordinal()];
    }

    public static DispatchRespondToNode getUncached() {
        return getUncached(DispatchConfiguration.PRIVATE_DOES_RESPOND);
    }

    public final DispatchConfiguration config;
    @Child protected MetaClassNode metaclassNode;
    @Child protected LookupMethodNode methodLookup;

    protected DispatchRespondToNode(
            DispatchConfiguration config,
            MetaClassNode metaclassNode,
            LookupMethodNode methodLookup) {
        this.config = config;
        this.metaclassNode = metaclassNode;
        this.methodLookup = methodLookup;
    }

    protected DispatchRespondToNode(DispatchConfiguration config) {
        this(config, MetaClassNode.create(), LookupMethodNode.create());
    }

    public boolean doesRespondTo(VirtualFrame frame, String methodName, Object receiver) {
        return (boolean) execute(frame, receiver, methodName, null, EMPTY_ARGUMENTS);
    }

    public Object execute(VirtualFrame frame, Object receiver, String methodName, RubyProc block, Object[] arguments) {
        assert config.dispatchAction == DispatchAction.RESPOND_TO_METHOD;
        final RubyClass metaclass = metaclassNode.execute(receiver);
        final InternalMethod method = methodLookup.execute(frame, metaclass, methodName, config);
        return method != null && method.isDefined() && method.isImplemented();
    }

    private static class Uncached extends DispatchRespondToNode {

        static final Uncached[] UNCACHED_NODES = new Uncached[DispatchConfiguration.values().length];
        static {
            for (DispatchConfiguration config : DispatchConfiguration.values()) {
                UNCACHED_NODES[config.ordinal()] = new Uncached(config);
            }
        }

        protected Uncached(DispatchConfiguration config) {
            super(config, MetaClassNodeGen.getUncached(), LookupMethodNodeGen.getUncached());
        }

        @Override
        public Object execute(VirtualFrame frame, Object receiver, String methodName, RubyProc block,
                Object[] arguments) {
            return super.execute(null, receiver, methodName, block, arguments);
        }

        @Override
        public NodeCost getCost() {
            return NodeCost.MEGAMORPHIC;
        }

        @Override
        public boolean isAdoptable() {
            return false;
        }
    }
}
