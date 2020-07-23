require_relative 'mcts/node'
require_relative 'mcts/root'
require_relative 'mcts/playout'
require_relative 'mcts/mcts'

module MCTS
  UCT_BIAS_FACTOR = 2
  DEFAULT_PLAYOUTS = 1_000
end


