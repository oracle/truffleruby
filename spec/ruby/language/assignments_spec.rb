require_relative '../spec_helper'

# Should be synchronized with spec/ruby/language/optional_assignments_spec.rb
describe 'Assignments' do
  describe 'using +=' do
    describe 'using an accessor' do
      before do
        klass = Class.new { attr_accessor :b }
        @a    = klass.new
      end

      it 'does evaluate receiver only once when assigns' do
        ScratchPad.record []
        @a.b = 1

        (ScratchPad << :evaluated; @a).b += 2

        ScratchPad.recorded.should == [:evaluated]
        @a.b.should == 3
      end

      it 'ignores method visibility when receiver is self' do
        klass_with_private_methods = Class.new do
          def initialize(n) @a = n end
          def public_method(n); self.a += n end
          private
          def a; @a end
          def a=(n) @a = n; 42 end
        end

        a = klass_with_private_methods.new(0)
        a.public_method(2).should == 2
      end
    end

    describe 'using a #[]' do
      it 'evaluates receiver only once when assigns' do
        ScratchPad.record []
        a = {k: 1}

        (ScratchPad << :evaluated; a)[:k] += 2

        ScratchPad.recorded.should == [:evaluated]
        a[:k].should == 3
      end

      it 'ignores method visibility when receiver is self' do
        klass_with_private_methods = Class.new do
          def initialize(h) @a = h end
          def public_method(k, n); self[k] += n end
          private
          def [](k) @a[k] end
          def []=(k, v) @a[k] = v; 42 end
        end

        a = klass_with_private_methods.new(k: 0)
        a.public_method(:k, 2).should == 2
      end
    end

    describe 'using compounded constants' do
      it 'causes side-effects of the module part to be applied only once (when assigns)' do
        module ConstantSpecs
          OpAssignTrue = 1
        end

        suppress_warning do # already initialized constant
          x = 0
          (x += 1; ConstantSpecs)::OpAssignTrue += 2
          x.should == 1
          ConstantSpecs::OpAssignTrue.should == 3
        end

        ConstantSpecs.send :remove_const, :OpAssignTrue
      end
    end
  end
end
