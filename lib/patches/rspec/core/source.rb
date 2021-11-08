# truffleruby_primitives: true

# Copyright © 2012 Chad Humphries, David Chelimsky, Myron Marston
# Copyright © 2009 Chad Humphries, David Chelimsky
# Copyright © 2006 David Chelimsky, The RSpec Development Team
# Copyright © 2005 Steven Baker
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

require Primitive.get_original_require(__FILE__)

module RSpec
  module Core
    class Source
      def nodes_by_line_number
        @nodes_by_line_number ||= Hash.new { [] }
      end

      def tokens_by_line_number
        @tokens_by_line_number ||= Hash.new { [] }
      end
    end
  end
end
