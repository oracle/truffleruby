package org.truffleruby.stdlib.psych;

import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.yaml.snakeyaml.parser.Parser;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout
public interface PsychParserLayout extends BasicObjectLayout {

    DynamicObjectFactory createPsychParserShape(DynamicObject logicalClass, DynamicObject metaClass);

    DynamicObject createPsychParser(DynamicObjectFactory factory, Parser parser);

    boolean isPsychParser(DynamicObject object);

    Parser getParser(DynamicObject object);
}
