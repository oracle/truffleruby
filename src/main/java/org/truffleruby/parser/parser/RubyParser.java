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

import com.oracle.truffle.api.strings.TruffleString;

import java.util.Set;
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
// line 134 "-"
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
     3,     1,     5,     4,     3,     2,     1,     2,     1,     6,
     6,     0,     0,     7,     0,     0,     7,     5,     4,     5,
     0,     0,     9,     0,     6,     0,     7,     0,     5,     0,
     0,     7,     0,     0,     9,     1,     1,     1,     1,     1,
     1,     1,     1,     1,     1,     2,     1,     1,     1,     5,
     1,     2,     1,     1,     1,     3,     1,     3,     1,     4,
     6,     3,     5,     2,     4,     1,     3,     4,     2,     2,
     2,     1,     2,     0,     6,     8,     4,     6,     4,     2,
     6,     2,     4,     6,     2,     4,     2,     4,     1,     1,
     0,     2,     3,     1,     4,     1,     4,     1,     3,     1,
     1,     0,     0,     5,     0,     5,     0,     2,     0,     3,
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
   311,   312,     0,     0,    72,     0,   338,     2,     0,     5,
     0,     0,     0,     0,     7,   187,   198,   188,   211,   184,
   204,   194,   193,   214,   215,   209,   192,   191,   186,   212,
   216,   217,   196,   185,   199,   203,   205,   190,   206,   213,
   208,     0,     0,     0,     0,   183,   202,   201,   218,   182,
   189,   180,   181,     0,     0,     0,     0,   137,   641,   640,
     0,   643,   172,   173,   169,   150,   151,   152,   159,   156,
   158,   153,   154,   174,   175,   160,   161,   739,   166,   165,
   149,   171,   168,   167,   163,   164,   157,   155,   147,   170,
   148,   176,   162,   197,   138,   359,     0,   738,   139,   207,
   200,   210,   195,   177,   178,   179,   135,   136,   141,   140,
   143,     0,   142,   144,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   776,   777,     0,     0,
     0,   778,     0,     0,   365,   366,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   369,     0,     0,   382,   383,     0,
     0,   328,     0,     0,     0,     0,   605,     0,     0,   285,
    70,     0,     0,     0,   743,   289,    71,     0,    68,     0,
     0,   452,    67,     0,   769,     0,     0,    19,     0,     0,
     0,   241,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,    13,    12,     0,     0,     0,     0,
     0,   269,     0,     0,     0,   741,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,   254,    52,   253,   627,   626,
   628,   624,   625,     0,     0,     0,     0,   592,   601,     0,
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
     1,   351,    69,    70,   669,   590,   591,   209,   436,   730,
   731,   445,   732,   733,   196,    71,    72,    73,    74,    75,
   354,   353,    76,   540,   356,    77,    78,   711,    79,    80,
   437,    81,    82,    83,    84,   625,   447,   448,   312,   313,
    86,    87,    88,    89,   314,   230,   302,   810,  1050,  1271,
   811,   918,    91,   489,   922,   592,   638,   288,    92,   771,
    93,    94,   615,   616,   734,   211,   842,   232,   847,   848,
   547,  1076,   965,   877,   798,   617,    96,    97,   281,   462,
   804,   320,   233,   315,   593,   545,   544,   736,   737,   855,
   549,   550,   100,   101,   861,  1153,  1223,   948,   739,  1079,
  1080,   740,   326,   492,   284,   102,   529,  1081,   480,   285,
   481,   867,   741,   423,   401,   632,   553,   551,   103,   104,
   648,   234,   212,   213,   742,  1141,   938,   851,   106,   317,
  1084,   269,   493,   856,   857,  1142,   198,   743,   399,   485,
   765,   107,   108,   109,   744,   745,   746,   747,   641,   410,
   949,   110,   111,   112,   113,   660,  1027,  1028,  1181,  1030,
  1031,  1032,  1033,  1105,  1106,  1107,  1209,  1108,  1035,  1036,
  1037,  1038,  1039,  1040,  1041,  1042,  1043,  1044,  1045,  1046,
  1047,  1048,  1133,  1134,  1119,  1109,     2,   239,   240,   511,
   501,   486,   646,   522,   289,   214,   318,   319,   698,   451,
   242,   664,   823,   243,   824,   674,  1054,   790,   452,   788,
   642,   442,   644,   645,   916,   749,   879,   359,   715,   714,
   548,   554,   757,   552,   755,   818,   928,  1189,  1111,  1101,
   708,   707,   838,   937,  1055,  1139,   789,   799,   441,
    }, yySindex = {
//yySindex 1290
     0,     0, 21762, 23333,     0,     0, 21107, 21509,     0, 24503,
 24503, 19515,     0,     0, 25023, 22154, 22154,     0,     0,     0,
  -236,  -177,     0,     0,     0,     0,   114, 21375,   158,  -160,
  -127,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0, 24633, 24633,   940, 24633, 24633,   -44, 21893,     0, 22809,
 23202, 20074, 24633, 24763, 21241,     0,     0,     0,   249,   259,
     0,     0,     0,     0,     0,     0,     0,   270,   346,     0,
     0,     0,   -61,     0,     0,     0,   -84,     0,     0,     0,
     0,     0,     0,     0,     0,  1706,    80,  7772,     0,   285,
  1041,   568,     0,   688,     0,    60,   497,     0,   517,     0,
     0,     0, 25153,   544,     0,   282,     0,     0,   649,     0,
  -112, 22154, 25283, 25413,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   -68,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   596,     0,     0, 22024,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   341, 22024,    80,
   104,  1139,   308,   605,   380,   104,     0,     0,   649,   459,
   674,     0, 24503, 24503,     0,     0,  -236,  -177,     0,     0,
     0,     0,   394,   158,     0,     0,     0,     0,     0,     0,
     0,     0,   940,   435,     0,  1197,     0,     0,     0,   364,
  -112,     0, 24633, 24633, 24633, 24633,     0, 24633,  7772,     0,
     0,   416,   721,   743,     0,     0,     0, 17967,     0, 22154,
 22285,     0,     0, 19647,     0, 24503,   486,     0, 23593, 21762,
 22024,     0,  1320,   451,   461,  4268,  4268,   498, 23463,     0,
 21893,   529,   649,  1706,     0,     0,     0,   158,   158, 23463,
   500,     0,    69,    87,   416,     0,   545,    87,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   579, 25543,  1344,     0,   890,     0,     0,     0,     0,     0,
     0,     0,     0,  1332,  1440,  1694,  1293,     0,     0,   598,
     0,     0,     0,     0,     0,     0, 24503, 24503, 24503, 24503,
 23463, 24503, 24503, 24633, 24633, 24633, 24633, 24633,     0,     0,
 24633, 24633, 24633, 24633, 24633, 24633, 24633, 24633, 24633, 24633,
 24633, 24633, 24633, 24633,     0,     0, 24633, 24633, 24633, 24633,
     0,     0,     0,     0,  4218, 22154,  4729, 24633,     0,     0,
  5295, 24763,     0, 23723, 21893, 20204,   899, 23723, 24763,     0,
 20334,     0,   608,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0, 24503,  -184,     0,   601,  1390,     0,
     0, 24503,     0,     0,     0,     0,     0,   700,   710,   498,
     0, 22024,   715,  7657, 22154, 25885, 24633, 24633, 24633, 22024,
  -144, 23853,   722,     0,   208,   208,   650,     0,     0, 25943,
 22154, 26001,     0,     0,     0,     0,  1035,     0, 24633, 22416,
     0,     0, 22940,     0,   158,     0,   662,     0, 24633,     0,
     0,   966,   973,   158,   158,    92,     0,     0,     0,     0,
     0, 21509, 24503,  7772,   657,   668,  7657, 25885, 24633, 24633,
  1706,   671,   158,     0,     0, 20464,     0,     0,  1706,     0,
 23071,     0,     0, 23202,     0,     0,     0,     0,     0,   991,
 26059, 22154, 26117, 25543,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,  1703,  -203,     0,     0,  3708,  1753,
   494,   775,     0,   679,     0,     0,     0,     0,     0,     0,
     0,     0,   451,  3338,  3338,  3338,  3338,  4807,  4352,  3338,
  3338,  4268,  4268,  1680,  1680,   451,  1220,   451,   451,  1157,
  1157,  1800,  1800,  6248,  3172,   781,   712,     0,   714,  -177,
     0,     0,     0,  1016,   158,   735,     0,   737,  -177,     0,
     0,  3172,     0,     0,  -177,     0,   776,  7264,  1446,     0,
     0,    60,  1019, 24633,  7264,     0,     0,     0,     0,  1051,
   158, 25543,  1052,     0,     0,   801,     0,     0,     0,     0,
     0,     0,     0,    80,     0,     0,     0,     0,     0, 26175,
 22154, 26233, 22024,    92,   757, 21643, 21509, 23983,   832,     0,
   553,     0,   767,   771,   158,   777,   779,   832,     0,   856,
   860,   118,     0,     0,     0,     0,     0,     0,     0,  -177,
   158,     0,     0,  -177, 24503, 24633,     0, 24633,   416,   743,
     0,     0,     0,     0, 22547, 22940,     0,     0,     0,     0,
    92,     0,     0,   451,     0, 21762,     0,     0,   158,    87,
 25543,     0,     0,   158,     0,     0,   991,     0,  1274,     0,
     0,   210,     0,  1097,  3708,  -174,     0,     0,     0,     0,
     0,     0,     0,     0,  2734,     0,     0,     0,     0,     0,
     0,   830,   833,  1112,     0,     0,  1113,  1115,     0,     0,
  1122,     0,     0,     0,   -58,  1124, 24633,     0,  1109,  1124,
     0,   257,     0,  1140,     0,     0,     0,     0,  1117,     0,
 24763, 24763,     0,   608, 22416,   838,   834, 24763, 24763,     0,
   608,     0,     0,   285,   -84, 23463, 24633, 26291, 22154, 26349,
 24763,     0, 24113,     0,   991, 25543, 23463,   817,   649, 24503,
 22024,     0,     0,     0,   158,   927,     0,  3708, 22024,  3708,
     0,     0,     0,     0,   852,     0, 22024,   930,     0, 24503,
     0,   932, 24633, 24633,   867, 24633, 24633,   945,     0,     0,
     0, 24243, 22024, 22024, 22024,     0,   208,     0,     0,     0,
  1168,   158,     0,   855,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   158,     0, 22024,
 22024,     0,  2734,  1123,     0,  1176,     0,   158,     0,     0,
  3169,     0,  3708,     0,  3255,     0,  1036,     0,     0,     0,
   503,     0,     0, 24633,     0,     0,     0, 22024,     0,  -136,
 22024, 24633,     0,     0,     0,     0,     0, 24763,     0,     0,
     0,     0,     0,     0,     0,     0,     0,  7772,   712,   714,
   158,   735,   737, 24633,     0,   991,     0,     0, 22024,   649,
   957,     0,     0,   158,   961,   649,   757, 25673,   104,     0,
     0, 22024,     0,     0,   104,     0, 24633,     0, 20772,     0,
   342,   971,   972,     0, 22940,     0,     0,     0,   900,  1196,
   993,   894,   158,  1673,  1217,  3326,     0,  1219,     0,     0,
  1223,  1231,     0,     0,  1234,     0,  1219,     0,     0,   975,
  1124,     0,     0,     0,  2588,     0,     0,  7772,  7772,     0,
   838,     0,  1017,     0,     0,     0,     0,     0, 22024,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   974,  1601,     0,     0, 22024,     0, 22024,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,  3823,  3823,
   466,     0,  3699,   158,   980,     0,  1708,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   254,   104,   610,    65,
     0,  1165,     0,     0,     0,     0,   604,     0,     0,     0,
   175,     0,  1248,     0,  1249,     0,     0,     0, 20977,     0,
     0,     0,     0,     0, 22024,     0,     0,  1545,     0,     0,
     0,  1251,  1673,  1146,     0,  3169,     0,  3169,     0,  3255,
     0,  3169,     0,     0,     0,  1253,   158,  1257,     0,     0,
  1259,  1260,     0,   946,     0,  1124, 25803,  1250,  1124,     0,
  1045,     0, 26407, 22154, 26465,   700,   553,     0,     0,     0,
     0, 20977,     0,   992,   158,   158, 17703,     0,  1269,     0,
   951,   469,     0,  1121,     0,     0,     0,     0, 24503,     0,
     0,     0,     0, 24503, 24503,  1029, 20875, 20977,  3823,  3823,
   466,   158,   158, 20772, 20772,   469, 20977,   992,  1074, 22024,
     0,    97,     0,     0,  1673,  1251,  1673,  1302,  1219,  1304,
  1219,  1219,  3169,     0,   995,  3255,     0,  1036,     0,  3255,
     0,     0,     0,     0,     0,  1054,  1782, 25803,     0,     0,
     0,     0,   158,     0,     0,     0,     0,   177,     0,     0,
    17,   992,  1316,     0,     0,     0,   158,     0,  1342, 22024,
     0,     0,     0,     0,  1317,     0,     0,     0,     0,     0,
     0,   158,   158,   158,   158,   158,   158,     0,     0,  1341,
     0,     0,  1007,  1545,     0,  1251,  1673,     0,  3169,     0,
     0,     0,  1366,     0,     0,  1369,  1375,     0,  1124,  1382,
  1366,     0,     0, 26523,  1782,     0,     0,  1395, 20977,     0,
   436,     0,     0,  -210, 20977,     0,     0,     0,     0,     0,
     0, 20875,     0,     0,  1251,  1219,  3169,     0,  3169,     0,
  3255,     0,     0,  3169,     0,     0,     0,     0, 20977,  1399,
     0,     0,     0,  1399,     0,     0,     0,  1366,  1402,  1366,
  1366,  1399, 20977,     0,  3169,     0,     0,     0,  1366,     0,
    }, yyRindex = {
//yyRindex 1290
     0,     0,   758,     0,     0,     0,     0,     0,     0,     0,
     0,  1177,     0,     0,     0, 11472, 11609,     0,     0,     0,
  5578,  5112,  9613,  9964, 10315, 10666, 24893,     0, 24373,     0,
     0, 11017, 13075, 13187,  5910,  4096, 13322, 13426,  6044, 13538,
     0,     0,     0,     0,     0,     0,     0,   116, 19378,  1106,
  1093,    31,     0,     0,  1852,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0, 10419,     0,     0,     0, 10907,     0,     0,     0,
     0,     0,     0,     0,     0,    76,  1758, 17377, 11121, 17480,
     0, 13673,     0, 17515,     0, 14024,     0,     0,     0,     0,
     0,     0,   194,     0,     0,     0,     0,     0,    63,     0,
 22678, 11713,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,  5216,  5682,  6197,  6705,     0,     0,     0,     0,     0,
     0,     0,     0,  7213,  7721, 12054, 12391,     0,     0,     0,
 12728,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,  3318,     0,     0,  1632,  2513,  8450,  8664,  8801,  9015,
  9152,  9366,  2925,  9503,  9717,  3080,  9854,     0,   116, 17649,
     0,     0, 10556,     0,     0,     0,     0,     0,  -133,     0,
   -60,     0,     0,     0,     0,     0, 11258, 10068,   410,   729,
   750,  1268,     0,  1132,  1280,  1312,  1376,  2169,  1652,  1670,
  2766,  1882,     0,     0,     0,     0,  1996,     0,     0,     0,
     0,     0,  2198,     0, 17117,     0,     0,     0,  2709,     0,
     0, 16963, 17160, 17160,     0,     0,     0,  1134,     0,     0,
   156,     0,     0,  1134,     0,     0,     0,     0,     0,   100,
   100,     0,     0, 12162,  1596, 16167, 16273, 14130,     0, 18977,
   116,     0,  2557,  1741,     0,     0,    57,  1134,  1134,     0,
     0,     0,  1119,  1119,     0,     0,     0,  1116,   897,  1037,
  1195,  1435,  1453,  1754,  1967,  2409,  2257,  2350,  2607,  2631,
     0,     0,     0,  2697,   200,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,  3192,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0, 11825, 11960,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,    18,     0,     0,     0,     0,
     0,     0,     0,     0,   116,   217,   219,     0,     0,     0,
    82,     0, 17206,     0,     0,     0,     0,     0,     0,     0,
     0,     0, 18106, 18250,     0,     0,     0, 19109,     0,     0,
     0,     0,     0,     0,     0,     0,     0,   815,     0, 10770,
     0,   707, 17837,     0,    18,     0,     0,     0,     0,   725,
     0,     0,     0,     0,     0,     0,     0,     0,  2109,     0,
    18,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,  1134,     0,     0,     0,   176,     0,
     0,   222,   207,  1134,  1134,  1134,     0,     0,     0,     0,
     0,     0,     0, 16529,     0,     0,     0,     0,     0,     0,
  2187,     0,  1134,     0,     0,  2648,    68,     0,   229,     0,
  1143,     0,     0,   -41,     0,     0,     0,  2710,     0,   224,
     0,    18,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0, 12297, 15263, 15366, 15477, 15608, 15719, 16017, 15822,
 15911, 16362, 16423, 14377, 14481, 12499, 14619, 12634, 12836, 13777,
 13915, 14779, 14910,  1427, 15021,     0,  6418,  4470,  7942, 22678,
     0,  4604,     0,    98,  1156,  6552,     0,  6926,  5444,     0,
     0, 15124,     0,     0,  8316,     0,  2083, 16874,     0,     0,
     0, 14266,     0,     0,  1405,     0,     0,     0,     0,     0,
  1134,     0,   263,     0,     0,  8488,     0,  1539,     0,     0,
     0,     0,     0,   702,     0, 18708,     0,     0,     0,     0,
    18,     0,  1632,  1134, 12740,     0,     0,   822,   685,     0,
  1222,     0,  3454,  4978,  1156,  3588,  3962,  1222,     0,     0,
     0,     0,     0,     0,     0,     0,     0,  3213,   524,     0,
  1156,  3794,  4302, 10205,     0,     0,     0,     0, 17027, 17160,
     0,     0,     0,     0,   231,   248,     0,     0,     0,     0,
  1134,     0,     0, 12971,     0,   100,   140,     0,  1134,  1119,
     0,  2752,   886,  1156, 12047, 12072,   283,     0,     0,     0,
     0,     0,     0,     0,   130,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,   294,   267,   363,     0,     0,   363,   363,     0,     0,
   407,     0,     0,     0,   621,   407,   146,     0,   665,   407,
     0,     0,     0,     0,     0, 18840,     0, 19246,     0,     0,
     0,     0,     0, 17273,   136,  8911,     0,     0,     0,     0,
 17337,     0,     0, 17593, 11394,     0,     0,     0,    18,     0,
     0,  1298,     0,     0,   333,     0,     0,     0,     0,     0,
  1632, 18407, 18551,     0,  1156,     0,     0,   258,  1632,   732,
     0,     0,     0,   821,   280,     0,   840,  1222,     0,     0,
     0,     0,     0,     0,  2045,     0,     0,     0,     0,     0,
     0,     0,   947,   675,   675,   910,     0,     0,     0,     0,
   207,  1134,     0,     0,     0,     0,     0,  1039,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     3,     0,  1632,
   100,     0,     0,   260,     0,   274,     0,  1134,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,  1632,     0,     0,
   100,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0, 16618,  7060,  8076,
  1156,  7434,  7568,     0, 16232,   370,     0,     0,  1632,     0,
     0,     0,     0,  1134,     0,     0, 12740,     0,     0,     0,
     0,   675,     0,     0,     0,     0,     0,     0,     0,     0,
  1222,     0,     0,     0,   331,     0,     0,     0,     0,   188,
     0,     0,  1134,     0,   289,     0,     0,   363,     0,     0,
   363,   363,     0,     0,   363,     0,   363,     0,     0,   621,
   407,     0,     0,     0,    -1,     0,     0, 16721, 16785,     0,
  9262, 17702,     0,     0,     0,     0,     0,     0,  1632,  2231,
  2591,  2615,  2755,  5219,  5685,  8706,  2228,  9020,  9305,  2817,
  9371,     0,     0,  9402,     0,  1632,     0,   707,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,  1156,   620,  2689,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,  1286,     0,   733,   686,
     0,  8878,     0,     0,     0,     0,   902,     0,     0,     0,
  1698,     0,  1700,     0,  1935,     0,     0,     0,   769,     0,
     0,     0,     0,     0,   675,     0,     0,     0,     0,     0,
     0,   309,     0,   317,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,    93,    -1,    93,     0,     0,
   110,    93,     0,     0,     0,   110,   137,   138,   110,     0,
     0,  9408,     0,    18,     0,   815,  1222,     0,     0,     0,
     0,     0,     0,  6707,  1156,  1156,  2210,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,   911,     0, 11649, 13009,
     0, 20566, 20669,     0,     0,  1764,     0,  1058,     0,    16,
     0,   367,     0,     0,     0,   326,     0,   338,   363,   363,
   363,   363,     0,     0,     0,   126,     0,     0,     0,     0,
     0,     0,     0,  1542,  2189,     0,   150,     0,     0,     0,
  8554,  1449,  1156,  8557, 10175,     0,     0,  1134,     0,     0,
  4202,  7215,  4710,     0,     0,     0,  1128,     0,     0,    86,
     0,     0,     0,     0,   934,     0,     0,     0,     0,     0,
     0,  1134,  1134,  1134,  1156,  1156,  1156,     0,     0,  4844,
     0,     0,     0,     0,     0,   355,     0,     0,     0,     0,
     0,     0,    93,     0,     0,    93,    93,     0,   110,    93,
    93,     0,     0,     0,   154, 10877,     0,  5684,     0,     0,
     0,     0,     0,  1222,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,   414,   363,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,  8806,  6118,     0,  5818,
     0,     0,     0,  1258,     0,     0,     0,    93,    93,    93,
    93,  6792,     0,     0,     0,     0,     0,     0,    93,     0,
    }, yyGindex = {
//yyGindex 239
     0,     0,    26,     0,  -316,     0,    37,     6,  -425,  -695,
     0,     0,     0,  -690,     0, 15241,     0,  1432, 15819, 16686,
  -274,  1450,     0,  -202,     0, 18935, 18987,   956, 18998,    38,
  1403,  -180,   117,     0,    14,     0,   190,   509,     0,    67,
    40,  2247,   -13,    62,  1000,    29,    21,  -644,     0,     0,
   422,     0,     0,   782,     0,    90,     0,   -12,  1514,   919,
     0,     0,  -384,   796,  -769,     0,     0,   205,  -465,   982,
     0,     0,     0,   770,   612,  -351,   -88,    -5,   585,  -445,
     0,     0,   504,    -2,   462,     0,     0,    56,   673,  -179,
     0,     0, 19035, 19166,  -593,  1884,   584,   153,   693,   411,
     0,     0,     0,     5,  -440,     0,   101,   425,  -266,  -463,
     0,  -665,   178,   -70,   798,  -564,   958,  1187,  1574,    46,
   502,   723,     0,   -22,  -840,     0,  -822,     0, 19182,  -191,
  -998,     0,  -353,  -841,   747,   389,     0, -1001,  1522,   340,
   265,  -281,     0,    22,   550,   184,  2334,  -283,   -82,     0,
  -369,  1348,  1797,     0,     0,   371,     0,     0,  2524,     0,
     0,   488,  -890,  -239,     0,   690,  -362,   979,     0,  -816,
   499,     0,     0,     0,  -443,     0,   484, -1011,     0,     0,
   491,     0,     0,     0,     0,   441,     0,   536,    -3,     0,
     0,    45,     0,  -242,     0,     0,     0,     0,     0,  -232,
     0,  -378,     0,     0,     0,     0,     0,     0,   264,     0,
     0,     0,     0,     0,     0,  2359,     0,     0,     0,     0,
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
    null,null,null,null,null,null,"'|'",null,null,null,null,null,null,
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
    yyVals[0+yyTop] = lexer.inKwarg;
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
    yyVal = support.new_hash_pattern(null, support.new_hash_pattern_tail(support.getPosition(yyVals[-1+yyTop]), null, null));
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
    yyVal = support.new_array_pattern_tail(support.getPosition(((TruffleString)yyVals[0+yyTop])), null, true, ((TruffleString)yyVals[0+yyTop]), null);
    return yyVal;
};
states[517] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_array_pattern_tail(support.getPosition(((TruffleString)yyVals[-2+yyTop])), null, true, ((TruffleString)yyVals[-2+yyTop]), ((ListParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[518] = (support, lexer, yyVal, yyVals, yyTop) -> {
    yyVal = support.new_find_pattern_tail(support.getPosition(((TruffleString)yyVals[-4+yyTop])), ((TruffleString)yyVals[-4+yyTop]), ((ListParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop]));
    support.warn(support.getPosition(((TruffleString)yyVals[-4+yyTop])), "Find pattern is experimental, and the behavior may change in future versions of Ruby!");
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
    yyVal = support.new_hash_pattern_tail(support.getPosition(((HashParseNode)yyVals[-2+yyTop])), ((HashParseNode)yyVals[-2+yyTop]), ((TruffleString)yyVals[0+yyTop]));
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
    yyVal = support.new_hash_pattern_tail(support.getPosition(((TruffleString)yyVals[0+yyTop])), null, ((TruffleString)yyVals[0+yyTop]));
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
    support.error_duplicate_pattern_key(((TruffleString)yyVals[-1+yyTop]));

    ParseNode label = support.asSymbol(support.getPosition(((TruffleString)yyVals[-1+yyTop])), ((TruffleString)yyVals[-1+yyTop]));

    yyVal = new ParseNodeTuple(label, ((ParseNode)yyVals[0+yyTop]));
    return yyVal;
};
states[531] = (support, lexer, yyVal, yyVals, yyTop) -> {
    support.error_duplicate_pattern_key(((TruffleString)yyVals[0+yyTop]));
    if (((TruffleString)yyVals[0+yyTop]) != null && !support.is_local_id(((TruffleString)yyVals[0+yyTop]))) {
        support.yyerror("key must be valid as local variables");
    }
    support.error_duplicate_pattern_variable(((TruffleString)yyVals[0+yyTop]));

    ParseNode label = support.asSymbol(support.getPosition(((TruffleString)yyVals[0+yyTop])), ((TruffleString)yyVals[0+yyTop]));
    yyVal = new ParseNodeTuple(label, support.assignableLabelOrIdentifier(((TruffleString)yyVals[0+yyTop]), null));
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
    yyVal = new FileParseNode(lexer.getPosition(), TruffleString.fromByteArrayUncached(lexer.getFile().getBytes(), lexer.tencoding , true), lexer.encoding);
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
    yyVal = support.gettable(((TruffleString)yyVals[0+yyTop]));
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
    yyVal = TStringConstants.RCURLY;
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
// line 3249 "RubyParser.y"

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
<<<<<<< HEAD
// line 12302 "-"
=======
// line 12101 "-"
>>>>>>> 3fc6ad8a15 (The rbrace rule should match tRCURLY since that is what RubyLexer emits)
