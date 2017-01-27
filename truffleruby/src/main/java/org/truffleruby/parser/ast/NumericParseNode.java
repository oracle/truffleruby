package org.truffleruby.parser.ast;

import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.ast.types.ILiteralNode;

/**
 * Any node representing a numeric value.
 */
public abstract class NumericParseNode extends ParseNode implements ILiteralNode {
    public NumericParseNode(SourceIndexLength position) {
        super(position, false);
    }
}
