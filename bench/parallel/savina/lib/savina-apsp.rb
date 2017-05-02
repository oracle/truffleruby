# This code is derived from the Savina benchmark suite,
# maintained at https://github.com/shamsmahmood/savina.
# This benchmark is a Ruby version of the Scala benchmark
# "ApspAkkaActorBenchmark.scala" in that repository.
# The LICENSE is GPLv2 as the original benchmark:
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html

# Savina All-Pairs Shortest Path

#ARGS = [180, 30, 100]
#ARGS = [90, 30, 100] # Too short
#ARGS = [180, 60, 100] # 9 actors + main
ARGS = [180, 90, 100] # 4 actors

require_relative 'savina-common'

NUM_WORKERS, BLOCK_SIZE, MAX_EDGE_WEIGHT = ARGS

raise "bad params"  unless NUM_WORKERS % BLOCK_SIZE == 0
NUM_BLOCKS_IN_SINGLE_DIM = NUM_WORKERS / BLOCK_SIZE
NUM_BLOCKS = NUM_BLOCKS_IN_SINGLE_DIM * NUM_BLOCKS_IN_SINGLE_DIM

NeighborMessage = Struct.new(:neighbors)
InitialMessage = :initial
ResultMessage = Struct.new(:k, :block_id, :init_data)

class Matrix
  def initialize(n)
    @n = n
    @data = Array.new(n*n) { |k|
      i = k / n
      j = k % n
      yield(i,j)
    }
    freeze
  end

  def [](i,j)
    @data[i*@n+j]
  end
end

module ApspUtils
  def self.generate_graph
    random = MyRandom.new(NUM_WORKERS)
    Matrix.new(NUM_WORKERS) { |i,j|
      if j == i
        0
      else
        random.next_int(MAX_EDGE_WEIGHT)
      end
    }
  end

  GRAPH_DATA = generate_graph

  def self.get_block(block_id)
    global_start_row = (block_id / NUM_BLOCKS_IN_SINGLE_DIM) * BLOCK_SIZE
    global_start_col = (block_id % NUM_BLOCKS_IN_SINGLE_DIM) * BLOCK_SIZE
    Matrix.new(BLOCK_SIZE) { |i,j|
      GRAPH_DATA[global_start_row + i, global_start_col + j]
    }
  end

  def self.solve(graph)
    # Floyd-Warshall
    dist = Array.new(NUM_WORKERS) { |i|
      Array.new(NUM_WORKERS) { |j|
        GRAPH_DATA[i,j]
      }
    }
    NUM_WORKERS.times { |k|
      NUM_WORKERS.times { |i|
        NUM_WORKERS.times { |j|
          if dist[i][j] > dist[i][k] + dist[k][j]
            dist[i][j] = dist[i][k] + dist[k][j]
          end
        }
      }
    }
    dist
  end

  SOLUTION = solve(GRAPH_DATA)
end

class SavinaApsp < SavinaBenchmark
  def benchmark
    block_actors = Array.new(NUM_BLOCKS_IN_SINGLE_DIM) { |i|
      Array.new(NUM_BLOCKS_IN_SINGLE_DIM) { |j|
        block_id = i * NUM_BLOCKS_IN_SINGLE_DIM + j
        FloydWarshallActor.new(block_id)
      }
    }

    # create the links to the neighbors
    NUM_BLOCKS_IN_SINGLE_DIM.times { |i|
      NUM_BLOCKS_IN_SINGLE_DIM.times { |j|
        current = block_actors[i][j]
        neighbors = (block_actors.map { |row| row[j] } + block_actors[i])
        neighbors.delete current
        current.send! NeighborMessage.new(neighbors.freeze)
      }
    }

    # start the computation
    block_actors.each { |row| row.each { |actor| actor.send! InitialMessage } }

    Actor.await_all_actors

    block_actors
  end

  def self.verify(block_actors)
    result = Array.new(NUM_WORKERS) { Array.new(NUM_WORKERS, -1) }
    block_actors.each { |row|
      row.each { |actor|
        BLOCK_SIZE.times { |i|
          BLOCK_SIZE.times { |j|
            result[actor.row_offset+i][actor.col_offset + j] = actor.current_iter_data[i,j]
          }
        }
      }
    }
    unless result == ApspUtils::SOLUTION
      puts ApspUtils::SOLUTION.map(&:to_s)
      puts
      puts result.map(&:to_s)
      raise "Wrong result"
    end
    true
  end
end

class FloydWarshallActor < Actor
  NUM_NEIGHBORS = 2 * (NUM_BLOCKS_IN_SINGLE_DIM - 1)

  attr_reader :current_iter_data, :row_offset, :col_offset

  def initialize(block_id)
    @block_id = block_id
    @row_offset = (block_id / NUM_BLOCKS_IN_SINGLE_DIM) * BLOCK_SIZE
    @col_offset = (block_id % NUM_BLOCKS_IN_SINGLE_DIM) * BLOCK_SIZE
    @k = -1
    @neighbor_data_per_iteration = Array.new(NUM_BLOCKS, nil)
    @received = 0
    @current_iter_data = ApspUtils.get_block(block_id)
  end

  def process(message)
    case message
    when NeighborMessage
      @neighbors = message.neighbors
    when InitialMessage
      notify_neighbors
    when ResultMessage
      raise unless @neighbors.size == NUM_NEIGHBORS
      have_all_data = store_iteration_data(message.k, message.block_id, message.init_data)
      if have_all_data
        # received enough data from neighbors, can proceed to do computation for next k
        @k += 1

        perform_computation
        notify_neighbors
        @neighbor_data_per_iteration.fill(nil)
        @received = 0

        if @k == NUM_WORKERS - 1
          # we've completed the computation
          :exit
        end
      end
    end
  end

  def store_iteration_data(iteration, source_id, data_array)
    #raise [iteration,@k].inspect unless iteration == @k or iteration == -1
    @received += 1 unless @neighbor_data_per_iteration[source_id]
    @neighbor_data_per_iteration[source_id] = data_array
    @received == NUM_NEIGHBORS
  end

  def perform_computation
    prev_iter_data = @current_iter_data
    # make modifications on a fresh local data array for this iteration
    @current_iter_data = Matrix.new(BLOCK_SIZE) { |i,j|
      gi = @row_offset + i
      gj = @col_offset + j
      new_iter_data = element_at(gi, @k, prev_iter_data) + element_at(@k, gj, prev_iter_data)
      [prev_iter_data[i,j], new_iter_data].min
    }
  end

  def element_at(row, col, prev_iter_data)
    dest_block_id = (row / BLOCK_SIZE) * NUM_BLOCKS_IN_SINGLE_DIM + col / BLOCK_SIZE
    local_row = row % BLOCK_SIZE
    local_col = col % BLOCK_SIZE

    # puts "Accessing block-#{dest_block_id} from block-#{@block_id} for (#{row}, #{col})"

    if dest_block_id == @block_id
      prev_iter_data[local_row,local_col]
    else
      @neighbor_data_per_iteration[dest_block_id][local_row, local_col]
    end
  end

  def notify_neighbors
    # send the current result to all other blocks who might need it
    # note: this is inefficient version where data is sent to neighbors
    # who might not need it for the current value of k
    result_message = ResultMessage.new(@k, @block_id, @current_iter_data)
    @neighbors.each { |neighbor| neighbor.send!(result_message) }
  end
end
