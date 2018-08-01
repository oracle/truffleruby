namespace yyDebug {
  using System;
  using System.IO;
  
  public interface yyDebug {
    void push (int state, object value);
    void lex (int state, int token, string name, object value);
    void shift (int from, int to, int errorFlag);
    void pop (int state);
    void discard (int state, int token, string name, object value);
    void reduce (int from, int to, int rule, string text, int len);
    void shift (int from, int to);
    void accept (object value);
    void error (string message);
    void reject ();
  }
	 
  public class yyDebugAdapter : yyDebug {
    protected TextWriter Out;
    
    public yyDebugAdapter (TextWriter Out) { this.Out = Out; }
    
    public yyDebugAdapter () : this(Console.Out) { }
    
    public void push (int state, object value) {
      Out.WriteLine("push\tstate "+state+"\tvalue "+value);
    }
    
    public void lex (int state, int token, string name, object value) {
      Out.WriteLine("lex\tstate "+state+"\treading "+name+"\tvalue "+value);
    }
    
    public void shift (int from, int to, int errorFlag) {
      switch (errorFlag) {
      default:				// normally
        Out.WriteLine("shift\tfrom state "+from+" to "+to);
        break;
      case 0: case 1: case 2:		// in error recovery
        Out.WriteLine("shift\tfrom state "+from+" to "+to
   			     +"\t"+errorFlag+" left to recover");
        break;
      case 3:				// normally
        Out.WriteLine("shift\tfrom state "+from+" to "+to+"\ton error");
        break;
      }
    }
    
    public void pop (int state) {
      Out.WriteLine("pop\tstate "+state+"\ton error");
    }
    
    public void discard (int state, int token, string name, object value) {
      Out.WriteLine("discard\tstate "+state+"\ttoken "+name+"\tvalue "+value);
    }
    
    public void reduce (int from, int to, int rule, string text, int len) {
      Out.WriteLine("reduce\tstate "+from+"\tuncover "+to
            +"\trule ("+rule+") "+text);
    }
    
    public void shift (int from, int to) {
      Out.WriteLine("goto\tfrom state "+from+" to "+to);
    }
    
    public void accept (object value) {
      Out.WriteLine("accept\tvalue "+value);
    }
    
    public void error (string message) {
      Out.WriteLine("error\t"+message);
    }
    
    public void reject () {
      Out.WriteLine("reject");
    }
    
  }
}
