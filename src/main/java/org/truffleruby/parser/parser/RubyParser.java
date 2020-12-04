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
 * Copyright (C) 2008-2017 Thomas E Enebo <enebo@acm.org>
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
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 ***** END LICENSE BLOCK *****/
// created by jay 1.0.2 (c) 2002-2004 ats@cs.rit.edu
// skeleton Java 1.0 (c) 2002 ats@cs.rit.edu

// line 2 "RubyParser.y"
package org.truffleruby.parser.parser;

import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.RubyWarnings;
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

import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;
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

    public RubyParser(RubyContext context, LexerSource source, RubyWarnings warnings) {
        this.support = new ParserSupport(context, source, warnings);
        this.lexer = new RubyLexer(support, source, warnings);
        support.setLexer(lexer);
    }
// line 127 "-"
  // %token constants
  public static final int keyword_class = 257;
  public static final int keyword_module = 258;
  public static final int keyword_def = 259;
  public static final int keyword_undef = 260;
  public static final int keyword_begin = 261;
  public static final int keyword_rescue = 262;
  public static final int keyword_ensure = 263;
  public static final int keyword_end = 264;
  public static final int keyword_if = 265;
  public static final int keyword_unless = 266;
  public static final int keyword_then = 267;
  public static final int keyword_elsif = 268;
  public static final int keyword_else = 269;
  public static final int keyword_case = 270;
  public static final int keyword_when = 271;
  public static final int keyword_while = 272;
  public static final int keyword_until = 273;
  public static final int keyword_for = 274;
  public static final int keyword_break = 275;
  public static final int keyword_next = 276;
  public static final int keyword_redo = 277;
  public static final int keyword_retry = 278;
  public static final int keyword_in = 279;
  public static final int keyword_do = 280;
  public static final int keyword_do_cond = 281;
  public static final int keyword_do_block = 282;
  public static final int keyword_return = 283;
  public static final int keyword_yield = 284;
  public static final int keyword_super = 285;
  public static final int keyword_self = 286;
  public static final int keyword_nil = 287;
  public static final int keyword_true = 288;
  public static final int keyword_false = 289;
  public static final int keyword_and = 290;
  public static final int keyword_or = 291;
  public static final int keyword_not = 292;
  public static final int modifier_if = 293;
  public static final int modifier_unless = 294;
  public static final int modifier_while = 295;
  public static final int modifier_until = 296;
  public static final int modifier_rescue = 297;
  public static final int keyword_alias = 298;
  public static final int keyword_defined = 299;
  public static final int keyword_BEGIN = 300;
  public static final int keyword_END = 301;
  public static final int keyword__LINE__ = 302;
  public static final int keyword__FILE__ = 303;
  public static final int keyword__ENCODING__ = 304;
  public static final int keyword_do_lambda = 305;
  public static final int tIDENTIFIER = 306;
  public static final int tFID = 307;
  public static final int tGVAR = 308;
  public static final int tIVAR = 309;
  public static final int tCONSTANT = 310;
  public static final int tCVAR = 311;
  public static final int tLABEL = 312;
  public static final int tCHAR = 313;
  public static final int tUPLUS = 314;
  public static final int tUMINUS = 315;
  public static final int tUMINUS_NUM = 316;
  public static final int tPOW = 317;
  public static final int tCMP = 318;
  public static final int tEQ = 319;
  public static final int tEQQ = 320;
  public static final int tNEQ = 321;
  public static final int tGEQ = 322;
  public static final int tLEQ = 323;
  public static final int tANDOP = 324;
  public static final int tOROP = 325;
  public static final int tMATCH = 326;
  public static final int tNMATCH = 327;
  public static final int tDOT = 328;
  public static final int tDOT2 = 329;
  public static final int tDOT3 = 330;
  public static final int tBDOT2 = 331;
  public static final int tBDOT3 = 332;
  public static final int tAREF = 333;
  public static final int tASET = 334;
  public static final int tLSHFT = 335;
  public static final int tRSHFT = 336;
  public static final int tANDDOT = 337;
  public static final int tCOLON2 = 338;
  public static final int tCOLON3 = 339;
  public static final int tOP_ASGN = 340;
  public static final int tASSOC = 341;
  public static final int tLPAREN = 342;
  public static final int tLPAREN2 = 343;
  public static final int tRPAREN = 344;
  public static final int tLPAREN_ARG = 345;
  public static final int tLBRACK = 346;
  public static final int tRBRACK = 347;
  public static final int tLBRACE = 348;
  public static final int tLBRACE_ARG = 349;
  public static final int tSTAR = 350;
  public static final int tSTAR2 = 351;
  public static final int tAMPER = 352;
  public static final int tAMPER2 = 353;
  public static final int tTILDE = 354;
  public static final int tPERCENT = 355;
  public static final int tDIVIDE = 356;
  public static final int tPLUS = 357;
  public static final int tMINUS = 358;
  public static final int tLT = 359;
  public static final int tGT = 360;
  public static final int tPIPE = 361;
  public static final int tBANG = 362;
  public static final int tCARET = 363;
  public static final int tLCURLY = 364;
  public static final int tRCURLY = 365;
  public static final int tBACK_REF2 = 366;
  public static final int tSYMBEG = 367;
  public static final int tSTRING_BEG = 368;
  public static final int tXSTRING_BEG = 369;
  public static final int tREGEXP_BEG = 370;
  public static final int tWORDS_BEG = 371;
  public static final int tQWORDS_BEG = 372;
  public static final int tSTRING_DBEG = 373;
  public static final int tSTRING_DVAR = 374;
  public static final int tSTRING_END = 375;
  public static final int tLAMBDA = 376;
  public static final int tLAMBEG = 377;
  public static final int tNTH_REF = 378;
  public static final int tBACK_REF = 379;
  public static final int tSTRING_CONTENT = 380;
  public static final int tINTEGER = 381;
  public static final int tIMAGINARY = 382;
  public static final int tFLOAT = 383;
  public static final int tRATIONAL = 384;
  public static final int tREGEXP_END = 385;
  public static final int tSYMBOLS_BEG = 386;
  public static final int tQSYMBOLS_BEG = 387;
  public static final int tDSTAR = 388;
  public static final int tSTRING_DEND = 389;
  public static final int tLABEL_END = 390;
  public static final int tLOWEST = 391;
  public static final int yyErrorCode = 256;

  /** number of final state.
    */
  protected static final int yyFinal = 1;

  /** parser tables.
      Order is mandated by <i>jay</i>.
    */
  protected static final short[] yyLhs = {
//yyLhs 666
    -1,   154,     0,   140,   141,   141,   141,   141,   142,   142,
    37,    36,    38,    38,    38,    38,    44,   157,    44,   158,
    39,    39,    39,    39,    39,    39,    39,    39,    39,    39,
    39,    39,    39,    39,    39,    39,    39,    31,    31,    31,
    31,    31,    31,    31,    31,    61,    61,    61,    40,    40,
    40,    40,    40,    40,    45,    32,    32,    60,    60,   114,
   150,    43,    43,    43,    43,    43,    43,    43,    43,    43,
    43,    43,   117,   117,   128,   128,   118,   118,   118,   118,
   118,   118,   118,   118,   118,   118,    75,    75,   104,   104,
   105,   105,    76,    76,    76,    76,    76,    76,    76,    76,
    76,    76,    76,    76,    76,    76,    76,    76,    76,    76,
    76,    81,    81,    81,    81,    81,    81,    81,    81,    81,
    81,    81,    81,    81,    81,    81,    81,    81,    81,    81,
     8,     8,    30,    30,    30,     7,     7,     7,     7,     7,
   121,   121,   122,   122,    64,   160,    64,     6,     6,     6,
     6,     6,     6,     6,     6,     6,     6,     6,     6,     6,
     6,     6,     6,     6,     6,     6,     6,     6,     6,     6,
     6,     6,     6,     6,     6,     6,     6,   135,   135,   135,
   135,   135,   135,   135,   135,   135,   135,   135,   135,   135,
   135,   135,   135,   135,   135,   135,   135,   135,   135,   135,
   135,   135,   135,   135,   135,   135,   135,   135,   135,   135,
   135,   135,   135,   135,   135,   135,   135,   135,   135,    41,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    41,   137,   137,   137,   137,    51,    51,    77,    80,
    80,    80,    80,    62,    62,    54,    54,    58,    58,   131,
   131,   131,   131,   131,    52,    52,    52,    52,    52,   162,
    56,   108,   107,   107,    83,    83,    83,    83,    35,    35,
    74,    74,    74,    42,    42,    42,    42,    42,    42,    42,
    42,    42,    42,    42,   163,    42,   164,    42,   165,   166,
    42,    42,    42,    42,    42,    42,    42,    42,    42,    42,
    42,    42,    42,    42,    42,    42,    42,    42,    42,   168,
   170,    42,   171,   172,    42,    42,    42,   173,   174,    42,
   175,    42,   177,    42,   178,    42,   179,   180,    42,   181,
   182,    42,    42,    42,    42,    42,    46,   152,   153,   151,
   167,   167,   167,   169,   169,    49,    49,    47,    47,   130,
   130,   132,   132,    88,    88,   133,   133,   133,   133,   133,
   133,   133,   133,   133,    95,    95,    95,    95,    95,    94,
    94,    70,    70,    70,    70,    70,    70,    70,    70,    70,
    70,    70,    70,    70,    70,    70,    72,   184,    72,    71,
    71,    71,   125,   125,   124,   124,   134,   134,   185,   186,
   127,   187,    69,   188,    69,    69,   126,   126,   113,    59,
    59,    59,    59,    22,    22,    22,    22,    22,    22,    22,
    22,    22,   112,   112,   189,   190,   115,   191,   192,   116,
    78,    48,    48,   119,   119,    79,    79,    79,    50,    50,
    53,    53,    28,    28,    28,    15,    16,    16,    16,    17,
    18,    19,    25,    85,    85,    27,    27,    91,    89,    89,
    26,    92,    84,    84,    90,    90,    20,    20,    21,    21,
    24,    24,    23,   193,    23,   194,   195,   196,   197,   198,
    23,    65,    65,    65,    65,     2,     1,     1,     1,     1,
    29,    33,    33,    34,    34,    34,    34,    57,    57,    57,
    57,    57,    57,    57,    57,    57,    57,    57,    57,   120,
   120,   120,   120,   120,   120,   120,   120,   120,   120,   120,
   120,    66,    66,   199,    55,    55,    73,   200,    73,    96,
    96,    96,    96,    96,    93,    93,    67,    67,    68,    68,
    68,    68,    68,    68,    68,    68,    68,    68,    68,    68,
    68,    68,    68,   146,   136,   136,   136,   136,     9,     9,
   149,   123,   123,    86,    86,   145,    97,    97,    98,    98,
    99,    99,   100,   100,   143,   143,   183,   144,   144,    63,
   129,   106,   106,    87,    87,    10,    10,    13,    13,    12,
    12,   111,   110,   110,    14,   201,    14,   101,   101,   102,
   102,   103,   103,   103,   103,     3,     3,     3,     4,     4,
     4,     4,     5,     5,     5,    11,    11,   147,   147,   148,
   148,   155,   155,   159,   159,   138,   139,   161,   161,   161,
   176,   176,   156,   156,    82,   109,
    }, yyLen = {
//yyLen 666
     2,     0,     2,     2,     1,     1,     3,     2,     1,     4,
     4,     2,     1,     1,     3,     2,     1,     0,     5,     0,
     4,     3,     3,     3,     2,     3,     3,     3,     3,     3,
     4,     1,     3,     3,     5,     3,     1,     3,     3,     6,
     5,     5,     5,     5,     3,     1,     3,     1,     1,     3,
     3,     3,     2,     1,     1,     1,     1,     1,     4,     3,
     1,     2,     3,     4,     5,     4,     5,     2,     2,     2,
     2,     2,     1,     3,     1,     3,     1,     2,     3,     5,
     2,     4,     2,     4,     1,     3,     1,     3,     2,     3,
     1,     3,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     4,     3,     3,     3,     3,     2,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     4,     3,     3,     3,     3,     2,     1,
     1,     1,     2,     1,     3,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     0,     4,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     3,
     3,     6,     5,     5,     5,     5,     4,     3,     3,     2,
     2,     3,     2,     2,     3,     3,     3,     3,     3,     3,
     4,     2,     2,     3,     3,     3,     3,     1,     3,     3,
     3,     3,     3,     2,     2,     3,     3,     3,     3,     3,
     6,     1,     1,     1,     1,     1,     3,     3,     1,     1,
     2,     4,     2,     1,     3,     3,     3,     1,     1,     1,
     1,     2,     4,     2,     1,     2,     2,     4,     1,     0,
     2,     2,     2,     1,     1,     2,     3,     4,     1,     1,
     3,     4,     2,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     0,     4,     0,     3,     0,     0,
     5,     3,     3,     2,     3,     3,     1,     4,     3,     1,
     5,     4,     3,     2,     1,     2,     2,     6,     6,     0,
     0,     7,     0,     0,     7,     5,     4,     0,     0,     9,
     0,     6,     0,     7,     0,     5,     0,     0,     7,     0,
     0,     9,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     2,     1,     1,     1,     5,     1,     2,     1,
     1,     1,     3,     1,     3,     1,     4,     6,     3,     5,
     2,     4,     1,     3,     4,     2,     2,     2,     1,     2,
     0,     6,     8,     4,     6,     4,     2,     6,     2,     4,
     6,     2,     4,     2,     4,     1,     1,     0,     2,     3,
     1,     4,     1,     4,     1,     3,     1,     1,     0,     0,
     4,     0,     5,     0,     2,     0,     3,     3,     3,     2,
     4,     5,     5,     2,     4,     4,     3,     3,     3,     2,
     1,     4,     3,     3,     0,     0,     4,     0,     0,     4,
     5,     1,     1,     6,     0,     1,     1,     1,     2,     1,
     2,     1,     1,     1,     1,     1,     1,     1,     2,     3,
     3,     3,     4,     0,     3,     1,     2,     4,     0,     3,
     4,     4,     0,     3,     0,     3,     0,     2,     0,     2,
     0,     2,     1,     0,     3,     0,     0,     0,     0,     0,
     8,     1,     1,     1,     1,     2,     1,     1,     1,     1,
     3,     1,     2,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     0,     4,     0,     3,     0,     3,     4,
     2,     2,     2,     1,     2,     0,     1,     0,     6,     8,
     4,     6,     4,     6,     2,     4,     6,     2,     4,     2,
     4,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     3,     1,     3,     1,     2,     1,     2,     1,
     1,     3,     1,     3,     1,     1,     2,     2,     1,     3,
     3,     1,     3,     1,     3,     1,     1,     2,     1,     1,
     1,     2,     2,     0,     1,     0,     4,     1,     2,     1,
     3,     3,     2,     4,     2,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     0,     1,     0,     1,     2,     2,     0,     1,     1,
     1,     1,     1,     2,     0,     0,
    }, yyDefRed = {
//yyDefRed 1128
     1,     0,     0,     0,   367,   368,     0,     0,   314,     0,
     0,     0,   339,   342,     0,     0,     0,   364,   365,   369,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   476,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,   496,   498,   500,     0,     0,
   428,   551,   552,   523,   526,   524,   525,     0,     0,   473,
    60,   304,     0,   477,   305,   306,     0,   307,   308,   303,
   474,    31,    48,   472,   521,     0,     0,     0,     0,     0,
     0,     0,   311,     0,    56,     0,     0,    86,     0,     4,
   309,   310,     0,     0,    72,     0,     2,     0,     5,     0,
     0,     0,     0,     7,   187,   198,   188,   211,   184,   204,
   194,   193,   214,   215,   209,   192,   191,   186,   212,   216,
   217,   196,   185,   199,   203,   205,   197,   190,   206,   213,
   208,     0,     0,     0,     0,   183,   202,   201,   218,   182,
   189,   180,   181,     0,     0,     0,     0,   137,   529,   528,
     0,   531,   172,   173,   169,   150,   151,   152,   159,   156,
   158,   153,   154,   174,   175,   160,   161,   625,   166,   165,
   149,   171,   168,   167,   163,   164,   157,   155,   147,   170,
   148,   176,   162,   138,   356,     0,   624,   139,   207,   200,
   210,   195,   177,   178,   179,   135,   136,   141,   140,   143,
     0,   142,   144,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,   660,   661,     0,     0,     0,
   662,     0,     0,   362,   363,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   366,     0,     0,   379,   380,     0,     0,
   326,     0,     0,     0,     0,   496,     0,     0,   284,    70,
     0,     0,     0,   629,   288,    71,     0,    68,     0,     0,
   449,    67,     0,   654,     0,     0,    19,     0,     0,     0,
   241,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,    13,    12,     0,     0,     0,     0,     0,
   269,     0,     0,     0,   627,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   254,    52,   253,   518,   517,   519,
   515,   516,     0,     0,     0,     0,   483,   492,   336,     0,
   488,   494,   478,   457,   454,   335,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,   264,   265,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   263,   262,     0,     0,     0,     0,
   457,   439,   647,   648,     0,     0,     0,     0,   650,   649,
     0,     0,    88,     0,     0,     0,     0,     0,     0,     3,
     0,   443,     0,   333,    69,   533,   532,   534,   535,   537,
   536,   538,     0,     0,     0,     0,   133,     0,     0,   312,
   354,     0,   357,   645,   646,   359,   145,     0,     0,     0,
   371,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   663,     0,     0,     0,   522,     0,     0,
     0,     0,   347,   632,   295,   291,     0,   634,     0,     0,
   285,   293,     0,   286,     0,   328,     0,   290,     0,   280,
   279,     0,     0,     0,     0,     0,   332,    51,    21,    23,
    22,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   321,    11,     0,     0,   317,     0,   324,
     0,   658,   270,     0,   272,   325,   628,     0,    90,     0,
     0,     0,     0,     0,   505,   503,   520,   502,   499,   479,
   497,   480,   481,   501,     0,     0,   431,   429,     0,     0,
     0,     0,   458,     0,   455,    25,    26,    27,    28,    29,
    49,    50,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   446,     0,   448,     0,     0,   640,     0,     0,   641,   447,
     0,   638,   639,     0,    47,     0,     0,     0,    44,   227,
     0,     0,     0,     0,    37,   219,    33,   294,     0,     0,
     0,     0,    89,    32,     0,   298,     0,    38,   220,     6,
   454,    62,     0,   130,     0,   132,   553,   350,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,   315,     0,
   372,     0,     0,     0,     0,     0,     0,     0,     0,   346,
   374,   340,   373,   343,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   631,     0,     0,     0,   292,   630,   327,
   655,     0,     0,   275,   276,   331,    20,     0,     9,    30,
     0,   226,     0,     0,    14,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   506,     0,   482,   485,     0,   490,
     0,     0,     0,   589,   586,   585,   584,   587,   595,   604,
   583,     0,   616,   615,   620,   619,   605,   590,     0,     0,
     0,   613,   434,     0,     0,   581,   602,     0,   563,   593,
   588,     0,     0,     0,   582,     0,     0,   487,     0,   491,
     0,   453,     0,   452,     0,     0,   438,     0,     0,   445,
     0,     0,     0,     0,     0,   278,     0,   444,   277,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,    87,
     0,     0,     0,     0,     0,     0,     0,     0,     0,   134,
     0,     0,   626,     0,     0,     0,   360,   146,   466,     0,
     0,   467,     0,     0,   377,     0,   375,     0,     0,     0,
     0,     0,     0,     0,   345,     0,     0,     0,     0,     0,
     0,   633,   297,   287,     0,   330,     0,   320,   271,    91,
     0,   507,   511,   512,   513,   504,   514,   484,   486,   493,
     0,   566,     0,     0,   430,     0,     0,   381,     0,   383,
     0,   617,   621,     0,   579,     0,   574,     0,   577,     0,
   560,   606,   607,     0,   561,   596,     0,   562,   489,   495,
     0,   416,     0,     0,     0,    43,   224,    42,   225,    66,
     0,   656,    40,   222,    41,   223,    64,   442,   441,    46,
     0,     0,     0,     0,     0,     0,     0,     0,     0,    34,
    59,     0,     0,     0,   451,   355,     0,     0,     0,     0,
     0,     0,   469,   378,     0,    10,   471,     0,   337,     0,
   338,   296,     0,     0,     0,   348,     0,    18,   508,     0,
     0,     0,     0,     0,     0,     0,     0,   592,     0,   564,
   591,     0,     0,   594,     0,   614,     0,   603,   622,     0,
     0,   609,   459,   420,     0,   418,   456,     0,     0,    39,
     0,     0,     0,   554,   351,   556,   358,   558,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   468,     0,   470,     0,   461,   460,
   462,   341,   344,     0,   509,   432,     0,   437,   436,   382,
     0,     0,     0,   384,     0,   580,     0,   572,     0,   570,
     0,   575,   578,   559,     0,     0,     0,   415,   600,     0,
     0,   398,     0,   611,     0,     0,     0,     0,   353,     0,
     0,     0,     0,     0,     0,     0,     0,     0,   426,     0,
   424,   427,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   413,     0,     0,   408,     0,   395,     0,   411,   419,
   396,     0,     0,     0,     0,     0,   397,   361,     0,     0,
     0,     0,     0,   463,   376,   349,     0,     0,   423,     0,
     0,   573,     0,   568,   571,   576,     0,   399,   421,     0,
     0,   601,     0,     0,     0,   612,   323,     0,     0,     0,
   510,   425,     0,     0,     0,   414,     0,   405,     0,   403,
   394,     0,   409,   412,     0,     0,   569,     0,     0,     0,
     0,   407,     0,   401,   404,   410,     0,   402,
    }, yyDgoto = {
//yyDgoto 202
     1,   350,    69,    70,   666,   590,   591,   208,   436,   727,
   728,   445,   729,   730,   195,    71,    72,    73,    74,    75,
   353,   352,    76,   538,   355,    77,    78,   708,    79,    80,
   437,    81,    82,    83,    84,   624,   447,   448,   311,   312,
    86,    87,    88,    89,   313,   229,   301,   806,   989,   807,
   911,    91,   489,   915,   592,   637,   287,    92,   767,    93,
    94,   614,   615,   731,   210,   835,   231,   840,   841,   547,
  1015,   955,   870,   794,   616,    96,    97,   280,   462,   800,
   319,   232,   314,   491,   545,   544,   733,   734,   848,   549,
   550,   100,   101,   854,  1051,  1087,   939,   736,  1018,  1019,
   737,   325,   492,   283,   102,   529,  1020,   480,   284,   481,
   860,   738,   423,   401,   631,   553,   551,   103,   104,   647,
   233,   211,   212,   739,  1039,   929,   844,   358,   316,  1023,
   268,   493,   849,   850,  1040,   197,   740,   399,   485,   761,
   106,   107,   108,   741,   742,   743,   744,   640,   410,   940,
   269,   270,   111,   112,     2,   238,   239,   511,   501,   486,
   645,   522,   288,   213,   317,   318,   695,   451,   241,   661,
   817,   242,   818,   671,   993,   786,   452,   784,   641,   442,
   643,   644,   909,   746,   872,   359,   712,   711,   548,   554,
   754,   552,   752,   705,   704,   831,   928,   994,  1037,   785,
   795,   441,
    }, yySindex = {
//yySindex 1128
     0,     0, 20019, 21470,     0,     0, 19374, 19770,     0, 22649,
 22649, 18577,     0,     0, 23173, 20414, 20414,     0,     0,     0,
  -269,  -157,     0,     0,     0,     0,   123, 19638,   191,  -142,
   -58,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0, 22780, 22780,  1501, 22780, 22780,   -17, 20151,     0, 20942,
 21338, 18845, 22780, 22911, 19506,     0,     0,     0,   309,   313,
     0,     0,     0,     0,     0,     0,     0,   329,   360,     0,
     0,     0,   -25,     0,     0,     0,   -97,     0,     0,     0,
     0,     0,     0,     0,     0,  1748,   334,  3581,     0,    71,
   517,   363,     0,   378,     0,    65,   357,     0,   409,     0,
     0,     0, 23304,   425,     0,   168,     0,   139,     0,  -158,
 20414, 23435, 23566,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,    -9,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   502,     0,     0, 20283,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   252, 20283,   334,   155,
   553,   227,   511,   270,   155,     0,     0,   139,   310,   555,
     0, 22649, 22649,     0,     0,  -269,  -157,     0,     0,     0,
     0,   269,   191,     0,     0,     0,     0,     0,     0,     0,
     0,  1501,   316,     0,   635,     0,     0,     0,   341,  -158,
     0, 22780, 22780, 22780, 22780,     0, 22780,  3581,     0,     0,
   317,   589,   665,     0,     0,     0, 16873,     0, 20414, 20546,
     0,     0, 18713,     0, 22649,   319,     0, 21732, 20019, 20283,
     0,   953,   397,   402,  4683,  4683,   395, 21601,     0, 20151,
   403,   139,  1748,     0,     0,     0,   191,   191, 21601,   399,
     0,   235,   241,   317,     0,   392,   241,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,   450,
 23697,  1291,     0,   723,     0,     0,     0,     0,     0,     0,
     0,     0,   554,   698,   800,   445,     0,     0,     0,   423,
     0,     0,     0,     0,     0,     0, 22649, 22649, 22649, 22649,
 21601, 22649, 22649, 22780, 22780, 22780, 22780, 22780,     0,     0,
 22780, 22780, 22780, 22780, 22780, 22780, 22780, 22780, 22780, 22780,
 22780, 22780, 22780, 22780,     0,     0, 22780, 22780, 22780, 22780,
     0,     0,     0,     0,  4040, 20414,  4548, 22780,     0,     0,
 24505, 22911,     0, 21863, 20151, 18981,   731, 21863, 22911,     0,
 19112,     0,   427,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0, 22649,    83,     0,   429,  1314,     0,
     0, 22649,     0,     0,     0,     0,     0,   524,   531,   395,
     0, 20283,   530,  5148, 20414,  5614, 22780, 22780, 22780, 20283,
   310, 21994,   535,     0,    96,    96,   461,     0,     0,  6036,
 20414,  8020,     0,     0,     0,     0,   987,     0, 22780, 20678,
     0,     0, 21074,     0,   191,     0,   463,     0, 22780,     0,
     0,   761,   764,   191,   191,   315,     0,     0,     0,     0,
     0, 19770, 22649,  3581,   455,   458,  5148,  5614, 22780, 22780,
  1748,   460,   191,     0,     0, 19243,     0,     0,  1748,     0,
 21206,     0,     0, 21338,     0,     0,     0,     0,     0,   784,
 14096, 20414, 24041, 23697,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,  1302,  -178,     0,     0,  4126,  1415,
   169,   569,     0,   481,     0,     0,     0,     0,     0,     0,
     0,     0,   397,  3197,  3197,  3197,  3197,  6631,  6123,  3197,
  3197,  4683,  4683,   840,   840,   397,  2177,   397,   397,   422,
   422,  2549,  2549,  6545,  3690,   583,   522,     0,   523,  -157,
     0,     0,     0,   191,   527,     0,   533,  -157,     0,     0,
  3690,     0,     0,  -157,     0,   582,  3067,  1370,     0,     0,
    65,   823, 22780,  3067,     0,     0,     0,     0,   845,   191,
 23697,   848,     0,     0,   599,     0,     0,     0,     0,     0,
     0,     0,   334,     0,     0,     0,     0,     0, 24099, 20414,
 24157, 20283,   315,   561, 19902, 19770, 22125,   628,     0,   420,
     0,   558,   565,   191,   567,   574,   628,   652,   694,     0,
     0,     0,     0,     0,     0,     0,  -157,   191,     0,     0,
  -157, 22649, 22780,     0, 22780,   317,   665,     0,     0,     0,
     0, 20678, 21074,     0,     0,     0,     0,   315,     0,     0,
   397,     0, 20019,     0,     0,   191,   241, 23697,     0,     0,
   191,     0,     0,   784,     0,   443,     0,     0,   183,     0,
   890,  4126,  -161,     0,     0,     0,     0,     0,     0,     0,
     0,  1793,     0,     0,     0,     0,     0,     0,   620,   633,
   898,     0,     0,   904,   906,     0,     0,   909,     0,     0,
     0,    24,   915, 22780,     0,   886,   915,     0,   222,     0,
   933,     0,     0,     0,     0,   902,     0, 22911, 22911,     0,
   427,   627,   637, 22911, 22911,     0,   427,     0,     0,    71,
   -97, 21601, 22780, 24215, 20414, 24273, 22911,     0, 22256,     0,
   784, 23697, 21601,   615,   139, 22649, 20283,     0,     0,     0,
   191,   726,     0,  4126, 20283,  4126,     0,     0,     0,     0,
   645,     0, 20283,   729,     0, 22649,     0,   736, 22780, 22780,
   653, 22780, 22780,   741,     0, 22387, 20283, 20283, 20283,     0,
    96,     0,     0,     0,   951,     0,   644,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   191,     0, 20283, 20283,     0,  1793,   787,     0,   966,     0,
   191,     0,     0,  4189,     0,  4217,     0,  4217,     0,   789,
     0,     0,     0,   365,     0,     0, 22780,     0,     0,     0,
 20283,     0,  -143, 20283, 22780,     0,     0,     0,     0,     0,
 22911,     0,     0,     0,     0,     0,     0,     0,     0,     0,
  3581,   522,   523,   191,   527,   533, 22780,     0,   784,     0,
     0, 20283,   139,   748,     0,     0,   191,   752,   139,   561,
 23828,   155,     0,     0, 20283,     0,     0,   155,     0, 22780,
     0,     0,     2,   755,   758,     0, 21074,     0,     0,   681,
   968,   768,   663,   191,  1044,   993,  2072,     0,   994,     0,
     0,   995,   999,     0,  1002,     0,   994,     0,     0,   742,
   915,     0,     0,     0,   876,     0,     0,  3581,  3581,     0,
   627,     0,   785,     0,     0,     0,     0,     0, 20283,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   745,  1399,     0,     0, 20283,     0, 20283,     0,     0,
     0,     0,     0, 20283,     0,     0,  1535,     0,     0,     0,
  1007,  1044,  1277,     0,  4189,     0,  4189,     0,  4217,     0,
  4189,     0,     0,     0,  1008,   191,  1013,     0,     0,  1021,
  1022,     0,   713,     0,   915, 23959,  1015,   915,     0,   817,
     0, 24331, 20414, 24389,   524,   420,   821, 20283,     0,   292,
     0,     0,  1044,  1007,  1044,  1045,   994,  1047,   994,   994,
  4189,     0,   738,  4217,     0,   789,     0,  4217,     0,     0,
     0,     0,     0,   782,  1482, 23959,     0,     0,     0,     0,
   191,     0,     0,     0,     0,     0,   720,  1535,     0,  1007,
  1044,     0,  4189,     0,     0,     0,  1066,     0,     0,  1074,
  1075,     0,   915,  1078,  1066,     0,     0, 24447,  1482,     0,
     0,     0,  1007,   994,  4189,     0,  4189,     0,  4217,     0,
     0,  4189,     0,     0,     0,     0,     0,  1066,  1079,  1066,
  1066,     0,  4189,     0,     0,     0,  1066,     0,
    }, yyRindex = {
//yyRindex 1128
     0,     0,   642,     0,     0,     0,     0,     0,     0,     0,
     0,   854,     0,     0,     0,  2687, 10838,     0,     0,     0,
  5433,  4967, 12378, 12490, 12618, 12730, 23042,     0, 22518,     0,
     0, 12858, 12970, 13098,  5766,  3951, 13210, 13338,  5899, 13450,
     0,     0,     0,     0,     0,     0,     0,    37, 18444,   781,
   766,   158,     0,     0,  1438,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0, 10186,     0,     0,     0, 10291,     0,     0,     0,
     0,     0,     0,     0,     0,    82,  1909,  1894, 10402,  6141,
     0, 13564,     0,  6649,     0, 13671,     0,     0,     0,     0,
     0,     0,   256,     0,     0,     0,     0,    61,     0, 20810,
 10955,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   977,  5073,  5539,  5988,     0,     0,     0,     0,     0,
     0,     0,     0,  6497,  7005,  7513,  8414,     0,     0,     0,
  9954,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
  2141,     0,     0,    88,  2309,  8522,  8638,  8762,  8886,  9002,
  9126,  2797,  9250,  9366,  2935,  9490,     0,    37,  1274,     0,
     0,  9854,     0,     0,     0,     0,     0,   854,     0,   861,
     0,     0,     0,     0,     0, 10623,  9614,   489,   897,   910,
   935,     0,   793,  1046,  1588,  1596,   970,  1749,  1756,  1099,
  1958,     0,     0,     0,     0,  2154,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0, 16591,     0,     0,
  1162,  3177,  3177,     0,     0,     0,   802,     0,     0,    44,
     0,     0,   802,     0,     0,     0,     0,     0,    42,    42,
     0,     0, 11418, 10734, 15983, 16071, 13781,     0, 18039,    37,
     0,  3066,   669,     0,     0,   146,   802,   802,     0,     0,
     0,   804,   804,     0,     0,     0,   790,  2069,  2280,  2615,
  9068,  9432,  9825,  9946,  1358, 10475, 11171,  1724, 11203,     0,
     0,     0, 13309,   279,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,  3536,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0, 11086, 11288,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,    97,     0,     0,     0,     0,
     0,     0,     0,     0,    37,   336,   446,     0,     0,     0,
    89,     0,  3685,     0,     0,     0,     0,     0,     0,     0,
     0,     0, 17006, 17142,     0,     0,     0, 18175,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   573,     0, 10070,
     0,   896, 17770,     0,    97,     0,     0,     0,     0,   700,
     0,     0,     0,     0,     0,     0,     0,     0,  2223,     0,
    97,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   802,     0,     0,     0,    80,     0,
     0,   136,   136,   802,   802,   802,     0,     0,     0,     0,
     0,     0,     0, 16287,     0,     0,     0,     0,     0,     0,
   735,     0,   802,     0,     0,  3077,   274,     0,   166,     0,
   809,     0,     0,   -64,     0,     0,     0, 13421,     0,   601,
     0,    97,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0, 11529, 15177, 15308, 15398, 15486, 15619, 15895, 15707,
 15795, 16135, 16199, 14331, 14435, 11658, 14572, 11770, 11898, 13886,
 14003, 14683, 14787,  1089, 14904,     0,  6274,  4326,  7798, 20810,
     0,  4459,     0,   814,  6407,     0,  6782,  5300,     0,     0,
 15035,     0,     0,  8173,     0,  2421, 16679,     0,     0,     0,
 14218,     0,     0,  1488,     0,     0,     0,     0,     0,   802,
     0,   673,     0,     0,  7665,     0,  1694,     0,     0,     0,
     0,     0,   150,     0, 17636,     0,     0,     0,     0,    97,
     0,    88,   802,  1454,     0,     0,   594,   480,     0,   885,
     0,  3310,  4834,   814,  3443,  3818,   885,     0,     0,     0,
     0,     0,     0,     0,  1522,  1020,     0,   814,  2416,  2423,
  9730,     0,     0,     0,     0,  1961,  3177,     0,     0,     0,
     0,   144,   180,     0,     0,     0,     0,   802,     0,     0,
 12010,     0,    42,   120,     0,   802,   804,     0,  1935,  1656,
   814,  1985,  2552,   689,     0,     0,     0,     0,     0,     0,
     0,   151,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,    43,     0,
   240,     0,     0,   240,   240,     0,     0,   245,     0,     0,
     0,   182,   245,   194,     0,   232,   245,     0,     0,     0,
     0,     0, 17906,     0, 18308,     0,     0,     0,     0,     0,
  4193, 12138,     0,     0,     0,     0,  4701,     0,     0,  7157,
  2508,     0,     0,     0,    97,     0,     0, 15265,     0,     0,
   697,     0,     0,     0,     0,     0,    88, 17322, 17456,     0,
   814,     0,     0,   167,    88,   177,     0,     0,     0,   288,
   321,     0,   198,   885,     0,     0,     0,     0,     0,     0,
  8306,     0,     0,     0,     0,     0,  1095,   114,   114,  2633,
     0,     0,     0,     0,   136,     0,     0,     0,     0,     0,
  2456,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   -23,     0,    88,    42,     0,     0,   174,     0,   178,     0,
   802,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    88,     0,     0,    42,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
 16375,  6915,  7931,   814,  7290,  7423,     0, 16695,   703,     0,
     0,    88,     0,     0,     0,     0,   802,     0,     0,  1454,
     0,     0,     0,     0,   114,     0,     0,     0,     0,     0,
     0,     0,   885,     0,     0,     0,   207,     0,     0,     0,
   286,     0,     0,   802,     0,   195,     0,     0,   240,     0,
     0,   240,   240,     0,   240,     0,   240,     0,     0,   182,
   245,     0,     0,     0,   -24,     0,     0, 16439, 16503,     0,
 12250, 16779,     0,     0,     0,     0,     0,     0,    88,   722,
   775,   965,   986,  1043,  1388,  1520,  1411,  1644,  1811,  1670,
  1883,     0,     0,  2159,     0,    88,     0,   896,     0,     0,
     0,     0,     0,   114,     0,     0,     0,     0,     0,     0,
   221,     0,   233,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,    34,   -24,    34,     0,     0,    53,
    34,     0,     0,     0,    53,    81,    84,    53,     0,     0,
  2385,     0,    97,     0,   573,   885,     0,    36,     0,   311,
     0,     0,     0,   239,     0,   248,   240,   240,   240,   240,
     0,     0,     0,   140,     0,     0,     0,     0,     0,     0,
     0,  2047, 10019,     0,   113,     0,     0,     0,  8412,   572,
   814,  8704, 10489,     0,     0,     0,     0,     0,     0,   250,
     0,     0,     0,     0,     0,     0,    34,     0,     0,    34,
    34,     0,    53,    34,    34,     0,     0,     0,   135,   903,
     0,     0,   259,   240,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0, 10508,   236,     0,    34,    34,    34,
    34,     0,     0,     0,     0,     0,    34,     0,
    }, yyGindex = {
//yyGindex 202
     0,     0,    31,     0,  -337,     0,    -4,     5,  -284,   648,
     0,     0,     0,   388,     0,     0,     0,  1091,     0,     0,
   917,  1122,     0,  -285,     0,     0,     0,   630,     0,    33,
  1069,  -282,   -27,     0,    99,     0,   351,   107,     0,    78,
   299,  2066,    15,   -11,   687,     6,    -2,  -476,     0,   170,
     0,     0,  1129,     0,    54,     0,    10,  1197,   614,     0,
     0,  -279,   112,  -736,     0,     0,   101,  -473,   671,     0,
     0,     0,   468,   314,  -297,   -78,    14,   928,  -445,     0,
     0,  1254,    35,    56,     0,     0,  6810,   375,  -848,     0,
     0,     0,     0,   545,  2898,   282,  -437,   374,   187,     0,
     0,     0,    27,  -461,     0,   121,   184,  -255,  -454,     0,
  -363, 10379,   -70,   475,  -537,   621,   860,  1248,    17,   242,
  1518,     0,     3,  -741,     0,  -754,     0,     0,  -120,  -888,
     0,  -357,  -774,   421,   192,     0,  -821,  1179,  -260,  -612,
  -264,     0,    30,  -721,  1814,  1815,   988,   -71,     0,   163,
   841,  1105,     0,     0,     0,    51,    60,     0,     0,    -8,
     0,  -243,     0,     0,     0,     0,     0,  -224,     0,  -387,
     0,     0,     0,     0,     0,     0,    28,     0,     0,     0,
     0,     0,     0,  2407,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,
    };
    protected static final short[] yyTable = YyTables.yyTable();
    protected static final short[] yyCheck = YyTables.yyCheck();

  /** maps symbol value to printable name.
      @see #yyExpecting
    */
  protected static final String[] yyNames = {
    "end-of-file",null,null,null,null,null,null,null,null,null,"'\\n'",
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,"' '",null,null,null,null,null,
    null,null,null,null,null,null,"','",null,null,null,null,null,null,
    null,null,null,null,null,null,null,"':'","';'",null,"'='",null,"'?'",
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,
    "'['",null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,
    "keyword_class","keyword_module","keyword_def","keyword_undef",
    "keyword_begin","keyword_rescue","keyword_ensure","keyword_end",
    "keyword_if","keyword_unless","keyword_then","keyword_elsif",
    "keyword_else","keyword_case","keyword_when","keyword_while",
    "keyword_until","keyword_for","keyword_break","keyword_next",
    "keyword_redo","keyword_retry","keyword_in","keyword_do",
    "keyword_do_cond","keyword_do_block","keyword_return","keyword_yield",
    "keyword_super","keyword_self","keyword_nil","keyword_true",
    "keyword_false","keyword_and","keyword_or","keyword_not",
    "modifier_if","modifier_unless","modifier_while","modifier_until",
    "modifier_rescue","keyword_alias","keyword_defined","keyword_BEGIN",
    "keyword_END","keyword__LINE__","keyword__FILE__",
    "keyword__ENCODING__","keyword_do_lambda","tIDENTIFIER","tFID",
    "tGVAR","tIVAR","tCONSTANT","tCVAR","tLABEL","tCHAR","tUPLUS",
    "tUMINUS","tUMINUS_NUM","tPOW","tCMP","tEQ","tEQQ","tNEQ","tGEQ",
    "tLEQ","tANDOP","tOROP","tMATCH","tNMATCH","tDOT","tDOT2","tDOT3",
    "tBDOT2","tBDOT3","tAREF","tASET","tLSHFT","tRSHFT","tANDDOT",
    "tCOLON2","tCOLON3","tOP_ASGN","tASSOC","tLPAREN","tLPAREN2",
    "tRPAREN","tLPAREN_ARG","tLBRACK","tRBRACK","tLBRACE","tLBRACE_ARG",
    "tSTAR","tSTAR2","tAMPER","tAMPER2","tTILDE","tPERCENT","tDIVIDE",
    "tPLUS","tMINUS","tLT","tGT","tPIPE","tBANG","tCARET","tLCURLY",
    "tRCURLY","tBACK_REF2","tSYMBEG","tSTRING_BEG","tXSTRING_BEG",
    "tREGEXP_BEG","tWORDS_BEG","tQWORDS_BEG","tSTRING_DBEG",
    "tSTRING_DVAR","tSTRING_END","tLAMBDA","tLAMBEG","tNTH_REF",
    "tBACK_REF","tSTRING_CONTENT","tINTEGER","tIMAGINARY","tFLOAT",
    "tRATIONAL","tREGEXP_END","tSYMBOLS_BEG","tQSYMBOLS_BEG","tDSTAR",
    "tSTRING_DEND","tLABEL_END","tLOWEST",
    };


  /** computes list of expected tokens on error by tracing the tables.
      @param state for which to compute the list.
      @return list of token names.
    */
  protected String[] yyExpecting (int state) {
    int token, n, len = 0;
    boolean[] ok = new boolean[yyNames.length];

    if ((n = yySindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           token < yyNames.length && n+token < yyTable.length; ++ token)
        if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
          ++ len;
          ok[token] = true;
        }
    if ((n = yyRindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           token < yyNames.length && n+token < yyTable.length; ++ token)
        if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
          ++ len;
          ok[token] = true;
        }

    String result[] = new String[len];
    for (n = token = 0; n < len;  ++ token)
      if (ok[token]) result[n++] = yyNames[token];
    return result;
  }

  /** the generated parser, with debugging messages.
      Maintains a dynamic state and value stack.
      @param yyLex scanner.
      @return result of the last reduction, if any.
    */
  public Object yyparse (RubyLexer yyLex, Object ayydebug) {
    return yyparse(yyLex);
  }

  /** initial size and increment of the state/value stack [default 256].
      This is not final so that it can be overwritten outside of invocations
      of {@link #yyparse}.
    */
  protected int yyMax;

  /** executed at the beginning of a reduce action.
      Used as <tt>$$ = yyDefault($1)</tt>, prior to the user-specified action, if any.
      Can be overwritten to provide deep copy, etc.
      @param first value for <tt>$1</tt>, or <tt>null</tt>.
      @return first.
    */
  protected Object yyDefault (Object first) {
    return first;
  }

  /** the generated parser.
      Maintains a dynamic state and value stack.
      @param yyLex scanner.
      @return result of the last reduction, if any.
    */
  public Object yyparse (RubyLexer yyLex) {
    if (yyMax <= 0) yyMax = 256;			// initial size
    int yyState = 0, yyStates[] = new int[yyMax];	// state stack
    Object yyVal = null, yyVals[] = new Object[yyMax];	// value stack
    int yyToken = -1;					// current input
    int yyErrorFlag = 0;				// #tokens to shift

    yyLoop: for (int yyTop = 0;; ++ yyTop) {
      if (yyTop >= yyStates.length) {			// dynamically increase
        int[] i = new int[yyStates.length+yyMax];
        System.arraycopy(yyStates, 0, i, 0, yyStates.length);
        yyStates = i;
        Object[] o = new Object[yyVals.length+yyMax];
        System.arraycopy(yyVals, 0, o, 0, yyVals.length);
        yyVals = o;
      }
      yyStates[yyTop] = yyState;
      yyVals[yyTop] = yyVal;

      yyDiscarded: for (;;) {	// discarding a token does not change stack
        int yyN;
        if ((yyN = yyDefRed[yyState]) == 0) {	// else [default] reduce (yyN)
          if (yyToken < 0) {
//            yyToken = yyLex.advance() ? yyLex.token() : 0;
            yyToken = yyLex.nextToken();
          }
          if ((yyN = yySindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < yyTable.length && yyCheck[yyN] == yyToken) {
            yyState = yyTable[yyN];		// shift to yyN
            yyVal = yyLex.value();
            yyToken = -1;
            if (yyErrorFlag > 0) -- yyErrorFlag;
            continue yyLoop;
          }
          if ((yyN = yyRindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < yyTable.length && yyCheck[yyN] == yyToken)
            yyN = yyTable[yyN];			// reduce (yyN)
          else
            switch (yyErrorFlag) {
  
            case 0:
              support.yyerror("syntax error", yyExpecting(yyState), yyNames[yyToken]);
              break;
  
            case 1: case 2:
              yyErrorFlag = 3;
              do {
                if ((yyN = yySindex[yyStates[yyTop]]) != 0
                    && (yyN += yyErrorCode) >= 0 && yyN < yyTable.length
                    && yyCheck[yyN] == yyErrorCode) {
                  yyState = yyTable[yyN];
                  yyVal = yyLex.value();
                  continue yyLoop;
                }
              } while (-- yyTop >= 0);
              support.yyerror("irrecoverable syntax error");
              break;

            case 3:
              if (yyToken == 0) {
                support.yyerror("irrecoverable syntax error at end-of-file");
              }
              yyToken = -1;
              continue yyDiscarded;		// leave stack alone
            }
        }
        int yyV = yyTop + 1-yyLen[yyN];
        ParserState state = states[yyN];
        if (state == null) {
            yyVal = yyDefault(yyV > yyTop ? null : yyVals[yyV]);
        } else {
            yyVal = state.execute(support, lexer, yyVal, yyVals, yyTop);
        }
//        switch (yyN) {
// ACTIONS_END
//        }
        yyTop -= yyLen[yyN];
        yyState = yyStates[yyTop];
        int yyM = yyLhs[yyN];
        if (yyState == 0 && yyM == 0) {
          yyState = yyFinal;
          if (yyToken < 0) {
            yyToken = yyLex.nextToken();
//            yyToken = yyLex.advance() ? yyLex.token() : 0;
          }
          if (yyToken == 0) {
            return yyVal;
          }
          continue yyLoop;
        }
        if ((yyN = yyGindex[yyM]) != 0 && (yyN += yyState) >= 0
            && yyN < yyTable.length && yyCheck[yyN] == yyState)
          yyState = yyTable[yyN];
        else
          yyState = yyDgoto[yyM];
        continue yyLoop;
      }
    }
  }

static ParserState[] states = new ParserState[666];
static {
states[1] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_BEG);
    support.initTopLocalVariables();
    return yyVal;
};
states[2] = (support, lexer, yyVal, yyVals, yyTop) -> {
  /* ENEBO: Removed !compile_for_eval which probably is to reduce warnings*/
                  if (((ParseNode)yyVals[0+yyTop]) != null) {
                      /* last expression should not be void */
                      if (((ParseNode)yyVals[0+yyTop]) instanceof BlockParseNode) {
                          support.checkUselessStatement(((BlockParseNode)yyVals[0+yyTop]).getLast());
                      } else {
                          support.checkUselessStatement(((ParseNode)yyVals[0+yyTop]));
                      }
                  }
                  support.getResult().setAST(support.addRootNode(((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[3] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-1+yyTop]) instanceof BlockParseNode) {
        support.checkUselessStatements(((BlockParseNode)yyVals[-1+yyTop]));
    }
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[5] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newline_node(((ParseNode)yyVals[0+yyTop]), support.getPosition(((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[6] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.appendToBlock(((ParseNode)yyVals[-2+yyTop]), support.newline_node(((ParseNode)yyVals[0+yyTop]), support.getPosition(((ParseNode)yyVals[0+yyTop]))));
    return yyVal;
};
states[7] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[9] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.getResult().addBeginNode(new PreExe19ParseNode(((SourceIndexLength)yyVals[-3+yyTop]), support.getCurrentScope(), ((ParseNode)yyVals[-1+yyTop])));
    yyVal = null;
    return yyVal;
};
states[10] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node = ((ParseNode)yyVals[-3+yyTop]);

    if (((RescueBodyParseNode)yyVals[-2+yyTop]) != null) {
        node = new RescueParseNode(support.getPosition(((ParseNode)yyVals[-3+yyTop])), ((ParseNode)yyVals[-3+yyTop]), ((RescueBodyParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    } else if (((ParseNode)yyVals[-1+yyTop]) != null) {
        support.warn(support.getPosition(((ParseNode)yyVals[-3+yyTop])), "else without rescue is useless");
        node = support.appendToBlock(((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    }
    if (((ParseNode)yyVals[0+yyTop]) != null) {
        if (node != null) {
            node = new EnsureParseNode(support.getPosition(((ParseNode)yyVals[-3+yyTop])), support.makeNullNil(node), ((ParseNode)yyVals[0+yyTop]));
        } else {
            node = support.appendToBlock(((ParseNode)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
        }
    }

    support.fixpos(node, ((ParseNode)yyVals[-3+yyTop]));
    yyVal = node;
    return yyVal;
};
states[11] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-1+yyTop]) instanceof BlockParseNode) {
        support.checkUselessStatements(((BlockParseNode)yyVals[-1+yyTop]));
    }
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[13] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newline_node(((ParseNode)yyVals[0+yyTop]), support.getPosition(((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[14] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.appendToBlock(((ParseNode)yyVals[-2+yyTop]), support.newline_node(((ParseNode)yyVals[0+yyTop]), support.getPosition(((ParseNode)yyVals[0+yyTop]))));
    return yyVal;
};
states[15] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[16] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[17] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("BEGIN is permitted only at toplevel");
    return yyVal;
};
states[18] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BeginParseNode(((SourceIndexLength)yyVals[-4+yyTop]), support.makeNullNil(((ParseNode)yyVals[-3+yyTop])));
    return yyVal;
};
states[19] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_FNAME|EXPR_FITEM);
    return yyVal;
};
states[20] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newAlias(((SourceIndexLength)yyVals[-3+yyTop]), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[21] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new VAliasParseNode(((SourceIndexLength)yyVals[-2+yyTop]), support.symbolID(((Rope)yyVals[-1+yyTop])), support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[22] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new VAliasParseNode(((SourceIndexLength)yyVals[-2+yyTop]), support.symbolID(((Rope)yyVals[-1+yyTop])), support.symbolID(((BackRefParseNode)yyVals[0+yyTop]).getByteName()));
    return yyVal;
};
states[23] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("can't make alias for the number variables");
    return yyVal;
};
states[24] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[25] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[-2+yyTop]), null);
    support.fixpos(((ParseNode)yyVal), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[26] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.getConditionNode(((ParseNode)yyVals[0+yyTop])), null, ((ParseNode)yyVals[-2+yyTop]));
    support.fixpos(((ParseNode)yyVal), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[27] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-2+yyTop]) != null && ((ParseNode)yyVals[-2+yyTop]) instanceof BeginParseNode) {
        yyVal = new WhileParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((BeginParseNode)yyVals[-2+yyTop]).getBodyNode(), false);
    } else {
        yyVal = new WhileParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[-2+yyTop]), true);
    }
    return yyVal;
};
states[28] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-2+yyTop]) != null && ((ParseNode)yyVals[-2+yyTop]) instanceof BeginParseNode) {
        yyVal = new UntilParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((BeginParseNode)yyVals[-2+yyTop]).getBodyNode(), false);
    } else {
        yyVal = new UntilParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[-2+yyTop]), true);
    }
    return yyVal;
};
states[29] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newRescueModNode(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[30] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) {
        support.warn(((SourceIndexLength)yyVals[-3+yyTop]), "END in method; use at_exit");
    }
    yyVal = new PostExeParseNode(((SourceIndexLength)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[32] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    ((MultipleAsgnParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
    yyVal = ((MultipleAsgnParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[33] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.node_assign(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[34] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[-2+yyTop]));
    yyVal = support.node_assign(((MultipleAsgnParseNode)yyVals[-4+yyTop]), support.newRescueModNode(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[35] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
    yyVal = ((MultipleAsgnParseNode)yyVals[-2+yyTop]);
    ((MultipleAsgnParseNode)yyVals[-2+yyTop]).setPosition(support.getPosition(((MultipleAsgnParseNode)yyVals[-2+yyTop])));
    return yyVal;
};
states[37] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.node_assign(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[38] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));

    SourceIndexLength pos = ((AssignableParseNode)yyVals[-2+yyTop]).getPosition();
    Rope asgnOp = ((Rope)yyVals[-1+yyTop]);
    if (asgnOp == RopeConstants.OR_OR) {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
        yyVal = new OpAsgnOrParseNode(pos, support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), ((AssignableParseNode)yyVals[-2+yyTop]));
    } else if (asgnOp == RopeConstants.AMPERSAND_AMPERSAND) {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
        yyVal = new OpAsgnAndParseNode(pos, support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), ((AssignableParseNode)yyVals[-2+yyTop]));
    } else {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(support.getOperatorCallNode(support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), asgnOp, ((ParseNode)yyVals[0+yyTop])));
        ((AssignableParseNode)yyVals[-2+yyTop]).setPosition(pos);
        yyVal = ((AssignableParseNode)yyVals[-2+yyTop]);
    }
    return yyVal;
};
states[39] = (support, lexer, yyVal, yyVals, yyTop) -> {
  /* FIXME: arg_concat logic missing for opt_call_args*/
                    yyVal = support.new_opElementAsgnNode(((ParseNode)yyVals[-5+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[40] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[41] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[42] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength pos = ((ParseNode)yyVals[-4+yyTop]).getPosition();
    yyVal = support.newOpConstAsgn(pos, support.new_colon2(pos, ((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop])), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[43] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[44] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.backrefAssignError(((ParseNode)yyVals[-2+yyTop]));
    return yyVal;
};
states[45] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[46] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[-2+yyTop]));
    yyVal = support.newRescueModNode(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[49] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newAndNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[50] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newOrNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[51] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode)yyVals[0+yyTop])), RopeConstants.BANG);
    return yyVal;
};
states[52] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[54] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[58] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[59] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((IterParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[60] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_fcall(((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[61] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.frobnicate_fcall_args(((FCallParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    yyVal = ((FCallParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[62] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.frobnicate_fcall_args(((FCallParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((IterParseNode)yyVals[0+yyTop]));
    yyVal = ((FCallParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[63] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[64] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop]), ((Rope)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((IterParseNode)yyVals[0+yyTop])); 
    return yyVal;
};
states[65] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[66] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((IterParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[67] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_super(((SourceIndexLength)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[68] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_yield(((SourceIndexLength)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[69] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ReturnParseNode(((SourceIndexLength)yyVals[-1+yyTop]), support.ret_args(((ParseNode)yyVals[0+yyTop]), ((SourceIndexLength)yyVals[-1+yyTop])));
    return yyVal;
};
states[70] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BreakParseNode(((SourceIndexLength)yyVals[-1+yyTop]), support.ret_args(((ParseNode)yyVals[0+yyTop]), ((SourceIndexLength)yyVals[-1+yyTop])));
    return yyVal;
};
states[71] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new NextParseNode(((SourceIndexLength)yyVals[-1+yyTop]), support.ret_args(((ParseNode)yyVals[0+yyTop]), ((SourceIndexLength)yyVals[-1+yyTop])));
    return yyVal;
};
states[73] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[74] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((MultipleAsgnParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[75] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((SourceIndexLength)yyVals[-2+yyTop]), support.newArrayNode(((SourceIndexLength)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop])), null, null);
    return yyVal;
};
states[76] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[0+yyTop]).getPosition(), ((ListParseNode)yyVals[0+yyTop]), null, null);
    return yyVal;
};
states[77] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]).add(((ParseNode)yyVals[0+yyTop])), null, null);
    return yyVal;
};
states[78] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-2+yyTop]).getPosition(), ((ListParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]), (ListParseNode) null);
    return yyVal;
};
states[79] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-4+yyTop]).getPosition(), ((ListParseNode)yyVals[-4+yyTop]), ((ParseNode)yyVals[-2+yyTop]), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[80] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), new StarParseNode(lexer.getPosition()), null);
    return yyVal;
};
states[81] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), new StarParseNode(lexer.getPosition()), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[82] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ParseNode)yyVals[0+yyTop]).getPosition(), null, ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[83] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ParseNode)yyVals[-2+yyTop]).getPosition(), null, ((ParseNode)yyVals[-2+yyTop]), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[84] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), null);
    return yyVal;
};
states[85] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[87] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[88] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((ParseNode)yyVals[-1+yyTop]).getPosition(), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[89] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[90] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[91] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[92] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.assignableLabelOrIdentifier(((Rope)yyVals[0+yyTop]), null);
    return yyVal;
};
states[93] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstAsgnParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[94] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalAsgnParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[95] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) support.compile_error("dynamic constant assignment");
    yyVal = new ConstDeclParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), null, NilImplicitParseNode.NIL);
    return yyVal;
};
states[96] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarAsgnParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[97] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to nil");
    yyVal = null;
    return yyVal;
};
states[98] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't change the value of self");
    yyVal = null;
    return yyVal;
};
states[99] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to true");
    yyVal = null;
    return yyVal;
};
states[100] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to false");
    yyVal = null;
    return yyVal;
};
states[101] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __FILE__");
    yyVal = null;
    return yyVal;
};
states[102] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __LINE__");
    yyVal = null;
    return yyVal;
};
states[103] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __ENCODING__");
    yyVal = null;
    return yyVal;
};
states[104] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.aryset(((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[105] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[106] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[107] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[108] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) support.yyerror("dynamic constant assignment");

    SourceIndexLength position = support.getPosition(((ParseNode)yyVals[-2+yyTop]));

    yyVal = new ConstDeclParseNode(position, (Rope) null, support.new_colon2(position, ((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[109] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) {
        support.yyerror("dynamic constant assignment");
    }

    SourceIndexLength position = lexer.tokline;

    yyVal = new ConstDeclParseNode(position, (Rope) null, support.new_colon3(position, ((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[110] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.backrefAssignError(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[111] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.assignableLabelOrIdentifier(((Rope)yyVals[0+yyTop]), null);
    return yyVal;
};
states[112] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstAsgnParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[113] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalAsgnParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[114] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) support.compile_error("dynamic constant assignment");

    yyVal = new ConstDeclParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), null, NilImplicitParseNode.NIL);
    return yyVal;
};
states[115] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarAsgnParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[116] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to nil");
    yyVal = null;
    return yyVal;
};
states[117] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't change the value of self");
    yyVal = null;
    return yyVal;
};
states[118] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to true");
    yyVal = null;
    return yyVal;
};
states[119] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to false");
    yyVal = null;
    return yyVal;
};
states[120] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __FILE__");
    yyVal = null;
    return yyVal;
};
states[121] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __LINE__");
    yyVal = null;
    return yyVal;
};
states[122] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __ENCODING__");
    yyVal = null;
    return yyVal;
};
states[123] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.aryset(((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[124] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[125] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[126] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[127] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) {
        support.yyerror("dynamic constant assignment");
    }

    SourceIndexLength position = support.getPosition(((ParseNode)yyVals[-2+yyTop]));

    yyVal = new ConstDeclParseNode(position, (Rope) null, support.new_colon2(position, ((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[128] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) {
        support.yyerror("dynamic constant assignment");
    }

    SourceIndexLength position = lexer.tokline;

    yyVal = new ConstDeclParseNode(position, (Rope) null, support.new_colon3(position, ((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[129] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.backrefAssignError(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[130] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("class/module name must be CONSTANT");
    return yyVal;
};
states[131] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[132] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon3(lexer.tokline, ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[133] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon2(lexer.tokline, null, ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[134] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon2(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[135] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[136] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[137] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[138] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_ENDFN);
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[139] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_ENDFN);
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[140] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new LiteralParseNode(lexer.getPosition(), support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[141] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new LiteralParseNode(lexer.getPosition(), support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[142] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((LiteralParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[143] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[144] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newUndef(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[145] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_FNAME|EXPR_FITEM);
    return yyVal;
};
states[146] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.appendToBlock(((ParseNode)yyVals[-3+yyTop]), support.newUndef(((ParseNode)yyVals[-3+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[147] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[148] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[149] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[150] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[151] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[152] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[153] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[154] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[155] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[156] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[157] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[158] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[159] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[160] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[161] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[162] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[163] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[164] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[165] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[166] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[167] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[168] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[169] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[170] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[171] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[172] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[173] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[174] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[175] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[176] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[177] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.__LINE__.bytes;
    return yyVal;
};
states[178] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.__FILE__.bytes;
    return yyVal;
};
states[179] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.__ENCODING__.bytes;
    return yyVal;
};
states[180] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.LBEGIN.bytes;
    return yyVal;
};
states[181] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.LEND.bytes;
    return yyVal;
};
states[182] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.ALIAS.bytes;
    return yyVal;
};
states[183] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.AND.bytes;
    return yyVal;
};
states[184] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.BEGIN.bytes;
    return yyVal;
};
states[185] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.BREAK.bytes;
    return yyVal;
};
states[186] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.CASE.bytes;
    return yyVal;
};
states[187] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.CLASS.bytes;
    return yyVal;
};
states[188] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.DEF.bytes;
    return yyVal;
};
states[189] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.DEFINED_P.bytes;
    return yyVal;
};
states[190] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.DO.bytes;
    return yyVal;
};
states[191] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.ELSE.bytes;
    return yyVal;
};
states[192] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.ELSIF.bytes;
    return yyVal;
};
states[193] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.END.bytes;
    return yyVal;
};
states[194] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.ENSURE.bytes;
    return yyVal;
};
states[195] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.FALSE.bytes;
    return yyVal;
};
states[196] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.FOR.bytes;
    return yyVal;
};
states[197] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.IN.bytes;
    return yyVal;
};
states[198] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.MODULE.bytes;
    return yyVal;
};
states[199] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.NEXT.bytes;
    return yyVal;
};
states[200] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.NIL.bytes;
    return yyVal;
};
states[201] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.NOT.bytes;
    return yyVal;
};
states[202] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.OR.bytes;
    return yyVal;
};
states[203] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.REDO.bytes;
    return yyVal;
};
states[204] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.RESCUE.bytes;
    return yyVal;
};
states[205] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.RETRY.bytes;
    return yyVal;
};
states[206] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.RETURN.bytes;
    return yyVal;
};
states[207] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.SELF.bytes;
    return yyVal;
};
states[208] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.SUPER.bytes;
    return yyVal;
};
states[209] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.THEN.bytes;
    return yyVal;
};
states[210] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.TRUE.bytes;
    return yyVal;
};
states[211] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.UNDEF.bytes;
    return yyVal;
};
states[212] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.WHEN.bytes;
    return yyVal;
};
states[213] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.YIELD.bytes;
    return yyVal;
};
states[214] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.IF.bytes;
    return yyVal;
};
states[215] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.UNLESS.bytes;
    return yyVal;
};
states[216] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.WHILE.bytes;
    return yyVal;
};
states[217] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.UNTIL.bytes;
    return yyVal;
};
states[218] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RubyLexer.Keyword.RESCUE.bytes;
    return yyVal;
};
states[219] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.node_assign(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    /* FIXME: Consider fixing node_assign itself rather than single case*/
    ((ParseNode)yyVal).setPosition(support.getPosition(((ParseNode)yyVals[-2+yyTop])));
    return yyVal;
};
states[220] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));

    SourceIndexLength pos = ((AssignableParseNode)yyVals[-2+yyTop]).getPosition();
    Rope asgnOp = ((Rope)yyVals[-1+yyTop]);
    if (asgnOp == RopeConstants.OR_OR) {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
        yyVal = new OpAsgnOrParseNode(pos, support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), ((AssignableParseNode)yyVals[-2+yyTop]));
    } else if (asgnOp == RopeConstants.AMPERSAND_AMPERSAND) {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
        yyVal = new OpAsgnAndParseNode(pos, support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), ((AssignableParseNode)yyVals[-2+yyTop]));
    } else {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(support.getOperatorCallNode(support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), asgnOp, ((ParseNode)yyVals[0+yyTop])));
        ((AssignableParseNode)yyVals[-2+yyTop]).setPosition(pos);
        yyVal = ((AssignableParseNode)yyVals[-2+yyTop]);
    }
    return yyVal;
};
states[221] = (support, lexer, yyVal, yyVals, yyTop) -> {
  /* FIXME: arg_concat missing for opt_call_args*/
                    yyVal = support.new_opElementAsgnNode(((ParseNode)yyVals[-5+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[222] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[223] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[224] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[225] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength pos = support.getPosition(((ParseNode)yyVals[-4+yyTop]));
    yyVal = support.newOpConstAsgn(pos, support.new_colon2(pos, ((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-2+yyTop])), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[226] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength pos = lexer.getPosition();
    yyVal = support.newOpConstAsgn(pos, new Colon3ParseNode(pos, support.symbolID(((Rope)yyVals[-2+yyTop]))), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[227] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.backrefAssignError(((ParseNode)yyVals[-2+yyTop]));
    return yyVal;
};
states[228] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[-2+yyTop]));
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    
    boolean isLiteral = ((ParseNode)yyVals[-2+yyTop]) instanceof FixnumParseNode && ((ParseNode)yyVals[0+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.makeNullNil(((ParseNode)yyVals[-2+yyTop])), support.makeNullNil(((ParseNode)yyVals[0+yyTop])), false, isLiteral);
    return yyVal;
};
states[229] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.checkExpression(((ParseNode)yyVals[-1+yyTop]));

    boolean isLiteral = ((ParseNode)yyVals[-1+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[-1+yyTop])), support.makeNullNil(((ParseNode)yyVals[-1+yyTop])), NilImplicitParseNode.NIL, false, isLiteral);
    return yyVal;
};
states[230] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));

    boolean isLiteral = ((ParseNode)yyVals[0+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), NilImplicitParseNode.NIL, support.makeNullNil(((ParseNode)yyVals[0+yyTop])), false, isLiteral);
    return yyVal;
};
states[231] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[-2+yyTop]));
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));

    boolean isLiteral = ((ParseNode)yyVals[-2+yyTop]) instanceof FixnumParseNode && ((ParseNode)yyVals[0+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.makeNullNil(((ParseNode)yyVals[-2+yyTop])), support.makeNullNil(((ParseNode)yyVals[0+yyTop])), true, isLiteral);
    return yyVal;
};
states[232] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.checkExpression(((ParseNode)yyVals[-1+yyTop]));

    boolean isLiteral = ((ParseNode)yyVals[-1+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[-1+yyTop])), support.makeNullNil(((ParseNode)yyVals[-1+yyTop])), NilImplicitParseNode.NIL, true, isLiteral);
    return yyVal;
};
states[233] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));

    boolean isLiteral = ((ParseNode)yyVals[0+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), NilImplicitParseNode.NIL, support.makeNullNil(((ParseNode)yyVals[0+yyTop])), true, isLiteral);
    return yyVal;
};
states[234] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[235] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[236] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[237] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[238] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[239] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[240] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(support.getOperatorCallNode(((NumericParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition()), ((Rope)yyVals[-3+yyTop]));
    return yyVal;
};
states[241] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[0+yyTop]), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[242] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[0+yyTop]), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[243] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[244] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[245] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[246] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[247] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[248] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[249] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[250] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[251] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getMatchNode(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
  /* ENEBO
        $$ = match_op($1, $3);
        if (nd_type($1) == NODE_LIT && TYPE($1->nd_lit) == T_REGEXP) {
            $$ = reg_named_capture_assign($1->nd_lit, $$);
        }
  */
    return yyVal;
};
states[252] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[253] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[254] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[0+yyTop]), ((Rope)yyVals[-1+yyTop]));
    return yyVal;
};
states[255] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[256] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[257] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newAndNode(((ParseNode)yyVals[-2+yyTop]).getPosition(), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[258] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newOrNode(((ParseNode)yyVals[-2+yyTop]).getPosition(), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[259] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_defined(((SourceIndexLength)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[260] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[-5+yyTop]));
    yyVal = new IfParseNode(support.getPosition(((ParseNode)yyVals[-5+yyTop])), support.getConditionNode(((ParseNode)yyVals[-5+yyTop])), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[261] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[262] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[263] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[264] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[265] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[266] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[267] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.warning(lexer.getPosition(), "comparison '" + ((Rope)yyVals[-1+yyTop]).getString() + "' after comparison");
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[268] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.makeNullNil(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[270] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[271] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.arg_append(((ParseNode)yyVals[-3+yyTop]), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[272] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((HashParseNode)yyVals[-1+yyTop]).getPosition(), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[273] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[274] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[-2+yyTop]));
    yyVal = support.newRescueModNode(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[275] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    if (yyVal != null) ((ParseNode)yyVal).setPosition(((SourceIndexLength)yyVals[-2+yyTop]));
    return yyVal;
};
states[276] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(null);
    /* NOTE(norswap, 06 Nov 2020): location (0) arg is unused*/
    SplatParseNode splat = support.newSplatNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_REST_VAR));
    BlockPassParseNode block = new BlockPassParseNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_BLOCK_VAR));
    yyVal = support.arg_blk_pass(splat, block);
    return yyVal;
};
states[281] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[282] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.arg_append(((ParseNode)yyVals[-3+yyTop]), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[283] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((HashParseNode)yyVals[-1+yyTop]).getPosition(), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[284] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newArrayNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[285] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.arg_blk_pass(((ParseNode)yyVals[-1+yyTop]), ((BlockPassParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[286] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((HashParseNode)yyVals[-1+yyTop]).getPosition(), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    yyVal = support.arg_blk_pass((ParseNode)yyVal, ((BlockPassParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[287] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.arg_append(((ParseNode)yyVals[-3+yyTop]), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    yyVal = support.arg_blk_pass((ParseNode)yyVal, ((BlockPassParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[288] = (support, lexer, yyVal, yyVals, yyTop) -> yyVal;
states[289] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getCmdArgumentState().getStack();
    lexer.getCmdArgumentState().begin();
    return yyVal;
};
states[290] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getCmdArgumentState().reset(((Long)yyVals[-1+yyTop]).longValue());
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[291] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BlockPassParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[292] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((BlockPassParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[294] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength pos = ((ParseNode)yyVals[0+yyTop]) == null ? lexer.getPosition() : ((ParseNode)yyVals[0+yyTop]).getPosition();
    yyVal = support.newArrayNode(pos, ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[295] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newSplatNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[296] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node = support.splat_array(((ParseNode)yyVals[-2+yyTop]));

    if (node != null) {
        yyVal = support.list_append(node, ((ParseNode)yyVals[0+yyTop]));
    } else {
        yyVal = support.arg_append(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    }
    return yyVal;
};
states[297] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node = null;

    /* FIXME: lose syntactical elements here (and others like this)*/
    if (((ParseNode)yyVals[0+yyTop]) instanceof ArrayParseNode &&
        (node = support.splat_array(((ParseNode)yyVals[-3+yyTop]))) != null) {
        yyVal = support.list_concat(node, ((ParseNode)yyVals[0+yyTop]));
    } else {
        yyVal = support.arg_concat(support.getPosition(((ParseNode)yyVals[-3+yyTop])), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    }
    return yyVal;
};
states[298] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[299] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[300] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node = support.splat_array(((ParseNode)yyVals[-2+yyTop]));

    if (node != null) {
        yyVal = support.list_append(node, ((ParseNode)yyVals[0+yyTop]));
    } else {
        yyVal = support.arg_append(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    }
    return yyVal;
};
states[301] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node = null;

    if (((ParseNode)yyVals[0+yyTop]) instanceof ArrayParseNode &&
        (node = support.splat_array(((ParseNode)yyVals[-3+yyTop]))) != null) {
        yyVal = support.list_concat(node, ((ParseNode)yyVals[0+yyTop]));
    } else {
        yyVal = support.arg_concat(((ParseNode)yyVals[-3+yyTop]).getPosition(), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    }
    return yyVal;
};
states[302] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newSplatNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[309] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[0+yyTop]); /* FIXME: Why complaining without $$ = $1;*/
    return yyVal;
};
states[310] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[0+yyTop]); /* FIXME: Why complaining without $$ = $1;*/
    return yyVal;
};
states[313] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_fcall(((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[314] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getCmdArgumentState().getStack();
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[315] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getCmdArgumentState().reset(((Long)yyVals[-2+yyTop]).longValue());
    yyVal = new BeginParseNode(((SourceIndexLength)yyVals[-3+yyTop]), support.makeNullNil(((ParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[316] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_ENDARG);
    return yyVal;
};
states[317] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null; /*FIXME: Should be implicit nil?*/
    return yyVal;
};
states[318] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getCmdArgumentState().getStack();
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[319] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_ENDARG); 
    return yyVal;
};
states[320] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getCmdArgumentState().reset(((Long)yyVals[-3+yyTop]).longValue());
    yyVal = ((ParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[321] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-1+yyTop]) != null) {
        /* compstmt position includes both parens around it*/
        ((ParseNode)yyVals[-1+yyTop]).setPosition(((SourceIndexLength)yyVals[-2+yyTop]));
        yyVal = ((ParseNode)yyVals[-1+yyTop]);
    } else {
        yyVal = new NilParseNode(((SourceIndexLength)yyVals[-2+yyTop]));
    }
    return yyVal;
};
states[322] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon2(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[323] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon3(lexer.tokline, ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[324] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(((ParseNode)yyVals[-1+yyTop]));
    if (((ParseNode)yyVals[-1+yyTop]) == null) {
        yyVal = new ZArrayParseNode(position); /* zero length array */
    } else {
        yyVal = ((ParseNode)yyVals[-1+yyTop]);
    }
    return yyVal;
};
states[325] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((HashParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[326] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ReturnParseNode(((SourceIndexLength)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
    return yyVal;
};
states[327] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_yield(((SourceIndexLength)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[328] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new YieldParseNode(((SourceIndexLength)yyVals[-2+yyTop]), null);
    return yyVal;
};
states[329] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new YieldParseNode(((SourceIndexLength)yyVals[0+yyTop]), null);
    return yyVal;
};
states[330] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_defined(((SourceIndexLength)yyVals[-4+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[331] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode)yyVals[-1+yyTop])), RopeConstants.BANG);
    return yyVal;
};
states[332] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(NilImplicitParseNode.NIL, RopeConstants.BANG);
    return yyVal;
};
states[333] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.frobnicate_fcall_args(((FCallParseNode)yyVals[-1+yyTop]), null, ((IterParseNode)yyVals[0+yyTop]));
    yyVal = ((FCallParseNode)yyVals[-1+yyTop]);                    
    return yyVal;
};
states[335] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-1+yyTop]) != null && 
          ((BlockAcceptingParseNode)yyVals[-1+yyTop]).getIterNode() instanceof BlockPassParseNode) {
          lexer.compile_error(PID.BLOCK_ARG_AND_BLOCK_GIVEN, "Both block arg and actual block given.");
    }
    yyVal = ((BlockAcceptingParseNode)yyVals[-1+yyTop]).setIterNode(((IterParseNode)yyVals[0+yyTop]));
    ((ParseNode)yyVal).setPosition(((ParseNode)yyVals[-1+yyTop]).getPosition());
    return yyVal;
};
states[336] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((LambdaParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[337] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(((SourceIndexLength)yyVals[-5+yyTop]), support.getConditionNode(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[338] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(((SourceIndexLength)yyVals[-5+yyTop]), support.getConditionNode(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[-2+yyTop]));
    return yyVal;
};
states[339] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().begin();
    return yyVal;
};
states[340] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().end();
    return yyVal;
};
states[341] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));
    yyVal = new WhileParseNode(((SourceIndexLength)yyVals[-6+yyTop]), support.getConditionNode(((ParseNode)yyVals[-4+yyTop])), body);
    return yyVal;
};
states[342] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().begin();
    return yyVal;
};
states[343] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().end();
    return yyVal;
};
states[344] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));
    yyVal = new UntilParseNode(((SourceIndexLength)yyVals[-6+yyTop]), support.getConditionNode(((ParseNode)yyVals[-4+yyTop])), body);
    return yyVal;
};
states[345] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newCaseNode(((SourceIndexLength)yyVals[-4+yyTop]), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[346] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newCaseNode(((SourceIndexLength)yyVals[-3+yyTop]), null, ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[347] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().begin();
    return yyVal;
};
states[348] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().end();
    return yyVal;
};
states[349] = (support, lexer, yyVal, yyVals, yyTop) -> {
    /* ENEBO: Lots of optz in 1.9 parser here*/
  yyVal = new ForParseNode(((SourceIndexLength)yyVals[-8+yyTop]), ((ParseNode)yyVals[-7+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[-4+yyTop]), support.getCurrentScope());
    return yyVal;
};
states[350] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) {
        support.yyerror("class definition in method body");
    }
    support.pushLocalScope();
    yyVal = support.isInClass(); /* MRI reuses $1 but we use the value for position.*/
    support.setIsInClass(true);
    return yyVal;
};
states[351] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));

    yyVal = new ClassParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-5+yyTop]), lexer.getPosition()), ((Colon3ParseNode)yyVals[-4+yyTop]), support.getCurrentScope(), body, ((ParseNode)yyVals[-3+yyTop]));
    support.popCurrentScope();
    support.setIsInClass(((Boolean)yyVals[-2+yyTop]).booleanValue());
    return yyVal;
};
states[352] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = (support.isInClass() ? 2 : 0) | (support.isInDef() ? 1 : 0);
    support.setInDef(false);
    support.setIsInClass(false);
    support.pushLocalScope();
    return yyVal;
};
states[353] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));

    yyVal = new SClassParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-6+yyTop]), lexer.getPosition()), ((ParseNode)yyVals[-4+yyTop]), support.getCurrentScope(), body);
    support.popCurrentScope();
    support.setInDef(((((Integer)yyVals[-3+yyTop]).intValue()) & 1) != 0);
    support.setIsInClass(((((Integer)yyVals[-3+yyTop]).intValue()) & 2) != 0);
    return yyVal;
};
states[354] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) { 
        support.yyerror("module definition in method body");
    }
    yyVal = support.isInClass();
    support.setIsInClass(true);
    support.pushLocalScope();
    return yyVal;
};
states[355] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));

    yyVal = new ModuleParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-4+yyTop]), lexer.getPosition()), ((Colon3ParseNode)yyVals[-3+yyTop]), support.getCurrentScope(), body);
    support.popCurrentScope();
    support.setIsInClass(((Boolean)yyVals[-2+yyTop]).booleanValue());
    return yyVal;
};
states[356] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushLocalScope();
    yyVal = lexer.getCurrentArg();
    lexer.setCurrentArg(null);
    support.checkMethodName(((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[357] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.isInDef();
    support.setInDef(true);
    return yyVal;
};
states[358] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));

    yyVal = new DefnParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-6+yyTop]), ((SourceIndexLength)yyVals[0+yyTop])), support.symbolID(((Rope)yyVals[-5+yyTop])), (ArgsParseNode) yyVals[-2+yyTop], support.getCurrentScope(), body);
    support.popCurrentScope();
    support.setInDef(((Boolean)yyVals[-3+yyTop]).booleanValue());
    lexer.setCurrentArg(((Rope)yyVals[-4+yyTop]));
    return yyVal;
};
states[359] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_FNAME); 
    yyVal = support.isInDef();
    support.setInDef(true);
    return yyVal;
};
states[360] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushLocalScope();
    lexer.setState(EXPR_ENDFN|EXPR_LABEL); /* force for args */
    yyVal = lexer.getCurrentArg();
    lexer.setCurrentArg(null);
    support.checkMethodName(((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[361] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = ((ParseNode)yyVals[-1+yyTop]);
    if (body == null) body = NilImplicitParseNode.NIL;

    yyVal = new DefsParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-8+yyTop]), ((SourceIndexLength)yyVals[0+yyTop])), ((ParseNode)yyVals[-7+yyTop]), support.symbolID(((Rope)yyVals[-4+yyTop])), (ArgsParseNode) yyVals[-2+yyTop], support.getCurrentScope(), body);
    support.popCurrentScope();
    support.setInDef(((Boolean)yyVals[-5+yyTop]).booleanValue());
    lexer.setCurrentArg(((Rope)yyVals[-3+yyTop]));
    return yyVal;
};
states[362] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BreakParseNode(((SourceIndexLength)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
    return yyVal;
};
states[363] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new NextParseNode(((SourceIndexLength)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
    return yyVal;
};
states[364] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new RedoParseNode(((SourceIndexLength)yyVals[0+yyTop]));
    return yyVal;
};
states[365] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new RetryParseNode(((SourceIndexLength)yyVals[0+yyTop]));
    return yyVal;
};
states[366] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    if (yyVal == null) yyVal = NilImplicitParseNode.NIL;
    return yyVal;
};
states[367] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((SourceIndexLength)yyVals[0+yyTop]);
    return yyVal;
};
states[368] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((SourceIndexLength)yyVals[0+yyTop]);
    return yyVal;
};
states[369] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInClass() && !support.isInDef() && !support.getCurrentScope().isBlockScope()) {
        lexer.compile_error(PID.TOP_LEVEL_RETURN, "Invalid return in class/module body");
    }
    yyVal = ((SourceIndexLength)yyVals[0+yyTop]);
    return yyVal;
};
states[376] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(((SourceIndexLength)yyVals[-4+yyTop]), support.getConditionNode(((ParseNode)yyVals[-3+yyTop])), ((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[378] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[380] = (support, lexer, yyVal, yyVals, yyTop) -> yyVal;
states[381] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.assignableInCurr(((Rope)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
    return yyVal;
};
states[382] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[383] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[384] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[385] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[0+yyTop]).getPosition(), ((ListParseNode)yyVals[0+yyTop]), null, null);
    return yyVal;
};
states[386] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), support.assignableInCurr(((Rope)yyVals[0+yyTop]), null), null);
    return yyVal;
};
states[387] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), support.assignableInCurr(((Rope)yyVals[-2+yyTop]), null), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[388] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-2+yyTop]).getPosition(), ((ListParseNode)yyVals[-2+yyTop]), new StarParseNode(lexer.getPosition()), null);
    return yyVal;
};
states[389] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-4+yyTop]).getPosition(), ((ListParseNode)yyVals[-4+yyTop]), new StarParseNode(lexer.getPosition()), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[390] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, support.assignableInCurr(((Rope)yyVals[0+yyTop]), null), null);
    return yyVal;
};
states[391] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, support.assignableInCurr(((Rope)yyVals[-2+yyTop]), null), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[392] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), null);
    return yyVal;
};
states[393] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(support.getPosition(((ListParseNode)yyVals[0+yyTop])), null, null, ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[394] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), ((Rope)yyVals[-1+yyTop]), ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[395] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), (Rope) null, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[396] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, ((Rope)yyVals[-1+yyTop]), ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[397] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, RubyLexer.Keyword.NIL.bytes, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[398] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((BlockArgParseNode)yyVals[0+yyTop]).getPosition(), null, (Rope) null, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[399] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsTailHolder)yyVals[0+yyTop]);
    return yyVal;
};
states[400] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, (Rope) null, null);
    return yyVal;
};
states[401] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), ((ListParseNode)yyVals[-3+yyTop]), ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[402] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-7+yyTop]).getPosition(), ((ListParseNode)yyVals[-7+yyTop]), ((ListParseNode)yyVals[-5+yyTop]), ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[403] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[404] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), ((ListParseNode)yyVals[-3+yyTop]), null, ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[405] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), null, ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[406] = (support, lexer, yyVal, yyVals, yyTop) -> {
    RestArgParseNode rest = new UnnamedRestArgParseNode(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ParserSupport.ANONYMOUS_REST_VAR, support.getCurrentScope().addVariable("*"), false);
    yyVal = support.new_args(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), null, rest, null, (ArgsTailHolder) null);
    return yyVal;
};
states[407] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), null, ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[408] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), null, null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[409] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(support.getPosition(((ListParseNode)yyVals[-3+yyTop])), null, ((ListParseNode)yyVals[-3+yyTop]), ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[410] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(support.getPosition(((ListParseNode)yyVals[-5+yyTop])), null, ((ListParseNode)yyVals[-5+yyTop]), ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[411] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(support.getPosition(((ListParseNode)yyVals[-1+yyTop])), null, ((ListParseNode)yyVals[-1+yyTop]), null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[412] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-3+yyTop]), null, ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[413] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((RestArgParseNode)yyVals[-1+yyTop]).getPosition(), null, null, ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[414] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((RestArgParseNode)yyVals[-3+yyTop]).getPosition(), null, null, ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[415] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ArgsTailHolder)yyVals[0+yyTop]).getPosition(), null, null, null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[416] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, null);
    return yyVal;
};
states[417] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.enterBlockParameters();
    return yyVal;
};
states[418] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.commandStart = true;
    yyVal = ((ArgsParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[419] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
    return yyVal;
};
states[420] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
    return yyVal;
};
states[421] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = ((ArgsParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[422] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[423] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[424] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[425] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[426] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.new_bv(((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[427] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[428] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushBlockScope();
    yyVal = lexer.getLeftParenBegin();
    lexer.setLeftParenBegin(lexer.incrementParenNest());
    return yyVal;
};
states[429] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = Long.valueOf(lexer.getCmdArgumentState().getStack());
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[430] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getCmdArgumentState().reset(((Long)yyVals[-1+yyTop]).longValue());
    lexer.getCmdArgumentState().restart();
    yyVal = new LambdaParseNode(((ArgsParseNode)yyVals[-2+yyTop]).getPosition(), ((ArgsParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]), support.getCurrentScope());
    lexer.setLeftParenBegin(((Integer)yyVals[-3+yyTop]));
    support.popCurrentScope();
    return yyVal;
};
states[431] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.enterBlockParameters();
    return yyVal;
};
states[432] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[433] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.enterBlockParameters();
    return yyVal;
};
states[434] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[435] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, null);
    return yyVal;
};
states[436] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[437] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[438] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((IterParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[439] = (support, lexer, yyVal, yyVals, yyTop) -> {
    /* Workaround for JRUBY-2326 (MRI does not enter this production for some reason)*/
    if (((ParseNode)yyVals[-1+yyTop]) instanceof YieldParseNode) {
        lexer.compile_error(PID.BLOCK_GIVEN_TO_YIELD, "block given to yield");
    }
    if (((ParseNode)yyVals[-1+yyTop]) instanceof BlockAcceptingParseNode && ((BlockAcceptingParseNode)yyVals[-1+yyTop]).getIterNode() instanceof BlockPassParseNode) {
        lexer.compile_error(PID.BLOCK_ARG_AND_BLOCK_GIVEN, "Both block arg and actual block given.");
    }
    if (((ParseNode)yyVals[-1+yyTop]) instanceof NonLocalControlFlowParseNode) {
        ((BlockAcceptingParseNode) ((NonLocalControlFlowParseNode)yyVals[-1+yyTop]).getValueNode()).setIterNode(((IterParseNode)yyVals[0+yyTop]));
    } else {
        ((BlockAcceptingParseNode)yyVals[-1+yyTop]).setIterNode(((IterParseNode)yyVals[0+yyTop]));
    }
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    ((ParseNode)yyVal).setPosition(((ParseNode)yyVals[-1+yyTop]).getPosition());
    return yyVal;
};
states[440] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[441] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop]), ((Rope)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((IterParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[442] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-4+yyTop]), ((Rope)yyVals[-3+yyTop]), ((Rope)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((IterParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[443] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.frobnicate_fcall_args(((FCallParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    yyVal = ((FCallParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[444] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((Rope)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[445] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[446] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[0+yyTop]), null, null);
    return yyVal;
};
states[447] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[-1+yyTop]), RopeConstants.CALL, ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[448] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-2+yyTop]), RopeConstants.CALL, ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[449] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_super(((SourceIndexLength)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[450] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ZSuperParseNode(((SourceIndexLength)yyVals[0+yyTop]));
    return yyVal;
};
states[451] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-3+yyTop]) instanceof SelfParseNode) {
        yyVal = support.new_fcall(RopeConstants.LBRACKET_RBRACKET);
        support.frobnicate_fcall_args(((FCallParseNode)yyVal), ((ParseNode)yyVals[-1+yyTop]), null);
    } else {
        yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), RopeConstants.LBRACKET_RBRACKET, ((ParseNode)yyVals[-1+yyTop]), null);
    }
    return yyVal;
};
states[452] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((IterParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[453] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((IterParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[454] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getPosition();
    return yyVal;
};
states[455] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushBlockScope();
    yyVal = Long.valueOf(lexer.getCmdArgumentState().getStack()) >> 1;
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[456] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IterParseNode(((SourceIndexLength)yyVals[-3+yyTop]), ((ArgsParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), support.getCurrentScope());
     support.popCurrentScope();
    lexer.getCmdArgumentState().reset(((Long)yyVals[-2+yyTop]).longValue());
    return yyVal;
};
states[457] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getPosition();
    return yyVal;
};
states[458] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushBlockScope();
    yyVal = Long.valueOf(lexer.getCmdArgumentState().getStack());
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[459] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IterParseNode(((SourceIndexLength)yyVals[-3+yyTop]), ((ArgsParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), support.getCurrentScope());
     support.popCurrentScope();
    lexer.getCmdArgumentState().reset(((Long)yyVals[-2+yyTop]).longValue());
    return yyVal;
};
states[460] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newWhenNode(((SourceIndexLength)yyVals[-4+yyTop]), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[463] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node;
    if (((ParseNode)yyVals[-3+yyTop]) != null) {
        node = support.appendToBlock(support.node_assign(((ParseNode)yyVals[-3+yyTop]), new GlobalVarParseNode(((SourceIndexLength)yyVals[-5+yyTop]), support.symbolID(RopeConstants.DOLLAR_BANG))), ((ParseNode)yyVals[-1+yyTop]));
        if (((ParseNode)yyVals[-1+yyTop]) != null) {
            node.setPosition(((SourceIndexLength)yyVals[-5+yyTop]));
        }
    } else {
        node = ((ParseNode)yyVals[-1+yyTop]);
    }
    ParseNode body = support.makeNullNil(node);
    yyVal = new RescueBodyParseNode(((SourceIndexLength)yyVals[-5+yyTop]), ((ParseNode)yyVals[-4+yyTop]), body, ((RescueBodyParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[464] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null; 
    return yyVal;
};
states[465] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[466] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.splat_array(((ParseNode)yyVals[0+yyTop]));
    if (yyVal == null) yyVal = ((ParseNode)yyVals[0+yyTop]); /* ArgsCat or ArgsPush*/
    return yyVal;
};
states[468] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[470] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[472] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((NumericParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[473] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.asSymbol(lexer.getPosition(), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[475] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]) instanceof EvStrParseNode ? new DStrParseNode(((ParseNode)yyVals[0+yyTop]).getPosition(), lexer.getEncoding()).add(((ParseNode)yyVals[0+yyTop])) : ((ParseNode)yyVals[0+yyTop]);
    /*
    NODE *node = $1;
    if (!node) {
        node = NEW_STR(STR_NEW0());
    } else {
        node = evstr2dstr(node);
    }
    $$ = node;
    */
    return yyVal;
};
states[476] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((StrParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[477] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[478] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[479] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.heredoc_dedent(((ParseNode)yyVals[-1+yyTop]));
    lexer.setHeredocIndent(0);
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[480] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(((ParseNode)yyVals[-1+yyTop]));

    lexer.heredoc_dedent(((ParseNode)yyVals[-1+yyTop]));
    lexer.setHeredocIndent(0);

    if (((ParseNode)yyVals[-1+yyTop]) == null) {
        yyVal = new XStrParseNode(position, null, CodeRange.CR_7BIT);
    } else if (((ParseNode)yyVals[-1+yyTop]) instanceof StrParseNode) {
        yyVal = new XStrParseNode(position, (Rope) ((StrParseNode)yyVals[-1+yyTop]).getValue(), ((StrParseNode)yyVals[-1+yyTop]).getCodeRange());
    } else if (((ParseNode)yyVals[-1+yyTop]) instanceof DStrParseNode) {
        yyVal = new DXStrParseNode(position, ((DStrParseNode)yyVals[-1+yyTop]));

        ((ParseNode)yyVal).setPosition(position);
    } else {
        yyVal = new DXStrParseNode(position).add(((ParseNode)yyVals[-1+yyTop]));
    }
    return yyVal;
};
states[481] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newRegexpNode(support.getPosition(((ParseNode)yyVals[-1+yyTop])), ((ParseNode)yyVals[-1+yyTop]), ((RegexpParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[482] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[483] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition());
    return yyVal;
};
states[484] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[-1+yyTop]) instanceof EvStrParseNode ? new DStrParseNode(((ListParseNode)yyVals[-2+yyTop]).getPosition(), lexer.getEncoding()).add(((ParseNode)yyVals[-1+yyTop])) : ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[485] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[486] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[487] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[488] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition());
    return yyVal;
};
states[489] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[-1+yyTop]) instanceof EvStrParseNode ? new DSymbolParseNode(((ListParseNode)yyVals[-2+yyTop]).getPosition()).add(((ParseNode)yyVals[-1+yyTop])) : support.asSymbol(((ListParseNode)yyVals[-2+yyTop]).getPosition(), ((ParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[490] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[491] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[492] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition());
    return yyVal;
};
states[493] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[494] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition());
    return yyVal;
};
states[495] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(support.asSymbol(((ListParseNode)yyVals[-2+yyTop]).getPosition(), ((ParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[496] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.createStr(RopeOperations.emptyRope(lexer.getEncoding()), 0);
    return yyVal;
};
states[497] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[498] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[499] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[500] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[501] = (support, lexer, yyVal, yyVals, yyTop) -> {
    /* FIXME: mri is different here.*/
                    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[502] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[503] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getStrTerm();
    lexer.setStrTerm(null);
    lexer.setState(EXPR_BEG);
    return yyVal;
};
states[504] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setStrTerm(((StrTerm)yyVals[-1+yyTop]));
    yyVal = new EvStrParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[505] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getStrTerm();
    lexer.setStrTerm(null);
    lexer.getConditionState().stop();
    return yyVal;
};
states[506] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getCmdArgumentState().getStack();
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[507] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getState();
    lexer.setState(EXPR_BEG);
    return yyVal;
};
states[508] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getBraceNest();
    lexer.setBraceNest(0);
    return yyVal;
};
states[509] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getHeredocIndent();
    lexer.setHeredocIndent(0);
    return yyVal;
};
states[510] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().restart();
    lexer.setStrTerm(((StrTerm)yyVals[-6+yyTop]));
    lexer.getCmdArgumentState().reset(((Long)yyVals[-5+yyTop]).longValue());
    lexer.setState(((Integer)yyVals[-4+yyTop]));
    lexer.setBraceNest(((Integer)yyVals[-3+yyTop]));
    lexer.setHeredocIndent(((Integer)yyVals[-2+yyTop]));
    lexer.setHeredocLineIndent(-1);

    yyVal = support.newEvStrNode(support.getPosition(((ParseNode)yyVals[-1+yyTop])), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[511] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalVarParseNode(lexer.getPosition(), support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[512] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstVarParseNode(lexer.getPosition(), support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[513] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarParseNode(lexer.getPosition(), support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[515] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_END|EXPR_ENDARG);
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[517] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[518] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[519] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[520] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_END|EXPR_ENDARG);

    /* DStrNode: :"some text #{some expression}"*/
    /* StrNode: :"some text"*/
    /* EvStrNode :"#{some expression}"*/
    /* Ruby 1.9 allows empty strings as symbols*/
    if (((ParseNode)yyVals[-1+yyTop]) == null) {
        yyVal = support.asSymbol(lexer.getPosition(), RopeConstants.EMPTY_US_ASCII_ROPE);
    } else if (((ParseNode)yyVals[-1+yyTop]) instanceof DStrParseNode) {
        yyVal = new DSymbolParseNode(((ParseNode)yyVals[-1+yyTop]).getPosition(), ((DStrParseNode)yyVals[-1+yyTop]));
    } else if (((ParseNode)yyVals[-1+yyTop]) instanceof StrParseNode) {
        yyVal = support.asSymbol(((ParseNode)yyVals[-1+yyTop]).getPosition(), ((ParseNode)yyVals[-1+yyTop]));
    } else {
        yyVal = new DSymbolParseNode(((ParseNode)yyVals[-1+yyTop]).getPosition());
        ((DSymbolParseNode)yyVal).add(((ParseNode)yyVals[-1+yyTop]));
    }
    return yyVal;
};
states[521] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((NumericParseNode)yyVals[0+yyTop]);  
    return yyVal;
};
states[522] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.negateNumeric(((NumericParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[523] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[524] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((FloatParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[525] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((RationalParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[526] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[527] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.declareIdentifier(((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[528] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstVarParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[529] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalVarParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[530] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ConstParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[531] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])));
    return yyVal;
};
states[532] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new NilParseNode(lexer.tokline);
    return yyVal;
};
states[533] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new SelfParseNode(lexer.tokline);
    return yyVal;
};
states[534] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new TrueParseNode((SourceIndexLength) yyVal);
    return yyVal;
};
states[535] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new FalseParseNode((SourceIndexLength) yyVal);
    return yyVal;
};
states[536] = (support, lexer, yyVal, yyVals, yyTop) -> {
    Encoding encoding = support.getConfiguration().getContext() == null ? UTF8Encoding.INSTANCE : support.getConfiguration().getContext().getEncodingManager().getLocaleEncoding();
    yyVal = new FileParseNode(lexer.tokline, StringOperations.encodeRope(lexer.getFile(), encoding, CR_UNKNOWN));
    return yyVal;
};
states[537] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new FixnumParseNode(lexer.tokline, lexer.tokline.toSourceSection(lexer.getSource()).getStartLine() + lexer.getLineOffset());
    return yyVal;
};
states[538] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new EncodingParseNode(lexer.tokline, lexer.getEncoding());
    return yyVal;
};
states[539] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.assignableLabelOrIdentifier(((Rope)yyVals[0+yyTop]), null);
    return yyVal;
};
states[540] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstAsgnParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[541] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalAsgnParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[542] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) support.compile_error("dynamic constant assignment");

    yyVal = new ConstDeclParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), null, NilImplicitParseNode.NIL);
    return yyVal;
};
states[543] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarAsgnParseNode(lexer.tokline, support.symbolID(((Rope)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[544] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to nil");
    yyVal = null;
    return yyVal;
};
states[545] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't change the value of self");
    yyVal = null;
    return yyVal;
};
states[546] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to true");
    yyVal = null;
    return yyVal;
};
states[547] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to false");
    yyVal = null;
    return yyVal;
};
states[548] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __FILE__");
    yyVal = null;
    return yyVal;
};
states[549] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __LINE__");
    yyVal = null;
    return yyVal;
};
states[550] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __ENCODING__");
    yyVal = null;
    return yyVal;
};
states[551] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[552] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[553] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_BEG);
    lexer.commandStart = true;
    return yyVal;
};
states[554] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[555] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[556] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsParseNode)yyVals[-1+yyTop]);
    lexer.setState(EXPR_BEG);
    lexer.commandStart = true;
    return yyVal;
};
states[557] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.inKwarg;
    lexer.inKwarg = true;
    lexer.setState(lexer.getState() | EXPR_LABEL);
    return yyVal;
};
states[558] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.inKwarg = ((Boolean)yyVals[-2+yyTop]);
     yyVal = ((ArgsParseNode)yyVals[-1+yyTop]);
     lexer.setState(EXPR_BEG);
     lexer.commandStart = true;
    return yyVal;
};
states[559] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), ((Rope)yyVals[-1+yyTop]), ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[560] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), (Rope) null, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[561] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, ((Rope)yyVals[-1+yyTop]), ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[562] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, RubyLexer.Keyword.NIL.bytes, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[563] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((BlockArgParseNode)yyVals[0+yyTop]).getPosition(), null, (Rope) null, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[564] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsTailHolder)yyVals[0+yyTop]);
    return yyVal;
};
states[565] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, (Rope) null, null);
    return yyVal;
};
states[566] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[567] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
    return yyVal;
};
states[568] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), ((ListParseNode)yyVals[-3+yyTop]), ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[569] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-7+yyTop]).getPosition(), ((ListParseNode)yyVals[-7+yyTop]), ((ListParseNode)yyVals[-5+yyTop]), ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[570] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[571] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), ((ListParseNode)yyVals[-3+yyTop]), null, ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[572] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), null, ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[573] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), null, ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[574] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), null, null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[575] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-3+yyTop]), ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[576] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-5+yyTop]), ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[577] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-1+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-1+yyTop]), null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[578] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-3+yyTop]), null, ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[579] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((RestArgParseNode)yyVals[-1+yyTop]).getPosition(), null, null, ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[580] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((RestArgParseNode)yyVals[-3+yyTop]).getPosition(), null, null, ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[581] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ArgsTailHolder)yyVals[0+yyTop]).getPosition(), null, null, null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[582] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(null);
    RestArgParseNode splat = new RestArgParseNode(position, ParserSupport.FORWARD_ARGS_REST_VAR, 0);
    BlockArgParseNode block = new BlockArgParseNode(position, 1, ParserSupport.FORWARD_ARGS_BLOCK_VAR);
    ArgsTailHolder argsTail = support.new_args_tail(position, null, null, block);
    yyVal = support.new_args(position, null, null, splat, null, argsTail);
    return yyVal;
};
states[584] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("formal argument cannot be a constant");
    return yyVal;
};
states[585] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("formal argument cannot be an instance variable");
    return yyVal;
};
states[586] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("formal argument cannot be a global variable");
    return yyVal;
};
states[587] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("formal argument cannot be a class variable");
    return yyVal;
};
states[588] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]); /* Not really reached*/
    return yyVal;
};
states[589] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.formal_argument(((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[590] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(((Rope)yyVals[0+yyTop]));
    yyVal = support.arg_var(((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[591] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = ((ArgumentParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[592] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
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
    return yyVal;
};
states[593] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[594] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[0+yyTop]));
    yyVal = ((ListParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[595] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.arg_var(support.formal_argument(((Rope)yyVals[0+yyTop])));
    lexer.setCurrentArg(((Rope)yyVals[0+yyTop]));
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[596] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = support.keyword_arg(((ParseNode)yyVals[0+yyTop]).getPosition(), support.assignableKeyword(((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[597] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = support.keyword_arg(lexer.getPosition(), support.assignableKeyword(((Rope)yyVals[0+yyTop]), RequiredKeywordArgumentValueParseNode.INSTANCE));
    return yyVal;
};
states[598] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.keyword_arg(support.getPosition(((ParseNode)yyVals[0+yyTop])), support.assignableKeyword(((Rope)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[599] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.keyword_arg(lexer.getPosition(), support.assignableKeyword(((Rope)yyVals[0+yyTop]), RequiredKeywordArgumentValueParseNode.INSTANCE));
    return yyVal;
};
states[600] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[601] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[602] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[603] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[604] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[605] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[607] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.shadowing_lvar(((Rope)yyVals[0+yyTop]));
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[608] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ParserSupport.INTERNAL_ID;
    return yyVal;
};
states[609] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = new OptArgParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), support.assignableLabelOrIdentifier(((ArgumentParseNode)yyVals[-2+yyTop]).getName(), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[610] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = new OptArgParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), support.assignableLabelOrIdentifier(((ArgumentParseNode)yyVals[-2+yyTop]).getName(), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[611] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BlockParseNode(((ParseNode)yyVals[0+yyTop]).getPosition()).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[612] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.appendToBlock(((ListParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[613] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BlockParseNode(((ParseNode)yyVals[0+yyTop]).getPosition()).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[614] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.appendToBlock(((ListParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[615] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[616] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[617] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (!support.is_local_id(((Rope)yyVals[0+yyTop]))) {
        support.yyerror("rest argument must be local variable");
    }
                    
    yyVal = new RestArgParseNode(support.arg_var(support.shadowing_lvar(((Rope)yyVals[0+yyTop]))));
    return yyVal;
};
states[618] = (support, lexer, yyVal, yyVals, yyTop) -> {
  /* FIXME: bytelist_love: somewhat silly to remake the empty bytelist over and over but this type should change (using null vs "" is a strange distinction).*/
  yyVal = new UnnamedRestArgParseNode(lexer.getPosition(), ParserSupport.UNNAMED_REST_VAR, support.getCurrentScope().addVariable("*"), true);
    return yyVal;
};
states[619] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[620] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[621] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (!support.is_local_id(((Rope)yyVals[0+yyTop]))) {
        support.yyerror("block argument must be local variable");
    }
                    
    yyVal = new BlockArgParseNode(support.arg_var(support.shadowing_lvar(((Rope)yyVals[0+yyTop]))));
    return yyVal;
};
states[622] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((BlockArgParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[623] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[624] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[625] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_BEG);
    return yyVal;
};
states[626] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-1+yyTop]) == null) {
        support.yyerror("can't define single method for ().");
    } else if (((ParseNode)yyVals[-1+yyTop]) instanceof ILiteralNode) {
        support.yyerror("can't define single method for literals.");
    }
    value_expr(lexer, ((ParseNode)yyVals[-1+yyTop]));
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[627] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new HashParseNode(lexer.getPosition());
    return yyVal;
};
states[628] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[629] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new HashParseNode(lexer.getPosition(), ((ParseNodeTuple)yyVals[0+yyTop]));
    return yyVal;
};
states[630] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((HashParseNode)yyVals[-2+yyTop]).add(((ParseNodeTuple)yyVals[0+yyTop]));
    return yyVal;
};
states[631] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.createKeyValue(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[632] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode label = support.asSymbol(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((Rope)yyVals[-1+yyTop]));
    yyVal = support.createKeyValue(label, ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[633] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-2+yyTop]) instanceof StrParseNode) {
        DStrParseNode dnode = new DStrParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), lexer.getEncoding());
        dnode.add(((ParseNode)yyVals[-2+yyTop]));
        yyVal = support.createKeyValue(new DSymbolParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), dnode), ((ParseNode)yyVals[0+yyTop]));
    } else if (((ParseNode)yyVals[-2+yyTop]) instanceof DStrParseNode) {
        yyVal = support.createKeyValue(new DSymbolParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((DStrParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    } else {
        support.compile_error("Uknown type for assoc in strings: " + ((ParseNode)yyVals[-2+yyTop]));
    }

    return yyVal;
};
states[634] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.createKeyValue(null, ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[635] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[636] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[637] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[638] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[639] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[640] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[641] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[642] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[643] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[644] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[645] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[646] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[647] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[648] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[650] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[655] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[656] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((Rope)yyVals[0+yyTop]);
    return yyVal;
};
states[664] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[665] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
}
// line 2796 "RubyParser.y"

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
// line 10832 "-"
