exclude :test_massign_const_order, "<[:x1, :y1, :x2, [:[], 1, 2, 3], [:[], 4], :r1, :r2]> expected but was <[:x1, :y1, [:[], 4], :r1, :r2, :x2, [:[], 1, 2, 3]]>."
exclude :test_massign_order, "<[:x1, :y1, :x2, :r1, :r2, [:z=, :r1], [:[]=, 1, 2, 3, [6, 7]], [:[]=, 4, :r2]]> expected but was <[:x1, :y1, :r1, :r2, [:z=, :r1], :x2, [:[]=, 1, 2, 3, [6, 7]], [:[]=, 4, :r2]]>."
