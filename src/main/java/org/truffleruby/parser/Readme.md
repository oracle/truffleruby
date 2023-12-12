# Translators

## Prism nodes

It might be helpful to have semantically close nodes grouped and categorised to easily navigate through the translators source code.

### Generic containers

- ProgramNode
- StatementsNode

### Literals

#### Not compound nodes

- NilNode
- FalseNode
- TrueNode
- IntegerNode
- FloatNode
- SelfNode
- ImaginaryNode
- RationalNode
- ArrayNode
- RangeNode

#### Compound nodes

##### Hash

- HashNode
- AssocNode
- AssocSplatNode

##### String

- StringNode
- XStringNode
- InterpolatedStringNode
- InterpolatedXStringNode
- EmbeddedStatementsNode
- EmbeddedVariableNode

Interpolated-prefixed nodes may also contain EmbeddedStatementsNode, EmbeddedVariableNode and StringNode.

##### Symbol

SymbolNode
InterpolatedSymbolNode

InterpolatedSymbolNode may also contain EmbeddedStatementsNode, EmbeddedVariableNode and StringNode.

##### Regexp

- RegularExpressionNode
- InterpolatedRegularExpressionNode
- MatchLastLineNode
- InterpolatedMatchLastLineNode
- MatchWriteNode

Interpolated-prefixed nodes may also contain EmbeddedStatementsNode, EmbeddedVariableNode and StringNode.

### Operators

BeginNode
ParenthesesNode
SplatNode
DefinedNode
UndefNode

### Logical operators

- AndNode
- OrNode

### Assignment operators (+=, /=, etc)

#### Generic nodes (except nodes for &&= and ||=)

- ClassVariableOperatorWriteNode
- ConstantOperatorWriteNode
- ConstantPathOperatorWriteNode
- GlobalVariableOperatorWriteNode
- InstanceVariableOperatorWriteNode
- LocalVariableOperatorWriteNode
- IndexOperatorWriteNode
- CallOperatorWriteNode

#### &&= operator

- ClassVariableAndWriteNode
- ConstantAndWriteNode
- ConstantPathAndWriteNode
- GlobalVariableAndWriteNode
- InstanceVariableAndWriteNode
- LocalVariableAndWriteNode
- CallAndWriteNode
- IndexAndWriteNode

#### ||= operator

- ClassVariableOrWriteNode
- ConstantOrWriteNode
- ConstantPathOrWriteNode
- GlobalVariableOrWriteNode
- InstanceVariableOrWriteNode
- LocalVariableOrWriteNode
- CallOrWriteNode
- IndexOrWriteNode

### Control flow operators

- IfNode
- ElseNode
- UnlessNode
- WhileNode
- UntilNode
- FlipFlopNode
- ForNode
- BreakNode
- NextNode
- RedoNode
- RetryNode
- ReturnNode

Operators that accept arguments, e.g. break, next, return, have nested ArgumentsNode to represent specified arguments.

#### Case operator

- CaseNode
- CaseMatchNode
- InNode

### Exception handling

- RescueNode
- EnsureNode
- RescueModifierNode

The capturing exception object with `=> e` operator is represented with Target nodes (see "Target nodes" section).

### Module/Class

- ClassNode
- ModuleNode
- SingletonClassNode

### Method/block definition

Method definition and block definition AST share too many nodes to keep them separated.

#### Method-specific nodes

- DefNode
- ForwardingParameterNode

#### Block-specific nodes

- BlockNode
- LambdaNode
- BlockParametersNode
- NumberedParametersNode
- BlockLocalVariableNode
- ImplicitRestNode

#### Shared nodes

- ParametersNode
- RequiredParameterNode
- OptionalParameterNode
- RestParameterNode
- RequiredKeywordParameterNode
- OptionalKeywordParameterNode
- KeywordRestParameterNode
- NoKeywordsParameterNode
- BlockParameterNode
- MultiTargetNode

MultiTargetNode node is used to express destructuring Array argument in parameters:

```ruby
def f(a, (b, c), d))
end
```

### Method call

- CallNode
- SuperNode
- ForwardingSuperNode
- YieldNode
- ArgumentsNode
- KeywordHashNode
- ForwardingArgumentsNode
- BlockArgumentNode

Method-call-like nodes have nested ArgumentsNode to represent specified arguments.

### Variables/constants reading/writing

- ClassVariableReadNode
- ClassVariableWriteNode
- ConstantPathNode
- ConstantPathWriteNode
- ConstantReadNode
- ConstantWriteNode
- GlobalVariableReadNode
- GlobalVariableWriteNode
- InstanceVariableReadNode
- InstanceVariableWriteNode
- LocalVariableReadNode
- LocalVariableWriteNode

#### Global variables specific nodes

- BackReferenceReadNode
- NumberedReferenceReadNode

### Target nodes

Target nodes express type and name of entity that should be assigned some value. It could be a variable, a constant or even a method call.

Target nodes are used to represent `=> e` in `rescue` clause and left-hand-side components in multi-assignment expression (`a, b = []`).

- ClassVariableTargetNode
- ConstantPathTargetNode
- ConstantTargetNode
- GlobalVariableTargetNode
- InstanceVariableTargetNode
- LocalVariableTargetNode
- MultiTargetNode

MultiTargetNode is used to express nesting:

```ruby
a, (b, c), d = []
```

### Multi-assignment

Multi-assignment nodes represent assignment expression when left-hand-side contains several entities (variables, constants, etc).

- MultiWriteNode
- MultiTargetNode
- ImplicitRestNode

### Pattern matching

- AlternationPatternNode
- ArrayPatternNode
- HashPatternNode
- CapturePatternNode
- FindPatternNode
- MatchPredicateNode
- MatchRequiredNode
- PinnedExpressionNode
- PinnedVariableNode

### Constants

- SourceEncodingNode
- SourceFileNode
- SourceLineNode

### Hooks

- PostExecutionNode
- PreExecutionNode

### Nodes not matching source code

ImplicitNode
MissingNode