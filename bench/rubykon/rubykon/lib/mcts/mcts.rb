module MCTS
  class MCTS
    def start(game_state, playouts = DEFAULT_PLAYOUTS)
      root = Root.new(game_state)

      playouts.times do |i|
        root.explore_tree
      end
      root
    end
  end
end
