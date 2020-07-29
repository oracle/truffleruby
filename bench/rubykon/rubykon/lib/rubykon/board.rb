# Board it acts a bit like a giant 2 dimensional array - but one based
# not zero based
module Rubykon
  class Board
    include Enumerable

    BLACK = :black
    WHITE = :white
    EMPTY = nil

    attr_reader :size, :board

    # weird constructor for dup
    def initialize(size, board = create_board(size))
      @size  = size
      @board = board
    end

    def each
      @board.each_with_index do |color, identifier|
        yield identifier, color
      end
    end

    def cutting_point_count
      @board.size
    end

    def [](identifier)
      @board[identifier]
    end

    def []=(identifier, color)
      @board[identifier] = color
    end

    # this method is rather raw and explicit, it gets called a lot
    def neighbours_of(identifier)
      x                        = identifier % size
      y                        = identifier / size
      right                    = identifier + 1
      below                    = identifier + @size
      left                     = identifier - 1
      above                    = identifier - @size
      board_edge               = @size - 1
      not_on_x_edge            = x > 0 && x < board_edge
      not_on_y_edge            = y > 0 && y < board_edge

      if not_on_x_edge && not_on_y_edge
        [[right, @board[right]], [below, @board[below]],
         [left, @board[left]], [above, @board[above]]]
      else
        handle_edge_cases(x, y, above, below, left, right, board_edge,
                          not_on_x_edge, not_on_y_edge)
      end
    end

    def neighbour_colors_of(identifier)
      neighbours_of(identifier).map {|identifier, color| color}
    end

    def diagonal_colors_of(identifier)
      diagonal_coordinates(identifier).inject([]) do |res, n_identifier|
        res << self[n_identifier] if on_board?(n_identifier)
        res
      end
    end

    def on_edge?(identifier)
      x, y = x_y_from identifier
      x == 1 || x == size || y == 1 || y == size
    end

    def on_board?(identifier)
      identifier >= 0 && identifier < @board.size
    end
    
    COLOR_TO_CHARACTER = {BLACK => ' X', WHITE => ' O', EMPTY => ' .'}
    CHARACTER_TO_COLOR = COLOR_TO_CHARACTER.invert
    LEGACY_CONVERSION  = {'X' => ' X', 'O' => ' O', '-' => ' .'}
    CHARS_PER_GLYPH    = 2

    def ==(other_board)
      board == other_board.board
    end

    def to_s
      @board.each_slice(@size).map do |row|
        row_chars = row.map do |color|
          COLOR_TO_CHARACTER.fetch(color)
        end
        row_chars.join
      end.join("\n") << "\n"
    end

    def dup
      self.class.new @size, @board.dup
    end

    MAKE_IT_OUT_OF_BOUNDS = 1000

    def identifier_for(x, y)
      return nil if x.nil? || y.nil?
      x = MAKE_IT_OUT_OF_BOUNDS if x > @size || x < 1
      (y - 1) * @size + (x - 1)
    end

    def x_y_from(identifier)
      x = (identifier % (@size)) + 1
      y = (identifier / (@size)) + 1
      [x, y]
    end

    def self.from(string)
      new_board = new(string.index("\n") / CHARS_PER_GLYPH)
      each_move_from(string) do |index, color|
        new_board[index] = color
      end
      new_board
    end

    def self.each_move_from(string)
      glyphs = string.tr("\n", '').chars.each_slice(CHARS_PER_GLYPH).map(&:join)
      relevant_glyphs = glyphs.select do |glyph|
        CHARACTER_TO_COLOR.has_key?(glyph)
      end
      relevant_glyphs.each_with_index do |glyph, index|
        yield index, CHARACTER_TO_COLOR.fetch(glyph)
      end
    end

    def self.convert(old_board_string)
      old_board_string.gsub /[XO-]/, LEGACY_CONVERSION
    end

    private

    def create_board(size)
      Array.new(size * size, EMPTY)
    end

    def handle_edge_cases(x, y, above, below, left, right, board_edge, not_on_x_edge, not_on_y_edge)
      left_edge   = x == 0
      right_edge  = x == board_edge
      top_edge    = y == 0
      bottom_edge = y == board_edge
      if left_edge && not_on_y_edge
        [[right, @board[right]], [below, @board[below]],
         [above, @board[above]]]
      elsif right_edge && not_on_y_edge
        [[below, @board[below]],
         [left, @board[left]], [above, @board[above]]]
      elsif top_edge && not_on_x_edge
        [[right, @board[right]], [below, @board[below]],
         [left, @board[left]]]
      elsif bottom_edge && not_on_x_edge
        [[right, @board[right]],
         [left, @board[left]], [above, @board[above]]]
      else
        handle_corner_case(above, below, left, right, bottom_edge, left_edge, right_edge, top_edge)
      end
    end

    def handle_corner_case(above, below, left, right, bottom_edge, left_edge, right_edge, top_edge)
      if left_edge && top_edge
        [[right, @board[right]], [below, @board[below]]]
      elsif left_edge && bottom_edge
        [[above, @board[above]], [right, @board[right]]]
      elsif right_edge && top_edge
        [[left, @board[left]], [below, @board[below]]]
      elsif right_edge && bottom_edge
        [[left, @board[left]], [above, @board[above]]]
      end
    end

    def diagonal_coordinates(identifier)
      x = identifier % size
      if x == 0
        [identifier + 1 - @size, identifier + 1 + @size]
      elsif x == size - 1
        [identifier - 1 - @size, identifier - 1 + @size]
      else
        [identifier - 1 - @size, identifier - 1 + @size,
         identifier + 1 - @size, identifier + 1 + @size]
      end
    end
  end
end
