require_relative 'rubykon/lib/rubykon'

game_state = Rubykon::GameState.new Rubykon::Game.new(19)
mcts = MCTS::MCTS.new

benchmark do
  mcts.start game_state, 1_000
end
