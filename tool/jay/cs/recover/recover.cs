// created by jay 1.0 (c) 2002 ats@cs.rit.edu
// skeleton c# 1.0 (c) 2002 ats@cs.rit.edu

#line 2 "recover.jay"
  using System;
  using System.Collections;	// Hashtable
  using System.IO;		// TextReader
  using System.Text;		// StringBuilder

  /// <summary>
  ///   demonstrates robust error recovery for typical iterations.
  ///   Based on Schreiner/Friedman 'Introduction to Compiler Construction
  ///   with Unix' ISBN 0-13-474396-2.
  /// </summary>
  public class Recover {
#line 17 "-"
  // %token constants
  public const int LIST = 257;
  public const int OPT = 258;
  public const int SEQ = 259;
  public const int WORD = 260;
  public const int yyErrorCode = 256;

  /// <summary>
  ///   final state of parser.
  /// </summary>
  protected const int yyFinal = 1;

  /// <summary>
  ///   parser tables.
  ///   Order is mandated by jay.
  /// </summary>
  protected static readonly short[] yyLhs = {
//yyLhs 18
    -1,     0,     0,     0,     0,     1,     1,     1,     2,     2,
     2,     2,     3,     3,     3,     3,     3,     3,
    }, yyLen = {
//yyLen 18
     2,     0,     4,     4,     4,     0,     2,     2,     1,     2,
     1,     2,     1,     3,     1,     2,     3,     3,
    }, yyDefRed = {
//yyDefRed 24
     1,     0,     0,     5,     0,    14,    12,     0,     0,    10,
     8,     0,     0,     4,     0,     7,     6,     2,    11,     9,
     3,    16,    17,    13,
    }, yyDgoto = {
//yyDgoto 4
     1,     8,    11,     7,
    }, yySindex = {
//yySindex 24
     0,  -246,  -252,     0,  -251,     0,     0,    -8,   -10,     0,
     0,    -9,  -253,     0,  -250,     0,     0,     0,     0,     0,
     0,     0,     0,     0,
    }, yyRindex = {
//yyRindex 24
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,    -7,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,
    }, yyGindex = {
//yyGindex 4
     0,     0,     0,     0,
    }, yyTable = {
//yyTable 252
    17,    20,    13,    15,     5,     9,    22,    21,     6,    10,
    23,     2,     3,     4,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,    14,    15,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,    15,    18,    12,    15,
    16,    19,
    }, yyCheck = {
//yyCheck 252
    10,    10,    10,    10,   256,   256,   256,   260,   260,   260,
   260,   257,   258,   259,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    44,    44,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,   256,   256,   256,   256,
   260,   260,
    };

  /// <summary>
  ///   maps symbol value to printable name.
  ///   see <c>yyExpecting</c>
  /// </summary>
  protected static readonly string[] yyNames = {
    "end-of-file",null,null,null,null,null,null,null,null,null,"'\\n'",
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,"','",null,null,null,null,null,null,null,
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
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,"LIST","OPT","SEQ",
    "WORD",
    };

  /// <summary>
  ///   printable rules for debugging.
  /// </summary>
  protected static readonly string [] yyRule = {
    "$accept : line",
    "line :",
    "line : line OPT opt '\\n'",
    "line : line SEQ seq '\\n'",
    "line : line LIST list '\\n'",
    "opt :",
    "opt : opt WORD",
    "opt : opt error",
    "seq : WORD",
    "seq : seq WORD",
    "seq : error",
    "seq : seq error",
    "list : WORD",
    "list : list ',' WORD",
    "list : error",
    "list : list error",
    "list : list error WORD",
    "list : list ',' error",
    };

  /// <summary>
  ///   debugging support, requires <c>yyDebug</c>.
  ///   Set to <c>null</c> to suppress debugging messages.
  /// </summary>
  protected yyDebug.yyDebug yyDebug;

  /// <summary>
  ///   index-checked interface to <c>yyNames[]</c>.
  /// </summary>
  /// <param name='token'>single character or <c>%token</c> value</param>
  /// <returns>token name or <c>[illegal]</c> or <c>[unknown]</c></returns>
  public static string yyName (int token) {
    if ((token < 0) || (token > yyNames.Length)) return "[illegal]";
    string name;
    if ((name = yyNames[token]) != null) return name;
    return "[unknown]";
  }

  /// <summary>
  ///   thrown for irrecoverable syntax errors and stack overflow.
  /// </summary>
  /// <remarks>
  ///   Nested for convenience, does not depend on parser class.
  /// </remarks>
  public class yyException : System.Exception {
    public yyException (string message) : base (message) {
    }
  }

  /// <summary>
  ///   must be implemented by a scanner object to supply input to the parser.
  /// </summary>
  /// <remarks>
  ///   Nested for convenience, does not depend on parser class.
  /// </remarks>
  public interface yyInput {

    /// <summary>
    ///   move on to next token.
    /// </summary>
    /// <returns><c>false</c> if positioned beyond tokens</returns>
    /// <exception><c>IOException</c> on input error</exception>
    bool Advance ();

    /// <summary>
    ///   classifies current token by <c>%token</c> value or single character.
    /// </summary>
    /// <remarks>
    ///   Should not be called if <c>Advance()</c> returned false.
    /// </remarks>
    int Token { get; }

    /// <summary>
    ///   value associated with current token.
    /// </summary>
    /// <remarks>
    ///   Should not be called if <c>Advance()</c> returned false.
    /// </remarks>
    object Value { get; }
  }

  /// <summary>
  ///   simplified error message.
  /// </summary>
  public void yyError (string message) {
    yyError(message, null);
  }

  /// <summary>
  ///   (syntax) error message.
  ///   Can be overwritten to control message format.
  /// </summary>
  /// <param name='message'>text to be displayed</param>
  /// <param name='expected'>list of acceptable tokens, if available</param>
  public void yyError (string message, string[] expected) {
    if ((expected != null) && (expected.Length > 0)) {
      System.Console.Write (message+", expecting");
      for (int n = 0; n < expected.Length; ++ n)
        System.Console.Write(" "+expected[n]);
        System.Console.WriteLine();
    } else
      System.Console.WriteLine(message);
  }

  /// <summary>
  ///   computes list of expected tokens on error by tracing the tables.
  /// </summary>
  /// <param name='state'>for which to compute the list</param>
  /// <returns>list of token names</returns>
  protected string[] yyExpecting (int state) {
    int token, n, len = 0;
    bool[] ok = new bool[yyNames.Length];

    if ((n = yySindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           (token < yyNames.Length) && (n+token < yyTable.Length); ++ token)
        if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
          ++ len;
          ok[token] = true;
        }
    if ((n = yyRindex[state]) != 0)
      for (token = n < 0 ? -n : 0;
           (token < yyNames.Length) && (n+token < yyTable.Length); ++ token)
        if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
          ++ len;
          ok[token] = true;
        }

    string [] result = new string[len];
    for (n = token = 0; n < len;  ++ token)
      if (ok[token]) result[n++] = yyNames[token];
    return result;
  }

  /// <summary>
  ///   the generated parser, with debugging messages.
  ///   Maintains a dynamic state and value stack.
  /// </summary>
  /// <param name='yyLex'>scanner</param>
  /// <param name='yyDebug'>debug message writer implementing <c>yyDebug</c>,
  ///   or <c>null</c></param>
  /// <returns>result of the last reduction, if any</returns>
  /// <exceptions><c>yyException</c> on irrecoverable parse error</exceptions>
  public object yyParse (yyInput yyLex, object yyDebug) {
    this.yyDebug = (yyDebug.yyDebug)yyDebug;
    return yyParse(yyLex);
  }

  /// <summary>
  ///   initial size and increment of the state/value stack [default 256].
  ///    This is not final so that it can be overwritten outside of invocations
  ///    of <c>yyParse()</c>.
  /// </summary>
  protected int yyMax;

  /// <summary>
  ///   executed at the beginning of a reduce action.
  ///   Used as <c>$$ = yyDefault($1)</c>, prior to the user-specified action, if any.
  ///   Can be overwritten to provide deep copy, etc.
  /// </summary>
  /// <param first value for $1, or null.
  /// <return first.
  protected object yyDefault (object first) {
    return first;
  }

  /// <summary>
  ///   the generated parser, with debugging messages.
  ///   Maintains a dynamic state and value stack.
  /// </summary>
  /// <param name='yyLex'>scanner</param>
  /// <returns>result of the last reduction, if any</returns>
  /// <exceptions><c>yyException</c> on irrecoverable parse error</exceptions>
  public object yyParse (yyInput yyLex) {
    if (yyMax <= 0) yyMax = 256;			// initial size
    int yyState = 0;                                   // state stack ptr
    int [] yyStates = new int[yyMax];	                // state stack 
    object yyVal = null;                               // value stack ptr
    object [] yyVals = new object[yyMax];	        // value stack
    int yyToken = -1;					// current input
    int yyErrorFlag = 0;				// #tokens to shift

    int yyTop = 0;
    goto skip;
    yyLoop:
    yyTop++;
    skip:
    for (;; ++ yyTop) {
      if (yyTop >= yyStates.Length) {			// dynamically increase
        int[] i = new int[yyStates.Length+yyMax];
        yyStates.CopyTo (i, 0);
        yyStates = i;
        object[] o = new object[yyVals.Length+yyMax];
        yyVals.CopyTo (o, 0);
        yyVals = o;
      }
      yyStates[yyTop] = yyState;
      yyVals[yyTop] = yyVal;
      if (yyDebug != null) yyDebug.push(yyState, yyVal);

      yyDiscarded: for (;;) {	// discarding a token does not change stack
        int yyN;
        if ((yyN = yyDefRed[yyState]) == 0) {	// else [default] reduce (yyN)
          if (yyToken < 0) {
            yyToken = yyLex.Advance() ? yyLex.Token : 0;
            if (yyDebug != null)
              yyDebug.lex(yyState, yyToken, yyName(yyToken), yyLex.Value);
          }
          if ((yyN = yySindex[yyState]) != 0 && ((yyN += yyToken) >= 0)
              && (yyN < yyTable.Length) && (yyCheck[yyN] == yyToken)) {
            if (yyDebug != null)
              yyDebug.shift(yyState, yyTable[yyN], yyErrorFlag-1);
            yyState = yyTable[yyN];		// shift to yyN
            yyVal = yyLex.Value;
            yyToken = -1;
            if (yyErrorFlag > 0) -- yyErrorFlag;
            goto yyLoop;
          }
          if ((yyN = yyRindex[yyState]) != 0 && (yyN += yyToken) >= 0
              && yyN < yyTable.Length && yyCheck[yyN] == yyToken)
            yyN = yyTable[yyN];			// reduce (yyN)
          else
            switch (yyErrorFlag) {
  
            case 0:
              yyError("syntax error", yyExpecting(yyState));
              if (yyDebug != null) yyDebug.error("syntax error");
              goto case 1;
            case 1: case 2:
              yyErrorFlag = 3;
              do {
                if ((yyN = yySindex[yyStates[yyTop]]) != 0
                    && (yyN += yyErrorCode) >= 0 && yyN < yyTable.Length
                    && yyCheck[yyN] == yyErrorCode) {
                  if (yyDebug != null)
                    yyDebug.shift(yyStates[yyTop], yyTable[yyN], 3);
                  yyState = yyTable[yyN];
                  yyVal = yyLex.Value;
                  goto yyLoop;
                }
                if (yyDebug != null) yyDebug.pop(yyStates[yyTop]);
              } while (-- yyTop >= 0);
              if (yyDebug != null) yyDebug.reject();
              throw new yyException("irrecoverable syntax error");
  
            case 3:
              if (yyToken == 0) {
                if (yyDebug != null) yyDebug.reject();
                throw new yyException("irrecoverable syntax error at end-of-file");
              }
              if (yyDebug != null)
                yyDebug.discard(yyState, yyToken, yyName(yyToken),
  							yyLex.Value);
              yyToken = -1;
              goto yyDiscarded;		// leave stack alone
            }
        }
        int yyV = yyTop + 1-yyLen[yyN];
        if (yyDebug != null)
          yyDebug.reduce(yyState, yyStates[yyV-1], yyN, yyRule[yyN], yyLen[yyN]);
        yyVal = yyDefault(yyV > yyTop ? null : yyVals[yyV]);
        switch (yyN) {
case 2:
#line 18 "recover.jay"
  { yyErrorFlag = 0; Console.WriteLine("opt"); }
  break;
case 3:
#line 19 "recover.jay"
  { yyErrorFlag = 0; Console.WriteLine("seq"); }
  break;
case 4:
#line 20 "recover.jay"
  { yyErrorFlag = 0; Console.WriteLine("list"); }
  break;
case 6:
#line 23 "recover.jay"
  { yyErrorFlag = 0; }
  break;
case 9:
#line 27 "recover.jay"
  { yyErrorFlag = 0; }
  break;
case 13:
#line 32 "recover.jay"
  { yyErrorFlag = 0; }
  break;
case 16:
#line 35 "recover.jay"
  { yyErrorFlag = 0; }
  break;
#line 443 "-"
        }
        yyTop -= yyLen[yyN];
        yyState = yyStates[yyTop];
        int yyM = yyLhs[yyN];
        if (yyState == 0 && yyM == 0) {
          if (yyDebug != null) yyDebug.shift(0, yyFinal);
          yyState = yyFinal;
          if (yyToken < 0) {
            yyToken = yyLex.Advance() ? yyLex.Token : 0;
            if (yyDebug != null)
               yyDebug.lex(yyState, yyToken,yyName(yyToken), yyLex.Value);
          }
          if (yyToken == 0) {
            if (yyDebug != null) yyDebug.accept(yyVal);
            return yyVal;
          }
          goto yyLoop;
        }
        if (((yyN = yyGindex[yyM]) != 0) && ((yyN += yyState) >= 0)
            && (yyN < yyTable.Length) && (yyCheck[yyN] == yyState))
          yyState = yyTable[yyN];
        else
          yyState = yyDgoto[yyM];
        if (yyDebug != null) yyDebug.shift(yyStates[yyTop], yyState);
	 goto yyLoop;
      }
    }
  }

#line 39 "recover.jay"
  /// <summary>
  ///   start with any argument to trace.
  /// </summary>
  public static void Main (string[] args) {
    yyDebug.yyDebug debug =
      args.Length > 0 ? new yyDebug.yyDebugAdapter() : null;
    yyInput scanner = new Scanner(Console.In);
    try {
      new Recover().yyParse(scanner, debug);
    } catch (yyException ye) {Console.WriteLine(ye); }
  }

  /// <summary>
  ///   converts input characters into parser symbols.
  /// </summary>
  protected class Scanner : yyInput {
    /// <summary>
    ///   input stream to scan.
    /// </summary>
    protected readonly TextReader In;
    
    /// <summary>
    ///   <c>true</c> once end of input is reached.
    ///   used to disallow further <c>Advance()</c>.
    /// </summary>
    protected bool AtEof = false;
    
    /// <summary>
    ///   maps rserved words to token values.
    /// </summary>
    protected Hashtable Symbols = new Hashtable();
    
    /// <summary>
    ///   represents current <c>Token</c>.
    /// </summary>
    private int token;
    
    /// <summary>
    ///   connect input stream, define reserved words.
    /// </summary>
    public Scanner (TextReader In) {
      this.In = In;
      Symbols["list"] = LIST;
      Symbols["opt"] = OPT;
      Symbols["seq"] = SEQ;
    }
  
    /// <summary>
    ///   move on to next token.
    ///   This method does the actual work of cutting up the input sequence.
    /// </summary>
    /// <returns><c>false</c> if positioned beyond tokens</returns>
    /// <exception><c>IOException</c> on input error</exception>
    public bool Advance () {
      int ch;
  
      if (AtEof) return false;
      for (;;)
	switch (ch = In.Read()) {
  
	case -1:
	  AtEof = true; return false;
  
	case ' ': case '\t':
	  continue;
  
	default:
	  if (Char.IsLetterOrDigit((char)ch)) {
            StringBuilder s = new StringBuilder();
            s.Append((char)ch);
            while (Char.IsLetterOrDigit((char)In.Peek()))
              s.Append((char)In.Read());
            Object o = Symbols[s.ToString()];
            if (o != null) { token = (int)o; return true; }
	    token = WORD; return true;
	  }
	  token = ch; return true;
	}
    }
  
    /// <summary>
    ///   classifies current token by <c>%token</c> value or single character.
    /// </summary>
    /// <remarks>
    ///   Should not be called if <c>Advance()</c> returned false.
    /// </remarks>
    public int Token {
      get { return token; }
    }

    /// <summary>
    ///   no value associated with current token.
    /// </summary>
    public object Value {
      get { return null; }
    }
  }
}
#line 572 "-"
