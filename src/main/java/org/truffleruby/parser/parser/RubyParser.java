// created by jay 1.0.2 (c) 2002-2004 ats@cs.rit.edu
// skeleton Java 1.0 (c) 2002 ats@cs.rit.edu

// line 2 "RubyParser.y"
/***** BEGIN LICENSE BLOCK *****
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
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
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 ***** END LICENSE BLOCK *****/
package org.truffleruby.parser.parser;

import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_BEG;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_END;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_ENDARG;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_ENDFN;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_FITEM;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_FNAME;
import static org.truffleruby.parser.lexer.RubyLexer.EXPR_LABEL;

import java.nio.charset.Charset;

import org.jcodings.Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.parser.RubyWarnings;
import org.truffleruby.parser.TranslatorEnvironment;
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

// CheckStyle: start generated
@SuppressFBWarnings("IP")
@SuppressWarnings({ "unchecked", "fallthrough", "cast" })
public class RubyParser {
    protected final ParserSupport support;
    protected final RubyLexer lexer;

    public RubyParser(RubyContext context, LexerSource source, RubyWarnings warnings) {
        this.support = new ParserSupport(context, source.getSource().getName(), warnings);
        this.lexer = new RubyLexer(support, source, warnings);
        support.setLexer(lexer);
    }

    // line 159 "-"
    // %token constants
    public static final int kCLASS = 257;
    public static final int kMODULE = 258;
    public static final int kDEF = 259;
    public static final int kUNDEF = 260;
    public static final int kBEGIN = 261;
    public static final int kRESCUE = 262;
    public static final int kENSURE = 263;
    public static final int kEND = 264;
    public static final int kIF = 265;
    public static final int kUNLESS = 266;
    public static final int kTHEN = 267;
    public static final int kELSIF = 268;
    public static final int kELSE = 269;
    public static final int kCASE = 270;
    public static final int kWHEN = 271;
    public static final int kWHILE = 272;
    public static final int kUNTIL = 273;
    public static final int kFOR = 274;
    public static final int kBREAK = 275;
    public static final int kNEXT = 276;
    public static final int kREDO = 277;
    public static final int kRETRY = 278;
    public static final int kIN = 279;
    public static final int kDO = 280;
    public static final int kDO_COND = 281;
    public static final int kDO_BLOCK = 282;
    public static final int kRETURN = 283;
    public static final int kYIELD = 284;
    public static final int kSUPER = 285;
    public static final int kSELF = 286;
    public static final int kNIL = 287;
    public static final int kTRUE = 288;
    public static final int kFALSE = 289;
    public static final int kAND = 290;
    public static final int kOR = 291;
    public static final int kNOT = 292;
    public static final int kIF_MOD = 293;
    public static final int kUNLESS_MOD = 294;
    public static final int kWHILE_MOD = 295;
    public static final int kUNTIL_MOD = 296;
    public static final int kRESCUE_MOD = 297;
    public static final int kALIAS = 298;
    public static final int kDEFINED = 299;
    public static final int klBEGIN = 300;
    public static final int klEND = 301;
    public static final int k__LINE__ = 302;
    public static final int k__FILE__ = 303;
    public static final int k__ENCODING__ = 304;
    public static final int kDO_LAMBDA = 305;
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
    public static final int tAREF = 331;
    public static final int tASET = 332;
    public static final int tLSHFT = 333;
    public static final int tRSHFT = 334;
    public static final int tANDDOT = 335;
    public static final int tCOLON2 = 336;
    public static final int tCOLON3 = 337;
    public static final int tOP_ASGN = 338;
    public static final int tASSOC = 339;
    public static final int tLPAREN = 340;
    public static final int tLPAREN2 = 341;
    public static final int tRPAREN = 342;
    public static final int tLPAREN_ARG = 343;
    public static final int tLBRACK = 344;
    public static final int tRBRACK = 345;
    public static final int tLBRACE = 346;
    public static final int tLBRACE_ARG = 347;
    public static final int tSTAR = 348;
    public static final int tSTAR2 = 349;
    public static final int tAMPER = 350;
    public static final int tAMPER2 = 351;
    public static final int tTILDE = 352;
    public static final int tPERCENT = 353;
    public static final int tDIVIDE = 354;
    public static final int tPLUS = 355;
    public static final int tMINUS = 356;
    public static final int tLT = 357;
    public static final int tGT = 358;
    public static final int tPIPE = 359;
    public static final int tBANG = 360;
    public static final int tCARET = 361;
    public static final int tLCURLY = 362;
    public static final int tRCURLY = 363;
    public static final int tBACK_REF2 = 364;
    public static final int tSYMBEG = 365;
    public static final int tSTRING_BEG = 366;
    public static final int tXSTRING_BEG = 367;
    public static final int tREGEXP_BEG = 368;
    public static final int tWORDS_BEG = 369;
    public static final int tQWORDS_BEG = 370;
    public static final int tSTRING_DBEG = 371;
    public static final int tSTRING_DVAR = 372;
    public static final int tSTRING_END = 373;
    public static final int tLAMBDA = 374;
    public static final int tLAMBEG = 375;
    public static final int tNTH_REF = 376;
    public static final int tBACK_REF = 377;
    public static final int tSTRING_CONTENT = 378;
    public static final int tINTEGER = 379;
    public static final int tIMAGINARY = 380;
    public static final int tFLOAT = 381;
    public static final int tRATIONAL = 382;
    public static final int tREGEXP_END = 383;
    public static final int tSYMBOLS_BEG = 384;
    public static final int tQSYMBOLS_BEG = 385;
    public static final int tDSTAR = 386;
    public static final int tSTRING_DEND = 387;
    public static final int tLABEL_END = 388;
    public static final int tLOWEST = 389;
    public static final int yyErrorCode = 256;

    /** number of final state.
    */
    protected static final int yyFinal = 1;

    /** parser tables.
      Order is mandated by <i>jay</i>.
    */
    protected static final short[] yyLhs = {
            //yyLhs 645
            -1, 143, 0, 133, 134, 134, 134, 134, 135, 146,
            135, 37, 36, 38, 38, 38, 38, 44, 147, 44,
            148, 39, 39, 39, 39, 39, 39, 39, 39, 39,
            39, 39, 39, 39, 39, 39, 39, 39, 39, 39,
            39, 39, 39, 39, 31, 31, 40, 40, 40, 40,
            40, 40, 45, 32, 32, 59, 59, 150, 110, 142,
            43, 43, 43, 43, 43, 43, 43, 43, 43, 43,
            43, 111, 111, 122, 122, 112, 112, 112, 112, 112,
            112, 112, 112, 112, 112, 71, 71, 100, 100, 101,
            101, 72, 72, 72, 72, 72, 72, 72, 72, 72,
            72, 72, 72, 72, 72, 72, 72, 72, 72, 72,
            77, 77, 77, 77, 77, 77, 77, 77, 77, 77,
            77, 77, 77, 77, 77, 77, 77, 77, 77, 6,
            6, 30, 30, 30, 7, 7, 7, 7, 7, 115,
            115, 116, 116, 61, 151, 61, 8, 8, 8, 8,
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
            8, 8, 8, 8, 8, 8, 131, 131, 131, 131,
            131, 131, 131, 131, 131, 131, 131, 131, 131, 131,
            131, 131, 131, 131, 131, 131, 131, 131, 131, 131,
            131, 131, 131, 131, 131, 131, 131, 131, 131, 131,
            131, 131, 131, 131, 131, 131, 131, 131, 41, 41,
            41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
            41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
            41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
            41, 41, 41, 41, 41, 41, 41, 41, 41, 41,
            41, 41, 73, 76, 76, 76, 76, 53, 57, 57,
            125, 125, 125, 125, 125, 51, 51, 51, 51, 51,
            153, 55, 104, 103, 103, 79, 79, 79, 79, 35,
            35, 70, 70, 70, 42, 42, 42, 42, 42, 42,
            42, 42, 42, 42, 42, 154, 42, 155, 42, 156,
            157, 42, 42, 42, 42, 42, 42, 42, 42, 42,
            42, 42, 42, 42, 42, 42, 42, 42, 42, 42,
            159, 161, 42, 162, 163, 42, 42, 42, 164, 165,
            42, 166, 42, 168, 169, 42, 170, 42, 171, 42,
            172, 173, 42, 42, 42, 42, 42, 46, 158, 158,
            158, 160, 160, 49, 49, 47, 47, 124, 124, 126,
            126, 84, 84, 127, 127, 127, 127, 127, 127, 127,
            127, 127, 91, 91, 91, 91, 90, 90, 66, 66,
            66, 66, 66, 66, 66, 66, 66, 66, 66, 66,
            66, 66, 66, 68, 68, 67, 67, 67, 119, 119,
            118, 118, 128, 128, 174, 175, 121, 65, 65, 120,
            120, 176, 109, 58, 58, 58, 58, 22, 22, 22,
            22, 22, 22, 22, 22, 22, 177, 108, 178, 108,
            74, 48, 48, 113, 113, 75, 75, 75, 50, 50,
            52, 52, 28, 28, 28, 15, 16, 16, 16, 17,
            18, 19, 25, 25, 81, 81, 27, 27, 87, 87,
            85, 85, 26, 26, 88, 88, 80, 80, 86, 86,
            20, 20, 21, 21, 24, 24, 23, 179, 23, 180,
            181, 182, 183, 184, 23, 62, 62, 62, 62, 2,
            1, 1, 1, 1, 29, 33, 33, 34, 34, 34,
            34, 56, 56, 56, 56, 56, 56, 56, 56, 56,
            56, 56, 56, 114, 114, 114, 114, 114, 114, 114,
            114, 114, 114, 114, 114, 63, 63, 185, 54, 54,
            69, 186, 69, 92, 92, 92, 92, 89, 89, 64,
            64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
            64, 64, 64, 64, 132, 132, 132, 132, 9, 9,
            141, 117, 117, 82, 82, 138, 93, 93, 94, 94,
            95, 95, 96, 96, 136, 136, 137, 137, 60, 123,
            102, 102, 83, 83, 11, 11, 13, 13, 12, 12,
            107, 106, 106, 14, 187, 14, 97, 97, 98, 98,
            99, 99, 99, 99, 3, 3, 3, 4, 4, 4,
            4, 5, 5, 5, 10, 10, 139, 139, 140, 140,
            144, 144, 149, 149, 129, 130, 152, 152, 152, 167,
            167, 145, 145, 78, 105,
    }, yyLen = {
            //yyLen 645
            2, 0, 2, 2, 1, 1, 3, 2, 1, 0,
            5, 4, 2, 1, 1, 3, 2, 1, 0, 5,
            0, 4, 3, 3, 3, 2, 3, 3, 3, 3,
            3, 4, 1, 3, 3, 6, 5, 5, 5, 5,
            3, 3, 3, 1, 3, 3, 1, 3, 3, 3,
            2, 1, 1, 1, 1, 1, 4, 0, 5, 1,
            2, 3, 4, 5, 4, 5, 2, 2, 2, 2,
            2, 1, 3, 1, 3, 1, 2, 3, 5, 2,
            4, 2, 4, 1, 3, 1, 3, 2, 3, 1,
            3, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 4, 3, 3, 3, 3, 2, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 4, 3, 3, 3, 3, 2, 1, 1,
            1, 2, 1, 3, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 0, 4, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 3, 5,
            3, 5, 6, 5, 5, 5, 5, 4, 3, 3,
            3, 3, 3, 3, 3, 3, 3, 4, 2, 2,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
            3, 3, 3, 2, 2, 3, 3, 3, 3, 3,
            6, 1, 1, 1, 2, 4, 2, 3, 1, 1,
            1, 1, 2, 4, 2, 1, 2, 2, 4, 1,
            0, 2, 2, 2, 1, 1, 2, 3, 4, 1,
            1, 3, 4, 2, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 0, 4, 0, 3, 0,
            0, 5, 3, 3, 2, 3, 3, 1, 4, 3,
            1, 5, 4, 3, 2, 1, 2, 2, 6, 6,
            0, 0, 7, 0, 0, 7, 5, 4, 0, 0,
            9, 0, 6, 0, 0, 8, 0, 5, 0, 6,
            0, 0, 9, 1, 1, 1, 1, 1, 1, 1,
            2, 1, 1, 1, 5, 1, 2, 1, 1, 1,
            3, 1, 3, 1, 4, 6, 3, 5, 2, 4,
            1, 3, 4, 2, 2, 1, 2, 0, 6, 8,
            4, 6, 4, 2, 6, 2, 4, 6, 2, 4,
            2, 4, 1, 1, 1, 3, 1, 4, 1, 4,
            1, 3, 1, 1, 0, 0, 4, 4, 1, 3,
            3, 0, 5, 2, 4, 5, 5, 2, 4, 4,
            3, 3, 3, 2, 1, 4, 0, 5, 0, 5,
            5, 1, 1, 6, 0, 1, 1, 1, 2, 1,
            2, 1, 1, 1, 1, 1, 1, 1, 2, 3,
            3, 3, 3, 3, 0, 3, 1, 2, 3, 3,
            0, 3, 3, 3, 3, 3, 0, 3, 0, 3,
            0, 2, 0, 2, 0, 2, 1, 0, 3, 0,
            0, 0, 0, 0, 8, 1, 1, 1, 1, 2,
            1, 1, 1, 1, 3, 1, 2, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 0, 4, 0,
            3, 0, 3, 4, 2, 2, 1, 2, 0, 6,
            8, 4, 6, 4, 6, 2, 4, 6, 2, 4,
            2, 4, 1, 0, 1, 1, 1, 1, 1, 1,
            1, 1, 3, 1, 3, 1, 2, 1, 2, 1,
            1, 3, 1, 3, 1, 1, 2, 1, 3, 3,
            1, 3, 1, 3, 1, 1, 2, 1, 1, 1,
            2, 2, 0, 1, 0, 4, 1, 2, 1, 3,
            3, 2, 4, 2, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            0, 1, 0, 1, 2, 2, 0, 1, 1, 1,
            1, 1, 2, 0, 0,
    }, yyDefRed = {
            //yyDefRed 1095
            1, 0, 0, 0, 0, 0, 0, 0, 305, 0,
            0, 0, 330, 333, 0, 0, 0, 355, 356, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 9,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            456, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 480, 482, 484, 0, 0, 414, 535,
            536, 507, 510, 508, 509, 0, 0, 453, 59, 295,
            0, 457, 296, 297, 0, 298, 299, 294, 454, 32,
            46, 452, 505, 0, 0, 0, 0, 0, 0, 302,
            0, 54, 0, 0, 85, 0, 4, 300, 301, 0,
            0, 71, 0, 2, 0, 5, 0, 7, 353, 354,
            317, 0, 0, 517, 516, 518, 519, 0, 0, 521,
            520, 522, 0, 513, 512, 0, 515, 0, 0, 0,
            0, 132, 0, 357, 0, 303, 0, 346, 186, 197,
            187, 210, 183, 203, 193, 192, 213, 214, 208, 191,
            190, 185, 211, 215, 216, 195, 184, 198, 202, 204,
            196, 189, 205, 212, 207, 0, 0, 0, 0, 182,
            201, 200, 217, 181, 188, 179, 180, 0, 0, 0,
            0, 136, 0, 171, 172, 168, 149, 150, 151, 158,
            155, 157, 152, 153, 173, 174, 159, 160, 604, 165,
            164, 148, 170, 167, 166, 162, 163, 156, 154, 146,
            169, 147, 175, 161, 348, 137, 0, 603, 138, 206,
            199, 209, 194, 176, 177, 178, 134, 135, 140, 139,
            142, 0, 141, 143, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 639, 640, 0, 0, 0,
            641, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 367, 368,
            0, 0, 0, 0, 0, 480, 0, 0, 275, 69,
            0, 0, 0, 608, 279, 70, 68, 0, 67, 0,
            0, 433, 66, 0, 633, 0, 0, 20, 0, 0,
            0, 238, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 14, 13, 0, 0, 0, 0, 0, 263,
            0, 0, 0, 606, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 254, 50, 253, 502, 501, 503, 499,
            500, 0, 0, 0, 0, 0, 0, 0, 0, 327,
            415, 0, 0, 0, 0, 458, 438, 436, 326, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 421, 423, 626, 627, 0, 0, 0, 629,
            628, 0, 0, 87, 0, 0, 0, 0, 0, 0,
            3, 0, 427, 0, 324, 0, 506, 0, 129, 0,
            131, 537, 341, 0, 0, 0, 0, 0, 0, 624,
            625, 350, 144, 0, 0, 0, 359, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 642,
            0, 0, 0, 0, 0, 0, 338, 611, 286, 282,
            0, 613, 0, 0, 276, 284, 0, 277, 0, 319,
            0, 281, 271, 270, 0, 0, 0, 0, 323, 49,
            22, 24, 23, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 312, 12, 0, 0, 308,
            0, 315, 0, 637, 264, 0, 266, 316, 607, 0,
            89, 0, 0, 0, 0, 0, 489, 487, 504, 486,
            483, 459, 481, 460, 461, 485, 462, 463, 466, 0,
            472, 473, 0, 0, 468, 469, 0, 474, 475, 0,
            0, 0, 26, 27, 28, 29, 30, 47, 48, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            430, 0, 432, 0, 0, 619, 0, 0, 620, 431,
            617, 618, 0, 40, 0, 0, 45, 44, 0, 41,
            285, 0, 0, 0, 0, 0, 88, 33, 42, 289,
            0, 34, 0, 6, 57, 61, 0, 0, 0, 0,
            0, 0, 133, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 306, 0, 360, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 337, 362, 331, 361,
            334, 0, 0, 0, 0, 0, 0, 0, 610, 0,
            0, 0, 283, 609, 318, 634, 0, 0, 267, 322,
            21, 0, 0, 31, 0, 0, 0, 0, 15, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 490, 0,
            465, 467, 477, 569, 566, 565, 564, 567, 575, 584,
            0, 0, 595, 594, 599, 598, 585, 570, 0, 0,
            0, 592, 418, 0, 0, 0, 562, 582, 0, 546,
            573, 568, 0, 0, 0, 0, 471, 479, 406, 0,
            404, 0, 403, 0, 0, 0, 0, 0, 429, 0,
            0, 0, 0, 0, 269, 0, 428, 268, 0, 0,
            0, 0, 0, 0, 86, 0, 0, 0, 0, 344,
            0, 0, 435, 347, 605, 0, 0, 0, 351, 145,
            446, 0, 0, 447, 0, 0, 365, 0, 363, 0,
            0, 0, 0, 0, 0, 0, 336, 0, 0, 0,
            0, 0, 0, 612, 288, 278, 0, 321, 10, 0,
            311, 265, 90, 0, 491, 495, 496, 497, 488, 498,
            0, 0, 369, 0, 371, 0, 0, 596, 600, 0,
            560, 0, 0, 416, 0, 555, 0, 558, 0, 544,
            586, 0, 545, 576, 0, 0, 0, 0, 402, 580,
            0, 0, 385, 0, 590, 0, 0, 0, 0, 0,
            0, 0, 0, 39, 0, 38, 0, 65, 0, 635,
            36, 0, 37, 0, 63, 426, 425, 0, 0, 0,
            0, 0, 0, 0, 538, 342, 540, 349, 542, 0,
            0, 0, 449, 366, 0, 11, 451, 0, 328, 0,
            329, 287, 0, 0, 0, 339, 0, 19, 492, 0,
            0, 0, 0, 572, 0, 0, 547, 571, 0, 0,
            0, 0, 574, 0, 593, 0, 583, 601, 0, 588,
            0, 400, 0, 0, 395, 0, 383, 0, 398, 405,
            384, 0, 0, 0, 0, 0, 0, 439, 437, 0,
            422, 35, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 448, 0, 450, 0, 441, 440, 442, 332,
            335, 0, 493, 370, 0, 0, 0, 372, 417, 0,
            561, 420, 419, 0, 553, 0, 551, 0, 556, 559,
            543, 0, 386, 407, 0, 0, 581, 0, 0, 0,
            591, 314, 0, 0, 412, 0, 410, 413, 58, 345,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 401, 0,
            392, 0, 390, 382, 0, 396, 399, 0, 0, 0,
            409, 352, 0, 0, 0, 0, 0, 443, 364, 340,
            0, 0, 0, 554, 0, 549, 552, 557, 0, 0,
            0, 0, 411, 0, 494, 0, 0, 394, 0, 388,
            391, 397, 550, 0, 389,
    }, yyDgoto = {
            //yyDgoto 188
            1, 359, 67, 68, 641, 600, 131, 229, 601, 727,
            451, 728, 729, 730, 216, 69, 70, 71, 72, 73,
            362, 361, 74, 540, 364, 75, 76, 549, 77, 78,
            132, 79, 80, 81, 82, 628, 453, 454, 320, 321,
            84, 85, 86, 87, 322, 249, 312, 798, 987, 799,
            901, 492, 905, 602, 442, 298, 89, 766, 90, 91,
            731, 231, 828, 251, 732, 733, 856, 750, 751, 648,
            619, 93, 94, 290, 468, 792, 328, 252, 323, 494,
            368, 366, 734, 735, 833, 372, 374, 97, 98, 840,
            941, 1012, 926, 737, 859, 860, 738, 334, 495, 293,
            99, 531, 861, 484, 294, 485, 849, 739, 434, 413,
            635, 100, 101, 653, 253, 232, 233, 740, 1025, 863,
            843, 369, 325, 864, 280, 496, 834, 835, 1026, 489,
            760, 218, 741, 103, 104, 105, 742, 743, 744, 445,
            421, 927, 136, 2, 258, 259, 309, 513, 503, 490,
            778, 651, 524, 299, 234, 326, 327, 699, 457, 261,
            668, 809, 262, 810, 676, 991, 638, 458, 636, 893,
            446, 448, 650, 899, 370, 553, 595, 561, 560, 709,
            708, 824, 918, 992, 1038, 637, 649, 447,
    }, yySindex = {
            //yySindex 1095
            0, 0, 18905, 20204, 3558, 22139, 18270, 18660, 0, 21365,
            21365, 17346, 0, 0, 21881, 19294, 19294, 0, 0, 19294,
            -199, -185, 0, 0, 0, 0, 74, 18530, 155, 0,
            -190, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 21494, 21494, 1419, -103, 19035, 0, 19684, 20074, 17748,
            21494, 21623, 18400, 0, 0, 0, 225, 261, 0, 0,
            0, 0, 0, 0, 0, 264, 267, 0, 0, 0,
            -153, 0, 0, 0, -127, 0, 0, 0, 0, 0,
            0, 0, 0, 1759, 339, 5011, 0, -46, 523, 0,
            467, 0, -33, 272, 0, 306, 0, 0, 0, 22010,
            319, 0, 86, 0, 189, 0, -118, 0, 0, 0,
            0, -199, -185, 0, 0, 0, 0, 48, 155, 0,
            0, 0, 0, 0, 0, 0, 0, 1419, 21365, 22,
            19165, 0, 45, 0, 602, 0, -118, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, -48, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 392, 0, 0, 19165, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 190, 339, 215,
            724, 165, 532, 271, 215, 0, 0, 189, 385, 573,
            0, 21365, 21365, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 363, 985, 0, 0, 0,
            432, 21494, 21494, 21494, 21494, 0, 21494, 5011, 0, 0,
            343, 686, 691, 0, 0, 0, 0, 15861, 0, 19294,
            19294, 0, 0, 17615, 0, 21365, -41, 0, 20462, 375,
            19165, 0, 1074, 425, 436, 407, 20333, 0, 19035, 419,
            189, 1759, 0, 0, 0, 155, 155, 21365, 422, 0,
            141, 157, 343, 0, 414, 157, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 473, 22268,
            1141, 0, 741, 0, 0, 0, 0, 0, 0, 0,
            0, 595, 776, 1139, 409, 416, 1287, 423, -64, 0,
            0, 435, 1360, 443, -18, 0, 0, 0, 0, 21365,
            21365, 21365, 21365, 20333, 21365, 21365, 21494, 21494, 21494, 21494,
            21494, 21494, 21494, 21494, 21494, 21494, 21494, 21494, 21494, 21494,
            21494, 21494, 21494, 21494, 21494, 21494, 21494, 21494, 21494, 21494,
            21494, 21494, 0, 0, 0, 0, 3041, 19294, 5850, 0,
            0, 23222, 21623, 0, 20591, 19035, 17883, 754, 20591, 21623,
            0, 18012, 0, 486, 0, 495, 0, 339, 0, 0,
            0, 0, 0, 22606, 19294, 22662, 19165, 21365, 497, 0,
            0, 0, 0, 581, 550, 407, 0, 19165, 582, 22718,
            19294, 22774, 21494, 21494, 21494, 19165, 385, 20720, 586, 0,
            159, 159, 0, 22830, 19294, 22886, 0, 0, 0, 0,
            962, 0, 21494, 19424, 0, 0, 19814, 0, 155, 0,
            510, 0, 0, 0, 810, 816, 155, 352, 0, 0,
            0, 0, 0, 18660, 21365, 5011, 18905, 499, 22718, 22774,
            21494, 21494, 1759, 506, 155, 0, 0, 18141, 0, 0,
            339, 0, 19944, 0, 0, 20074, 0, 0, 0, 0,
            0, 827, 22942, 19294, 22998, 22268, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 146,
            0, 0, 841, 2448, 0, 0, 198, 0, 0, 852,
            -148, -148, 0, 0, 0, 0, 0, 0, 0, 425,
            3560, 3560, 3560, 3560, 3070, 3070, 3447, 3981, 3560, 3560,
            2973, 2973, 1206, 1206, 425, 2483, 425, 425, 474, 474,
            3070, 3070, 2570, 2570, 11973, -148, 554, 0, 560, -185,
            0, 0, 0, 155, 564, 0, 567, -185, 0, 0,
            0, 0, -185, 0, 5011, 21494, 0, 0, 4064, 0,
            0, 824, 846, 155, 22268, 849, 0, 0, 0, 0,
            0, 0, 4549, 0, 0, 0, 189, 21365, 19165, 0,
            0, -185, 0, 155, -185, 642, 352, 2495, 19165, 2495,
            18790, 18660, 20849, 644, 0, 452, 0, 572, 580, 155,
            583, 596, 4064, 644, 651, 228, 0, 0, 0, 0,
            0, 0, 0, 155, 0, 0, 21365, 21494, 0, 21494,
            343, 691, 0, 0, 0, 0, 19424, 19814, 0, 0,
            0, 352, 576, 0, 425, 5011, 18905, 0, 0, 155,
            157, 22268, 0, 0, 155, 0, 0, 827, 0, 441,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            2402, 2495, 0, 0, 0, 0, 0, 0, 647, 654,
            897, 0, 0, -123, 917, 926, 0, 0, 927, 0,
            0, 0, 668, 935, 21494, 896, 0, 0, 0, 1465,
            0, 19165, 0, 19165, 929, 19165, 21623, 21623, 0, 486,
            652, 653, 21623, 21623, 0, 486, 0, 0, -46, -127,
            0, 21494, 21623, 20978, 0, 827, 22268, 21494, -148, 0,
            189, 746, 0, 0, 0, 155, 747, 189, 0, 0,
            0, 0, 674, 0, 19165, 751, 0, 21365, 0, 766,
            21494, 21494, 694, 21494, 21494, 769, 0, 21107, 19165, 19165,
            19165, 0, 159, 0, 0, 0, 991, 0, 0, 677,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            2402, 641, 0, 1001, 0, 155, 155, 0, 0, 2545,
            0, 19165, 19165, 0, 2495, 0, 2495, 0, -28, 0,
            0, 456, 0, 0, 21494, 1020, 155, 1023, 0, 0,
            1024, 1025, 0, 719, 0, 935, 22397, 1019, 1035, 820,
            720, 21494, 832, 0, 5011, 0, 5011, 0, 21623, 0,
            0, 5011, 0, 5011, 0, 0, 0, 5011, 21494, 0,
            827, 5011, 19165, 19165, 0, 0, 0, 0, 0, 497,
            22526, 215, 0, 0, 19165, 0, 0, 215, 0, 21494,
            0, 0, 403, 833, 834, 0, 19814, 0, 0, 155,
            893, 1060, 2421, 0, 770, 1069, 0, 0, 851, 756,
            1073, 1076, 0, 1078, 0, 1069, 0, 0, 935, 0,
            2545, 0, 767, 2495, 0, -28, 0, 2495, 0, 0,
            0, 0, 0, 818, 1154, 22397, 1335, 0, 0, 5011,
            0, 0, 5011, 0, 771, 873, 19165, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 828,
            1165, 0, 0, 19165, 0, 19165, 0, 0, 0, 0,
            0, 19165, 0, 0, 1096, 893, 672, 0, 0, 2545,
            0, 0, 0, 2545, 0, 2495, 0, 2545, 0, 0,
            0, 1098, 0, 0, 1115, 1116, 0, 935, 1118, 1098,
            0, 0, 23054, 1154, 0, 173, 0, 0, 0, 0,
            902, 0, 23110, 19294, 23166, 581, 452, 905, 19165, 893,
            1096, 893, 1126, 1069, 1129, 1069, 1069, 2545, 0, 2545,
            0, 2495, 0, 0, 2545, 0, 0, 0, 0, 1335,
            0, 0, 0, 0, 155, 0, 0, 0, 0, 0,
            787, 1096, 893, 0, 2545, 0, 0, 0, 1098, 1134,
            1098, 1098, 0, 0, 0, 1096, 1069, 0, 2545, 0,
            0, 0, 0, 1098, 0,
    }, yyRindex = {
            //yyRindex 1095
            0, 0, 297, 0, 0, 0, 0, 0, 0, 0,
            0, 911, 0, 0, 0, 9497, 9694, 0, 0, 9803,
            4795, 4333, 10765, 10870, 11066, 11171, 21752, 0, 21236, 0,
            0, 11248, 11369, 11551, 5126, 3325, 11672, 11749, 5257, 11854,
            0, 0, 0, 0, 0, 133, 17480, 845, 847, 179,
            0, 0, 1379, 0, 0, 0, 1467, 266, 0, 0,
            0, 0, 0, 0, 0, 1486, 330, 0, 0, 0,
            8764, 0, 0, 0, 8878, 0, 0, 0, 0, 0,
            0, 0, 0, 59, 925, 9638, 8980, 15539, 0, 0,
            15589, 0, 12050, 0, 0, 0, 0, 0, 0, 263,
            0, 0, 0, 0, 40, 0, 19554, 0, 0, 0,
            0, 9286, 6598, 0, 0, 0, 0, 0, 850, 0,
            0, 0, 15994, 0, 0, 16131, 0, 0, 0, 0,
            133, 0, 16942, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 1369, 2041, 2427, 2558, 0,
            0, 0, 0, 0, 0, 0, 0, 2931, 3435, 3939,
            4422, 0, 4884, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 9230, 0, 0, 966, 6712, 6904, 7018, 7120, 7426,
            7528, 7642, 2185, 7834, 7948, 2317, 8050, 0, 794, 0,
            0, 8458, 0, 0, 0, 0, 0, 911, 0, 924,
            0, 0, 0, 1095, 1124, 1252, 1563, 1662, 1667, 1697,
            1939, 1919, 2089, 2058, 2505, 0, 0, 2604, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 15006, 0, 0,
            15266, 11476, 11476, 0, 0, 0, 0, 855, 0, 0,
            164, 0, 0, 855, 0, 0, 0, 0, 0, 0,
            60, 0, 0, 9910, 9388, 12155, 0, 16808, 133, 0,
            1614, 335, 0, 0, 68, 855, 855, 0, 0, 0,
            863, 863, 0, 0, 0, 854, 547, 5764, 7050, 7458,
            7586, 7615, 7980, 1085, 8388, 8516, 1539, 9318, 0, 0,
            0, 10495, 273, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 32, 0, 0,
            0, 0, 0, 0, 0, 133, 276, 310, 0, 0,
            0, 41, 0, 15364, 0, 0, 0, 195, 0, 16538,
            0, 0, 0, 0, 32, 0, 966, 0, 1520, 0,
            0, 0, 0, 601, 0, 8572, 0, 778, 16673, 0,
            32, 0, 0, 0, 0, 635, 0, 0, 0, 0,
            0, 0, 2977, 0, 32, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 855, 0,
            0, 0, 0, 0, 54, 54, 855, 855, 0, 0,
            0, 0, 0, 0, 0, 6357, 60, 0, 0, 0,
            0, 0, 500, 0, 855, 0, 0, 1937, 151, 0,
            181, 0, 864, 0, 0, -159, 0, 0, 0, 10874,
            0, 428, 0, 32, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, -109, 0, 0, 0, 0, 0, 0,
            17077, 17211, 0, 0, 0, 0, 0, 0, 0, 10032,
            1327, 13600, 13730, 13840, 13120, 13250, 13926, 14201, 14029, 14115,
            1968, 14304, 12530, 12640, 10153, 12770, 10280, 10382, 12257, 12400,
            13360, 13490, 12880, 13010, 1156, 17077, 1802, 3698, 6134, 19554,
            0, 3829, 0, 867, 5630, 0, 5761, 4664, 0, 0,
            0, 0, 6003, 0, 7273, 0, 0, 0, 15815, 0,
            0, 0, 0, 855, 0, 555, 0, 0, 0, 0,
            1099, 0, 15094, 0, 0, 0, 0, 0, 966, 16266,
            16403, 0, 0, 867, 8356, 0, 855, 192, 966, 656,
            0, 0, 624, 556, 0, 960, 0, 2690, 4202, 867,
            2821, 3194, 15180, 960, 0, 0, 0, 0, 0, 0,
            0, 4489, 738, 867, 4951, 5382, 0, 0, 0, 0,
            15324, 11476, 0, 0, 0, 0, 106, 180, 0, 0,
            0, 855, 0, 0, 10520, 8203, 60, 36, 0, 855,
            863, 0, 1609, 5317, 867, 1624, 9177, 600, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 163, 0, 0, 0, 0, 0, 0, 552, 0,
            95, 0, 0, 0, 95, 95, 0, 0, 211, 0,
            0, 0, 680, 211, 39, 156, 0, 0, 0, 58,
            0, 966, 0, 60, 0, 966, 0, 0, 0, 15450,
            10688, 0, 0, 0, 0, 15486, 0, 0, 15642, 1499,
            79, 0, 0, 0, 0, 666, 0, 0, 17211, 0,
            0, 0, 0, 0, 0, 855, 0, 0, 0, 0,
            0, 692, 217, 0, 780, 960, 0, 0, 0, 0,
            0, 0, 6496, 0, 0, 0, 0, 0, 577, 105,
            105, 1080, 0, 0, 0, 0, 54, 0, 0, 0,
            0, 0, 0, 6662, 0, 0, 0, 0, 0, 0,
            0, 219, 0, 229, 0, 855, 17, 0, 0, 0,
            0, 966, 60, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 120, 58, 120, 0, 0,
            122, 120, 0, 0, 0, 122, 67, 93, -139, 0,
            0, 0, 0, 0, 9133, 0, 14390, 0, 0, 0,
            0, 14476, 0, 14574, 0, 0, 0, 14660, 0, 15678,
            774, 14746, 60, 966, 0, 0, 0, 0, 0, 1520,
            0, 0, 0, 0, 105, 0, 0, 0, 0, 0,
            0, 0, 960, 0, 0, 0, 204, 0, 0, 855,
            0, 231, 0, 0, 0, 95, 0, 0, 0, 0,
            95, 95, 0, 95, 0, 95, 0, 0, 211, 0,
            0, 0, 0, 134, 0, 0, 0, 0, 0, 0,
            0, 1678, 9445, 0, 96, 0, 0, 0, 0, 14834,
            0, 0, 14920, 15728, 0, 0, 966, 913, 1519, 1706,
            6572, 6818, 6877, 6880, 933, 7197, 7225, 995, 7267, 0,
            0, 7309, 0, 966, 0, 778, 0, 0, 0, 0,
            0, 105, 0, 0, 236, 0, 237, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 120, 0, 0, 120, 120, 0, 122, 120, 120,
            0, 0, 0, 117, 0, -61, 0, 0, 0, 0,
            0, 7339, 0, 32, 0, 601, 960, 0, 29, 0,
            248, 0, 250, 95, 95, 95, 95, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 11257, 1167, 0,
            0, 0, 1681, 916, 867, 6242, 6245, 0, 0, 0,
            0, 253, 0, 0, 0, 0, 0, 0, 120, 120,
            120, 120, 0, 918, 0, 258, 95, 0, 0, 0,
            0, 0, 0, 120, 0,
    }, yyGindex = {
            //yyGindex 188
            0, 0, 8, 0, -325, 0, -82, 4, 38, -267,
            0, 0, 0, 361, 0, 0, 0, 1169, 0, 0,
            952, 1186, 0, 1862, 0, 0, 0, 870, 0, 16,
            1245, -371, -31, 0, 104, 0, 123, -422, 0, 23,
            1344, 1525, 46, 18, 743, 64, 3, -504, 0, 234,
            0, 338, 0, 51, 0, -5, 1249, 659, 0, 0,
            -736, 0, 0, 483, -489, 0, 0, 0, -467, 373,
            -321, -70, -17, 819, -445, 0, 0, 646, 872, 66,
            0, 0, 4789, 437, -186, 0, 0, 0, 0, 220,
            1352, 527, -355, 431, 337, 0, 0, 0, 52, -459,
            0, -449, 340, -289, -425, 0, -117, 5758, -73, 517,
            -571, 1273, -14, 255, 710, 0, -23, -709, 0, -561,
            0, 0, -206, -806, 0, -387, -859, 468, 238, 87,
            -534, 0, -835, -384, 0, 33, 0, 76, 695, -79,
            0, -439, 240, 0, 20, -42, 0, 0, 0, -26,
            0, 0, -227, 0, 0, 0, 0, 0, -226, 0,
            -433, 0, 0, 0, 0, 0, 0, 44, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
    };
    protected static final short[] yyTable = YyTables.yyTable();
    protected static final short[] yyCheck = YyTables.yyCheck();

    /** maps symbol value to printable name.
      @see #yyExpecting
    */
    protected static final String[] yyNames = {
            "end-of-file", null, null, null, null, null, null, null, null, null, "'\\n'",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, "' '", null, null, null, null, null,
            null, null, null, null, null, null, "','", null, null, null, null, null, null,
            null, null, null, null, null, null, null, "':'", "';'", null, "'='", null, "'?'",
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null,
            "'['", null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null, null,
            "kCLASS", "kMODULE", "kDEF", "kUNDEF", "kBEGIN", "kRESCUE", "kENSURE",
            "kEND", "kIF", "kUNLESS", "kTHEN", "kELSIF", "kELSE", "kCASE", "kWHEN",
            "kWHILE", "kUNTIL", "kFOR", "kBREAK", "kNEXT", "kREDO", "kRETRY", "kIN",
            "kDO", "kDO_COND", "kDO_BLOCK", "kRETURN", "kYIELD", "kSUPER", "kSELF",
            "kNIL", "kTRUE", "kFALSE", "kAND", "kOR", "kNOT", "kIF_MOD", "kUNLESS_MOD",
            "kWHILE_MOD", "kUNTIL_MOD", "kRESCUE_MOD", "kALIAS", "kDEFINED", "klBEGIN",
            "klEND", "k__LINE__", "k__FILE__", "k__ENCODING__", "kDO_LAMBDA",
            "tIDENTIFIER", "tFID", "tGVAR", "tIVAR", "tCONSTANT", "tCVAR", "tLABEL",
            "tCHAR", "tUPLUS", "tUMINUS", "tUMINUS_NUM", "tPOW", "tCMP", "tEQ", "tEQQ",
            "tNEQ", "tGEQ", "tLEQ", "tANDOP", "tOROP", "tMATCH", "tNMATCH", "tDOT",
            "tDOT2", "tDOT3", "tAREF", "tASET", "tLSHFT", "tRSHFT", "tANDDOT", "tCOLON2",
            "tCOLON3", "tOP_ASGN", "tASSOC", "tLPAREN", "tLPAREN2", "tRPAREN",
            "tLPAREN_ARG", "tLBRACK", "tRBRACK", "tLBRACE", "tLBRACE_ARG", "tSTAR",
            "tSTAR2", "tAMPER", "tAMPER2", "tTILDE", "tPERCENT", "tDIVIDE", "tPLUS",
            "tMINUS", "tLT", "tGT", "tPIPE", "tBANG", "tCARET", "tLCURLY", "tRCURLY",
            "tBACK_REF2", "tSYMBEG", "tSTRING_BEG", "tXSTRING_BEG", "tREGEXP_BEG",
            "tWORDS_BEG", "tQWORDS_BEG", "tSTRING_DBEG", "tSTRING_DVAR",
            "tSTRING_END", "tLAMBDA", "tLAMBEG", "tNTH_REF", "tBACK_REF",
            "tSTRING_CONTENT", "tINTEGER", "tIMAGINARY", "tFLOAT", "tRATIONAL",
            "tREGEXP_END", "tSYMBOLS_BEG", "tQSYMBOLS_BEG", "tDSTAR", "tSTRING_DEND",
            "tLABEL_END", "tLOWEST",
    };


    /** computes list of expected tokens on error by tracing the tables.
      @param state for which to compute the list.
      @return list of token names.
    */
    protected String[] yyExpecting(int state) {
        int token, n, len = 0;
        boolean[] ok = new boolean[yyNames.length];

        if ((n = yySindex[state]) != 0)
            for (token = n < 0 ? -n : 0; token < yyNames.length && n + token < yyTable.length; ++token)
                if (yyCheck[n + token] == token && !ok[token] && yyNames[token] != null) {
                    ++len;
                    ok[token] = true;
                }
        if ((n = yyRindex[state]) != 0)
            for (token = n < 0 ? -n : 0; token < yyNames.length && n + token < yyTable.length; ++token)
                if (yyCheck[n + token] == token && !ok[token] && yyNames[token] != null) {
                    ++len;
                    ok[token] = true;
                }

        String result[] = new String[len];
        for (n = token = 0; n < len; ++token)
            if (ok[token])
                result[n++] = yyNames[token];
        return result;
    }

    /** the generated parser, with debugging messages.
      Maintains a dynamic state and value stack.
      @param yyLex scanner.
      @return result of the last reduction, if any.
    */
    public Object yyparse(RubyLexer yyLex, Object ayydebug) {
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
    protected Object yyDefault(Object first) {
        return first;
    }

    /** the generated parser.
      Maintains a dynamic state and value stack.
      @param yyLex scanner.
      @return result of the last reduction, if any.
    */
    public Object yyparse(RubyLexer yyLex) {
        if (yyMax <= 0)
            yyMax = 256;			// initial size
        int yyState = 0, yyStates[] = new int[yyMax];	// state stack
        Object yyVal = null, yyVals[] = new Object[yyMax];	// value stack
        int yyToken = -1;					// current input
        int yyErrorFlag = 0;				// #tokens to shift

        yyLoop: for (int yyTop = 0;; ++yyTop) {
            if (yyTop >= yyStates.length) {			// dynamically increase
                int[] i = new int[yyStates.length + yyMax];
                System.arraycopy(yyStates, 0, i, 0, yyStates.length);
                yyStates = i;
                Object[] o = new Object[yyVals.length + yyMax];
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
                    if ((yyN = yySindex[yyState]) != 0 && (yyN += yyToken) >= 0 && yyN < yyTable.length && yyCheck[yyN] == yyToken) {
                        yyState = yyTable[yyN];		// shift to yyN
                        yyVal = yyLex.value();
                        yyToken = -1;
                        if (yyErrorFlag > 0)
                            --yyErrorFlag;
                        continue yyLoop;
                    }
                    if ((yyN = yyRindex[yyState]) != 0 && (yyN += yyToken) >= 0 && yyN < yyTable.length && yyCheck[yyN] == yyToken)
                        yyN = yyTable[yyN];			// reduce (yyN)
                    else
                        switch (yyErrorFlag) {

                            case 0:
                                support.yyerror("syntax error", yyExpecting(yyState), yyNames[yyToken]);

                            case 1:
                            case 2:
                                yyErrorFlag = 3;
                                do {
                                    if ((yyN = yySindex[yyStates[yyTop]]) != 0 && (yyN += yyErrorCode) >= 0 && yyN < yyTable.length && yyCheck[yyN] == yyErrorCode) {
                                        yyState = yyTable[yyN];
                                        yyVal = yyLex.value();
                                        continue yyLoop;
                                    }
                                } while (--yyTop >= 0);
                                support.yyerror("irrecoverable syntax error");

                            case 3:
                                if (yyToken == 0) {
                                    support.yyerror("irrecoverable syntax error at end-of-file");
                                }
                                yyToken = -1;
                                continue yyDiscarded;		// leave stack alone
                        }
                }
                int yyV = yyTop + 1 - yyLen[yyN];
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
                if ((yyN = yyGindex[yyM]) != 0 && (yyN += yyState) >= 0 && yyN < yyTable.length && yyCheck[yyN] == yyState)
                    yyState = yyTable[yyN];
                else
                    yyState = yyDgoto[yyM];
                continue yyLoop;
            }
        }
    }

    static ParserState[] states = new ParserState[645];
    static {
        states[1] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_BEG);
            support.initTopLocalVariables();
            return yyVal;
        };
        states[2] = (support, lexer, yyVal, yyVals, yyTop) -> {
            /* ENEBO: Removed !compile_for_eval which probably is to reduce warnings*/
            if (((ParseNode) yyVals[0 + yyTop]) != null) {
                /* last expression should not be void */
                if (((ParseNode) yyVals[0 + yyTop]) instanceof BlockParseNode) {
                    support.checkUselessStatement(((BlockParseNode) yyVals[0 + yyTop]).getLast());
                } else {
                    support.checkUselessStatement(((ParseNode) yyVals[0 + yyTop]));
                }
            }
            support.getResult().setAST(support.addRootNode(((ParseNode) yyVals[0 + yyTop])));
            return yyVal;
        };
        states[3] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (((ParseNode) yyVals[-1 + yyTop]) instanceof BlockParseNode) {
                support.checkUselessStatements(((BlockParseNode) yyVals[-1 + yyTop]));
            }
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[5] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newline_node(((ParseNode) yyVals[0 + yyTop]), support.getPosition(((ParseNode) yyVals[0 + yyTop])));
            return yyVal;
        };
        states[6] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.appendToBlock(((ParseNode) yyVals[-2 + yyTop]), support.newline_node(((ParseNode) yyVals[0 + yyTop]), support.getPosition(((ParseNode) yyVals[0 + yyTop]))));
            return yyVal;
        };
        states[7] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[9] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle()) {
                support.yyerror("BEGIN in method");
            }
            return yyVal;
        };
        states[10] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.getResult().addBeginNode(new PreExe19ParseNode(((SourceIndexLength) yyVals[-4 + yyTop]), support.getCurrentScope(), ((ParseNode) yyVals[-1 + yyTop])));
            yyVal = null;
            return yyVal;
        };
        states[11] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode node = ((ParseNode) yyVals[-3 + yyTop]);

            if (((RescueBodyParseNode) yyVals[-2 + yyTop]) != null) {
                node = new RescueParseNode(support.getPosition(((ParseNode) yyVals[-3 + yyTop])), ((ParseNode) yyVals[-3 + yyTop]), ((RescueBodyParseNode) yyVals[-2 + yyTop]),
                        ((ParseNode) yyVals[-1 + yyTop]));
            } else if (((ParseNode) yyVals[-1 + yyTop]) != null) {
                support.warn(support.getPosition(((ParseNode) yyVals[-3 + yyTop])), "else without rescue is useless");
                node = support.appendToBlock(((ParseNode) yyVals[-3 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]));
            }
            if (((ParseNode) yyVals[0 + yyTop]) != null) {
                node = new EnsureParseNode(support.getPosition(((ParseNode) yyVals[-3 + yyTop])), support.makeNullNil(node), ((ParseNode) yyVals[0 + yyTop]));
            }

            support.fixpos(node, ((ParseNode) yyVals[-3 + yyTop]));
            yyVal = node;
            return yyVal;
        };
        states[12] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (((ParseNode) yyVals[-1 + yyTop]) instanceof BlockParseNode) {
                support.checkUselessStatements(((BlockParseNode) yyVals[-1 + yyTop]));
            }
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[14] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newline_node(((ParseNode) yyVals[0 + yyTop]), support.getPosition(((ParseNode) yyVals[0 + yyTop])));
            return yyVal;
        };
        states[15] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.appendToBlock(((ParseNode) yyVals[-2 + yyTop]), support.newline_node(((ParseNode) yyVals[0 + yyTop]), support.getPosition(((ParseNode) yyVals[0 + yyTop]))));
            return yyVal;
        };
        states[16] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[17] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[18] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.yyerror("BEGIN is permitted only at toplevel");
            return yyVal;
        };
        states[19] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new BeginParseNode(((SourceIndexLength) yyVals[-4 + yyTop]), support.makeNullNil(((ParseNode) yyVals[-3 + yyTop])));
            return yyVal;
        };
        states[20] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_FNAME | EXPR_FITEM);
            return yyVal;
        };
        states[21] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newAlias(((SourceIndexLength) yyVals[-3 + yyTop]), ((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[22] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new VAliasParseNode(((SourceIndexLength) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[23] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new VAliasParseNode(((SourceIndexLength) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), "$" + ((BackRefParseNode) yyVals[0 + yyTop]).getType());
            return yyVal;
        };
        states[24] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.yyerror("can't make alias for the number variables");
            return yyVal;
        };
        states[25] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[26] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IfParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), support.getConditionNode(((ParseNode) yyVals[0 + yyTop])), ((ParseNode) yyVals[-2 + yyTop]), null);
            support.fixpos(((ParseNode) yyVal), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[27] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IfParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), support.getConditionNode(((ParseNode) yyVals[0 + yyTop])), null, ((ParseNode) yyVals[-2 + yyTop]));
            support.fixpos(((ParseNode) yyVal), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[28] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (((ParseNode) yyVals[-2 + yyTop]) != null && ((ParseNode) yyVals[-2 + yyTop]) instanceof BeginParseNode) {
                yyVal = new WhileParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), support.getConditionNode(((ParseNode) yyVals[0 + yyTop])),
                        ((BeginParseNode) yyVals[-2 + yyTop]).getBodyNode(), false);
            } else {
                yyVal = new WhileParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), support.getConditionNode(((ParseNode) yyVals[0 + yyTop])), ((ParseNode) yyVals[-2 + yyTop]), true);
            }
            return yyVal;
        };
        states[29] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (((ParseNode) yyVals[-2 + yyTop]) != null && ((ParseNode) yyVals[-2 + yyTop]) instanceof BeginParseNode) {
                yyVal = new UntilParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), support.getConditionNode(((ParseNode) yyVals[0 + yyTop])),
                        ((BeginParseNode) yyVals[-2 + yyTop]).getBodyNode(), false);
            } else {
                yyVal = new UntilParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), support.getConditionNode(((ParseNode) yyVals[0 + yyTop])), ((ParseNode) yyVals[-2 + yyTop]), true);
            }
            return yyVal;
        };
        states[30] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newRescueModNode(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[31] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle()) {
                support.warn(((SourceIndexLength) yyVals[-3 + yyTop]), "END in method; use at_exit");
            }
            yyVal = new PostExeParseNode(((SourceIndexLength) yyVals[-3 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[33] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));
            ((MultipleAsgnParseNode) yyVals[-2 + yyTop]).setValueNode(((ParseNode) yyVals[0 + yyTop]));
            yyVal = ((MultipleAsgnParseNode) yyVals[-2 + yyTop]);
            return yyVal;
        };
        states[34] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));

            SourceIndexLength pos = ((AssignableParseNode) yyVals[-2 + yyTop]).getPosition();
            String asgnOp = ((String) yyVals[-1 + yyTop]);
            if (asgnOp.equals("||")) {
                ((AssignableParseNode) yyVals[-2 + yyTop]).setValueNode(((ParseNode) yyVals[0 + yyTop]));
                yyVal = new OpAsgnOrParseNode(pos, support.gettable2(((AssignableParseNode) yyVals[-2 + yyTop])), ((AssignableParseNode) yyVals[-2 + yyTop]));
            } else if (asgnOp.equals("&&")) {
                ((AssignableParseNode) yyVals[-2 + yyTop]).setValueNode(((ParseNode) yyVals[0 + yyTop]));
                yyVal = new OpAsgnAndParseNode(pos, support.gettable2(((AssignableParseNode) yyVals[-2 + yyTop])), ((AssignableParseNode) yyVals[-2 + yyTop]));
            } else {
                ((AssignableParseNode) yyVals[-2 + yyTop]).setValueNode(
                        support.getOperatorCallNode(support.gettable2(((AssignableParseNode) yyVals[-2 + yyTop])), asgnOp, ((ParseNode) yyVals[0 + yyTop])));
                ((AssignableParseNode) yyVals[-2 + yyTop]).setPosition(pos);
                yyVal = ((AssignableParseNode) yyVals[-2 + yyTop]);
            }
            return yyVal;
        };
        states[35] = (support, lexer, yyVal, yyVals, yyTop) -> {
            /* FIXME: arg_concat logic missing for opt_call_args*/
            yyVal = support.new_opElementAsgnNode(((ParseNode) yyVals[-5 + yyTop]), ((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[36] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newOpAsgn(support.getPosition(((ParseNode) yyVals[-4 + yyTop])), ((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]),
                    ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[37] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newOpAsgn(support.getPosition(((ParseNode) yyVals[-4 + yyTop])), ((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]),
                    ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[38] = (support, lexer, yyVal, yyVals, yyTop) -> {
            SourceIndexLength pos = ((ParseNode) yyVals[-4 + yyTop]).getPosition();
            yyVal = support.newOpConstAsgn(pos, support.new_colon2(pos, ((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop])), ((String) yyVals[-1 + yyTop]),
                    ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[39] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newOpAsgn(support.getPosition(((ParseNode) yyVals[-4 + yyTop])), ((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]),
                    ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[40] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.backrefAssignError(((ParseNode) yyVals[-2 + yyTop]));
            return yyVal;
        };
        states[41] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.node_assign(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[42] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ((AssignableParseNode) yyVals[-2 + yyTop]).setValueNode(((ParseNode) yyVals[0 + yyTop]));
            yyVal = ((MultipleAsgnParseNode) yyVals[-2 + yyTop]);
            ((MultipleAsgnParseNode) yyVals[-2 + yyTop]).setPosition(support.getPosition(((MultipleAsgnParseNode) yyVals[-2 + yyTop])));
            return yyVal;
        };
        states[44] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));
            yyVal = support.node_assign(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[45] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));
            yyVal = support.node_assign(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[47] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newAndNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), ((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[48] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newOrNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), ((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[49] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode) yyVals[0 + yyTop])), "!");
            return yyVal;
        };
        states[50] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode) yyVals[0 + yyTop])), "!");
            return yyVal;
        };
        states[52] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[56] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-3 + yyTop]), ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[57] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.pushBlockScope();
            return yyVal;
        };
        states[58] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IterParseNode(((SourceIndexLength) yyVals[-4 + yyTop]), ((ArgsParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]), support.getCurrentScope());
            support.popCurrentScope();
            return yyVal;
        };
        states[59] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_fcall(((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[60] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.frobnicate_fcall_args(((FCallParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), null);
            yyVal = ((FCallParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[61] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.frobnicate_fcall_args(((FCallParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]), ((IterParseNode) yyVals[0 + yyTop]));
            yyVal = ((FCallParseNode) yyVals[-2 + yyTop]);
            return yyVal;
        };
        states[62] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-3 + yyTop]), ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[63] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop]), ((String) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]),
                    ((IterParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[64] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-3 + yyTop]), ((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[65] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]), ((IterParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[66] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_super(((SourceIndexLength) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[67] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_yield(((SourceIndexLength) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[68] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ReturnParseNode(((SourceIndexLength) yyVals[-1 + yyTop]), support.ret_args(((ParseNode) yyVals[0 + yyTop]), ((SourceIndexLength) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[69] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new BreakParseNode(((SourceIndexLength) yyVals[-1 + yyTop]), support.ret_args(((ParseNode) yyVals[0 + yyTop]), ((SourceIndexLength) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[70] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new NextParseNode(((SourceIndexLength) yyVals[-1 + yyTop]), support.ret_args(((ParseNode) yyVals[0 + yyTop]), ((SourceIndexLength) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[72] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[73] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((MultipleAsgnParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[74] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((SourceIndexLength) yyVals[-2 + yyTop]), support.newArrayNode(((SourceIndexLength) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop])), null, null);
            return yyVal;
        };
        states[75] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[0 + yyTop]).getPosition(), ((ListParseNode) yyVals[0 + yyTop]), null, null);
            return yyVal;
        };
        states[76] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[-1 + yyTop]).getPosition(), ((ListParseNode) yyVals[-1 + yyTop]).add(((ParseNode) yyVals[0 + yyTop])), null, null);
            return yyVal;
        };
        states[77] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[-2 + yyTop]).getPosition(), ((ListParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), (ListParseNode) null);
            return yyVal;
        };
        states[78] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[-4 + yyTop]).getPosition(), ((ListParseNode) yyVals[-4 + yyTop]), ((ParseNode) yyVals[-2 + yyTop]),
                    ((ListParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[79] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[-1 + yyTop]).getPosition(), ((ListParseNode) yyVals[-1 + yyTop]), new StarParseNode(lexer.getPosition()), null);
            return yyVal;
        };
        states[80] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), ((ListParseNode) yyVals[-3 + yyTop]), new StarParseNode(lexer.getPosition()),
                    ((ListParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[81] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ParseNode) yyVals[0 + yyTop]).getPosition(), null, ((ParseNode) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[82] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ParseNode) yyVals[-2 + yyTop]).getPosition(), null, ((ParseNode) yyVals[-2 + yyTop]), ((ListParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[83] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), null);
            return yyVal;
        };
        states[84] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), ((ListParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[86] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[87] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newArrayNode(((ParseNode) yyVals[-1 + yyTop]).getPosition(), ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[88] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]).add(((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[89] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newArrayNode(((ParseNode) yyVals[0 + yyTop]).getPosition(), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[90] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]).add(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[91] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.assignableLabelOrIdentifier(((String) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[92] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new InstAsgnParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[93] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new GlobalAsgnParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[94] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle())
                support.compile_error("dynamic constant assignment");

            yyVal = new ConstDeclParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), null, NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[95] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ClassVarAsgnParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[96] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to nil");
            yyVal = null;
            return yyVal;
        };
        states[97] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't change the value of self");
            yyVal = null;
            return yyVal;
        };
        states[98] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to true");
            yyVal = null;
            return yyVal;
        };
        states[99] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to false");
            yyVal = null;
            return yyVal;
        };
        states[100] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to __FILE__");
            yyVal = null;
            return yyVal;
        };
        states[101] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to __LINE__");
            yyVal = null;
            return yyVal;
        };
        states[102] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to __ENCODING__");
            yyVal = null;
            return yyVal;
        };
        states[103] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.aryset(((ParseNode) yyVals[-3 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[104] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.attrset(((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[105] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.attrset(((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[106] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.attrset(((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[107] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle()) {
                support.yyerror("dynamic constant assignment");
            }

            SourceIndexLength position = support.getPosition(((ParseNode) yyVals[-2 + yyTop]));

            yyVal = new ConstDeclParseNode(position, null, support.new_colon2(position, ((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[0 + yyTop])), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[108] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle()) {
                support.yyerror("dynamic constant assignment");
            }

            SourceIndexLength position = lexer.getPosition();

            yyVal = new ConstDeclParseNode(position, null, support.new_colon3(position, ((String) yyVals[0 + yyTop])), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[109] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.backrefAssignError(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[110] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.assignableLabelOrIdentifier(((String) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[111] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new InstAsgnParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[112] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new GlobalAsgnParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[113] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle())
                support.compile_error("dynamic constant assignment");

            yyVal = new ConstDeclParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), null, NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[114] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ClassVarAsgnParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[115] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to nil");
            yyVal = null;
            return yyVal;
        };
        states[116] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't change the value of self");
            yyVal = null;
            return yyVal;
        };
        states[117] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to true");
            yyVal = null;
            return yyVal;
        };
        states[118] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to false");
            yyVal = null;
            return yyVal;
        };
        states[119] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to __FILE__");
            yyVal = null;
            return yyVal;
        };
        states[120] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to __LINE__");
            yyVal = null;
            return yyVal;
        };
        states[121] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to __ENCODING__");
            yyVal = null;
            return yyVal;
        };
        states[122] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.aryset(((ParseNode) yyVals[-3 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[123] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.attrset(((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[124] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.attrset(((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[125] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.attrset(((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[126] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle()) {
                support.yyerror("dynamic constant assignment");
            }

            SourceIndexLength position = support.getPosition(((ParseNode) yyVals[-2 + yyTop]));

            yyVal = new ConstDeclParseNode(position, null, support.new_colon2(position, ((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[0 + yyTop])), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[127] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle()) {
                support.yyerror("dynamic constant assignment");
            }

            SourceIndexLength position = lexer.getPosition();

            yyVal = new ConstDeclParseNode(position, null, support.new_colon3(position, ((String) yyVals[0 + yyTop])), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[128] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.backrefAssignError(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[129] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.yyerror("class/module name must be CONSTANT");
            return yyVal;
        };
        states[131] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_colon3(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[132] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_colon2(lexer.getPosition(), null, ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[133] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_colon2(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), ((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[137] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_ENDFN);
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[138] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_ENDFN);
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[139] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new LiteralParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[140] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new LiteralParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[141] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((LiteralParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[142] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[143] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newUndef(((ParseNode) yyVals[0 + yyTop]).getPosition(), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[144] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_FNAME | EXPR_FITEM);
            return yyVal;
        };
        states[145] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.appendToBlock(((ParseNode) yyVals[-3 + yyTop]), support.newUndef(((ParseNode) yyVals[-3 + yyTop]).getPosition(), ((ParseNode) yyVals[0 + yyTop])));
            return yyVal;
        };
        states[176] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "__LINE__";
            return yyVal;
        };
        states[177] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "__FILE__";
            return yyVal;
        };
        states[178] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "__ENCODING__";
            return yyVal;
        };
        states[179] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "BEGIN";
            return yyVal;
        };
        states[180] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "END";
            return yyVal;
        };
        states[181] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "alias";
            return yyVal;
        };
        states[182] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "and";
            return yyVal;
        };
        states[183] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "begin";
            return yyVal;
        };
        states[184] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "break";
            return yyVal;
        };
        states[185] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "case";
            return yyVal;
        };
        states[186] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "class";
            return yyVal;
        };
        states[187] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "def";
            return yyVal;
        };
        states[188] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "defined?";
            return yyVal;
        };
        states[189] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "do";
            return yyVal;
        };
        states[190] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "else";
            return yyVal;
        };
        states[191] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "elsif";
            return yyVal;
        };
        states[192] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "end";
            return yyVal;
        };
        states[193] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "ensure";
            return yyVal;
        };
        states[194] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "false";
            return yyVal;
        };
        states[195] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "for";
            return yyVal;
        };
        states[196] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "in";
            return yyVal;
        };
        states[197] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "module";
            return yyVal;
        };
        states[198] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "next";
            return yyVal;
        };
        states[199] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "nil";
            return yyVal;
        };
        states[200] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "not";
            return yyVal;
        };
        states[201] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "or";
            return yyVal;
        };
        states[202] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "redo";
            return yyVal;
        };
        states[203] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "rescue";
            return yyVal;
        };
        states[204] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "retry";
            return yyVal;
        };
        states[205] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "return";
            return yyVal;
        };
        states[206] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "self";
            return yyVal;
        };
        states[207] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "super";
            return yyVal;
        };
        states[208] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "then";
            return yyVal;
        };
        states[209] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "true";
            return yyVal;
        };
        states[210] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "undef";
            return yyVal;
        };
        states[211] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "when";
            return yyVal;
        };
        states[212] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "yield";
            return yyVal;
        };
        states[213] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "if";
            return yyVal;
        };
        states[214] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "unless";
            return yyVal;
        };
        states[215] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "while";
            return yyVal;
        };
        states[216] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "until";
            return yyVal;
        };
        states[217] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "rescue";
            return yyVal;
        };
        states[218] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.node_assign(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            /* FIXME: Consider fixing node_assign itself rather than single case*/
            ((ParseNode) yyVal).setPosition(support.getPosition(((ParseNode) yyVals[-2 + yyTop])));
            return yyVal;
        };
        states[219] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.node_assign(((ParseNode) yyVals[-4 + yyTop]), support.newRescueModNode(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop])));
            return yyVal;
        };
        states[220] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));

            SourceIndexLength pos = ((AssignableParseNode) yyVals[-2 + yyTop]).getPosition();
            String asgnOp = ((String) yyVals[-1 + yyTop]);
            if (asgnOp.equals("||")) {
                ((AssignableParseNode) yyVals[-2 + yyTop]).setValueNode(((ParseNode) yyVals[0 + yyTop]));
                yyVal = new OpAsgnOrParseNode(pos, support.gettable2(((AssignableParseNode) yyVals[-2 + yyTop])), ((AssignableParseNode) yyVals[-2 + yyTop]));
            } else if (asgnOp.equals("&&")) {
                ((AssignableParseNode) yyVals[-2 + yyTop]).setValueNode(((ParseNode) yyVals[0 + yyTop]));
                yyVal = new OpAsgnAndParseNode(pos, support.gettable2(((AssignableParseNode) yyVals[-2 + yyTop])), ((AssignableParseNode) yyVals[-2 + yyTop]));
            } else {
                ((AssignableParseNode) yyVals[-2 + yyTop]).setValueNode(
                        support.getOperatorCallNode(support.gettable2(((AssignableParseNode) yyVals[-2 + yyTop])), asgnOp, ((ParseNode) yyVals[0 + yyTop])));
                ((AssignableParseNode) yyVals[-2 + yyTop]).setPosition(pos);
                yyVal = ((AssignableParseNode) yyVals[-2 + yyTop]);
            }
            return yyVal;
        };
        states[221] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[-2 + yyTop]));
            ParseNode rescue = support.newRescueModNode(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));

            SourceIndexLength pos = ((AssignableParseNode) yyVals[-4 + yyTop]).getPosition();
            String asgnOp = ((String) yyVals[-3 + yyTop]);
            if (asgnOp.equals("||")) {
                ((AssignableParseNode) yyVals[-4 + yyTop]).setValueNode(rescue);
                yyVal = new OpAsgnOrParseNode(pos, support.gettable2(((AssignableParseNode) yyVals[-4 + yyTop])), ((AssignableParseNode) yyVals[-4 + yyTop]));
            } else if (asgnOp.equals("&&")) {
                ((AssignableParseNode) yyVals[-4 + yyTop]).setValueNode(rescue);
                yyVal = new OpAsgnAndParseNode(pos, support.gettable2(((AssignableParseNode) yyVals[-4 + yyTop])), ((AssignableParseNode) yyVals[-4 + yyTop]));
            } else {
                ((AssignableParseNode) yyVals[-4 + yyTop]).setValueNode(support.getOperatorCallNode(support.gettable2(((AssignableParseNode) yyVals[-4 + yyTop])), asgnOp, rescue));
                ((AssignableParseNode) yyVals[-4 + yyTop]).setPosition(pos);
                yyVal = ((AssignableParseNode) yyVals[-4 + yyTop]);
            }
            return yyVal;
        };
        states[222] = (support, lexer, yyVal, yyVals, yyTop) -> {
            /* FIXME: arg_concat missing for opt_call_args*/
            yyVal = support.new_opElementAsgnNode(((ParseNode) yyVals[-5 + yyTop]), ((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[223] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newOpAsgn(support.getPosition(((ParseNode) yyVals[-4 + yyTop])), ((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]),
                    ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[224] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newOpAsgn(support.getPosition(((ParseNode) yyVals[-4 + yyTop])), ((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]),
                    ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[225] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newOpAsgn(support.getPosition(((ParseNode) yyVals[-4 + yyTop])), ((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]),
                    ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[226] = (support, lexer, yyVal, yyVals, yyTop) -> {
            SourceIndexLength pos = support.getPosition(((ParseNode) yyVals[-4 + yyTop]));
            yyVal = support.newOpConstAsgn(pos, support.new_colon2(pos, ((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-2 + yyTop])), ((String) yyVals[-1 + yyTop]),
                    ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[227] = (support, lexer, yyVal, yyVals, yyTop) -> {
            SourceIndexLength pos = lexer.getPosition();
            yyVal = support.newOpConstAsgn(pos, new Colon3ParseNode(pos, ((String) yyVals[-2 + yyTop])), ((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[228] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.backrefAssignError(((ParseNode) yyVals[-2 + yyTop]));
            return yyVal;
        };
        states[229] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[-2 + yyTop]));
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));

            boolean isLiteral = ((ParseNode) yyVals[-2 + yyTop]) instanceof FixnumParseNode && ((ParseNode) yyVals[0 + yyTop]) instanceof FixnumParseNode;
            yyVal = new DotParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), support.makeNullNil(((ParseNode) yyVals[-2 + yyTop])), support.makeNullNil(((ParseNode) yyVals[0 + yyTop])),
                    false, isLiteral);
            return yyVal;
        };
        states[230] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[-2 + yyTop]));
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));

            boolean isLiteral = ((ParseNode) yyVals[-2 + yyTop]) instanceof FixnumParseNode && ((ParseNode) yyVals[0 + yyTop]) instanceof FixnumParseNode;
            yyVal = new DotParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), support.makeNullNil(((ParseNode) yyVals[-2 + yyTop])), support.makeNullNil(((ParseNode) yyVals[0 + yyTop])),
                    true, isLiteral);
            return yyVal;
        };
        states[231] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "+", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[232] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "-", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[233] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "*", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[234] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "/", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[235] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "%", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[236] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "**", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[237] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(support.getOperatorCallNode(((NumericParseNode) yyVals[-2 + yyTop]), "**", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition()), "-@");
            return yyVal;
        };
        states[238] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[0 + yyTop]), "+@");
            return yyVal;
        };
        states[239] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[0 + yyTop]), "-@");
            return yyVal;
        };
        states[240] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "|", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[241] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "^", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[242] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "&", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[243] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "<=>", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[244] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), ">", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[245] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), ">=", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[246] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "<", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[247] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "<=", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[248] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "==", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[249] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "===", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[250] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "!=", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[251] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getMatchNode(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            /* ENEBO
                  $$ = match_op($1, $3);
                  if (nd_type($1) == NODE_LIT && TYPE($1->nd_lit) == T_REGEXP) {
            $$ = reg_named_capture_assign($1->nd_lit, $$);
                  }
            */
            return yyVal;
        };
        states[252] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "!~", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[253] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode) yyVals[0 + yyTop])), "!");
            return yyVal;
        };
        states[254] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[0 + yyTop]), "~");
            return yyVal;
        };
        states[255] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), "<<", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[256] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(((ParseNode) yyVals[-2 + yyTop]), ">>", ((ParseNode) yyVals[0 + yyTop]), lexer.getPosition());
            return yyVal;
        };
        states[257] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newAndNode(((ParseNode) yyVals[-2 + yyTop]).getPosition(), ((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[258] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newOrNode(((ParseNode) yyVals[-2 + yyTop]).getPosition(), ((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[259] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_defined(((SourceIndexLength) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[260] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IfParseNode(support.getPosition(((ParseNode) yyVals[-5 + yyTop])), support.getConditionNode(((ParseNode) yyVals[-5 + yyTop])), ((ParseNode) yyVals[-3 + yyTop]),
                    ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[261] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[262] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));
            yyVal = support.makeNullNil(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[264] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[265] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.arg_append(((ParseNode) yyVals[-3 + yyTop]), support.remove_duplicate_keys(((HashParseNode) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[266] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newArrayNode(((HashParseNode) yyVals[-1 + yyTop]).getPosition(), support.remove_duplicate_keys(((HashParseNode) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[267] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            if (yyVal != null)
                ((ParseNode) yyVal).setPosition(((SourceIndexLength) yyVals[-2 + yyTop]));
            return yyVal;
        };
        states[272] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[273] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.arg_append(((ParseNode) yyVals[-3 + yyTop]), support.remove_duplicate_keys(((HashParseNode) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[274] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newArrayNode(((HashParseNode) yyVals[-1 + yyTop]).getPosition(), support.remove_duplicate_keys(((HashParseNode) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[275] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newArrayNode(support.getPosition(((ParseNode) yyVals[0 + yyTop])), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[276] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.arg_blk_pass(((ParseNode) yyVals[-1 + yyTop]), ((BlockPassParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[277] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newArrayNode(((HashParseNode) yyVals[-1 + yyTop]).getPosition(), support.remove_duplicate_keys(((HashParseNode) yyVals[-1 + yyTop])));
            yyVal = support.arg_blk_pass((ParseNode) yyVal, ((BlockPassParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[278] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.arg_append(((ParseNode) yyVals[-3 + yyTop]), support.remove_duplicate_keys(((HashParseNode) yyVals[-1 + yyTop])));
            yyVal = support.arg_blk_pass((ParseNode) yyVal, ((BlockPassParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[279] = (support, lexer, yyVal, yyVals, yyTop) -> yyVal;
        states[280] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getCmdArgumentState().getStack();
            lexer.getCmdArgumentState().begin();
            return yyVal;
        };
        states[281] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getCmdArgumentState().reset(((Long) yyVals[-1 + yyTop]).longValue());
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[282] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new BlockPassParseNode(support.getPosition(((ParseNode) yyVals[0 + yyTop])), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[283] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((BlockPassParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[285] = (support, lexer, yyVal, yyVals, yyTop) -> {
            SourceIndexLength pos = ((ParseNode) yyVals[0 + yyTop]) == null ? lexer.getPosition() : ((ParseNode) yyVals[0 + yyTop]).getPosition();
            yyVal = support.newArrayNode(pos, ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[286] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newSplatNode(support.getPosition(((ParseNode) yyVals[0 + yyTop])), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[287] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode node = support.splat_array(((ParseNode) yyVals[-2 + yyTop]));

            if (node != null) {
                yyVal = support.list_append(node, ((ParseNode) yyVals[0 + yyTop]));
            } else {
                yyVal = support.arg_append(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            }
            return yyVal;
        };
        states[288] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode node = null;

            /* FIXME: lose syntactical elements here (and others like this)*/
            if (((ParseNode) yyVals[0 + yyTop]) instanceof ArrayParseNode &&
                    (node = support.splat_array(((ParseNode) yyVals[-3 + yyTop]))) != null) {
                yyVal = support.list_concat(node, ((ParseNode) yyVals[0 + yyTop]));
            } else {
                yyVal = support.arg_concat(support.getPosition(((ParseNode) yyVals[-3 + yyTop])), ((ParseNode) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            }
            return yyVal;
        };
        states[289] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[290] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[291] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode node = support.splat_array(((ParseNode) yyVals[-2 + yyTop]));

            if (node != null) {
                yyVal = support.list_append(node, ((ParseNode) yyVals[0 + yyTop]));
            } else {
                yyVal = support.arg_append(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            }
            return yyVal;
        };
        states[292] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode node = null;

            if (((ParseNode) yyVals[0 + yyTop]) instanceof ArrayParseNode &&
                    (node = support.splat_array(((ParseNode) yyVals[-3 + yyTop]))) != null) {
                yyVal = support.list_concat(node, ((ParseNode) yyVals[0 + yyTop]));
            } else {
                yyVal = support.arg_concat(((ParseNode) yyVals[-3 + yyTop]).getPosition(), ((ParseNode) yyVals[-3 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            }
            return yyVal;
        };
        states[293] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newSplatNode(support.getPosition(((ParseNode) yyVals[0 + yyTop])), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[300] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[0 + yyTop]); /* FIXME: Why complaining without $$ = $1;*/
            return yyVal;
        };
        states[301] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[0 + yyTop]); /* FIXME: Why complaining without $$ = $1;*/
            return yyVal;
        };
        states[304] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_fcall(((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[305] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getCmdArgumentState().getStack();
            lexer.getCmdArgumentState().reset();
            return yyVal;
        };
        states[306] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getCmdArgumentState().reset(((Long) yyVals[-2 + yyTop]).longValue());
            yyVal = new BeginParseNode(((SourceIndexLength) yyVals[-3 + yyTop]), support.makeNullNil(((ParseNode) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[307] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_ENDARG);
            return yyVal;
        };
        states[308] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null; /*FIXME: Should be implicit nil?*/
            return yyVal;
        };
        states[309] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getCmdArgumentState().getStack();
            lexer.getCmdArgumentState().reset();
            return yyVal;
        };
        states[310] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_ENDARG);
            return yyVal;
        };
        states[311] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getCmdArgumentState().reset(((Long) yyVals[-3 + yyTop]).longValue());
            yyVal = ((ParseNode) yyVals[-2 + yyTop]);
            return yyVal;
        };
        states[312] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (((ParseNode) yyVals[-1 + yyTop]) != null) {
                /* compstmt position includes both parens around it*/
                ((ParseNode) yyVals[-1 + yyTop]).setPosition(((SourceIndexLength) yyVals[-2 + yyTop]));
                yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            } else {
                yyVal = new NilParseNode(((SourceIndexLength) yyVals[-2 + yyTop]));
            }
            return yyVal;
        };
        states[313] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_colon2(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), ((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[314] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_colon3(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[315] = (support, lexer, yyVal, yyVals, yyTop) -> {
            SourceIndexLength position = support.getPosition(((ParseNode) yyVals[-1 + yyTop]));
            if (((ParseNode) yyVals[-1 + yyTop]) == null) {
                yyVal = new ZArrayParseNode(position); /* zero length array */
            } else {
                yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            }
            return yyVal;
        };
        states[316] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((HashParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[317] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ReturnParseNode(((SourceIndexLength) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[318] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_yield(((SourceIndexLength) yyVals[-3 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[319] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new YieldParseNode(((SourceIndexLength) yyVals[-2 + yyTop]), null);
            return yyVal;
        };
        states[320] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new YieldParseNode(((SourceIndexLength) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[321] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_defined(((SourceIndexLength) yyVals[-4 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[322] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(support.getConditionNode(((ParseNode) yyVals[-1 + yyTop])), "!");
            return yyVal;
        };
        states[323] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.getOperatorCallNode(NilImplicitParseNode.NIL, "!");
            return yyVal;
        };
        states[324] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.frobnicate_fcall_args(((FCallParseNode) yyVals[-1 + yyTop]), null, ((IterParseNode) yyVals[0 + yyTop]));
            yyVal = ((FCallParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[326] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (((ParseNode) yyVals[-1 + yyTop]) != null &&
                    ((BlockAcceptingParseNode) yyVals[-1 + yyTop]).getIterNode() instanceof BlockPassParseNode) {
                lexer.compile_error(PID.BLOCK_ARG_AND_BLOCK_GIVEN, "Both block arg and actual block given.");
            }
            yyVal = ((BlockAcceptingParseNode) yyVals[-1 + yyTop]).setIterNode(((IterParseNode) yyVals[0 + yyTop]));
            ((ParseNode) yyVal).setPosition(((ParseNode) yyVals[-1 + yyTop]).getPosition());
            return yyVal;
        };
        states[327] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((LambdaParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[328] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IfParseNode(((SourceIndexLength) yyVals[-5 + yyTop]), support.getConditionNode(((ParseNode) yyVals[-4 + yyTop])), ((ParseNode) yyVals[-2 + yyTop]),
                    ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[329] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IfParseNode(((SourceIndexLength) yyVals[-5 + yyTop]), support.getConditionNode(((ParseNode) yyVals[-4 + yyTop])), ((ParseNode) yyVals[-1 + yyTop]),
                    ((ParseNode) yyVals[-2 + yyTop]));
            return yyVal;
        };
        states[330] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getConditionState().begin();
            return yyVal;
        };
        states[331] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getConditionState().end();
            return yyVal;
        };
        states[332] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode body = support.makeNullNil(((ParseNode) yyVals[-1 + yyTop]));
            yyVal = new WhileParseNode(((SourceIndexLength) yyVals[-6 + yyTop]), support.getConditionNode(((ParseNode) yyVals[-4 + yyTop])), body);
            return yyVal;
        };
        states[333] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getConditionState().begin();
            return yyVal;
        };
        states[334] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getConditionState().end();
            return yyVal;
        };
        states[335] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode body = support.makeNullNil(((ParseNode) yyVals[-1 + yyTop]));
            yyVal = new UntilParseNode(((SourceIndexLength) yyVals[-6 + yyTop]), support.getConditionNode(((ParseNode) yyVals[-4 + yyTop])), body);
            return yyVal;
        };
        states[336] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newCaseNode(((SourceIndexLength) yyVals[-4 + yyTop]), ((ParseNode) yyVals[-3 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[337] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newCaseNode(((SourceIndexLength) yyVals[-3 + yyTop]), null, ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[338] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getConditionState().begin();
            return yyVal;
        };
        states[339] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getConditionState().end();
            return yyVal;
        };
        states[340] = (support, lexer, yyVal, yyVals, yyTop) -> {
            /* ENEBO: Lots of optz in 1.9 parser here*/
            yyVal = new ForParseNode(((SourceIndexLength) yyVals[-8 + yyTop]), ((ParseNode) yyVals[-7 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[-4 + yyTop]),
                    support.getCurrentScope());
            return yyVal;
        };
        states[341] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle()) {
                support.yyerror("class definition in method body");
            }
            support.pushLocalScope();
            return yyVal;
        };
        states[342] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode body = support.makeNullNil(((ParseNode) yyVals[-1 + yyTop]));

            yyVal = new ClassParseNode(((SourceIndexLength) yyVals[-5 + yyTop]), ((Colon3ParseNode) yyVals[-4 + yyTop]), support.getCurrentScope(), body, ((ParseNode) yyVals[-3 + yyTop]));
            support.popCurrentScope();
            return yyVal;
        };
        states[343] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = Boolean.valueOf(support.isInDef());
            support.setInDef(false);
            return yyVal;
        };
        states[344] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = Integer.valueOf(support.getInSingle());
            support.setInSingle(0);
            support.pushLocalScope();
            return yyVal;
        };
        states[345] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode body = support.makeNullNil(((ParseNode) yyVals[-1 + yyTop]));

            yyVal = new SClassParseNode(((SourceIndexLength) yyVals[-7 + yyTop]), ((ParseNode) yyVals[-5 + yyTop]), support.getCurrentScope(), body);
            support.popCurrentScope();
            support.setInDef(((Boolean) yyVals[-4 + yyTop]).booleanValue());
            support.setInSingle(((Integer) yyVals[-2 + yyTop]).intValue());
            return yyVal;
        };
        states[346] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle()) {
                support.yyerror("module definition in method body");
            }
            support.pushLocalScope();
            return yyVal;
        };
        states[347] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode body = support.makeNullNil(((ParseNode) yyVals[-1 + yyTop]));

            yyVal = new ModuleParseNode(((SourceIndexLength) yyVals[-4 + yyTop]), ((Colon3ParseNode) yyVals[-3 + yyTop]), support.getCurrentScope(), body);
            support.popCurrentScope();
            return yyVal;
        };
        states[348] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.setInDef(true);
            support.pushLocalScope();
            yyVal = lexer.getCurrentArg();
            lexer.setCurrentArg(null);
            return yyVal;
        };
        states[349] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode body = support.makeNullNil(((ParseNode) yyVals[-1 + yyTop]));

            yyVal = new DefnParseNode(support.extendedUntil(((SourceIndexLength) yyVals[-5 + yyTop]), ((SourceIndexLength) yyVals[0 + yyTop])), ((String) yyVals[-4 + yyTop]),
                    (ArgsParseNode) yyVals[-2 + yyTop], support.getCurrentScope(), body);
            support.popCurrentScope();
            support.setInDef(false);
            lexer.setCurrentArg(((String) yyVals[-3 + yyTop]));
            return yyVal;
        };
        states[350] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_FNAME);
            return yyVal;
        };
        states[351] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.setInSingle(support.getInSingle() + 1);
            support.pushLocalScope();
            lexer.setState(EXPR_ENDFN | EXPR_LABEL); /* force for args */
            yyVal = lexer.getCurrentArg();
            lexer.setCurrentArg(null);
            return yyVal;
        };
        states[352] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode body = ((ParseNode) yyVals[-1 + yyTop]);
            if (body == null)
                body = NilImplicitParseNode.NIL;

            yyVal = new DefsParseNode(support.extendedUntil(((SourceIndexLength) yyVals[-8 + yyTop]), ((SourceIndexLength) yyVals[0 + yyTop])), ((ParseNode) yyVals[-7 + yyTop]),
                    ((String) yyVals[-4 + yyTop]), (ArgsParseNode) yyVals[-2 + yyTop], support.getCurrentScope(), body);
            support.popCurrentScope();
            support.setInSingle(support.getInSingle() - 1);
            lexer.setCurrentArg(((String) yyVals[-3 + yyTop]));
            return yyVal;
        };
        states[353] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new BreakParseNode(((SourceIndexLength) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[354] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new NextParseNode(((SourceIndexLength) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[355] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new RedoParseNode(((SourceIndexLength) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[356] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new RetryParseNode(((SourceIndexLength) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[357] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.checkExpression(((ParseNode) yyVals[0 + yyTop]));
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            if (yyVal == null)
                yyVal = NilImplicitParseNode.NIL;
            return yyVal;
        };
        states[364] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IfParseNode(((SourceIndexLength) yyVals[-4 + yyTop]), support.getConditionNode(((ParseNode) yyVals[-3 + yyTop])), ((ParseNode) yyVals[-1 + yyTop]),
                    ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[366] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[368] = (support, lexer, yyVal, yyVals, yyTop) -> yyVal;
        states[369] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.assignableInCurr(((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[370] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[371] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newArrayNode(((ParseNode) yyVals[0 + yyTop]).getPosition(), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[372] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]).add(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[373] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[0 + yyTop]).getPosition(), ((ListParseNode) yyVals[0 + yyTop]), null, null);
            return yyVal;
        };
        states[374] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), ((ListParseNode) yyVals[-3 + yyTop]), support.assignableInCurr(((String) yyVals[0 + yyTop]), null),
                    null);
            return yyVal;
        };
        states[375] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[-5 + yyTop]).getPosition(), ((ListParseNode) yyVals[-5 + yyTop]), support.assignableInCurr(((String) yyVals[-2 + yyTop]), null),
                    ((ListParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[376] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[-2 + yyTop]).getPosition(), ((ListParseNode) yyVals[-2 + yyTop]), new StarParseNode(lexer.getPosition()), null);
            return yyVal;
        };
        states[377] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(((ListParseNode) yyVals[-4 + yyTop]).getPosition(), ((ListParseNode) yyVals[-4 + yyTop]), new StarParseNode(lexer.getPosition()),
                    ((ListParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[378] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, support.assignableInCurr(((String) yyVals[0 + yyTop]), null), null);
            return yyVal;
        };
        states[379] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, support.assignableInCurr(((String) yyVals[-2 + yyTop]), null), ((ListParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[380] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(lexer.getPosition(), null, new StarParseNode(lexer.getPosition()), null);
            return yyVal;
        };
        states[381] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new MultipleAsgnParseNode(support.getPosition(((ListParseNode) yyVals[0 + yyTop])), null, null, ((ListParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[382] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), ((ListParseNode) yyVals[-3 + yyTop]), ((String) yyVals[-1 + yyTop]),
                    ((BlockArgParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[383] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(((ListParseNode) yyVals[-1 + yyTop]).getPosition(), ((ListParseNode) yyVals[-1 + yyTop]), null, ((BlockArgParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[384] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(lexer.getPosition(), null, ((String) yyVals[-1 + yyTop]), ((BlockArgParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[385] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(((BlockArgParseNode) yyVals[0 + yyTop]).getPosition(), null, null, ((BlockArgParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[386] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ArgsTailHolder) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[387] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(lexer.getPosition(), null, null, null);
            return yyVal;
        };
        states[388] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-5 + yyTop]).getPosition(), ((ListParseNode) yyVals[-5 + yyTop]), ((ListParseNode) yyVals[-3 + yyTop]),
                    ((RestArgParseNode) yyVals[-1 + yyTop]), null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[389] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-7 + yyTop]).getPosition(), ((ListParseNode) yyVals[-7 + yyTop]), ((ListParseNode) yyVals[-5 + yyTop]),
                    ((RestArgParseNode) yyVals[-3 + yyTop]), ((ListParseNode) yyVals[-1 + yyTop]), ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[390] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), ((ListParseNode) yyVals[-3 + yyTop]), ((ListParseNode) yyVals[-1 + yyTop]), null, null,
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[391] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-5 + yyTop]).getPosition(), ((ListParseNode) yyVals[-5 + yyTop]), ((ListParseNode) yyVals[-3 + yyTop]), null,
                    ((ListParseNode) yyVals[-1 + yyTop]), ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[392] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), ((ListParseNode) yyVals[-3 + yyTop]), null, ((RestArgParseNode) yyVals[-1 + yyTop]), null,
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[393] = (support, lexer, yyVal, yyVals, yyTop) -> {
            RestArgParseNode rest = new UnnamedRestArgParseNode(((ListParseNode) yyVals[-1 + yyTop]).getPosition(), TranslatorEnvironment.TEMP_PREFIX + "anon_rest",
                    support.getCurrentScope().addVariable("*"), false);
            yyVal = support.new_args(((ListParseNode) yyVals[-1 + yyTop]).getPosition(), ((ListParseNode) yyVals[-1 + yyTop]), null, rest, null, (ArgsTailHolder) null);
            return yyVal;
        };
        states[394] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-5 + yyTop]).getPosition(), ((ListParseNode) yyVals[-5 + yyTop]), null, ((RestArgParseNode) yyVals[-3 + yyTop]),
                    ((ListParseNode) yyVals[-1 + yyTop]), ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[395] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-1 + yyTop]).getPosition(), ((ListParseNode) yyVals[-1 + yyTop]), null, null, null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[396] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(support.getPosition(((ListParseNode) yyVals[-3 + yyTop])), null, ((ListParseNode) yyVals[-3 + yyTop]), ((RestArgParseNode) yyVals[-1 + yyTop]), null,
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[397] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(support.getPosition(((ListParseNode) yyVals[-5 + yyTop])), null, ((ListParseNode) yyVals[-5 + yyTop]), ((RestArgParseNode) yyVals[-3 + yyTop]),
                    ((ListParseNode) yyVals[-1 + yyTop]), ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[398] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(support.getPosition(((ListParseNode) yyVals[-1 + yyTop])), null, ((ListParseNode) yyVals[-1 + yyTop]), null, null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[399] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), null, ((ListParseNode) yyVals[-3 + yyTop]), null, ((ListParseNode) yyVals[-1 + yyTop]),
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[400] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((RestArgParseNode) yyVals[-1 + yyTop]).getPosition(), null, null, ((RestArgParseNode) yyVals[-1 + yyTop]), null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[401] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((RestArgParseNode) yyVals[-3 + yyTop]).getPosition(), null, null, ((RestArgParseNode) yyVals[-3 + yyTop]), ((ListParseNode) yyVals[-1 + yyTop]),
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[402] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ArgsTailHolder) yyVals[0 + yyTop]).getPosition(), null, null, null, null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[403] = (support, lexer, yyVal, yyVals, yyTop) -> {
            /* was $$ = null;*/
            yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
            return yyVal;
        };
        states[404] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.commandStart = true;
            yyVal = ((ArgsParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[405] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setCurrentArg(null);
            yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
            return yyVal;
        };
        states[406] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
            return yyVal;
        };
        states[407] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setCurrentArg(null);
            yyVal = ((ArgsParseNode) yyVals[-2 + yyTop]);
            return yyVal;
        };
        states[408] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[409] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[410] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[411] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[412] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.new_bv(((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[413] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[414] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.pushBlockScope();
            yyVal = lexer.getLeftParenBegin();
            lexer.setLeftParenBegin(lexer.incrementParenNest());
            return yyVal;
        };
        states[415] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getCmdArgumentState().getStack();
            lexer.getCmdArgumentState().reset();
            return yyVal;
        };
        states[416] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new LambdaParseNode(((ArgsParseNode) yyVals[-1 + yyTop]).getPosition(), ((ArgsParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), support.getCurrentScope());
            support.popCurrentScope();
            lexer.setLeftParenBegin(((Integer) yyVals[-3 + yyTop]));
            lexer.getCmdArgumentState().reset(((Long) yyVals[-2 + yyTop]).longValue());
            return yyVal;
        };
        states[417] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ArgsParseNode) yyVals[-2 + yyTop]);
            return yyVal;
        };
        states[418] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ArgsParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[419] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[420] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[421] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.pushBlockScope();
            return yyVal;
        };
        states[422] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IterParseNode(((SourceIndexLength) yyVals[-4 + yyTop]), ((ArgsParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]), support.getCurrentScope());
            support.popCurrentScope();
            return yyVal;
        };
        states[423] = (support, lexer, yyVal, yyVals, yyTop) -> {
            /* Workaround for JRUBY-2326 (MRI does not enter this production for some reason)*/
            if (((ParseNode) yyVals[-1 + yyTop]) instanceof YieldParseNode) {
                lexer.compile_error(PID.BLOCK_GIVEN_TO_YIELD, "block given to yield");
            }
            if (((ParseNode) yyVals[-1 + yyTop]) instanceof BlockAcceptingParseNode && ((BlockAcceptingParseNode) yyVals[-1 + yyTop]).getIterNode() instanceof BlockPassParseNode) {
                lexer.compile_error(PID.BLOCK_ARG_AND_BLOCK_GIVEN, "Both block arg and actual block given.");
            }
            if (((ParseNode) yyVals[-1 + yyTop]) instanceof NonLocalControlFlowParseNode) {
                ((BlockAcceptingParseNode) ((NonLocalControlFlowParseNode) yyVals[-1 + yyTop]).getValueNode()).setIterNode(((IterParseNode) yyVals[0 + yyTop]));
            } else {
                ((BlockAcceptingParseNode) yyVals[-1 + yyTop]).setIterNode(((IterParseNode) yyVals[0 + yyTop]));
            }
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            ((ParseNode) yyVal).setPosition(((ParseNode) yyVals[-1 + yyTop]).getPosition());
            return yyVal;
        };
        states[424] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-3 + yyTop]), ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[425] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop]), ((String) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]),
                    ((IterParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[426] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-4 + yyTop]), ((String) yyVals[-3 + yyTop]), ((String) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]),
                    ((IterParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[427] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.frobnicate_fcall_args(((FCallParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), null);
            yyVal = ((FCallParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[428] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-3 + yyTop]), ((String) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[429] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-3 + yyTop]), ((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[430] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[0 + yyTop]), null, null);
            return yyVal;
        };
        states[431] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-2 + yyTop]), ((String) yyVals[-1 + yyTop]), "call", ((ParseNode) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[432] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_call(((ParseNode) yyVals[-2 + yyTop]), "call", ((ParseNode) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[433] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_super(((SourceIndexLength) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[434] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ZSuperParseNode(((SourceIndexLength) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[435] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (((ParseNode) yyVals[-3 + yyTop]) instanceof SelfParseNode) {
                yyVal = support.new_fcall("[]");
                support.frobnicate_fcall_args(((FCallParseNode) yyVal), ((ParseNode) yyVals[-1 + yyTop]), null);
            } else {
                yyVal = support.new_call(((ParseNode) yyVals[-3 + yyTop]), "[]", ((ParseNode) yyVals[-1 + yyTop]), null);
            }
            return yyVal;
        };
        states[436] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.pushBlockScope();
            return yyVal;
        };
        states[437] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IterParseNode(((SourceIndexLength) yyVals[-4 + yyTop]), ((ArgsParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]), support.getCurrentScope());
            support.popCurrentScope();
            return yyVal;
        };
        states[438] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.pushBlockScope();
            return yyVal;
        };
        states[439] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new IterParseNode(((SourceIndexLength) yyVals[-4 + yyTop]), ((ArgsParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]), support.getCurrentScope());
            support.popCurrentScope();
            return yyVal;
        };
        states[440] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newWhenNode(((SourceIndexLength) yyVals[-4 + yyTop]), ((ParseNode) yyVals[-3 + yyTop]), ((ParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[443] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode node;
            if (((ParseNode) yyVals[-3 + yyTop]) != null) {
                node = support.appendToBlock(support.node_assign(((ParseNode) yyVals[-3 + yyTop]), new GlobalVarParseNode(((SourceIndexLength) yyVals[-5 + yyTop]), "$!")),
                        ((ParseNode) yyVals[-1 + yyTop]));
                if (((ParseNode) yyVals[-1 + yyTop]) != null) {
                    node.setPosition(((SourceIndexLength) yyVals[-5 + yyTop]));
                }
            } else {
                node = ((ParseNode) yyVals[-1 + yyTop]);
            }
            ParseNode body = support.makeNullNil(node);
            yyVal = new RescueBodyParseNode(((SourceIndexLength) yyVals[-5 + yyTop]), ((ParseNode) yyVals[-4 + yyTop]), body, ((RescueBodyParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[444] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[445] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newArrayNode(((ParseNode) yyVals[0 + yyTop]).getPosition(), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[446] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.splat_array(((ParseNode) yyVals[0 + yyTop]));
            if (yyVal == null)
                yyVal = ((ParseNode) yyVals[0 + yyTop]); /* ArgsCat or ArgsPush*/
            return yyVal;
        };
        states[448] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[450] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[452] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((NumericParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[453] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.asSymbol(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[455] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]) instanceof EvStrParseNode
                    ? new DStrParseNode(((ParseNode) yyVals[0 + yyTop]).getPosition(), lexer.getEncoding()).add(((ParseNode) yyVals[0 + yyTop])) : ((ParseNode) yyVals[0 + yyTop]);
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
        states[456] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((StrParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[457] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[458] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.literal_concat(((ParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[459] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.heredoc_dedent(((ParseNode) yyVals[-1 + yyTop]));
            lexer.setHeredocIndent(0);
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[460] = (support, lexer, yyVal, yyVals, yyTop) -> {
            SourceIndexLength position = support.getPosition(((ParseNode) yyVals[-1 + yyTop]));

            lexer.heredoc_dedent(((ParseNode) yyVals[-1 + yyTop]));
            lexer.setHeredocIndent(0);

            if (((ParseNode) yyVals[-1 + yyTop]) == null) {
                yyVal = new XStrParseNode(position, null, CodeRange.CR_7BIT);
            } else if (((ParseNode) yyVals[-1 + yyTop]) instanceof StrParseNode) {
                yyVal = new XStrParseNode(position, ((StrParseNode) yyVals[-1 + yyTop]).getValue(), ((StrParseNode) yyVals[-1 + yyTop]).getCodeRange());
            } else if (((ParseNode) yyVals[-1 + yyTop]) instanceof DStrParseNode) {
                yyVal = new DXStrParseNode(position, ((DStrParseNode) yyVals[-1 + yyTop]));

                ((ParseNode) yyVal).setPosition(position);
            } else {
                yyVal = new DXStrParseNode(position).add(((ParseNode) yyVals[-1 + yyTop]));
            }
            return yyVal;
        };
        states[461] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.newRegexpNode(support.getPosition(((ParseNode) yyVals[-1 + yyTop])), ((ParseNode) yyVals[-1 + yyTop]), ((RegexpParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[462] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ZArrayParseNode(lexer.getPosition());
            return yyVal;
        };
        states[463] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[464] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ArrayParseNode(lexer.getPosition());
            return yyVal;
        };
        states[465] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]).add(((ParseNode) yyVals[-1 + yyTop]) instanceof EvStrParseNode
                    ? new DStrParseNode(((ListParseNode) yyVals[-2 + yyTop]).getPosition(), lexer.getEncoding()).add(((ParseNode) yyVals[-1 + yyTop])) : ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[466] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[467] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.literal_concat(((ParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[468] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ArrayParseNode(lexer.getPosition());
            return yyVal;
        };
        states[469] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[470] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ArrayParseNode(lexer.getPosition());
            return yyVal;
        };
        states[471] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]).add(
                    ((ParseNode) yyVals[-1 + yyTop]) instanceof EvStrParseNode ? new DSymbolParseNode(((ListParseNode) yyVals[-2 + yyTop]).getPosition()).add(((ParseNode) yyVals[-1 + yyTop]))
                            : support.asSymbol(((ListParseNode) yyVals[-2 + yyTop]).getPosition(), ((ParseNode) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[472] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ZArrayParseNode(lexer.getPosition());
            return yyVal;
        };
        states[473] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[474] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ZArrayParseNode(lexer.getPosition());
            return yyVal;
        };
        states[475] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[476] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ArrayParseNode(lexer.getPosition());
            return yyVal;
        };
        states[477] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]).add(((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[478] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ArrayParseNode(lexer.getPosition());
            return yyVal;
        };
        states[479] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]).add(support.asSymbol(((ListParseNode) yyVals[-2 + yyTop]).getPosition(), ((ParseNode) yyVals[-1 + yyTop])));
            return yyVal;
        };
        states[480] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.createStr(RopeOperations.emptyRope(lexer.getEncoding()), 0);
            return yyVal;
        };
        states[481] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.literal_concat(((ParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[482] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[483] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.literal_concat(((ParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[484] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[485] = (support, lexer, yyVal, yyVals, yyTop) -> {
            /* FIXME: mri is different here.*/
            yyVal = support.literal_concat(((ParseNode) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[486] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[487] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getStrTerm();
            lexer.setStrTerm(null);
            lexer.setState(EXPR_BEG);
            return yyVal;
        };
        states[488] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setStrTerm(((StrTerm) yyVals[-1 + yyTop]));
            yyVal = new EvStrParseNode(support.getPosition(((ParseNode) yyVals[0 + yyTop])), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[489] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getStrTerm();
            lexer.setStrTerm(null);
            lexer.getConditionState().stop();
            return yyVal;
        };
        states[490] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getCmdArgumentState().getStack();
            lexer.getCmdArgumentState().reset();
            return yyVal;
        };
        states[491] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getState();
            lexer.setState(EXPR_BEG);
            return yyVal;
        };
        states[492] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getBraceNest();
            lexer.setBraceNest(0);
            return yyVal;
        };
        states[493] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.getHeredocIndent();
            lexer.setHeredocIndent(0);
            return yyVal;
        };
        states[494] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.getConditionState().restart();
            lexer.setStrTerm(((StrTerm) yyVals[-6 + yyTop]));
            lexer.getCmdArgumentState().reset(((Long) yyVals[-5 + yyTop]).longValue());
            lexer.setState(((Integer) yyVals[-4 + yyTop]));
            lexer.setBraceNest(((Integer) yyVals[-3 + yyTop]));
            lexer.setHeredocIndent(((Integer) yyVals[-2 + yyTop]));
            lexer.setHeredocLineIndent(-1);

            yyVal = support.newEvStrNode(support.getPosition(((ParseNode) yyVals[-1 + yyTop])), ((ParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[495] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new GlobalVarParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[496] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new InstVarParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[497] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ClassVarParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[499] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_END);
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[504] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_END);

            /* DStrParseNode: :"some text #{some expression}"*/
            /* StrParseNode: :"some text"*/
            /* EvStrParseNode :"#{some expression}"*/
            /* Ruby 1.9 allows empty strings as symbols*/
            if (((ParseNode) yyVals[-1 + yyTop]) == null) {
                yyVal = support.asSymbol(lexer.getPosition(), RopeConstants.EMPTY_US_ASCII_ROPE);
            } else if (((ParseNode) yyVals[-1 + yyTop]) instanceof DStrParseNode) {
                yyVal = new DSymbolParseNode(((ParseNode) yyVals[-1 + yyTop]).getPosition(), ((DStrParseNode) yyVals[-1 + yyTop]));
            } else if (((ParseNode) yyVals[-1 + yyTop]) instanceof StrParseNode) {
                yyVal = support.asSymbol(((ParseNode) yyVals[-1 + yyTop]).getPosition(), ((ParseNode) yyVals[-1 + yyTop]));
            } else {
                yyVal = new DSymbolParseNode(((ParseNode) yyVals[-1 + yyTop]).getPosition());
                ((DSymbolParseNode) yyVal).add(((ParseNode) yyVals[-1 + yyTop]));
            }
            return yyVal;
        };
        states[505] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((NumericParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[506] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.negateNumeric(((NumericParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[507] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[508] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((FloatParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[509] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((RationalParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[510] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[511] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.declareIdentifier(((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[512] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new InstVarParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[513] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new GlobalVarParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[514] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ConstParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[515] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ClassVarParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[516] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new NilParseNode(lexer.getPosition());
            return yyVal;
        };
        states[517] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new SelfParseNode(lexer.getPosition());
            return yyVal;
        };
        states[518] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new TrueParseNode((SourceIndexLength) yyVal);
            return yyVal;
        };
        states[519] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new FalseParseNode((SourceIndexLength) yyVal);
            return yyVal;
        };
        states[520] = (support, lexer, yyVal, yyVals, yyTop) -> {
            Encoding encoding = support.getConfiguration().getContext() == null ? EncodingManager.getEncoding(Charset.defaultCharset().name())
                    : support.getConfiguration().getContext().getEncodingManager().getLocaleEncoding();
            yyVal = new FileParseNode(lexer.getPosition(), RopeOperations.create(lexer.getFile().getBytes(), encoding, CR_UNKNOWN));
            return yyVal;
        };
        states[521] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new FixnumParseNode(lexer.getPosition(), lexer.tokline.toSourceSection(lexer.getSource()).getStartLine());
            return yyVal;
        };
        states[522] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new EncodingParseNode(lexer.getPosition(), lexer.getEncoding());
            return yyVal;
        };
        states[523] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.assignableLabelOrIdentifier(((String) yyVals[0 + yyTop]), null);
            return yyVal;
        };
        states[524] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new InstAsgnParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[525] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new GlobalAsgnParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[526] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (support.isInDef() || support.isInSingle())
                support.compile_error("dynamic constant assignment");

            yyVal = new ConstDeclParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), null, NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[527] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ClassVarAsgnParseNode(lexer.getPosition(), ((String) yyVals[0 + yyTop]), NilImplicitParseNode.NIL);
            return yyVal;
        };
        states[528] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to nil");
            yyVal = null;
            return yyVal;
        };
        states[529] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't change the value of self");
            yyVal = null;
            return yyVal;
        };
        states[530] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to true");
            yyVal = null;
            return yyVal;
        };
        states[531] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to false");
            yyVal = null;
            return yyVal;
        };
        states[532] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to __FILE__");
            yyVal = null;
            return yyVal;
        };
        states[533] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to __LINE__");
            yyVal = null;
            return yyVal;
        };
        states[534] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.compile_error("Can't assign to __ENCODING__");
            yyVal = null;
            return yyVal;
        };
        states[535] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[536] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[537] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_BEG);
            lexer.commandStart = true;
            return yyVal;
        };
        states[538] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[539] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[540] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ArgsParseNode) yyVals[-1 + yyTop]);
            lexer.setState(EXPR_BEG);
            lexer.commandStart = true;
            return yyVal;
        };
        states[541] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = lexer.inKwarg;
            lexer.inKwarg = true;
            lexer.setState(lexer.getState() | EXPR_LABEL);
            return yyVal;
        };
        states[542] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.inKwarg = ((Boolean) yyVals[-2 + yyTop]);
            yyVal = ((ArgsParseNode) yyVals[-1 + yyTop]);
            lexer.setState(EXPR_BEG);
            lexer.commandStart = true;
            return yyVal;
        };
        states[543] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), ((ListParseNode) yyVals[-3 + yyTop]), ((String) yyVals[-1 + yyTop]),
                    ((BlockArgParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[544] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(((ListParseNode) yyVals[-1 + yyTop]).getPosition(), ((ListParseNode) yyVals[-1 + yyTop]), null, ((BlockArgParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[545] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(lexer.getPosition(), null, ((String) yyVals[-1 + yyTop]), ((BlockArgParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[546] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(((BlockArgParseNode) yyVals[0 + yyTop]).getPosition(), null, null, ((BlockArgParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[547] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ArgsTailHolder) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[548] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args_tail(lexer.getPosition(), null, null, null);
            return yyVal;
        };
        states[549] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-5 + yyTop]).getPosition(), ((ListParseNode) yyVals[-5 + yyTop]), ((ListParseNode) yyVals[-3 + yyTop]),
                    ((RestArgParseNode) yyVals[-1 + yyTop]), null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[550] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-7 + yyTop]).getPosition(), ((ListParseNode) yyVals[-7 + yyTop]), ((ListParseNode) yyVals[-5 + yyTop]),
                    ((RestArgParseNode) yyVals[-3 + yyTop]), ((ListParseNode) yyVals[-1 + yyTop]), ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[551] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), ((ListParseNode) yyVals[-3 + yyTop]), ((ListParseNode) yyVals[-1 + yyTop]), null, null,
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[552] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-5 + yyTop]).getPosition(), ((ListParseNode) yyVals[-5 + yyTop]), ((ListParseNode) yyVals[-3 + yyTop]), null,
                    ((ListParseNode) yyVals[-1 + yyTop]), ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[553] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), ((ListParseNode) yyVals[-3 + yyTop]), null, ((RestArgParseNode) yyVals[-1 + yyTop]), null,
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[554] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-5 + yyTop]).getPosition(), ((ListParseNode) yyVals[-5 + yyTop]), null, ((RestArgParseNode) yyVals[-3 + yyTop]),
                    ((ListParseNode) yyVals[-1 + yyTop]), ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[555] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-1 + yyTop]).getPosition(), ((ListParseNode) yyVals[-1 + yyTop]), null, null, null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[556] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), null, ((ListParseNode) yyVals[-3 + yyTop]), ((RestArgParseNode) yyVals[-1 + yyTop]), null,
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[557] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-5 + yyTop]).getPosition(), null, ((ListParseNode) yyVals[-5 + yyTop]), ((RestArgParseNode) yyVals[-3 + yyTop]),
                    ((ListParseNode) yyVals[-1 + yyTop]), ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[558] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-1 + yyTop]).getPosition(), null, ((ListParseNode) yyVals[-1 + yyTop]), null, null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[559] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ListParseNode) yyVals[-3 + yyTop]).getPosition(), null, ((ListParseNode) yyVals[-3 + yyTop]), null, ((ListParseNode) yyVals[-1 + yyTop]),
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[560] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((RestArgParseNode) yyVals[-1 + yyTop]).getPosition(), null, null, ((RestArgParseNode) yyVals[-1 + yyTop]), null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[561] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((RestArgParseNode) yyVals[-3 + yyTop]).getPosition(), null, null, ((RestArgParseNode) yyVals[-3 + yyTop]), ((ListParseNode) yyVals[-1 + yyTop]),
                    ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[562] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(((ArgsTailHolder) yyVals[0 + yyTop]).getPosition(), null, null, null, null, ((ArgsTailHolder) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[563] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.new_args(lexer.getPosition(), null, null, null, null, (ArgsTailHolder) null);
            return yyVal;
        };
        states[564] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.yyerror("formal argument cannot be a constant");
            return yyVal;
        };
        states[565] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.yyerror("formal argument cannot be an instance variable");
            return yyVal;
        };
        states[566] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.yyerror("formal argument cannot be a global variable");
            return yyVal;
        };
        states[567] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.yyerror("formal argument cannot be a class variable");
            return yyVal;
        };
        states[569] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.formal_argument(((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[570] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setCurrentArg(((String) yyVals[0 + yyTop]));
            yyVal = support.arg_var(((String) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[571] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setCurrentArg(null);
            yyVal = ((ArgumentParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[572] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
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
        states[573] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ArrayParseNode(lexer.getPosition(), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[574] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ((ListParseNode) yyVals[-2 + yyTop]).add(((ParseNode) yyVals[0 + yyTop]));
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]);
            return yyVal;
        };
        states[575] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.arg_var(support.formal_argument(((String) yyVals[0 + yyTop])));
            lexer.setCurrentArg(((String) yyVals[0 + yyTop]));
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[576] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setCurrentArg(null);
            yyVal = support.keyword_arg(((ParseNode) yyVals[0 + yyTop]).getPosition(), support.assignableKeyword(((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop])));
            return yyVal;
        };
        states[577] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setCurrentArg(null);
            yyVal = support.keyword_arg(lexer.getPosition(), support.assignableKeyword(((String) yyVals[0 + yyTop]), new RequiredKeywordArgumentValueParseNode()));
            return yyVal;
        };
        states[578] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.keyword_arg(support.getPosition(((ParseNode) yyVals[0 + yyTop])), support.assignableKeyword(((String) yyVals[-1 + yyTop]), ((ParseNode) yyVals[0 + yyTop])));
            return yyVal;
        };
        states[579] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.keyword_arg(lexer.getPosition(), support.assignableKeyword(((String) yyVals[0 + yyTop]), new RequiredKeywordArgumentValueParseNode()));
            return yyVal;
        };
        states[580] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ArrayParseNode(((ParseNode) yyVals[0 + yyTop]).getPosition(), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[581] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]).add(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[582] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new ArrayParseNode(((ParseNode) yyVals[0 + yyTop]).getPosition(), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[583] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((ListParseNode) yyVals[-2 + yyTop]).add(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[584] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[585] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[586] = (support, lexer, yyVal, yyVals, yyTop) -> {
            support.shadowing_lvar(((String) yyVals[0 + yyTop]));
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[587] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.internalId();
            return yyVal;
        };
        states[588] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setCurrentArg(null);
            yyVal = new OptArgParseNode(support.getPosition(((ParseNode) yyVals[0 + yyTop])),
                    support.assignableLabelOrIdentifier(((ArgumentParseNode) yyVals[-2 + yyTop]).getName(), ((ParseNode) yyVals[0 + yyTop])));
            return yyVal;
        };
        states[589] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setCurrentArg(null);
            yyVal = new OptArgParseNode(support.getPosition(((ParseNode) yyVals[0 + yyTop])),
                    support.assignableLabelOrIdentifier(((ArgumentParseNode) yyVals[-2 + yyTop]).getName(), ((ParseNode) yyVals[0 + yyTop])));
            return yyVal;
        };
        states[590] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new BlockParseNode(((ParseNode) yyVals[0 + yyTop]).getPosition()).add(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[591] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.appendToBlock(((ListParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[592] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new BlockParseNode(((ParseNode) yyVals[0 + yyTop]).getPosition()).add(((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[593] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.appendToBlock(((ListParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[596] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (!support.is_local_id(((String) yyVals[0 + yyTop]))) {
                support.yyerror("rest argument must be local variable");
            }

            yyVal = new RestArgParseNode(support.arg_var(support.shadowing_lvar(((String) yyVals[0 + yyTop]))));
            return yyVal;
        };
        states[597] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new UnnamedRestArgParseNode(lexer.getPosition(), TranslatorEnvironment.TEMP_PREFIX + "rest", support.getCurrentScope().addVariable("*"), true);
            return yyVal;
        };
        states[600] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (!support.is_local_id(((String) yyVals[0 + yyTop]))) {
                support.yyerror("block argument must be local variable");
            }

            yyVal = new BlockArgParseNode(support.arg_var(support.shadowing_lvar(((String) yyVals[0 + yyTop]))));
            return yyVal;
        };
        states[601] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((BlockArgParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[602] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[603] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (!(((ParseNode) yyVals[0 + yyTop]) instanceof SelfParseNode)) {
                support.checkExpression(((ParseNode) yyVals[0 + yyTop]));
            }
            yyVal = ((ParseNode) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[604] = (support, lexer, yyVal, yyVals, yyTop) -> {
            lexer.setState(EXPR_BEG);
            return yyVal;
        };
        states[605] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (((ParseNode) yyVals[-1 + yyTop]) == null) {
                support.yyerror("can't define single method for ().");
            } else if (((ParseNode) yyVals[-1 + yyTop]) instanceof ILiteralNode) {
                support.yyerror("can't define single method for literals.");
            }
            support.checkExpression(((ParseNode) yyVals[-1 + yyTop]));
            yyVal = ((ParseNode) yyVals[-1 + yyTop]);
            return yyVal;
        };
        states[606] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new HashParseNode(lexer.getPosition());
            return yyVal;
        };
        states[607] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.remove_duplicate_keys(((HashParseNode) yyVals[-1 + yyTop]));
            return yyVal;
        };
        states[608] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = new HashParseNode(lexer.getPosition(), ((ParseNodeTuple) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[609] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((HashParseNode) yyVals[-2 + yyTop]).add(((ParseNodeTuple) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[610] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.createKeyValue(((ParseNode) yyVals[-2 + yyTop]), ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[611] = (support, lexer, yyVal, yyVals, yyTop) -> {
            ParseNode label = support.asSymbol(support.getPosition(((ParseNode) yyVals[0 + yyTop])), ((String) yyVals[-1 + yyTop]));
            yyVal = support.createKeyValue(label, ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[612] = (support, lexer, yyVal, yyVals, yyTop) -> {
            if (((ParseNode) yyVals[-2 + yyTop]) instanceof StrParseNode) {
                DStrParseNode dnode = new DStrParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), lexer.getEncoding());
                dnode.add(((ParseNode) yyVals[-2 + yyTop]));
                yyVal = support.createKeyValue(new DSymbolParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), dnode), ((ParseNode) yyVals[0 + yyTop]));
            } else if (((ParseNode) yyVals[-2 + yyTop]) instanceof DStrParseNode) {
                yyVal = support.createKeyValue(new DSymbolParseNode(support.getPosition(((ParseNode) yyVals[-2 + yyTop])), ((DStrParseNode) yyVals[-2 + yyTop])), ((ParseNode) yyVals[0 + yyTop]));
            } else {
                support.compile_error("Uknown type for assoc in strings: " + ((ParseNode) yyVals[-2 + yyTop]));
            }

            return yyVal;
        };
        states[613] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = support.createKeyValue(null, ((ParseNode) yyVals[0 + yyTop]));
            return yyVal;
        };
        states[626] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[627] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[629] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = "::";
            return yyVal;
        };
        states[634] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[635] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = ((String) yyVals[0 + yyTop]);
            return yyVal;
        };
        states[643] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
        states[644] = (support, lexer, yyVal, yyVals, yyTop) -> {
            yyVal = null;
            return yyVal;
        };
    }
    // line 2572 "RubyParser.y"

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
// line 10092 "-"
