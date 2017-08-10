/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MiscTest {

    @Test
    public void testMembersAndStringUnboxing() {
        try (Context context = Context.create()) {
            Value result = context.eval("ruby", "Truffle::Interop.object_literal(id: 42, text: '42', arr: [1,42,3])");
            assertTrue(result.hasMembers());

            int id = result.getMember("id").asInt();
            assertEquals(42, id);

            String text = result.getMember("text").asString();
            assertEquals("42", text);

            Value array = result.getMember("arr");
            assertTrue(array.hasArrayElements());
            assertEquals(3, array.getArraySize());
            assertEquals(42, array.getArrayElement(1).asInt());
        }
    }

}
