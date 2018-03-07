package org.truffleruby.stdlib.psych;

import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.yaml.snakeyaml.events.Event;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout
public interface PsychEventLayout extends BasicObjectLayout {

    DynamicObjectFactory createPsychEventShape(DynamicObject logicalClass, DynamicObject metaClass);

    DynamicObject createPsychEvent(DynamicObjectFactory factory, Event event);

    boolean isPsychEvent(DynamicObject object);

    Event getEvent(DynamicObject object);
}
