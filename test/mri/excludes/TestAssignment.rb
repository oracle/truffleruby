exclude :test_assign_private_self, "needs investigation"
exclude :test_massign_order, "<[:x1, :y1, :x2, :r1, :r2, [:z=, :r1], [:[]=, 1, 2, 3, 6], [:[]=, 4, :r2]]> expected but was"
exclude :test_massign_const_order, "<[:x1, :y1, :x2, [:[], 1, 2, 3], [:[], 4], :r1, :r2]> expected but was"
exclude :test_const_assign_order, "[RuntimeError] exception expected, not #<ArgumentError: bar>."
