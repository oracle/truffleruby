A = 1

module Lib
  module Mod
  end
  class Base
  end
end

module App
  class Foo < Lib::Base
    include Lib::Mod
    CONST1 = Foo.new
    class Bar
      CONST2 = Bar.new
    end
  end

  CONST3 = Foo::Bar.new
  class Baz < App::Foo
    CONST4 = Foo::Bar.new
  end
end

__END__
# Classes
class Object
  A: Integer
end

module Lib
  module Mod
  end

  class Base
  end
end

module App
  CONST3: Foo::Bar

  class Foo < Lib::Base
    CONST1: Foo
    include Lib::Mod

    class Bar
      CONST2: Bar
    end
  end

  class Baz < Foo
    CONST4: Foo::Bar
  end
end
