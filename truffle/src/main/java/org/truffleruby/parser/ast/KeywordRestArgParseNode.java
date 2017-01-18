package org.truffleruby.parser.ast;

import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.ast.visitor.NodeVisitor;

/**
 *
 */
public class KeywordRestArgParseNode extends ArgumentParseNode {
    public KeywordRestArgParseNode(SourceIndexLength position, String name, int index) {
        super(position, name, index);
    }
    
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitKeywordRestArgNode(this);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.KEYWORDRESTARGNODE;
    }
}
