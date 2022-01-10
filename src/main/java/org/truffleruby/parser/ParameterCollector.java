/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.ArrayList;
import java.util.List;

import org.truffleruby.parser.ast.ArgsParseNode;
import org.truffleruby.parser.ast.ArgumentParseNode;
import org.truffleruby.parser.ast.ArrayParseNode;
import org.truffleruby.parser.ast.BlockArgParseNode;
import org.truffleruby.parser.ast.BlockParseNode;
import org.truffleruby.parser.ast.ClassVarAsgnParseNode;
import org.truffleruby.parser.ast.DAsgnParseNode;
import org.truffleruby.parser.ast.KeywordRestArgParseNode;
import org.truffleruby.parser.ast.ListParseNode;
import org.truffleruby.parser.ast.LocalAsgnParseNode;
import org.truffleruby.parser.ast.MultipleAsgnParseNode;
import org.truffleruby.parser.ast.OptArgParseNode;
import org.truffleruby.parser.ast.ParseNode;
import org.truffleruby.parser.ast.RestArgParseNode;
import org.truffleruby.parser.ast.visitor.AbstractNodeVisitor;

/** Collects parameter names from a JRuby AST. */
public class ParameterCollector extends AbstractNodeVisitor<Object> {

    private final List<String> parameters = new ArrayList<>();

    public Iterable<String> getParameters() {
        return parameters;
    }

    @Override
    protected Object defaultVisit(ParseNode node) {
        return null;
    }

    @Override
    public Object visitArgsNode(ArgsParseNode node) {
        for (ParseNode child : node.childNodes()) {
            child.accept(this);
        }
        return null;
    }

    @Override
    public Object visitArgumentNode(ArgumentParseNode node) {
        parameters.add(node.getName());
        return null;
    }

    @Override
    public Object visitArrayNode(ArrayParseNode node) {
        for (ParseNode child : node.children()) {
            if (child != null) {
                child.accept(this);
            }
        }
        return null;
    }

    @Override
    public Object visitBlockArgNode(BlockArgParseNode node) {
        parameters.add(node.getName());
        return null;
    }

    @Override
    public Object visitBlockNode(BlockParseNode node) {
        for (ParseNode child : node.children()) {
            if (child != null) {
                child.accept(this);
            }
        }
        return null;
    }

    @Override
    public Object visitClassVarAsgnNode(ClassVarAsgnParseNode node) {
        throw new UnsupportedOperationException(node.toString());
    }

    @Override
    public Object visitDAsgnNode(DAsgnParseNode node) {
        parameters.add(node.getName());
        return null;
    }

    @Override
    public Object visitListNode(ListParseNode node) {
        for (ParseNode child : node.children()) {
            if (child != null) {
                child.accept(this);
            }
        }
        return null;
    }

    @Override
    public Object visitMultipleAsgnNode(MultipleAsgnParseNode node) {
        for (ParseNode child : node.childNodes()) {
            if (child != null) {
                child.accept(this);
            }
        }
        return null;
    }

    @Override
    public Object visitOptArgNode(OptArgParseNode node) {
        parameters.add(node.getName());
        node.getValue().accept(this);
        return null;
    }

    @Override
    public Object visitLocalAsgnNode(LocalAsgnParseNode node) {
        parameters.add(node.getName());
        node.getValueNode().accept(this);
        return null;
    }

    @Override
    public Object visitRestArgNode(RestArgParseNode node) {
        parameters.add(node.getName());
        return null;
    }

    @Override
    public Object visitKeywordRestArgNode(KeywordRestArgParseNode node) {
        parameters.add(node.getName());
        return null;
    }

}
