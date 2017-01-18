/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.truffleruby.parser.ast;

import org.truffleruby.language.SourceIndexLength;

/**
 *
 * @author enebo
 */
public class WhenOneArgParseNode extends WhenParseNode {
    public WhenOneArgParseNode(SourceIndexLength position, ParseNode expressionNode, ParseNode bodyNode, ParseNode nextCase) {
        super(position, expressionNode, bodyNode, nextCase);
    }
}
