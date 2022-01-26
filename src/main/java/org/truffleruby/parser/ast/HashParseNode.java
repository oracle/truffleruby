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
 * Copyright (C) 2002 Anders Bengtsson <ndrsbngtssn@yahoo.se>
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
 ***** END LICENSE BLOCK *****/
package org.truffleruby.parser.ast;

import java.util.ArrayList;
import java.util.List;

import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.ast.types.ILiteralNode;
import org.truffleruby.parser.ast.visitor.NodeVisitor;
import org.truffleruby.parser.parser.ParseNodeTuple;

/** A Literal Hash that can represent either a {a=&amp;b, c=&amp;d} type expression or keyword arguments passed in a
 * method call (foo(k: value), foo(**kw) or a mix of both). */
public class HashParseNode extends ParseNode implements ILiteralNode {
    private final List<ParseNodeTuple> pairs;

    /** Does this hash parse node represent formal keyword arguments? */
    private boolean keywordArguments = false;

    public HashParseNode(SourceIndexLength position) {
        super(position);

        pairs = new ArrayList<>();
    }

    public HashParseNode(SourceIndexLength position, ParseNodeTuple pair) {
        this(position);

        pairs.add(pair);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.HASHNODE;
    }

    public HashParseNode add(ParseNodeTuple pair) {
        pairs.add(pair);

        return this;
    }

    /** Accept for the visitor pattern.
     * 
     * @param iVisitor the visitor **/
    @Override
    public <T> T accept(NodeVisitor<T> iVisitor) {
        return iVisitor.visitHashNode(this);
    }

    public boolean isEmpty() {
        return pairs.isEmpty();
    }

    public List<ParseNodeTuple> getPairs() {
        return pairs;
    }

    @Override
    public List<ParseNode> childNodes() {
        List<ParseNode> children = new ArrayList<>();

        for (ParseNodeTuple pair : pairs) {
            children.add(pair.getKey());
            children.add(pair.getValue());
        }

        return children;
    }

    public boolean isKeywordArguments() {
        return keywordArguments;
    }

    public void setKeywordArguments(boolean keywordArguments) {
        this.keywordArguments = keywordArguments;
    }
}
