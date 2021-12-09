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
 * Copyright (C) 2001 Chad Fowler <chadfowler@chadfowler.com>
 * Copyright (C) 2001-2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2001-2002 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2007 Mirko Stocker <me@misto.ch>
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

import java.util.Arrays;
import java.util.List;

import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.parser.Helpers;
import org.truffleruby.parser.ast.types.INameNode;
import org.truffleruby.parser.ast.visitor.NodeVisitor;
import org.truffleruby.parser.parser.ParserSupport;
import org.truffleruby.parser.scope.StaticScope;

/** Represents the argument declarations of a method. The fields: foo(p1, ..., pn, o1 = v1, ..., on = v2, *r, q1, ...,
 * qn, k1:, ..., kn:, **K, &b)
 *
 * p1...pn = pre arguments o1...on = optional arguments r = rest argument q1...qn = post arguments (only in 1.9) k1...kn
 * = keyword arguments K = keyword rest argument b = block arg */
public class ArgsParseNode extends ParseNode {

    private ParseNode[] args;
    private short optIndex;
    private short postIndex;
    private short keywordsIndex;

    /** Whether numbered parameters were used in the body (and therefore this has no other formal parameters, or a
     * SyntaxError would have resulted. Can change as numbered parameters are encountered. */
    private boolean isNumbered = false;

    protected final ArgumentParseNode restArgNode;
    private final KeywordRestArgParseNode keyRest;
    private final BlockArgParseNode blockArgNode;

    private Arity arity;

    private static final ParseNode[] NO_ARGS = ParseNode.EMPTY_ARRAY;

    /** Construct a new ArgsParseNode with no keyword arguments. */
    public ArgsParseNode(
            SourceIndexLength position,
            ListParseNode pre,
            ListParseNode optionalArguments,
            RestArgParseNode rest,
            ListParseNode post,
            BlockArgParseNode blockArgNode) {
        this(position, pre, optionalArguments, rest, post, null, null, blockArgNode);
    }

    /** Construct a new ArgsParseNode with keyword arguments. */
    public ArgsParseNode(
            SourceIndexLength position,
            ListParseNode pre,
            ListParseNode optionalArguments,
            RestArgParseNode rest,
            ListParseNode post,
            ListParseNode keywords,
            KeywordRestArgParseNode keyRest,
            BlockArgParseNode blockArgNode) {
        super(position);

        int preSize = pre != null ? pre.size() : 0;
        int optSize = optionalArguments != null ? optionalArguments.size() : 0;
        int postSize = post != null ? post.size() : 0;
        int keywordsSize = keywords != null ? keywords.size() : 0;
        int size = preSize + optSize + postSize + keywordsSize;

        args = size > 0 ? new ParseNode[size] : NO_ARGS;
        optIndex = (short) preSize;
        postIndex = (short) (optSize != 0 ? optIndex + optSize : optIndex);
        keywordsIndex = (short) (postSize != 0 ? postIndex + postSize : postIndex);

        if (preSize > 0) {
            System.arraycopy(pre.children(), 0, args, 0, preSize);
        }
        if (optSize > 0) {
            System.arraycopy(optionalArguments.children(), 0, args, optIndex, optSize);
        }
        if (postSize > 0) {
            System.arraycopy(post.children(), 0, args, postIndex, postSize);
        }
        if (keywordsSize > 0) {
            System.arraycopy(keywords.children(), 0, args, keywordsIndex, keywordsSize);
        }

        this.restArgNode = rest;
        this.blockArgNode = blockArgNode;
        this.keyRest = keyRest;

        this.arity = createArity();
    }

    private Arity createArity() {
        final String[] keywordArguments;
        boolean[] keywordArgumentsOptional = null;
        boolean allKeywordsOptional = true;
        if (getKeywordCount() > 0) {
            final ParseNode[] keywordNodes = getKeywords().children();
            final int keywordsCount = keywordNodes.length;
            keywordArguments = new String[keywordsCount];
            keywordArgumentsOptional = new boolean[keywordsCount];

            for (int i = 0; i < keywordsCount; i++) {
                final KeywordArgParseNode kwarg = (KeywordArgParseNode) keywordNodes[i];
                final AssignableParseNode assignableNode = kwarg.getAssignable();
                if (!(assignableNode instanceof LocalAsgnParseNode || assignableNode instanceof DAsgnParseNode)) {
                    throw new UnsupportedOperationException("unsupported keyword arg " + kwarg);
                }
                keywordArguments[i] = ((INameNode) assignableNode).getName();
                boolean isRequired = Helpers.isRequiredKeywordArgumentValueNode(assignableNode);
                keywordArgumentsOptional[i] = !isRequired;
                allKeywordsOptional &= !isRequired;
            }
        } else {
            keywordArguments = Arity.NO_KEYWORDS;
        }

        return new Arity(
                getPreCount(),
                getOptionalArgsCount(),
                hasRestArg(),
                getPostCount(),
                keywordArguments,
                allKeywordsOptional,
                hasKeyRest(),
                keywordArgumentsOptional);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ARGSNODE;
    }

    @Override
    public <T> T accept(NodeVisitor<T> iVisitor) {
        return iVisitor.visitArgsNode(this);
    }

    public Arity getArity() {
        return arity.getPreRequired() == getPreCount()
                ? arity
                : (arity = createArity()); // numbered parameters were added
    }

    public ParseNode[] getArgs() {
        return args;
    }

    public int getOptArgIndex() {
        return optIndex;
    }

    public int getPostIndex() {
        return postIndex;
    }

    public int getKeywordsIndex() {
        return keywordsIndex;
    }

    public int getPreCount() {
        return isNumbered()
                ? args.length
                : optIndex;
    }

    public int getOptionalArgsCount() {
        return postIndex - optIndex;
    }

    public int getPostCount() {
        return keywordsIndex - postIndex;
    }

    public int getKeywordCount() {
        return args.length - keywordsIndex;
    }

    public int getRequiredCount() {
        return getPreCount() + getPostCount();
    }

    public ListParseNode getPre() {
        return new ArrayParseNode(getPosition()).addAll(args, 0, getPreCount());
    }

    public ListParseNode getOptArgs() {
        return new ArrayParseNode(getPosition()).addAll(args, optIndex, getOptionalArgsCount());
    }

    public ListParseNode getPost() {
        return new ArrayParseNode(getPosition()).addAll(args, postIndex, getPostCount());
    }


    public ListParseNode getKeywords() {
        return new ArrayParseNode(getPosition()).addAll(args, keywordsIndex, getKeywordCount());
    }

    public boolean hasRestArg() {
        return restArgNode != null;
    }

    public ArgumentParseNode getRestArgNode() {
        return restArgNode;
    }

    public boolean hasKeyRest() {
        return keyRest != null;
    }

    public KeywordRestArgParseNode getKeyRest() {
        return keyRest;
    }

    public boolean hasKwargs() {
        boolean keywords = getKeywordCount() > 0;
        return keywords || keyRest != null;
    }

    /** Gets the explicit block argument of the parameter list (&block).
     *
     * @return Returns a BlockArgParseNode */
    public BlockArgParseNode getBlock() {
        return blockArgNode;
    }

    /** Whether numbered parameters were used in the body (and therefore this has no other formal parameters, or a
     * SyntaxError would have resulted. Can change as numbered parameters are encountered. */
    public boolean isNumbered() {
        return isNumbered;
    }

    /** Add a numbered parameter as a pre-argument (we do this after the node has been created, as numbered parameters
     * are implicit and do not appear in the parameter list).
     *
     * Do NOT call this, call {@link StaticScope#addNumberedParameter} instead */
    public void addNumberedParameter(String name, SourceIndexLength position) {
        assert ParserSupport.isNumberedParameter(name);

        if (!isNumbered) {
            isNumbered = true;
        }

        int ordinal = name.charAt(1) - '0';

        if (args.length < ordinal) { // new numbered parameter
            int oldLength = args.length;
            args = Arrays.copyOf(args, ordinal);
            for (int i = oldLength; i < ordinal; ++i) {
                // implicitly define the preceding arguments
                args[i] = new ArgumentParseNode(position, ("_" + (char) ('0' + ordinal)).intern());
                optIndex = postIndex = keywordsIndex = (short) ordinal;
            }
        }
        // either new, or replaces a previously implicitly-defined argument (setting the proper position)
        args[args.length - 1] = new ArgumentParseNode(position, name);
    }

    @Override
    public List<ParseNode> childNodes() {
        ListParseNode pre = getPre();
        ListParseNode optArgs = getOptArgs();
        ListParseNode post = getPost();
        ListParseNode keywords = getKeywords();

        return ParseNode.createList(pre, optArgs, restArgNode, post, keywords, keyRest, blockArgNode);
    }

}
