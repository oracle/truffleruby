/*
 ***** BEGIN LICENSE BLOCK *****
 * Version: EPL 2.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v20.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2001-2002 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2001-2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004 Thomas E Enebo <enebo@acm.org>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 *
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 ***** END LICENSE BLOCK *****/
package org.truffleruby.parser.ast;

import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.ast.visitor.NodeVisitor;

import java.util.List;

/** Represents an in condition */
public class InParseNode extends ParseNode {
    protected final ParseNode expressionNodes;
    protected final ParseNode bodyNode;
    private final ParseNode nextCase;

    public InParseNode(
            SourceIndexLength position,
            ParseNode expressionNodes,
            ParseNode bodyNode,
            ParseNode nextCase) {
        super(position);

        expressionNodes = ((ArrayParseNode) expressionNodes).get(0);

        this.expressionNodes = expressionNodes;
        this.bodyNode = bodyNode;
        this.nextCase = nextCase;

        assert bodyNode != null : "bodyNode is not null";
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.INNODE;
    }

    /** Accept for the visitor pattern.
     * 
     * @param iVisitor the visitor **/
    @Override
    public <T> T accept(NodeVisitor<T> iVisitor) {
        return iVisitor.visitInNode(this);
    }

    /** Gets the bodyNode.
     * 
     * @return Returns a INode */
    public ParseNode getBodyNode() {
        return bodyNode;
    }

    /** Gets the next case node (if any). */
    public ParseNode getNextCase() {
        return nextCase;
    }

    /** Get the expressionNode(s). */
    public ParseNode getExpressionNodes() {
        return expressionNodes;
    }

    @Override
    public List<ParseNode> childNodes() {
        return ParseNode.createList(expressionNodes, bodyNode, nextCase);
    }
}
