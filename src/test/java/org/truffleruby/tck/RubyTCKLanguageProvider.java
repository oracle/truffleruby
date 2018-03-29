/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.tck;

import static org.graalvm.polyglot.tck.TypeDescriptor.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.tck.LanguageProvider;
import org.graalvm.polyglot.tck.ResultVerifier;
import org.graalvm.polyglot.tck.Snippet;
import org.graalvm.polyglot.tck.TypeDescriptor;
import org.junit.Assert;
import org.truffleruby.RubyTest;

public class RubyTCKLanguageProvider implements LanguageProvider {

    private static final String ID = "ruby";

    private static final String PATTERN_VALUE_FNC = "-> { %s }";
    private static final String PATTERN_UNARY_OP = "-> a { %s }";
    private static final String PATTERN_BINARY_OP = "-> a, b { %s }";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public Value createIdentityFunction(Context context) {
        return context.eval(ID, "-> v { v }");
    }

    @Override
    public Collection<? extends Snippet> createValueConstructors(Context context) {
        final List<Snippet> vals = new ArrayList<>();
        // nil
        vals.add(createValueConstructor(context, "nil", NULL));
        // true/false
        vals.add(createValueConstructor(context, "false", BOOLEAN));
        // Numeric
        // NOTE: NUMBER is only for primitives and types which are instanceof java.lang.Number.
        vals.add(createValueConstructor(context, "7", NUMBER)); // int
        vals.add(createValueConstructor(context, "1 << 42", NUMBER)); // long
        vals.add(createValueConstructor(context, "3.14", NUMBER));
        vals.add(createValueConstructor(context, "1 << 84", OBJECT)); // Bignum
        vals.add(createValueConstructor(context, "Rational(1, 3)", OBJECT));
        vals.add(createValueConstructor(context, "Complex(1, 2)", OBJECT));
        // String
        vals.add(createValueConstructor(context, "'test'", STRING));
        vals.add(createValueConstructor(context, "'0123456789' + '0123456789'", STRING)); // Rope
        // Array
        // arrays are array-like and also regular objects
        final TypeDescriptor numArray = alsoRegularObject(array(NUMBER));
        vals.add(createValueConstructor(context, "[1, 2]", numArray));
        vals.add(createValueConstructor(context, "[1.2, 3.4]", numArray));
        vals.add(createValueConstructor(context, "[1<<33, 1<<34]", numArray));
        final TypeDescriptor arrayType = alsoRegularObject(ARRAY);
        vals.add(createValueConstructor(context, "[]", arrayType));
        vals.add(createValueConstructor(context, "[Object.new]", arrayType));
        vals.add(createValueConstructor(context, "[true, false]", arrayType));
        vals.add(createValueConstructor(context, "[Object.new, 65]", arrayType));
        // Hash
        vals.add(createValueConstructor(context, "{ name: 'test' }", OBJECT));
        // Struct
        vals.add(createValueConstructor(context, "Struct.new(:foo, :bar).new(1, 'two')", OBJECT));
        // Object
        vals.add(createValueConstructor(context, "Object.new.tap { |obj| obj.instance_variable_set(:@name, 'test') }", OBJECT));
        // Proc
        vals.add(createValueConstructor(context, "proc {}", alsoRegularObject(EXECUTABLE)));
        vals.add(createValueConstructor(context, "-> {}", alsoRegularObject(executable(NULL, false))));
        vals.add(createValueConstructor(context, ":itself.to_proc", alsoRegularObject(executable(ANY, ANY))));
        // Method
        vals.add(createValueConstructor(context, "1.method(:itself)", alsoRegularObject(executable(NUMBER, false))));

        return Collections.unmodifiableList(vals);
    }

    private static TypeDescriptor alsoRegularObject(TypeDescriptor type) {
        return intersection(OBJECT, type);
    }

    @Override
    public Collection<? extends Snippet> createExpressions(Context context) {
        final List<Snippet> ops = new ArrayList<>();

        // arithmetic
        // +
        ops.add(createBinaryOperator(context, "a + b", NUMBER, NUMBER, NUMBER));
        ops.add(createBinaryOperator(context, "a + b", STRING, STRING, STRING));
        ops.add(createBinaryOperator(context, "a + b", ARRAY, ARRAY, ARRAY));
        // -
        ops.add(createBinaryOperator(context, "a - b", NUMBER, NUMBER, NUMBER));
        ops.add(createBinaryOperator(context, "a - b", ARRAY, ARRAY, ARRAY));
        // *
        ops.add(createBinaryOperator(context, "a * b", NUMBER, NUMBER, NUMBER));
        ops.add(createBinaryOperator(context, "a * b", STRING, NUMBER, STRING));
        ops.add(createBinaryOperator(context, "a * b", ARRAY, NUMBER, ARRAY));
        // /
        ops.add(createBinaryOperator(context, "a / b", NUMBER, NUMBER, NUMBER));
        // %
        ops.add(createBinaryOperator(context, "a % b", NUMBER, NUMBER, NUMBER));
        // **
        ops.add(createBinaryOperator(context, "a ** b", NUMBER, NUMBER, NUMBER));

        // comparison
        // equal?
        ops.add(createBinaryOperator(context, "a.equal?(b)", ANY, ANY, BOOLEAN));
        // ==
        ops.add(createBinaryOperator(context, "a == b", ANY, ANY, BOOLEAN));
        // !=
        ops.add(createBinaryOperator(context, "a != b", ANY, ANY, BOOLEAN));
        // <
        ops.add(createBinaryOperator(context, "a < b", NUMBER, NUMBER, BOOLEAN));
        // >
        ops.add(createBinaryOperator(context, "a > b", NUMBER, NUMBER, BOOLEAN));
        // <=
        ops.add(createBinaryOperator(context, "a <= b", NUMBER, NUMBER, BOOLEAN));
        // <=
        ops.add(createBinaryOperator(context, "a >= b", NUMBER, NUMBER, BOOLEAN));

        // bitwise
        // <<
        ops.add(createBinaryOperator(context, "a << b", NUMBER, NUMBER, NUMBER));
        // >>
        ops.add(createBinaryOperator(context, "a >> b", NUMBER, NUMBER, NUMBER));
        // &
        ops.add(createBinaryOperator(context, "a & b", NUMBER, NUMBER, NUMBER));
        // |
        ops.add(createBinaryOperator(context, "a | b", NUMBER, NUMBER, NUMBER));
        // ^
        ops.add(createBinaryOperator(context, "a ^ b", NUMBER, NUMBER, NUMBER));

        // logical
        // &&
        ops.add(createBinaryOperator(context, "a && b", ANY, ANY, ANY));
        // ||
        ops.add(createBinaryOperator(context, "a || b", ANY, ANY, ANY));

        // unary operators
        // +
        ops.add(createUnaryOperator(context, "+a", NUMBER, NUMBER));
        // -
        ops.add(createUnaryOperator(context, "-a", NUMBER, NUMBER));
        // ~
        ops.add(createUnaryOperator(context, "~a", NUMBER, NUMBER));
        // !
        ops.add(createUnaryOperator(context, "!a", ANY, BOOLEAN));

        return Collections.unmodifiableList(ops);
    }

    @Override
    public Collection<? extends Snippet> createStatements(Context context) {
        final List<Snippet> res = new ArrayList<>();
        // if
        res.add(createStatement(context, "if", "-> c { if c; true else false end };", ANY, BOOLEAN));
        res.add(createStatement(context, "unless", "-> c { unless c; true else false end };", ANY, BOOLEAN));
        // postfix if
        res.add(createStatement(context, "postfix if", "-> c { true if c };", ANY, union(BOOLEAN, NULL)));
        res.add(createStatement(context, "postfix unless", "-> c { true unless c };", ANY, union(BOOLEAN, NULL)));
        // while
        res.add(createStatement(context, "while", "-> c { while c; break; end }", ANY, NULL));
        res.add(createStatement(context, "until", "-> c { until c; break; end }", ANY, NULL));
        // do while
        res.add(createStatement(context, "do while", "-> c { begin; break; end while c }", ANY, NULL));
        res.add(createStatement(context, "do until", "-> c { begin; break; end until c }", ANY, NULL));
        // for
        res.add(createStatement(context, "for", "-> array { for e in array do; end }", ARRAY, ARRAY));
        // case
        res.add(createStatement(context, "case", "-> e { case e; when Integer; 1; else 2; end }", ANY, NUMBER));
        // raise
        res.add(createStatement(context, "raise", "-> msg { begin; raise msg; rescue => e; e; end}", STRING, OBJECT));

        return Collections.unmodifiableList(res);
    }

    @Override
    public Collection<? extends Snippet> createScripts(Context context) {
        final List<Snippet> res = new ArrayList<>();

        Snippet.newBuilder("array of Points", context.eval(RubyTest.getSource("src/test/ruby/points.rb")), array(OBJECT)).resultVerifier(run -> {
            ResultVerifier.getDefaultResultVerfier().accept(run);
            final Value result = run.getResult();
            Assert.assertEquals("Array size", 2, result.getArraySize());
            Value p1 = result.getArrayElement(0);
            Value p2 = result.getArrayElement(1);
            Assert.assertEquals("res[0].x", 30, p1.getMember("x").asInt());
            Assert.assertEquals("res[0].y", 15, p1.getMember("y").asInt());
            Assert.assertEquals("res[1].x", 5, p2.getMember("x").asInt());
            Assert.assertEquals("res[1].y", 7, p2.getMember("y").asInt());
        });

        Snippet.newBuilder("recursion", context.eval(RubyTest.getSource("src/test/ruby/recursion.rb")), array(NUMBER)).resultVerifier(run -> {
            ResultVerifier.getDefaultResultVerfier().accept(run);
            final Value result = run.getResult();
            Assert.assertEquals("Array size", 3, result.getArraySize());
            Assert.assertEquals("res[0]", 3628800, result.getArrayElement(0).asInt());
            Assert.assertEquals("res[1]", 55, result.getArrayElement(1).asInt());
            Assert.assertEquals("res[2]", 125, result.getArrayElement(2).asInt());
        });

        return Collections.unmodifiableList(res);
    }

    @Override
    public Collection<? extends Source> createInvalidSyntaxScripts(Context context) {
        final List<Source> res = new ArrayList<>();
        res.add(Source.create(ID, "{"));
        res.add(Source.create(ID, "("));
        res.add(Source.create(ID, "if no_end"));
        return Collections.unmodifiableList(res);
    }

    // TODO (eregon, 29 Mar 2018): Add createInlineScripts(), which requires to implement
    // TruffleLanguage.parse(InlineParsingRequest).

    private static Snippet createValueConstructor(Context context, String value, TypeDescriptor type) {
        return Snippet.newBuilder(value, context.eval(ID, String.format(PATTERN_VALUE_FNC, value)), type).build();
    }

    private static Snippet createUnaryOperator(Context context, String operator, TypeDescriptor operandType, TypeDescriptor returnType) {
        final Value fnc = context.eval(ID, String.format(PATTERN_UNARY_OP, operator));
        return Snippet.newBuilder(operator, fnc, returnType).parameterTypes(operandType).build();
    }

    private static Snippet createBinaryOperator(Context context, String operator, TypeDescriptor lhsType, TypeDescriptor rhsType, TypeDescriptor returnType) {
        return createBinaryOperator(context, operator, lhsType, rhsType, returnType, null);
    }

    private static Snippet createBinaryOperator(Context context, String operator, TypeDescriptor lhsType, TypeDescriptor rhsType, TypeDescriptor returnType, ResultVerifier verifier) {
        final Value fnc = context.eval(ID, String.format(PATTERN_BINARY_OP, operator));
        return Snippet.newBuilder(operator, fnc, returnType).parameterTypes(lhsType, rhsType).resultVerifier(verifier).build();
    }

    private static Snippet createStatement(Context context, String name, String expression, TypeDescriptor argumentType, TypeDescriptor returnType) {
        final Value fnc = context.eval(ID, expression);
        return Snippet.newBuilder(name, fnc, returnType).parameterTypes(argumentType).build();
    }

}

