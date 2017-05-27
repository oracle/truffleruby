package org.truffleruby.parser.ast;

import org.truffleruby.language.SourceIndexLength;

/**
 * f rescue nil
 */
public class RescueModParseNode extends RescueParseNode {
    public RescueModParseNode(SourceIndexLength position, ParseNode bodyNode, RescueBodyParseNode rescueNode) {
        super(position, bodyNode, rescueNode, null /* else */);
    }
}
