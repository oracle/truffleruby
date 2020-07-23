module Rubykon
  class Group

    attr_reader :identifier, :stones, :liberties, :liberty_count

    NOT_SET = :not_set

    def initialize(id, stones = [id], liberties = {}, liberty_count = 0)
      @identifier    = id
      @stones        = stones
      @liberties     = liberties
      @liberty_count = liberty_count
    end
    
    def connect(stone_identifier, stone_group, group_tracker)
      return if stone_group == self
      if stone_group
        merge(stone_group, group_tracker)
      else
        add_stone(stone_identifier, group_tracker)
      end
      remove_connector_liberty(stone_identifier)
    end

    def gain_liberties_from_capture_of(captured_group, group_tracker)
      new_liberties = @liberties.select do |_identifier, stone_identifier|
        group_tracker.group_id_of(stone_identifier) == captured_group.identifier
      end
      new_liberties.each do |identifier, _group_id|
        add_liberty(identifier)
      end
    end

    def dup
      self.class.new @identifier, @stones.dup, @liberties.dup, @liberty_count
    end

    def add_liberty(identifier)
      return if already_counted_as_liberty?(identifier, Board::EMPTY)
      @liberties[identifier] = Board::EMPTY
      @liberty_count += 1
    end

    def remove_liberty(identifier)
      return if already_counted_as_liberty?(identifier, identifier)
      @liberties[identifier] = identifier
      @liberty_count -= 1
    end

    def caught?
      @liberty_count <= 0
    end

    def add_enemy_group_at(enemy_identifier)
      liberties[enemy_identifier] = enemy_identifier
    end
    
    private

    def merge(other_group, group_tracker)
      merge_stones(other_group, group_tracker)
      merge_liberties(other_group)
    end

    def merge_stones(other_group, group_tracker)
      other_group.stones.each do |identifier|
        add_stone(identifier, group_tracker)
      end
    end

    def merge_liberties(other_group)
      @liberty_count += other_group.liberty_count
      @liberties.merge!(other_group.liberties) do |_key, my_identifier, other_identifier|
        if shared_liberty?(my_identifier, other_identifier)
          @liberty_count -= 1
        end
        my_identifier
      end
    end

    def add_stone(identifier, group_tracker)
      group_tracker.stone_joins_group(identifier, @identifier)
      @stones << identifier
    end

    def shared_liberty?(my_identifier, other_identifier)
      my_identifier == Board::EMPTY || other_identifier == Board::EMPTY
    end

    def remove_connector_liberty(identifier)
      @liberties.delete(identifier)
      @liberty_count -= 1
    end

    def already_counted_as_liberty?(identifier, value)
      @liberties.fetch(identifier, NOT_SET) == value
    end
  end
end
