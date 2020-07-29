module Rubykon
  class Game
    attr_reader :board, :group_tracker, :move_count, :ko, :captures
    attr_accessor :komi

    DEFAULT_KOMI = 6.5

    # the freakish constructor is here so that we can have a decent dup
    def initialize(size = 19, komi = DEFAULT_KOMI, board = Board.new(size),
                   move_count = 0, consecutive_passes = 0,
                   ko = nil, captures = initial_captures,
                   move_validator = MoveValidator.new,
                   group_tracker = GroupTracker.new)
      @board              = board
      @komi               = komi
      @move_count         = move_count
      @consecutive_passes = consecutive_passes
      @ko                 = ko
      @captures           = captures
      @move_validator     = move_validator
      @group_tracker      = group_tracker
    end

    def play(x, y, color)
      identifier = @board.identifier_for(x, y)
      if valid_move?(identifier, color)
        set_valid_move(identifier, color)
        true
      else
        false
      end
    end

    def play!(x, y, color)
      raise IllegalMoveException unless play(x, y, color)
    end

    def no_moves_played?
      @move_count == 0
    end

    def next_turn_color
      move_count.even? ? Board::BLACK : Board::WHITE
    end

    def finished?
      @consecutive_passes >= 2
    end

    def set_valid_move(identifier, color)
      @move_count += 1
      if Game.pass?(identifier)
        @consecutive_passes += 1
      else
        set_move(color, identifier)
      end
    end

    def safe_set_move(identifier, color)
      return if color == Board::EMPTY
      set_valid_move(identifier, color)
    end

    def dup
      self.class.new @size, @komi, @board.dup, @move_count, @consecutive_passes,
                     @ko, @captures.dup, @move_validator, @group_tracker.dup
    end

    def self.other_color(color)
      if color == :black
        :white
      else
        :black
      end
    end

    def self.pass?(identifier)
      identifier.nil?
    end

    def self.from(string)
      game = new(string.index("\n") / Board::CHARS_PER_GLYPH)
      Board.each_move_from(string) do |identifier, color|
        game.safe_set_move(identifier, color)
      end
      game
    end

    private
    def initial_captures
      {Board::BLACK => 0, Board::WHITE => 0}
    end

    def valid_move?(identifier, color)
      @move_validator.valid?(identifier, color, self)
    end

    def set_move(color, identifier)
      @board[identifier] = color
      potential_eye = EyeDetector.new.candidate_eye_color(identifier, @board)
      captures = @group_tracker.assign(identifier, color, board)
      determine_ko_move(captures, potential_eye)
      @captures[color] += captures.size
      @consecutive_passes = 0
    end

    def determine_ko_move(captures, potential_eye)
      if captures.size == 1 && potential_eye
        @ko = captures[0]
      else
        @ko = nil
      end
    end
  end
end
