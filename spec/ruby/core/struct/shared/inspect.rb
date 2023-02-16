describe :struct_inspect, shared: true do
  it "returns a string representation without the class name for anonymous structs" do
    Struct.new(:a).new("").send(@method).should == '#<struct a="">'
  end

  it "returns a string representation without the class name for structs nested in anonymous classes" do
    obj = Object.new
    obj.singleton_class.class_eval <<~DOC
      class Foo < Struct.new(:a); end
    DOC

    obj.singleton_class::Foo.new("").send(@method).should == '#<struct a="">'
  end

  it "returns a string representation without the class name for structs nested in anonymous modules" do
    m = Module.new
    m.module_eval <<~DOC
      class Foo < Struct.new(:a); end
    DOC

    m::Foo.new("").send(@method).should == '#<struct a="">'
  end
end
