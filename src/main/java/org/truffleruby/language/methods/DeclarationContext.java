/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import java.util.Collections;
import java.util.Map;

import com.oracle.truffle.api.frame.MaterializedFrame;
import org.truffleruby.RubyContext;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.language.objects.SingletonClassNode;

/** The set of values captured when a method is defined:
 * <ul>
 * <li>the visibility of the method</li>
 * <li>the current module (default definee) to define the method on</li>
 * <li>the currently activated refinement modules which apply to the method</li>
 * </ul>
*/
public class DeclarationContext {
    public static final Map<RubyModule, RubyModule[]> NO_REFINEMENTS = Collections.emptyMap();

    /** @see <a href="http://yugui.jp/articles/846">http://yugui.jp/articles/846</a> */
    private interface DefaultDefinee {
        RubyModule getModuleToDefineMethods();
    }

    /** #instance_eval, the default definee is self.singleton_class */
    public static class SingletonClassOfSelfDefaultDefinee implements DefaultDefinee {
        private final Object self;

        public SingletonClassOfSelfDefaultDefinee(Object self) {
            this.self = self;
        }

        public RubyModule getModuleToDefineMethods() {
            return SingletonClassNode.getUncached().executeSingletonClass(self);
        }
    }

    /** class/module body or Module#class_eval, the default definee is opened module */
    public static class FixedDefaultDefinee implements DefaultDefinee {
        private final RubyModule module;

        public FixedDefaultDefinee(RubyModule module) {
            this.module = module;
        }

        public RubyModule getModuleToDefineMethods() {
            return module;
        }
    }

    public static DeclarationContext topLevel(RubyContext context) {
        return topLevel(context.getCoreLibrary().objectClass);
    }

    public static DeclarationContext topLevel(RubyModule defaultDefinee) {
        return new DeclarationContext(Visibility.PRIVATE, new FixedDefaultDefinee(defaultDefinee));
    }

    public final Visibility visibility;
    public final DefaultDefinee defaultDefinee;
    /** Maps a refined class (C) to refinement modules (M) */
    private final Map<RubyModule, RubyModule[]> refinements; // immutable

    public DeclarationContext(Visibility visibility, DefaultDefinee defaultDefinee) {
        this(visibility, defaultDefinee, NO_REFINEMENTS);
    }

    public DeclarationContext(
            Visibility visibility,
            DefaultDefinee defaultDefinee,
            Map<RubyModule, RubyModule[]> refinements) {
        assert refinements == NO_REFINEMENTS ||
                !refinements.isEmpty() : "Should use NO_REFINEMENTS if empty for faster getRefinementsFor()";
        this.visibility = visibility;
        this.defaultDefinee = defaultDefinee;
        this.refinements = refinements;
    }

    private static Frame lookupVisibility(Frame frame) {
        final Visibility visibility = RubyArguments.getDeclarationContext(frame).visibility;
        if (visibility != null) {
            return frame;
        } else {
            return lookupVisibilityInternal(RubyArguments.getDeclarationFrame(frame));
        }
    }

    @TruffleBoundary
    private static Frame lookupVisibilityInternal(MaterializedFrame frame) {
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

    public static Visibility findVisibility(Frame frame) {
        final Frame visibilityFrame = lookupVisibility(frame);
        return RubyArguments.getDeclarationContext(visibilityFrame).visibility;
    }

    public static Frame findVisibilityFrame(Frame frame) {
        return lookupVisibility(frame);
    }

    private static void changeVisibility(Frame frame, Visibility newVisibility) {
        final Frame visibilityFrame = lookupVisibility(frame);
        final DeclarationContext oldDeclarationContext = RubyArguments.getDeclarationContext(visibilityFrame);
        if (newVisibility != oldDeclarationContext.visibility) {
            RubyArguments.setDeclarationContext(visibilityFrame, oldDeclarationContext.withVisibility(newVisibility));
        }
    }

    public static void setCurrentVisibility(Frame callerFrame, Visibility visibility) {
        changeVisibility(callerFrame, visibility);
    }

    public static void setRefinements(Frame callerFrame, DeclarationContext declarationContext,
            Map<RubyModule, RubyModule[]> refinements) {
        RubyArguments.setDeclarationContext(callerFrame, declarationContext.withRefinements(refinements));
    }

    public DeclarationContext withVisibility(Visibility visibility) {
        if (visibility == this.visibility) {
            return this;
        } else {
            return new DeclarationContext(visibility, defaultDefinee, refinements);
        }
    }

    public DeclarationContext withRefinements(Map<RubyModule, RubyModule[]> refinements) {
        assert refinements != null;
        return new DeclarationContext(visibility, defaultDefinee, refinements);
    }

    public Map<RubyModule, RubyModule[]> getRefinements() {
        return refinements;
    }

    public RubyModule[] getRefinementsFor(RubyModule module) {
        return refinements.get(module);
    }

    @TruffleBoundary
    public RubyModule getModuleToDefineMethods() {
        assert defaultDefinee != null : "Trying to find the default definee but this method should not have method definitions inside";
        return defaultDefinee.getModuleToDefineMethods();
    }

    public boolean hasRefinements() {
        return refinements != NO_REFINEMENTS;
    }

    /** Used when we know there cannot be a method definition inside a given method. */
    public static final DeclarationContext NONE = new DeclarationContext(Visibility.PUBLIC, null);

    @TruffleBoundary
    @Override
    public String toString() {
        return "DeclarationContext{" +
                "visibility=" + visibility +
                ", defaultDefinee=" + defaultDefinee +
                ", refinements=" + refinements +
                "}@" + hashCode();
    }
}
