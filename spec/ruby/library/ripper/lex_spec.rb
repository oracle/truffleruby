require_relative '../../spec_helper'
require 'ripper'

describe "Ripper.lex" do
  it "lexes a simple method declaration" do
    expected = ['[[1, 0], :on_kw, "def", #<Ripper::Lexer::State: EXPR_FNAME>]',
                '[[1, 3], :on_sp, " ", #<Ripper::Lexer::State: EXPR_FNAME>]',
                '[[1, 4], :on_ident, "m", #<Ripper::Lexer::State: EXPR_ENDFN>]',
                '[[1, 5], :on_lparen, "(", #<Ripper::Lexer::State: EXPR_BEG|EXPR_LABEL>]',
                '[[1, 6], :on_ident, "a", #<Ripper::Lexer::State: EXPR_ARG>]',
                '[[1, 7], :on_rparen, ")", #<Ripper::Lexer::State: EXPR_ENDFN>]',
                '[[1, 8], :on_sp, " ", #<Ripper::Lexer::State: EXPR_BEG>]',
                '[[1, 9], :on_kw, "nil", #<Ripper::Lexer::State: EXPR_END>]',
                '[[1, 12], :on_sp, " ", #<Ripper::Lexer::State: EXPR_END>]',
                '[[1, 13], :on_kw, "end", #<Ripper::Lexer::State: EXPR_END>]']
    lexed = Ripper.lex("def m(a) nil end")
    lexed.map(&:inspect).should == expected
  end
end
