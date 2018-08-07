require_relative '../spec_helper'

describe "Magic comments" do
  
  [:main, :load, :require, :eval].each do |context|
    find_file = proc do |file|
      File.expand_path("fixtures/" + file, File.dirname(__FILE__))
    end
    
    case context
    when :main
      description = "in the main file"
      run = proc do |file, encoding='utf-8'|
        ruby_exe(File.read(find_file.call(file), encoding: encoding).gsub(/^\$magic_comment_result = /, 'print '))
      end
    when :load
      description = "in a loaded file"
      run = proc do |file|
        load find_file.call(file)
        $magic_comment_result
      end
    when :require
      description = "in a required file"
      run = proc do |file|
        require find_file.call(file)
        $magic_comment_result
      end
    when :eval
      description = "in an eval"
      run = proc do |file, encoding='utf-8'|
        eval(File.read(find_file.call(file), encoding: encoding).gsub(/$magic_comment_result = /, ''))
      end
    end
    
    describe description do
      it "are optional" do
        run.call('no_magic_comment.rb').should == Encoding::UTF_8.name
      end

      it "are case-insensitive" do
        run.call('case_magic_comment.rb').should == Encoding::Big5.name
      end

      it "must be at the first line" do
        run.call('second_line_magic_comment.rb').should == Encoding::UTF_8.name
      end

      it "must be the first token of the line" do
        run.call('second_token_magic_comment.rb').should == Encoding::UTF_8.name
      end

      it "can be after the shebang" do
        run.call('shebang_magic_comment.rb').should == Encoding::Big5.name
      end

      it "can take Emacs style" do
        run.call('emacs_magic_comment.rb').should == Encoding::Big5.name
      end

      it "can take vim style" do
        run.call('vim_magic_comment.rb').should == Encoding::Big5.name
      end

      it "determine __ENCODING__" do
        run.call('magic_comment.rb').should == Encoding::Big5.name
      end

      it "do not cause bytes to be mangled by passing them through the wrong encoding" do
        run.call('bytes_magic_comment.rb', 'ascii-8bit').should == [167, 65, 166, 110].inspect
      end
    end
  end
    
end
