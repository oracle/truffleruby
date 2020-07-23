module MCTS
  class Playout

    attr_reader :game_state

    def initialize(game_state)
      @game_state = game_state.dup
    end

    def play
      my_color = @game_state.last_turn_color
      playout
      @game_state.won?(my_color)
    end

    def playout
      until @game_state.finished?
        @game_state.set_move @game_state.generate_move
      end
    end
  end
end