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
 * Copyright (C) 2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2002-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2004 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2004 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2006-2007 Mirko Stocker <me@misto.ch>
 * Copyright (C) 2006 Thomas Corbat <tcorbat@hsr.ch>
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
package org.truffleruby.parser.parser;

import static org.truffleruby.core.rope.CodeRange.CR_BROKEN;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.EUCJPEncoding;
import org.jcodings.specific.SJISEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.regexp.ClassicRegexp;
import org.truffleruby.core.regexp.RegexpOptions;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.RubyDeferredWarnings;
import org.truffleruby.parser.ast.AliasParseNode;
import org.truffleruby.parser.ast.AndParseNode;
import org.truffleruby.parser.ast.ArgsCatParseNode;
import org.truffleruby.parser.ast.ArgsParseNode;
import org.truffleruby.parser.ast.ArgsPushParseNode;
import org.truffleruby.parser.ast.ArgumentParseNode;
import org.truffleruby.parser.ast.ArrayParseNode;
import org.truffleruby.parser.ast.AssignableParseNode;
import org.truffleruby.parser.ast.AttrAssignParseNode;
import org.truffleruby.parser.ast.BackRefParseNode;
import org.truffleruby.parser.ast.BeginParseNode;
import org.truffleruby.parser.ast.BigRationalParseNode;
import org.truffleruby.parser.ast.BignumParseNode;
import org.truffleruby.parser.ast.BinaryOperatorParseNode;
import org.truffleruby.parser.ast.BlockArgParseNode;
import org.truffleruby.parser.ast.BlockParseNode;
import org.truffleruby.parser.ast.BlockPassParseNode;
import org.truffleruby.parser.ast.CallParseNode;
import org.truffleruby.parser.ast.CaseInParseNode;
import org.truffleruby.parser.ast.CaseParseNode;
import org.truffleruby.parser.ast.ClassVarParseNode;
import org.truffleruby.parser.ast.Colon2ConstParseNode;
import org.truffleruby.parser.ast.Colon2ImplicitParseNode;
import org.truffleruby.parser.ast.Colon2ParseNode;
import org.truffleruby.parser.ast.Colon3ParseNode;
import org.truffleruby.parser.ast.ComplexParseNode;
import org.truffleruby.parser.ast.ConstParseNode;
import org.truffleruby.parser.ast.DAsgnParseNode;
import org.truffleruby.parser.ast.DParseNode;
import org.truffleruby.parser.ast.DRegexpParseNode;
import org.truffleruby.parser.ast.DStrParseNode;
import org.truffleruby.parser.ast.DSymbolParseNode;
import org.truffleruby.parser.ast.DefinedParseNode;
import org.truffleruby.parser.ast.DotParseNode;
import org.truffleruby.parser.ast.EvStrParseNode;
import org.truffleruby.parser.ast.FCallParseNode;
import org.truffleruby.parser.ast.FalseParseNode;
import org.truffleruby.parser.ast.FixnumParseNode;
import org.truffleruby.parser.ast.FlipParseNode;
import org.truffleruby.parser.ast.FloatParseNode;
import org.truffleruby.parser.ast.GlobalAsgnParseNode;
import org.truffleruby.parser.ast.GlobalVarParseNode;
import org.truffleruby.parser.ast.HashParseNode;
import org.truffleruby.parser.ast.IArgumentNode;
import org.truffleruby.parser.ast.IfParseNode;
import org.truffleruby.parser.ast.InParseNode;
import org.truffleruby.parser.ast.InstAsgnParseNode;
import org.truffleruby.parser.ast.InstVarParseNode;
import org.truffleruby.parser.ast.KeywordArgParseNode;
import org.truffleruby.parser.ast.KeywordRestArgParseNode;
import org.truffleruby.parser.ast.ListParseNode;
import org.truffleruby.parser.ast.LocalAsgnParseNode;
import org.truffleruby.parser.ast.Match2ParseNode;
import org.truffleruby.parser.ast.Match3ParseNode;
import org.truffleruby.parser.ast.MatchParseNode;
import org.truffleruby.parser.ast.MultipleAsgnParseNode;
import org.truffleruby.parser.ast.NilImplicitParseNode;
import org.truffleruby.parser.ast.NilParseNode;
import org.truffleruby.parser.ast.NthRefParseNode;
import org.truffleruby.parser.ast.NumericParseNode;
import org.truffleruby.parser.ast.OpAsgnConstDeclParseNode;
import org.truffleruby.parser.ast.OpAsgnParseNode;
import org.truffleruby.parser.ast.OpElementAsgnParseNode;
import org.truffleruby.parser.ast.OrParseNode;
import org.truffleruby.parser.ast.ParseNode;
import org.truffleruby.parser.ast.RationalParseNode;
import org.truffleruby.parser.ast.RegexpParseNode;
import org.truffleruby.parser.ast.RescueBodyParseNode;
import org.truffleruby.parser.ast.RescueModParseNode;
import org.truffleruby.parser.ast.RestArgParseNode;
import org.truffleruby.parser.ast.RootParseNode;
import org.truffleruby.parser.ast.SValueParseNode;
import org.truffleruby.parser.ast.SplatParseNode;
import org.truffleruby.parser.ast.StrParseNode;
import org.truffleruby.parser.ast.SuperParseNode;
import org.truffleruby.parser.ast.SymbolParseNode;
import org.truffleruby.parser.ast.TrueParseNode;
import org.truffleruby.parser.ast.UndefParseNode;
import org.truffleruby.parser.ast.WhenOneArgParseNode;
import org.truffleruby.parser.ast.WhenParseNode;
import org.truffleruby.parser.ast.YieldParseNode;
import org.truffleruby.parser.ast.types.ILiteralNode;
import org.truffleruby.parser.ast.types.INameNode;
import org.truffleruby.parser.lexer.LexerSource;
import org.truffleruby.parser.lexer.RubyLexer;
import org.truffleruby.parser.lexer.SyntaxException.PID;
import org.truffleruby.parser.scope.StaticScope;

public class ParserSupport {

    public static final char TEMP_PREFIX = '%';

    public static final String UNNAMED_REST_VAR = prefixName("unnamed_rest");
    public static final String ANONYMOUS_REST_VAR = prefixName("anon_rest");
    public static final String FORWARD_ARGS_REST_VAR = prefixName("forward_rest");
    public static final String FORWARD_ARGS_BLOCK_VAR = prefixName("forward_block");

    private static String prefixName(String name) {
        return (TEMP_PREFIX + name).intern();
    }

    // Parser states:
    protected StaticScope currentScope;

    protected RubyLexer lexer;

    // Is the parser current within a singleton (value is number of nested singletons)
    private int inSingleton;

    // Is the parser currently within a method definition
    private boolean inDefinition;

    // Is the parser currently within a class body.
    private boolean inClass;

    protected ParserConfiguration configuration;
    private RubyParserResult result;

    private final String file;
    private final RubyDeferredWarnings warnings;
    private final ParserRopeOperations parserRopeOperations = new ParserRopeOperations();

    public ParserSupport(LexerSource source, RubyDeferredWarnings warnings) {
        this.file = source.getSourcePath();
        this.warnings = warnings;
    }

    public void reset() {
        inSingleton = 0;
        inDefinition = false;
    }

    public StaticScope getCurrentScope() {
        return currentScope;
    }

    public ParserConfiguration getConfiguration() {
        return configuration;
    }

    public void popCurrentScope() {
        if (!currentScope.isBlockScope()) {
            lexer.getCmdArgumentState().reset(currentScope.getCommandArgumentStack());
        }
        currentScope = currentScope.getEnclosingScope();
    }

    public void pushBlockScope() {
        currentScope = new StaticScope(StaticScope.Type.BLOCK, currentScope, lexer.getFile());
    }

    public void pushLocalScope() {
        currentScope = new StaticScope(StaticScope.Type.LOCAL, currentScope, lexer.getFile());
        currentScope.setCommandArgumentStack(lexer.getCmdArgumentState().getStack());
        lexer.getCmdArgumentState().reset(0);
    }

    public ParseNode arg_concat(SourceIndexLength position, ParseNode node1, ParseNode node2) {
        return node2 == null ? node1 : new ArgsCatParseNode(position, node1, node2);
    }

    public ParseNode arg_blk_pass(ParseNode firstNode, BlockPassParseNode secondNode) {
        if (secondNode != null) {
            secondNode.setArgsNode(firstNode);
            return secondNode;
        }
        return firstNode;
    }

    /** We know for callers of this that it cannot be any of the specials checked in gettable.
     *
     * @param node to check its variable type
     * @return an AST node representing this new variable */
    public ParseNode gettable2(ParseNode node) {
        switch (node.getNodeType()) {
            case DASGNNODE: // LOCALVAR
            case LOCALASGNNODE:
                String name = ((INameNode) node).getName();
                final Rope currentArg = lexer.getCurrentArg();
                if (currentArg != null && name.equals(currentArg.getString())) {
                    warn(node.getPosition(), "circular argument reference - " + name);
                }
                checkDeclarationForNumberedParameterMisuse(name, node);
                return currentScope.declare(node.getPosition(), name);
            case CONSTDECLNODE: // CONSTANT
                return new ConstParseNode(node.getPosition(), ((INameNode) node).getName());
            case INSTASGNNODE: // INSTANCE VARIABLE
                return new InstVarParseNode(node.getPosition(), ((INameNode) node).getName());
            case CLASSVARDECLNODE:
            case CLASSVARASGNNODE:
                return new ClassVarParseNode(node.getPosition(), ((INameNode) node).getName());
            case GLOBALASGNNODE:
                return new GlobalVarParseNode(node.getPosition(), ((INameNode) node).getName());
        }

        getterIdentifierError(node.getPosition(), ((INameNode) node).getName());
        return null;
    }

    private void checkDeclarationForNumberedParameterMisuse(String name, ParseNode node) {
        if (isNumberedParameter(name)) {
            int depth = currentScope.isDefined(name) >> 16;
            if (depth < 0) { // not defined
                SourceIndexLength position = node == null
                        ? lexer.getPosition()
                        : node.getPosition();
                warnNumberedParameterLikeDeclaration(position, name);
            } else if (depth == 0 && currentScope.isBlockScope() &&
                    currentScope.isNumberedBlockScope()) {
                // "real" implicit parameter
                throw compile_error("Can't assign to numbered parameter " + name);
            }
        }
    }

    private void warnNumberedParameterLikeDeclaration(SourceIndexLength position, String name) {
        warn(position, "`" + name + "' is reserved for numbered parameter; consider another name");
    }

    public static boolean isNumberedParameter(String name) {
        return name.length() == 2 && name.charAt(0) == '_' && '1' <= name.charAt(1) && name.charAt(1) <= '9';
    }

    public void checkMethodName(Rope rope) {
        String name = rope.getString();
        if (isNumberedParameter(name)) {
            warnNumberedParameterLikeDeclaration(lexer.getPosition(), name);
        }
    }

    public ParseNode declareIdentifier(Rope rope) {
        return declareIdentifier(rope.getString());
    }

    // Despite the confusing name, called for every identifier use in expressions.
    public ParseNode declareIdentifier(String string) {
        String name = string.intern();
        final Rope currentArg = lexer.getCurrentArg();
        if (currentArg != null && name.equals(currentArg.getString())) {
            warn(lexer.getPosition(), "circular argument reference - " + name);
        }

        if (currentScope.isBlockScope() && isNumberedParameter(name)) {

            int definitionDepth = currentScope.isDefined(name) >> 16;

            // Parameter not defined as a local variable: usable only if the block does not have ordinary parameters.
            if (definitionDepth < 0) { // numbered parameter is not in scope
                if (!currentScope.hasBlockParameters()) {
                    currentScope.addNumberedParameter(name, lexer.getPosition());
                } else {
                    throw compile_error("ordinary parameter is defined");
                }
            }

            // Numbered parameters may not be nested in another block that uses without an intervening scope gate.
            // This is true even if there is a local declaration in between, e.g.
            // "->{ p _1; ->{ _1 = 1; p _1 } }" will fail, even though "->{ p _1; ->{ _1 = 1 } }" will not.

            // We need to check two cases:
            // 1. There was a numbered inner scope, then we try using a numbered parameter in the this (outer) scope.
            if (currentScope.hasNumberedSubScope()) {
                throw compile_error("numbered parameter is already used in inner block");
            }
            // 2. There was a numbered outer scope, then we try using a numbered parameter in the this (inner) scope.
            if (currentScope.hasNumberedSuperScope()) {
                throw compile_error("numbered parameter is already used in outer block");
            }
        }

        return currentScope.declare(lexer.tokline, name);
    }

    // We know it has to be tLABEL or tIDENTIFIER so none of the other assignable logic is needed
    public AssignableParseNode assignableLabelOrIdentifier(Rope name, ParseNode value) {
        return assignableLabelOrIdentifier(name.getString().intern(), value);
    }

    public AssignableParseNode assignableLabelOrIdentifier(String name, ParseNode value) {
        checkDeclarationForNumberedParameterMisuse(name, value);
        return currentScope.assign(lexer.getPosition(), name, makeNullNil(value));
    }

    // We know it has to be tLABEL or tIDENTIFIER so none of the other assignable logic is needed
    public AssignableParseNode assignableKeyword(Rope name, ParseNode value) {
        // JRuby does some extra kwarg tracking when it sees an assignable keyword. We track kwargs in a different
        // manner and thus don't require a special method for it. However, keeping this method in ParserSupport helps
        // reduce the differences with the JRuby grammar.
        return assignableLabelOrIdentifier(name, value);
    }

    protected void getterIdentifierError(SourceIndexLength position, String identifier) {
        lexer.compile_error(PID.BAD_IDENTIFIER, "identifier " + identifier + " is not valid to get");
    }

    /** Wraps node with NEWLINE node.
     *
     * @param node */
    public ParseNode newline_node(ParseNode node, SourceIndexLength position) {
        if (node == null) {
            return null;
        }
        node.setNewline();
        return node;
    }

    // This is the last node made in the AST unintuitively so so post-processing can occur here.
    public ParseNode addRootNode(ParseNode topOfAST) {
        final int endPosition = lexer.getEndPosition();

        SourceIndexLength position;

        if (topOfAST == null) {
            topOfAST = NilImplicitParseNode.NIL;
            position = lexer.getPosition();
        } else {
            position = topOfAST.getPosition();
        }

        BlockParseNode beginAST = null;
        if (!result.getBeginNodes().isEmpty()) {
            position = topOfAST != null ? topOfAST.getPosition() : result.getBeginNodes().get(0).getPosition();
            beginAST = new BlockParseNode(position);
            for (ParseNode beginNode : result.getBeginNodes()) {
                appendToBlock(beginAST, beginNode);
            }
        }

        return new RootParseNode(lexer.getSource(), position, beginAST, topOfAST, endPosition);
    }

    /* MRI: block_append */
    public ParseNode appendToBlock(ParseNode head, ParseNode tail) {
        if (tail == null) {
            return head;
        }
        if (head == null) {
            return tail;
        }

        if (!(head instanceof BlockParseNode)) {
            head = new BlockParseNode(head.getPosition()).add(head);
        }

        if (isBreakStatement(((ListParseNode) head).getLast())) {
            warnings.warning(
                    file,
                    tail.getPosition().toSourceSection(lexer.getSource()).getStartLine(),
                    "statement not reached");
        }

        // Assumption: tail is never a list node
        ((ListParseNode) head).add(tail);
        return head;
    }

    // We know it has to be tLABEL or tIDENTIFIER so none of the other assignable logic is needed
    public AssignableParseNode assignableInCurr(Rope name, ParseNode value) {
        String nameString = name.getString().intern();
        checkDeclarationForNumberedParameterMisuse(nameString, value);
        currentScope.addVariableThisScope(nameString);
        return currentScope.assign(lexer.getPosition(), nameString, makeNullNil(value));
    }

    public ParseNode getOperatorCallNode(ParseNode firstNode, Rope operator) {
        value_expr(lexer, firstNode);

        return new CallParseNode(firstNode.getPosition(), firstNode, operator.getString(), null, null);
    }

    public ParseNode getOperatorCallNode(ParseNode firstNode, Rope operator, ParseNode secondNode) {
        return getOperatorCallNode(firstNode, operator, secondNode, null);
    }

    public ParseNode getOperatorCallNode(ParseNode firstNode, Rope operator, ParseNode secondNode,
            SourceIndexLength defaultPosition) {
        if (defaultPosition != null) {
            firstNode = checkForNilNode(firstNode, defaultPosition);
            secondNode = checkForNilNode(secondNode, defaultPosition);
        }

        value_expr(lexer, firstNode);
        value_expr(lexer, secondNode);

        return new CallParseNode(
                firstNode.getPosition(),
                firstNode,
                operator.getString(),
                new ArrayParseNode(secondNode.getPosition(), secondNode),
                null);
    }

    public ParseNode getMatchNode(ParseNode firstNode, ParseNode secondNode) {
        if (firstNode instanceof DRegexpParseNode) {
            return new Match2ParseNode(firstNode.getPosition(), firstNode, secondNode);
        } else if (firstNode instanceof RegexpParseNode) {
            allocateNamedLocals((RegexpParseNode) firstNode);
            return new Match2ParseNode(firstNode.getPosition(), firstNode, secondNode);
        } else if (secondNode instanceof DRegexpParseNode || secondNode instanceof RegexpParseNode) {
            return new Match3ParseNode(firstNode.getPosition(), firstNode, secondNode);
        }

        return getOperatorCallNode(firstNode, RopeConstants.EQ_TILDE, secondNode);
    }

    /** Define an array set condition so we can return lhs
     *
     * @param receiver array being set
     * @param index node which should evalute to index of array set
     * @return an AttrAssignParseNode */
    public ParseNode aryset(ParseNode receiver, ParseNode index) {
        value_expr(lexer, receiver);

        return new_attrassign(receiver.getPosition(), receiver, "[]=", index, false);
    }

    /** Define an attribute set condition so we can return lhs
     *
     * @param receiver object which contains attribute
     * @param name of the attribute being set
     * @return an AttrAssignParseNode */
    public ParseNode attrset(ParseNode receiver, Rope name) {
        return attrset(receiver, RopeConstants.DOT, name);
    }

    public ParseNode attrset(ParseNode receiver, Rope callType, Rope name) {
        value_expr(lexer, receiver);

        return new_attrassign(receiver.getPosition(), receiver, name.getString() + "=", null, isLazy(callType));
    }

    public void backrefAssignError(ParseNode node) {
        if (node instanceof NthRefParseNode) {
            String varName = "$" + ((NthRefParseNode) node).getMatchNumber();
            lexer.compile_error(PID.INVALID_ASSIGNMENT, "Can't set variable " + varName + '.');
        } else if (node instanceof BackRefParseNode) {
            String varName = "$" + ((BackRefParseNode) node).getType();
            lexer.compile_error(PID.INVALID_ASSIGNMENT, "Can't set variable " + varName + '.');
        }
    }

    public static ParseNode arg_add(SourceIndexLength position, ParseNode node1, ParseNode node2) {
        if (node1 == null) {
            if (node2 == null) {
                return new ArrayParseNode(position, NilImplicitParseNode.NIL);
            } else {
                return new ArrayParseNode(node2.getPosition(), node2);
            }
        }
        if (node1 instanceof ArrayParseNode) {
            return ((ArrayParseNode) node1).add(node2);
        }

        return new ArgsPushParseNode(position, node1, node2);
    }

    /** @fixme position **/
    public ParseNode node_assign(ParseNode lhs, ParseNode rhs) {
        if (lhs == null) {
            return null;
        }

        ParseNode newNode = lhs;

        value_expr(lexer, rhs);
        if (lhs instanceof AssignableParseNode) {
            ((AssignableParseNode) lhs).setValueNode(rhs);
        } else if (lhs instanceof IArgumentNode) {
            IArgumentNode invokableNode = (IArgumentNode) lhs;

            return invokableNode.setArgsNode(arg_add(lhs.getPosition(), invokableNode.getArgsNode(), rhs));
        }

        return newNode;
    }

    public ParseNode ret_args(ParseNode node, SourceIndexLength position) {
        if (node != null) {
            if (node instanceof BlockPassParseNode) {
                lexer.compile_error(PID.BLOCK_ARG_UNEXPECTED, "block argument should not be given");
            } else if (node instanceof ArrayParseNode && ((ArrayParseNode) node).size() == 1) {
                node = ((ArrayParseNode) node).get(0);
            } else if (node instanceof SplatParseNode) {
                node = newSValueNode(position, node);
            }
        }

        if (node == null) {
            node = NilImplicitParseNode.NIL;
        }

        return node;
    }

    /** Is the supplied node a break/control statement?
     *
     * @param node to be checked
     * @return true if a control node, false otherwise */
    public boolean isBreakStatement(ParseNode node) {
        do { // breakLoop:
            if (node == null) {
                return false;
            }

            switch (node.getNodeType()) {
                case BREAKNODE:
                case NEXTNODE:
                case REDONODE:
                case RETRYNODE:
                case RETURNNODE:
                    return true;
                default:
                    return false;
            }
        } while (true);
    }

    public void warnUnlessEOption(ParseNode node, String message) {
        if (!configuration.isInlineSource()) {
            warnings.warn(file, node.getPosition().toSourceSection(lexer.getSource()).getStartLine(), message);
        }
    }

    public static boolean value_expr(RubyLexer lexer, ParseNode node) {
        boolean conditional = false;

        while (node != null) {
            switch (node.getNodeType()) {
                case RETURNNODE:
                case BREAKNODE:
                case NEXTNODE:
                case REDONODE:
                case RETRYNODE:
                    if (!conditional) {
                        lexer.compile_error(PID.VOID_VALUE_EXPRESSION, "void value expression");
                    }

                    return false;
                case BLOCKNODE:
                    node = ((BlockParseNode) node).getLast();
                    break;
                case BEGINNODE:
                    node = ((BeginParseNode) node).getBodyNode();
                    break;
                case IFNODE:
                    if (!value_expr(lexer, ((IfParseNode) node).getThenBody())) {
                        return false;
                    }
                    node = ((IfParseNode) node).getElseBody();
                    break;
                case ANDNODE:
                case ORNODE:
                    conditional = true;
                    node = ((BinaryOperatorParseNode) node).getSecondNode();
                    break;
                default: // ParseNode
                    return true;
            }
        }

        return true;
    }

    public boolean checkExpression(ParseNode node) {
        return value_expr(lexer, node);
    }

    private void handleUselessWarn(ParseNode node, String useless) {
        warnings.warning(
                file,
                node.getPosition().toSourceSection(lexer.getSource()).getStartLine(),
                "Useless use of " + useless + " in void context.");
    }

    /** Check to see if current node is an useless statement. If useless a warning if printed.
     *
     * @param node to be checked. */
    public void checkUselessStatement(ParseNode node) {
        if (!configuration.isInlineSource() && configuration.isEvalParse()) {
            return;
        }

        if (node == null) {
            return;
        }

        switch (node.getNodeType()) {
            case CALLNODE:
                String name = ((CallParseNode) node).getName();
                switch (name) {
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                    case "%":
                    case "**":
                    case "+@":
                    case "-@":
                    case "|":
                    case "^":
                    case "&":
                    case "<=>":
                    case ">":
                    case ">=":
                    case "<":
                    case "<=":
                    case "==":
                    case "!=":
                        handleUselessWarn(node, name);
                        break;
                    default:
                        break;
                }
                break;
            case BACKREFNODE:
            case DVARNODE:
            case GLOBALVARNODE:
            case LOCALVARNODE:
            case NTHREFNODE:
            case CLASSVARNODE:
            case INSTVARNODE:
                handleUselessWarn(node, "a variable");
                break;
            // FIXME: Temporarily disabling because this fires way too much running Rails tests.
            // case CONSTNODE: handleUselessWarn(node, "a constant"); break;
            case BIGNUMNODE:
            case DREGEXPNODE:
            case DSTRNODE:
            case DSYMBOLNODE:
            case FIXNUMNODE:
            case FLOATNODE:
            case REGEXPNODE:
            case STRNODE:
            case SYMBOLNODE:
                handleUselessWarn(node, "a literal");
                break;
            // FIXME: Temporarily disabling because this fires way too much running Rails tests.
            // case CLASSNODE: case COLON2NODE: handleUselessWarn(node, "::"); break;
            case DOTNODE:
                handleUselessWarn(node, ((DotParseNode) node).isExclusive() ? "..." : "..");
                break;
            case DEFINEDNODE:
                handleUselessWarn(node, "defined?");
                break;
            case FALSENODE:
                handleUselessWarn(node, "false");
                break;
            case NILNODE:
                handleUselessWarn(node, "nil");
                break;
            // FIXME: Temporarily disabling because this fires way too much running Rails tests.
            // case SELFNODE: handleUselessWarn(node, "self"); break;
            case TRUENODE:
                handleUselessWarn(node, "true");
                break;
            default:
                break;
        }
    }

    /** Check all nodes but the last one in a BlockParseNode for useless (void context) statements.
     *
     * @param blockNode to be checked. */
    public void checkUselessStatements(BlockParseNode blockNode) {
        ParseNode lastNode = blockNode.getLast();

        for (int i = 0; i < blockNode.size(); i++) {
            ParseNode currentNode = blockNode.get(i);

            if (lastNode != currentNode) {
                checkUselessStatement(currentNode);
            }
        }
    }

    /** assign_in_cond **/
    private boolean checkAssignmentInCondition(ParseNode node) {
        if (node instanceof MultipleAsgnParseNode ||
                node instanceof LocalAsgnParseNode ||
                node instanceof DAsgnParseNode ||
                node instanceof GlobalAsgnParseNode ||
                node instanceof InstAsgnParseNode) {
            ParseNode valueNode = ((AssignableParseNode) node).getValueNode();
            if (isStaticContent(valueNode)) {
                warnings.warn(
                        file,
                        node.getPosition().toSourceSection(lexer.getSource()).getStartLine(),
                        "found = in conditional, should be ==");
            }
            return true;
        }

        return false;
    }

    // Only literals or does it contain something more dynamic like variables?
    private boolean isStaticContent(ParseNode node) {
        if (node instanceof HashParseNode) {
            HashParseNode hash = (HashParseNode) node;
            for (ParseNodeTuple pair : hash.getPairs()) {
                if (!isStaticContent(pair.getKey()) || !isStaticContent(pair.getValue())) {
                    return false;
                }
            }
            return true;
        } else if (node instanceof ArrayParseNode) {
            ArrayParseNode array = (ArrayParseNode) node;
            int size = array.size();

            for (int i = 0; i < size; i++) {
                if (!isStaticContent(array.get(i))) {
                    return false;
                }
            }
            return true;
        } else if (node instanceof FalseParseNode || node instanceof NilParseNode || node instanceof TrueParseNode) {
            return true;
        } else if (node instanceof ILiteralNode) {
            return !(node instanceof DParseNode);
        }

        return false;
    }

    protected ParseNode makeNullNil(ParseNode node) {
        return node == null ? NilImplicitParseNode.NIL : node;
    }

    private ParseNode cond0(ParseNode node) {
        checkAssignmentInCondition(node);

        if (node == null) {
            return new NilParseNode(lexer.getPosition());
        }

        ParseNode leftNode;
        ParseNode rightNode;

        // FIXME: DSTR,EVSTR,STR: warning "string literal in condition"
        switch (node.getNodeType()) {
            case DREGEXPNODE: {
                SourceIndexLength position = node.getPosition();

                return new Match2ParseNode(position, node, new GlobalVarParseNode(position, "$_"));
            }
            case ANDNODE:
                leftNode = cond0(((AndParseNode) node).getFirstNode());
                rightNode = cond0(((AndParseNode) node).getSecondNode());

                return new AndParseNode(node.getPosition(), makeNullNil(leftNode), makeNullNil(rightNode));
            case ORNODE:
                leftNode = cond0(((OrParseNode) node).getFirstNode());
                rightNode = cond0(((OrParseNode) node).getSecondNode());

                return new OrParseNode(node.getPosition(), makeNullNil(leftNode), makeNullNil(rightNode));
            case DOTNODE: {
                DotParseNode dotNode = (DotParseNode) node;
                if (dotNode.isLiteral()) {
                    return node;
                }

                String label = String.valueOf("FLIP" + node.hashCode());
                currentScope.getLocalScope().addVariable(label);
                int slot = currentScope.isDefined(label);

                return new FlipParseNode(
                        node.getPosition(),
                        getFlipConditionNode(((DotParseNode) node).getBeginNode()),
                        getFlipConditionNode(((DotParseNode) node).getEndNode()),
                        dotNode.isExclusive(),
                        slot);
            }
            case REGEXPNODE:
                warnUnlessEOption(node, "regex literal in condition");
                return new MatchParseNode(node.getPosition(), node);
        }

        return node;
    }

    public ParseNode getConditionNode(ParseNode node) {
        ParseNode cond = cond0(node);

        cond.setNewline();

        return cond;
    }

    /* MRI: range_op */
    private ParseNode getFlipConditionNode(ParseNode node) {
        if (!configuration.isInlineSource()) {
            return node;
        }

        node = getConditionNode(node);

        if (node instanceof FixnumParseNode) {
            warnUnlessEOption(node, "integer literal in conditional range");
            return getOperatorCallNode(node, RopeConstants.EQ_EQ, new GlobalVarParseNode(node.getPosition(), "$."));
        }

        return node;
    }

    public SValueParseNode newSValueNode(SourceIndexLength position, ParseNode node) {
        return new SValueParseNode(position, node);
    }

    public SplatParseNode newSplatNode(SourceIndexLength position, ParseNode node) {
        return new SplatParseNode(position, makeNullNil(node));
    }

    public ArrayParseNode newArrayNode(SourceIndexLength position, ParseNode firstNode) {
        return new ArrayParseNode(position, makeNullNil(firstNode));
    }

    public SourceIndexLength position(ParseNode one, ParseNode two) {
        return one == null ? two.getPosition() : one.getPosition();
    }

    public AndParseNode newAndNode(SourceIndexLength position, ParseNode left, ParseNode right) {
        value_expr(lexer, left);

        if (left == null && right == null) {
            return new AndParseNode(position, NilImplicitParseNode.NIL, NilImplicitParseNode.NIL);
        }

        return new AndParseNode(position(left, right), makeNullNil(left), makeNullNil(right));
    }

    public OrParseNode newOrNode(SourceIndexLength position, ParseNode left, ParseNode right) {
        value_expr(lexer, left);

        if (left == null && right == null) {
            return new OrParseNode(position, NilImplicitParseNode.NIL, NilImplicitParseNode.NIL);
        }

        return new OrParseNode(position(left, right), makeNullNil(left), makeNullNil(right));
    }

    /** Ok I admit that this is somewhat ugly. We post-process a chain of when nodes and analyze them to re-insert them
     * back into our new CaseParseNode the way we want. The grammar is being difficult and until I go back into the
     * depths of that this is where things are.
     *
     * @param expression of the case node (e.g. case foo)
     * @param firstWhenNode first when (which could also be the else)
     * @return a new case node */
    public CaseParseNode newCaseNode(SourceIndexLength position, ParseNode expression, ParseNode firstWhenNode) {
        ArrayParseNode cases = new ArrayParseNode(firstWhenNode != null ? firstWhenNode.getPosition() : position);
        CaseParseNode caseNode = new CaseParseNode(position, expression, cases);

        for (ParseNode current = firstWhenNode; current != null; current = ((WhenParseNode) current).getNextCase()) {
            if (current instanceof WhenOneArgParseNode) {
                cases.add(current);
            } else if (current instanceof WhenParseNode) {
                simplifyMultipleArgumentWhenNodes((WhenParseNode) current, cases);
            } else {
                caseNode.setElseNode(current);
                break;
            }
        }

        return caseNode;
    }

    /* This method exists for us to break up multiple expression when nodes (e.g. when 1,2,3:) into individual
     * whenNodes. The primary reason for this is to ensure lazy evaluation of the arguments (when foo,bar,gar:) to
     * prevent side-effects. In the old code this was done using nested when statements, which was awful for interpreter
     * and compilation.
     *
     * Notes: This has semantic equivalence but will not be lexically equivalent. Compiler needs to detect same bodies
     * to simplify bytecode generated. */
    private void simplifyMultipleArgumentWhenNodes(WhenParseNode sourceWhen, ArrayParseNode cases) {
        ParseNode expressionNodes = sourceWhen.getExpressionNodes();

        if (expressionNodes instanceof SplatParseNode || expressionNodes instanceof ArgsCatParseNode) {
            cases.add(sourceWhen);
            return;
        }

        if (expressionNodes instanceof ListParseNode) {
            ListParseNode list = (ListParseNode) expressionNodes;
            SourceIndexLength position = sourceWhen.getPosition();
            ParseNode bodyNode = sourceWhen.getBodyNode();

            for (int i = 0; i < list.size(); i++) {
                ParseNode expression = list.get(i);

                if (expression instanceof SplatParseNode || expression instanceof ArgsCatParseNode) {
                    cases.add(new WhenParseNode(position, expression, bodyNode, null));
                } else {
                    cases.add(new WhenOneArgParseNode(position, expression, bodyNode, null));
                }
            }
        } else {
            cases.add(sourceWhen);
        }
    }

    public WhenParseNode newWhenNode(SourceIndexLength position, ParseNode expressionNodes, ParseNode bodyNode,
            ParseNode nextCase) {
        if (bodyNode == null) {
            bodyNode = NilImplicitParseNode.NIL;
        }

        if (expressionNodes instanceof SplatParseNode || expressionNodes instanceof ArgsCatParseNode ||
                expressionNodes instanceof ArgsPushParseNode) {
            return new WhenParseNode(position, expressionNodes, bodyNode, nextCase);
        }

        ListParseNode list = (ListParseNode) expressionNodes;

        if (list.size() == 1) {
            ParseNode element = list.get(0);

            if (!(element instanceof SplatParseNode)) {
                return new WhenOneArgParseNode(position, element, bodyNode, nextCase);
            }
        }

        return new WhenParseNode(position, expressionNodes, bodyNode, nextCase);
    }

    /** Ok I admit that this is somewhat ugly. We post-process a chain of in nodes and analyze them to re-insert them
     * back into our new CaseInParseNode the way we want. The grammar is being difficult and until I go back into the
     * depths of that this is where things are.
     *
     * @param expression of the case node (e.g. case foo)
     * @param firstInNode first in (which could also be the else)
     * @return a new case node */
    public CaseInParseNode newCaseInNode(SourceIndexLength position, ParseNode expression, ParseNode firstInNode) {
        ArrayParseNode cases = new ArrayParseNode(firstInNode != null ? firstInNode.getPosition() : position);
        CaseInParseNode caseNode = new CaseInParseNode(position, expression, cases);

        for (ParseNode current = firstInNode; current != null; current = ((InParseNode) current).getNextCase()) {
            if (current instanceof InParseNode) {
                cases.add(current);
            } else {
                caseNode.setElseNode(current);
                break;
            }
        }

        return caseNode;
    }

    public InParseNode newInNode(SourceIndexLength position, ParseNode expressionNodes, ParseNode bodyNode,
            ParseNode nextCase) {
        if (bodyNode == null) {
            bodyNode = NilImplicitParseNode.NIL;
        }

        return new InParseNode(position, expressionNodes, bodyNode, nextCase);
    }

    // FIXME: Currently this is passing in position of receiver
    public ParseNode new_opElementAsgnNode(ParseNode receiverNode, Rope operatorName, ParseNode argsNode,
            ParseNode valueNode) {
        SourceIndexLength position = lexer.tokline;  // FIXME: ruby_sourceline in new lexer.

        ParseNode newNode = new OpElementAsgnParseNode(
                position,
                receiverNode,
                operatorName.getString(),
                argsNode,
                valueNode);

        fixpos(newNode, receiverNode);

        return newNode;
    }

    // JRuby would return a RubySymbol but we don't want to create RubySymbols so early, and don't need the Symbol
    public Rope symbolID(Rope identifier) {
        return identifier;
    }

    public ParseNode newOpAsgn(SourceIndexLength position, ParseNode receiverNode, Rope callType, ParseNode valueNode,
            Rope variableName, Rope operatorName) {
        return new OpAsgnParseNode(
                position,
                receiverNode,
                valueNode,
                variableName.getString(),
                operatorName.getString(),
                isLazy(callType));
    }

    public ParseNode newOpConstAsgn(SourceIndexLength position, ParseNode lhs, Rope operatorName, ParseNode rhs) {
        // FIXME: Maybe need to fixup position?
        if (lhs != null) {
            return new OpAsgnConstDeclParseNode(position, lhs, operatorName, rhs);
        } else {
            return new BeginParseNode(position, NilImplicitParseNode.NIL);
        }
    }

    public boolean isLazy(Rope callType) {
        return callType == RopeConstants.AMPERSAND_DOT;
    }

    public ParseNode new_attrassign(SourceIndexLength position, ParseNode receiver, String name, ParseNode args,
            boolean isLazy) {
        return new AttrAssignParseNode(position, receiver, name, args, isLazy);
    }

    public ParseNode new_call(ParseNode receiver, Rope callType, Rope name, ParseNode argsNode, ParseNode iter) {
        if (argsNode instanceof BlockPassParseNode) {
            if (iter != null) {
                lexer.compile_error(PID.BLOCK_ARG_AND_BLOCK_GIVEN, "Both block arg and actual block given.");
            }

            BlockPassParseNode blockPass = (BlockPassParseNode) argsNode;
            return new CallParseNode(
                    position(receiver, argsNode),
                    receiver,
                    name.getString(),
                    blockPass.getArgsNode(),
                    blockPass,
                    isLazy(callType));
        }

        return new CallParseNode(
                position(receiver, argsNode),
                receiver,
                name.getString(),
                argsNode,
                iter,
                isLazy(callType));

    }

    public ParseNode new_call(ParseNode receiver, Rope name, ParseNode argsNode, ParseNode iter) {
        return new_call(receiver, RopeConstants.DOT, name, argsNode, iter);
    }

    public Colon2ParseNode new_colon2(SourceIndexLength position, ParseNode leftNode, Rope name) {
        if (leftNode == null) {
            return new Colon2ImplicitParseNode(position, name);
        }

        return new Colon2ConstParseNode(position, leftNode, name);
    }

    public Colon3ParseNode new_colon3(SourceIndexLength position, Rope name) {
        return new Colon3ParseNode(position, name);
    }

    public void frobnicate_fcall_args(FCallParseNode fcall, ParseNode args, ParseNode iter) {
        if (args instanceof BlockPassParseNode) {
            if (iter != null) {
                lexer.compile_error(PID.BLOCK_ARG_AND_BLOCK_GIVEN, "Both block arg and actual block given.");
            }

            BlockPassParseNode blockPass = (BlockPassParseNode) args;
            args = blockPass.getArgsNode();
            iter = blockPass;
        }

        fcall.setArgsNode(args);
        fcall.setIterNode(iter);
    }

    public void fixpos(ParseNode node, ParseNode orig) {
        if (node == null || orig == null) {
            return;
        }

        node.setPosition(orig.getPosition());
    }

    public ParseNode new_fcall(Rope operation) {
        return new FCallParseNode(lexer.tokline, operation.getString());
    }

    public ParseNode new_super(SourceIndexLength position, ParseNode args) {
        if (args != null && args instanceof BlockPassParseNode) {
            return new SuperParseNode(position, ((BlockPassParseNode) args).getArgsNode(), args);
        }
        return new SuperParseNode(position, args);
    }

    /** Description of the RubyMethod */
    public void initTopLocalVariables() {
        currentScope = configuration.getScope(lexer.getFile());
    }

    /** Getter for property inSingle.
     *
     * @return Value of property inSingle. */
    public boolean isInSingle() {
        return inSingleton != 0;
    }

    /** Setter for property inSingle.
     *
     * @param inSingle New value of property inSingle. */
    public void setInSingle(int inSingle) {
        this.inSingleton = inSingle;
    }

    public boolean isInDef() {
        return inDefinition;
    }

    public void setInDef(boolean inDef) {
        this.inDefinition = inDef;
    }

    public boolean isInClass() {
        return inClass;
    }

    public void setIsInClass(boolean inClass) {
        this.inClass = inClass;
    }

    public void enterBlockParameters() {
        currentScope.setHasBlockParameters();
    }

    /** Getter for property inSingle.
     *
     * @return Value of property inSingle. */
    public int getInSingle() {
        return inSingleton;
    }

    /** Gets the result.
     *
     * @return Returns a RubyParserResult */
    public RubyParserResult getResult() {
        return result;
    }

    /** Sets the result.
     *
     * @param result The result to set */
    public void setResult(RubyParserResult result) {
        this.result = result;
    }

    /** Sets the configuration.
     *
     * @param configuration The configuration to set */
    public void setConfiguration(ParserConfiguration configuration) {
        this.configuration = configuration;

    }

    public void setLexer(RubyLexer lexer) {
        this.lexer = lexer;
    }

    public DStrParseNode createDStrNode(SourceIndexLength position) {
        return new DStrParseNode(position, lexer.getEncoding());
    }

    public ParseNodeTuple createKeyValue(ParseNode key, ParseNode value) {
        if (key != null && key instanceof StrParseNode) {
            ((StrParseNode) key).setFrozen(true);
        }

        return new ParseNodeTuple(key, value);
    }

    public ParseNode asSymbol(SourceIndexLength position, String value) {
        final SymbolParseNode symbolParseNode = new SymbolParseNode(
                position,
                value,
                lexer.getEncoding(),
                lexer.getTokenCR());
        checkSymbolCodeRange(symbolParseNode);
        return symbolParseNode;
    }

    public ParseNode asSymbol(SourceIndexLength position, Rope value) {
        final SymbolParseNode symbolParseNode = new SymbolParseNode(position, value);
        checkSymbolCodeRange(symbolParseNode);
        return symbolParseNode;
    }

    public ParseNode asSymbol(SourceIndexLength position, ParseNode value) {
        final ParseNode parseNode;
        if (value instanceof StrParseNode) {
            final SymbolParseNode symbolParseNode = new SymbolParseNode(position, ((StrParseNode) value).getValue());
            checkSymbolCodeRange(symbolParseNode);
            parseNode = symbolParseNode;
        } else {
            parseNode = new DSymbolParseNode(position, (DStrParseNode) value);
        }
        return parseNode;
    }

    private void checkSymbolCodeRange(SymbolParseNode symbolParseNode) {
        if (symbolParseNode.getRope().getCodeRange() == CR_BROKEN) {
            throw new RaiseException(
                    RubyLanguage.getCurrentContext(),
                    getConfiguration().getContext().getCoreExceptions().encodingError("invalid encoding symbol", null));
        }
    }

    public ParseNode literal_concat(ParseNode head, ParseNode tail) {
        if (head == null) {
            return tail;
        }
        if (tail == null) {
            return head;
        }

        if (head instanceof EvStrParseNode) {
            head = createDStrNode(head.getPosition()).add(head);
        }

        if (lexer.getHeredocIndent() > 0) {
            if (head instanceof StrParseNode) {
                head = createDStrNode(head.getPosition()).add(head);
                return list_append(head, tail);
            } else if (head instanceof DStrParseNode) {
                return list_append(head, tail);
            }
        }

        if (tail instanceof StrParseNode) {
            if (head instanceof StrParseNode) {
                StrParseNode front = (StrParseNode) head;
                // string_contents always makes an empty strnode...which is sometimes valid but
                // never if it ever is in literal_concat.
                if (front.getValue().byteLength() > 0) {
                    return new StrParseNode(head.getPosition(), front, (StrParseNode) tail);
                } else {
                    return tail;
                }
            }
            head.setPosition(head.getPosition());
            return ((ListParseNode) head).add(tail);

        } else if (tail instanceof DStrParseNode) {
            if (head instanceof StrParseNode) { // Str + oDStr -> Dstr(Str, oDStr.contents)
                DStrParseNode newDStr = new DStrParseNode(head.getPosition(), ((DStrParseNode) tail).getEncoding());
                newDStr.add(head);
                newDStr.add(tail);
                return newDStr;
            }

            return ((ListParseNode) head).add(tail);
        }

        // tail must be EvStrParseNode at this point
        if (head instanceof StrParseNode) {

            //Do not add an empty string node
            if (((StrParseNode) head).getValue().byteLength() == 0) {
                head = createDStrNode(head.getPosition());
            } else {
                head = createDStrNode(head.getPosition()).add(head);
            }
        }
        return ((DStrParseNode) head).add(tail);
    }

    public ParseNode newRescueModNode(ParseNode body, ParseNode rescueBody) {
        if (rescueBody == null) {
            rescueBody = NilImplicitParseNode.NIL; // foo rescue () can make null.
        }
        SourceIndexLength pos = getPosition(body);

        return new RescueModParseNode(pos, body, new RescueBodyParseNode(pos, null, rescueBody, null));
    }

    public ParseNode newEvStrNode(SourceIndexLength position, ParseNode node) {
        if (node instanceof StrParseNode || node instanceof DStrParseNode || node instanceof EvStrParseNode) {
            return node;
        }

        return new EvStrParseNode(position, node);
    }

    public ParseNode new_yield(SourceIndexLength position, ParseNode node) {
        if (node != null && node instanceof BlockPassParseNode) {
            lexer.compile_error(PID.BLOCK_ARG_UNEXPECTED, "Block argument should not be given.");
        }

        return new YieldParseNode(position, node);
    }

    public NumericParseNode negateInteger(NumericParseNode integerNode) {
        if (integerNode instanceof FixnumParseNode) {
            FixnumParseNode fixnumNode = (FixnumParseNode) integerNode;

            fixnumNode.setValue(-fixnumNode.getValue());
            return fixnumNode;
        } else if (integerNode instanceof BignumParseNode) {
            BignumParseNode bignumNode = (BignumParseNode) integerNode;

            BigInteger value = bignumNode.getValue().negate();

            // Negating a bignum will make the last negative value of our bignum
            if (value.compareTo(LONG_MIN) >= 0) {
                return new FixnumParseNode(bignumNode.getPosition(), value.longValue());
            }

            bignumNode.setValue(value);
        }

        return integerNode;
    }

    private static final int BIT_SIZE = 64;
    private static final long MAX = (1L << (BIT_SIZE - 1)) - 1;
    public static final BigInteger LONG_MIN = BigInteger.valueOf(-MAX - 1);

    public FloatParseNode negateFloat(FloatParseNode floatNode) {
        floatNode.setValue(-floatNode.getValue());

        return floatNode;
    }

    public ComplexParseNode negateComplexNode(ComplexParseNode complexNode) {
        complexNode.setNumber(negateNumeric(complexNode.getNumber()));

        return complexNode;
    }

    public RationalParseNode negateRational(RationalParseNode rationalNode) {
        return new RationalParseNode(
                rationalNode.getPosition(),
                -rationalNode.getNumerator(),
                rationalNode.getDenominator());
    }

    public BigRationalParseNode negateBigRational(BigRationalParseNode rationalNode) {
        return new BigRationalParseNode(
                rationalNode.getPosition(),
                rationalNode.getNumerator().negate(),
                rationalNode.getDenominator());
    }

    private ParseNode checkForNilNode(ParseNode node, SourceIndexLength defaultPosition) {
        return (node == null) ? new NilParseNode(defaultPosition) : node;
    }

    public ParseNode new_args(SourceIndexLength position, ListParseNode pre, ListParseNode optional,
            RestArgParseNode rest, ListParseNode post, ArgsTailHolder tail) {
        ArgsParseNode argsNode;
        if (tail == null) {
            argsNode = new ArgsParseNode(position, pre, optional, rest, post, null);
        } else {
            argsNode = new ArgsParseNode(
                    position,
                    pre,
                    optional,
                    rest,
                    post,
                    tail.getKeywordArgs(),
                    tail.getKeywordRestArgNode(),
                    tail.getBlockArg());
        }
        currentScope.setArgsParseNode(argsNode);
        return argsNode;
    }

    public ArgsTailHolder new_args_tail(SourceIndexLength position, ListParseNode keywordArg,
            Rope keywordRestArgNameRope, BlockArgParseNode blockArg) {
        if (keywordRestArgNameRope == null) {
            return new ArgsTailHolder(position, keywordArg, null, blockArg);
        } else if (keywordRestArgNameRope == RubyLexer.Keyword.NIL.bytes) { // def m(**nil)
            // TODO (eregon, 6 Nov 2020): actually implement **nil semantics
            return new ArgsTailHolder(position, keywordArg, null, blockArg);
        }

        final String restKwargsName;
        if (keywordRestArgNameRope.isEmpty()) {
            restKwargsName = TEMP_PREFIX + "_kwrest";
        } else {
            restKwargsName = keywordRestArgNameRope.getString().intern();
        }

        int slot = currentScope.exists(restKwargsName);
        if (slot == -1) {
            slot = currentScope.addVariable(restKwargsName);
        }

        KeywordRestArgParseNode keywordRestArg = new KeywordRestArgParseNode(position, restKwargsName, slot);

        return new ArgsTailHolder(position, keywordArg, keywordRestArg, blockArg);
    }

    public ParseNode remove_duplicate_keys(HashParseNode hash) {
        List<ParseNode> encounteredKeys = new ArrayList<>();
        visitPairsRecursive(hash, encounteredKeys);
        return hash;
    }

    private void visitPairsRecursive(HashParseNode hash, List<ParseNode> encounteredKeys) {
        for (ParseNodeTuple pair : hash.getPairs()) {
            ParseNode key = pair.getKey();
            if (key == null) {
                if (pair.getValue() instanceof HashParseNode) {
                    visitPairsRecursive((HashParseNode) pair.getValue(), encounteredKeys);
                }
                continue;
            }
            int index = matchesExistingIndex(key, encounteredKeys);
            if (index >= 0) {
                encounteredKeys.set(index, key);
                warn(hash.getPosition(), "key " + keyToString(key) +
                        " is duplicated and overwritten on line " +
                        (encounteredKeys.get(index).getPosition().toSourceSection(lexer.getSource()).getStartLine()));
            } else {
                encounteredKeys.add(key);
            }
        }
    }

    private String keyToString(ParseNode node) {
        if (node instanceof SymbolParseNode) {
            return ":" + ((SymbolParseNode) node).getName();
        } else {
            return node.toString();
        }
    }

    private int matchesExistingIndex(ParseNode currentNode, List<ParseNode> encounteredKeys) {
        for (int i = 0; i < encounteredKeys.size(); i++) {
            final ParseNode parseNode = encounteredKeys.get(i);
            // TODO BJF 27-Nov-17 Handle additional literal nodes, consider interface with valueEquals
            if (parseNode instanceof SymbolParseNode && currentNode instanceof SymbolParseNode) {
                if (((SymbolParseNode) parseNode).getRope().equals(((SymbolParseNode) currentNode).getRope())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public ParseNode newAlias(SourceIndexLength position, ParseNode newNode, ParseNode oldNode) {
        return new AliasParseNode(position, newNode, oldNode);
    }

    public ParseNode newUndef(SourceIndexLength position, ParseNode nameNode) {
        return new UndefParseNode(position, nameNode);
    }

    /** generate parsing error */
    public void yyerror(String message) {
        lexer.compile_error(PID.GRAMMAR_ERROR, message);
    }

    /** generate parsing error
     *
     * @param message text to be displayed.
     * @param expected list of acceptable tokens, if available. */
    public void yyerror(String message, String[] expected, String found) {
        lexer.compile_error(PID.GRAMMAR_ERROR, message + ", unexpected " + found);
    }

    public SourceIndexLength getPosition(ParseNode start) {
        if (start != null) {
            return start.getPosition();
        } else {
            return lexer.getPosition();
        }
    }

    public void warn(SourceIndexLength position, String message) {
        warnings.warn(file, position.toSourceSection(lexer.getSource()).getStartLine(), message);
    }

    public void warning(SourceIndexLength position, String message) {
        warnings.warning(file, position.toSourceSection(lexer.getSource()).getStartLine(), message);
    }

    // ENEBO: Totally weird naming (in MRI is not allocated and is a local var name) [1.9]
    public boolean is_local_id(Rope name) {
        return lexer.isIdentifierChar(name.get(0) & 0xFF);
    }

    // 1.9
    public ListParseNode list_append(ParseNode list, ParseNode item) {
        if (list == null) {
            return new ArrayParseNode(item.getPosition(), item);
        }
        if (!(list instanceof ListParseNode)) {
            return new ArrayParseNode(list.getPosition(), list).add(item);
        }

        return ((ListParseNode) list).add(item);
    }

    // 1.9
    public ParseNode new_bv(Rope identifier) {
        if (!is_local_id(identifier)) {
            getterIdentifierError(lexer.getPosition(), identifier.getString());
        }
        shadowing_lvar(identifier);

        return arg_var(identifier);
    }

    // 1.9
    @SuppressFBWarnings("ES")
    public ArgumentParseNode arg_var(Rope rope) {
        return arg_var(rope.getString());
    }

    // Called with parameter names
    @SuppressFBWarnings("ES")
    public ArgumentParseNode arg_var(String string) {
        String name = string.intern();
        StaticScope current = getCurrentScope();

        // Multiple _ arguments are allowed.  To not screw with tons of arity
        // issues in our runtime we will allocate unnamed bogus vars so things
        // still work. MRI does not use name as intern'd value so they don't
        // have this issue.
        if (name == "_") {
            int count = 0;
            while (current.exists(name) >= 0) {
                name = ("_$" + count++).intern();
            }
        }

        if (isNumberedParameter(name)) {
            warnNumberedParameterLikeDeclaration(lexer.getPosition(), name);
        }

        return new ArgumentParseNode(lexer.getPosition(), name, current.addVariableThisScope(name));
    }

    public Rope formal_argument(Rope identifier) {
        lexer.validateFormalIdentifier(identifier);

        return shadowing_lvar(identifier);
    }

    // 1.9
    @SuppressFBWarnings("ES")
    public Rope shadowing_lvar(Rope rope) {
        String name = rope.getString().intern();
        if (name == "_") {
            return rope;
        }

        StaticScope current = getCurrentScope();
        if (current.exists(name) >= 0) {
            yyerror("duplicated argument name");
        }

        return rope;
    }

    // 1.9
    public ListParseNode list_concat(ParseNode first, ParseNode second) {
        if (first instanceof ListParseNode) {
            if (second instanceof ListParseNode) {
                return ((ListParseNode) first).addAll((ListParseNode) second);
            } else {
                return ((ListParseNode) first).add(second);
            }
        }

        return new ArrayParseNode(first.getPosition(), first).add(second);
    }

    // 1.9
    /** If node is a splat and it is splatting a literal array then return the literal array. Otherwise return null.
     * This allows grammar to not splat into a Ruby Array if splatting a literal array. */
    public ParseNode splat_array(ParseNode node) {
        if (node instanceof SplatParseNode) {
            node = ((SplatParseNode) node).getValue();
        }
        if (node instanceof ArrayParseNode) {
            return node;
        }
        return null;
    }

    // 1.9
    public ParseNode arg_append(ParseNode node1, ParseNode node2) {
        if (node1 == null) {
            return new ArrayParseNode(node2.getPosition(), node2);
        }
        if (node1 instanceof ListParseNode) {
            return ((ListParseNode) node1).add(node2);
        }
        if (node1 instanceof BlockPassParseNode) {
            return arg_append(((BlockPassParseNode) node1).getBodyNode(), node2);
        }
        if (node1 instanceof ArgsPushParseNode) {
            ArgsPushParseNode pushNode = (ArgsPushParseNode) node1;
            ParseNode body = pushNode.getSecondNode();

            return new ArgsCatParseNode(
                    pushNode.getPosition(),
                    pushNode.getFirstNode(),
                    new ArrayParseNode(body.getPosition(), body).add(node2));
        }

        return new ArgsPushParseNode(position(node1, node2), node1, node2);
    }

    // MRI: reg_fragment_check
    public Rope regexpFragmentCheck(RegexpParseNode end, Rope value) {
        value = setRegexpEncoding(end, value);
        try {
            ClassicRegexp.preprocessCheck(configuration.getContext(), value);
        } catch (RaiseException re) {
            throw compile_error(re.getMessage());
        }
        return value;
    }

    private void allocateNamedLocals(RegexpParseNode regexpNode) {
        ClassicRegexp pattern = new ClassicRegexp(
                configuration.getContext(),
                regexpNode.getValue(),
                regexpNode.getOptions());
        pattern.setLiteral();
        String[] names = pattern.getNames();
        int length = names.length;
        StaticScope scope = getCurrentScope();

        for (int i = 0; i < length; i++) {
            // TODO: Pass by non-local-varnamed things but make sure consistent with list we get from regexp
            if (RubyLexer.getKeyword(names[i]) == null && !Character.isUpperCase(names[i].charAt(0))) {
                int slot = scope.isDefined(names[i]);
                if (slot >= 0) {
                    // If verbose and the variable is not just another named capture, warn
                    if (!scope.isNamedCapture(slot)) {
                        warn(getPosition(regexpNode), "named capture conflicts a local variable - " + names[i]);
                    }
                } else {
                    getCurrentScope().addNamedCaptureVariable(names[i]);
                }
            }
        }
    }

    private boolean is7BitASCII(Rope value) {
        return value.isAsciiOnly();
    }

    // TODO: Put somewhere more consolidated (similar)
    private char optionsEncodingChar(Encoding optionEncoding) {
        if (optionEncoding == USASCIIEncoding.INSTANCE) {
            return 'n';
        }
        if (optionEncoding == EUCJPEncoding.INSTANCE) {
            return 'e';
        }
        if (optionEncoding == SJISEncoding.INSTANCE) {
            return 's';
        }
        if (optionEncoding == UTF8Encoding.INSTANCE) {
            return 'u';
        }

        return ' ';
    }

    public RuntimeException compile_error(String message) { // mri: rb_compile_error_with_enc
        String line = lexer.getCurrentLine();
        SourceIndexLength position = lexer.getPosition();
        String errorMessage = lexer.getFile() + ":" + position.toSourceSection(lexer.getSource()).getStartLine() + ": ";

        if (line.length() > 5) {
            boolean addNewline = message != null && !message.endsWith("\n");
            message += (addNewline ? "\n" : "") + line;
        }

        throw new RaiseException(
                RubyLanguage.getCurrentContext(),
                getConfiguration().getContext().getCoreExceptions().syntaxError(
                        errorMessage + message,
                        null,
                        position.toSourceSection(lexer.getSource())));
    }

    protected void compileError(Encoding optionEncoding, Encoding encoding) {
        lexer.compile_error(
                PID.REGEXP_ENCODING_MISMATCH,
                "regexp encoding option '" + optionsEncodingChar(optionEncoding) +
                        "' differs from source encoding '" + encoding + "'");
    }

    public Encoding getEncoding(Rope name) {
        return EncodingManager.getEncoding(name);
    }

    // MRI: reg_fragment_setenc_gen
    public Rope setRegexpEncoding(RegexpParseNode end, Rope value) {
        RegexpOptions options = end.getOptions();
        Encoding optionsEncoding = options.setup();

        // Change encoding to one specified by regexp options as long as the string is compatible.
        if (optionsEncoding != null) {
            if (optionsEncoding != value.getEncoding() && !is7BitASCII(value)) {
                compileError(optionsEncoding, value.getEncoding());
            }

            value = parserRopeOperations.withEncoding(value, optionsEncoding);
        } else if (options.isEncodingNone()) {
            if (value.getEncoding() == ASCIIEncoding.INSTANCE && !is7BitASCII(value)) {
                compileError(null, value.getEncoding());
            }
            value = parserRopeOperations.withEncoding(value, ASCIIEncoding.INSTANCE);
        } else if (lexer.getEncoding() == USASCIIEncoding.INSTANCE) {
            if (!is7BitASCII(value)) {
                value = parserRopeOperations.withEncoding(value, USASCIIEncoding.INSTANCE); // This will raise later
            } else {
                value = parserRopeOperations.withEncoding(value, ASCIIEncoding.INSTANCE);
            }
        }
        return value;
    }

    protected ClassicRegexp checkRegexpSyntax(Rope value, RegexpOptions options) {
        try {
            // This is only for syntax checking but this will as a side-effect create an entry in the regexp cache.
            return new ClassicRegexp(getConfiguration().getContext(), value, options);
        } catch (RaiseException re) {
            throw compile_error(re.getMessage());
        }
    }

    public ParseNode newRegexpNode(SourceIndexLength position, ParseNode contents, RegexpParseNode end) {
        RegexpOptions options = end.getOptions();
        Encoding encoding = lexer.getEncoding();

        if (contents == null) {
            Rope newValue = RopeConstants.EMPTY_US_ASCII_ROPE;
            if (encoding != null) {
                newValue = parserRopeOperations.withEncoding(newValue, encoding);
            }

            newValue = regexpFragmentCheck(end, newValue);
            return new RegexpParseNode(position, newValue, options.withoutOnce());
        } else if (contents instanceof StrParseNode) {
            Rope meat = ((StrParseNode) contents).getValue();
            meat = regexpFragmentCheck(end, meat);
            checkRegexpSyntax(meat, options.withoutOnce());
            return new RegexpParseNode(contents.getPosition(), meat, options.withoutOnce());
        } else if (contents instanceof DStrParseNode) {
            DStrParseNode dStrNode = (DStrParseNode) contents;

            for (int i = 0; i < dStrNode.size(); i++) {
                ParseNode fragment = dStrNode.get(i);
                if (fragment instanceof StrParseNode) {
                    Rope frag = ((StrParseNode) fragment).getValue();
                    regexpFragmentCheck(end, frag);
                }
            }

            DRegexpParseNode dRegexpNode = new DRegexpParseNode(position, options, encoding);
            dRegexpNode.add(new StrParseNode(contents.getPosition(), createMaster(options)));
            dRegexpNode.addAll(dStrNode);
            return dRegexpNode;
        }

        // EvStrParseNode: #{val}: no fragment check, but at least set encoding
        Rope master = createMaster(options);
        master = regexpFragmentCheck(end, master);
        encoding = master.getEncoding();
        DRegexpParseNode node = new DRegexpParseNode(position, options, encoding);
        node.add(new StrParseNode(contents.getPosition(), master));
        node.add(contents);
        return node;
    }

    // Create the magical empty 'master' string which will be encoded with
    // regexp options encoding so dregexps can end up starting with the
    // right encoding.
    private Rope createMaster(RegexpOptions options) {
        final Encoding encoding = options.setup();
        return RopeOperations.emptyRope(encoding == null ? ASCIIEncoding.INSTANCE : encoding);
    }

    public KeywordArgParseNode keyword_arg(SourceIndexLength position, AssignableParseNode assignable) {
        return new KeywordArgParseNode(position, assignable);
    }

    public NumericParseNode negateNumeric(NumericParseNode node) {
        switch (node.getNodeType()) {
            case FIXNUMNODE:
            case BIGNUMNODE:
                return negateInteger(node);
            case COMPLEXNODE:
                return negateComplexNode((ComplexParseNode) node);
            case FLOATNODE:
                return negateFloat((FloatParseNode) node);
            case RATIONALNODE:
                return negateRational((RationalParseNode) node);
            case BIGRATIONALNODE:
                return negateBigRational((BigRationalParseNode) node);
        }

        yyerror("Invalid or unimplemented numeric to negate: " + node.toString());
        return null;
    }

    public ParseNode new_defined(SourceIndexLength position, ParseNode something) {
        return new DefinedParseNode(position, makeNullNil(something));
    }

    public static final Rope INTERNAL_ID = RopeConstants.EMPTY_US_ASCII_ROPE;

    public SourceIndexLength extendedUntil(SourceIndexLength start, SourceIndexLength end) {
        return new SourceIndexLength(start.getCharIndex(), end.getCharEnd() - start.getCharIndex());
    }

}
