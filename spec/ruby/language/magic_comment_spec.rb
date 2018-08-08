require_relative '../spec_helper'

# See core/kernel/eval_spec.rb for more magic comments specs for eval()
describe :magic_comments, shared: true do
  it "are optional" do
    @object.call('no_magic_comment.rb').should == Encoding::UTF_8.name
  end

  it "are case-insensitive" do
    @object.call('case_magic_comment.rb').should == Encoding::Big5.name
  end

  it "must be at the first line" do
    @object.call('second_line_magic_comment.rb').should == Encoding::UTF_8.name
  end

  it "must be the first token of the line" do
    @object.call('second_token_magic_comment.rb').should == Encoding::UTF_8.name
  end

  it "can be after the shebang" do
    @object.call('shebang_magic_comment.rb').should == Encoding::Big5.name
  end

  it "can take Emacs style" do
    @object.call('emacs_magic_comment.rb').should == Encoding::Big5.name
  end

  it "can take vim style" do
    @object.call('vim_magic_comment.rb').should == Encoding::Big5.name
  end

  it "determine __ENCODING__" do
    @object.call('magic_comment.rb').should == Encoding::Big5.name
  end

  it "do not cause bytes to be mangled by passing them through the wrong encoding" do
    @object.call('bytes_magic_comment.rb', 'ascii-8bit').should == [167, 65, 166, 110].inspect
  end
end

describe "Magic comments" do
  describe "in the main file" do
    it_behaves_like :magic_comments, nil, proc { |file, encoding='utf-8'|
      print_at_exit = fixture(__FILE__, "print_magic_comment_result_at_exit.rb")
      ruby_exe(fixture(__FILE__, file), options: "-r#{print_at_exit}")
    }
  end

  describe "in a loaded file" do
    it_behaves_like :magic_comments, nil, proc { |file|
      load fixture(__FILE__, file)
      $magic_comment_result
    }
  end

  describe "in a required file" do
    it_behaves_like :magic_comments, nil, proc { |file|
      require fixture(__FILE__, file)
      $magic_comment_result
    }
  end

  describe "in an eval" do
    it_behaves_like :magic_comments, nil, proc { |file, encoding='utf-8'|
      eval(File.read(fixture(__FILE__, file), encoding: encoding).gsub("$magic_comment_result = ", ''))
    }
  end
end
