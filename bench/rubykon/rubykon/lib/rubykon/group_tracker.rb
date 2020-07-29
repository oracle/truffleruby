module Rubykon
  class GroupTracker

    attr_reader :groups, :stone_to_group

    def initialize(groups = {}, stone_to_group = {})
      @groups         = groups
      @stone_to_group = stone_to_group
    end

    def assign(identifier, color, board)
      neighbours_by_color = color_to_neighbour(board, identifier)
      join_group_of_friendly_stones(neighbours_by_color[color], identifier)
      create_own_group(identifier) unless group_id_of(identifier)
      add_liberties(neighbours_by_color[Board::EMPTY], identifier)
      take_liberties_of_enemies(neighbours_by_color[Game.other_color(color)], identifier, board, color)
    end

    def liberty_count_at(identifier)
      group_of(identifier).liberty_count
    end

    def group_id_of(identifier)
      @stone_to_group[identifier]
    end

    def group_of(identifier)
      group(group_id_of(identifier))
    end

    def group(id)
      @groups[id]
    end

    def stone_joins_group(stone_identifier, group_identifier)
      @stone_to_group[stone_identifier] = group_identifier
    end

    def dup
      self.class.new(dup_groups, @stone_to_group.dup)
    end

    private
    def color_to_neighbour(board, identifier)
      neighbors = board.neighbours_of(identifier)
      hash = neighbors.inject({}) do |hash, (n_identifier, color)|
        (hash[color] ||= []) << n_identifier
        hash
      end
      hash.default = []
      hash
    end

    def take_liberties_of_enemies(enemy_neighbours, identifier, board, capturer_color)
      my_group = group_of(identifier)
      captures = enemy_neighbours.inject([]) do |caught, enemy_identifier|
        enemy_group = group_of(enemy_identifier)
        remove_liberties(enemy_identifier, enemy_group, identifier, my_group)
        collect_captured_groups(caught, enemy_group)
      end
      remove_caught_groups(board, capturer_color, captures)
    end

    def remove_liberties(enemy_identifier, enemy_group, identifier, my_group)
      enemy_group.remove_liberty(identifier)
      # this needs to be the identifier and not the group, as groups
      # might get merged
      my_group.add_enemy_group_at(enemy_identifier)
    end

    def collect_captured_groups(caught, enemy_group)
      if enemy_group.caught? && !caught.include?(enemy_group)
        caught << enemy_group
      end
      caught
    end

    def remove_caught_groups(board, capturer_color, caught)
      captures = caught.inject([]) do |captures, enemy_group|
        captures + remove(enemy_group, board)
      end
      captures
    end

    def remove(enemy_group, board)
      regain_liberties_from_capture(enemy_group)
      delete_group(enemy_group)
      remove_captured_stones(board, enemy_group)
    end

    def remove_captured_stones(board, enemy_group)
      captured_stones = enemy_group.stones
      captured_stones.each do |identifier|
        @stone_to_group.delete identifier
        board[identifier] = Board::EMPTY
      end
      captured_stones
    end

    def delete_group(enemy_group)
      @groups.delete(enemy_group.identifier)
    end

    def regain_liberties_from_capture(enemy_group)
      neighboring_groups_of(enemy_group).each do |neighbor_group|
        neighbor_group.gain_liberties_from_capture_of(enemy_group, self)
      end
    end

    def neighboring_groups_of(enemy_group)
      enemy_group.liberties.map do |identifier, _|
        group_of(identifier)
      end.compact.uniq
    end

    def add_liberties(liberties, identifier)
      liberties.each do |liberty_identifier|
        group_of(identifier).add_liberty(liberty_identifier)
      end
    end

    def join_group_of_friendly_stones(friendly_stones, identifier)
      friendly_stones.each do |f_identifier, _color|
        friendly_group = group_of(f_identifier)
        friendly_group.connect identifier, group_of(identifier), self
      end
    end

    def create_own_group(identifier)
      # we can use the identifier of the stone, as it should not be taken
      # (it may have been taken before, but for that stone to be played the
      # other group would have had to be captured before)
      @groups[identifier] = Group.new(identifier)
      @stone_to_group[identifier] = identifier
    end

    def dup_groups
      @groups.inject({}) do |dupped, (key, group)|
        dupped[key] = group.dup
        dupped
      end
    end
  end
end