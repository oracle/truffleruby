module MCTS
  class Root < Node
    def initialize(game_state)
      super game_state, nil, nil
    end

    def root?
      true
    end

    def best_child
      children.max_by &:win_percentage
    end

    def best_move
      best_child.move
    end

    def explore_tree
      selected_node = select
      playout_node =  if selected_node.leaf?
                        selected_node
                      else
                        selected_node.expand
                      end
      won = playout_node.rollout
      playout_node.backpropagate(won)
    end

    def update_won(won)
      # logic reversed as the node accumulates its children and has no move
      # of its own
      if won
        self.lost
      else
        self.won
      end
    end

    private
    def select
      node = self
      until node.untried_moves? || node.leaf? do
        node = node.uct_select_child
      end
      node
    end
  end
end
