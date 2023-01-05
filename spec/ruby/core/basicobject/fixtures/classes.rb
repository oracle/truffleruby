module BasicObjectSpecs
  class IVars
    def initialize
      @secret = 99
    end
  end

  module InstExec
    def self.included(base)
      base.instance_exec { @@count = 2 }
    end
  end

  module InstExecIncluded
    include InstExec
  end

  module InstEvalCVar
    instance_eval { @@count = 2 }
  end

  module InstEval
    module Constants
      module ConstantInReceiverSingletonClass
        module ReceiverScope
          FOO = :ReceiverScope

          class ReceiverParent
            FOO = :ReceiverParent
          end

          class Receiver < ReceiverParent
            FOO = :Receiver

            def initialize
              self.singleton_class.const_set(:FOO, :singleton_class)
            end
          end
        end

        module CallerScope
          FOO = :CallerScope

          class CallerParent
            FOO = :CallerParent
          end

          class Caller < CallerParent
            FOO = :Caller

            def get_constant_with_string(receiver)
              receiver.instance_eval("FOO")
            end
          end
        end
      end

      module ConstantInReceiverClass
        module ReceiverScope
          FOO = :ReceiverScope

          class ReceiverParent
            FOO = :ReceiverParent
          end

          class Receiver < ReceiverParent
            FOO = :Receiver
          end
        end

        module CallerScope
          FOO = :CallerScope

          class CallerParent
            FOO = :CallerParent
          end

          class Caller < CallerParent
            FOO = :Caller

            def get_constant_with_string(receiver)
              receiver.instance_eval("FOO")
            end
          end
        end
      end

      module ConstantInCallerClass
        module ReceiverScope
          FOO = :ReceiverScope

          class ReceiverParent
            FOO = :ReceiverParent
          end

          class Receiver < ReceiverParent
            # FOO is not declared in a receiver class
          end
        end

        module CallerScope
          FOO = :CallerScope

          class CallerParent
            FOO = :CallerParent
          end

          class Caller < CallerParent
            FOO = :Caller

            def get_constant_with_string(receiver)
              receiver.instance_eval("FOO")
            end
          end
        end
      end

      module ConstantInCallerOuterScopes
        module ReceiverScope
          FOO = :ReceiverScope

          class ReceiverParent
            FOO = :ReceiverParent
          end

          class Receiver < ReceiverParent
            # FOO is not declared in a receiver class
          end
        end

        module CallerScope
          FOO = :CallerScope

          class CallerParent
            FOO = :CallerParent
          end

          class Caller < CallerParent
            # FOO is not declared in a caller class

            def get_constant_with_string(receiver)
              receiver.instance_eval("FOO")
            end
          end
        end
      end

      module ConstantInReceiverParentClass
        module ReceiverScope
          FOO = :ReceiverScope

          class ReceiverParent
            FOO = :ReceiverParent
          end

          class Receiver < ReceiverParent
            # FOO is not declared in a receiver class
          end
        end

        module CallerScope
          # FOO is not declared in a caller outer scopes

          class CallerParent
            FOO = :CallerParent
          end

          class Caller < CallerParent
            # FOO is not declared in a caller class

            def get_constant_with_string(receiver)
              receiver.instance_eval("FOO")
            end
          end
        end
      end
    end
  end

  class InstEvalConst
    INST_EVAL_CONST_X = 2
  end

  module InstEvalOuter
    module Inner
      obj = InstEvalConst.new
      X_BY_BLOCK = obj.instance_eval { INST_EVAL_CONST_X } rescue nil
    end
  end
end
