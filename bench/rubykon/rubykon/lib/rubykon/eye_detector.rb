module Rubykon
  class EyeDetector
    def is_eye?(identifier, board)
      candidate_eye_color = candidate_eye_color(identifier, board)
      return false unless candidate_eye_color
      is_real_eye?(identifier, board, candidate_eye_color)
    end

    def candidate_eye_color(identifier, board)
      neighbor_colors = board.neighbour_colors_of(identifier)
      candidate_eye_color = neighbor_colors.first
      return false if candidate_eye_color == Board::EMPTY
      if neighbor_colors.all? {|color| color == candidate_eye_color}
        candidate_eye_color
      else
        nil
      end
    end

    private
    def is_real_eye?(identifier, board, candidate_eye_color)
      enemy_color = Game.other_color(candidate_eye_color)
      enemy_count = board.diagonal_colors_of(identifier).count(enemy_color)
      (enemy_count < 1) || (!board.on_edge?(identifier) && enemy_count < 2)
    end
  end
end
