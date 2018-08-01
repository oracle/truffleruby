// created by jay 1.0 (c) 2002 ats@cs.rit.edu
// skeleton c# 1.0 (c) 2002 ats@cs.rit.edu

#line 2 "arith.jay"
  namespace Arith {			// can first specify namespace
    // could specify using directives
    // [this has not been done here to stress-test the skeleton]
    
    /// <summary>
    ///   start with an argument to trace
    /// </summary>
    public class Arith {		// must specify class header
	// must not use yy[A-Z].* as identifiers
	// could overwrite some methods named yy[A-Z].* in subclass
#line 16 "-"
  // %token constants
  public const int Number = 99;
  public const int UNARY = 257;
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
//yyLhs 13
    -1,     1,     1,     1,     1,     1,     1,     1,     1,     0,
     0,     0,     0,
    }, yyLen = {
//yyLen 13
     2,     3,     3,     3,     3,     2,     2,     3,     1,     0,
     3,     2,     3,
    }, yyDefRed = {
//yyDefRed 23
     9,     0,     0,     8,     0,     0,     0,    11,     0,    12,
     5,     6,     0,     0,     0,     0,     0,    10,     7,     0,
     0,     3,     4,
    }, yyDgoto = {
//yyDgoto 2
     1,     8,
    }, yySindex = {
//yySindex 23
     0,   -10,    -7,     0,   -39,   -39,   -39,     0,    -5,     0,
     0,     0,   -19,   -39,   -39,   -39,   -39,     0,     0,   -40,
   -40,     0,     0,
    }, yyRindex = {
//yyRindex 23
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,    -2,
     3,     0,     0,
    }, yyGindex = {
//yyGindex 2
     0,     5,
    }, yyTable = {
//yyTable 247
     7,     6,    15,     9,     4,    17,     5,    16,     1,    10,
    11,    12,     0,     2,     0,     0,     0,     0,    19,    20,
    21,    22,    18,    15,    13,     0,    14,     0,    16,     0,
     6,     0,     0,     4,     0,     5,     0,    15,    13,     1,
    14,     1,    16,     1,     2,     0,     2,     0,     2,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     3,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     3,
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
     0,     0,     0,     0,     0,     0,     2,
    }, yyCheck = {
//yyCheck 247
    10,    40,    42,    10,    43,    10,    45,    47,    10,     4,
     5,     6,    -1,    10,    -1,    -1,    -1,    -1,    13,    14,
    15,    16,    41,    42,    43,    -1,    45,    -1,    47,    -1,
    40,    -1,    -1,    43,    -1,    45,    -1,    42,    43,    41,
    45,    43,    47,    45,    41,    -1,    43,    -1,    45,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    99,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    99,
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
    -1,    -1,    -1,    -1,    -1,    -1,   256,
    };

  /// <summary>
  ///   maps symbol value to printable name.
  ///   see <c>yyExpecting</c>
  /// </summary>
  protected static readonly string[] yyNames = {
    "end-of-file",null,null,null,null,null,null,null,null,null,"'\\n'",
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,"'('","')'","'*'","'+'",null,"'-'",null,"'/'",null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,"Number",null,null,null,null,null,null,
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
    null,null,null,null,null,null,null,null,null,null,null,"UNARY",
    };

  /// <summary>
  ///   printable rules for debugging.
  /// </summary>
  protected static readonly string [] yyRule = {
    "$accept : prog",
    "expr : expr '+' expr",
    "expr : expr '-' expr",
    "expr : expr '*' expr",
    "expr : expr '/' expr",
    "expr : '+' expr",
    "expr : '-' expr",
    "expr : '(' expr ')'",
    "expr : Number",
    "prog :",
    "prog : prog expr '\\n'",
    "prog : prog '\\n'",
    "prog : prog error '\\n'",
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
case 1:
#line 28 "arith.jay"
  { yyVal = ((double)yyVals[-2+yyTop]) + ((double)yyVals[0+yyTop]); }
  break;
case 2:
#line 29 "arith.jay"
  { yyVal = ((double)yyVals[-2+yyTop]) - ((double)yyVals[0+yyTop]); }
  break;
case 3:
#line 30 "arith.jay"
  { yyVal = ((double)yyVals[-2+yyTop]) * ((double)yyVals[0+yyTop]); }
  break;
case 4:
#line 31 "arith.jay"
  { yyVal = ((double)yyVals[-2+yyTop]) / ((double)yyVals[0+yyTop]); }
  break;
case 5:
#line 32 "arith.jay"
  { yyVal = yyVals[0+yyTop]; }
  break;
case 6:
#line 33 "arith.jay"
  { yyVal = - ((double)yyVals[0+yyTop]); }
  break;
case 7:
#line 34 "arith.jay"
  { yyVal = ((double)yyVals[-1+yyTop]); }
  break;
case 10:
#line 38 "arith.jay"
  { System.Console.WriteLine("\t"+((double)yyVals[-1+yyTop])); }
  break;
case 12:
#line 40 "arith.jay"
  { yyErrorFlag = 0; }
  break;
#line 440 "-"
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

#line 42 "arith.jay"
	// rest is emitted after yyParse()
      
    /// <summary>
    ///   start with any argument to trace.
    /// </summary>
    public static void Main (string[] args) {
      yyDebug.yyDebug debug =
        args.Length > 0 ? new yyDebug.yyDebugAdapter() : null;
      yyInput scanner = new Scanner(System.Console.In);
      try {
        new Arith().yyParse(scanner, debug);
      } catch (yyException ye) { System.Console.WriteLine(ye); }
    }
  
  } // must specify trailing } for parser class

  /// <summary>
  ///   converts input characters into parser symbols.
  /// </summary>
  /// <remarks>
  ///   This class could have been internal to the parser class.
  ///   It is external to demonstrate how to reference token values
  ///   and the input interface.
  /// </remarks>
  public class Scanner : Arith.yyInput {
    /// <summary>
    ///   input stream to scan.
    /// </summary>
    protected readonly System.IO.TextReader In;
    
    /// <summary>
    ///   <c>true</c> once end of input is reached.
    ///   used to disallow further <c>Advance()</c>.
    /// </summary>
    protected bool AtEof = false;
    
    /// <summary>
    ///   represents current <c>Token</c>.
    /// </summary>
    private int token;
    
    /// <summary>
    ///   represents current <c>Value</c>.
    /// </summary>
    private double value;
  
    /// <summary>
    ///   connect input stream.
    /// </summary>
    public Scanner (System.IO.TextReader In) {
      this.In = In;
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
	  if (System.Char.IsDigit((char)ch)) { // support decimal integer values as double
	    value = ch - '0';
	    while (System.Char.IsDigit((char)In.Peek())) {
	      value *= 10;
	      value += In.Read() - '0';
	    }
	    token = Arith.Number; return true;
	  }
	  value = token = ch; return true;
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
    ///   value associated with current token.
    /// </summary>
    /// <remarks>
    ///   Should not be called if <c>Advance()</c> returned false.
    /// </remarks>
    public object Value {
      get { return this.value; }
    }
  }
} // must specify trailing } for namespace, if any
#line 577 "-"
