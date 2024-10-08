/*----------------------------------------------------------------------------*/
/* This file is generated by the templates/template.rb script and should not  */
/* be modified manually. See                                                  */
/* templates/java/org/prism/AbstractNodeVisitor.java.erb                      */
/* if you are looking to modify the                                           */
/* template                                                                   */
/*----------------------------------------------------------------------------*/

package org.prism;

// GENERATED BY AbstractNodeVisitor.java.erb
// @formatter:off
public abstract class AbstractNodeVisitor<T> {

    protected abstract T defaultVisit(Nodes.Node node);

    /**
     * Visit a AliasGlobalVariableNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitAliasGlobalVariableNode(Nodes.AliasGlobalVariableNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a AliasMethodNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitAliasMethodNode(Nodes.AliasMethodNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a AlternationPatternNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitAlternationPatternNode(Nodes.AlternationPatternNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a AndNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitAndNode(Nodes.AndNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ArgumentsNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitArgumentsNode(Nodes.ArgumentsNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ArrayNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitArrayNode(Nodes.ArrayNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ArrayPatternNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitArrayPatternNode(Nodes.ArrayPatternNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a AssocNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitAssocNode(Nodes.AssocNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a AssocSplatNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitAssocSplatNode(Nodes.AssocSplatNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a BackReferenceReadNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitBackReferenceReadNode(Nodes.BackReferenceReadNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a BeginNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitBeginNode(Nodes.BeginNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a BlockArgumentNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitBlockArgumentNode(Nodes.BlockArgumentNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a BlockLocalVariableNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitBlockLocalVariableNode(Nodes.BlockLocalVariableNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a BlockNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitBlockNode(Nodes.BlockNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a BlockParameterNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitBlockParameterNode(Nodes.BlockParameterNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a BlockParametersNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitBlockParametersNode(Nodes.BlockParametersNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a BreakNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitBreakNode(Nodes.BreakNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a CallAndWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitCallAndWriteNode(Nodes.CallAndWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a CallNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitCallNode(Nodes.CallNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a CallOperatorWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitCallOperatorWriteNode(Nodes.CallOperatorWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a CallOrWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitCallOrWriteNode(Nodes.CallOrWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a CallTargetNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitCallTargetNode(Nodes.CallTargetNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a CapturePatternNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitCapturePatternNode(Nodes.CapturePatternNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a CaseMatchNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitCaseMatchNode(Nodes.CaseMatchNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a CaseNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitCaseNode(Nodes.CaseNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ClassNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitClassNode(Nodes.ClassNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ClassVariableAndWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitClassVariableAndWriteNode(Nodes.ClassVariableAndWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ClassVariableOperatorWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitClassVariableOperatorWriteNode(Nodes.ClassVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ClassVariableOrWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitClassVariableOrWriteNode(Nodes.ClassVariableOrWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ClassVariableReadNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitClassVariableReadNode(Nodes.ClassVariableReadNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ClassVariableTargetNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitClassVariableTargetNode(Nodes.ClassVariableTargetNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ClassVariableWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitClassVariableWriteNode(Nodes.ClassVariableWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantAndWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantAndWriteNode(Nodes.ConstantAndWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantOperatorWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantOperatorWriteNode(Nodes.ConstantOperatorWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantOrWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantOrWriteNode(Nodes.ConstantOrWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantPathAndWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantPathAndWriteNode(Nodes.ConstantPathAndWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantPathNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantPathNode(Nodes.ConstantPathNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantPathOperatorWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantPathOperatorWriteNode(Nodes.ConstantPathOperatorWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantPathOrWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantPathOrWriteNode(Nodes.ConstantPathOrWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantPathTargetNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantPathTargetNode(Nodes.ConstantPathTargetNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantPathWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantPathWriteNode(Nodes.ConstantPathWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantReadNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantReadNode(Nodes.ConstantReadNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantTargetNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantTargetNode(Nodes.ConstantTargetNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ConstantWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitConstantWriteNode(Nodes.ConstantWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a DefNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitDefNode(Nodes.DefNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a DefinedNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitDefinedNode(Nodes.DefinedNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ElseNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitElseNode(Nodes.ElseNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a EmbeddedStatementsNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitEmbeddedStatementsNode(Nodes.EmbeddedStatementsNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a EmbeddedVariableNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitEmbeddedVariableNode(Nodes.EmbeddedVariableNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a EnsureNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitEnsureNode(Nodes.EnsureNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a FalseNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitFalseNode(Nodes.FalseNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a FindPatternNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitFindPatternNode(Nodes.FindPatternNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a FlipFlopNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitFlipFlopNode(Nodes.FlipFlopNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a FloatNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitFloatNode(Nodes.FloatNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ForNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitForNode(Nodes.ForNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ForwardingArgumentsNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitForwardingArgumentsNode(Nodes.ForwardingArgumentsNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ForwardingParameterNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitForwardingParameterNode(Nodes.ForwardingParameterNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ForwardingSuperNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitForwardingSuperNode(Nodes.ForwardingSuperNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a GlobalVariableAndWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitGlobalVariableAndWriteNode(Nodes.GlobalVariableAndWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a GlobalVariableOperatorWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitGlobalVariableOperatorWriteNode(Nodes.GlobalVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a GlobalVariableOrWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitGlobalVariableOrWriteNode(Nodes.GlobalVariableOrWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a GlobalVariableReadNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitGlobalVariableReadNode(Nodes.GlobalVariableReadNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a GlobalVariableTargetNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitGlobalVariableTargetNode(Nodes.GlobalVariableTargetNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a GlobalVariableWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitGlobalVariableWriteNode(Nodes.GlobalVariableWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a HashNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitHashNode(Nodes.HashNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a HashPatternNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitHashPatternNode(Nodes.HashPatternNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a IfNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitIfNode(Nodes.IfNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ImaginaryNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitImaginaryNode(Nodes.ImaginaryNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ImplicitNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitImplicitNode(Nodes.ImplicitNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ImplicitRestNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitImplicitRestNode(Nodes.ImplicitRestNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInNode(Nodes.InNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a IndexAndWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitIndexAndWriteNode(Nodes.IndexAndWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a IndexOperatorWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitIndexOperatorWriteNode(Nodes.IndexOperatorWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a IndexOrWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitIndexOrWriteNode(Nodes.IndexOrWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a IndexTargetNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitIndexTargetNode(Nodes.IndexTargetNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InstanceVariableAndWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInstanceVariableAndWriteNode(Nodes.InstanceVariableAndWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InstanceVariableOperatorWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInstanceVariableOperatorWriteNode(Nodes.InstanceVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InstanceVariableOrWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInstanceVariableOrWriteNode(Nodes.InstanceVariableOrWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InstanceVariableReadNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInstanceVariableReadNode(Nodes.InstanceVariableReadNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InstanceVariableTargetNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInstanceVariableTargetNode(Nodes.InstanceVariableTargetNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InstanceVariableWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInstanceVariableWriteNode(Nodes.InstanceVariableWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a IntegerNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitIntegerNode(Nodes.IntegerNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InterpolatedMatchLastLineNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInterpolatedMatchLastLineNode(Nodes.InterpolatedMatchLastLineNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InterpolatedRegularExpressionNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInterpolatedRegularExpressionNode(Nodes.InterpolatedRegularExpressionNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InterpolatedStringNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInterpolatedStringNode(Nodes.InterpolatedStringNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InterpolatedSymbolNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInterpolatedSymbolNode(Nodes.InterpolatedSymbolNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a InterpolatedXStringNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitInterpolatedXStringNode(Nodes.InterpolatedXStringNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ItLocalVariableReadNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitItLocalVariableReadNode(Nodes.ItLocalVariableReadNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ItParametersNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitItParametersNode(Nodes.ItParametersNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a KeywordHashNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitKeywordHashNode(Nodes.KeywordHashNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a KeywordRestParameterNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitKeywordRestParameterNode(Nodes.KeywordRestParameterNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a LambdaNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitLambdaNode(Nodes.LambdaNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a LocalVariableAndWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitLocalVariableAndWriteNode(Nodes.LocalVariableAndWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a LocalVariableOperatorWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitLocalVariableOperatorWriteNode(Nodes.LocalVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a LocalVariableOrWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitLocalVariableOrWriteNode(Nodes.LocalVariableOrWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a LocalVariableReadNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitLocalVariableReadNode(Nodes.LocalVariableReadNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a LocalVariableTargetNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitLocalVariableTargetNode(Nodes.LocalVariableTargetNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a LocalVariableWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitLocalVariableWriteNode(Nodes.LocalVariableWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a MatchLastLineNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitMatchLastLineNode(Nodes.MatchLastLineNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a MatchPredicateNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitMatchPredicateNode(Nodes.MatchPredicateNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a MatchRequiredNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitMatchRequiredNode(Nodes.MatchRequiredNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a MatchWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitMatchWriteNode(Nodes.MatchWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a MissingNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitMissingNode(Nodes.MissingNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ModuleNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitModuleNode(Nodes.ModuleNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a MultiTargetNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitMultiTargetNode(Nodes.MultiTargetNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a MultiWriteNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitMultiWriteNode(Nodes.MultiWriteNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a NextNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitNextNode(Nodes.NextNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a NilNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitNilNode(Nodes.NilNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a NoKeywordsParameterNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitNoKeywordsParameterNode(Nodes.NoKeywordsParameterNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a NumberedParametersNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitNumberedParametersNode(Nodes.NumberedParametersNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a NumberedReferenceReadNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitNumberedReferenceReadNode(Nodes.NumberedReferenceReadNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a OptionalKeywordParameterNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitOptionalKeywordParameterNode(Nodes.OptionalKeywordParameterNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a OptionalParameterNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitOptionalParameterNode(Nodes.OptionalParameterNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a OrNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitOrNode(Nodes.OrNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ParametersNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitParametersNode(Nodes.ParametersNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ParenthesesNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitParenthesesNode(Nodes.ParenthesesNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a PinnedExpressionNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitPinnedExpressionNode(Nodes.PinnedExpressionNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a PinnedVariableNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitPinnedVariableNode(Nodes.PinnedVariableNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a PostExecutionNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitPostExecutionNode(Nodes.PostExecutionNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a PreExecutionNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitPreExecutionNode(Nodes.PreExecutionNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ProgramNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitProgramNode(Nodes.ProgramNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RangeNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRangeNode(Nodes.RangeNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RationalNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRationalNode(Nodes.RationalNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RedoNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRedoNode(Nodes.RedoNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RegularExpressionNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRegularExpressionNode(Nodes.RegularExpressionNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RequiredKeywordParameterNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRequiredKeywordParameterNode(Nodes.RequiredKeywordParameterNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RequiredParameterNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRequiredParameterNode(Nodes.RequiredParameterNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RescueModifierNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRescueModifierNode(Nodes.RescueModifierNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RescueNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRescueNode(Nodes.RescueNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RestParameterNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRestParameterNode(Nodes.RestParameterNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a RetryNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitRetryNode(Nodes.RetryNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ReturnNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitReturnNode(Nodes.ReturnNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a SelfNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitSelfNode(Nodes.SelfNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a ShareableConstantNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitShareableConstantNode(Nodes.ShareableConstantNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a SingletonClassNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitSingletonClassNode(Nodes.SingletonClassNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a SourceEncodingNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitSourceEncodingNode(Nodes.SourceEncodingNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a SourceFileNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitSourceFileNode(Nodes.SourceFileNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a SourceLineNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitSourceLineNode(Nodes.SourceLineNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a SplatNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitSplatNode(Nodes.SplatNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a StatementsNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitStatementsNode(Nodes.StatementsNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a StringNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitStringNode(Nodes.StringNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a SuperNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitSuperNode(Nodes.SuperNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a SymbolNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitSymbolNode(Nodes.SymbolNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a TrueNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitTrueNode(Nodes.TrueNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a UndefNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitUndefNode(Nodes.UndefNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a UnlessNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitUnlessNode(Nodes.UnlessNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a UntilNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitUntilNode(Nodes.UntilNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a WhenNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitWhenNode(Nodes.WhenNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a WhileNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitWhileNode(Nodes.WhileNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a XStringNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitXStringNode(Nodes.XStringNode node) {
        return defaultVisit(node);
    }

    /**
     * Visit a YieldNode node.
     *
     * @param node The node to visit.
     * @return The result of visiting the node.
     */
    public T visitYieldNode(Nodes.YieldNode node) {
        return defaultVisit(node);
    }

}
// @formatter:on
