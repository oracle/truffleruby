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


import java.util.Set
import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;

import org.truffleruby.Layouts;
import org.truffleruby.annotations.SuppressFBWarnings;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.string.TStringConstants;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.RubyDeferredWarnings;
import org.truffleruby.parser.ast.ArgsParseNode;
import org.truffleruby.parser.ast.ArgumentParseNode;
import org.truffleruby.parser.ast.ArrayParseNode;
import org.truffleruby.parser.ast.ArrayPatternParseNode;
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
import org.truffleruby.parser.ast.DVarParseNode;
import org.truffleruby.parser.ast.EncodingParseNode;
import org.truffleruby.parser.ast.EnsureParseNode;
import org.truffleruby.parser.ast.EvStrParseNode;
import org.truffleruby.parser.ast.FCallParseNode;
import org.truffleruby.parser.ast.FalseParseNode;
import org.truffleruby.parser.ast.FileParseNode;
import org.truffleruby.parser.ast.FindPatternParseNode;
import org.truffleruby.parser.ast.FixnumParseNode;
import org.truffleruby.parser.ast.FloatParseNode;
import org.truffleruby.parser.ast.ForParseNode;
import org.truffleruby.parser.ast.GlobalAsgnParseNode;
import org.truffleruby.parser.ast.GlobalVarParseNode;
import org.truffleruby.parser.ast.HashParseNode;
import org.truffleruby.parser.ast.HashPatternParseNode;
import org.truffleruby.parser.ast.IfParseNode;
import org.truffleruby.parser.ast.InParseNode;
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
// line 133 "-"
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
  public static final int keyword_do = 279;
  public static final int keyword_do_cond = 280;
  public static final int keyword_do_block = 281;
  public static final int keyword_return = 282;
  public static final int keyword_yield = 283;
  public static final int keyword_super = 284;
  public static final int keyword_self = 285;
  public static final int keyword_nil = 286;
  public static final int keyword_true = 287;
  public static final int keyword_false = 288;
  public static final int keyword_and = 289;
  public static final int keyword_or = 290;
  public static final int keyword_not = 291;
  public static final int modifier_if = 292;
  public static final int modifier_unless = 293;
  public static final int modifier_while = 294;
  public static final int modifier_until = 295;
  public static final int modifier_rescue = 296;
  public static final int keyword_alias = 297;
  public static final int keyword_defined = 298;
  public static final int keyword_BEGIN = 299;
  public static final int keyword_END = 300;
  public static final int keyword__LINE__ = 301;
  public static final int keyword__FILE__ = 302;
  public static final int keyword__ENCODING__ = 303;
  public static final int keyword_do_lambda = 304;
  public static final int tIDENTIFIER = 305;
  public static final int tFID = 306;
  public static final int tGVAR = 307;
  public static final int tIVAR = 308;
  public static final int tCONSTANT = 309;
  public static final int tCVAR = 310;
  public static final int tLABEL = 311;
  public static final int tCHAR = 312;
  public static final int tUPLUS = 313;
  public static final int tUMINUS = 314;
  public static final int tUMINUS_NUM = 315;
  public static final int tPOW = 316;
  public static final int tCMP = 317;
  public static final int tEQ = 318;
  public static final int tEQQ = 319;
  public static final int tNEQ = 320;
  public static final int tGEQ = 321;
  public static final int tLEQ = 322;
  public static final int tANDOP = 323;
  public static final int tOROP = 324;
  public static final int tMATCH = 325;
  public static final int tNMATCH = 326;
  public static final int tDOT = 327;
  public static final int tDOT2 = 328;
  public static final int tDOT3 = 329;
  public static final int tBDOT2 = 330;
  public static final int tBDOT3 = 331;
  public static final int tAREF = 332;
  public static final int tASET = 333;
  public static final int tLSHFT = 334;
  public static final int tRSHFT = 335;
  public static final int tANDDOT = 336;
  public static final int tCOLON2 = 337;
  public static final int tCOLON3 = 338;
  public static final int tOP_ASGN = 339;
  public static final int tASSOC = 340;
  public static final int tLPAREN = 341;
  public static final int tLPAREN2 = 342;
  public static final int tRPAREN = 343;
  public static final int tLPAREN_ARG = 344;
  public static final int tLBRACK = 345;
  public static final int tRBRACK = 346;
  public static final int tLBRACE = 347;
  public static final int tLBRACE_ARG = 348;
  public static final int tSTAR = 349;
  public static final int tSTAR2 = 350;
  public static final int tAMPER = 351;
  public static final int tAMPER2 = 352;
  public static final int tTILDE = 353;
  public static final int tPERCENT = 354;
  public static final int tDIVIDE = 355;
  public static final int tPLUS = 356;
  public static final int tMINUS = 357;
  public static final int tLT = 358;
  public static final int tGT = 359;
  public static final int tPIPE = 360;
  public static final int tBANG = 361;
  public static final int tCARET = 362;
  public static final int tLCURLY = 363;
  public static final int tRCURLY = 364;
  public static final int tBACK_REF2 = 365;
  public static final int tSYMBEG = 366;
  public static final int tSTRING_BEG = 367;
  public static final int tXSTRING_BEG = 368;
  public static final int tREGEXP_BEG = 369;
  public static final int tWORDS_BEG = 370;
  public static final int tQWORDS_BEG = 371;
  public static final int tSTRING_DBEG = 372;
  public static final int tSTRING_DVAR = 373;
  public static final int tSTRING_END = 374;
  public static final int tLAMBDA = 375;
  public static final int tLAMBEG = 376;
  public static final int tNTH_REF = 377;
  public static final int tBACK_REF = 378;
  public static final int tSTRING_CONTENT = 379;
  public static final int tINTEGER = 380;
  public static final int tIMAGINARY = 381;
  public static final int tFLOAT = 382;
  public static final int tRATIONAL = 383;
  public static final int tREGEXP_END = 384;
  public static final int tSYMBOLS_BEG = 385;
  public static final int tQSYMBOLS_BEG = 386;
  public static final int tDSTAR = 387;
  public static final int tSTRING_DEND = 388;
  public static final int tLABEL_END = 389;
  public static final int keyword_in = 390;
  public static final int tLOWEST = 391;
  public static final int yyErrorCode = 256;

  /** number of final state.
    */
  protected static final int yyFinal = 1;

  /** parser tables.
      Order is mandated by <i>jay</i>.
    */
  protected static final short[] yyLhs = {
//yyLhs 782
    -1,   186,     0,   141,   142,   142,   142,   142,   143,   143,
    37,    36,    38,    38,    38,    38,    44,   189,    44,   190,
    39,    39,    39,    39,    39,    39,    39,    39,    39,    39,
    39,    39,    39,    39,    39,    39,    39,    31,    31,    31,
    31,    31,    31,    31,    31,    62,    62,    62,    40,    40,
    40,    40,    40,    40,    45,    32,    32,    61,    61,   115,
   151,    43,    43,    43,    43,    43,    43,    43,    43,    43,
    43,    43,   118,   118,   129,   129,   119,   119,   119,   119,
   119,   119,   119,   119,   119,   119,    76,    76,   105,   105,
   106,   106,    77,    77,    77,    77,    77,    77,    77,    77,
    77,    77,    77,    77,    77,    77,    77,    77,    77,    77,
    77,    82,    82,    82,    82,    82,    82,    82,    82,    82,
    82,    82,    82,    82,    82,    82,    82,    82,    82,    82,
     8,     8,    30,    30,    30,     7,     7,     7,     7,     7,
   122,   122,   123,   123,    65,   192,    65,     6,     6,     6,
     6,     6,     6,     6,     6,     6,     6,     6,     6,     6,
     6,     6,     6,     6,     6,     6,     6,     6,     6,     6,
     6,     6,     6,     6,     6,     6,     6,   136,   136,   136,
   136,   136,   136,   136,   136,   136,   136,   136,   136,   136,
   136,   136,   136,   136,   136,   136,   136,   136,   136,   136,
   136,   136,   136,   136,   136,   136,   136,   136,   136,   136,
   136,   136,   136,   136,   136,   136,   136,   136,   136,    41,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    41,    41,    41,    41,    41,    41,    41,    41,    41,
    41,    41,   138,   138,   138,   138,    52,    52,    78,    81,
    81,    81,    81,    63,    63,    55,    55,    55,    59,    59,
   132,   132,   132,   132,   132,    53,    53,    53,    53,    53,
   194,    57,   109,   109,   108,   108,    84,    84,    84,    84,
    35,    35,    75,    75,    75,    42,    42,    42,    42,    42,
    42,    42,    42,    42,    42,    42,   195,    42,   196,    42,
   197,   198,    42,    42,    42,    42,    42,    42,    42,    42,
    42,    42,    42,    42,    42,    42,    42,    42,    42,    42,
    42,   200,   202,    42,   203,   204,    42,    42,    42,    42,
   205,   206,    42,   207,    42,   209,    42,   210,    42,   211,
   212,    42,   213,   214,    42,    42,    42,    42,    42,    46,
   153,   154,   152,   199,   199,   199,   201,   201,    50,    50,
    47,    47,   131,   131,   133,   133,    89,    89,   134,   134,
   134,   134,   134,   134,   134,   134,   134,    96,    96,    96,
    96,    96,    95,    95,    71,    71,    71,    71,    71,    71,
    71,    71,    71,    71,    71,    71,    71,    71,    71,    73,
   216,    73,    72,    72,    72,   126,   126,   125,   125,   135,
   135,   217,   218,   128,   219,    70,   220,    70,    70,   127,
   127,   114,    60,    60,    60,    60,    22,    22,    22,    22,
    22,    22,    22,    22,    22,   113,   113,   221,   222,   116,
   223,   224,   117,    79,    48,    48,   225,   226,   227,   155,
    49,    49,   156,   156,   156,   157,   157,   157,   157,   157,
   157,   158,   159,   159,   160,   160,   182,   183,   161,   161,
   161,   161,   161,   161,   161,   161,   161,   161,   161,   161,
   161,   228,   161,   161,   229,   161,   163,   163,   163,   163,
   163,   163,   163,   163,   164,   164,   165,   165,   162,   177,
   177,   166,   166,   167,   174,   174,   174,   174,   175,   175,
   176,   176,   181,   181,   178,   178,   179,   180,   180,   168,
   168,   168,   168,   168,   168,   168,   168,   168,   168,   169,
   169,   169,   169,   169,   169,   169,   169,   169,   169,   169,
   169,   169,   169,   169,   169,   170,   171,   171,   172,   173,
   173,   173,   120,   120,    80,    80,    80,    51,    51,    54,
    54,    28,    28,    28,    15,    16,    16,    16,    17,    18,
    19,    25,    86,    86,    27,    27,    92,    90,    90,    26,
    93,    85,    85,    91,    91,    20,    20,    21,    21,    24,
    24,    23,   230,    23,   231,   232,   233,   234,   235,    23,
    66,    66,    66,    66,     2,     1,     1,     1,     1,    29,
    33,    33,   184,   184,   184,    34,    34,    34,    34,    58,
    58,    58,    58,    58,    58,    58,    58,    58,    58,    58,
    58,   121,   121,   121,   121,   121,   121,   121,   121,   121,
   121,   121,   121,    67,    67,   236,    56,    56,    74,   237,
    74,    97,    97,    97,    97,    97,    94,    94,    68,    68,
    69,    69,    69,    69,    69,    69,    69,    69,    69,    69,
    69,    69,    69,    69,    69,    69,   147,   137,   137,   137,
   137,     9,     9,   150,   124,   124,    87,    87,   146,    98,
    98,    99,    99,   100,   100,   101,   101,   144,   144,   215,
   145,   145,    64,   130,   107,   107,    88,    88,    10,    10,
    13,    13,    12,    12,   112,   112,   111,   111,    14,   238,
    14,   102,   102,   103,   103,   104,   104,   104,   104,   104,
     3,     3,     3,     4,     4,     4,     4,     5,     5,     5,
    11,    11,   148,   148,   149,   149,   187,   187,   191,   191,
   139,   140,   185,   193,   193,   193,   208,   208,   188,   188,
    83,   110,
    }, yyLen = {
//yyLen 782
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
     2,     4,     2,     1,     3,     3,     5,     3,     1,     1,
     1,     1,     2,     4,     2,     1,     2,     2,     4,     1,
     0,     2,     2,     1,     2,     1,     1,     2,     3,     4,
     1,     1,     3,     4,     2,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     0,     4,     0,     3,
     0,     0,     5,     3,     3,     2,     3,     3,     1,     4,
     3,     1,     5,     4,     3,     2,     1,     2,     2,     6,
     6,     0,     0,     7,     0,     0,     7,     5,     4,     5,
     0,     0,     9,     0,     6,     0,     7,     0,     5,     0,
     0,     7,     0,     0,     9,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     2,     1,     1,     1,     5,
     1,     2,     1,     1,     1,     3,     1,     3,     1,     4,
     6,     3,     5,     2,     4,     1,     3,     4,     2,     2,
     2,     1,     2,     0,     6,     8,     4,     6,     4,     2,
     6,     2,     4,     6,     2,     4,     2,     4,     1,     1,
     0,     2,     3,     1,     4,     1,     4,     1,     3,     1,
     1,     0,     0,     4,     0,     5,     0,     2,     0,     3,
     3,     3,     2,     4,     5,     5,     2,     4,     4,     3,
     3,     3,     2,     1,     4,     3,     3,     0,     0,     4,
     0,     0,     4,     5,     1,     1,     0,     0,     0,     8,
     1,     1,     1,     3,     3,     1,     2,     3,     1,     1,
     1,     1,     3,     1,     3,     1,     1,     1,     1,     1,
     4,     4,     4,     3,     4,     4,     4,     3,     3,     3,
     2,     0,     4,     2,     0,     4,     1,     1,     2,     3,
     5,     2,     4,     1,     2,     3,     1,     3,     5,     2,
     1,     1,     3,     1,     3,     1,     2,     1,     1,     3,
     2,     1,     1,     3,     2,     1,     2,     1,     1,     1,
     3,     3,     2,     2,     1,     1,     1,     2,     2,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     2,     2,     4,     2,
     3,     1,     6,     0,     1,     1,     1,     2,     1,     2,
     1,     1,     1,     1,     1,     1,     1,     2,     3,     3,
     3,     4,     0,     3,     1,     2,     4,     0,     3,     4,
     4,     0,     3,     0,     3,     0,     2,     0,     2,     0,
     2,     1,     0,     3,     0,     0,     0,     0,     0,     8,
     1,     1,     1,     1,     2,     1,     1,     1,     1,     3,
     1,     2,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     0,     4,     0,     3,     0,
     3,     4,     2,     2,     2,     1,     2,     0,     1,     0,
     6,     8,     4,     6,     4,     6,     2,     4,     6,     2,
     4,     2,     4,     1,     3,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     3,     1,     3,     1,     2,
     1,     2,     1,     1,     3,     1,     3,     1,     1,     2,
     2,     1,     3,     3,     1,     3,     1,     3,     1,     1,
     2,     1,     1,     1,     2,     1,     2,     0,     1,     0,
     4,     1,     2,     1,     3,     3,     2,     1,     4,     2,
     1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     1,     0,     1,     0,     1,
     2,     2,     2,     0,     1,     1,     1,     1,     1,     2,
     0,     0,
    }, yyDefRed = {
//yyDefRed 1290
     1,     0,     0,     0,   370,   371,     0,     0,   316,     0,
     0,     0,   341,   344,     0,     0,     0,   367,   368,   372,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   585,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,   605,   607,   609,     0,     0,
   431,   663,   664,   635,   638,   636,   637,     0,     0,   582,
    60,   306,     0,   586,   307,   308,     0,   309,   310,   305,
   583,    31,    48,   581,   630,     0,     0,     0,     0,     0,
     0,     0,   313,     0,    56,     0,     0,    86,     0,     4,
   311,   312,     0,     0,    72,     0,     2,     0,     5,     0,
     0,     0,     0,     7,   187,   198,   188,   211,   184,   204,
   194,   193,   214,   215,   209,   192,   191,   186,   212,   216,
   217,   196,   185,   199,   203,   205,   190,   206,   213,   208,
     0,     0,     0,     0,   183,   202,   201,   218,   182,   189,
   180,   181,     0,     0,     0,     0,   137,   641,   640,     0,
   643,   172,   173,   169,   150,   151,   152,   159,   156,   158,
   153,   154,   174,   175,   160,   161,   739,   166,   165,   149,
   171,   168,   167,   163,   164,   157,   155,   147,   170,   148,
   176,   162,   197,   138,   359,     0,   738,   139,   207,   200,
   210,   195,   177,   178,   179,   135,   136,   141,   140,   143,
     0,   142,   144,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,   776,   777,     0,     0,     0,
   778,     0,     0,   365,   366,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   369,     0,     0,   382,   383,     0,     0,
   328,     0,     0,     0,     0,   605,     0,     0,   285,    70,
     0,     0,     0,   743,   289,    71,     0,    68,     0,     0,
   452,    67,     0,   769,     0,     0,    19,     0,     0,     0,
   241,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,    13,    12,     0,     0,     0,     0,     0,
   269,     0,     0,     0,   741,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   254,    52,   253,   627,   626,   628,
   624,   625,     0,     0,     0,     0,   592,   601,   338,     0,
   597,   603,   587,   460,   457,   337,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,   264,   265,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   263,   262,     0,     0,     0,     0,
   460,   442,   762,   763,     0,     0,     0,     0,   765,   764,
     0,     0,    88,     0,     0,     0,     0,     0,     0,     3,
     0,   446,     0,   335,    69,   645,   644,   646,   647,   649,
   648,   650,     0,     0,     0,     0,   133,     0,     0,   314,
   357,     0,   360,   760,   761,   362,   145,     0,     0,     0,
   374,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   779,     0,     0,     0,   631,     0,     0,
     0,     0,   350,   746,   297,   292,     0,   749,     0,     0,
   286,   295,     0,   287,     0,   330,     0,   291,     0,   281,
   280,     0,     0,     0,     0,     0,   334,    51,    21,    23,
    22,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   323,    11,     0,     0,   319,     0,   326,
     0,   774,   270,     0,   272,   327,   742,     0,    90,     0,
     0,     0,     0,     0,   614,   612,   629,   611,   608,   588,
   606,   589,   590,   610,     0,     0,   434,   432,     0,     0,
     0,     0,   461,     0,   458,    25,    26,    27,    28,    29,
    49,    50,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   449,     0,   451,     0,     0,     0,   755,     0,     0,   756,
   450,     0,   753,   754,     0,    47,     0,     0,     0,    44,
   227,     0,     0,     0,     0,    37,   219,    33,   296,     0,
     0,     0,     0,    89,    32,     0,   300,     0,    38,   220,
     6,   457,    62,     0,   130,     0,   132,   665,   353,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,   317,
     0,   375,     0,     0,     0,     0,     0,     0,   466,     0,
     0,     0,   348,   377,   342,   376,   345,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   745,     0,     0,     0,
   294,   744,   329,   770,     0,     0,   275,   277,   333,    20,
     0,     9,    30,     0,   226,     0,     0,    14,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   615,     0,   591,
   594,     0,   599,     0,     0,     0,   702,   699,   698,   697,
   700,   708,   717,   696,     0,   729,   728,   733,   732,   718,
   703,     0,     0,     0,   726,   437,     0,     0,   693,   715,
     0,   675,   706,   701,     0,     0,     0,   695,     0,     0,
   596,     0,   600,     0,   456,     0,   455,     0,     0,   441,
     0,     0,   448,     0,     0,     0,     0,     0,     0,   279,
     0,   447,   278,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,    87,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   134,     0,     0,   740,     0,     0,     0,
   363,   146,   575,     0,     0,   576,     0,     0,   380,     0,
   378,     0,     0,     0,     0,     0,     0,     0,   467,   347,
   349,     0,     0,     0,     0,     0,     0,   748,   299,   288,
     0,     0,   332,     0,   322,   271,    91,     0,   616,   620,
   621,   622,   613,   623,   593,   595,   602,     0,   678,     0,
     0,   433,     0,     0,   384,     0,   386,     0,   730,   734,
     0,   691,     0,   686,     0,   689,     0,   672,   719,   720,
     0,   673,   709,     0,   674,   598,   604,     0,   419,     0,
     0,     0,    43,   224,    42,   225,    66,     0,   771,    40,
   222,    41,   223,    64,   445,   444,    46,     0,     0,     0,
     0,     0,     0,     0,     0,     0,    34,    59,     0,     0,
     0,   454,   358,     0,     0,     0,     0,     0,     0,   578,
   381,     0,    10,   580,     0,   339,     0,   340,     0,   298,
     0,     0,     0,   351,     0,   276,    18,   617,     0,     0,
     0,     0,     0,     0,     0,     0,   705,     0,   676,   704,
     0,     0,   707,   694,     0,   727,     0,   716,   736,     0,
     0,   722,   462,   423,     0,   421,   459,     0,     0,    39,
     0,     0,     0,   666,   354,   668,   361,   670,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   577,     0,   579,     0,   558,   557,
   559,   560,   562,   561,   563,   565,   571,   532,     0,     0,
     0,   504,     0,     0,     0,   605,     0,   550,   551,   552,
   553,   554,   549,   555,   556,   564,     0,     0,     0,     0,
   481,     0,   485,   478,   479,   488,     0,   489,   544,   545,
     0,   480,     0,   528,     0,   537,   538,   527,     0,   464,
   463,   465,   343,   346,     0,   618,   435,     0,   440,   439,
   385,     0,     0,     0,   387,     0,   692,     0,   684,     0,
   682,     0,   687,   690,   671,     0,     0,     0,   418,   713,
     0,     0,   401,     0,   724,     0,     0,     0,     0,   356,
     0,     0,     0,     0,     0,     0,     0,   547,   548,   131,
   569,     0,   500,     0,     0,     0,     0,   513,     0,   503,
     0,     0,   519,     0,   566,   633,   632,   634,     0,   567,
   536,   534,   468,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   429,     0,   427,   430,     0,     0,     0,     0,     0,     0,
     0,     0,     0,   416,     0,     0,   411,     0,   398,     0,
   414,   422,   399,     0,     0,     0,     0,     0,   400,   364,
     0,     0,     0,     0,     0,   572,   379,     0,   499,   498,
     0,     0,     0,   514,   772,   605,     0,   533,     0,     0,
   473,   474,   482,   477,     0,   484,   540,   541,   570,   497,
   493,     0,     0,     0,     0,     0,     0,   529,   524,     0,
   521,   352,     0,     0,   426,     0,     0,   685,     0,   680,
   683,   688,     0,   402,   424,     0,     0,   714,     0,     0,
     0,   725,   325,     0,     0,     0,   505,     0,     0,   515,
     0,   502,   568,     0,     0,   491,   490,   492,   495,   494,
   496,     0,   619,   428,     0,     0,     0,   417,     0,   408,
     0,   406,   397,     0,   412,   415,     0,     0,     0,     0,
   470,   469,   471,     0,   522,   518,   681,     0,     0,     0,
     0,     0,     0,   410,     0,   404,   407,   413,     0,   405,
    }, yyDgoto = {
//yyDgoto 239
     1,   350,    69,    70,   669,   590,   591,   208,   436,   730,
   731,   445,   732,   733,   195,    71,    72,    73,    74,    75,
   353,   352,    76,   540,   355,    77,    78,   711,    79,    80,
   437,    81,    82,    83,    84,   625,   447,   448,   311,   312,
    86,    87,    88,    89,   313,   229,   301,   810,  1050,  1271,
   811,   918,    91,   489,   922,   592,   638,   287,    92,   771,
    93,    94,   615,   616,   734,   210,   842,   231,   847,   848,
   547,  1076,   965,   877,   798,   617,    96,    97,   280,   462,
   804,   319,   232,   314,   593,   545,   544,   736,   737,   855,
   549,   550,   100,   101,   861,  1153,  1223,   948,   739,  1079,
  1080,   740,   325,   492,   283,   102,   529,  1081,   480,   284,
   481,   867,   741,   423,   401,   632,   553,   551,   103,   104,
   648,   233,   211,   212,   742,  1141,   938,   851,  1025,   316,
  1084,   268,   493,   856,   857,  1142,   197,   743,   399,   485,
   765,   106,   107,   108,   744,   745,   746,   747,   641,   410,
   949,   109,   110,   111,   112,   660,  1027,  1028,  1181,  1030,
  1031,  1032,  1033,  1105,  1106,  1107,  1209,  1108,  1035,  1036,
  1037,  1038,  1039,  1040,  1041,  1042,  1043,  1044,  1045,  1046,
  1047,  1048,  1133,  1134,  1119,  1109,     2,   238,   239,   511,
   501,   486,   646,   522,   288,   213,   317,   318,   698,   451,
   241,   664,   823,   242,   824,   674,  1054,   790,   452,   788,
   642,   442,   644,   645,   916,   749,   879,   359,   715,   714,
   548,   554,   757,   552,   755,   818,   928,  1189,  1111,  1101,
   708,   707,   838,   937,  1055,  1139,   789,   799,   441,
    }, yySindex = {
//yySindex 1290
     0,     0, 22191, 23762,     0,     0, 21536, 21938,     0, 24932,
 24932, 19984,     0,     0, 25452, 22583, 22583,     0,     0,     0,
  -213,  -204,     0,     0,     0,     0,    44, 21804,   170,  -161,
  -139,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0, 25062, 25062,  1395, 25062, 25062,   -63, 22322,     0, 23238,
 23631, 20504, 25062, 25192, 21670,     0,     0,     0,   228,   238,
     0,     0,     0,     0,     0,     0,     0,   261,   271,     0,
     0,     0,   -40,     0,     0,     0,  -167,     0,     0,     0,
     0,     0,     0,     0,     0,  2499,   -54,  7664,     0,   101,
   552,   592,     0,   617,     0,    -6,   298,     0,   344,     0,
     0,     0, 25582,   368,     0,   121,     0,   147,     0,  -142,
 22583, 25712, 25842,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,  -133,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   453,     0,     0, 22453,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   199, 22453,   -54,    87,
   644,   192,   492,   232,    87,     0,     0,   147,   306,   551,
     0, 24932, 24932,     0,     0,  -213,  -204,     0,     0,     0,
     0,   272,   170,     0,     0,     0,     0,     0,     0,     0,
     0,  1395,   370,     0,  1120,     0,     0,     0,   294,  -142,
     0, 25062, 25062, 25062, 25062,     0, 25062,  7664,     0,     0,
   288,   624,   642,     0,     0,     0, 17997,     0, 22583, 22714,
     0,     0, 20115,     0, 24932,   473,     0, 24022, 22191, 22453,
     0,  1161,   383,   420,  5631,  5631,   399, 23892,     0, 22322,
   416,   147,  2499,     0,     0,     0,   170,   170, 23892,   396,
     0,   112,   125,   288,     0,   384,   125,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,   461,
 25972,  1224,     0,   751,     0,     0,     0,     0,     0,     0,
     0,     0,  1477,  1524,  1548,   770,     0,     0,     0,   444,
     0,     0,     0,     0,     0,     0, 24932, 24932, 24932, 24932,
 23892, 24932, 24932, 25062, 25062, 25062, 25062, 25062,     0,     0,
 25062, 25062, 25062, 25062, 25062, 25062, 25062, 25062, 25062, 25062,
 25062, 25062, 25062, 25062,     0,     0, 25062, 25062, 25062, 25062,
     0,     0,     0,     0,  4621, 22583,  7549, 25062,     0,     0,
  5187, 25192,     0, 24152, 22322, 20634,   764, 24152, 25192,     0,
 20764,     0,   464,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0, 24932,   -48,     0,   458,  1236,     0,
     0, 24932,     0,     0,     0,     0,     0,   556,   561,   399,
     0, 22453,   562, 13879, 22583, 18674, 25062, 25062, 25062, 22453,
  -193, 24282,   575,     0,   301,   301,   500,     0,     0, 19036,
 22583, 26314,     0,     0,     0,     0,  1371,     0, 25062, 22845,
     0,     0, 23369,     0,   170,     0,   503,     0, 25062,     0,
     0,   804,   811,   170,   170,   533,     0,     0,     0,     0,
     0, 21938, 24932,  7664,   496,   504, 13879, 18674, 25062, 25062,
  2499,   518,   170,     0,     0, 20894,     0,     0,  2499,     0,
 23500,     0,     0, 23631,     0,     0,     0,     0,     0,   840,
 26372, 22583, 26430, 25972,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,  1666,  -181,     0,     0,  4682,  1893,
   291,   628,     0,   530,     0,     0,     0,     0,     0,     0,
     0,     0,   383,  3106,  3106,  3106,  3106,  5688,  4755,  3106,
  3106,  5631,  5631,  2536,  2536,   383,  2668,   383,   383,  1344,
  1344,  3229,  3229,  6140,  3194,   636,   579,     0,   582,  -204,
     0,     0,     0,   880,   170,   591,     0,   609,  -204,     0,
     0,  3194,     0,     0,  -204,     0,   663,  7156,  1573,     0,
     0,    -6,   903, 25062,  7156,     0,     0,     0,     0,   928,
   170, 25972,   959,     0,     0,   712,     0,     0,     0,     0,
     0,     0,     0,   -54,     0,     0,     0,     0,     0, 26488,
 22583, 26546, 22453,   533,   669, 22072, 21938, 24412,   748,     0,
   499,     0,   680,   686,   170,   696,   709,   748,     0,   786,
   789,   810,     0,     0,     0,     0,     0,     0,     0,  -204,
   170,     0,     0,  -204, 24932, 25062,     0, 25062,   288,   642,
     0,     0,     0,     0, 22976, 23369,     0,     0,     0,     0,
   533,     0,     0,   383,     0, 22191,     0,     0,   170,   125,
 25972,     0,     0,   170,     0,     0,   840,     0,   568,     0,
     0,   200,     0,  1022,  4682,  -237,     0,     0,     0,     0,
     0,     0,     0,     0,  2014,     0,     0,     0,     0,     0,
     0,   755,   756,  1035,     0,     0,  1040,  1041,     0,     0,
  1042,     0,     0,     0,  -104,  1044, 25062,     0,  1034,  1044,
     0,   346,     0,  1074,     0,     0,     0,     0,  1059,     0,
 25192, 25192,     0,   464, 22845,   791,   775, 25192, 25192,     0,
   464,     0,     0,   101,  -167, 23892, 25062, 26604, 22583, 26662,
 25192,     0, 24542,     0,   840, 25972, 23892,   771,   147, 24932,
 22453,     0,     0,     0,   170,   874,     0,  4682, 22453,  4682,
     0,     0,     0,     0,   812,     0, 22453,   895,     0, 24932,
     0,   901, 25062, 25062,   832, 25062, 25062,   908,     0,     0,
     0, 24672, 22453, 22453, 22453,     0,   301,     0,     0,     0,
  1131,   170,     0,   813,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   170,     0, 22453,
 22453,     0,  2014,   679,     0,  1134,     0,   170,     0,     0,
  3084,     0,  4682,     0,  3598,     0,   857,     0,     0,     0,
   427,     0,     0, 25062,     0,     0,     0, 22453,     0,  -135,
 22453, 25062,     0,     0,     0,     0,     0, 25192,     0,     0,
     0,     0,     0,     0,     0,     0,     0,  7664,   579,   582,
   170,   591,   609, 25062,     0,   840,     0,     0, 22453,   147,
   918,     0,     0,   170,   919,   147,   669, 26102,    87,     0,
     0, 22453,     0,     0,    87,     0, 25062,     0, 21099,     0,
   405,   922,   923,     0, 23369,     0,     0,     0,   849,  1135,
   931,   834,   170,  3070,  1155,  2418,     0,  1156,     0,     0,
  1160,  1163,     0,     0,  1174,     0,  1156,     0,     0,   914,
  1044,     0,     0,     0,   996,     0,     0,  7664,  7664,     0,
   791,     0,   958,     0,     0,     0,     0,     0, 22453,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   921,  1621,     0,     0, 22453,     0, 22453,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,  3707,  3707,
   206,     0,  4099,   170,   927,     0,  1818,     0,     0,     0,
     0,     0,     0,     0,     0,     0,    97,    87,   593,    12,
     0,  1103,     0,     0,     0,     0,   588,     0,     0,     0,
   214,     0,  1192,     0,  1194,     0,     0,     0, 21406,     0,
     0,     0,     0,     0, 22453,     0,     0,  2475,     0,     0,
     0,  1198,  3070,  1626,     0,  3084,     0,  3084,     0,  3598,
     0,  3084,     0,     0,     0,  1201,   170,  1210,     0,     0,
  1213,  1217,     0,   911,     0,  1044, 26232,  1202,  1044,     0,
  1010,     0, 26720, 22583, 26778,   556,   499,     0,     0,     0,
     0, 21406,     0,   936,   170,   170, 21202,     0,  1238,     0,
  1162,   912,     0,  1605,     0,     0,     0,     0, 24932,     0,
     0,     0,     0, 24932, 24932,   981, 21304, 21406,  3707,  3707,
   206,   170,   170, 21099, 21099,   912, 21406,   936,  1028, 22453,
     0,   163,     0,     0,  3070,  1198,  3070,  1254,  1156,  1256,
  1156,  1156,  3084,     0,   949,  3598,     0,   857,     0,  3598,
     0,     0,     0,     0,     0,  1001,  1794, 26232,     0,     0,
     0,     0,   170,     0,     0,     0,     0,    24,     0,     0,
     9,   936,  1269,     0,     0,     0,   170,     0,  1273, 22453,
     0,     0,     0,     0,  1277,     0,     0,     0,     0,     0,
     0,   170,   170,   170,   170,   170,   170,     0,     0,  1279,
     0,     0,   929,  2475,     0,  1198,  3070,     0,  3084,     0,
     0,     0,  1284,     0,     0,  1287,  1289,     0,  1044,  1292,
  1284,     0,     0, 26836,  1794,     0,     0,  1294, 21406,     0,
  1076,     0,     0,  -160, 21406,     0,     0,     0,     0,     0,
     0, 21304,     0,     0,  1198,  1156,  3084,     0,  3084,     0,
  3598,     0,     0,  3084,     0,     0,     0,     0, 21406,  1296,
     0,     0,     0,  1296,     0,     0,     0,  1284,  1299,  1284,
  1284,  1296, 21406,     0,  3084,     0,     0,     0,  1284,     0,
    }, yyRindex = {
//yyRindex 1290
     0,     0,   242,     0,     0,     0,     0,     0,     0,     0,
     0,  1073,     0,     0,     0, 11852, 11957,     0,     0,     0,
  5470,  5004,  9505,  9856, 10207, 10558, 25322,     0, 24802,     0,
     0, 10909, 11260, 13454,  5802,  3988, 13565, 13643,  5936, 13756,
     0,     0,     0,     0,     0,     0,     0,   133, 19854,  1003,
   988,   155,     0,     0,  2008,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,  8556,     0,     0,     0, 11150,     0,     0,     0,
     0,     0,     0,     0,     0,    86,  2674,  1275, 11364, 13377,
     0, 13996,     0, 17493,     0, 14347,     0,     0,     0,     0,
     0,     0,   166,     0,     0,     0,     0,    55,     0, 23107,
 12068,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
  5108,  5574,  6089,  6597,     0,     0,     0,     0,     0,     0,
     0,     0,  7105,  7613, 11613, 12144,     0,     0,     0, 12205,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
  3210,     0,     0,  1206,  8693,  8907,  9044,  9258,  9395,  9609,
  9746,  2819,  9960, 10097,  2972, 10311,     0,   133, 17628,     0,
     0, 10799,     0,     0,     0,     0,     0,  -180,     0,  -151,
     0,     0,     0,     0,     0, 11501, 10448,   843,  1159,  1429,
  1762,     0,  1015,  1782,  1906,  1970,  1090,  2247,  2363,  1967,
  2375,     0,     0,     0,     0,  2597,     0,     0,     0,     0,
     0, 16920,     0, 14698,     0,     0,     0, 16856,     0,     0,
 17112, 17252, 17252,     0,     0,     0,  1016,     0,     0,   175,
     0,     0,  1016,     0,     0,     0,     0,     0,    92,    92,
     0,     0, 12524, 11715, 16255, 16316, 14452,     0, 19454,   133,
     0,  3103,  1325,     0,     0,    67,  1016,  1016,     0,     0,
     0,  1018,  1018,     0,     0,     0,  1004,  1314,  1316,  1556,
  1601,  1858,  2006,  2049,  1491,  2267,  2306,  1632,  2308,     0,
     0,     0,  2408,   177,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,  4184,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0, 12310, 12421,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,    98,     0,     0,     0,     0,
     0,     0,     0,     0,   133,   198,   204,     0,     0,     0,
    61,     0, 17355,     0,     0,     0,     0,     0,     0,     0,
     0,     0, 18461, 18592,     0,     0,     0, 19585,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   893,     0, 11013,
     0,   773, 17867,     0,    98,     0,     0,     0,     0,  1033,
     0,     0,     0,     0,     0,     0,     0,     0,  2670,     0,
    98,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,  1016,     0,     0,     0,   212,     0,
     0,   216,   254,  1016,  1016,  1016,     0,     0,     0,     0,
     0,     0,     0,  1989,     0,     0,     0,     0,     0,     0,
  1773,     0,  1016,     0,     0,  3152,   111,     0,   223,     0,
  1026,     0,     0,  -230,     0,     0,     0,  2498,     0,   221,
     0,    98,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0, 12663, 15501, 15612, 15715, 15804, 15910,  2414, 16060,
 16166, 16422, 16511, 14803, 14914, 12861,  1518, 13000, 13103, 14107,
 14210, 15017, 15156,  1318, 15259,     0,  6310,  4362,  7834, 23107,
     0,  4496,     0,   142,  1036,  6444,     0,  6818,  5336,     0,
     0, 15370,     0,     0,  8208,     0,  1214, 17023,     0,     0,
     0, 14561,     0,     0, 16149,     0,     0,     0,     0,     0,
  1016,     0,   237,     0,     0,  8380,     0, 17693,     0,     0,
     0,     0,     0,   257,     0, 19185,     0,     0,     0,     0,
    98,     0,  1206,  1016, 12587,     0,     0,   595,   727,     0,
  1125,     0,  3346,  4870,  1036,  3480,  3854,  1125,     0,     0,
     0,     0,     0,     0,     0,     0,     0,  2067,  1060,     0,
  1036,  2224,  3107, 10662,     0,     0,     0,     0, 17217, 17252,
     0,     0,     0,     0,   258,   263,     0,     0,     0,     0,
  1016,     0,     0, 13214,     0,    92,   120,     0,  1016,  1018,
     0,  1881,  1102,  1036,  2105,  2143,   334,     0,     0,     0,
     0,     0,     0,     0,   144,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   622,   371,   374,     0,     0,   374,   374,     0,     0,
   697,     0,     0,     0,   917,   697,   560,     0,  1052,   697,
     0,     0,     0,     0,     0, 19316,     0, 19723,     0,     0,
     0,     0,     0, 17390,   178,  8803,     0,     0,     0,     0,
 17428,     0,     0, 17570,  2575,     0,     0,     0,    98,     0,
     0,  1415,     0,     0,   337,     0,     0,     0,     0,     0,
  1206, 18823, 18954,     0,  1036,     0,     0,   269,  1206,   259,
     0,     0,     0,   851,   702,     0,   782,  1125,     0,     0,
     0,     0,     0,     0,  8342,     0,     0,     0,     0,     0,
     0,     0,   794,   673,   673,  4194,     0,     0,     0,     0,
   254,  1016,     0,     0,     0,     0,     0,  1937,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   -31,     0,  1206,
    92,     0,     0,   282,     0,   287,     0,  1016,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,  1206,     0,     0,
    92,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0, 16614,  6952,  7968,
  1036,  7326,  7460,     0, 17706,   359,     0,     0,  1206,     0,
     0,     0,     0,  1016,     0,     0, 12587,     0,     0,     0,
     0,   673,     0,     0,     0,     0,     0,     0,  2321,     0,
  1125,     0,     0,     0,   278,     0,     0,     0,     0,   -35,
     0,     0,  1016,     0,   314,     0,     0,   374,     0,     0,
   374,   374,     0,     0,   374,     0,   374,     0,     0,   917,
   697,     0,     0,     0,   -43,     0,     0, 16678, 16767,     0,
  9154, 17758,     0,     0,     0,     0,     0,     0,  1206,  1457,
  1646,  2123,  2154,  2240,  2885,  5111,  1764,  5577,  8598,  2656,
  8912,     0,     0,  9197,     0,  1206,     0,   773,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,  3615,  3615,
     0,     0,  3215,   850,   777,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,  1663,     0,   765,   823,
     0,  8770,     0,     0,     0,     0,  2616,     0,     0,     0,
 11890,     0,  2529,     0,   790,     0,     0,     0, 18329,     0,
     0,     0,     0,     0,   673,     0,     0,     0,     0,     0,
     0,   316,     0,   319,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,    83,   -43,    83,     0,     0,
   104,    83,     0,     0,     0,   104,   109,   161,   104,     0,
     0,  9263,     0,    98,     0,   893,  1125,     0,     0,     0,
     0,  4201,     0,  6599,  1036,  1036,  3586,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,  3720,  4201, 18207, 18277,
     0, 18110, 20996,  2321,  2321,  4602,  4201,  1943,     0,    52,
     0,   235,     0,     0,     0,   330,     0,   355,   374,   374,
   374,   374,     0,     0,     0,   140,     0,     0,     0,     0,
     0,     0,     0,  2490,  2712,     0,   262,     0,     0,     0,
  8446,  1187,  1036,  8449, 10418,     0,     0,  1016,     0,     0,
  2707,  7107,  4736,     0,     0,     0,  1265,     0,     0,   103,
     0,     0,     0,     0,   897,     0,     0,     0,     0,     0,
     0,  1016,  1016,  1016,  1036,  1036,  1036,     0,     0,  5576,
     0,     0,     0,     0,     0,   361,     0,     0,     0,     0,
     0,     0,    83,     0,     0,    83,    83,     0,   104,    83,
    83,     0,     0,     0,   279,  1494,     0,  5710,  4201,     0,
     0,     0,     0,  1125,  4201,     0,     0,     0,     0,     0,
     0,  4216,     0,     0,   387,   374,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,  8698,   678,  4201,  6042,
     0,     0,     0,   909,     0,     0,     0,    83,    83,    83,
    83,  6684,  4201,     0,     0,     0,     0,     0,    83,     0,
    }, yyGindex = {
//yyGindex 239
     0,     0,    -5,     0,  -366,     0,     6,     5,  -421,  -609,
     0,     0,     0,  -611,     0, 12935,     0,  1320, 15134, 15712,
  -270,  1338,     0,  -194,     0, 19402, 19443,   852, 19458,    18,
  1286,  -234,     4,     0,    38,     0,   275,  1923,     0,   188,
     8,  2139,    -8,    60,   885,   116,    21,  -599,     0,     0,
   308,     0,     0,   742,     0,   162,     0,    31,  1400,   805,
     0,     0,  -294,   618,  -784,     0,     0,   172,  -403,   860,
     0,     0,     0,   653,   497,  -391,   -82,    22,  1955,  -452,
     0,     0,   445,    -2,   724,     0,     0,  -361,   550,  -169,
     0,     0, 19503, 19515,  -588,  1700,   452,   325,   558,   265,
     0,     0,     0,    49,  -440,     0,  -412,   286,  -255,  -394,
     0,  -679,  -155,   -73,   645,  -645,   815,  1043,  1437,    32,
   357,   613,     0,   -12,  -799,     0,  -863,     0,  1398,  -173,
 -1049,     0,  -364,  -850,   608,   248,     0,  -973,  1375,  1038,
  -138,  -277,     0,     7,   185,   107,  2251,  -280,   -86,     0,
  -519,  1239,  1689,     0,     0,   229,     0,     0,   892,     0,
     0,   352,  -905,   -24,     0,   543,  -548,  -259,     0,   412,
   356,     0,     0,     0,  -535,     0,   349, -1034,     0,     0,
   354,     0,     0,     0,     0,   305,     0,   -25,    -7,     0,
     0,    43,     0,  -276,     0,     0,     0,     0,     0,  -233,
     0,  -426,     0,     0,     0,     0,     0,     0,    40,     0,
     0,     0,     0,     0,     0,  2229,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,
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
    null,null,"'('","')'",null,null,"','",null,null,null,null,null,null,
    null,null,null,null,null,null,null,"':'","';'",null,"'='",null,"'?'",
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,
    "'['",null,null,"'^'",null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,"'|'","'}'",null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    "keyword_class","keyword_module","keyword_def","keyword_undef",
    "keyword_begin","keyword_rescue","keyword_ensure","keyword_end",
    "keyword_if","keyword_unless","keyword_then","keyword_elsif",
    "keyword_else","keyword_case","keyword_when","keyword_while",
    "keyword_until","keyword_for","keyword_break","keyword_next",
    "keyword_redo","keyword_retry","keyword_do","keyword_do_cond",
    "keyword_do_block","keyword_return","keyword_yield","keyword_super",
    "keyword_self","keyword_nil","keyword_true","keyword_false",
    "keyword_and","keyword_or","keyword_not","modifier_if",
    "modifier_unless","modifier_while","modifier_until","modifier_rescue",
    "keyword_alias","keyword_defined","keyword_BEGIN","keyword_END",
    "keyword__LINE__","keyword__FILE__","keyword__ENCODING__",
    "keyword_do_lambda","tIDENTIFIER","tFID","tGVAR","tIVAR","tCONSTANT",
    "tCVAR","tLABEL","tCHAR","unary+","unary-","tUMINUS_NUM","'**'",
"'<=>'","'=='","'==='","'!='","'>='","'<='","'&&'","'||'","'=~'",
"'!~'","'.'","'..'","'...'","tBDOT2","tBDOT3","'[]'","'[]='",
"'<<'","'>>'","'&.'","'::'","':: at EXPR_BEG'","tOP_ASGN","'=>'",
"'('","'( arg'","')'","'['","'['","'[ arg'",
"'{'","'{ args'","'*'","'*'","'&'","'&'","'~'",
"'%'","'/'","'+'","'-'","'<'","'>'","'|'","'!'",
"'^'","'{'","'}'","'`'","':'","tSTRING_BEG",
    "tXSTRING_BEG","tREGEXP_BEG","tWORDS_BEG","tQWORDS_BEG",
    "tSTRING_DBEG","tSTRING_DVAR","tSTRING_END","'->'","tLAMBEG",
    "tNTH_REF","tBACK_REF","tSTRING_CONTENT","tINTEGER","tIMAGINARY",
    "tFLOAT","tRATIONAL","tREGEXP_END","tSYMBOLS_BEG","tQSYMBOLS_BEG",
"'**'","tSTRING_DEND","tLABEL_END","keyword_in","tLOWEST",
    };

  /** printable rules for debugging.
    */
  protected static final String [] yyRule = {
    "$accept : program",
    "$$1 :",
    "program : $$1 top_compstmt",
    "top_compstmt : top_stmts opt_terms",
    "top_stmts : none",
    "top_stmts : top_stmt",
    "top_stmts : top_stmts terms top_stmt",
    "top_stmts : error top_stmt",
    "top_stmt : stmt",
    "top_stmt : keyword_BEGIN tLCURLY top_compstmt tRCURLY",
    "bodystmt : compstmt opt_rescue opt_else opt_ensure",
    "compstmt : stmts opt_terms",
    "stmts : none",
    "stmts : stmt_or_begin",
    "stmts : stmts terms stmt_or_begin",
    "stmts : error stmt",
    "stmt_or_begin : stmt",
    "$$2 :",
    "stmt_or_begin : keyword_begin $$2 tLCURLY top_compstmt tRCURLY",
    "$$3 :",
    "stmt : keyword_alias fitem $$3 fitem",
    "stmt : keyword_alias tGVAR tGVAR",
    "stmt : keyword_alias tGVAR tBACK_REF",
    "stmt : keyword_alias tGVAR tNTH_REF",
    "stmt : keyword_undef undef_list",
    "stmt : stmt modifier_if expr_value",
    "stmt : stmt modifier_unless expr_value",
    "stmt : stmt modifier_while expr_value",
    "stmt : stmt modifier_until expr_value",
    "stmt : stmt modifier_rescue stmt",
    "stmt : keyword_END tLCURLY compstmt tRCURLY",
    "stmt : command_asgn",
    "stmt : mlhs '=' command_call",
    "stmt : lhs '=' mrhs",
    "stmt : mlhs '=' mrhs_arg modifier_rescue stmt",
    "stmt : mlhs '=' mrhs_arg",
    "stmt : expr",
    "command_asgn : lhs '=' command_rhs",
    "command_asgn : var_lhs tOP_ASGN command_rhs",
    "command_asgn : primary_value '[' opt_call_args rbracket tOP_ASGN command_rhs",
    "command_asgn : primary_value call_op tIDENTIFIER tOP_ASGN command_rhs",
    "command_asgn : primary_value call_op tCONSTANT tOP_ASGN command_rhs",
    "command_asgn : primary_value tCOLON2 tCONSTANT tOP_ASGN command_rhs",
    "command_asgn : primary_value tCOLON2 tIDENTIFIER tOP_ASGN command_rhs",
    "command_asgn : backref tOP_ASGN command_rhs",
    "command_rhs : command_call",
    "command_rhs : command_call modifier_rescue stmt",
    "command_rhs : command_asgn",
    "expr : command_call",
    "expr : expr keyword_and expr",
    "expr : expr keyword_or expr",
    "expr : keyword_not opt_nl expr",
    "expr : tBANG command_call",
    "expr : arg",
    "expr_value : expr",
    "command_call : command",
    "command_call : block_command",
    "block_command : block_call",
    "block_command : block_call call_op2 operation2 command_args",
    "cmd_brace_block : tLBRACE_ARG brace_body tRCURLY",
    "fcall : operation",
    "command : fcall command_args",
    "command : fcall command_args cmd_brace_block",
    "command : primary_value call_op operation2 command_args",
    "command : primary_value call_op operation2 command_args cmd_brace_block",
    "command : primary_value tCOLON2 operation2 command_args",
    "command : primary_value tCOLON2 operation2 command_args cmd_brace_block",
    "command : keyword_super command_args",
    "command : keyword_yield command_args",
    "command : k_return call_args",
    "command : keyword_break call_args",
    "command : keyword_next call_args",
    "mlhs : mlhs_basic",
    "mlhs : tLPAREN mlhs_inner rparen",
    "mlhs_inner : mlhs_basic",
    "mlhs_inner : tLPAREN mlhs_inner rparen",
    "mlhs_basic : mlhs_head",
    "mlhs_basic : mlhs_head mlhs_item",
    "mlhs_basic : mlhs_head tSTAR mlhs_node",
    "mlhs_basic : mlhs_head tSTAR mlhs_node ',' mlhs_post",
    "mlhs_basic : mlhs_head tSTAR",
    "mlhs_basic : mlhs_head tSTAR ',' mlhs_post",
    "mlhs_basic : tSTAR mlhs_node",
    "mlhs_basic : tSTAR mlhs_node ',' mlhs_post",
    "mlhs_basic : tSTAR",
    "mlhs_basic : tSTAR ',' mlhs_post",
    "mlhs_item : mlhs_node",
    "mlhs_item : tLPAREN mlhs_inner rparen",
    "mlhs_head : mlhs_item ','",
    "mlhs_head : mlhs_head mlhs_item ','",
    "mlhs_post : mlhs_item",
    "mlhs_post : mlhs_post ',' mlhs_item",
    "mlhs_node : tIDENTIFIER",
    "mlhs_node : tIVAR",
    "mlhs_node : tGVAR",
    "mlhs_node : tCONSTANT",
    "mlhs_node : tCVAR",
    "mlhs_node : keyword_nil",
    "mlhs_node : keyword_self",
    "mlhs_node : keyword_true",
    "mlhs_node : keyword_false",
    "mlhs_node : keyword__FILE__",
    "mlhs_node : keyword__LINE__",
    "mlhs_node : keyword__ENCODING__",
    "mlhs_node : primary_value '[' opt_call_args rbracket",
    "mlhs_node : primary_value call_op tIDENTIFIER",
    "mlhs_node : primary_value tCOLON2 tIDENTIFIER",
    "mlhs_node : primary_value call_op tCONSTANT",
    "mlhs_node : primary_value tCOLON2 tCONSTANT",
    "mlhs_node : tCOLON3 tCONSTANT",
    "mlhs_node : backref",
    "lhs : tIDENTIFIER",
    "lhs : tIVAR",
    "lhs : tGVAR",
    "lhs : tCONSTANT",
    "lhs : tCVAR",
    "lhs : keyword_nil",
    "lhs : keyword_self",
    "lhs : keyword_true",
    "lhs : keyword_false",
    "lhs : keyword__FILE__",
    "lhs : keyword__LINE__",
    "lhs : keyword__ENCODING__",
    "lhs : primary_value '[' opt_call_args rbracket",
    "lhs : primary_value call_op tIDENTIFIER",
    "lhs : primary_value tCOLON2 tIDENTIFIER",
    "lhs : primary_value call_op tCONSTANT",
    "lhs : primary_value tCOLON2 tCONSTANT",
    "lhs : tCOLON3 tCONSTANT",
    "lhs : backref",
    "cname : tIDENTIFIER",
    "cname : tCONSTANT",
    "cpath : tCOLON3 cname",
    "cpath : cname",
    "cpath : primary_value tCOLON2 cname",
    "fname : tIDENTIFIER",
    "fname : tCONSTANT",
    "fname : tFID",
    "fname : op",
    "fname : reswords",
    "fsym : fname",
    "fsym : symbol",
    "fitem : fsym",
    "fitem : dsym",
    "undef_list : fitem",
    "$$4 :",
    "undef_list : undef_list ',' $$4 fitem",
    "op : tPIPE",
    "op : tCARET",
    "op : tAMPER2",
    "op : tCMP",
    "op : tEQ",
    "op : tEQQ",
    "op : tMATCH",
    "op : tNMATCH",
    "op : tGT",
    "op : tGEQ",
    "op : tLT",
    "op : tLEQ",
    "op : tNEQ",
    "op : tLSHFT",
    "op : tRSHFT",
    "op : tDSTAR",
    "op : tPLUS",
    "op : tMINUS",
    "op : tSTAR2",
    "op : tSTAR",
    "op : tDIVIDE",
    "op : tPERCENT",
    "op : tPOW",
    "op : tBANG",
    "op : tTILDE",
    "op : tUPLUS",
    "op : tUMINUS",
    "op : tAREF",
    "op : tASET",
    "op : tBACK_REF2",
    "reswords : keyword__LINE__",
    "reswords : keyword__FILE__",
    "reswords : keyword__ENCODING__",
    "reswords : keyword_BEGIN",
    "reswords : keyword_END",
    "reswords : keyword_alias",
    "reswords : keyword_and",
    "reswords : keyword_begin",
    "reswords : keyword_break",
    "reswords : keyword_case",
    "reswords : keyword_class",
    "reswords : keyword_def",
    "reswords : keyword_defined",
    "reswords : keyword_do",
    "reswords : keyword_else",
    "reswords : keyword_elsif",
    "reswords : keyword_end",
    "reswords : keyword_ensure",
    "reswords : keyword_false",
    "reswords : keyword_for",
    "reswords : keyword_in",
    "reswords : keyword_module",
    "reswords : keyword_next",
    "reswords : keyword_nil",
    "reswords : keyword_not",
    "reswords : keyword_or",
    "reswords : keyword_redo",
    "reswords : keyword_rescue",
    "reswords : keyword_retry",
    "reswords : keyword_return",
    "reswords : keyword_self",
    "reswords : keyword_super",
    "reswords : keyword_then",
    "reswords : keyword_true",
    "reswords : keyword_undef",
    "reswords : keyword_when",
    "reswords : keyword_yield",
    "reswords : keyword_if",
    "reswords : keyword_unless",
    "reswords : keyword_while",
    "reswords : keyword_until",
    "reswords : modifier_rescue",
    "arg : lhs '=' arg_rhs",
    "arg : var_lhs tOP_ASGN arg_rhs",
    "arg : primary_value '[' opt_call_args rbracket tOP_ASGN arg",
    "arg : primary_value call_op tIDENTIFIER tOP_ASGN arg_rhs",
    "arg : primary_value call_op tCONSTANT tOP_ASGN arg_rhs",
    "arg : primary_value tCOLON2 tIDENTIFIER tOP_ASGN arg_rhs",
    "arg : primary_value tCOLON2 tCONSTANT tOP_ASGN arg_rhs",
    "arg : tCOLON3 tCONSTANT tOP_ASGN arg_rhs",
    "arg : backref tOP_ASGN arg_rhs",
    "arg : arg tDOT2 arg",
    "arg : arg tDOT2",
    "arg : tBDOT2 arg",
    "arg : arg tDOT3 arg",
    "arg : arg tDOT3",
    "arg : tBDOT3 arg",
    "arg : arg tPLUS arg",
    "arg : arg tMINUS arg",
    "arg : arg tSTAR2 arg",
    "arg : arg tDIVIDE arg",
    "arg : arg tPERCENT arg",
    "arg : arg tPOW arg",
    "arg : tUMINUS_NUM simple_numeric tPOW arg",
    "arg : tUPLUS arg",
    "arg : tUMINUS arg",
    "arg : arg tPIPE arg",
    "arg : arg tCARET arg",
    "arg : arg tAMPER2 arg",
    "arg : arg tCMP arg",
    "arg : rel_expr",
    "arg : arg tEQ arg",
    "arg : arg tEQQ arg",
    "arg : arg tNEQ arg",
    "arg : arg tMATCH arg",
    "arg : arg tNMATCH arg",
    "arg : tBANG arg",
    "arg : tTILDE arg",
    "arg : arg tLSHFT arg",
    "arg : arg tRSHFT arg",
    "arg : arg tANDOP arg",
    "arg : arg tOROP arg",
    "arg : keyword_defined opt_nl arg",
    "arg : arg '?' arg opt_nl ':' arg",
    "arg : primary",
    "relop : tGT",
    "relop : tLT",
    "relop : tGEQ",
    "relop : tLEQ",
    "rel_expr : arg relop arg",
    "rel_expr : rel_expr relop arg",
    "arg_value : arg",
    "aref_args : none",
    "aref_args : args trailer",
    "aref_args : args ',' assocs trailer",
    "aref_args : assocs trailer",
    "arg_rhs : arg",
    "arg_rhs : arg modifier_rescue arg",
    "paren_args : tLPAREN2 opt_call_args rparen",
    "paren_args : tLPAREN2 args ',' args_forward rparen",
    "paren_args : tLPAREN2 args_forward rparen",
    "opt_paren_args : none",
    "opt_paren_args : paren_args",
    "opt_call_args : none",
    "opt_call_args : call_args",
    "opt_call_args : args ','",
    "opt_call_args : args ',' assocs ','",
    "opt_call_args : assocs ','",
    "call_args : command",
    "call_args : args opt_block_arg",
    "call_args : assocs opt_block_arg",
    "call_args : args ',' assocs opt_block_arg",
    "call_args : block_arg",
    "$$5 :",
    "command_args : $$5 call_args",
    "block_arg : tAMPER arg_value",
    "block_arg : tAMPER",
    "opt_block_arg : ',' block_arg",
    "opt_block_arg : none_block_pass",
    "args : arg_value",
    "args : tSTAR arg_value",
    "args : args ',' arg_value",
    "args : args ',' tSTAR arg_value",
    "mrhs_arg : mrhs",
    "mrhs_arg : arg_value",
    "mrhs : args ',' arg_value",
    "mrhs : args ',' tSTAR arg_value",
    "mrhs : tSTAR arg_value",
    "primary : literal",
    "primary : strings",
    "primary : xstring",
    "primary : regexp",
    "primary : words",
    "primary : qwords",
    "primary : symbols",
    "primary : qsymbols",
    "primary : var_ref",
    "primary : backref",
    "primary : tFID",
    "$$6 :",
    "primary : keyword_begin $$6 bodystmt keyword_end",
    "$$7 :",
    "primary : tLPAREN_ARG $$7 rparen",
    "$$8 :",
    "$$9 :",
    "primary : tLPAREN_ARG $$8 stmt $$9 rparen",
    "primary : tLPAREN compstmt tRPAREN",
    "primary : primary_value tCOLON2 tCONSTANT",
    "primary : tCOLON3 tCONSTANT",
    "primary : tLBRACK aref_args tRBRACK",
    "primary : tLBRACE assoc_list tRCURLY",
    "primary : k_return",
    "primary : keyword_yield tLPAREN2 call_args rparen",
    "primary : keyword_yield tLPAREN2 rparen",
    "primary : keyword_yield",
    "primary : keyword_defined opt_nl tLPAREN2 expr rparen",
    "primary : keyword_not tLPAREN2 expr rparen",
    "primary : keyword_not tLPAREN2 rparen",
    "primary : fcall brace_block",
    "primary : method_call",
    "primary : method_call brace_block",
    "primary : tLAMBDA lambda",
    "primary : keyword_if expr_value then compstmt if_tail keyword_end",
    "primary : keyword_unless expr_value then compstmt opt_else keyword_end",
    "$$10 :",
    "$$11 :",
    "primary : keyword_while $$10 expr_value do $$11 compstmt keyword_end",
    "$$12 :",
    "$$13 :",
    "primary : keyword_until $$12 expr_value do $$13 compstmt keyword_end",
    "primary : keyword_case expr_value opt_terms case_body keyword_end",
    "primary : keyword_case opt_terms case_body keyword_end",
    "primary : keyword_case expr_value opt_terms p_case_body keyword_end",
    "$$14 :",
    "$$15 :",
    "primary : keyword_for for_var keyword_in $$14 expr_value do $$15 compstmt keyword_end",
    "$$16 :",
    "primary : k_class cpath superclass $$16 bodystmt keyword_end",
    "$$17 :",
    "primary : k_class tLSHFT expr $$17 term bodystmt keyword_end",
    "$$18 :",
    "primary : k_module cpath $$18 bodystmt keyword_end",
    "$$19 :",
    "$$20 :",
    "primary : keyword_def fname $$19 $$20 f_arglist bodystmt keyword_end",
    "$$21 :",
    "$$22 :",
    "primary : keyword_def singleton dot_or_colon $$21 fname $$22 f_arglist bodystmt keyword_end",
    "primary : keyword_break",
    "primary : keyword_next",
    "primary : keyword_redo",
    "primary : keyword_retry",
    "primary_value : primary",
    "k_class : keyword_class",
    "k_module : keyword_module",
    "k_return : keyword_return",
    "then : term",
    "then : keyword_then",
    "then : term keyword_then",
    "do : term",
    "do : keyword_do_cond",
    "if_tail : opt_else",
    "if_tail : keyword_elsif expr_value then compstmt if_tail",
    "opt_else : none",
    "opt_else : keyword_else compstmt",
    "for_var : lhs",
    "for_var : mlhs",
    "f_marg : f_norm_arg",
    "f_marg : tLPAREN f_margs rparen",
    "f_marg_list : f_marg",
    "f_marg_list : f_marg_list ',' f_marg",
    "f_margs : f_marg_list",
    "f_margs : f_marg_list ',' tSTAR f_norm_arg",
    "f_margs : f_marg_list ',' tSTAR f_norm_arg ',' f_marg_list",
    "f_margs : f_marg_list ',' tSTAR",
    "f_margs : f_marg_list ',' tSTAR ',' f_marg_list",
    "f_margs : tSTAR f_norm_arg",
    "f_margs : tSTAR f_norm_arg ',' f_marg_list",
    "f_margs : tSTAR",
    "f_margs : tSTAR ',' f_marg_list",
    "block_args_tail : f_block_kwarg ',' f_kwrest opt_f_block_arg",
    "block_args_tail : f_block_kwarg opt_f_block_arg",
    "block_args_tail : f_kwrest opt_f_block_arg",
    "block_args_tail : f_no_kwarg opt_f_block_arg",
    "block_args_tail : f_block_arg",
    "opt_block_args_tail : ',' block_args_tail",
    "opt_block_args_tail :",
    "block_param : f_arg ',' f_block_optarg ',' f_rest_arg opt_block_args_tail",
    "block_param : f_arg ',' f_block_optarg ',' f_rest_arg ',' f_arg opt_block_args_tail",
    "block_param : f_arg ',' f_block_optarg opt_block_args_tail",
    "block_param : f_arg ',' f_block_optarg ',' f_arg opt_block_args_tail",
    "block_param : f_arg ',' f_rest_arg opt_block_args_tail",
    "block_param : f_arg ','",
    "block_param : f_arg ',' f_rest_arg ',' f_arg opt_block_args_tail",
    "block_param : f_arg opt_block_args_tail",
    "block_param : f_block_optarg ',' f_rest_arg opt_block_args_tail",
    "block_param : f_block_optarg ',' f_rest_arg ',' f_arg opt_block_args_tail",
    "block_param : f_block_optarg opt_block_args_tail",
    "block_param : f_block_optarg ',' f_arg opt_block_args_tail",
    "block_param : f_rest_arg opt_block_args_tail",
    "block_param : f_rest_arg ',' f_arg opt_block_args_tail",
    "block_param : block_args_tail",
    "opt_block_param : none",
    "$$23 :",
    "opt_block_param : $$23 block_param_def",
    "block_param_def : tPIPE opt_bv_decl tPIPE",
    "block_param_def : tOROP",
    "block_param_def : tPIPE block_param opt_bv_decl tPIPE",
    "opt_bv_decl : opt_nl",
    "opt_bv_decl : opt_nl ';' bv_decls opt_nl",
    "bv_decls : bvar",
    "bv_decls : bv_decls ',' bvar",
    "bvar : tIDENTIFIER",
    "bvar : f_bad_arg",
    "$$24 :",
    "$$25 :",
    "lambda : $$24 f_larglist $$25 lambda_body",
    "$$26 :",
    "f_larglist : tLPAREN2 $$26 f_args opt_bv_decl tRPAREN",
    "$$27 :",
    "f_larglist : $$27 f_args_any",
    "f_larglist :",
    "lambda_body : tLAMBEG compstmt tRCURLY",
    "lambda_body : keyword_do_lambda bodystmt keyword_end",
    "do_block : keyword_do_block do_body keyword_end",
    "block_call : command do_block",
    "block_call : block_call call_op2 operation2 opt_paren_args",
    "block_call : block_call call_op2 operation2 opt_paren_args brace_block",
    "block_call : block_call call_op2 operation2 command_args do_block",
    "method_call : fcall paren_args",
    "method_call : primary_value call_op operation2 opt_paren_args",
    "method_call : primary_value tCOLON2 operation2 paren_args",
    "method_call : primary_value tCOLON2 operation3",
    "method_call : primary_value call_op paren_args",
    "method_call : primary_value tCOLON2 paren_args",
    "method_call : keyword_super paren_args",
    "method_call : keyword_super",
    "method_call : primary_value '[' opt_call_args rbracket",
    "brace_block : tLCURLY brace_body tRCURLY",
    "brace_block : keyword_do do_body keyword_end",
    "$$28 :",
    "$$29 :",
    "brace_body : $$28 $$29 opt_block_param compstmt",
    "$$30 :",
    "$$31 :",
    "do_body : $$30 $$31 opt_block_param bodystmt",
    "case_body : keyword_when args then compstmt cases",
    "cases : opt_else",
    "cases : case_body",
    "$$32 :",
    "$$33 :",
    "$$34 :",
    "p_case_body : keyword_in $$32 $$33 p_top_expr then $$34 compstmt p_cases",
    "p_cases : opt_else",
    "p_cases : p_case_body",
    "p_top_expr : p_top_expr_body",
    "p_top_expr : p_top_expr_body modifier_if expr_value",
    "p_top_expr : p_top_expr_body modifier_unless expr_value",
    "p_top_expr_body : p_expr",
    "p_top_expr_body : p_expr ','",
    "p_top_expr_body : p_expr ',' p_args",
    "p_top_expr_body : p_find",
    "p_top_expr_body : p_args_tail",
    "p_top_expr_body : p_kwargs",
    "p_expr : p_as",
    "p_as : p_expr tASSOC p_variable",
    "p_as : p_alt",
    "p_alt : p_alt '|' p_expr_basic",
    "p_alt : p_expr_basic",
    "p_lparen : '('",
    "p_lbracket : '['",
    "p_expr_basic : p_value",
    "p_expr_basic : p_variable",
    "p_expr_basic : p_const p_lparen p_args rparen",
    "p_expr_basic : p_const p_lparen p_find rparen",
    "p_expr_basic : p_const p_lparen p_kwargs rparen",
    "p_expr_basic : p_const '(' rparen",
    "p_expr_basic : p_const p_lbracket p_args rbracket",
    "p_expr_basic : p_const p_lbracket p_find rbracket",
    "p_expr_basic : p_const p_lbracket p_kwargs rbracket",
    "p_expr_basic : p_const '[' rbracket",
    "p_expr_basic : tLBRACK p_args rbracket",
    "p_expr_basic : tLBRACK p_find rbracket",
    "p_expr_basic : tLBRACK rbracket",
    "$$35 :",
    "p_expr_basic : tLBRACE $$35 p_kwargs rbrace",
    "p_expr_basic : tLBRACE rbrace",
    "$$36 :",
    "p_expr_basic : tLPAREN $$36 p_expr rparen",
    "p_args : p_expr",
    "p_args : p_args_head",
    "p_args : p_args_head p_arg",
    "p_args : p_args_head tSTAR tIDENTIFIER",
    "p_args : p_args_head tSTAR tIDENTIFIER ',' p_args_post",
    "p_args : p_args_head tSTAR",
    "p_args : p_args_head tSTAR ',' p_args_post",
    "p_args : p_args_tail",
    "p_args_head : p_arg ','",
    "p_args_head : p_args_head p_arg ','",
    "p_args_tail : p_rest",
    "p_args_tail : p_rest ',' p_args_post",
    "p_find : p_rest ',' p_args_post ',' p_rest",
    "p_rest : tSTAR tIDENTIFIER",
    "p_rest : tSTAR",
    "p_args_post : p_arg",
    "p_args_post : p_args_post ',' p_arg",
    "p_arg : p_expr",
    "p_kwargs : p_kwarg ',' p_any_kwrest",
    "p_kwargs : p_kwarg",
    "p_kwargs : p_kwarg ','",
    "p_kwargs : p_any_kwrest",
    "p_kwarg : p_kw",
    "p_kwarg : p_kwarg ',' p_kw",
    "p_kw : p_kw_label p_expr",
    "p_kw : p_kw_label",
    "p_kw_label : tLABEL",
    "p_kw_label : tSTRING_BEG string_contents tLABEL_END",
    "p_kwrest : kwrest_mark tIDENTIFIER",
    "p_kwrest : kwrest_mark",
    "p_kwnorest : kwrest_mark keyword_nil",
    "p_any_kwrest : p_kwrest",
    "p_any_kwrest : p_kwnorest",
    "p_value : p_primitive",
    "p_value : p_primitive tDOT2 p_primitive",
    "p_value : p_primitive tDOT3 p_primitive",
    "p_value : p_primitive tDOT2",
    "p_value : p_primitive tDOT3",
    "p_value : p_var_ref",
    "p_value : p_expr_ref",
    "p_value : p_const",
    "p_value : tBDOT2 p_primitive",
    "p_value : tBDOT3 p_primitive",
    "p_primitive : literal",
    "p_primitive : strings",
    "p_primitive : xstring",
    "p_primitive : regexp",
    "p_primitive : words",
    "p_primitive : qwords",
    "p_primitive : symbols",
    "p_primitive : qsymbols",
    "p_primitive : keyword_nil",
    "p_primitive : keyword_self",
    "p_primitive : keyword_true",
    "p_primitive : keyword_false",
    "p_primitive : keyword__FILE__",
    "p_primitive : keyword__LINE__",
    "p_primitive : keyword__ENCODING__",
    "p_primitive : lambda",
    "p_variable : tIDENTIFIER",
    "p_var_ref : '^' tIDENTIFIER",
    "p_var_ref : '^' nonlocal_var",
    "p_expr_ref : '^' tLPAREN expr_value ')'",
    "p_const : tCOLON3 cname",
    "p_const : p_const tCOLON2 cname",
    "p_const : tCONSTANT",
    "opt_rescue : keyword_rescue exc_list exc_var then compstmt opt_rescue",
    "opt_rescue :",
    "exc_list : arg_value",
    "exc_list : mrhs",
    "exc_list : none",
    "exc_var : tASSOC lhs",
    "exc_var : none",
    "opt_ensure : keyword_ensure compstmt",
    "opt_ensure : none",
    "literal : numeric",
    "literal : symbol",
    "literal : dsym",
    "strings : string",
    "string : tCHAR",
    "string : string1",
    "string : string string1",
    "string1 : tSTRING_BEG string_contents tSTRING_END",
    "xstring : tXSTRING_BEG xstring_contents tSTRING_END",
    "regexp : tREGEXP_BEG regexp_contents tREGEXP_END",
    "words : tWORDS_BEG ' ' word_list tSTRING_END",
    "word_list :",
    "word_list : word_list word ' '",
    "word : string_content",
    "word : word string_content",
    "symbols : tSYMBOLS_BEG ' ' symbol_list tSTRING_END",
    "symbol_list :",
    "symbol_list : symbol_list word ' '",
    "qwords : tQWORDS_BEG ' ' qword_list tSTRING_END",
    "qsymbols : tQSYMBOLS_BEG ' ' qsym_list tSTRING_END",
    "qword_list :",
    "qword_list : qword_list tSTRING_CONTENT ' '",
    "qsym_list :",
    "qsym_list : qsym_list tSTRING_CONTENT ' '",
    "string_contents :",
    "string_contents : string_contents string_content",
    "xstring_contents :",
    "xstring_contents : xstring_contents string_content",
    "regexp_contents :",
    "regexp_contents : regexp_contents string_content",
    "string_content : tSTRING_CONTENT",
    "$$37 :",
    "string_content : tSTRING_DVAR $$37 string_dvar",
    "$$38 :",
    "$$39 :",
    "$$40 :",
    "$$41 :",
    "$$42 :",
    "string_content : tSTRING_DBEG $$38 $$39 $$40 $$41 $$42 compstmt tSTRING_DEND",
    "string_dvar : tGVAR",
    "string_dvar : tIVAR",
    "string_dvar : tCVAR",
    "string_dvar : backref",
    "symbol : tSYMBEG sym",
    "sym : fname",
    "sym : tIVAR",
    "sym : tGVAR",
    "sym : tCVAR",
    "dsym : tSYMBEG xstring_contents tSTRING_END",
    "numeric : simple_numeric",
    "numeric : tUMINUS_NUM simple_numeric",
    "nonlocal_var : tIVAR",
    "nonlocal_var : tGVAR",
    "nonlocal_var : tCVAR",
    "simple_numeric : tINTEGER",
    "simple_numeric : tFLOAT",
    "simple_numeric : tRATIONAL",
    "simple_numeric : tIMAGINARY",
    "var_ref : tIDENTIFIER",
    "var_ref : tIVAR",
    "var_ref : tGVAR",
    "var_ref : tCONSTANT",
    "var_ref : tCVAR",
    "var_ref : keyword_nil",
    "var_ref : keyword_self",
    "var_ref : keyword_true",
    "var_ref : keyword_false",
    "var_ref : keyword__FILE__",
    "var_ref : keyword__LINE__",
    "var_ref : keyword__ENCODING__",
    "var_lhs : tIDENTIFIER",
    "var_lhs : tIVAR",
    "var_lhs : tGVAR",
    "var_lhs : tCONSTANT",
    "var_lhs : tCVAR",
    "var_lhs : keyword_nil",
    "var_lhs : keyword_self",
    "var_lhs : keyword_true",
    "var_lhs : keyword_false",
    "var_lhs : keyword__FILE__",
    "var_lhs : keyword__LINE__",
    "var_lhs : keyword__ENCODING__",
    "backref : tNTH_REF",
    "backref : tBACK_REF",
    "$$43 :",
    "superclass : tLT $$43 expr_value term",
    "superclass :",
    "f_arglist : tLPAREN2 f_args rparen",
    "$$44 :",
    "f_arglist : $$44 f_args term",
    "args_tail : f_kwarg ',' f_kwrest opt_f_block_arg",
    "args_tail : f_kwarg opt_f_block_arg",
    "args_tail : f_kwrest opt_f_block_arg",
    "args_tail : f_no_kwarg opt_f_block_arg",
    "args_tail : f_block_arg",
    "opt_args_tail : ',' args_tail",
    "opt_args_tail :",
    "f_args : f_args_any",
    "f_args :",
    "f_args_any : f_arg ',' f_optarg ',' f_rest_arg opt_args_tail",
    "f_args_any : f_arg ',' f_optarg ',' f_rest_arg ',' f_arg opt_args_tail",
    "f_args_any : f_arg ',' f_optarg opt_args_tail",
    "f_args_any : f_arg ',' f_optarg ',' f_arg opt_args_tail",
    "f_args_any : f_arg ',' f_rest_arg opt_args_tail",
    "f_args_any : f_arg ',' f_rest_arg ',' f_arg opt_args_tail",
    "f_args_any : f_arg opt_args_tail",
    "f_args_any : f_optarg ',' f_rest_arg opt_args_tail",
    "f_args_any : f_optarg ',' f_rest_arg ',' f_arg opt_args_tail",
    "f_args_any : f_optarg opt_args_tail",
    "f_args_any : f_optarg ',' f_arg opt_args_tail",
    "f_args_any : f_rest_arg opt_args_tail",
    "f_args_any : f_rest_arg ',' f_arg opt_args_tail",
    "f_args_any : args_tail",
    "f_args_any : f_arg ',' args_forward",
    "f_args_any : args_forward",
    "args_forward : tBDOT3",
    "f_bad_arg : tCONSTANT",
    "f_bad_arg : tIVAR",
    "f_bad_arg : tGVAR",
    "f_bad_arg : tCVAR",
    "f_norm_arg : f_bad_arg",
    "f_norm_arg : tIDENTIFIER",
    "f_arg_asgn : f_norm_arg",
    "f_arg_item : f_arg_asgn",
    "f_arg_item : tLPAREN f_margs rparen",
    "f_arg : f_arg_item",
    "f_arg : f_arg ',' f_arg_item",
    "f_label : tLABEL",
    "f_kw : f_label arg_value",
    "f_kw : f_label",
    "f_block_kw : f_label primary_value",
    "f_block_kw : f_label",
    "f_block_kwarg : f_block_kw",
    "f_block_kwarg : f_block_kwarg ',' f_block_kw",
    "f_kwarg : f_kw",
    "f_kwarg : f_kwarg ',' f_kw",
    "kwrest_mark : tPOW",
    "kwrest_mark : tDSTAR",
    "f_no_kwarg : kwrest_mark keyword_nil",
    "f_kwrest : kwrest_mark tIDENTIFIER",
    "f_kwrest : kwrest_mark",
    "f_opt : f_arg_asgn '=' arg_value",
    "f_block_opt : f_arg_asgn '=' primary_value",
    "f_block_optarg : f_block_opt",
    "f_block_optarg : f_block_optarg ',' f_block_opt",
    "f_optarg : f_opt",
    "f_optarg : f_optarg ',' f_opt",
    "restarg_mark : tSTAR2",
    "restarg_mark : tSTAR",
    "f_rest_arg : restarg_mark tIDENTIFIER",
    "f_rest_arg : restarg_mark",
    "blkarg_mark : tAMPER2",
    "blkarg_mark : tAMPER",
    "f_block_arg : blkarg_mark tIDENTIFIER",
    "f_block_arg : blkarg_mark",
    "opt_f_block_arg : ',' f_block_arg",
    "opt_f_block_arg :",
    "singleton : var_ref",
    "$$45 :",
    "singleton : tLPAREN2 $$45 expr rparen",
    "assoc_list : none",
    "assoc_list : assocs trailer",
    "assocs : assoc",
    "assocs : assocs ',' assoc",
    "assoc : arg_value tASSOC arg_value",
    "assoc : tLABEL arg_value",
    "assoc : tLABEL",
    "assoc : tSTRING_BEG string_contents tLABEL_END arg_value",
    "assoc : tDSTAR arg_value",
    "operation : tIDENTIFIER",
    "operation : tCONSTANT",
    "operation : tFID",
    "operation2 : tIDENTIFIER",
    "operation2 : tCONSTANT",
    "operation2 : tFID",
    "operation2 : op",
    "operation3 : tIDENTIFIER",
    "operation3 : tFID",
    "operation3 : op",
    "dot_or_colon : tDOT",
    "dot_or_colon : tCOLON2",
    "call_op : tDOT",
    "call_op : tANDDOT",
    "call_op2 : call_op",
    "call_op2 : tCOLON2",
    "opt_terms :",
    "opt_terms : terms",
    "opt_nl :",
    "opt_nl : '\\n'",
    "rparen : opt_nl tRPAREN",
    "rbracket : opt_nl tRBRACK",
    "rbrace : opt_nl '}'",
    "trailer :",
    "trailer : '\\n'",
    "trailer : ','",
    "term : ';'",
    "term : '\\n'",
    "terms : term",
    "terms : terms ';'",
    "none :",
    "none_block_pass :",
    };

  protected org.truffleruby.parser.parser.YYDebug yydebug;

  /** index-checked interface to {@link #yyNames}.
      @param token single character or <tt>%token</tt> value.
      @return token name or <tt>[illegal]</tt> or <tt>[unknown]</tt>.
    */
  public static String yyName (int token) {
    if (token < 0 || token > yyNames.length) return "[illegal]";
    String name;
    if ((name = yyNames[token]) != null) return name;
    return "[unknown]";
  }


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
    this.yydebug = (org.truffleruby.parser.parser.YYDebug) ayydebug;
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
      if (yydebug != null) yydebug.push(yyState, yyVal);

      yyDiscarded: for (;;) {	// discarding a token does not change stack
        int yyN;
        if ((yyN = yyDefRed[yyState]) == 0) {	// else [default] reduce (yyN)
          if (yyToken < 0) {
//            yyToken = yyLex.advance() ? yyLex.token() : 0;
            yyToken = yyLex.nextToken();
            if (yydebug != null)
              yydebug.lex(yyState, yyToken, yyName(yyToken), yyLex.value());
          }
          if ((yyN = yySindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < yyTable.length && yyCheck[yyN] == yyToken) {
            if (yydebug != null)
              yydebug.shift(yyState, yyTable[yyN], yyErrorFlag-1);
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
              if (yydebug != null) yydebug.error("syntax error");
              break;
  
            case 1: case 2:
              yyErrorFlag = 3;
              do {
                if ((yyN = yySindex[yyStates[yyTop]]) != 0
                    && (yyN += yyErrorCode) >= 0 && yyN < yyTable.length
                    && yyCheck[yyN] == yyErrorCode) {
                  if (yydebug != null)
                    yydebug.shift(yyStates[yyTop], yyTable[yyN], 3);
                  yyState = yyTable[yyN];
                  yyVal = yyLex.value();
                  continue yyLoop;
                }
                if (yydebug != null) yydebug.pop(yyStates[yyTop]);
              } while (-- yyTop >= 0);
              if (yydebug != null) yydebug.reject();
              support.yyerror("irrecoverable syntax error");
              break;

            case 3:
              if (yyToken == 0) {
                if (yydebug != null) yydebug.reject();
                support.yyerror("irrecoverable syntax error at end-of-file");
              }
              if (yydebug != null)
                yydebug.discard(yyState, yyToken, yyName(yyToken),
  							yyLex.value());
              yyToken = -1;
              continue yyDiscarded;		// leave stack alone
            }
        }
        int yyV = yyTop + 1-yyLen[yyN];
        if (yydebug != null)
          yydebug.reduce(yyState, yyStates[yyV-1], yyN, yyRule[yyN], yyLen[yyN]);
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
          if (yydebug != null) yydebug.shift(0, yyFinal);
          yyState = yyFinal;
          if (yyToken < 0) {
            yyToken = yyLex.nextToken();
//            yyToken = yyLex.advance() ? yyLex.token() : 0;
            if (yydebug != null)
               yydebug.lex(yyState, yyToken,yyName(yyToken), yyLex.value());
          }
          if (yyToken == 0) {
            if (yydebug != null) yydebug.accept(yyVal);
            return yyVal;
          }
          continue yyLoop;
        }
        if ((yyN = yyGindex[yyM]) != 0 && (yyN += yyState) >= 0
            && yyN < yyTable.length && yyCheck[yyN] == yyState)
          yyState = yyTable[yyN];
        else
          yyState = yyDgoto[yyM];
        if (yydebug != null) yydebug.shift(yyStates[yyTop], yyState);
        continue yyLoop;
      }
    }
  }

static ParserState[] states = new ParserState[782];
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
            node = new EnsureParseNode(support.extendedUntil(support.getPosition(((ParseNode)yyVals[-3+yyTop])), support.getPosition(((ParseNode)yyVals[0+yyTop]))), support.makeNullNil(node), ((ParseNode)yyVals[0+yyTop]));
        } else {
            node = support.appendToBlock(((ParseNode)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
        }
    }

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
    yyVal = new VAliasParseNode(((SourceIndexLength)yyVals[-2+yyTop]), support.symbolID(((TruffleString)yyVals[-1+yyTop])), support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[22] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new VAliasParseNode(((SourceIndexLength)yyVals[-2+yyTop]), support.symbolID(((TruffleString)yyVals[-1+yyTop])), support.symbolID(((BackRefParseNode)yyVals[0+yyTop]).getByteName()));
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
    ((ParseNode)yyVal).extendPosition(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[26] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.getConditionNode(((ParseNode)yyVals[0+yyTop])), null, ((ParseNode)yyVals[-2+yyTop]));
    ((ParseNode)yyVal).extendPosition(((ParseNode)yyVals[0+yyTop]));
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
    TruffleString asgnOp = ((TruffleString)yyVals[-1+yyTop]);
    if (asgnOp == TStringConstants.OR_OR) {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
        yyVal = new OpAsgnOrParseNode(pos, support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), ((AssignableParseNode)yyVals[-2+yyTop]));
    } else if (asgnOp == TStringConstants.AMPERSAND_AMPERSAND) {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
        yyVal = new OpAsgnAndParseNode(pos, support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), ((AssignableParseNode)yyVals[-2+yyTop]));
    } else {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(support.getOperatorCallNode(support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), asgnOp, ((ParseNode)yyVals[0+yyTop])));
        yyVal = ((AssignableParseNode)yyVals[-2+yyTop]);
    }
    return yyVal;
};
states[39] = (support, lexer, yyVal, yyVals, yyTop) -> {
  /* FIXME: arg_concat logic missing for opt_call_args*/
                    yyVal = support.new_opElementAsgnNode(((ParseNode)yyVals[-5+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[40] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[41] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[42] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength pos = ((ParseNode)yyVals[-4+yyTop]).getPosition();
    yyVal = support.newOpConstAsgn(pos, support.new_colon2(pos, ((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-2+yyTop])), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[43] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]));
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
    yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode)yyVals[0+yyTop])), TStringConstants.BANG);
    return yyVal;
};
states[52] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[54] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[58] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[59] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((IterParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[60] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_fcall(((TruffleString)yyVals[0+yyTop]));
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
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[64] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-3+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((IterParseNode)yyVals[0+yyTop])); 
    return yyVal;
};
states[65] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[66] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((IterParseNode)yyVals[0+yyTop]));
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
    yyVal = support.assignableLabelOrIdentifier(((TruffleString)yyVals[0+yyTop]), null);
    return yyVal;
};
states[93] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstAsgnParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[94] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalAsgnParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[95] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) support.compile_error("dynamic constant assignment");
    yyVal = new ConstDeclParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), null, NilImplicitParseNode.NIL);
    return yyVal;
};
states[96] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarAsgnParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
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
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[106] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[107] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[108] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) support.yyerror("dynamic constant assignment");

    SourceIndexLength position = support.getPosition(((ParseNode)yyVals[-2+yyTop]));

    yyVal = new ConstDeclParseNode(position, (TruffleString) null, support.new_colon2(position, ((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[109] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) {
        support.yyerror("dynamic constant assignment");
    }

    SourceIndexLength position = lexer.tokline;

    yyVal = new ConstDeclParseNode(position, (TruffleString) null, support.new_colon3(position, ((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[110] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.backrefAssignError(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[111] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.assignableLabelOrIdentifier(((TruffleString)yyVals[0+yyTop]), null);
    return yyVal;
};
states[112] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstAsgnParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[113] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalAsgnParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[114] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) support.compile_error("dynamic constant assignment");

    yyVal = new ConstDeclParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), null, NilImplicitParseNode.NIL);
    return yyVal;
};
states[115] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarAsgnParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
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
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[125] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[126] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.attrset(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[127] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) {
        support.yyerror("dynamic constant assignment");
    }

    SourceIndexLength position = support.getPosition(((ParseNode)yyVals[-2+yyTop]));

    yyVal = new ConstDeclParseNode(position, (TruffleString) null, support.new_colon2(position, ((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[128] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) {
        support.yyerror("dynamic constant assignment");
    }

    SourceIndexLength position = lexer.tokline;

    yyVal = new ConstDeclParseNode(position, (TruffleString) null, support.new_colon3(position, ((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
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
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[132] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon3(lexer.tokline, ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[133] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon2(lexer.tokline, null, ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[134] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon2(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[135] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[136] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[137] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[138] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_ENDFN);
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[139] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_ENDFN);
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[140] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new LiteralParseNode(lexer.getPosition(), support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[141] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new LiteralParseNode(lexer.getPosition(), support.symbolID(((TruffleString)yyVals[0+yyTop])));
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
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[148] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[149] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[150] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[151] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[152] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[153] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[154] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[155] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[156] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[157] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[158] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[159] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[160] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[161] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[162] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[163] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[164] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[165] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[166] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[167] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[168] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[169] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[170] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[171] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[172] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[173] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[174] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[175] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[176] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
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
    ((ParseNode)yyVal).extendPosition(((ParseNode)yyVals[-2+yyTop]));
    return yyVal;
};
states[220] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));

    SourceIndexLength pos = ((AssignableParseNode)yyVals[-2+yyTop]).getPosition();
    TruffleString asgnOp = ((TruffleString)yyVals[-1+yyTop]);
    if (asgnOp == TStringConstants.OR_OR) {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
        yyVal = new OpAsgnOrParseNode(pos, support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), ((AssignableParseNode)yyVals[-2+yyTop]));
    } else if (asgnOp == TStringConstants.AMPERSAND_AMPERSAND) {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(((ParseNode)yyVals[0+yyTop]));
        yyVal = new OpAsgnAndParseNode(pos, support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), ((AssignableParseNode)yyVals[-2+yyTop]));
    } else {
        ((AssignableParseNode)yyVals[-2+yyTop]).setValueNode(support.getOperatorCallNode(support.gettable2(((AssignableParseNode)yyVals[-2+yyTop])), asgnOp, ((ParseNode)yyVals[0+yyTop])));
        yyVal = ((AssignableParseNode)yyVals[-2+yyTop]);
    }
    return yyVal;
};
states[221] = (support, lexer, yyVal, yyVals, yyTop) -> {
  /* FIXME: arg_concat missing for opt_call_args*/
                    yyVal = support.new_opElementAsgnNode(((ParseNode)yyVals[-5+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[222] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[223] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[224] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newOpAsgn(support.getPosition(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[225] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength pos = support.getPosition(((ParseNode)yyVals[-4+yyTop]));
    yyVal = support.newOpConstAsgn(pos, support.new_colon2(pos, ((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-2+yyTop])), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[226] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength pos = lexer.getPosition();
    yyVal = support.newOpConstAsgn(pos, new Colon3ParseNode(pos, support.symbolID(((TruffleString)yyVals[-2+yyTop]))), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
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
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[235] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[236] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[237] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[238] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[239] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[240] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(support.getOperatorCallNode(((NumericParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition()), ((TruffleString)yyVals[-3+yyTop]));
    return yyVal;
};
states[241] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[0+yyTop]), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[242] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[0+yyTop]), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[243] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[244] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[245] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[246] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[247] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[248] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[249] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[250] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
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
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[253] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[254] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[0+yyTop]), ((TruffleString)yyVals[-1+yyTop]));
    return yyVal;
};
states[255] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[256] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[257] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newAndNode(((ParseNode)yyVals[-2+yyTop]).getPosition(), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[258] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newOrNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
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
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[263] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[264] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[265] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[266] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
    return yyVal;
};
states[267] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.warning(lexer.getPosition(), "comparison '" + ((TruffleString)yyVals[-1+yyTop]).toJavaStringUncached() + "' after comparison");
    yyVal = support.getOperatorCallNode(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), lexer.getPosition());
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
    ((HashParseNode)yyVals[-1+yyTop]).setKeywordArguments(true);
    yyVal = support.arg_append(((ParseNode)yyVals[-3+yyTop]), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[272] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ((HashParseNode)yyVals[-1+yyTop]).setKeywordArguments(true);
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
    if (yyVal != null) ((ParseNode)yyVal).extendPosition(((SourceIndexLength)yyVals[-2+yyTop]));
    return yyVal;
};
states[276] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(null);
    /* NOTE(norswap, 02 Jun 2021): location (0) arg is unused*/
    SplatParseNode splat = support.newSplatNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_REST_VAR));
    HashParseNode kwrest = new HashParseNode(position, support.createKeyValue(null, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_KWREST_VAR)));
    kwrest.setKeywordArguments(true);
    BlockPassParseNode block = new BlockPassParseNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_BLOCK_VAR));
    yyVal = support.arg_concat(support.getPosition(((ParseNode)yyVals[-3+yyTop])), ((ParseNode)yyVals[-3+yyTop]), splat);
    yyVal = support.arg_append((ParseNode) yyVal, kwrest);
    yyVal = support.arg_blk_pass((ParseNode) yyVal, block);
    return yyVal;
};
states[277] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(null);
    /* NOTE(norswap, 06 Nov 2020): location (0) arg is unused*/
    SplatParseNode splat = support.newSplatNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_REST_VAR));
    HashParseNode kwrest = new HashParseNode(position, support.createKeyValue(null, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_KWREST_VAR)));
    kwrest.setKeywordArguments(true);
    BlockPassParseNode block = new BlockPassParseNode(position, new LocalVarParseNode(position, 0, ParserSupport.FORWARD_ARGS_BLOCK_VAR));
    yyVal = support.arg_append(splat, kwrest);
    yyVal = support.arg_blk_pass((ParseNode) yyVal, block);
    return yyVal;
};
states[282] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[283] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ((HashParseNode)yyVals[-1+yyTop]).setKeywordArguments(true);
    yyVal = support.arg_append(((ParseNode)yyVals[-3+yyTop]), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[284] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ((HashParseNode)yyVals[-1+yyTop]).setKeywordArguments(true);
    yyVal = support.newArrayNode(((HashParseNode)yyVals[-1+yyTop]).getPosition(), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[285] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.newArrayNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[286] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.arg_blk_pass(((ParseNode)yyVals[-1+yyTop]), ((BlockPassParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[287] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ((HashParseNode)yyVals[-1+yyTop]).setKeywordArguments(true);
    yyVal = support.newArrayNode(((HashParseNode)yyVals[-1+yyTop]).getPosition(), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    yyVal = support.arg_blk_pass((ParseNode)yyVal, ((BlockPassParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[288] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ((HashParseNode)yyVals[-1+yyTop]).setKeywordArguments(true);
    yyVal = support.arg_append(((ParseNode)yyVals[-3+yyTop]), support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop])));
    yyVal = support.arg_blk_pass((ParseNode)yyVal, ((BlockPassParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[289] = (support, lexer, yyVal, yyVals, yyTop) -> yyVal;
states[290] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getCmdArgumentState().getStack();
    lexer.getCmdArgumentState().begin();
    return yyVal;
};
states[291] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getCmdArgumentState().reset(((Long)yyVals[-1+yyTop]).longValue());
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[292] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BlockPassParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[293] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (!support.local_id(ParserSupport.FORWARD_ARGS_BLOCK_VAR)) {
        support.yyerror("no anonymous block parameter");
    }

    yyVal = new BlockPassParseNode(lexer.tokline, new LocalVarParseNode(support.getPosition(null), 0, ParserSupport.FORWARD_ARGS_BLOCK_VAR));
    return yyVal;
};
states[294] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((BlockPassParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[296] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[297] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newSplatNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[298] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node = support.splat_array(((ParseNode)yyVals[-2+yyTop]));

    if (node != null) {
        yyVal = support.list_append(node, ((ParseNode)yyVals[0+yyTop]));
    } else {
        yyVal = support.arg_append(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    }
    return yyVal;
};
states[299] = (support, lexer, yyVal, yyVals, yyTop) -> {
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
states[300] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[301] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[302] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node = support.splat_array(((ParseNode)yyVals[-2+yyTop]));

    if (node != null) {
        yyVal = support.list_append(node, ((ParseNode)yyVals[0+yyTop]));
    } else {
        yyVal = support.arg_append(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    }
    return yyVal;
};
states[303] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node = null;

    if (((ParseNode)yyVals[0+yyTop]) instanceof ArrayParseNode &&
        (node = support.splat_array(((ParseNode)yyVals[-3+yyTop]))) != null) {
        yyVal = support.list_concat(node, ((ParseNode)yyVals[0+yyTop]));
    } else {
        yyVal = support.arg_concat(((ParseNode)yyVals[-3+yyTop]).getPosition(), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    }
    return yyVal;
};
states[304] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newSplatNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[311] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[0+yyTop]); /* FIXME: Why complaining without $$ = $1;*/
    return yyVal;
};
states[312] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[0+yyTop]); /* FIXME: Why complaining without $$ = $1;*/
    return yyVal;
};
states[315] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_fcall(((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[316] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getCmdArgumentState().getStack();
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[317] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getCmdArgumentState().reset(((Long)yyVals[-2+yyTop]).longValue());
    yyVal = new BeginParseNode(((SourceIndexLength)yyVals[-3+yyTop]), support.makeNullNil(((ParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[318] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_ENDARG);
    return yyVal;
};
states[319] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null; /*FIXME: Should be implicit nil?*/
    return yyVal;
};
states[320] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getCmdArgumentState().getStack();
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[321] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_ENDARG); 
    return yyVal;
};
states[322] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getCmdArgumentState().reset(((Long)yyVals[-3+yyTop]).longValue());
    yyVal = ((ParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[323] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-1+yyTop]) != null) {
        /* compstmt position includes both parens around it*/
        ((ParseNode)yyVals[-1+yyTop]).extendPosition(((SourceIndexLength)yyVals[-2+yyTop]));
        yyVal = ((ParseNode)yyVals[-1+yyTop]);
    } else {
        yyVal = new NilParseNode(((SourceIndexLength)yyVals[-2+yyTop]));
    }
    return yyVal;
};
states[324] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon2(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[325] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon3(lexer.tokline, ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[326] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(((ParseNode)yyVals[-1+yyTop]));
    if (((ParseNode)yyVals[-1+yyTop]) == null) {
        yyVal = new ZArrayParseNode(position); /* zero length array */
    } else {
        yyVal = ((ParseNode)yyVals[-1+yyTop]);
    }
    return yyVal;
};
states[327] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((HashParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[328] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ReturnParseNode(((SourceIndexLength)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
    return yyVal;
};
states[329] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_yield(((SourceIndexLength)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[330] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new YieldParseNode(((SourceIndexLength)yyVals[-2+yyTop]), null);
    return yyVal;
};
states[331] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new YieldParseNode(((SourceIndexLength)yyVals[0+yyTop]), null);
    return yyVal;
};
states[332] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_defined(((SourceIndexLength)yyVals[-4+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[333] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode)yyVals[-1+yyTop])), TStringConstants.BANG);
    return yyVal;
};
states[334] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.getOperatorCallNode(NilImplicitParseNode.NIL, TStringConstants.BANG);
    return yyVal;
};
states[335] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.frobnicate_fcall_args(((FCallParseNode)yyVals[-1+yyTop]), null, ((IterParseNode)yyVals[0+yyTop]));
    yyVal = ((FCallParseNode)yyVals[-1+yyTop]);                    
    return yyVal;
};
states[337] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-1+yyTop]) != null && 
          ((BlockAcceptingParseNode)yyVals[-1+yyTop]).getIterNode() instanceof BlockPassParseNode) {
          lexer.compile_error(PID.BLOCK_ARG_AND_BLOCK_GIVEN, "Both block arg and actual block given.");
    }
    yyVal = ((BlockAcceptingParseNode)yyVals[-1+yyTop]).setIterNode(((IterParseNode)yyVals[0+yyTop]));
    ((ParseNode)yyVal).extendPosition(((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[338] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((LambdaParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[339] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(((SourceIndexLength)yyVals[-5+yyTop]), support.getConditionNode(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[340] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(((SourceIndexLength)yyVals[-5+yyTop]), support.getConditionNode(((ParseNode)yyVals[-4+yyTop])), ((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[-2+yyTop]));
    return yyVal;
};
states[341] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().begin();
    return yyVal;
};
states[342] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().end();
    return yyVal;
};
states[343] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));
    yyVal = new WhileParseNode(((SourceIndexLength)yyVals[-6+yyTop]), support.getConditionNode(((ParseNode)yyVals[-4+yyTop])), body);
    return yyVal;
};
states[344] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().begin();
    return yyVal;
};
states[345] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().end();
    return yyVal;
};
states[346] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));
    yyVal = new UntilParseNode(((SourceIndexLength)yyVals[-6+yyTop]), support.getConditionNode(((ParseNode)yyVals[-4+yyTop])), body);
    return yyVal;
};
states[347] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newCaseNode(((SourceIndexLength)yyVals[-4+yyTop]), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[348] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newCaseNode(((SourceIndexLength)yyVals[-3+yyTop]), null, ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[349] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newCaseInNode(((SourceIndexLength)yyVals[-4+yyTop]), ((ParseNode)yyVals[-3+yyTop]), ((InParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[350] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().begin();
    return yyVal;
};
states[351] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getConditionState().end();
    return yyVal;
};
states[352] = (support, lexer, yyVal, yyVals, yyTop) -> {
    /* ENEBO: Lots of optz in 1.9 parser here*/
  yyVal = new ForParseNode(((SourceIndexLength)yyVals[-8+yyTop]), ((ParseNode)yyVals[-7+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[-4+yyTop]), support.getCurrentScope());
    return yyVal;
};
states[353] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) {
        support.yyerror("class definition in method body");
    }
    support.pushLocalScope();
    yyVal = support.isInClass(); /* MRI reuses $1 but we use the value for position.*/
    support.setIsInClass(true);
    return yyVal;
};
states[354] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));

    yyVal = new ClassParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-5+yyTop]), lexer.getPosition()), ((Colon3ParseNode)yyVals[-4+yyTop]), support.getCurrentScope(), body, ((ParseNode)yyVals[-3+yyTop]));
    support.popCurrentScope();
    support.setIsInClass(((Boolean)yyVals[-2+yyTop]).booleanValue());
    return yyVal;
};
states[355] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = (support.isInClass() ? 2 : 0) | (support.isInDef() ? 1 : 0);
    support.setInDef(false);
    support.setIsInClass(false);
    support.pushLocalScope();
    return yyVal;
};
states[356] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));

    yyVal = new SClassParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-6+yyTop]), lexer.getPosition()), ((ParseNode)yyVals[-4+yyTop]), support.getCurrentScope(), body);
    support.popCurrentScope();
    support.setInDef(((((Integer)yyVals[-3+yyTop]).intValue()) & 1) != 0);
    support.setIsInClass(((((Integer)yyVals[-3+yyTop]).intValue()) & 2) != 0);
    return yyVal;
};
states[357] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) { 
        support.yyerror("module definition in method body");
    }
    yyVal = support.isInClass();
    support.setIsInClass(true);
    support.pushLocalScope();
    return yyVal;
};
states[358] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));

    yyVal = new ModuleParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-4+yyTop]), lexer.getPosition()), ((Colon3ParseNode)yyVals[-3+yyTop]), support.getCurrentScope(), body);
    support.popCurrentScope();
    support.setIsInClass(((Boolean)yyVals[-2+yyTop]).booleanValue());
    return yyVal;
};
states[359] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushLocalScope();
    yyVal = lexer.getCurrentArg();
    lexer.setCurrentArg(null);
    support.checkMethodName(((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[360] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.isInDef();
    support.setInDef(true);
    return yyVal;
};
states[361] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = support.makeNullNil(((ParseNode)yyVals[-1+yyTop]));

    yyVal = new DefnParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-6+yyTop]), ((SourceIndexLength)yyVals[0+yyTop])), support.symbolID(((TruffleString)yyVals[-5+yyTop])), (ArgsParseNode) yyVals[-2+yyTop], support.getCurrentScope(), body);
    support.popCurrentScope();
    support.setInDef(((Boolean)yyVals[-3+yyTop]).booleanValue());
    lexer.setCurrentArg(((TruffleString)yyVals[-4+yyTop]));
    return yyVal;
};
states[362] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_FNAME); 
    yyVal = support.isInDef();
    support.setInDef(true);
    return yyVal;
};
states[363] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushLocalScope();
    lexer.setState(EXPR_ENDFN|EXPR_LABEL); /* force for args */
    yyVal = lexer.getCurrentArg();
    lexer.setCurrentArg(null);
    support.checkMethodName(((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[364] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode body = ((ParseNode)yyVals[-1+yyTop]);
    if (body == null) body = NilImplicitParseNode.NIL;

    yyVal = new DefsParseNode(support.extendedUntil(((SourceIndexLength)yyVals[-8+yyTop]), ((SourceIndexLength)yyVals[0+yyTop])), ((ParseNode)yyVals[-7+yyTop]), support.symbolID(((TruffleString)yyVals[-4+yyTop])), (ArgsParseNode) yyVals[-2+yyTop], support.getCurrentScope(), body);
    support.popCurrentScope();
    support.setInDef(((Boolean)yyVals[-5+yyTop]).booleanValue());
    lexer.setCurrentArg(((TruffleString)yyVals[-3+yyTop]));
    return yyVal;
};
states[365] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BreakParseNode(((SourceIndexLength)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
    return yyVal;
};
states[366] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new NextParseNode(((SourceIndexLength)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
    return yyVal;
};
states[367] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new RedoParseNode(((SourceIndexLength)yyVals[0+yyTop]));
    return yyVal;
};
states[368] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new RetryParseNode(((SourceIndexLength)yyVals[0+yyTop]));
    return yyVal;
};
states[369] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    if (yyVal == null) yyVal = NilImplicitParseNode.NIL;
    return yyVal;
};
states[370] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((SourceIndexLength)yyVals[0+yyTop]);
    return yyVal;
};
states[371] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((SourceIndexLength)yyVals[0+yyTop]);
    return yyVal;
};
states[372] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInClass() && !support.isInDef() && !support.getCurrentScope().isBlockScope()) {
        lexer.compile_error(PID.TOP_LEVEL_RETURN, "Invalid return in class/module body");
    }
    yyVal = ((SourceIndexLength)yyVals[0+yyTop]);
    return yyVal;
};
states[379] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(((SourceIndexLength)yyVals[-4+yyTop]), support.getConditionNode(((ParseNode)yyVals[-3+yyTop])), ((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[381] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[383] = (support, lexer, yyVal, yyVals, yyTop) -> yyVal;
states[384] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.assignableInCurr(((TruffleString)yyVals[0+yyTop]), NilImplicitParseNode.NIL);
    return yyVal;
};
states[385] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[386] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[387] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[388] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[0+yyTop]).getPosition(), ((ListParseNode)yyVals[0+yyTop]), null, null);
    return yyVal;
};
states[389] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), support.assignableInCurr(((TruffleString)yyVals[0+yyTop]), null), null);
    return yyVal;
};
states[390] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), support.assignableInCurr(((TruffleString)yyVals[-2+yyTop]), null), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[391] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-2+yyTop]).getPosition(), ((ListParseNode)yyVals[-2+yyTop]), new StarParseNode(lexer.getPosition()), null);
    return yyVal;
};
states[392] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(((ListParseNode)yyVals[-4+yyTop]).getPosition(), ((ListParseNode)yyVals[-4+yyTop]), new StarParseNode(lexer.getPosition()), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[393] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, support.assignableInCurr(((TruffleString)yyVals[0+yyTop]), null), null);
    return yyVal;
};
states[394] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, support.assignableInCurr(((TruffleString)yyVals[-2+yyTop]), null), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[395] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), null);
    return yyVal;
};
states[396] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new MultipleAsgnParseNode(support.getPosition(((ListParseNode)yyVals[0+yyTop])), null, null, ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[397] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[398] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), (TruffleString) null, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[399] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, ((TruffleString)yyVals[-1+yyTop]), ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[400] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, RubyLexer.Keyword.NIL.bytes, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[401] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((BlockArgParseNode)yyVals[0+yyTop]).getPosition(), null, (TruffleString) null, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[402] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsTailHolder)yyVals[0+yyTop]);
    return yyVal;
};
states[403] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, (TruffleString) null, null);
    return yyVal;
};
states[404] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), ((ListParseNode)yyVals[-3+yyTop]), ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[405] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-7+yyTop]).getPosition(), ((ListParseNode)yyVals[-7+yyTop]), ((ListParseNode)yyVals[-5+yyTop]), ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[406] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[407] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), ((ListParseNode)yyVals[-3+yyTop]), null, ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[408] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), null, ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[409] = (support, lexer, yyVal, yyVals, yyTop) -> {
    RestArgParseNode rest = new UnnamedRestArgParseNode(((ListParseNode)yyVals[-1+yyTop]).getPosition(), Layouts.TEMP_PREFIX + "anonymous_rest", support.getCurrentScope().addVariable("*"), false);
    yyVal = support.new_args(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), null, rest, null, (ArgsTailHolder) null);
    return yyVal;
};
states[410] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), null, ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[411] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), null, null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[412] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(support.getPosition(((ListParseNode)yyVals[-3+yyTop])), null, ((ListParseNode)yyVals[-3+yyTop]), ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[413] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(support.getPosition(((ListParseNode)yyVals[-5+yyTop])), null, ((ListParseNode)yyVals[-5+yyTop]), ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[414] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(support.getPosition(((ListParseNode)yyVals[-1+yyTop])), null, ((ListParseNode)yyVals[-1+yyTop]), null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[415] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-3+yyTop]), null, ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[416] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((RestArgParseNode)yyVals[-1+yyTop]).getPosition(), null, null, ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[417] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((RestArgParseNode)yyVals[-3+yyTop]).getPosition(), null, null, ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[418] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ArgsTailHolder)yyVals[0+yyTop]).getPosition(), null, null, null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[419] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, null);
    return yyVal;
};
states[420] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.enterBlockParameters();
    return yyVal;
};
states[421] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.commandStart = true;
    yyVal = ((ArgsParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[422] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
    return yyVal;
};
states[423] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
    return yyVal;
};
states[424] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = ((ArgsParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[425] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[426] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[427] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[428] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[429] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.new_bv(((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[430] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[431] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushBlockScope();
    yyVal = lexer.getLeftParenBegin();
    lexer.setLeftParenBegin(lexer.incrementParenNest());
    return yyVal;
};
states[432] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = Long.valueOf(lexer.getCmdArgumentState().getStack());
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[433] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.getCmdArgumentState().reset(((Long)yyVals[-1+yyTop]).longValue());
    lexer.getCmdArgumentState().restart();
    yyVal = new LambdaParseNode(((ArgsParseNode)yyVals[-2+yyTop]).getPosition(), ((ArgsParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]), support.getCurrentScope());
    lexer.setLeftParenBegin(((Integer)yyVals[-3+yyTop]));
    support.popCurrentScope();
    return yyVal;
};
states[434] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.enterBlockParameters();
    return yyVal;
};
states[435] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[436] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.enterBlockParameters();
    return yyVal;
};
states[437] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[438] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, null);
    return yyVal;
};
states[439] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[440] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[441] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((IterParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[442] = (support, lexer, yyVal, yyVals, yyTop) -> {
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
    ((ParseNode)yyVal).extendPosition(((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[443] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[444] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-3+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((IterParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[445] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-4+yyTop]), ((TruffleString)yyVals[-3+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((IterParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[446] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.frobnicate_fcall_args(((FCallParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    yyVal = ((FCallParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[447] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((TruffleString)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[448] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[449] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop]), null, null);
    return yyVal;
};
states[450] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[-1+yyTop]), TStringConstants.CALL, ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[451] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_call(((ParseNode)yyVals[-2+yyTop]), TStringConstants.CALL, ((ParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[452] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_super(((SourceIndexLength)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[453] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ZSuperParseNode(((SourceIndexLength)yyVals[0+yyTop]));
    return yyVal;
};
states[454] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-3+yyTop]) instanceof SelfParseNode) {
        yyVal = support.new_fcall(TStringConstants.LBRACKET_RBRACKET);
        support.frobnicate_fcall_args(((FCallParseNode)yyVal), ((ParseNode)yyVals[-1+yyTop]), null);
    } else {
        yyVal = support.new_call(((ParseNode)yyVals[-3+yyTop]), TStringConstants.LBRACKET_RBRACKET, ((ParseNode)yyVals[-1+yyTop]), null);
    }
    return yyVal;
};
states[455] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((IterParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[456] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((IterParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[457] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getPosition();
    return yyVal;
};
states[458] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushBlockScope();
    yyVal = Long.valueOf(lexer.getCmdArgumentState().getStack()) >> 1;
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
    yyVal = lexer.getPosition();
    return yyVal;
};
states[461] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pushBlockScope();
    yyVal = Long.valueOf(lexer.getCmdArgumentState().getStack());
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[462] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IterParseNode(((SourceIndexLength)yyVals[-3+yyTop]), ((ArgsParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]), support.getCurrentScope());
     support.popCurrentScope();
    lexer.getCmdArgumentState().reset(((Long)yyVals[-2+yyTop]).longValue());
    return yyVal;
};
states[463] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newWhenNode(((SourceIndexLength)yyVals[-4+yyTop]), ((ParseNode)yyVals[-3+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[466] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_BEG|EXPR_LABEL);
    lexer.commandStart = false;
    /* Lexcontext object is not used in favour of lexer.inKwarg*/
    /* LexContext ctxt = (LexContext) lexer.getLexContext();*/
    yyVals[0+yyTop] = lexer.inKwarg;
    lexer.inKwarg = true;
    yyVal = support.push_pvtbl();
    return yyVal;
};
states[467] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.push_pktbl(); /* after in*/
    return yyVal;
};
states[468] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pop_pktbl(((Set)yyVals[-2+yyTop]));
    support.pop_pvtbl(((Set)yyVals[-3+yyTop]));
    lexer.inKwarg = ((Boolean)yyVals[-4+yyTop]);
    return yyVal;
};
states[469] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newInNode(support.getPosition(yyVals[-7+yyTop]), ((ParseNode)yyVals[-4+yyTop]), ((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[471] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((InParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[473] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.getConditionNode(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[-2+yyTop]), null);
    support.fixpos(((ParseNode)yyVal), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[474] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new IfParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.getConditionNode(((ParseNode)yyVals[0+yyTop])), null, ((ParseNode)yyVals[-2+yyTop]));
    support.fixpos(((ParseNode)yyVal), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[476] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern(support.getPosition(((ParseNode)yyVals[-1+yyTop])), null, ((ParseNode)yyVals[-1+yyTop]),
                                   support.new_array_pattern_tail(support.getPosition(((ParseNode)yyVals[-1+yyTop])), null, true, null, null));
    return yyVal;
};
states[477] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern(support.getPosition(((ParseNode)yyVals[-2+yyTop])), null, ((ParseNode)yyVals[-2+yyTop]), ((ArrayPatternParseNode)yyVals[0+yyTop]));
    /* the following line is a no-op. May or many not require an impl*/
    support.nd_set_first_loc(((ParseNode)yyVal), support.getPosition(((ParseNode)yyVals[-2+yyTop])));
    return yyVal;
};
states[478] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_find_pattern(null, ((FindPatternParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[479] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern(support.getPosition(((ArrayPatternParseNode)yyVals[0+yyTop])), null, null, ((ArrayPatternParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[480] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_hash_pattern(null, ((HashPatternParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[482] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new HashParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), new ParseNodeTuple(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[484] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newOrNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[486] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.push_pktbl();
    return yyVal;
};
states[487] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.push_pktbl();
    return yyVal;
};
states[490] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pop_pktbl(((Set)yyVals[-2+yyTop]));
    yyVal = support.new_array_pattern(support.getPosition(((ParseNode)yyVals[-3+yyTop])), ((ParseNode)yyVals[-3+yyTop]), null, ((ArrayPatternParseNode)yyVals[-1+yyTop]));
    support.nd_set_first_loc(((ParseNode)yyVal), support.getPosition(((ParseNode)yyVals[-3+yyTop])));
    return yyVal;
};
states[491] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pop_pktbl(((Set)yyVals[-2+yyTop]));
    yyVal = support.new_find_pattern(((ParseNode)yyVals[-3+yyTop]), ((FindPatternParseNode)yyVals[-1+yyTop]));
    support.nd_set_first_loc(((ParseNode)yyVal), support.getPosition(((ParseNode)yyVals[-3+yyTop])));
    return yyVal;
};
states[492] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pop_pktbl(((Set)yyVals[-2+yyTop]));
    yyVal = support.new_hash_pattern(((ParseNode)yyVals[-3+yyTop]), ((HashPatternParseNode)yyVals[-1+yyTop]));
    support.nd_set_first_loc(((ParseNode)yyVal), support.getPosition(((ParseNode)yyVals[-3+yyTop])));
    return yyVal;
};
states[493] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), null,
                                   support.new_array_pattern_tail(support.getPosition(((ParseNode)yyVals[-2+yyTop])), null, false, null, null));
    return yyVal;
};
states[494] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pop_pktbl(((Set)yyVals[-2+yyTop]));
    yyVal = support.new_array_pattern(support.getPosition(((ParseNode)yyVals[-3+yyTop])), ((ParseNode)yyVals[-3+yyTop]), null, ((ArrayPatternParseNode)yyVals[-1+yyTop]));
    support.nd_set_first_loc(((ParseNode)yyVal), support.getPosition(((ParseNode)yyVals[-3+yyTop])));
    return yyVal;
};
states[495] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pop_pktbl(((Set)yyVals[-2+yyTop]));
    yyVal = support.new_find_pattern(((ParseNode)yyVals[-3+yyTop]), ((FindPatternParseNode)yyVals[-1+yyTop]));
    support.nd_set_first_loc(((ParseNode)yyVal), support.getPosition(((ParseNode)yyVals[-3+yyTop])));
    return yyVal;
};
states[496] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pop_pktbl(((Set)yyVals[-2+yyTop]));
    yyVal = support.new_hash_pattern(((ParseNode)yyVals[-3+yyTop]), ((HashPatternParseNode)yyVals[-1+yyTop]));
    support.nd_set_first_loc(((ParseNode)yyVal), support.getPosition(((ParseNode)yyVals[-3+yyTop])));
    return yyVal;
};
states[497] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern(support.getPosition(((ParseNode)yyVals[-2+yyTop])), ((ParseNode)yyVals[-2+yyTop]), null,
            support.new_array_pattern_tail(support.getPosition(((ParseNode)yyVals[-2+yyTop])), null, false, null, null));
    return yyVal;
};
states[498] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern(support.getPosition(((TruffleString)yyVals[-2+yyTop])), null, null, ((ArrayPatternParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[499] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_find_pattern(null, ((FindPatternParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[500] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern(support.getPosition(((TruffleString)yyVals[-1+yyTop])), null, null,
            support.new_array_pattern_tail(support.getPosition(((TruffleString)yyVals[-1+yyTop])), null, false, null, null));
    return yyVal;
};
states[501] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.push_pktbl();
    ((SourceIndexLength)yyVals[0+yyTop]) = lexer.inKwarg;
    lexer.inKwarg = false;
    return yyVal;
};
states[502] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pop_pktbl(((Set)yyVals[-2+yyTop]));
    lexer.inKwarg = ((Boolean)yyVals[-3+yyTop]);
    yyVal = support.new_hash_pattern(null, ((HashPatternParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[503] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_hash_pattern(null, support.new_hash_pattern_tail(support.getPosition(((SourceIndexLength)yyVals[-1+yyTop])), null, null));
    return yyVal;
};
states[504] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.push_pktbl();
    return yyVal;
};
states[505] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.pop_pktbl(((Set)yyVals[-2+yyTop]));
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[506] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ListParseNode preArgs = support.newArrayNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    yyVal = support.new_array_pattern_tail(support.getPosition(((ParseNode)yyVals[0+yyTop])), preArgs, false, null, null);
    return yyVal;
};
states[507] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern_tail(support.getPosition(((ListParseNode)yyVals[0+yyTop])), ((ListParseNode)yyVals[0+yyTop]), true, null, null);
    return yyVal;
};
states[508] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern_tail(support.getPosition(((ListParseNode)yyVals[-1+yyTop])), support.list_concat(((ListParseNode)yyVals[-1+yyTop]), ((ListParseNode)yyVals[0+yyTop])), false, null, null);
    return yyVal;
};
states[509] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern_tail(support.getPosition(((ListParseNode)yyVals[-2+yyTop])), ((ListParseNode)yyVals[-2+yyTop]), true, ((TruffleString)yyVals[0+yyTop]), null);
    return yyVal;
};
states[510] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern_tail(support.getPosition(((ListParseNode)yyVals[-4+yyTop])), ((ListParseNode)yyVals[-4+yyTop]), true, ((TruffleString)yyVals[-2+yyTop]), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[511] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern_tail(support.getPosition(((ListParseNode)yyVals[-1+yyTop])), ((ListParseNode)yyVals[-1+yyTop]), true, null, null);
    return yyVal;
};
states[512] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern_tail(support.getPosition(((ListParseNode)yyVals[-3+yyTop])), ((ListParseNode)yyVals[-3+yyTop]), true, null, ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[513] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArrayPatternParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[514] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[515] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.list_concat(((ListParseNode)yyVals[-2+yyTop]), ((ListParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[516] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern_tail(support.getPosition(((Rope)yyVals[0+yyTop])), null, true, ((Rope)yyVals[0+yyTop]), null);
    return yyVal;
};
states[517] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern_tail(support.getPosition(((Rope)yyVals[-2+yyTop])), null, true, ((Rope)yyVals[-2+yyTop]), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[518] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_find_pattern_tail(support.getPosition(((Rope)yyVals[-4+yyTop])), ((Rope)yyVals[-4+yyTop]), ((ListParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[0+yyTop]));
    support.warn(support.getPosition(((Rope)yyVals[-4+yyTop])), "Find pattern is experimental, and the behavior may change in future versions of Ruby!");
    return yyVal;
};
states[519] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[520] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[522] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.list_concat(((ListParseNode)yyVals[-2+yyTop]), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[523] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[524] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_hash_pattern_tail(support.getPosition(((HashParseNode)yyVals[-2+yyTop])), ((HashParseNode)yyVals[-2+yyTop]), ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[525] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_hash_pattern_tail(support.getPosition(((HashParseNode)yyVals[0+yyTop])), ((HashParseNode)yyVals[0+yyTop]), null);
    return yyVal;
};
states[526] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_hash_pattern_tail(support.getPosition(((HashParseNode)yyVals[-1+yyTop])), ((HashParseNode)yyVals[-1+yyTop]), null);
    return yyVal;
};
states[527] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_hash_pattern_tail(support.getPosition(((Rope)yyVals[0+yyTop])), null, ((Rope)yyVals[0+yyTop]));
    return yyVal;
};
states[528] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new HashParseNode(support.getPosition(((ParseNodeTuple)yyVals[0+yyTop])), ((ParseNodeTuple)yyVals[0+yyTop]));
    return yyVal;
};
states[529] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ((HashParseNode)yyVals[-2+yyTop]).add(((ParseNodeTuple)yyVals[0+yyTop]));
    yyVal = ((HashParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[530] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.error_duplicate_pattern_key(((Rope)yyVals[-1+yyTop]));

    ParseNode label = support.asSymbol(support.getPosition(((Rope)yyVals[-1+yyTop])), ((Rope)yyVals[-1+yyTop]));

    yyVal = new ParseNodeTuple(label, ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[531] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.error_duplicate_pattern_key(((Rope)yyVals[0+yyTop]));
    if (((Rope)yyVals[0+yyTop]) != null && !support.is_local_id(((Rope)yyVals[0+yyTop]))) {
        support.yyerror("key must be valid as local variables");
    }
    support.error_duplicate_pattern_variable(((Rope)yyVals[0+yyTop]));

    ParseNode label = support.asSymbol(support.getPosition(((Rope)yyVals[0+yyTop])), ((Rope)yyVals[0+yyTop]));
    yyVal = new ParseNodeTuple(label, support.assignableLabelOrIdentifier(((Rope)yyVals[0+yyTop]), null));
    return yyVal;
};
states[533] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-1+yyTop]) == null || ((ParseNode)yyVals[-1+yyTop]) instanceof StrParseNode) {
        yyVal = ((StrParseNode)yyVals[-1+yyTop]).getValue();
    } else {
        support.yyerror("symbol literal with interpolation is not allowed");
        yyVal = null;
    }
    return yyVal;
};
states[534] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[535] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[536] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[538] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ParserSupport.KWNOREST;
    return yyVal;
};
states[540] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParserSupport.value_expr(lexer, ((ParseNode)yyVals[-2+yyTop]));
    ParserSupport.value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    boolean isLiteral = ((ParseNode)yyVals[-2+yyTop]) instanceof FixnumParseNode && ((ParseNode)yyVals[0+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.makeNullNil(((ParseNode)yyVals[-2+yyTop])), support.makeNullNil(((ParseNode)yyVals[0+yyTop])), false, isLiteral);
    return yyVal;
};
states[541] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParserSupport.value_expr(lexer, ((ParseNode)yyVals[-2+yyTop]));
    ParserSupport.value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    boolean isLiteral = ((ParseNode)yyVals[-2+yyTop]) instanceof FixnumParseNode && ((ParseNode)yyVals[0+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[-2+yyTop])), support.makeNullNil(((ParseNode)yyVals[-2+yyTop])), support.makeNullNil(((ParseNode)yyVals[0+yyTop])), true, isLiteral);
    return yyVal;
};
states[542] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParserSupport.value_expr(lexer, ((ParseNode)yyVals[-1+yyTop]));
    boolean isLiteral = ((ParseNode)yyVals[-1+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[-1+yyTop])), support.makeNullNil(((ParseNode)yyVals[-1+yyTop])), NilImplicitParseNode.NIL, false, isLiteral);
    return yyVal;
};
states[543] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParserSupport.value_expr(lexer, ((ParseNode)yyVals[-1+yyTop]));
    boolean isLiteral = ((ParseNode)yyVals[-1+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((ParseNode)yyVals[-1+yyTop])), support.makeNullNil(((ParseNode)yyVals[-1+yyTop])), NilImplicitParseNode.NIL, true, isLiteral);
    return yyVal;
};
states[547] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParserSupport.value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    boolean isLiteral = ((ParseNode)yyVals[0+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((TruffleString)yyVals[-1+yyTop])), NilImplicitParseNode.NIL, support.makeNullNil(((ParseNode)yyVals[0+yyTop])), false, isLiteral);
    return yyVal;
};
states[548] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParserSupport.value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    boolean isLiteral = ((ParseNode)yyVals[0+yyTop]) instanceof FixnumParseNode;
    yyVal = new DotParseNode(support.getPosition(((TruffleString)yyVals[-1+yyTop])), NilImplicitParseNode.NIL, support.makeNullNil(((ParseNode)yyVals[0+yyTop])), true, isLiteral);
    return yyVal;
};
states[553] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[554] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[555] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[556] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[557] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new NilParseNode(lexer.tokline);
    return yyVal;
};
states[558] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new SelfParseNode(lexer.tokline);
    return yyVal;
};
states[559] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new TrueParseNode(lexer.tokline);
    return yyVal;
};
states[560] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new FalseParseNode(lexer.tokline);
    return yyVal;
};
states[561] = (support, lexer, yyVal, yyVals, yyTop) -> {
    /* TODO: make a helper for this since it is used twice now*/
    Encoding encoding = support.getConfiguration().getContext() == null ? UTF8Encoding.INSTANCE : support.getConfiguration().getContext().getEncodingManager().getLocaleEncoding().jcoding;
    yyVal = new FileParseNode(lexer.tokline, StringOperations.encodeRope(lexer.getFile(), encoding, CR_UNKNOWN));
    return yyVal;
};
states[562] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new FixnumParseNode(lexer.tokline, lexer.getRubySourceLine());
    return yyVal;
};
states[563] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new EncodingParseNode(lexer.tokline, lexer.getEncoding());
    return yyVal;
};
states[564] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((LambdaParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[565] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.error_duplicate_pattern_variable(((TruffleString)yyVals[0+yyTop]));
    yyVal = support.assignableInCurr(((TruffleString)yyVals[0+yyTop]), null);
    return yyVal;
};
states[566] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode n = support.gettable(((TruffleString)yyVals[0+yyTop]));
    if (!(n instanceof LocalVarParseNode || n instanceof DVarParseNode)) {
        support.compile_error("" + ((TruffleString)yyVals[0+yyTop]) + ": no such local variable");
    }
    yyVal = n;
    return yyVal;
};
states[567] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.gettable(((Rope)yyVals[0+yyTop]));
    if (yyVal == null) yyVal = new BeginParseNode(lexer.tokline, NilImplicitParseNode.NIL);
    return yyVal;
};
states[568] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BeginParseNode(lexer.tokline, ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[569] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon3(lexer.tokline, ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[570] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_colon2(lexer.tokline, ((ParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[571] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ConstParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[572] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode node;
    if (((ParseNode)yyVals[-3+yyTop]) != null) {
        node = support.appendToBlock(support.node_assign(((ParseNode)yyVals[-3+yyTop]), new GlobalVarParseNode(((SourceIndexLength)yyVals[-5+yyTop]), support.symbolID(TStringConstants.DOLLAR_BANG))), ((ParseNode)yyVals[-1+yyTop]));
        if (((ParseNode)yyVals[-1+yyTop]) != null) {
            node.extendPosition(((SourceIndexLength)yyVals[-5+yyTop]));
        }
    } else {
        node = ((ParseNode)yyVals[-1+yyTop]);
    }
    ParseNode body = support.makeNullNil(node);
    yyVal = new RescueBodyParseNode(((SourceIndexLength)yyVals[-5+yyTop]), ((ParseNode)yyVals[-4+yyTop]), body, ((RescueBodyParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[573] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null; 
    return yyVal;
};
states[574] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newArrayNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[575] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.splat_array(((ParseNode)yyVals[0+yyTop]));
    if (yyVal == null) yyVal = ((ParseNode)yyVals[0+yyTop]); /* ArgsCat or ArgsPush*/
    return yyVal;
};
states[577] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[579] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[581] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((NumericParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[582] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.asSymbol(lexer.getPosition(), ((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[584] = (support, lexer, yyVal, yyVals, yyTop) -> {
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
states[585] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((StrParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[586] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[587] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[588] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.heredoc_dedent(((ParseNode)yyVals[-1+yyTop]));
    lexer.setHeredocIndent(0);
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[589] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(((ParseNode)yyVals[-1+yyTop]));

    lexer.heredoc_dedent(((ParseNode)yyVals[-1+yyTop]));
    lexer.setHeredocIndent(0);

    if (((ParseNode)yyVals[-1+yyTop]) == null) {
        yyVal = new XStrParseNode(position, null);
    } else if (((ParseNode)yyVals[-1+yyTop]) instanceof StrParseNode) {
        yyVal = new XStrParseNode(position, ((StrParseNode)yyVals[-1+yyTop]));
    } else if (((ParseNode)yyVals[-1+yyTop]) instanceof DStrParseNode) {
        yyVal = new DXStrParseNode(position, ((DStrParseNode)yyVals[-1+yyTop]));
    } else {
        yyVal = new DXStrParseNode(position).add(((ParseNode)yyVals[-1+yyTop]));
    }
    return yyVal;
};
states[590] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.newRegexpNode(support.getPosition(((ParseNode)yyVals[-1+yyTop])), ((ParseNode)yyVals[-1+yyTop]), ((RegexpParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[591] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[592] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition());
    return yyVal;
};
states[593] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[-1+yyTop]) instanceof EvStrParseNode ? new DStrParseNode(((ListParseNode)yyVals[-2+yyTop]).getPosition(), lexer.getEncoding()).add(((ParseNode)yyVals[-1+yyTop])) : ((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[594] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = yyVals[0+yyTop];
    return yyVal;
};
states[595] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[596] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[597] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition());
    return yyVal;
};
states[598] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[-1+yyTop]) instanceof EvStrParseNode ? new DSymbolParseNode(((ListParseNode)yyVals[-2+yyTop]).getPosition()).add(((ParseNode)yyVals[-1+yyTop])) : support.asSymbol(((ListParseNode)yyVals[-2+yyTop]).getPosition(), ((ParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[599] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[600] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[601] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition());
    return yyVal;
};
states[602] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[603] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition());
    return yyVal;
};
states[604] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(support.asSymbol(((ListParseNode)yyVals[-2+yyTop]).getPosition(), ((ParseNode)yyVals[-1+yyTop])));
    return yyVal;
};
states[605] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.createStr(lexer.encoding.tencoding.getEmpty(), lexer.encoding, 0);
    return yyVal;
};
states[606] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[607] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[608] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[609] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[610] = (support, lexer, yyVal, yyVals, yyTop) -> {
    /* FIXME: mri is different here.*/
                    yyVal = support.literal_concat(((ParseNode)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[611] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[612] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getStrTerm();
    lexer.setStrTerm(null);
    lexer.setState(EXPR_BEG);
    return yyVal;
};
states[613] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setStrTerm(((StrTerm)yyVals[-1+yyTop]));
    yyVal = new EvStrParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[614] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getStrTerm();
    lexer.setStrTerm(null);
    lexer.getConditionState().stop();
    return yyVal;
};
states[615] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getCmdArgumentState().getStack();
    lexer.getCmdArgumentState().reset();
    return yyVal;
};
states[616] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getState();
    lexer.setState(EXPR_BEG);
    return yyVal;
};
states[617] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getBraceNest();
    lexer.setBraceNest(0);
    return yyVal;
};
states[618] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.getHeredocIndent();
    lexer.setHeredocIndent(0);
    return yyVal;
};
states[619] = (support, lexer, yyVal, yyVals, yyTop) -> {
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
states[620] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalVarParseNode(lexer.getPosition(), support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[621] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstVarParseNode(lexer.getPosition(), support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[622] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarParseNode(lexer.getPosition(), support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[624] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_END|EXPR_ENDARG);
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[626] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[627] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[628] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[629] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_END|EXPR_ENDARG);

    /* DStrNode: :"some text #{some expression}"*/
    /* StrNode: :"some text"*/
    /* EvStrNode :"#{some expression}"*/
    /* Ruby 1.9 allows empty strings as symbols*/
    if (((ParseNode)yyVals[-1+yyTop]) == null) {
        yyVal = support.asSymbol(lexer.getPosition(), TStringConstants.EMPTY_US_ASCII);
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
states[630] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((NumericParseNode)yyVals[0+yyTop]);  
    return yyVal;
};
states[631] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.negateNumeric(((NumericParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[635] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[636] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((FloatParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[637] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((RationalParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[638] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[639] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.declareIdentifier(((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[640] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstVarParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[641] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalVarParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[642] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ConstParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[643] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])));
    return yyVal;
};
states[644] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new NilParseNode(lexer.tokline);
    return yyVal;
};
states[645] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new SelfParseNode(lexer.tokline);
    return yyVal;
};
states[646] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new TrueParseNode((SourceIndexLength) yyVal);
    return yyVal;
};
states[647] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new FalseParseNode((SourceIndexLength) yyVal);
    return yyVal;
};
states[648] = (support, lexer, yyVal, yyVals, yyTop) -> {
    RubyEncoding encoding = support.getConfiguration().getContext() == null ? Encodings.UTF_8 : support.getConfiguration().getContext().getEncodingManager().getLocaleEncoding();
    yyVal = new FileParseNode(lexer.tokline, TStringUtils.fromJavaString(lexer.getFile(), encoding), encoding);
    return yyVal;
};
states[649] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new FixnumParseNode(lexer.tokline, lexer.tokline.toSourceSection(lexer.getSource()).getStartLine() + lexer.getLineOffset());
    return yyVal;
};
states[650] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new EncodingParseNode(lexer.tokline, lexer.getEncoding());
    return yyVal;
};
states[651] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.assignableLabelOrIdentifier(((TruffleString)yyVals[0+yyTop]), null);
    return yyVal;
};
states[652] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new InstAsgnParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[653] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new GlobalAsgnParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[654] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (support.isInDef()) support.compile_error("dynamic constant assignment");

    yyVal = new ConstDeclParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), null, NilImplicitParseNode.NIL);
    return yyVal;
};
states[655] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ClassVarAsgnParseNode(lexer.tokline, support.symbolID(((TruffleString)yyVals[0+yyTop])), NilImplicitParseNode.NIL);
    return yyVal;
};
states[656] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to nil");
    yyVal = null;
    return yyVal;
};
states[657] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't change the value of self");
    yyVal = null;
    return yyVal;
};
states[658] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to true");
    yyVal = null;
    return yyVal;
};
states[659] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to false");
    yyVal = null;
    return yyVal;
};
states[660] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __FILE__");
    yyVal = null;
    return yyVal;
};
states[661] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __LINE__");
    yyVal = null;
    return yyVal;
};
states[662] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.compile_error("Can't assign to __ENCODING__");
    yyVal = null;
    return yyVal;
};
states[663] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[664] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[665] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_BEG);
    lexer.commandStart = true;
    return yyVal;
};
states[666] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[667] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[668] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsParseNode)yyVals[-1+yyTop]);
    lexer.setState(EXPR_BEG);
    lexer.commandStart = true;
    return yyVal;
};
states[669] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = lexer.inKwarg;
    lexer.inKwarg = true;
    lexer.setState(lexer.getState() | EXPR_LABEL);
    return yyVal;
};
states[670] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.inKwarg = ((Boolean)yyVals[-2+yyTop]);
     yyVal = ((ArgsParseNode)yyVals[-1+yyTop]);
     lexer.setState(EXPR_BEG);
     lexer.commandStart = true;
    return yyVal;
};
states[671] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), ((TruffleString)yyVals[-1+yyTop]), ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[672] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), (TruffleString) null, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[673] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, ((TruffleString)yyVals[-1+yyTop]), ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[674] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, RubyLexer.Keyword.NIL.bytes, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[675] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(((BlockArgParseNode)yyVals[0+yyTop]).getPosition(), null, (TruffleString) null, ((BlockArgParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[676] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsTailHolder)yyVals[0+yyTop]);
    return yyVal;
};
states[677] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args_tail(lexer.getPosition(), null, (TruffleString) null, null);
    return yyVal;
};
states[678] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ArgsParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[679] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
    return yyVal;
};
states[680] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), ((ListParseNode)yyVals[-3+yyTop]), ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[681] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-7+yyTop]).getPosition(), ((ListParseNode)yyVals[-7+yyTop]), ((ListParseNode)yyVals[-5+yyTop]), ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[682] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[683] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), ((ListParseNode)yyVals[-3+yyTop]), null, ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[684] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), ((ListParseNode)yyVals[-3+yyTop]), null, ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[685] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), ((ListParseNode)yyVals[-5+yyTop]), null, ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[686] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-1+yyTop]).getPosition(), ((ListParseNode)yyVals[-1+yyTop]), null, null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[687] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-3+yyTop]), ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[688] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-5+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-5+yyTop]), ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[689] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-1+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-1+yyTop]), null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[690] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ListParseNode)yyVals[-3+yyTop]).getPosition(), null, ((ListParseNode)yyVals[-3+yyTop]), null, ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[691] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((RestArgParseNode)yyVals[-1+yyTop]).getPosition(), null, null, ((RestArgParseNode)yyVals[-1+yyTop]), null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[692] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((RestArgParseNode)yyVals[-3+yyTop]).getPosition(), null, null, ((RestArgParseNode)yyVals[-3+yyTop]), ((ListParseNode)yyVals[-1+yyTop]), ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[693] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_args(((ArgsTailHolder)yyVals[0+yyTop]).getPosition(), null, null, null, null, ((ArgsTailHolder)yyVals[0+yyTop]));
    return yyVal;
};
states[694] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(null);
    RestArgParseNode splat = new RestArgParseNode(position, ParserSupport.FORWARD_ARGS_REST_VAR, 0);
    BlockArgParseNode block = new BlockArgParseNode(position, 1, ParserSupport.FORWARD_ARGS_BLOCK_VAR);
    ArgsTailHolder argsTail = support.new_args_tail(position, null, ParserSupport.FORWARD_ARGS_KWREST_VAR_TSTRING, block);
    yyVal = support.new_args(position, ((ListParseNode)yyVals[-2+yyTop]), null, splat, null, argsTail);
    return yyVal;
};
states[695] = (support, lexer, yyVal, yyVals, yyTop) -> {
    SourceIndexLength position = support.getPosition(null);
    RestArgParseNode splat = new RestArgParseNode(position, ParserSupport.FORWARD_ARGS_REST_VAR, 0);
    BlockArgParseNode block = new BlockArgParseNode(position, 1, ParserSupport.FORWARD_ARGS_BLOCK_VAR);
    ArgsTailHolder argsTail = support.new_args_tail(position, null, ParserSupport.FORWARD_ARGS_KWREST_VAR_TSTRING, block);
    yyVal = support.new_args(position, null, null, splat, null, argsTail);
    return yyVal;
};
states[697] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("formal argument cannot be a constant");
    return yyVal;
};
states[698] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("formal argument cannot be an instance variable");
    return yyVal;
};
states[699] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("formal argument cannot be a global variable");
    return yyVal;
};
states[700] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.yyerror("formal argument cannot be a class variable");
    return yyVal;
};
states[701] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]); /* Not really reached*/
    return yyVal;
};
states[702] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.formal_argument(((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[703] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(((TruffleString)yyVals[0+yyTop]));
    yyVal = support.arg_var(((TruffleString)yyVals[0+yyTop]));
    return yyVal;
};
states[704] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = ((ArgumentParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[705] = (support, lexer, yyVal, yyVals, yyTop) -> {
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
states[706] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(lexer.getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[707] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[0+yyTop]));
    yyVal = ((ListParseNode)yyVals[-2+yyTop]);
    return yyVal;
};
states[708] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.arg_var(support.formal_argument(((TruffleString)yyVals[0+yyTop])));
    lexer.setCurrentArg(((TruffleString)yyVals[0+yyTop]));
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[709] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = support.keyword_arg(((ParseNode)yyVals[0+yyTop]).getPosition(), support.assignableKeyword(((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[710] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = support.keyword_arg(lexer.getPosition(), support.assignableKeyword(((TruffleString)yyVals[0+yyTop]), RequiredKeywordArgumentValueParseNode.INSTANCE));
    return yyVal;
};
states[711] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.keyword_arg(support.getPosition(((ParseNode)yyVals[0+yyTop])), support.assignableKeyword(((TruffleString)yyVals[-1+yyTop]), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[712] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.keyword_arg(lexer.getPosition(), support.assignableKeyword(((TruffleString)yyVals[0+yyTop]), RequiredKeywordArgumentValueParseNode.INSTANCE));
    return yyVal;
};
states[713] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[714] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[715] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new ArrayParseNode(((ParseNode)yyVals[0+yyTop]).getPosition(), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[716] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((ListParseNode)yyVals[-2+yyTop]).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[717] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[718] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[720] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.shadowing_lvar(((TruffleString)yyVals[0+yyTop]));
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[721] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ParserSupport.INTERNAL_ID;
    return yyVal;
};
states[722] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = new OptArgParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), support.assignableLabelOrIdentifier(((ArgumentParseNode)yyVals[-2+yyTop]).getName(), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[723] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setCurrentArg(null);
    yyVal = new OptArgParseNode(support.getPosition(((ParseNode)yyVals[0+yyTop])), support.assignableLabelOrIdentifier(((ArgumentParseNode)yyVals[-2+yyTop]).getName(), ((ParseNode)yyVals[0+yyTop])));
    return yyVal;
};
states[724] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BlockParseNode(((ParseNode)yyVals[0+yyTop]).getPosition()).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[725] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.appendToBlock(((ListParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[726] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BlockParseNode(((ParseNode)yyVals[0+yyTop]).getPosition()).add(((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[727] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.appendToBlock(((ListParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[728] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[729] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[730] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (!support.is_local_id(((TruffleString)yyVals[0+yyTop]))) {
        support.yyerror("rest argument must be local variable");
    }
                    
    yyVal = new RestArgParseNode(support.arg_var(support.shadowing_lvar(((TruffleString)yyVals[0+yyTop]))));
    return yyVal;
};
states[731] = (support, lexer, yyVal, yyVals, yyTop) -> {
  /* FIXME: bytelist_love: somewhat silly to remake the empty bytelist over and over but this type should change (using null vs "" is a strange distinction).*/
  yyVal = new UnnamedRestArgParseNode(lexer.getPosition(), Layouts.TEMP_PREFIX + "unnamed_rest", support.getCurrentScope().addVariable("*"), true);
    return yyVal;
};
states[732] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[733] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[734] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (!support.is_local_id(((TruffleString)yyVals[0+yyTop]))) {
        support.yyerror("block argument must be local variable");
    }
                    
    yyVal = new BlockArgParseNode(support.arg_var(support.shadowing_lvar(((TruffleString)yyVals[0+yyTop]))));
    return yyVal;
};
states[735] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new BlockArgParseNode(support.arg_var(support.shadowing_lvar(ParserSupport.FORWARD_ARGS_BLOCK_VAR_TSTRING)));
    return yyVal;
};
states[736] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((BlockArgParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[737] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[738] = (support, lexer, yyVal, yyVals, yyTop) -> {
    value_expr(lexer, ((ParseNode)yyVals[0+yyTop]));
    yyVal = ((ParseNode)yyVals[0+yyTop]);
    return yyVal;
};
states[739] = (support, lexer, yyVal, yyVals, yyTop) -> {
    lexer.setState(EXPR_BEG);
    return yyVal;
};
states[740] = (support, lexer, yyVal, yyVals, yyTop) -> {
    if (((ParseNode)yyVals[-1+yyTop]) == null) {
        support.yyerror("can't define single method for ().");
    } else if (((ParseNode)yyVals[-1+yyTop]) instanceof ILiteralNode) {
        support.yyerror("can't define single method for literals.");
    }
    value_expr(lexer, ((ParseNode)yyVals[-1+yyTop]));
    yyVal = ((ParseNode)yyVals[-1+yyTop]);
    return yyVal;
};
states[741] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new HashParseNode(lexer.getPosition());
    return yyVal;
};
states[742] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.remove_duplicate_keys(((HashParseNode)yyVals[-1+yyTop]));
    return yyVal;
};
states[743] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = new HashParseNode(lexer.getPosition(), ((ParseNodeTuple)yyVals[0+yyTop]));
    return yyVal;
};
states[744] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((HashParseNode)yyVals[-2+yyTop]).add(((ParseNodeTuple)yyVals[0+yyTop]));
    return yyVal;
};
states[745] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.createKeyValue(((ParseNode)yyVals[-2+yyTop]), ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[746] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode label = support.asSymbol(support.getPosition(((ParseNode)yyVals[0+yyTop])), ((TruffleString)yyVals[-1+yyTop]));
    yyVal = support.createKeyValue(label, ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[747] = (support, lexer, yyVal, yyVals, yyTop) -> {
    ParseNode val = support.declareIdentifier(((TruffleString)yyVals[0+yyTop]));
    ParseNode label = support.asSymbol(support.getPosition(null), ((TruffleString)yyVals[0+yyTop]));
    yyVal = support.createKeyValue(label, val);
    return yyVal;
};
states[748] = (support, lexer, yyVal, yyVals, yyTop) -> {
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
states[749] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.createKeyValue(null, ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[750] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[751] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[752] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[753] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[754] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[755] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[756] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[757] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[758] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[759] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[760] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[761] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[762] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[763] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[765] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[770] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[771] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = ((TruffleString)yyVals[0+yyTop]);
    return yyVal;
};
states[772] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = RopeConstants.RCURLY;
    return yyVal;
};
states[780] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
states[781] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = null;
    return yyVal;
};
}
// line 3248 "RubyParser.y"

    /** The parse method use an lexer stream and parse it to an AST node 
     * structure
     */
    public RubyParserResult parse(ParserConfiguration configuration) {
        support.reset();
        support.setConfiguration(configuration);
        support.setResult(new RubyParserResult());
        
        yyparse(lexer, new org.truffleruby.parser.parser.YYDebug());
        
        return support.getResult();
    }
}
// CheckStyle: stop generated
// @formatter:on
// line 12155 "-"
