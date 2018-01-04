/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.objects.SingletonClassNode;

/**
 * Declaration context for methods:
 * <ul>
 * <li>visibility</li>
 * <li>default definee / current module (which module to define on)</li>
 * </ul>
 */
public class DeclarationContext {

    /** @see <a href="http://yugui.jp/articles/846">http://yugui.jp/articles/846</a> */
    private interface DefaultDefinee {
        DynamicObject getModuleToDefineMethods(Object self, InternalMethod method, RubyContext context, SingletonClassNode singletonClassNode);
    }

    private static class LexicalScopeDefaultDefinee implements DefaultDefinee {
        public DynamicObject getModuleToDefineMethods(Object self, InternalMethod method, RubyContext context, SingletonClassNode singletonClassNode) {
            return method.getSharedMethodInfo().getLexicalScope().getLiveModule();
        }
    }

    public static class SingletonClassOfSelfDefaultDefinee implements DefaultDefinee {
        private final Object self;

        public SingletonClassOfSelfDefaultDefinee(Object self) {
            this.self = self;
        }

        public DynamicObject getModuleToDefineMethods(Object self, InternalMethod method, RubyContext context, SingletonClassNode singletonClassNode) {
            return singletonClassNode.executeSingletonClass(this.self);
        }
    }

    public static class FixedDefaultDefinee implements DefaultDefinee {
        private final DynamicObject module;

        public FixedDefaultDefinee(DynamicObject module) {
            assert RubyGuards.isRubyModule(module);
            this.module = module;
        }

        public DynamicObject getModuleToDefineMethods(Object self, InternalMethod method, RubyContext context, SingletonClassNode singletonClassNode) {
            return module;
        }
    }

    public final Visibility visibility;
    public final DefaultDefinee defaultDefinee;

    public DeclarationContext(Visibility visibility, DefaultDefinee defaultDefinee) {
        this.visibility = visibility;
        this.defaultDefinee = defaultDefinee;
    }

    @TruffleBoundary
    private static Frame lookupVisibility(Frame frame) {
        while (frame != null) {
            final Visibility visibility = RubyArguments.getDeclarationContext(frame).visibility;
            if (visibility != null) {
                return frame;
            }
            frame = RubyArguments.getDeclarationFrame(frame);
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new UnsupportedOperationException("No declaration frame with visibility found");
    }

    @TruffleBoundary
    public static Visibility findVisibility(Frame frame) {
        final Frame visibilityFrame = lookupVisibility(frame);
        return RubyArguments.getDeclarationContext(visibilityFrame).visibility;
    }

    @TruffleBoundary
    private static void changeVisibility(Frame frame, Visibility newVisibility) {
        final Frame visibilityFrame = lookupVisibility(frame);
        final DeclarationContext oldDeclarationContext = RubyArguments.getDeclarationContext(visibilityFrame);
        if (newVisibility != oldDeclarationContext.visibility) {
            RubyArguments.setDeclarationContext(visibilityFrame, oldDeclarationContext.withVisibility(newVisibility));
        }
    }

    @TruffleBoundary
    public static void setCurrentVisibility(RubyContext context, Visibility visibility) {
        final Frame callerFrame = context.getCallStack().getCallerFrameIgnoringSend().getFrame(FrameAccess.READ_WRITE);
        changeVisibility(callerFrame, visibility);
    }

    private DeclarationContext withVisibility(Visibility visibility) {
        assert visibility != null;
        return new DeclarationContext(visibility, defaultDefinee);
    }

    @TruffleBoundary
    public DynamicObject getModuleToDefineMethods(Object self, InternalMethod method, RubyContext context, SingletonClassNode singletonClassNode) {
        assert defaultDefinee != null : "Trying to find the default definee but this method should not have method definitions inside";
        return defaultDefinee.getModuleToDefineMethods(self, method, context, singletonClassNode);
    }

    private static final DefaultDefinee LEXICAL_SCOPE_DEFAULT_DEFINEE = new LexicalScopeDefaultDefinee();

    public static final DeclarationContext METHOD = new DeclarationContext(Visibility.PUBLIC, LEXICAL_SCOPE_DEFAULT_DEFINEE);
    public static final DeclarationContext BLOCK = new DeclarationContext(null, LEXICAL_SCOPE_DEFAULT_DEFINEE);
    public static final DeclarationContext TOP_LEVEL = new DeclarationContext(Visibility.PRIVATE, LEXICAL_SCOPE_DEFAULT_DEFINEE);
    public static final DeclarationContext INSTANCE_EVAL = null;

    /** Used when we know there cannot be a method definition inside a given method. */
    public static final DeclarationContext NONE = new DeclarationContext(Visibility.PUBLIC, null);

}
