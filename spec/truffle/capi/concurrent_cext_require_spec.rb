describe "Requiring C extensions concurrently" do
  it "is thread-safe" do
    out = ruby_exe(fixture(__FILE__ , 'concurrent_cext_require.rb'))
    out.should == "success\n"
    $?.success?.should == true
  end
end
