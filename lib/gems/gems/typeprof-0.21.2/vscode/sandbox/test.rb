class C  
    def initialize
    end

    def get_foo
        :Foo_foo
    end

    def get_bar
        :Foo_bar
    end
end

class D
    def get_foo
        :Foo2_foo
    end
end

C.new.get_foo
C.new.get_bar

x = rand < 0.5 ? C.new : D.new
x.get_foo 