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
 * Copyright (C) 2006-2007 Thomas E Enebo <enebo@acm.org>
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
package org.truffleruby.parser.scope;

import java.util.Arrays;

import org.truffleruby.annotations.SuppressFBWarnings;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.ast.ArgsParseNode;
import org.truffleruby.parser.ast.AssignableParseNode;
import org.truffleruby.parser.ast.DAsgnParseNode;
import org.truffleruby.parser.ast.DVarParseNode;
import org.truffleruby.parser.ast.LocalAsgnParseNode;
import org.truffleruby.parser.ast.LocalVarParseNode;
import org.truffleruby.parser.ast.ParseNode;
import org.truffleruby.parser.ast.VCallParseNode;

/** StaticScope represents lexical scoping of variables and module/class constants.
 *
 * At a very high level every scopes enclosing scope contains variables in the next outer lexical layer. The enclosing
 * scopes variables may or may not be reachable depending on the scoping rules for variables (governed by
 * BlockStaticScope and LocalStaticScope).
 *
 * StaticScope also keeps track of current module/class that is in scope. previousCRefScope will point to the previous
 * scope of the enclosing module/class (cref). */
public class StaticScope {

    // Next immediate scope.  Variable and constant scoping rules make use of this variable
    // in different ways.
    protected final StaticScope enclosingScope;

    // Our name holder (offsets are assigned as variables are added)
    private String[] variableNames;

    // A list of booleans indicating which variables are named captures from regexp
    private boolean[] namedCaptures;

    // File name where this static scope came from or null if a native or artificial scope
    private String file;

    private static final String[] NO_NAMES = StringUtils.EMPTY_STRING_ARRAY;

    private Type type;
    private boolean isBlockOrEval;

    private long commandArgumentStack;

    /** Parameters (block, method) for the current scope. */
    private ArgsParseNode argsParseNode;

    /** Whether this <b>block</b> has parameters. Set to true before argsParseNode can be assigned, as we need this to
     * handle default values in the parameter list. */
    private boolean hasBlockParameter;

    /** Whether this is a block scope that has sub-scopes (without intervening scope gates) that use numbered
     * parameters. */
    private boolean hasNumberedSubScope;

    /** Whether this is a block scope that has enclosing scopes (without intervening scope gates) that use numbered
     * parameters, <b>at the time where the block is entered</b>. */
    private boolean hasNumberedSuperScope;

    public enum Type {
        LOCAL,
        BLOCK;
    }

    /**
     *
     */
    public StaticScope(Type type, StaticScope enclosingScope, String file) {
        this(type, enclosingScope, NO_NAMES);

        this.file = file;
    }

    /** Construct a new static scope.
     *
     * @param type the type of scope
     * @param enclosingScope the lexically containing scope. */
    public StaticScope(Type type, StaticScope enclosingScope) {
        this(type, enclosingScope, NO_NAMES);
    }

    /** Construct a new static scope. The array of strings should all be the interned versions, since several other
     * optimizations depend on being able to do object equality checks.
     *
     * @param type the type of scope
     * @param enclosingScope the lexically containing scope.
     * @param names The list of interned String variable names. */
    protected StaticScope(Type type, StaticScope enclosingScope, String[] names) {
        assert names != null : "names is not null";
        assert namesAreInterned(names);

        this.enclosingScope = enclosingScope;
        this.variableNames = names;
        this.type = type;
        this.isBlockOrEval = (type != Type.LOCAL);
        this.hasNumberedSuperScope = isBlockOrEval && enclosingScope != null && (enclosingScope.hasNumberedSuperScope ||
                enclosingScope.isNumberedBlockScope());
    }

    /** Check that all strings in the given array are the interned versions (for footprint and identity check reasons).
     *
     * @param names The array of strings
     * @return true if they are all interned, false otherwise */
    @SuppressFBWarnings("ES")
    private static boolean namesAreInterned(String[] names) {
        for (String name : names) {
            // Note that this object equality check is intentional, to ensure
            // the string and its interned version are the same object.
            if (name != name.intern()) {
                return false;
            }
        }
        return true;
    }

    /** Add a new variable to this (current) scope unless it is already defined in the current scope.
     *
     * @param name of new variable
     * @return index of variable */
    public int addVariableThisScope(String name) {
        // Ignore duplicate "_" args in blocks
        // (duplicate _ args are named "_$0")
        // Dont allocate slots for them.
        if (name.equals("_$0")) {
            return -1;
        }

        int slot = exists(name);

        if (slot >= 0) {
            return slot;
        }

        // This is perhaps inefficient timewise?  Optimal spacewise
        growVariableNames(name);

        // Returns slot of variable
        return variableNames.length - 1;
    }

    /** Add a new named capture variable to this (current) scope.
     *
     * @param name name of variable.
     * @return index of variable */
    public int addNamedCaptureVariable(String name) {
        int index = addVariableThisScope(name);

        growNamedCaptures(index);

        return index;
    }

    /** Add a new variable to this (current) scope unless it is already defined in any reachable scope.
     *
     * @param name of new variable
     * @return index+depth merged location of scope */
    public int addVariable(String name) {
        int slot = isDefined(name);

        if (slot >= 0) {
            return slot;
        }

        // This is perhaps inefficient timewise?  Optimal spacewise
        growVariableNames(name);

        // Returns slot of variable
        return variableNames.length - 1;
    }

    public void addNumberedParameter(String name, SourceIndexLength position) {
        assert argsParseNode != null;
        addVariable(name);
        argsParseNode.addNumberedParameter(name, position);

        // Set the hasNumberedSubScope flag for this and enclosing scopes.
        StaticScope scope = enclosingScope;
        while (scope != null && scope.isBlockScope() && !scope.hasNumberedSubScope) {
            scope.hasNumberedSubScope = true;
            scope = scope.enclosingScope;
        }
    }

    public String[] getVariables() {
        return variableNames;
    }

    public int getNumberOfVariables() {
        return variableNames.length;
    }

    public void setVariables(String[] names) {
        assert names != null : "names is not null";
        assert namesAreInterned(names);

        variableNames = new String[names.length];
        System.arraycopy(names, 0, variableNames, 0, names.length);
    }

    /** Next outer most scope in list of scopes. An enclosing scope may have no direct scoping relationship to its
     * child. If I am in a localScope and then I enter something which creates another localScope the enclosing scope
     * will be the first scope, but there are no valid scoping relationships between the two. Methods which walk the
     * enclosing scopes are responsible for enforcing appropriate scoping relationships.
     *
     * @return the parent scope */
    public StaticScope getEnclosingScope() {
        return enclosingScope;
    }

    /** Does the variable exist?
     *
     * @param name of the variable to find
     * @return index of variable or -1 if it does not exist */
    public int exists(String name) {
        return findVariableName(name);
    }

    @SuppressFBWarnings("ES")
    private int findVariableName(String name) {
        assert name == name.intern() : name;
        for (int i = 0; i < variableNames.length; i++) {
            if (name == variableNames[i]) {
                return i;
            }
        }
        return -1;
    }

    /** Is this name in the visible to the current scope
     *
     * @param name to be looked for
     * @return a location where the left-most 16 bits of number of scopes down it is and the right-most 16 bits
     *         represents its index in that scope */
    public int isDefined(String name) {
        return isDefined(name, 0);
    }

    /** Make a DASgn or LocalAsgn node based on scope logic
     *
     * @param position
     * @param name
     * @param value */
    public AssignableParseNode assign(SourceIndexLength position, String name, ParseNode value) {
        return assign(position, name, value, this, 0);
    }

    public int isDefined(String name, int depth) {
        if (isBlockOrEval) {
            int slot = exists(name);
            if (slot >= 0) {
                return (depth << 16) | slot;
            }

            return enclosingScope.isDefined(name, depth + 1);
        } else {
            return (depth << 16) | exists(name);
        }
    }

    public AssignableParseNode addAssign(SourceIndexLength position, String name, ParseNode value) {
        int slot = addVariable(name);
        // No bit math to store level since we know level is zero for this case
        return new DAsgnParseNode(position, name, slot, value);
    }

    public AssignableParseNode assign(SourceIndexLength position, String name, ParseNode value,
            StaticScope topScope, int depth) {
        int slot = exists(name);

        // We can assign if we already have variable of that name here or we are the only
        // scope in the chain (which Local scopes always are).
        if (slot >= 0) {
            return isBlockOrEval
                    ? new DAsgnParseNode(position, name, ((depth << 16) | slot), value)
                    : new LocalAsgnParseNode(position, name, ((depth << 16) | slot), value);
        } else if (!isBlockOrEval && (topScope == this)) {
            slot = addVariable(name);

            return new LocalAsgnParseNode(position, name, slot, value);
        }

        // If we are not a block-scope and we go there, we know that 'topScope' is a block scope
        // because a local scope cannot be within a local scope
        // If topScope was itself it would have created a LocalAsgnParseNode above.
        return isBlockOrEval
                ? enclosingScope.assign(position, name, value, topScope, depth + 1)
                : topScope.addAssign(position, name, value);
    }

    public ParseNode declare(SourceIndexLength position, String name, int depth) {
        int slot = exists(name);

        if (slot >= 0) {
            return isBlockOrEval
                    ? new DVarParseNode(position, ((depth << 16) | slot), name)
                    : new LocalVarParseNode(position, ((depth << 16) | slot), name);
        }

        return isBlockOrEval ? enclosingScope.declare(position, name, depth + 1) : new VCallParseNode(position, name);
    }

    /** Make a DVar or LocalVar node based on scoping logic
     *
     * @param position the location that in the source that the new node will come from
     * @param name of the variable to be created is named
     * @return a DVarParseNode or LocalVarParseNode */
    public ParseNode declare(SourceIndexLength position, String name) {
        return declare(position, name, 0);
    }

    /** Gets the Local Scope relative to the current Scope. For LocalScopes this will be itself. Blocks will contain the
     * LocalScope it contains.
     *
     * @return localScope */

    public StaticScope getLocalScope() {
        return (type != Type.BLOCK) ? this : enclosingScope.getLocalScope();
    }

    public boolean isBlockScope() {
        return isBlockOrEval;
    }

    /** Whether this is a block scope that uses numbered parameters. */
    public boolean isNumberedBlockScope() {
        return argsParseNode != null && argsParseNode.isNumbered();
    }

    public void setCommandArgumentStack(long commandArgumentStack) {
        this.commandArgumentStack = commandArgumentStack;
    }

    public long getCommandArgumentStack() {
        return commandArgumentStack;
    }

    public void setArgsParseNode(ArgsParseNode argsParseNode) {
        this.argsParseNode = argsParseNode;
    }

    public ArgsParseNode getArgsParseNode() {
        return argsParseNode;
    }

    /** Whether this **block** has parameter. Set to true before argsParseNode can be assigned, as we need this to
     * handle default values in the parameter list. */
    public boolean hasBlockParameters() {
        return hasBlockParameter;
    }

    /** see {@link #hasBlockParameter} */
    public void setHasBlockParameters() {
        hasBlockParameter = true;
    }

    /** Whether this is a block scope that has sub-scopes (without intervening scope gates) that use numbered
     * parameters. */
    public boolean hasNumberedSubScope() {
        return hasNumberedSubScope;
    }

    /** Whether this is a block scope that has enclosing scopes (without intervening scope gates) that use numbered
     * parameters, <b>at the time where the block is entered</b>. */
    public boolean hasNumberedSuperScope() {
        return hasNumberedSuperScope;
    }

    @SuppressFBWarnings("ES")
    private void growVariableNames(String name) {
        assert name == name.intern() : name;
        String[] newVariableNames = new String[variableNames.length + 1];
        System.arraycopy(variableNames, 0, newVariableNames, 0, variableNames.length);
        variableNames = newVariableNames;
        variableNames[variableNames.length - 1] = name;
    }

    private void growNamedCaptures(int index) {
        boolean[] namedCaptures = this.namedCaptures;
        boolean[] newNamedCaptures;
        if (namedCaptures != null) {
            newNamedCaptures = new boolean[Math.max(index + 1, namedCaptures.length)];
            System.arraycopy(namedCaptures, 0, newNamedCaptures, 0, namedCaptures.length);
        } else {
            newNamedCaptures = new boolean[index + 1];
        }
        newNamedCaptures[index] = true;
        this.namedCaptures = newNamedCaptures;
    }

    public boolean isNamedCapture(int index) {
        boolean[] namedCaptures = this.namedCaptures;
        return namedCaptures != null && index < namedCaptures.length && namedCaptures[index];
    }

    @Override
    public String toString() {
        // FIXME: Do we need to persist cref as well?
        return "StaticScope(" + type + "):" + Arrays.toString(variableNames);
    }

    public Type getType() {
        return type;
    }

    public String getFile() {
        return file;
    }
}
