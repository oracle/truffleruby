require "io/console"
require "io/wait"

module Optcarrot
  # Input driver for terminal (this is a joke feature)
  class TermInput < Input
    def init
      $stdin.raw!
      $stdin.getc if $stdin.ready?
      @escape = false
      @ticks = { start: 0, select: 0, a: 0, b: 0, right: 0, left: 0, down: 0, up: 0 }
    end

    def dispose
      $stdin.cooked!
    end

    def keydown(pads, code, frame)
      event(pads, :keydown, code, 0)
      @ticks[code] = frame
    end

    def tick(frame, pads)
      while $stdin.ready?
        ch = $stdin.getbyte
        if @escape
          @escape = false
          case ch
          when 0x5b then @escape = true
          when 0x41 then keydown(pads, :up, frame)
          when 0x42 then keydown(pads, :down, frame)
          when 0x43 then keydown(pads, :right, frame)
          when 0x44 then keydown(pads, :left, frame)
          end
        else
          case ch
          when 0x1b then @escape = true
          when 0x58, 0x78 then keydown(pads, :a, frame)
          when 0x5a, 0x7a then keydown(pads, :b, frame)
          when 0x0d       then keydown(pads, :select, frame)
          when 0x20       then keydown(pads, :start, frame)
          when 0x51, 0x71 then exit
          end
        end
      end

      @ticks.each do |code, prev_frame|
        event(pads, :keyup, code, 0) if prev_frame + 5 < frame
      end
    end
  end
end
