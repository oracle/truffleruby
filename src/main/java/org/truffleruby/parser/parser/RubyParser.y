%{
package org.truffleruby.parser.parser;

import com.oracle.truffle.api.strings.TruffleString;

import org.truffleruby.Layouts;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.string.TStringConstants;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.RubyDeferredWarnings;
import org.truffleruby.parser.ast.ArgsParseNode;
import org.truffleruby.parser.ast.ArgumentParseNode;
import org.truffleruby.parser.ast.ArrayParseNode;
import org.truffleruby.parser.ast.AssignableParseNode;
import org.truffleruby.parser.ast.BackRefParseNode;
import org.truffleruby.parser.ast.BeginParseNode;
import org.truffleruby.parser.ast.BlockAcceptingParseNode;
import org.truffleruby.parser.ast.BlockArgParseNode;
import org.truffleruby.parser.ast.BlockParseNode;
import org.truffleruby.parser.ast.BlockPassParseNode;
import org.truffleruby.parser.ast.BreakParseNode;
import org.truffleruby.parser.ast.ClassParseNode;
import org.truffleruby.parser.ast.ClassVarAsgnParseNode;
import org.truffleruby.parser.ast.ClassVarParseNode;
import org.truffleruby.parser.ast.Colon3ParseNode;
import org.truffleruby.parser.ast.ConstDeclParseNode;
import org.truffleruby.parser.ast.ConstParseNode;
import org.truffleruby.parser.ast.DStrParseNode;
import org.truffleruby.parser.ast.DSymbolParseNode;
import org.truffleruby.parser.ast.DXStrParseNode;
import org.truffleruby.parser.ast.DefnParseNode;
import org.truffleruby.parser.ast.DefsParseNode;
import org.truffleruby.parser.ast.DotParseNode;
import org.truffleruby.parser.ast.EncodingParseNode;
import org.truffleruby.parser.ast.EnsureParseNode;
import org.truffleruby.parser.ast.EvStrParseNode;
import org.truffleruby.parser.ast.FCallParseNode;
import org.truffleruby.parser.ast.FalseParseNode;
import org.truffleruby.parser.ast.FileParseNode;
import org.truffleruby.parser.ast.FixnumParseNode;
import org.truffleruby.parser.ast.FloatParseNode;
import org.truffleruby.parser.ast.ForParseNode;
import org.truffleruby.parser.ast.GlobalAsgnParseNode;
import org.truffleruby.parser.ast.GlobalVarParseNode;
import org.truffleruby.parser.ast.HashParseNode;
import org.truffleruby.parser.ast.IfParseNode;
import org.truffleruby.parser.ast.InstAsgnParseNode;
import org.truffleruby.parser.ast.InstVarParseNode;
import org.truffleruby.parser.ast.IterParseNode;
import org.truffleruby.parser.ast.LambdaParseNode;
import org.truffleruby.parser.ast.ListParseNode;
import org.truffleruby.parser.ast.LiteralParseNode;
import org.truffleruby.parser.ast.LocalVarParseNode;
import org.truffleruby.parser.ast.ModuleParseNode;
import org.truffleruby.parser.ast.MultipleAsgnParseNode;
import org.truffleruby.parser.ast.NextParseNode;
import org.truffleruby.parser.ast.NilImplicitParseNode;
import org.truffleruby.parser.ast.NilParseNode;
import org.truffleruby.parser.ast.NonLocalControlFlowParseNode;
import org.truffleruby.parser.ast.NumericParseNode;
import org.truffleruby.parser.ast.OpAsgnAndParseNode;
import org.truffleruby.parser.ast.OpAsgnOrParseNode;
import org.truffleruby.parser.ast.OptArgParseNode;
import org.truffleruby.parser.ast.ParseNode;
import org.truffleruby.parser.ast.PostExeParseNode;
import org.truffleruby.parser.ast.PreExe19ParseNode;
import org.truffleruby.parser.ast.RationalParseNode;
import org.truffleruby.parser.ast.RedoParseNode;
import org.truffleruby.parser.ast.RegexpParseNode;
import org.truffleruby.parser.ast.RequiredKeywordArgumentValueParseNode;
import org.truffleruby.parser.ast.RescueBodyParseNode;
import org.truffleruby.parser.ast.RescueParseNode;
import org.truffleruby.parser.ast.RestArgParseNode;
import org.truffleruby.parser.ast.RetryParseNode;
import org.truffleruby.parser.ast.ReturnParseNode;
import org.truffleruby.parser.ast.SClassParseNode;
import org.truffleruby.parser.ast.SelfParseNode;
import org.truffleruby.parser.ast.SplatParseNode;
import org.truffleruby.parser.ast.StarParseNode;
import org.truffleruby.parser.ast.StrParseNode;
import org.truffleruby.parser.ast.TrueParseNode;
import org.truffleruby.parser.ast.UnnamedRestArgParseNode;
import org.truffleruby.parser.ast.UntilParseNode;
import org.truffleruby.parser.ast.VAliasParseNode;
import org.truffleruby.parser.ast.WhileParseNode;
import org.truffleruby.parser.ast.XStrParseNode;
import org.truffleruby.parser.ast.YieldParseNode;
import org.truffleruby.parser.ast.ZArrayParseNode;
import org.truffleruby.parser.ast.ZSuperParseNode;
import org.truffleruby.parser.ast.types.ILiteralNode;
import org.truffleruby.parser.lexer.LexerSource;
import org.truffleruby.parser.lexer.RubyLexer;
import org.truffleruby.parser.lexer.StrTerm;
import org.truffleruby.parser.lexer.SyntaxException.PID;

import static org.truffleruby.parser.lexer.RubyLexer.EXPR_BEG;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_END;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_ENDARG;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_ENDFN;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_FITEM;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_FNAME;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_LABEL;
import static org.truffleruby.parser.parser.ParserSupport.value_expr;

// @formatter:off
// CheckStyle: start generated
@SuppressFBWarnings("IP")
@SuppressWarnings({"unchecked", "fallthrough", "cast"})
public class RubyParser {
    protected final ParserSupport support;
    protected final RubyLexer lexer;

    public RubyParser(LexerSource source, RubyDeferredWarnings warnings) {
        this.support = new ParserSupport(source, warnings);
        this.lexer = new RubyLexer(support, source, warnings);
        support.setLexer(lexer);
    }
%}

%token <SourceIndexLength> keyword_class keyword_module keyword_def keyword_undef
  keyword_begin keyword_rescue keyword_ensure keyword_end keyword_if
  keyword_unless keyword_then keyword_elsif keyword_else keyword_case
  keyword_when keyword_while keyword_until keyword_for keyword_break
  keyword_next keyword_redo keyword_retry keyword_in keyword_do
  keyword_do_cond keyword_do_block keyword_return keyword_yield keyword_super
  keyword_self keyword_nil keyword_true keyword_false keyword_and keyword_or
  keyword_not modifier_if modifier_unless modifier_while modifier_until
  modifier_rescue keyword_alias keyword_defined keyword_BEGIN keyword_END
  keyword__LINE__ keyword__FILE__ keyword__ENCODING__ keyword_do_lambda 

%token <TruffleString> tIDENTIFIER tFID tGVAR tIVAR tCONSTANT tCVAR tLABEL
%token <StrParseNode> tCHAR
%type <TruffleString> sym symbol operation operation2 operation3 op fname cname
%type <TruffleString> f_norm_arg restarg_mark
%type <TruffleString> dot_or_colon  blkarg_mark
%token <TruffleString> tUPLUS         /* unary+ */
%token <TruffleString> tUMINUS        /* unary- */
%token <TruffleString> tUMINUS_NUM    /* unary- */
%token <TruffleString> tPOW           /* ** */
%token <TruffleString> tCMP           /* <=> */
%token <TruffleString> tEQ            /* == */
%token <TruffleString> tEQQ           /* === */
%token <TruffleString> tNEQ           /* != */
%token <TruffleString> tGEQ           /* >= */
%token <TruffleString> tLEQ           /* <= */
%token <TruffleString> tANDOP tOROP   /* && and || */
%token <TruffleString> tMATCH tNMATCH /* =~ and !~ */
%token <TruffleString> tDOT           /* Is just '.' in ruby and not a token */
%token <TruffleString> tDOT2 tDOT3    /* .. and ... */
%token <TruffleString> tBDOT2 tBDOT3    /* (.. and (... */
%token <TruffleString> tAREF tASET    /* [] and []= */
%token <TruffleString> tLSHFT tRSHFT  /* << and >> */
%token <TruffleString> tANDDOT        /* &. */
%token <TruffleString> tCOLON2        /* :: */
%token <TruffleString> tCOLON3        /* :: at EXPR_BEG */
%token <TruffleString> tOP_ASGN       /* +=, -=  etc. */
%token <TruffleString> tASSOC         /* => */
%token <SourceIndexLength> tLPAREN       /* ( */
%token <SourceIndexLength> tLPAREN2      /* ( Is just '(' in ruby and not a token */
%token <TruffleString> tRPAREN        /* ) */
%token <SourceIndexLength> tLPAREN_ARG    /* ( */
%token <TruffleString> tLBRACK        /* [ */
%token <TruffleString> tRBRACK        /* ] */
%token <SourceIndexLength> tLBRACE        /* { */
%token <SourceIndexLength> tLBRACE_ARG    /* { */
%token <TruffleString> tSTAR          /* * */
%token <TruffleString> tSTAR2         /* *  Is just '*' in ruby and not a token */
%token <TruffleString> tAMPER         /* & */
%token <TruffleString> tAMPER2        /* &  Is just '&' in ruby and not a token */
%token <TruffleString> tTILDE         /* ` is just '`' in ruby and not a token */
%token <TruffleString> tPERCENT       /* % is just '%' in ruby and not a token */
%token <TruffleString> tDIVIDE        /* / is just '/' in ruby and not a token */
%token <TruffleString> tPLUS          /* + is just '+' in ruby and not a token */
%token <TruffleString> tMINUS         /* - is just '-' in ruby and not a token */
%token <TruffleString> tLT            /* < is just '<' in ruby and not a token */
%token <TruffleString> tGT            /* > is just '>' in ruby and not a token */
%token <TruffleString> tPIPE          /* | is just '|' in ruby and not a token */
%token <TruffleString> tBANG          /* ! is just '!' in ruby and not a token */
%token <TruffleString> tCARET         /* ^ is just '^' in ruby and not a token */
%token <SourceIndexLength> tLCURLY        /* { is just '{' in ruby and not a token */
%token <TruffleString> tRCURLY        /* } is just '}' in ruby and not a token */
%token <TruffleString> tBACK_REF2     /* { is just '`' in ruby and not a token */
%token <TruffleString> tSYMBEG tSTRING_BEG tXSTRING_BEG tREGEXP_BEG tWORDS_BEG tQWORDS_BEG
%token <TruffleString> tSTRING_DBEG tSTRING_DVAR tSTRING_END
%token <TruffleString> tLAMBDA tLAMBEG
%token <ParseNode> tNTH_REF tBACK_REF tSTRING_CONTENT tINTEGER tIMAGINARY
%token <FloatParseNode> tFLOAT  
%token <RationalParseNode> tRATIONAL
%token <RegexpParseNode>  tREGEXP_END
%type <RestArgParseNode> f_rest_arg
%type <ParseNode> singleton strings string string1 xstring regexp
%type <ParseNode> string_contents xstring_contents method_call
%type <Object> string_content
%type <ParseNode> regexp_contents
%type <ParseNode> words qwords word literal dsym cpath command_asgn command_call
%type <NumericParseNode> numeric simple_numeric 
%type <ParseNode> mrhs_arg
%type <ParseNode> compstmt bodystmt stmts stmt expr arg primary command 
%type <ParseNode> stmt_or_begin
%type <ParseNode> expr_value primary_value opt_else cases p_cases if_tail exc_var rel_expr
%type <ParseNode> call_args opt_ensure paren_args superclass
%type <ParseNode> command_args var_ref opt_paren_args block_call block_command
%type <ParseNode> command_rhs arg_rhs
%type <ParseNode> f_opt
%type <ParseNode> undef_list
%type <ParseNode> string_dvar backref
%type <ArgsParseNode> f_args f_args_any f_larglist block_param block_param_def opt_block_param
%type <Object> f_arglist
%type <ParseNode> mrhs mlhs_item mlhs_node arg_value case_body p_case_body exc_list aref_args
%type <ParseNode> lhs none args
%type <ListParseNode> qword_list word_list
%type <ListParseNode> f_arg f_optarg
%type <ListParseNode> f_marg_list symbol_list
%type <ListParseNode> qsym_list symbols qsymbols
   // FIXME: These are node until a better understanding of underlying type
%type <ArgsTailHolder> opt_args_tail opt_block_args_tail block_args_tail args_tail
%type <ParseNode> f_kw f_block_kw
%type <ListParseNode> f_block_kwarg f_kwarg
%type <HashParseNode> assoc_list
%type <HashParseNode> assocs
%type <ParseNodeTuple> assoc
%type <ListParseNode> mlhs_head mlhs_post
%type <ListParseNode> f_block_optarg
%type <BlockPassParseNode> opt_block_arg block_arg none_block_pass
%type <BlockArgParseNode> opt_f_block_arg f_block_arg
%type <IterParseNode> brace_block do_block cmd_brace_block brace_body do_body
%type <MultipleAsgnParseNode> mlhs mlhs_basic 
%type <RescueBodyParseNode> opt_rescue
%type <AssignableParseNode> var_lhs
%type <LiteralParseNode> fsym
%type <ParseNode> fitem
%type <ParseNode> f_arg_item
%type <ParseNode> bv_decls
%type <ParseNode> opt_bv_decl lambda_body 
%type <LambdaParseNode> lambda
%type <ParseNode> mlhs_inner f_block_opt for_var
%type <ParseNode> opt_call_args f_marg f_margs
%type <TruffleString> bvar
%type <TruffleString> reswords f_bad_arg relop
%type <TruffleString> rparen rbracket
%type <ParseNode> top_compstmt top_stmts top_stmt
%token <TruffleString> tSYMBOLS_BEG
%token <TruffleString> tQSYMBOLS_BEG
%token <TruffleString> tDSTAR
%token <TruffleString> tSTRING_DEND
%type <TruffleString> kwrest_mark f_kwrest f_label
%type <TruffleString> args_forward
%type <TruffleString> call_op call_op2
%type <ArgumentParseNode> f_arg_asgn
%type <FCallParseNode> fcall
%token <TruffleString> tLABEL_END
%type <SourceIndexLength> k_return k_class k_module

/*
 *    precedence table
 */

%nonassoc tLOWEST
%nonassoc tLBRACE_ARG

%nonassoc  modifier_if modifier_unless modifier_while modifier_until
%left  keyword_or keyword_and
%right keyword_not
%nonassoc keyword_defined
%right '=' tOP_ASGN
%left modifier_rescue
%right '?' ':'
%nonassoc tDOT2 tDOT3 tBDOT2 tBDOT3
%left  tOROP
%left  tANDOP
%nonassoc  tCMP tEQ tEQQ tNEQ tMATCH tNMATCH
%left  tGT tGEQ tLT tLEQ
%left  tPIPE tCARET
%left  tAMPER2
%left  tLSHFT tRSHFT
%left  tPLUS tMINUS
%left  tSTAR2 tDIVIDE tPERCENT
%right tUMINUS_NUM tUMINUS
%right tPOW
%right tBANG tTILDE tUPLUS

   //%token <Integer> tLAST_TOKEN

%%
program       : {
                  lexer.setState(EXPR_BEG);
                  support.initTopLocalVariables();
              } top_compstmt {
  // ENEBO: Removed !compile_for_eval which probably is to reduce warnings
                  if ($2 != null) {
                      /* last expression should not be void */
                      if ($2 instanceof BlockParseNode) {
                          support.checkUselessStatement($<BlockParseNode>2.getLast());
                      } else {
                          support.checkUselessStatement($2);
                      }
                  }
                  support.getResult().setAST(support.addRootNode($2));
              }

top_compstmt  : top_stmts opt_terms {
                  if ($1 instanceof BlockParseNode) {
                      support.checkUselessStatements($<BlockParseNode>1);
                  }
                  $$ = $1;
              }

top_stmts     : none
              | top_stmt {
                    $$ = support.newline_node($1, support.getPosition($1));
              }
              | top_stmts terms top_stmt {
                    $$ = support.appendToBlock($1, support.newline_node($3, support.getPosition($3)));
              }
              | error top_stmt {
                    $$ = $2;
              }

top_stmt      : stmt
              | keyword_BEGIN tLCURLY top_compstmt tRCURLY {
                    support.getResult().addBeginNode(new PreExe19ParseNode($1, support.getCurrentScope(), $3));
                    $$ = null;
              }

bodystmt      : compstmt opt_rescue opt_else opt_ensure {
                  ParseNode node = $1;

                  if ($2 != null) {
                      node = new RescueParseNode(support.getPosition($1), $1, $2, $3);
                  } else if ($3 != null) {
                      support.warn(support.getPosition($1), "else without rescue is useless");
                      node = support.appendToBlock($1, $3);
                  }
                  if ($4 != null) {
                      if (node != null) {
                          node = new EnsureParseNode(support.getPosition($1), support.makeNullNil(node), $4);
                      } else {
                          node = support.appendToBlock($4, NilImplicitParseNode.NIL);
                      }
                  }

                  support.fixpos(node, $1);
                  $$ = node;
                }

compstmt        : stmts opt_terms {
                    if ($1 instanceof BlockParseNode) {
                        support.checkUselessStatements($<BlockParseNode>1);
                    }
                    $$ = $1;
                }

stmts           : none
                | stmt_or_begin {
                    $$ = support.newline_node($1, support.getPosition($1));
                }
                | stmts terms stmt_or_begin {
                    $$ = support.appendToBlock($1, support.newline_node($3, support.getPosition($3)));
                }
                | error stmt {
                    $$ = $2;
                }

stmt_or_begin   : stmt {
                    $$ = $1;
                }
// FIXME: How can this new begin ever work?  is yyerror conditional in MRI?
                | keyword_begin {
                   support.yyerror("BEGIN is permitted only at toplevel");
                } tLCURLY top_compstmt tRCURLY {
                    $$ = new BeginParseNode($1, support.makeNullNil($2));
                }

stmt            : keyword_alias fitem {
                    lexer.setState(EXPR_FNAME|EXPR_FITEM);
                } fitem {
                    $$ = support.newAlias($1, $2, $4);
                }
                | keyword_alias tGVAR tGVAR {
                    $$ = new VAliasParseNode($1, support.symbolID($2), support.symbolID($3));
                }
                | keyword_alias tGVAR tBACK_REF {
                    $$ = new VAliasParseNode($1, support.symbolID($2), support.symbolID($<BackRefParseNode>3.getByteName()));
                }
                | keyword_alias tGVAR tNTH_REF {
                    support.yyerror("can't make alias for the number variables");
                }
                | keyword_undef undef_list {
                    $$ = $2;
                }
                | stmt modifier_if expr_value {
                    $$ = new IfParseNode(support.getPosition($1), support.getConditionNode($3), $1, null);
                    support.fixpos($<ParseNode>$, $3);
                }
                | stmt modifier_unless expr_value {
                    $$ = new IfParseNode(support.getPosition($1), support.getConditionNode($3), null, $1);
                    support.fixpos($<ParseNode>$, $3);
                }
                | stmt modifier_while expr_value {
                    if ($1 != null && $1 instanceof BeginParseNode) {
                        $$ = new WhileParseNode(support.getPosition($1), support.getConditionNode($3), $<BeginParseNode>1.getBodyNode(), false);
                    } else {
                        $$ = new WhileParseNode(support.getPosition($1), support.getConditionNode($3), $1, true);
                    }
                }
                | stmt modifier_until expr_value {
                    if ($1 != null && $1 instanceof BeginParseNode) {
                        $$ = new UntilParseNode(support.getPosition($1), support.getConditionNode($3), $<BeginParseNode>1.getBodyNode(), false);
                    } else {
                        $$ = new UntilParseNode(support.getPosition($1), support.getConditionNode($3), $1, true);
                    }
                }
                | stmt modifier_rescue stmt {
                    $$ = support.newRescueModNode($1, $3);
                }
                | keyword_END tLCURLY compstmt tRCURLY {
                    if (support.isInDef()) {
                        support.warn($1, "END in method; use at_exit");
                    }
                    $$ = new PostExeParseNode($1, $3);
                }
                | command_asgn
                | mlhs '=' command_call {
                    value_expr(lexer, $3);
                    $1.setValueNode($3);
                    $$ = $1;
                }
                | lhs '=' mrhs {
                    value_expr(lexer, $3);
                    $$ = support.node_assign($1, $3);
                }
                | mlhs '=' mrhs_arg modifier_rescue stmt {
                    value_expr(lexer, $3);
                    $$ = support.node_assign($1, support.newRescueModNode($3, $5));
                }
                | mlhs '=' mrhs_arg {
                    $<AssignableParseNode>1.setValueNode($3);
                    $$ = $1;
                    $1.setPosition(support.getPosition($1));
                }
                | expr

command_asgn    : lhs '=' command_rhs {
                    value_expr(lexer, $3);
                    $$ = support.node_assign($1, $3);
                }
                | var_lhs tOP_ASGN command_rhs {
                    value_expr(lexer, $3);

                    SourceIndexLength pos = $1.getPosition();
                    TruffleString asgnOp = $2;
                    if (asgnOp == TStringConstants.OR_OR) {
                        $1.setValueNode($3);
                        $$ = new OpAsgnOrParseNode(pos, support.gettable2($1), $1);
                    } else if (asgnOp == TStringConstants.AMPERSAND_AMPERSAND) {
                        $1.setValueNode($3);
                        $$ = new OpAsgnAndParseNode(pos, support.gettable2($1), $1);
                    } else {
                        $1.setValueNode(support.getOperatorCallNode(support.gettable2($1), asgnOp, $3));
                        $1.setPosition(pos);
                        $$ = $1;
                    }
                }
                | primary_value '[' opt_call_args rbracket tOP_ASGN command_rhs {
  // FIXME: arg_concat logic missing for opt_call_args
                    $$ = support.new_opElementAsgnNode($1, $5, $3, $6);
                }
                | primary_value call_op tIDENTIFIER tOP_ASGN command_rhs {
                    value_expr(lexer, $5);
                    $$ = support.newOpAsgn(support.getPosition($1), $1, $2, $5, $3, $4);
                }
                | primary_value call_op tCONSTANT tOP_ASGN command_rhs {
                    value_expr(lexer, $5);
                    $$ = support.newOpAsgn(support.getPosition($1), $1, $2, $5, $3, $4);
                }
                | primary_value tCOLON2 tCONSTANT tOP_ASGN command_rhs {
                    SourceIndexLength pos = $1.getPosition();
                    $$ = support.newOpConstAsgn(pos, support.new_colon2(pos, $1, $3), $4, $5);
                }

                | primary_value tCOLON2 tIDENTIFIER tOP_ASGN command_rhs {
                    value_expr(lexer, $5);
                    $$ = support.newOpAsgn(support.getPosition($1), $1, $2, $5, $3, $4);
                }
                | backref tOP_ASGN command_rhs {
                    support.backrefAssignError($1);
                }

command_rhs     : command_call %prec tOP_ASGN {
                    value_expr(lexer, $1);
                    $$ = $1;
                }
                | command_call modifier_rescue stmt {
                    value_expr(lexer, $1);
                    $$ = support.newRescueModNode($1, $3);
                }
                | command_asgn
 

// Node:expr *CURRENT* all but arg so far
expr            : command_call
                | expr keyword_and expr {
                    $$ = support.newAndNode(support.getPosition($1), $1, $3);
                }
                | expr keyword_or expr {
                    $$ = support.newOrNode(support.getPosition($1), $1, $3);
                }
                | keyword_not opt_nl expr {
                    $$ = support.getOperatorCallNode(support.getConditionNode($3), TStringConstants.BANG);
                }
                | tBANG command_call {
                    $$ = support.getOperatorCallNode(support.getConditionNode($2), $1);
                }
                | arg

expr_value      : expr {
                    value_expr(lexer, $1);
                }

// Node:command - call with or with block on end [!null]
command_call    : command
                | block_command

// Node:block_command - A call with a block (foo.bar {...}, foo::bar {...}, bar {...}) [!null]
block_command   : block_call
                | block_call call_op2 operation2 command_args {
                    $$ = support.new_call($1, $2, $3, $4, null);
                }

// :brace_block - [!null]
cmd_brace_block : tLBRACE_ARG brace_body tRCURLY {
                    $$ = $2;
                }

fcall           : operation {
                    $$ = support.new_fcall($1);
                }

// Node:command - fcall/call/yield/super [!null]
command        : fcall command_args %prec tLOWEST {
                    support.frobnicate_fcall_args($1, $2, null);
                    $$ = $1;
                }
                | fcall command_args cmd_brace_block {
                    support.frobnicate_fcall_args($1, $2, $3);
                    $$ = $1;
                }
                | primary_value call_op operation2 command_args %prec tLOWEST {
                    $$ = support.new_call($1, $2, $3, $4, null);
                }
                | primary_value call_op operation2 command_args cmd_brace_block {
                    $$ = support.new_call($1, $2, $3, $4, $5); 
                }
                | primary_value tCOLON2 operation2 command_args %prec tLOWEST {
                    $$ = support.new_call($1, $3, $4, null);
                }
                | primary_value tCOLON2 operation2 command_args cmd_brace_block {
                    $$ = support.new_call($1, $3, $4, $5);
                }
                | keyword_super command_args {
                    $$ = support.new_super($1, $2);
                }
                | keyword_yield command_args {
                    $$ = support.new_yield($1, $2);
                }
                | k_return call_args {
                    $$ = new ReturnParseNode($1, support.ret_args($2, $1));
                }
                | keyword_break call_args {
                    $$ = new BreakParseNode($1, support.ret_args($2, $1));
                }
                | keyword_next call_args {
                    $$ = new NextParseNode($1, support.ret_args($2, $1));
                }

// MultipleAssigNode:mlhs - [!null]
mlhs            : mlhs_basic
                | tLPAREN mlhs_inner rparen {
                    $$ = $2;
                }

// MultipleAssignNode:mlhs_entry - mlhs w or w/o parens [!null]
mlhs_inner      : mlhs_basic {
                    $$ = $1;
                }
                | tLPAREN mlhs_inner rparen {
                    $$ = new MultipleAsgnParseNode($1, support.newArrayNode($1, $2), null, null);
                }

// MultipleAssignNode:mlhs_basic - multiple left hand side (basic because used in multiple context) [!null]
mlhs_basic      : mlhs_head {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, null, null);
                }
                | mlhs_head mlhs_item {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1.add($2), null, null);
                }
                | mlhs_head tSTAR mlhs_node {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, $3, (ListParseNode) null);
                }
                | mlhs_head tSTAR mlhs_node ',' mlhs_post {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, $3, $5);
                }
                | mlhs_head tSTAR {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, new StarParseNode(lexer.getPosition()), null);
                }
                | mlhs_head tSTAR ',' mlhs_post {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, new StarParseNode(lexer.getPosition()), $4);
                }
                | tSTAR mlhs_node {
                    $$ = new MultipleAsgnParseNode($2.getPosition(), null, $2, null);
                }
                | tSTAR mlhs_node ',' mlhs_post {
                    $$ = new MultipleAsgnParseNode($2.getPosition(), null, $2, $4);
                }
                | tSTAR {
                      $$ = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), null);
                }
                | tSTAR ',' mlhs_post {
                      $$ = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), $3);
                }

mlhs_item       : mlhs_node
                | tLPAREN mlhs_inner rparen {
                    $$ = $2;
                }

// Set of mlhs terms at front of mlhs (a, *b, d, e = arr  # a is head)
mlhs_head       : mlhs_item ',' {
                    $$ = support.newArrayNode($1.getPosition(), $1);
                }
                | mlhs_head mlhs_item ',' {
                    $$ = $1.add($2);
                }

// Set of mlhs terms at end of mlhs (a, *b, d, e = arr  # d,e is post)
mlhs_post       : mlhs_item {
                    $$ = support.newArrayNode($1.getPosition(), $1);
                }
                | mlhs_post ',' mlhs_item {
                    $$ = $1.add($3);
                }

mlhs_node       : /*mri:user_variable*/ tIDENTIFIER {
                    $$ = support.assignableLabelOrIdentifier($1, null);
                }
                | tIVAR {
                   $$ = new InstAsgnParseNode(lexer.tokline, support.symbolID($1), NilImplicitParseNode.NIL);
                }
                | tGVAR {
                   $$ = new GlobalAsgnParseNode(lexer.tokline, support.symbolID($1), NilImplicitParseNode.NIL);
                }
                | tCONSTANT {
                    if (support.isInDef()) support.compile_error("dynamic constant assignment");
                    $$ = new ConstDeclParseNode(lexer.tokline, support.symbolID($1), null, NilImplicitParseNode.NIL);
                }
                | tCVAR {
                    $$ = new ClassVarAsgnParseNode(lexer.tokline, support.symbolID($1), NilImplicitParseNode.NIL);
                } /*mri:user_variable*/
                | /*mri:keyword_variable*/ keyword_nil {
                    support.compile_error("Can't assign to nil");
                    $$ = null;
                }
                | keyword_self {
                    support.compile_error("Can't change the value of self");
                    $$ = null;
                }
                | keyword_true {
                    support.compile_error("Can't assign to true");
                    $$ = null;
                }
                | keyword_false {
                    support.compile_error("Can't assign to false");
                    $$ = null;
                }
                | keyword__FILE__ {
                    support.compile_error("Can't assign to __FILE__");
                    $$ = null;
                }
                | keyword__LINE__ {
                    support.compile_error("Can't assign to __LINE__");
                    $$ = null;
                }
                | keyword__ENCODING__ {
                    support.compile_error("Can't assign to __ENCODING__");
                    $$ = null;
                } /*mri:keyword_variable*/
                | primary_value '[' opt_call_args rbracket {
                    $$ = support.aryset($1, $3);
                }
                | primary_value call_op tIDENTIFIER {
                    $$ = support.attrset($1, $2, $3);
                }
                | primary_value tCOLON2 tIDENTIFIER {
                    $$ = support.attrset($1, $3);
                }
                | primary_value call_op tCONSTANT {
                    $$ = support.attrset($1, $2, $3);
                }
                | primary_value tCOLON2 tCONSTANT {
                    if (support.isInDef()) support.yyerror("dynamic constant assignment");

                    SourceIndexLength position = support.getPosition($1);

                    $$ = new ConstDeclParseNode(position, (TruffleString) null, support.new_colon2(position, $1, $3), NilImplicitParseNode.NIL);
                }
                | tCOLON3 tCONSTANT {
                    if (support.isInDef()) {
                        support.yyerror("dynamic constant assignment");
                    }

                    SourceIndexLength position = lexer.tokline;

                    $$ = new ConstDeclParseNode(position, (TruffleString) null, support.new_colon3(position, $2), NilImplicitParseNode.NIL);
                }
                | backref {
                    support.backrefAssignError($1);
                }

// [!null or throws]
lhs             : /*mri:user_variable*/ tIDENTIFIER {
                    $$ = support.assignableLabelOrIdentifier($1, null);
                }
                | tIVAR {
                    $$ = new InstAsgnParseNode(lexer.tokline, support.symbolID($1), NilImplicitParseNode.NIL);
                }
                | tGVAR {
                    $$ = new GlobalAsgnParseNode(lexer.tokline, support.symbolID($1), NilImplicitParseNode.NIL);
                }
                | tCONSTANT {
                    if (support.isInDef()) support.compile_error("dynamic constant assignment");

                    $$ = new ConstDeclParseNode(lexer.tokline, support.symbolID($1), null, NilImplicitParseNode.NIL);
                }
                | tCVAR {
                    $$ = new ClassVarAsgnParseNode(lexer.tokline, support.symbolID($1), NilImplicitParseNode.NIL);
                } /*mri:user_variable*/
                | /*mri:keyword_variable*/ keyword_nil {
                    support.compile_error("Can't assign to nil");
                    $$ = null;
                }
                | keyword_self {
                    support.compile_error("Can't change the value of self");
                    $$ = null;
                }
                | keyword_true {
                    support.compile_error("Can't assign to true");
                    $$ = null;
                }
                | keyword_false {
                    support.compile_error("Can't assign to false");
                    $$ = null;
                }
                | keyword__FILE__ {
                    support.compile_error("Can't assign to __FILE__");
                    $$ = null;
                }
                | keyword__LINE__ {
                    support.compile_error("Can't assign to __LINE__");
                    $$ = null;
                }
                | keyword__ENCODING__ {
                    support.compile_error("Can't assign to __ENCODING__");
                    $$ = null;
                } /*mri:keyword_variable*/
                | primary_value '[' opt_call_args rbracket {
                    $$ = support.aryset($1, $3);
                }
                | primary_value call_op tIDENTIFIER {
                    $$ = support.attrset($1, $2, $3);
                }
                | primary_value tCOLON2 tIDENTIFIER {
                    $$ = support.attrset($1, $3);
                }
                | primary_value call_op tCONSTANT {
                    $$ = support.attrset($1, $2, $3);
                }
                | primary_value tCOLON2 tCONSTANT {
                    if (support.isInDef()) {
                        support.yyerror("dynamic constant assignment");
                    }

                    SourceIndexLength position = support.getPosition($1);

                    $$ = new ConstDeclParseNode(position, (TruffleString) null, support.new_colon2(position, $1, $3), NilImplicitParseNode.NIL);
                }
                | tCOLON3 tCONSTANT {
                    if (support.isInDef()) {
                        support.yyerror("dynamic constant assignment");
                    }

                    SourceIndexLength position = lexer.tokline;

                    $$ = new ConstDeclParseNode(position, (TruffleString) null, support.new_colon3(position, $2), NilImplicitParseNode.NIL);
                }
                | backref {
                    support.backrefAssignError($1);
                }

cname           : tIDENTIFIER {
                    support.yyerror("class/module name must be CONSTANT");
                }
                | tCONSTANT {
                   $$ = $1;
                }

cpath           : tCOLON3 cname {
                    $$ = support.new_colon3(lexer.tokline, $2);
                }
                | cname {
                    $$ = support.new_colon2(lexer.tokline, null, $1);
                }
                | primary_value tCOLON2 cname {
                    $$ = support.new_colon2(support.getPosition($1), $1, $3);
                }

// ByteList:fname - A function name [!null]
fname          : tIDENTIFIER {
                   $$ = $1;
               }
               | tCONSTANT {
                   $$ = $1;
               }
               | tFID  {
                   $$ = $1;
               }
               | op {
                   lexer.setState(EXPR_ENDFN);
                   $$ = $1;
               }
               | reswords {
                   lexer.setState(EXPR_ENDFN);
                   $$ = $1;
               }

// LiteralNode:fsym
fsym           : fname {
                   $$ = new LiteralParseNode(lexer.getPosition(), support.symbolID($1));
               }
               | symbol {
                   $$ = new LiteralParseNode(lexer.getPosition(), support.symbolID($1));
               }

// Node:fitem
fitem           : fsym {  // LiteralNode
                    $$ = $1;
                }
                | dsym {  // SymbolNode/DSymbolNode
                    $$ = $1;
                }

undef_list      : fitem {
                    $$ = support.newUndef($1.getPosition(), $1);
                }
                | undef_list ',' {
                    lexer.setState(EXPR_FNAME|EXPR_FITEM);
                } fitem {
                    $$ = support.appendToBlock($1, support.newUndef($1.getPosition(), $4));
                }

// ByteList:op
 op              : tPIPE {
                     $$ = $1;
                 }
                 | tCARET {
                     $$ = $1;
                 }
                 | tAMPER2 {
                     $$ = $1;
                 }
                 | tCMP {
                     $$ = $1;
                 }
                 | tEQ {
                     $$ = $1;
                 }
                 | tEQQ {
                     $$ = $1;
                 }
                 | tMATCH {
                     $$ = $1;
                 }
                 | tNMATCH {
                     $$ = $1;
                 }
                 | tGT {
                     $$ = $1;
                 }
                 | tGEQ {
                     $$ = $1;
                 }
                 | tLT {
                     $$ = $1;
                 }
                 | tLEQ {
                     $$ = $1;
                 }
                 | tNEQ {
                     $$ = $1;
                 }
                 | tLSHFT {
                     $$ = $1;
                 }
                 | tRSHFT{
                     $$ = $1;
                 }
                 | tDSTAR {
                     $$ = $1;
                 }
                 | tPLUS {
                     $$ = $1;
                 }
                 | tMINUS {
                     $$ = $1;
                 }
                 | tSTAR2 {
                     $$ = $1;
                 }
                 | tSTAR {
                     $$ = $1;
                 }
                 | tDIVIDE {
                     $$ = $1;
                 }
                 | tPERCENT {
                     $$ = $1;
                 }
                 | tPOW {
                     $$ = $1;
                 }
                 | tBANG {
                     $$ = $1;
                 }
                 | tTILDE {
                     $$ = $1;
                 }
                 | tUPLUS {
                     $$ = $1;
                 }
                 | tUMINUS {
                     $$ = $1;
                 }
                 | tAREF {
                     $$ = $1;
                 }
                 | tASET {
                     $$ = $1;
                 }
                 | tBACK_REF2 {
                     $$ = $1;
                 }
 
// String:op
reswords        : keyword__LINE__ {
                    $$ = RubyLexer.Keyword.__LINE__.bytes;
                }
                | keyword__FILE__ {
                    $$ = RubyLexer.Keyword.__FILE__.bytes;
                }
                | keyword__ENCODING__ {
                    $$ = RubyLexer.Keyword.__ENCODING__.bytes;
                }
                | keyword_BEGIN {
                    $$ = RubyLexer.Keyword.LBEGIN.bytes;
                }
                | keyword_END {
                    $$ = RubyLexer.Keyword.LEND.bytes;
                }
                | keyword_alias {
                    $$ = RubyLexer.Keyword.ALIAS.bytes;
                }
                | keyword_and {
                    $$ = RubyLexer.Keyword.AND.bytes;
                }
                | keyword_begin {
                    $$ = RubyLexer.Keyword.BEGIN.bytes;
                }
                | keyword_break {
                    $$ = RubyLexer.Keyword.BREAK.bytes;
                }
                | keyword_case {
                    $$ = RubyLexer.Keyword.CASE.bytes;
                }
                | keyword_class {
                    $$ = RubyLexer.Keyword.CLASS.bytes;
                }
                | keyword_def {
                    $$ = RubyLexer.Keyword.DEF.bytes;
                }
                | keyword_defined {
                    $$ = RubyLexer.Keyword.DEFINED_P.bytes;
                }
                | keyword_do {
                    $$ = RubyLexer.Keyword.DO.bytes;
                }
                | keyword_else {
                    $$ = RubyLexer.Keyword.ELSE.bytes;
                }
                | keyword_elsif {
                    $$ = RubyLexer.Keyword.ELSIF.bytes;
                }
                | keyword_end {
                    $$ = RubyLexer.Keyword.END.bytes;
                }
                | keyword_ensure {
                    $$ = RubyLexer.Keyword.ENSURE.bytes;
                }
                | keyword_false {
                    $$ = RubyLexer.Keyword.FALSE.bytes;
                }
                | keyword_for {
                    $$ = RubyLexer.Keyword.FOR.bytes;
                }
                | keyword_in {
                    $$ = RubyLexer.Keyword.IN.bytes;
                }
                | keyword_module {
                    $$ = RubyLexer.Keyword.MODULE.bytes;
                }
                | keyword_next {
                    $$ = RubyLexer.Keyword.NEXT.bytes;
                }
                | keyword_nil {
                    $$ = RubyLexer.Keyword.NIL.bytes;
                }
                | keyword_not {
                    $$ = RubyLexer.Keyword.NOT.bytes;
                }
                | keyword_or {
                    $$ = RubyLexer.Keyword.OR.bytes;
                }
                | keyword_redo {
                    $$ = RubyLexer.Keyword.REDO.bytes;
                }
                | keyword_rescue {
                    $$ = RubyLexer.Keyword.RESCUE.bytes;
                }
                | keyword_retry {
                    $$ = RubyLexer.Keyword.RETRY.bytes;
                }
                | keyword_return {
                    $$ = RubyLexer.Keyword.RETURN.bytes;
                }
                | keyword_self {
                    $$ = RubyLexer.Keyword.SELF.bytes;
                }
                | keyword_super {
                    $$ = RubyLexer.Keyword.SUPER.bytes;
                }
                | keyword_then {
                    $$ = RubyLexer.Keyword.THEN.bytes;
                }
                | keyword_true {
                    $$ = RubyLexer.Keyword.TRUE.bytes;
                }
                | keyword_undef {
                    $$ = RubyLexer.Keyword.UNDEF.bytes;
                }
                | keyword_when {
                    $$ = RubyLexer.Keyword.WHEN.bytes;
                }
                | keyword_yield {
                    $$ = RubyLexer.Keyword.YIELD.bytes;
                }
                | keyword_if {
                    $$ = RubyLexer.Keyword.IF.bytes;
                }
                | keyword_unless {
                    $$ = RubyLexer.Keyword.UNLESS.bytes;
                }
                | keyword_while {
                    $$ = RubyLexer.Keyword.WHILE.bytes;
                }
                | keyword_until {
                    $$ = RubyLexer.Keyword.UNTIL.bytes;
                }
                | modifier_rescue {
                    $$ = RubyLexer.Keyword.RESCUE.bytes;
                }

arg             : lhs '=' arg_rhs {
                    $$ = support.node_assign($1, $3);
                    // FIXME: Consider fixing node_assign itself rather than single case
                    $<ParseNode>$.setPosition(support.getPosition($1));
                }
                | var_lhs tOP_ASGN arg_rhs {
                    value_expr(lexer, $3);

                    SourceIndexLength pos = $1.getPosition();
                    TruffleString asgnOp = $2;
                    if (asgnOp == TStringConstants.OR_OR) {
                        $1.setValueNode($3);
                        $$ = new OpAsgnOrParseNode(pos, support.gettable2($1), $1);
                    } else if (asgnOp == TStringConstants.AMPERSAND_AMPERSAND) {
                        $1.setValueNode($3);
                        $$ = new OpAsgnAndParseNode(pos, support.gettable2($1), $1);
                    } else {
                        $1.setValueNode(support.getOperatorCallNode(support.gettable2($1), asgnOp, $3));
                        $1.setPosition(pos);
                        $$ = $1;
                    }
                }
                | primary_value '[' opt_call_args rbracket tOP_ASGN arg {
  // FIXME: arg_concat missing for opt_call_args
                    $$ = support.new_opElementAsgnNode($1, $5, $3, $6);
                }
                | primary_value call_op tIDENTIFIER tOP_ASGN arg_rhs {
                    value_expr(lexer, $5);
                    $$ = support.newOpAsgn(support.getPosition($1), $1, $2, $5, $3, $4);
                }
                | primary_value call_op tCONSTANT tOP_ASGN arg_rhs {
                    value_expr(lexer, $5);
                    $$ = support.newOpAsgn(support.getPosition($1), $1, $2, $5, $3, $4);
                }
                | primary_value tCOLON2 tIDENTIFIER tOP_ASGN arg_rhs {
                    value_expr(lexer, $5);
                    $$ = support.newOpAsgn(support.getPosition($1), $1, $2, $5, $3, $4);
                }
                | primary_value tCOLON2 tCONSTANT tOP_ASGN arg_rhs {
                    SourceIndexLength pos = support.getPosition($1);
                    $$ = support.newOpConstAsgn(pos, support.new_colon2(pos, $1, $3), $4, $5);
                }
                | tCOLON3 tCONSTANT tOP_ASGN arg_rhs {
                    SourceIndexLength pos = lexer.getPosition();
                    $$ = support.newOpConstAsgn(pos, new Colon3ParseNode(pos, support.symbolID($2)), $3, $4);
                }
                | backref tOP_ASGN arg_rhs {
                    support.backrefAssignError($1);
                }
                | arg tDOT2 arg {
                    value_expr(lexer, $1);
                    value_expr(lexer, $3);
    
                    boolean isLiteral = $1 instanceof FixnumParseNode && $3 instanceof FixnumParseNode;
                    $$ = new DotParseNode(support.getPosition($1), support.makeNullNil($1), support.makeNullNil($3), false, isLiteral);
                }
                | arg tDOT2 {
                    support.checkExpression($1);

                    boolean isLiteral = $1 instanceof FixnumParseNode;
                    $$ = new DotParseNode(support.getPosition($1), support.makeNullNil($1), NilImplicitParseNode.NIL, false, isLiteral);
                }
                | tBDOT2 arg {
                    value_expr(lexer, $2);

                    boolean isLiteral = $2 instanceof FixnumParseNode;
                    $$ = new DotParseNode(support.getPosition($2), NilImplicitParseNode.NIL, support.makeNullNil($2), false, isLiteral);
                }
                | arg tDOT3 arg {
                    value_expr(lexer, $1);
                    value_expr(lexer, $3);

                    boolean isLiteral = $1 instanceof FixnumParseNode && $3 instanceof FixnumParseNode;
                    $$ = new DotParseNode(support.getPosition($1), support.makeNullNil($1), support.makeNullNil($3), true, isLiteral);
                }
                | arg tDOT3 {
                    support.checkExpression($1);

                    boolean isLiteral = $1 instanceof FixnumParseNode;
                    $$ = new DotParseNode(support.getPosition($1), support.makeNullNil($1), NilImplicitParseNode.NIL, true, isLiteral);
                }
                | tBDOT3 arg {
                    value_expr(lexer, $2);

                    boolean isLiteral = $2 instanceof FixnumParseNode;
                    $$ = new DotParseNode(support.getPosition($2), NilImplicitParseNode.NIL, support.makeNullNil($2), true, isLiteral);
                }
                | arg tPLUS arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tMINUS arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tSTAR2 arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tDIVIDE arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tPERCENT arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tPOW arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | tUMINUS_NUM simple_numeric tPOW arg {
                    $$ = support.getOperatorCallNode(support.getOperatorCallNode($2, $3, $4, lexer.getPosition()), $1);
                }
                | tUPLUS arg {
                    $$ = support.getOperatorCallNode($2, $1);
                }
                | tUMINUS arg {
                    $$ = support.getOperatorCallNode($2, $1);
                }
                | arg tPIPE arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tCARET arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tAMPER2 arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tCMP arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | rel_expr   %prec tCMP {
                    $$ = $1;
                }
                | arg tEQ arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tEQQ arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tNEQ arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tMATCH arg {
                    $$ = support.getMatchNode($1, $3);
                  /* ENEBO
                        $$ = match_op($1, $3);
                        if (nd_type($1) == NODE_LIT && TYPE($1->nd_lit) == T_REGEXP) {
                            $$ = reg_named_capture_assign($1->nd_lit, $$);
                        }
                  */
                }
                | arg tNMATCH arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | tBANG arg {
                    $$ = support.getOperatorCallNode(support.getConditionNode($2), $1);
                }
                | tTILDE arg {
                    $$ = support.getOperatorCallNode($2, $1);
                }
                | arg tLSHFT arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tRSHFT arg {
                    $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | arg tANDOP arg {
                    $$ = support.newAndNode($1.getPosition(), $1, $3);
                }
                | arg tOROP arg {
                    $$ = support.newOrNode($1.getPosition(), $1, $3);
                }
                | keyword_defined opt_nl arg {
                    $$ = support.new_defined($1, $3);
                }
                | arg '?' arg opt_nl ':' arg {
                    value_expr(lexer, $1);
                    $$ = new IfParseNode(support.getPosition($1), support.getConditionNode($1), $3, $6);
                }
                | primary {
                    $$ = $1;
                }
 
relop           : tGT {
                    $$ = $1;
                }
                | tLT  {
                    $$ = $1;
                }
                | tGEQ {
                     $$ = $1;
                }
                | tLEQ {
                     $$ = $1;
                }

rel_expr        : arg relop arg   %prec tGT {
                     $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
                | rel_expr relop arg   %prec tGT {
                     support.warning(lexer.getPosition(), "comparison '" + $2.toJavaStringUncached() + "' after comparison");
                     $$ = support.getOperatorCallNode($1, $2, $3, lexer.getPosition());
                }
 
arg_value       : arg {
                    value_expr(lexer, $1);
                    $$ = support.makeNullNil($1);
                }

aref_args       : none
                | args trailer {
                    $$ = $1;
                }
                | args ',' assocs trailer {
                    $3.setKeywordArguments(true);
                    $$ = support.arg_append($1, support.remove_duplicate_keys($3));
                }
                | assocs trailer {
                    $1.setKeywordArguments(true);
                    $$ = support.newArrayNode($1.getPosition(), support.remove_duplicate_keys($1));
                }

arg_rhs         : arg %prec tOP_ASGN {
                    value_expr(lexer, $1);
                    $$ = $1;
                }
                | arg modifier_rescue arg {
                    value_expr(lexer, $1);
                    $$ = support.newRescueModNode($1, $3);
                }

paren_args      : tLPAREN2 opt_call_args rparen {
                    $$ = $2;
                    if ($$ != null) $<ParseNode>$.setPosition($1);
                }
                | tLPAREN2 args ',' args_forward rparen {
                    SourceIndexLength position = support.getPosition(null);
                    // NOTE(norswap, 02 Jun 2021): location (0) arg is unused
                    SplatParseNode splat = support.newSplatNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_REST_VAR));
                    HashParseNode kwrest = new HashParseNode(position, support.createKeyValue(null, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_KWREST_VAR)));
                    kwrest.setKeywordArguments(true);
                    BlockPassParseNode block = new BlockPassParseNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_BLOCK_VAR));
                    $$ = support.arg_concat(support.getPosition($2), $2, splat);
                    $$ = support.arg_append((ParseNode) $$, kwrest);
                    $$ = support.arg_blk_pass((ParseNode) $$, block);
                }
                | tLPAREN2 args_forward rparen {
                    SourceIndexLength position = support.getPosition(null);
                    // NOTE(norswap, 06 Nov 2020): location (0) arg is unused
                    SplatParseNode splat = support.newSplatNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_REST_VAR));
                    HashParseNode kwrest = new HashParseNode(position, support.createKeyValue(null, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_KWREST_VAR)));
                    kwrest.setKeywordArguments(true);
                    BlockPassParseNode block = new BlockPassParseNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_BLOCK_VAR));
                    $$ = support.arg_append(splat, kwrest);
                    $$ = support.arg_blk_pass((ParseNode) $$, block);
                }

opt_paren_args  : none | paren_args

opt_call_args   : none
                | call_args
                | args ',' {
                    $$ = $1;
                }
                | args ',' assocs ',' {
                    $3.setKeywordArguments(true);
                    $$ = support.arg_append($1, support.remove_duplicate_keys($3));
                }
                | assocs ',' {
                    $1.setKeywordArguments(true);
                    $$ = support.newArrayNode($1.getPosition(), support.remove_duplicate_keys($1));
                }
   

// [!null] - ArgsCatNode, SplatNode, ArrayNode, HashNode, BlockPassNode
call_args       : command {
                    value_expr(lexer, $1);
                    $$ = support.newArrayNode(support.getPosition($1), $1);
                }
                | args opt_block_arg {
                    $$ = support.arg_blk_pass($1, $2);
                }
                | assocs opt_block_arg {
                    $1.setKeywordArguments(true);
                    $$ = support.newArrayNode($1.getPosition(), support.remove_duplicate_keys($1));
                    $$ = support.arg_blk_pass((ParseNode)$$, $2);
                }
                | args ',' assocs opt_block_arg {
                    $3.setKeywordArguments(true);
                    $$ = support.arg_append($1, support.remove_duplicate_keys($3));
                    $$ = support.arg_blk_pass((ParseNode)$$, $4);
                }
                | block_arg {
                }

// [!null] - ArgsCatNode, SplatNode, ArrayNode, HashNode, BlockPassNode
command_args    : /* none */ {
                    $$ = lexer.getCmdArgumentState().getStack();
                    lexer.getCmdArgumentState().begin();
                } call_args {
                    lexer.getCmdArgumentState().reset($<Long>1.longValue());
                    $$ = $2;
                }

block_arg       : tAMPER arg_value {
                    $$ = new BlockPassParseNode(support.getPosition($2), $2);
                }

opt_block_arg   : ',' block_arg {
                    $$ = $2;
                }
                | none_block_pass

// [!null]
args            : arg_value { // ArrayNode
                    SourceIndexLength pos = $1 == null ? lexer.getPosition() : $1.getPosition();
                    $$ = support.newArrayNode(pos, $1);
                }
                | tSTAR arg_value { // SplatNode
                    $$ = support.newSplatNode(support.getPosition($2), $2);
                }
                | args ',' arg_value { // ArgsCatNode, SplatNode, ArrayNode
                    ParseNode node = support.splat_array($1);

                    if (node != null) {
                        $$ = support.list_append(node, $3);
                    } else {
                        $$ = support.arg_append($1, $3);
                    }
                }
                | args ',' tSTAR arg_value { // ArgsCatNode, SplatNode, ArrayNode
                    ParseNode node = null;

                    // FIXME: lose syntactical elements here (and others like this)
                    if ($4 instanceof ArrayParseNode &&
                        (node = support.splat_array($1)) != null) {
                        $$ = support.list_concat(node, $4);
                    } else {
                        $$ = support.arg_concat(support.getPosition($1), $1, $4);
                    }
                }

mrhs_arg        : mrhs {
                    $$ = $1;
                }
                | arg_value {
                    $$ = $1;
                }


mrhs            : args ',' arg_value {
                    ParseNode node = support.splat_array($1);

                    if (node != null) {
                        $$ = support.list_append(node, $3);
                    } else {
                        $$ = support.arg_append($1, $3);
                    }
                }
                | args ',' tSTAR arg_value {
                    ParseNode node = null;

                    if ($4 instanceof ArrayParseNode &&
                        (node = support.splat_array($1)) != null) {
                        $$ = support.list_concat(node, $4);
                    } else {
                        $$ = support.arg_concat($1.getPosition(), $1, $4);
                    }
                }
                | tSTAR arg_value {
                     $$ = support.newSplatNode(support.getPosition($2), $2);
                }

primary         : literal
                | strings
                | xstring
                | regexp
                | words
                | qwords
                | symbols { 
                     $$ = $1; // FIXME: Why complaining without $$ = $1;
                }
                | qsymbols {
                     $$ = $1; // FIXME: Why complaining without $$ = $1;
                }
                | var_ref
                | backref
                | tFID {
                     $$ = support.new_fcall($1);
                }
                | keyword_begin {
                    $$ = lexer.getCmdArgumentState().getStack();
                    lexer.getCmdArgumentState().reset();
                } bodystmt keyword_end {
                    lexer.getCmdArgumentState().reset($<Long>2.longValue());
                    $$ = new BeginParseNode($1, support.makeNullNil($3));
                }
                | tLPAREN_ARG {
                    lexer.setState(EXPR_ENDARG);
                } rparen {
                    $$ = null; //FIXME: Should be implicit nil?
                }
                | tLPAREN_ARG {
                    $$ = lexer.getCmdArgumentState().getStack();
                    lexer.getCmdArgumentState().reset();
                } stmt {
                    lexer.setState(EXPR_ENDARG); 
                } rparen {
                    lexer.getCmdArgumentState().reset($<Long>2.longValue());
                    $$ = $3;
                }
                | tLPAREN compstmt tRPAREN {
                    if ($2 != null) {
                        // compstmt position includes both parens around it
                        $2.setPosition($1);
                        $$ = $2;
                    } else {
                        $$ = new NilParseNode($1);
                    }
                }
                | primary_value tCOLON2 tCONSTANT {
                    $$ = support.new_colon2(support.getPosition($1), $1, $3);
                }
                | tCOLON3 tCONSTANT {
                    $$ = support.new_colon3(lexer.tokline, $2);
                }
                | tLBRACK aref_args tRBRACK {
                    SourceIndexLength position = support.getPosition($2);
                    if ($2 == null) {
                        $$ = new ZArrayParseNode(position); /* zero length array */
                    } else {
                        $$ = $2;
                    }
                }
                | tLBRACE assoc_list tRCURLY {
                    $$ = $2;
                }
                | k_return {
                    $$ = new ReturnParseNode($1, NilImplicitParseNode.NIL);
                }
                | keyword_yield tLPAREN2 call_args rparen {
                    $$ = support.new_yield($1, $3);
                }
                | keyword_yield tLPAREN2 rparen {
                    $$ = new YieldParseNode($1, null);
                }
                | keyword_yield {
                    $$ = new YieldParseNode($1, null);
                }
                | keyword_defined opt_nl tLPAREN2 expr rparen {
                    $$ = support.new_defined($1, $4);
                }
                | keyword_not tLPAREN2 expr rparen {
                    $$ = support.getOperatorCallNode(support.getConditionNode($3), TStringConstants.BANG);
                }
                | keyword_not tLPAREN2 rparen {
                    $$ = support.getOperatorCallNode(NilImplicitParseNode.NIL, TStringConstants.BANG);
                }
                | fcall brace_block {
                    support.frobnicate_fcall_args($1, null, $2);
                    $$ = $1;                    
                }
                | method_call
                | method_call brace_block {
                    if ($1 != null && 
                          $<BlockAcceptingParseNode>1.getIterNode() instanceof BlockPassParseNode) {
                          lexer.compile_error(PID.BLOCK_ARG_AND_BLOCK_GIVEN, "Both block arg and actual block given.");
                    }
                    $$ = $<BlockAcceptingParseNode>1.setIterNode($2);
                    $<ParseNode>$.setPosition($1.getPosition());
                }
                | tLAMBDA lambda {
                    $$ = $2;
                }
                | keyword_if expr_value then compstmt if_tail keyword_end {
                    $$ = new IfParseNode($1, support.getConditionNode($2), $4, $5);
                }
                | keyword_unless expr_value then compstmt opt_else keyword_end {
                    $$ = new IfParseNode($1, support.getConditionNode($2), $5, $4);
                }
                | keyword_while {
                    lexer.getConditionState().begin();
                } expr_value do {
                    lexer.getConditionState().end();
                } compstmt keyword_end {
                    ParseNode body = support.makeNullNil($6);
                    $$ = new WhileParseNode($1, support.getConditionNode($3), body);
                }
                | keyword_until {
                  lexer.getConditionState().begin();
                } expr_value do {
                  lexer.getConditionState().end();
                } compstmt keyword_end {
                    ParseNode body = support.makeNullNil($6);
                    $$ = new UntilParseNode($1, support.getConditionNode($3), body);
                }
                | keyword_case expr_value opt_terms case_body keyword_end {
                    $$ = support.newCaseNode($1, $2, $4);
                }
                | keyword_case opt_terms case_body keyword_end {
                    $$ = support.newCaseNode($1, null, $3);
                }
                | keyword_case expr_value opt_terms p_case_body keyword_end {
                    $$ = support.newCaseInNode($1, $2, $4);
                }
                | keyword_for for_var keyword_in {
                    lexer.getConditionState().begin();
                } expr_value do {
                    lexer.getConditionState().end();
                } compstmt keyword_end {
                      // ENEBO: Lots of optz in 1.9 parser here
                    $$ = new ForParseNode($1, $2, $8, $5, support.getCurrentScope());
                }
                | k_class cpath superclass {
                    if (support.isInDef()) {
                        support.yyerror("class definition in method body");
                    }
                    support.pushLocalScope();
                    $$ = support.isInClass(); // MRI reuses $1 but we use the value for position.
                    support.setIsInClass(true);
                } bodystmt keyword_end {
                    ParseNode body = support.makeNullNil($5);

                    $$ = new ClassParseNode(support.extendedUntil($1, lexer.getPosition()), $<Colon3ParseNode>2, support.getCurrentScope(), body, $3);
                    support.popCurrentScope();
                    support.setIsInClass($<Boolean>4.booleanValue());
                }
                | k_class tLSHFT expr {
                    $$ = (support.isInClass() ? 2 : 0) | (support.isInDef() ? 1 : 0);
                    support.setInDef(false);
                    support.setIsInClass(false);
                    support.pushLocalScope();
                } term bodystmt keyword_end {
                    ParseNode body = support.makeNullNil($6);

                    $$ = new SClassParseNode(support.extendedUntil($1, lexer.getPosition()), $3, support.getCurrentScope(), body);
                    support.popCurrentScope();
                    support.setInDef((($<Integer>4.intValue()) & 1) != 0);
                    support.setIsInClass((($<Integer>4.intValue()) & 2) != 0);
                }
                | k_module cpath {
                    if (support.isInDef()) { 
                        support.yyerror("module definition in method body");
                    }
                    $$ = support.isInClass();
                    support.setIsInClass(true);
                    support.pushLocalScope();
                } bodystmt keyword_end {
                    ParseNode body = support.makeNullNil($4);

                    $$ = new ModuleParseNode(support.extendedUntil($1, lexer.getPosition()), $<Colon3ParseNode>2, support.getCurrentScope(), body);
                    support.popCurrentScope();
                    support.setIsInClass($<Boolean>3.booleanValue());
                }
                | keyword_def fname {
                    support.pushLocalScope();
                    $$ = lexer.getCurrentArg();
                    lexer.setCurrentArg(null);
                    support.checkMethodName($2);
                } {
                    $$ = support.isInDef();
                    support.setInDef(true);
                } f_arglist bodystmt keyword_end {
                    ParseNode body = support.makeNullNil($6);

                    $$ = new DefnParseNode(support.extendedUntil($1, $7), support.symbolID($2), (ArgsParseNode) $5, support.getCurrentScope(), body);
                    support.popCurrentScope();
                    support.setInDef($<Boolean>4.booleanValue());
                    lexer.setCurrentArg($<TruffleString>3);
                }
                | keyword_def singleton dot_or_colon {
                    lexer.setState(EXPR_FNAME); 
                    $$ = support.isInDef();
                    support.setInDef(true);
               } fname {
                    support.pushLocalScope();
                    lexer.setState(EXPR_ENDFN|EXPR_LABEL); /* force for args */
                    $$ = lexer.getCurrentArg();
                    lexer.setCurrentArg(null);
                    support.checkMethodName($5);
                } f_arglist bodystmt keyword_end {
                    ParseNode body = $8;
                    if (body == null) body = NilImplicitParseNode.NIL;

                    $$ = new DefsParseNode(support.extendedUntil($1, $9), $2, support.symbolID($5), (ArgsParseNode) $7, support.getCurrentScope(), body);
                    support.popCurrentScope();
                    support.setInDef($<Boolean>4.booleanValue());
                    lexer.setCurrentArg($<TruffleString>6);
                }
                | keyword_break {
                    $$ = new BreakParseNode($1, NilImplicitParseNode.NIL);
                }
                | keyword_next {
                    $$ = new NextParseNode($1, NilImplicitParseNode.NIL);
                }
                | keyword_redo {
                    $$ = new RedoParseNode($1);
                }
                | keyword_retry {
                    $$ = new RetryParseNode($1);
                }

primary_value   : primary {
                    value_expr(lexer, $1);
                    $$ = $1;
                    if ($$ == null) $$ = NilImplicitParseNode.NIL;
                }

k_class         : keyword_class {
                    $$ = $1;
                }

k_module        : keyword_module {
                    $$ = $1;
                }

k_return        : keyword_return {
                    if (support.isInClass() && !support.isInDef() && !support.getCurrentScope().isBlockScope()) {
                        lexer.compile_error(PID.TOP_LEVEL_RETURN, "Invalid return in class/module body");
                    }
                    $$ = $1;
                }

then            : term
                | keyword_then
                | term keyword_then

do              : term
                | keyword_do_cond

if_tail         : opt_else
                | keyword_elsif expr_value then compstmt if_tail {
                    $$ = new IfParseNode($1, support.getConditionNode($2), $4, $5);
                }

opt_else        : none
                | keyword_else compstmt {
                    $$ = $2;
                }

// [!null]
for_var         : lhs
                | mlhs {
                }

f_marg          : f_norm_arg {
                     $$ = support.assignableInCurr($1, NilImplicitParseNode.NIL);
                }
                | tLPAREN f_margs rparen {
                    $$ = $2;
                }

// [!null]
f_marg_list     : f_marg {
                    $$ = support.newArrayNode($1.getPosition(), $1);
                }
                | f_marg_list ',' f_marg {
                    $$ = $1.add($3);
                }

f_margs         : f_marg_list {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, null, null);
                }
                | f_marg_list ',' tSTAR f_norm_arg {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, support.assignableInCurr($4, null), null);
                }
                | f_marg_list ',' tSTAR f_norm_arg ',' f_marg_list {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, support.assignableInCurr($4, null), $6);
                }
                | f_marg_list ',' tSTAR {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, new StarParseNode(lexer.getPosition()), null);
                }
                | f_marg_list ',' tSTAR ',' f_marg_list {
                    $$ = new MultipleAsgnParseNode($1.getPosition(), $1, new StarParseNode(lexer.getPosition()), $5);
                }
                | tSTAR f_norm_arg {
                    $$ = new MultipleAsgnParseNode(lexer.getPosition(), null, support.assignableInCurr($2, null), null);
                }
                | tSTAR f_norm_arg ',' f_marg_list {
                    $$ = new MultipleAsgnParseNode(lexer.getPosition(), null, support.assignableInCurr($2, null), $4);
                }
                | tSTAR {
                    $$ = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), null);
                }
                | tSTAR ',' f_marg_list {
                    $$ = new MultipleAsgnParseNode(support.getPosition($3), null, null, $3);
                }

block_args_tail : f_block_kwarg ',' f_kwrest opt_f_block_arg {
                    $$ = support.new_args_tail($1.getPosition(), $1, $3, $4);
                }
                | f_block_kwarg opt_f_block_arg {
                    $$ = support.new_args_tail($1.getPosition(), $1, (TruffleString) null, $2);
                }
                | f_kwrest opt_f_block_arg {
                    $$ = support.new_args_tail(lexer.getPosition(), null, $1, $2);
                }
                | f_no_kwarg opt_f_block_arg {
                    $$ = support.new_args_tail(lexer.getPosition(), null, RubyLexer.Keyword.NIL.bytes, $2);
                }
                | f_block_arg {
                    $$ = support.new_args_tail($1.getPosition(), null, (TruffleString) null, $1);
                }

opt_block_args_tail : ',' block_args_tail {
                    $$ = $2;
                }
                | /* none */ {
                    $$ = support.new_args_tail(lexer.getPosition(), null, (TruffleString) null, null);
                }

// [!null]
block_param     : f_arg ',' f_block_optarg ',' f_rest_arg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, $3, $5, null, $6);
                }
                | f_arg ',' f_block_optarg ',' f_rest_arg ',' f_arg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, $3, $5, $7, $8);
                }
                | f_arg ',' f_block_optarg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, $3, null, null, $4);
                }
                | f_arg ',' f_block_optarg ',' f_arg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, $3, null, $5, $6);
                }
                | f_arg ',' f_rest_arg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, null, $3, null, $4);
                }
                | f_arg ',' {
                    RestArgParseNode rest = new UnnamedRestArgParseNode($1.getPosition(), Layouts.TEMP_PREFIX + "anonymous_rest", support.getCurrentScope().addVariable("*"), false);
                    $$ = support.new_args($1.getPosition(), $1, null, rest, null, (ArgsTailHolder) null);
                }
                | f_arg ',' f_rest_arg ',' f_arg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, null, $3, $5, $6);
                }
                | f_arg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, null, null, null, $2);
                }
                | f_block_optarg ',' f_rest_arg opt_block_args_tail {
                    $$ = support.new_args(support.getPosition($1), null, $1, $3, null, $4);
                }
                | f_block_optarg ',' f_rest_arg ',' f_arg opt_block_args_tail {
                    $$ = support.new_args(support.getPosition($1), null, $1, $3, $5, $6);
                }
                | f_block_optarg opt_block_args_tail {
                    $$ = support.new_args(support.getPosition($1), null, $1, null, null, $2);
                }
                | f_block_optarg ',' f_arg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), null, $1, null, $3, $4);
                }
                | f_rest_arg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), null, null, $1, null, $2);
                }
                | f_rest_arg ',' f_arg opt_block_args_tail {
                    $$ = support.new_args($1.getPosition(), null, null, $1, $3, $4);
                }
                | block_args_tail {
                    $$ = support.new_args($1.getPosition(), null, null, null, null, $1);
                }

opt_block_param : none {
                    $$ = support.new_args(lexer.getPosition(), null, null, null, null, null);
                }
                | /* none */ {
                    support.enterBlockParameters();
                } block_param_def {
                    lexer.commandStart = true;
                    $$ = $2;
                }

block_param_def : tPIPE opt_bv_decl tPIPE {
                    lexer.setCurrentArg(null);
                    $$ = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
                }
                | tOROP {
                    $$ = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
                }
                | tPIPE block_param opt_bv_decl tPIPE {
                    lexer.setCurrentArg(null);
                    $$ = $2;
                }

// shadowed block variables....
opt_bv_decl     : opt_nl {
                    $$ = null;
                }
                | opt_nl ';' bv_decls opt_nl {
                    $$ = null;
                }

// ENEBO: This is confusing...
bv_decls        : bvar {
                    $$ = null;
                }
                | bv_decls ',' bvar {
                    $$ = null;
                }

bvar            : tIDENTIFIER {
                    support.new_bv($1);
                }
                | f_bad_arg {
                    $$ = null;
                }

lambda          : /* none */  {
                    support.pushBlockScope();
                    $$ = lexer.getLeftParenBegin();
                    lexer.setLeftParenBegin(lexer.incrementParenNest());
                } f_larglist {
                    $$ = Long.valueOf(lexer.getCmdArgumentState().getStack());
                    lexer.getCmdArgumentState().reset();
                } lambda_body {
                    lexer.getCmdArgumentState().reset($<Long>3.longValue());
                    lexer.getCmdArgumentState().restart();
                    $$ = new LambdaParseNode($2.getPosition(), $2, $4, support.getCurrentScope());
                    lexer.setLeftParenBegin($<Integer>1);
                    support.popCurrentScope();
                }

f_larglist      : tLPAREN2 {
                    support.enterBlockParameters();
                } f_args opt_bv_decl tRPAREN {
                    $$ = $3;
                }
                | /* none */ {
                    support.enterBlockParameters();
                } f_args_any {
                    $$ = $2;
                }
                | /* none */ {
                    $$ = support.new_args(lexer.getPosition(), null, null, null, null, null);
                }

lambda_body     : tLAMBEG compstmt tRCURLY {
                    $$ = $2;
                }
                | keyword_do_lambda bodystmt keyword_end {
                    $$ = $2;
                }

do_block        : keyword_do_block do_body keyword_end {
                    $$ = $2;
                }

  // JRUBY-2326 and GH #305 both end up hitting this production whereas in
  // MRI these do not.  I have never isolated the cause but I can work around
  // the individual reported problems with a few extra conditionals in this
  // first production
block_call      : command do_block {
                    // Workaround for JRUBY-2326 (MRI does not enter this production for some reason)
                    if ($1 instanceof YieldParseNode) {
                        lexer.compile_error(PID.BLOCK_GIVEN_TO_YIELD, "block given to yield");
                    }
                    if ($1 instanceof BlockAcceptingParseNode && $<BlockAcceptingParseNode>1.getIterNode() instanceof BlockPassParseNode) {
                        lexer.compile_error(PID.BLOCK_ARG_AND_BLOCK_GIVEN, "Both block arg and actual block given.");
                    }
                    if ($1 instanceof NonLocalControlFlowParseNode) {
                        ((BlockAcceptingParseNode) $<NonLocalControlFlowParseNode>1.getValueNode()).setIterNode($2);
                    } else {
                        $<BlockAcceptingParseNode>1.setIterNode($2);
                    }
                    $$ = $1;
                    $<ParseNode>$.setPosition($1.getPosition());
                }
                | block_call call_op2 operation2 opt_paren_args {
                    $$ = support.new_call($1, $2, $3, $4, null);
                }
                | block_call call_op2 operation2 opt_paren_args brace_block {
                    $$ = support.new_call($1, $2, $3, $4, $5);
                }
                | block_call call_op2 operation2 command_args do_block {
                    $$ = support.new_call($1, $2, $3, $4, $5);
                }

// [!null]
method_call     : fcall paren_args {
                    support.frobnicate_fcall_args($1, $2, null);
                    $$ = $1;
                }
                | primary_value call_op operation2 opt_paren_args {
                    $$ = support.new_call($1, $2, $3, $4, null);
                }
                | primary_value tCOLON2 operation2 paren_args {
                    $$ = support.new_call($1, $3, $4, null);
                }
                | primary_value tCOLON2 operation3 {
                    $$ = support.new_call($1, $3, null, null);
                }
                | primary_value call_op paren_args {
                    $$ = support.new_call($1, $2, TStringConstants.CALL, $3, null);
                }
                | primary_value tCOLON2 paren_args {
                    $$ = support.new_call($1, TStringConstants.CALL, $3, null);
                }
                | keyword_super paren_args {
                    $$ = support.new_super($1, $2);
                }
                | keyword_super {
                    $$ = new ZSuperParseNode($1);
                }
                | primary_value '[' opt_call_args rbracket {
                    if ($1 instanceof SelfParseNode) {
                        $$ = support.new_fcall(TStringConstants.LBRACKET_RBRACKET);
                        support.frobnicate_fcall_args($<FCallParseNode>$, $3, null);
                    } else {
                        $$ = support.new_call($1, TStringConstants.LBRACKET_RBRACKET, $3, null);
                    }
                }

brace_block     : tLCURLY brace_body tRCURLY {
                    $$ = $2;
                }
                | keyword_do do_body keyword_end {
                    $$ = $2;
                }

brace_body      : {
                    $$ = lexer.getPosition();
                } {
                    support.pushBlockScope();
                    $$ = Long.valueOf(lexer.getCmdArgumentState().getStack()) >> 1;
                    lexer.getCmdArgumentState().reset();
                } opt_block_param compstmt {
                    $$ = new IterParseNode($<SourceIndexLength>1, $3, $4, support.getCurrentScope());
                     support.popCurrentScope();
                    lexer.getCmdArgumentState().reset($<Long>2.longValue());
                }

do_body         : {
                    $$ = lexer.getPosition();
                } {
                    support.pushBlockScope();
                    $$ = Long.valueOf(lexer.getCmdArgumentState().getStack());
                    lexer.getCmdArgumentState().reset();
                } opt_block_param bodystmt {
                    $$ = new IterParseNode($<SourceIndexLength>1, $3, $4, support.getCurrentScope());
                     support.popCurrentScope();
                    lexer.getCmdArgumentState().reset($<Long>2.longValue());
                }
 
case_body       : keyword_when args then compstmt cases {
                    $$ = support.newWhenNode($1, $2, $4, $5);
                }

cases           : opt_else | case_body

p_case_body     : keyword_in args then compstmt p_cases {
                    $$ = support.newInNode($1, $2, $4, $5);
                }

p_cases         : opt_else | p_case_body

opt_rescue      : keyword_rescue exc_list exc_var then compstmt opt_rescue {
                    ParseNode node;
                    if ($3 != null) {
                        node = support.appendToBlock(support.node_assign($3, new GlobalVarParseNode($1, support.symbolID(TStringConstants.DOLLAR_BANG))), $5);
                        if ($5 != null) {
                            node.setPosition($1);
                        }
                    } else {
                        node = $5;
                    }
                    ParseNode body = support.makeNullNil(node);
                    $$ = new RescueBodyParseNode($1, $2, body, $6);
                }
                | {
                    $$ = null; 
                }

exc_list        : arg_value {
                    $$ = support.newArrayNode($1.getPosition(), $1);
                }
                | mrhs {
                    $$ = support.splat_array($1);
                    if ($$ == null) $$ = $1; // ArgsCat or ArgsPush
                }
                | none

exc_var         : tASSOC lhs {
                    $$ = $2;
                }
                | none

opt_ensure      : keyword_ensure compstmt {
                    $$ = $2;
                }
                | none

literal         : numeric {
                    $$ = $1;
                }
                | symbol {
                    $$ = support.asSymbol(lexer.getPosition(), $1);
                }
                | dsym

strings         : string {
                    $$ = $1 instanceof EvStrParseNode ? new DStrParseNode($1.getPosition(), lexer.getEncoding()).add($1) : $1;
                    /*
                    NODE *node = $1;
                    if (!node) {
                        node = NEW_STR(STR_NEW0());
                    } else {
                        node = evstr2dstr(node);
                    }
                    $$ = node;
                    */
                }

// [!null]
string          : tCHAR {
                    $$ = $1;
                }
                | string1 {
                    $$ = $1;
                }
                | string string1 {
                    $$ = support.literal_concat($1, $2);
                }

string1         : tSTRING_BEG string_contents tSTRING_END {
                    lexer.heredoc_dedent($2);
                    lexer.setHeredocIndent(0);
                    $$ = $2;
                }

xstring         : tXSTRING_BEG xstring_contents tSTRING_END {
                    SourceIndexLength position = support.getPosition($2);

                    lexer.heredoc_dedent($2);
                    lexer.setHeredocIndent(0);

                    if ($2 == null) {
                        $$ = new XStrParseNode(position, null);
                    } else if ($2 instanceof StrParseNode) {
                        $$ = new XStrParseNode(position, $<StrParseNode>2);
                    } else if ($2 instanceof DStrParseNode) {
                        $$ = new DXStrParseNode(position, $<DStrParseNode>2);

                        $<ParseNode>$.setPosition(position);
                    } else {
                        $$ = new DXStrParseNode(position).add($2);
                    }
                }

regexp          : tREGEXP_BEG regexp_contents tREGEXP_END {
                    $$ = support.newRegexpNode(support.getPosition($2), $2, $3);
                }

words           : tWORDS_BEG ' ' word_list tSTRING_END {
                    $$ = $3;
                }

word_list       : /* none */ {
                    $$ = new ArrayParseNode(lexer.getPosition());
                }
                | word_list word ' ' {
                     $$ = $1.add($2 instanceof EvStrParseNode ? new DStrParseNode($1.getPosition(), lexer.getEncoding()).add($2) : $2);
                }

word            : string_content {
                     $$ = $<ParseNode>1;
                }
                | word string_content {
                     $$ = support.literal_concat($1, $<ParseNode>2);
                }

symbols         : tSYMBOLS_BEG ' ' symbol_list tSTRING_END {
                    $$ = $3;
                }

symbol_list     : /* none */ {
                    $$ = new ArrayParseNode(lexer.getPosition());
                }
                | symbol_list word ' ' {
                    $$ = $1.add($2 instanceof EvStrParseNode ? new DSymbolParseNode($1.getPosition()).add($2) : support.asSymbol($1.getPosition(), $2));
                }

qwords          : tQWORDS_BEG ' ' qword_list tSTRING_END {
                    $$ = $3;
                }

qsymbols        : tQSYMBOLS_BEG ' ' qsym_list tSTRING_END {
                    $$ = $3;
                }


qword_list      : /* none */ {
                    $$ = new ArrayParseNode(lexer.getPosition());
                }
                | qword_list tSTRING_CONTENT ' ' {
                    $$ = $1.add($2);
                }

qsym_list      : /* none */ {
                    $$ = new ArrayParseNode(lexer.getPosition());
                }
                | qsym_list tSTRING_CONTENT ' ' {
                    $$ = $1.add(support.asSymbol($1.getPosition(), $2));
                }

string_contents : /* none */ {
                    $$ = lexer.createStr(lexer.encoding.tencoding.getEmpty(), lexer.encoding, 0);
                }
                | string_contents string_content {
                    $$ = support.literal_concat($1, $<ParseNode>2);
                }

xstring_contents: /* none */ {
                    $$ = null;
                }
                | xstring_contents string_content {
                    $$ = support.literal_concat($1, $<ParseNode>2);
                }

regexp_contents: /* none */ {
                    $$ = null;
                }
                | regexp_contents string_content {
    // FIXME: mri is different here.
                    $$ = support.literal_concat($1, $<ParseNode>2);
                }

string_content  : tSTRING_CONTENT {
                    $$ = $1;
                }
                | tSTRING_DVAR {
                    $$ = lexer.getStrTerm();
                    lexer.setStrTerm(null);
                    lexer.setState(EXPR_BEG);
                } string_dvar {
                    lexer.setStrTerm($<StrTerm>2);
                    $$ = new EvStrParseNode(support.getPosition($3), $3);
                }
                | tSTRING_DBEG {
                   $$ = lexer.getStrTerm();
                   lexer.setStrTerm(null);
                   lexer.getConditionState().stop();
                } {
                   $$ = lexer.getCmdArgumentState().getStack();
                   lexer.getCmdArgumentState().reset();
                } {
                   $$ = lexer.getState();
                   lexer.setState(EXPR_BEG);
                } {
                   $$ = lexer.getBraceNest();
                   lexer.setBraceNest(0);
                } {
                   $$ = lexer.getHeredocIndent();
                   lexer.setHeredocIndent(0);
                } compstmt tSTRING_DEND {
                   lexer.getConditionState().restart();
                   lexer.setStrTerm($<StrTerm>2);
                   lexer.getCmdArgumentState().reset($<Long>3.longValue());
                   lexer.setState($<Integer>4);
                   lexer.setBraceNest($<Integer>5);
                   lexer.setHeredocIndent($<Integer>6);
                   lexer.setHeredocLineIndent(-1);

                   $$ = support.newEvStrNode(support.getPosition($7), $7);
                }

string_dvar     : tGVAR {
                     $$ = new GlobalVarParseNode(lexer.getPosition(), support.symbolID($1));
                }
                | tIVAR {
                     $$ = new InstVarParseNode(lexer.getPosition(), support.symbolID($1));
                }
                | tCVAR {
                     $$ = new ClassVarParseNode(lexer.getPosition(), support.symbolID($1));
                }
                | backref

// ByteList:symbol
symbol          : tSYMBEG sym {
                     lexer.setState(EXPR_END|EXPR_ENDARG);
                     $$ = $2;
                }

// ByteList:symbol
sym             : fname
                | tIVAR {
                    $$ = $1;
                }
                | tGVAR {
                    $$ = $1;
                }
                | tCVAR {
                    $$ = $1;
                }

dsym            : tSYMBEG xstring_contents tSTRING_END {
                     lexer.setState(EXPR_END|EXPR_ENDARG);

                     // DStrNode: :"some text #{some expression}"
                     // StrNode: :"some text"
                     // EvStrNode :"#{some expression}"
                     // Ruby 1.9 allows empty strings as symbols
                     if ($2 == null) {
                         $$ = support.asSymbol(lexer.getPosition(), TStringConstants.EMPTY_US_ASCII);
                     } else if ($2 instanceof DStrParseNode) {
                         $$ = new DSymbolParseNode($2.getPosition(), $<DStrParseNode>2);
                     } else if ($2 instanceof StrParseNode) {
                         $$ = support.asSymbol($2.getPosition(), $2);
                     } else {
                         $$ = new DSymbolParseNode($2.getPosition());
                         $<DSymbolParseNode>$.add($2);
                     }
                }

numeric         : simple_numeric {
                    $$ = $1;  
                }
                | tUMINUS_NUM simple_numeric %prec tLOWEST {
                     $$ = support.negateNumeric($2);
                }

simple_numeric  : tINTEGER {
                    $$ = $1;
                }
                | tFLOAT {
                     $$ = $1;
                }
                | tRATIONAL {
                     $$ = $1;
                }
                | tIMAGINARY {
                     $$ = $1;
                }

// [!null]
var_ref         : /*mri:user_variable*/ tIDENTIFIER {
                    $$ = support.declareIdentifier($1);
                }
                | tIVAR {
                    $$ = new InstVarParseNode(lexer.tokline, support.symbolID($1));
                }
                | tGVAR {
                    $$ = new GlobalVarParseNode(lexer.tokline, support.symbolID($1));
                }
                | tCONSTANT {
                    $$ = new ConstParseNode(lexer.tokline, support.symbolID($1));
                }
                | tCVAR {
                    $$ = new ClassVarParseNode(lexer.tokline, support.symbolID($1));
                } /*mri:user_variable*/
                | /*mri:keyword_variable*/ keyword_nil { 
                    $$ = new NilParseNode(lexer.tokline);
                }
                | keyword_self {
                    $$ = new SelfParseNode(lexer.tokline);
                }
                | keyword_true { 
                    $$ = new TrueParseNode((SourceIndexLength) $$);
                }
                | keyword_false {
                    $$ = new FalseParseNode((SourceIndexLength) $$);
                }
                | keyword__FILE__ {
                    RubyEncoding encoding = support.getConfiguration().getContext() == null ? Encodings.UTF_8 : support.getConfiguration().getContext().getEncodingManager().getLocaleEncoding();
                    $$ = new FileParseNode(lexer.tokline, TStringUtils.fromJavaString(lexer.getFile(), encoding), encoding);
                }
                | keyword__LINE__ {
                    $$ = new FixnumParseNode(lexer.tokline, lexer.tokline.toSourceSection(lexer.getSource()).getStartLine() + lexer.getLineOffset());
                }
                | keyword__ENCODING__ {
                    $$ = new EncodingParseNode(lexer.tokline, lexer.getEncoding());
                } /*mri:keyword_variable*/

// [!null]
var_lhs         : /*mri:user_variable*/ tIDENTIFIER {
                    $$ = support.assignableLabelOrIdentifier($1, null);
                }
                | tIVAR {
                    $$ = new InstAsgnParseNode(lexer.tokline, support.symbolID($1), NilImplicitParseNode.NIL);
                }
                | tGVAR {
                    $$ = new GlobalAsgnParseNode(lexer.tokline, support.symbolID($1), NilImplicitParseNode.NIL);
                }
                | tCONSTANT {
                    if (support.isInDef()) support.compile_error("dynamic constant assignment");

                    $$ = new ConstDeclParseNode(lexer.tokline, support.symbolID($1), null, NilImplicitParseNode.NIL);
                }
                | tCVAR {
                    $$ = new ClassVarAsgnParseNode(lexer.tokline, support.symbolID($1), NilImplicitParseNode.NIL);
                } /*mri:user_variable*/
                | /*mri:keyword_variable*/ keyword_nil {
                    support.compile_error("Can't assign to nil");
                    $$ = null;
                }
                | keyword_self {
                    support.compile_error("Can't change the value of self");
                    $$ = null;
                }
                | keyword_true {
                    support.compile_error("Can't assign to true");
                    $$ = null;
                }
                | keyword_false {
                    support.compile_error("Can't assign to false");
                    $$ = null;
                }
                | keyword__FILE__ {
                    support.compile_error("Can't assign to __FILE__");
                    $$ = null;
                }
                | keyword__LINE__ {
                    support.compile_error("Can't assign to __LINE__");
                    $$ = null;
                }
                | keyword__ENCODING__ {
                    support.compile_error("Can't assign to __ENCODING__");
                    $$ = null;
                } /*mri:keyword_variable*/

// [!null]
backref         : tNTH_REF {
                    $$ = $1;
                }
                | tBACK_REF {
                    $$ = $1;
                }

superclass      : tLT {
                   lexer.setState(EXPR_BEG);
                   lexer.commandStart = true;
                } expr_value term {
                    $$ = $3;
                }
                | /* none */ {
                   $$ = null;
                }

// [!null]
f_arglist       : tLPAREN2 f_args rparen {
                    $$ = $2;
                    lexer.setState(EXPR_BEG);
                    lexer.commandStart = true;
                }
                | {
                   $$ = lexer.inKwarg;
                   lexer.inKwarg = true;
                   lexer.setState(lexer.getState() | EXPR_LABEL);
                } f_args term {
                   lexer.inKwarg = $<Boolean>1;
                    $$ = $2;
                    lexer.setState(EXPR_BEG);
                    lexer.commandStart = true;
                }


args_tail       : f_kwarg ',' f_kwrest opt_f_block_arg {
                    $$ = support.new_args_tail($1.getPosition(), $1, $3, $4);
                }
                | f_kwarg opt_f_block_arg {
                    $$ = support.new_args_tail($1.getPosition(), $1, (TruffleString) null, $2);
                }
                | f_kwrest opt_f_block_arg {
                    $$ = support.new_args_tail(lexer.getPosition(), null, $1, $2);
                }
                | f_no_kwarg opt_f_block_arg {
                    $$ = support.new_args_tail(lexer.getPosition(), null, RubyLexer.Keyword.NIL.bytes, $2);
                }
                | f_block_arg {
                    $$ = support.new_args_tail($1.getPosition(), null, (TruffleString) null, $1);
                }

opt_args_tail   : ',' args_tail {
                    $$ = $2;
                }
                | /* none */ {
                    $$ = support.new_args_tail(lexer.getPosition(), null, (TruffleString) null, null);
                }

f_args          : f_args_any {
                    $$ = $1;
                }
                | /* none */ {
                    $$ = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
                }

// [!null]
f_args_any      : f_arg ',' f_optarg ',' f_rest_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, $3, $5, null, $6);
                }
                | f_arg ',' f_optarg ',' f_rest_arg ',' f_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, $3, $5, $7, $8);
                }
                | f_arg ',' f_optarg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, $3, null, null, $4);
                }
                | f_arg ',' f_optarg ',' f_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, $3, null, $5, $6);
                }
                | f_arg ',' f_rest_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, null, $3, null, $4);
                }
                | f_arg ',' f_rest_arg ',' f_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, null, $3, $5, $6);
                }
                | f_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), $1, null, null, null, $2);
                }
                | f_optarg ',' f_rest_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), null, $1, $3, null, $4);
                }
                | f_optarg ',' f_rest_arg ',' f_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), null, $1, $3, $5, $6);
                }
                | f_optarg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), null, $1, null, null, $2);
                }
                | f_optarg ',' f_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), null, $1, null, $3, $4);
                }
                | f_rest_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), null, null, $1, null, $2);
                }
                | f_rest_arg ',' f_arg opt_args_tail {
                    $$ = support.new_args($1.getPosition(), null, null, $1, $3, $4);
                }
                | args_tail {
                    $$ = support.new_args($1.getPosition(), null, null, null, null, $1);
                }
                | f_arg ',' args_forward {
                    SourceIndexLength position = support.getPosition(null);
                    RestArgParseNode splat = new RestArgParseNode(position, ParserSupport.FORWARD_ARGS_REST_VAR, 0);
                    BlockArgParseNode block = new BlockArgParseNode(position, 1, ParserSupport.FORWARD_ARGS_BLOCK_VAR);
                    ArgsTailHolder argsTail = support.new_args_tail(position, null, ParserSupport.FORWARD_ARGS_KWREST_VAR_TSTRING, block);
                    $$ = support.new_args(position, $1, null, splat, null, argsTail);
                }
                | args_forward {
                    SourceIndexLength position = support.getPosition(null);
                    RestArgParseNode splat = new RestArgParseNode(position, ParserSupport.FORWARD_ARGS_REST_VAR, 0);
                    BlockArgParseNode block = new BlockArgParseNode(position, 1, ParserSupport.FORWARD_ARGS_BLOCK_VAR);
                    ArgsTailHolder argsTail = support.new_args_tail(position, null, ParserSupport.FORWARD_ARGS_KWREST_VAR_TSTRING, block);
                    $$ = support.new_args(position, null, null, splat, null, argsTail);
                }

args_forward    : tBDOT3

f_bad_arg       : tCONSTANT {
                    support.yyerror("formal argument cannot be a constant");
                }
                | tIVAR {
                    support.yyerror("formal argument cannot be an instance variable");
                }
                | tGVAR {
                    support.yyerror("formal argument cannot be a global variable");
                }
                | tCVAR {
                    support.yyerror("formal argument cannot be a class variable");
                }

// ByteList:f_norm_arg [!null]
f_norm_arg      : f_bad_arg {
                    $$ = $1; // Not really reached
                }
                | tIDENTIFIER {
                    $$ = support.formal_argument($1);
                }

f_arg_asgn      : f_norm_arg {
                    lexer.setCurrentArg($1);
                    $$ = support.arg_var($1);
                }

f_arg_item      : f_arg_asgn {
                    lexer.setCurrentArg(null);
                    $$ = $1;
                }
                | tLPAREN f_margs rparen {
                    $$ = $2;
                    /*            {
            ID tid = internal_id();
            arg_var(tid);
            if (dyna_in_block()) {
                $2->nd_value = NEW_DVAR(tid);
            }
            else {
                $2->nd_value = NEW_LVAR(tid);
            }
            $$ = NEW_ARGS_AUX(tid, 1);
            $$->nd_next = $2;*/
                }

// [!null]
f_arg           : f_arg_item {
                    $$ = new ArrayParseNode(lexer.getPosition(), $1);
                }
                | f_arg ',' f_arg_item {
                    $1.add($3);
                    $$ = $1;
                }

f_label         : tLABEL {
                    support.arg_var(support.formal_argument($1));
                    lexer.setCurrentArg($1);
                    $$ = $1;
                }

f_kw            : f_label arg_value {
                    lexer.setCurrentArg(null);
                    $$ = support.keyword_arg($2.getPosition(), support.assignableKeyword($1, $2));
                }
                | f_label {
                    lexer.setCurrentArg(null);
                    $$ = support.keyword_arg(lexer.getPosition(), support.assignableKeyword($1, RequiredKeywordArgumentValueParseNode.INSTANCE));
                }

f_block_kw      : f_label primary_value {
                    $$ = support.keyword_arg(support.getPosition($2), support.assignableKeyword($1, $2));
                }
                | f_label {
                    $$ = support.keyword_arg(lexer.getPosition(), support.assignableKeyword($1, RequiredKeywordArgumentValueParseNode.INSTANCE));
                }
             

f_block_kwarg   : f_block_kw {
                    $$ = new ArrayParseNode($1.getPosition(), $1);
                }
                | f_block_kwarg ',' f_block_kw {
                    $$ = $1.add($3);
                }

f_kwarg         : f_kw {
                    $$ = new ArrayParseNode($1.getPosition(), $1);
                }
                | f_kwarg ',' f_kw {
                    $$ = $1.add($3);
                }

kwrest_mark     : tPOW {
                    $$ = $1;
                }
                | tDSTAR {
                    $$ = $1;
                }

f_no_kwarg      : kwrest_mark keyword_nil
                ;

f_kwrest        : kwrest_mark tIDENTIFIER {
                    support.shadowing_lvar($2);
                    $$ = $2;
                }
                | kwrest_mark {
                    $$ = ParserSupport.INTERNAL_ID;
                }

f_opt           : f_arg_asgn '=' arg_value {
                    lexer.setCurrentArg(null);
                    $$ = new OptArgParseNode(support.getPosition($3), support.assignableLabelOrIdentifier($1.getName(), $3));
                }

f_block_opt     : f_arg_asgn '=' primary_value {
                    lexer.setCurrentArg(null);
                    $$ = new OptArgParseNode(support.getPosition($3), support.assignableLabelOrIdentifier($1.getName(), $3));
                }

f_block_optarg  : f_block_opt {
                    $$ = new BlockParseNode($1.getPosition()).add($1);
                }
                | f_block_optarg ',' f_block_opt {
                    $$ = support.appendToBlock($1, $3);
                }

f_optarg        : f_opt {
                    $$ = new BlockParseNode($1.getPosition()).add($1);
                }
                | f_optarg ',' f_opt {
                    $$ = support.appendToBlock($1, $3);
                }

restarg_mark    : tSTAR2 {
                    $$ = $1;
                }
                | tSTAR {
                    $$ = $1;
                }

// [!null]
f_rest_arg      : restarg_mark tIDENTIFIER {
                    if (!support.is_local_id($2)) {
                        support.yyerror("rest argument must be local variable");
                    }
                    
                    $$ = new RestArgParseNode(support.arg_var(support.shadowing_lvar($2)));
                }
                | restarg_mark {
  // FIXME: bytelist_love: somewhat silly to remake the empty bytelist over and over but this type should change (using null vs "" is a strange distinction).
  $$ = new UnnamedRestArgParseNode(lexer.getPosition(), Layouts.TEMP_PREFIX + "unnamed_rest", support.getCurrentScope().addVariable("*"), true);
                }

// [!null]
blkarg_mark     : tAMPER2 {
                    $$ = $1;
                }
                | tAMPER {
                    $$ = $1;
                }

// f_block_arg - Block argument def for function (foo(&block)) [!null]
f_block_arg     : blkarg_mark tIDENTIFIER {
                    if (!support.is_local_id($2)) {
                        support.yyerror("block argument must be local variable");
                    }
                    
                    $$ = new BlockArgParseNode(support.arg_var(support.shadowing_lvar($2)));
                }

opt_f_block_arg : ',' f_block_arg {
                    $$ = $2;
                }
                | /* none */ {
                    $$ = null;
                }

singleton       : var_ref {
                    value_expr(lexer, $1);
                    $$ = $1;
                }
                | tLPAREN2 {
                    lexer.setState(EXPR_BEG);
                } expr rparen {
                    if ($3 == null) {
                        support.yyerror("can't define single method for ().");
                    } else if ($3 instanceof ILiteralNode) {
                        support.yyerror("can't define single method for literals.");
                    }
                    value_expr(lexer, $3);
                    $$ = $3;
                }

// HashNode: [!null]
assoc_list      : none {
                    $$ = new HashParseNode(lexer.getPosition());
                }
                | assocs trailer {
                    $$ = support.remove_duplicate_keys($1);
                }

// [!null]
assocs          : assoc {
                    $$ = new HashParseNode(lexer.getPosition(), $1);
                }
                | assocs ',' assoc {
                    $$ = $1.add($3);
                }

// Cons: [!null]
assoc           : arg_value tASSOC arg_value {
                    $$ = support.createKeyValue($1, $3);
                }
                | tLABEL arg_value {
                    ParseNode label = support.asSymbol(support.getPosition($2), $1);
                    $$ = support.createKeyValue(label, $2);
                }
                | tLABEL {
                    ParseNode val = support.declareIdentifier($1);
                    ParseNode label = support.asSymbol(support.getPosition(null), $1);
                    $$ = support.createKeyValue(label, val);
                }
                | tSTRING_BEG string_contents tLABEL_END arg_value {
                    if ($2 instanceof StrParseNode) {
                        DStrParseNode dnode = new DStrParseNode(support.getPosition($2), lexer.getEncoding());
                        dnode.add($2);
                        $$ = support.createKeyValue(new DSymbolParseNode(support.getPosition($2), dnode), $4);
                    } else if ($2 instanceof DStrParseNode) {
                        $$ = support.createKeyValue(new DSymbolParseNode(support.getPosition($2), $<DStrParseNode>2), $4);
                    } else {
                        support.compile_error("Uknown type for assoc in strings: " + $2);
                    }

                }
                | tDSTAR arg_value {
                    $$ = support.createKeyValue(null, $2);
                }

operation       : tIDENTIFIER {
                    $$ = $1;
                }
                | tCONSTANT {
                    $$ = $1;
                }
                | tFID {
                    $$ = $1;
                }
operation2      : tIDENTIFIER  {
                    $$ = $1;
                }
                | tCONSTANT {
                    $$ = $1;
                }
                | tFID {
                    $$ = $1;
                }
                | op {
                    $$ = $1;
                }
                    
operation3      : tIDENTIFIER {
                    $$ = $1;
                }
                | tFID {
                    $$ = $1;
                }
                | op {
                    $$ = $1;
                }
                    
dot_or_colon    : tDOT {
                    $$ = $1;
                }
                | tCOLON2 {
                    $$ = $1;
                }

call_op         : tDOT {
                    $$ = $1;
                }
                | tANDDOT {
                    $$ = $1;
                }

call_op2        : call_op
                | tCOLON2 {
                    $$ = $1;
                }
  
opt_terms       : /* none */ | terms
opt_nl          : /* none */ | '\n'
rparen          : opt_nl tRPAREN {
                    $$ = $2;
                }
rbracket        : opt_nl tRBRACK {
                    $$ = $2;
                }
trailer         : /* none */ | '\n' | ','

term            : ';'
                | '\n'

terms           : term
                | terms ';'

none            : /* none */ {
                      $$ = null;
                }

none_block_pass : /* none */ {  
                  $$ = null;
                }

%%

    /** The parse method use an lexer stream and parse it to an AST node 
     * structure
     */
    public RubyParserResult parse(ParserConfiguration configuration) {
        support.reset();
        support.setConfiguration(configuration);
        support.setResult(new RubyParserResult());
        
        yyparse(lexer, null);
        
        return support.getResult();
    }
}
// CheckStyle: stop generated
// @formatter:on
