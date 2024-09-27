exclude :test_encoding_euc_jp, "<\"\\x{A4A2},\\x{A4A4},\\x{A4A6}\\n\"> expected but was <\"\\xA4\\xA2,\\xA4\\xA4,\\xA4\\xA6\\n\">."
exclude :test_encoding_utf8, "<\"あ,い,う\\n\"> expected but was <\"\\xE3\\x81\\x82,\\xE3\\x81\\x84,\\xE3\\x81\\x86\\n\">."
exclude :test_with_default_internal, "<\"\\x{A4A2},\\x{A4A4},\\x{A4A6}\\n\"> expected but was <\"\\xA4\\xA2,\\xA4\\xA4,\\xA4\\xA6\\n\">."
