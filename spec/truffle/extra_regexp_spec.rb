describe "Regexp" do

  it "does not reset enclosed capture groups" do
    /((a)|(b))+/.match("ab").captures.should == [ "b", "a", "b" ]
  end

  it "allows forward references" do
    /(?:(\2)|(.))+/.match("aa").captures.should == [ "a", "a" ]
  end

  it "disallows forward references >= 10" do
    (/\10()()()()()()()()()()/ =~ "\x08").should == 0
  end

  it "ignores backreferences > 1000" do
    /\99999/.match("99999")[0].should == "99999"
  end

  it "allows numeric subexpression calls" do
    /(a)\g<1>/.match("aa").to_a.should == [ "aa", "a" ]
  end

  it "allows numeric conditional backreferences" do
    /(a)(?(1)a|b)/.match("aa").to_a.should == [ "aa", "a" ]
    /(a)(?(<1>)a|b)/.match("aa").to_a.should == [ "aa", "a" ]
    /(a)(?('1')a|b)/.match("aa").to_a.should == [ "aa", "a" ]
  end

  it "allows either <> or '' in named conditional backreferences" do
    -> { Regexp.new("(?<a>a)(?(a)a|b)") }.should raise_error(RegexpError)
    /(?<a>a)(?(<a>)a|b)/.match("aa").to_a.should == [ "aa", "a" ]
    /(?<a>a)(?('a')a|b)/.match("aa").to_a.should == [ "aa", "a" ]
  end

  it "allows negative numeric backreferences" do
    /(a)\k<-1>/.match("aa").to_a.should == [ "aa", "a" ]
    /(a)\g<-1>/.match("aa").to_a.should == [ "aa", "a" ]
    /(a)(?(<-1>)a|b)/.match("aa").to_a.should == [ "aa", "a" ]
    /(a)(?('-1')a|b)/.match("aa").to_a.should == [ "aa", "a" ]
  end

  it "delimited numeric backreferences can start with 0" do
    /(a)\k<01>/.match("aa").to_a.should == [ "aa", "a" ]
    /(a)\g<01>/.match("aa").to_a.should == [ "aa", "a" ]
    /(a)(?(01)a|b)/.match("aa").to_a.should == [ "aa", "a" ]
    /(a)(?(<01>)a|b)/.match("aa").to_a.should == [ "aa", "a" ]
    /(a)(?('01')a|b)/.match("aa").to_a.should == [ "aa", "a" ]
  end

  it "regular numeric backreferences cannot start with 0" do
    /(a)\01/.match("aa").should == nil
    /(a)\01/.match("a\x01").to_a.should == [ "a\x01", "a" ]
  end

  it "group names cannot start with digits or minus" do
    -> { Regexp.new("(?<1a>a)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<-a>a)") }.should raise_error(RegexpError)
  end

  it "0 is not a valid backreference" do
    -> { Regexp.new("\\k<0>") }.should raise_error(RegexpError)
  end

  it "named capture groups invalidate numeric backreferences" do
    -> { Regexp.new("(?<a>a)\\1") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a>a)\\k<1>") }.should raise_error(RegexpError)
    -> { Regexp.new("(a)(?<a>a)\\1") }.should raise_error(RegexpError)
    -> { Regexp.new("(a)(?<a>a)\\k<1>") }.should raise_error(RegexpError)
  end

  it "allows + and - in group names and referential constructs that don't use levels, i.e. subexpression calls" do
    /(?<a+>a)\g<a+>/.match("aa").to_a.should == [ "aa", "a" ]
    /(?<a+b>a)\g<a+b>/.match("aa").to_a.should == [ "aa", "a" ]
    /(?<a+1>a)\g<a+1>/.match("aa").to_a.should == [ "aa", "a" ]
    /(?<a->a)\g<a->/.match("aa").to_a.should == [ "aa", "a" ]
    /(?<a-b>a)\g<a-b>/.match("aa").to_a.should == [ "aa", "a" ]
    /(?<a-1>a)\g<a-1>/.match("aa").to_a.should == [ "aa", "a" ]
  end

  it "treats + or - as the beginning of a level specifier in \\k<> backreferences and (?(...)...|...) conditional backreferences" do
    -> { Regexp.new("(?<a+>a)\\k<a+>") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a+b>a)\\k<a+b>") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a+1>a)\\k<a+1>") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a->a)\\k<a->") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a-b>a)\\k<a-b>") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a-1>a)\\k<a-1>") }.should raise_error(RegexpError)

    -> { Regexp.new("(?<a+>a)(?(<a+>)a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a+b>a)(?(<a+b>)a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a+1>a)(?(<a+1>)a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a->a)(?(<a->)a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a-b>a)(?(<a-b>)a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a-1>a)(?(<a-1>)a|b)") }.should raise_error(RegexpError)

    -> { Regexp.new("(?<a+>a)(?('a+')a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a+b>a)(?('a+b')a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a+1>a)(?('a+1')a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a->a)(?('a-')a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a-b>a)(?('a-b')a|b)") }.should raise_error(RegexpError)
    -> { Regexp.new("(?<a-1>a)(?('a-1')a|b)") }.should raise_error(RegexpError)
  end

  it "handles incomplete range quantifiers" do
    /a{}/.match("a{}")[0].should == "a{}"
    /a{,}/.match("a{,}")[0].should == "a{,}"
    /a{1/.match("a{1")[0].should == "a{1"
    /a{1,2/.match("a{1,2")[0].should == "a{1,2"
    /a{,5}/.match("aaa")[0].should == "aaa"
  end

  it "handles three digit octal escapes starting with 0" do
    /[\000-\b]/.match("\x00")[0].should == "\x00"
  end

  it "handles control escapes" do
    /\c*\cJ\cj/.match("\n\n\n")[0].should == "\n\n\n"
    /\C-*\C-J\C-j/.match("\n\n\n")[0].should == "\n\n\n"
  end

  it "handles forward references" do
    /(?:(\2)|(.))+/.match("aa").to_a.should == [ "aa", "a", "a" ]
  end

  # Works in the REPL, but not here. Maybe \R is disabled in test mode
  # because of 2.6.6 compatibility.
  # it "correctly parses \\R and processes line breaks" do
  #   /\R/.match("\n")
  # end

  it "supports the \\K keep operator" do
    /a\Kb/.match("ab")[0].should == "b"
  end

  it "lets us use quantifiers on assertions" do
    /a^?b/.match("ab")[0].should == "ab"
    /a$?b/.match("ab")[0].should == "ab"
    /a\A?b/.match("ab")[0].should == "ab"
    /a\Z?b/.match("ab")[0].should == "ab"
    /a\z?b/.match("ab")[0].should == "ab"
    /a\G?b/.match("ab")[0].should == "ab"
    /a\b?b/.match("ab")[0].should == "ab"
    /a\B?b/.match("ab")[0].should == "ab"
    /a(?=c)?b/.match("ab")[0].should == "ab"
    /a(?!=b)?b/.match("ab")[0].should == "ab"
    /a(?<=c)?b/.match("ab")[0].should == "ab"
    /a(?<!a)?b/.match("ab")[0].should == "ab"
  end

  it "does not delete optional assertions" do
    /(?=(a))?/.match("a").to_a.should == [ "", "a" ]
  end

  it "supports nested quantifiers" do
    /a***/.match("aaa")[0].should == "aaa"

    # a+?* should be equivalent to (?:a+?)?
    # this quantifiers take the first 'a' greedily and the others non-greedily
    /a+?*/.match("")[0].should      == ""
    /(?:a+?)?/.match("")[0].should  == ""

    /a+?*/.match("a")[0].should      == "a"
    /(?:a+?)?/.match("a")[0].should  == "a"

    /a+?*/.match("aa")[0].should     == "a"
    /(?:a+?)?/.match("aa")[0].should == "a"

    # both a**? and a+*? should be equivalent to (?:a+)??
    # this quantifier would rather match nothing, but if that's not possible,
    # it will greedily take everything
    /a**?/.match("")[0].should     == ""
    /a+*?/.match("")[0].should     == ""
    /(?:a+)??/.match("")[0].should == ""

    /a**?/.match("aaa")[0].should     == ""
    /a+*?/.match("aaa")[0].should     == ""
    /(?:a+)??/.match("aaa")[0].should == ""

    /b.**?b/.match("baaabaaab")[0].should     == "baaabaaab"
    /b.+*?b/.match("baaabaaab")[0].should     == "baaabaaab"
    /b(?:.+)??b/.match("baaabaaab")[0].should == "baaabaaab"
  end
end
