/*
 * Copyright (c) 2017, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser.ast;

import java.math.BigInteger;
import java.util.List;

import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.ast.visitor.NodeVisitor;

public class BigRationalParseNode extends NumericParseNode implements SideEffectFree {
    private final BigInteger numerator;
    private final BigInteger denominator;

    public BigRationalParseNode(SourceIndexLength position, BigInteger numerator, BigInteger denominator) {
        super(position);

        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitBigRationalNode(this);
    }

    @Override
    public List<ParseNode> childNodes() {
        return EMPTY_LIST;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.BIGRATIONALNODE;
    }

    public BigInteger getNumerator() {
        return numerator;
    }

    public BigInteger getDenominator() {
        return denominator;
    }
}
