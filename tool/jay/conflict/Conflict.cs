// created by jay 1.0.2 (c) 2002-2004 ats@cs.rit.edu
// skeleton c# 1.0.2 (c) 2002-2004 ats@cs.rit.edu

  // %token constants
  public const int IF = 257;
  public const int THEN = 258;
  public const int ELSE = 259;
  public const int Variable = 260;
  public const int Constant = 261;
  public const int yyErrorCode = 256;

  /// <summary>
  ///   final state of parser.
  /// </summary>
  protected const int yyFinal = 3;

  /// <summary>
  ///   parser tables.
  ///   Order is mandated by jay.
  /// </summary>
  protected static readonly short[] yyLhs = {
//yyLhs 8
    -1,     0,     0,     0,     0,     1,     1,     2,
    }, yyLen = {
//yyLen 8
     2,     4,     6,     3,     3,     1,     3,     1,
    }, yyDefRed = {
//yyDefRed 16
     0,     0,     0,     0,     7,     0,     0,     0,     0,     0,
     3,     0,     0,     6,     0,     2,
    }, yyDgoto = {
//yyDgoto 3
     3,     5,     6,
    }, yySindex = {
//yySindex 16
  -255,  -258,   -57,     0,     0,  -251,   -52,  -258,  -255,  -258,
     0,   -52,  -249,     0,  -255,     0,
    }, yyRindex = {
//yyRindex 16
     0,     0,     0,     0,     0,     0,  -246,     0,     0,     0,
     0,     1,    13,     0,     0,     0,
    }, yyGindex = {
//yyGindex 3
    -8,     7,     2,
    }, yyTable = {
//yyTable 261
    12,     4,     1,     4,     7,     2,    15,     8,     9,    11,
    14,    13,     5,     1,    10,     0,     0,     0,     0,     0,
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
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
     4,
    }, yyCheck = {
//yyCheck 261
     8,     0,   257,   261,    61,   260,    14,   258,    60,     7,
   259,     9,   258,     0,     7,    -1,    -1,    -1,    -1,    -1,
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
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
   259,
    };

  /// <summary>
  ///   maps symbol value to printable name.
  ///   see <c>yyExpecting</c>
  /// </summary>
  protected static readonly string[] yyNames = {
    "end-of-file",null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,null,null,null,null,null,null,null,null,
    null,null,null,null,null,null,"'<'","'='",null,null,null,null,null,
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
    null,null,null,null,null,null,null,null,"IF","THEN","ELSE","Variable",
    "Constant",
    };

//t  /// <summary>
//t  ///   printable rules for debugging.
//t  /// </summary>
//t  protected static readonly string [] yyRule = {
//t    "$accept : statement",
//t    "statement : IF condition THEN statement",
//t    "statement : IF condition THEN statement ELSE statement",
//t    "statement : Variable '=' condition",
//t    "statement : Variable '=' expression",
//t    "condition : expression",
//t    "condition : expression '<' expression",
//t    "expression : Constant",
//t    };
//t
//t  /// <summary>
//t  ///   debugging support, requires <c>yyDebug</c>.
//t  ///   Set to <c>null</c> to suppress debugging messages.
//t  /// </summary>
//t  protected yyDebug.yyDebug yyDebug;
//t
//t  /// <summary>
//t  ///   index-checked interface to <c>yyNames[]</c>.
//t  /// </summary>
//t  /// <param name='token'>single character or <c>%token</c> value</param>
//t  /// <returns>token name or <c>[illegal]</c> or <c>[unknown]</c></returns>
//t  public static string yyName (int token) {
//t    if ((token < 0) || (token > yyNames.Length)) return "[illegal]";
//t    string name;
//t    if ((name = yyNames[token]) != null) return name;
//t    return "[unknown]";
//t  }
//t
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
//t    this.yyDebug = (yyDebug.yyDebug)yyDebug;
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
//t      if (yyDebug != null) yyDebug.push(yyState, yyVal);

      yyDiscarded: for (;;) {	// discarding a token does not change stack
        int yyN;
        if ((yyN = yyDefRed[yyState]) == 0) {	// else [default] reduce (yyN)
          if (yyToken < 0) {
            yyToken = yyLex.Advance() ? yyLex.Token : 0;
//t            if (yyDebug != null)
//t              yyDebug.lex(yyState, yyToken, yyName(yyToken), yyLex.Value);
          }
          if ((yyN = yySindex[yyState]) != 0 && ((yyN += yyToken) >= 0)
              && (yyN < yyTable.Length) && (yyCheck[yyN] == yyToken)) {
//t            if (yyDebug != null)
//t              yyDebug.shift(yyState, yyTable[yyN], yyErrorFlag > 0 ? yyErrorFlag-1 : 0);
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
//t              if (yyDebug != null) yyDebug.error("syntax error");
              goto case 1;
            case 1: case 2:
              yyErrorFlag = 3;
              do {
                if ((yyN = yySindex[yyStates[yyTop]]) != 0
                    && (yyN += yyErrorCode) >= 0 && yyN < yyTable.Length
                    && yyCheck[yyN] == yyErrorCode) {
//t                  if (yyDebug != null)
//t                    yyDebug.shift(yyStates[yyTop], yyTable[yyN], 3);
                  yyState = yyTable[yyN];
                  yyVal = yyLex.Value;
                  goto yyLoop;
                }
//t                if (yyDebug != null) yyDebug.pop(yyStates[yyTop]);
              } while (-- yyTop >= 0);
//t              if (yyDebug != null) yyDebug.reject();
              throw new yyException("irrecoverable syntax error");
  
            case 3:
              if (yyToken == 0) {
//t                if (yyDebug != null) yyDebug.reject();
                throw new yyException("irrecoverable syntax error at end-of-file");
              }
//t              if (yyDebug != null)
//t                yyDebug.discard(yyState, yyToken, yyName(yyToken),
//t  							yyLex.Value);
              yyToken = -1;
              goto yyDiscarded;		// leave stack alone
            }
        }
        int yyV = yyTop + 1-yyLen[yyN];
//t        if (yyDebug != null)
//t          yyDebug.reduce(yyState, yyStates[yyV-1], yyN, yyRule[yyN], yyLen[yyN]);
        yyVal = yyDefault(yyV > yyTop ? null : yyVals[yyV]);
        switch (yyN) {
        }
        yyTop -= yyLen[yyN];
        yyState = yyStates[yyTop];
        int yyM = yyLhs[yyN];
        if (yyState == 0 && yyM == 0) {
//t          if (yyDebug != null) yyDebug.shift(0, yyFinal);
          yyState = yyFinal;
          if (yyToken < 0) {
            yyToken = yyLex.Advance() ? yyLex.Token : 0;
//t            if (yyDebug != null)
//t               yyDebug.lex(yyState, yyToken,yyName(yyToken), yyLex.Value);
          }
          if (yyToken == 0) {
//t            if (yyDebug != null) yyDebug.accept(yyVal);
            return yyVal;
          }
          goto yyLoop;
        }
        if (((yyN = yyGindex[yyM]) != 0) && ((yyN += yyState) >= 0)
            && (yyN < yyTable.Length) && (yyCheck[yyN] == yyState))
          yyState = yyTable[yyN];
        else
          yyState = yyDgoto[yyM];
//t        if (yyDebug != null) yyDebug.shift(yyStates[yyTop], yyState);
	 goto yyLoop;
      }
    }
  }

