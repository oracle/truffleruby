module MCTS
  class Node
    attr_reader :parent, :move, :wins, :visits, :children, :game_state

    def initialize(game_state, move, parent)
      @parent        = parent
      @game_state    = game_state
      @move          = move
      @wins          = 0.0
      @visits        = 0
      @children      = []
      @untried_moves = game_state.all_valid_moves
      @leaf          = game_state.finished? || @untried_moves.empty?
    end

    def uct_value
      win_percentage + UCT_BIAS_FACTOR * Math.sqrt(Math.log(parent.visits)/@visits)
    end

    def win_percentage
      @wins/@visits
    end

    def root?
      false
    end

    def leaf?
      @leaf
    end

    def uct_select_child
      children.max_by &:uct_value
    end

    # maybe get a maximum depth or soemthing in
    def expand
      move = @untried_moves.pop
      create_child(move)
    end

    def rollout
      playout = Playout.new(@game_state)
      playout.play
    end

    def won
      @visits += 1
      @wins += 1
    end

    def lost
      @visits += 1
    end

    def backpropagate(won)
      node = self
      node.update_won won
      until node.root? do
        won = !won # switching players perspective
        node = node.parent
        node.update_won(won)
      end
    end

    def untried_moves?
      !@untried_moves.empty?
    end

    def update_won(won)
      if won
        self.won
      else
        self.lost
      end
    end

    private

    def create_child(move)
      game_state = @game_state.dup
      game_state.set_move(move)
      child = Node.new game_state, move, self
      @children << child
      child
    end
  end
end
