class A
  class B
    class C
      def get_a
        A.new
      end
      def get_b
        B.new
      end
      def get_c
        C.new
      end
    end
  end
end

__END__
# Classes
class A
  class B
    class C
      def get_a: -> A
      def get_b: -> B
      def get_c: -> C
    end
  end
end
