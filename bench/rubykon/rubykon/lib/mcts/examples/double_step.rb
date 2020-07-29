module MCTS
  module Examples
    class DoubleStep

      FINAL_POSITION = 6
      STEPS = [1, 2]

      attr_reader :black, :white

      def initialize(black = 0, white = 0, n = 0)
        @black = black
        @white = white
        @move_count = n
      end

      def finished?
        @white >= FINAL_POSITION || @black >= FINAL_POSITION
      end

      def generate_move
        STEPS.sample
      end

      def set_move(move)
        steps = move
        case next_turn_color
        when :white
          @white += steps
        else
          @black += steps
        end
        @move_count += 1
      end

      def dup
        self.class.new @black, @white, @move_count
      end

      def won?(color)
        fail "Game not finished" unless finished?
        case color
        when :black
          @black > @white
        else
          @white > @black
        end
      end

      def all_valid_moves
        if finished?
          []
        else
          [1, 2]
        end
      end

      def last_turn_color
        @move_count.odd? ? :black : :white
      end

      private
      def next_turn_color
        @move_count.even? ? :black : :white
      end
    end
  end
end
