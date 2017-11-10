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

import java.util.Collections;
import java.util.Map;

/**
 * The set of values captured when a method is defined:
 * <ul>
 * <li>the visibility of the method</li>
 * <li>the current module (default definee) to define the method on</li>
 * <li>the currently activated refinement modules which apply to the method</li>
 * </ul>
 */
public class DeclarationContext {

    /** @see <a href="http://yugui.jp/articles/846">http://yugui.jp/articles/846</a> */
    private interface DefaultDefinee {
        DynamicObject getModuleToDefineMethods(SingletonClassNode singletonClassNode);
    }

    /** #instance_eval, the default definee is self.singleton_class */
    public static class SingletonClassOfSelfDefaultDefinee implements DefaultDefinee {
        private final Object self;

        public SingletonClassOfSelfDefaultDefinee(Object self) {
            this.self = self;
        }

        public DynamicObject getModuleToDefineMethods(SingletonClassNode singletonClassNode) {
            return singletonClassNode.executeSingletonClass(self);
        }
    }

    /** class/module body or Module#class_eval, the default definee is opened module */
    public static class FixedDefaultDefinee implements DefaultDefinee {
        private final DynamicObject module;

        public FixedDefaultDefinee(DynamicObject module) {
            assert RubyGuards.isRubyModule(module);
            this.module = module;
        }

        public DynamicObject getModuleToDefineMethods(SingletonClassNode singletonClassNode) {
            return module;
        }
    }

    public static DeclarationContext topLevel(RubyContext context) {
        return new DeclarationContext(Visibility.PRIVATE, new FixedDefaultDefinee(context.getCoreLibrary().getObjectClass()));
    }

    public final Visibility visibility;
    public final DefaultDefinee defaultDefinee;
    /** Maps a refined class (C) to refinement modules (M) */
    private final Map<DynamicObject, DynamicObject[]> refinements; // immutable

    public DeclarationContext(Visibility visibility, DefaultDefinee defaultDefinee) {
        this(visibility, defaultDefinee, Collections.emptyMap());
    }

    public DeclarationContext(Visibility visibility, DefaultDefinee defaultDefinee, Map<DynamicObject, DynamicObject[]> refinements) {
        this.visibility = visibility;
        this.defaultDefinee = defaultDefinee;
        this.refinements = refinements;
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

    @TruffleBoundary
    public static void setRefinements(Frame callerFrame, DeclarationContext declarationContext, Map<DynamicObject, DynamicObject[]> refinements) {
        RubyArguments.setDeclarationContext(callerFrame, declarationContext.withRefinements(refinements));
    }

    public DeclarationContext withVisibility(Visibility visibility) {
        if (visibility == this.visibility) {
            return this;
        } else {
            return new DeclarationContext(visibility, defaultDefinee, refinements);
        }
    }

    public DeclarationContext withRefinements(Map<DynamicObject, DynamicObject[]> refinements) {
        assert refinements != null;
        return new DeclarationContext(visibility, defaultDefinee, refinements);
    }

    public Map<DynamicObject, DynamicObject[]> getRefinements() {
        return refinements;
    }

    public DynamicObject[] getRefinementsFor(DynamicObject module) {
        return refinements.get(module);
    }

    @TruffleBoundary
    public DynamicObject getModuleToDefineMethods(SingletonClassNode singletonClassNode) {
        assert defaultDefinee != null : "Trying to find the default definee but this method should not have method definitions inside";
        return defaultDefinee.getModuleToDefineMethods(singletonClassNode);
    }

    /** Used when we know there cannot be a method definition inside a given method. */
    public static final DeclarationContext NONE = new DeclarationContext(Visibility.PUBLIC, null);

}
