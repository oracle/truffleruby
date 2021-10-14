require "diff/lcs"

class T; end
Diff::LCS.diff([T.new]+[T.new], [T.new]+[T.new]) {}
