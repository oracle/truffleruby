module Rubykon
  class GTPCoordinateConverter

    X_CHARS = ('A'..'Z').reject { |c| c == 'I'.freeze }

    def initialize(board)
      @board = board
    end

    def from(string)
      x = string[0]
      y = string[1..-1]
      x_index = X_CHARS.index(x) + 1
      y_index = @board.size - y.to_i + 1
      @board.identifier_for(x_index, y_index)
    end

    def to(index)
      x, y = @board.x_y_from(index)
      x_char = X_CHARS[x - 1]
      y_index = @board.size - y + 1
      "#{x_char}#{y_index}"
    end
  end
end