module Rubykon
  class GameScorer
    def score(game)
      game_score = {Board::BLACK => 0, Board::WHITE => game.komi}
      score_board(game, game_score)
      add_captures(game, game_score)
      determine_winner(game_score)
      game_score
    end

    private
    def score_board(game, game_score)
      board = game.board
      board.each do |identifier, color|
        if color == Board::EMPTY
          score_empty_cutting_point(identifier, board, game_score)
        else
          game_score[color] += 1
        end
      end
    end

    def score_empty_cutting_point(identifier, board, game_score)
      neighbor_colors = board.neighbour_colors_of(identifier)
      candidate_color = find_candidate_color(neighbor_colors)
      return unless candidate_color
      if only_one_color_adjacent?(neighbor_colors, candidate_color)
        game_score[candidate_color] += 1
      end
    end

    def find_candidate_color(neighbor_colors)
      neighbor_colors.find do |color|
        color != Board::EMPTY
      end
    end

    def only_one_color_adjacent?(neighbor_colors, candidate_color)
      enemy_color = Game.other_color(candidate_color)
      neighbor_colors.all? do |color|
        color != enemy_color
      end
    end

    def add_captures(game, game_score)
      game_score[Board::BLACK] += game.captures[Board::BLACK]
      game_score[Board::WHITE] += game.captures[Board::WHITE]
    end

    def determine_winner(game_score)
      game_score[:winner] = if black_won?(game_score)
                              Board::BLACK
                            else
                              Board::WHITE
                            end
    end

    def black_won?(game_score)
      game_score[Board::BLACK] > game_score[Board::WHITE]
    end
  end
end
