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
 * Copyright (C) 2006 Thomas E Enebo <enebo@acm.org>
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

import java.util.List;

import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.ast.visitor.NodeVisitor;

import com.oracle.truffle.api.source.Source;

/** Represents the top of the AST. This is a node not present in MRI. It was created to hold the top-most static scope
 * in an easy to grab way and it also exists to hold BEGIN and END nodes. These can then be interpreted/compiled in the
 * same places as the rest of the code. */
public final class RootParseNode extends ParseNode {

    private final ParseNode beginNode;
    private final Source source;
    private final ParseNode bodyNode;
    private final int endPosition;

    public RootParseNode(
            Source source,
            SourceIndexLength position,
            ParseNode beginNode,
            ParseNode bodyNode,
            int endPosition) {
        super(position);
        this.source = source;
        this.beginNode = beginNode;
        this.bodyNode = bodyNode;
        this.endPosition = endPosition;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ROOTNODE;
    }

    public Source getSource() {
        return source;
    }

    public ParseNode getBeginNode() {
        return beginNode;
    }

    /** First real AST node to be interpreted
     *
     * @return real top AST node */
    public ParseNode getBodyNode() {
        return bodyNode;
    }

    @Override
    public <T> T accept(NodeVisitor<T> iVisitor) {
        return iVisitor.visitRootNode(this);
    }

    @Override
    public List<ParseNode> childNodes() {
        return createList(bodyNode);
    }

    public boolean hasEndPosition() {
        return endPosition != -1;
    }

    public int getEndPosition() {
        return endPosition;
    }

}
