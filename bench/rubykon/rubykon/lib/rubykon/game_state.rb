module Rubykon
  class GameState

    attr_reader :game

    def initialize(game = Game.new,
                   validator = MoveValidator.new,
                   eye_detector = EyeDetector.new)
      @game = game
      @validator = validator
      @eye_detector = eye_detector
    end

    def finished?
      @game.finished?
    end

    def generate_move
      generate_random_move
    end

    def set_move(move)
      identifier = move.first
      color = move.last
      @game.set_valid_move identifier, color
    end

    def dup
      self.class.new @game.dup, @validator, @eye_detector
    end

    def won?(color)
      score[:winner] == color
    end

    def all_valid_moves
      color = @game.next_turn_color
      @game.board.inject([]) do |valid_moves, (identifier, _field_color)|
        valid_moves << [identifier, color] if plausible_move?(identifier, color)
        valid_moves
      end
    end

    def score
      @score ||= GameScorer.new.score(@game)
    end

    def last_turn_color
      Game.other_color(next_turn_color)
    end

    private
    def generate_random_move
      color = @game.next_turn_color
      cp_count   = @game.board.cutting_point_count
      start_point = rand(cp_count)
      identifier = start_point
      passes = 0

      until searched_whole_board?(identifier, passes, start_point) ||
        plausible_move?(identifier, color) do
        if identifier >= cp_count - 1
          identifier = 0
          passes += 1
        else
          identifier += 1
        end
      end

      if searched_whole_board?(identifier, passes, start_point)
        pass_move(color)
      else
        [identifier, color]
      end
    end

    def searched_whole_board?(identifier, passes, start_point)
      passes > 0 && identifier >= start_point
    end

    def pass_move(color)
      [nil, color]
    end

    def next_turn_color
      @game.next_turn_color
    end

    def plausible_move?(identifier, color)
      @validator.trusted_valid?(identifier, color, @game) && !@eye_detector.is_eye?(identifier, @game.board)
    end
  end
end
