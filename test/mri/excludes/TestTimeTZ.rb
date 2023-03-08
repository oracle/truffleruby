exclude :test_europe_berlin, "[ruby-core:67345] [Bug #10698]"
exclude :test_localtime_zone, "<\"SGT\"> expected but was"
exclude :test_utc_names, "ArgumentError: \"+HH:MM\", \"-HH:MM\", \"UTC\" or \"A\"..\"I\",\"K\"..\"Z\" expected for utc_offset: utc"
exclude :test_america_los_angeles, "TypeError: TruffleRuby doesn't have a case for the org.truffleruby.core.time.TimeNodesFactory$TimeSFromArrayPrimitiveNodeFactory$TimeSFromArrayPrimitiveNodeGen node with values of type Class(org.truffleruby.core.klass.RubyClass) org.truffleruby.language.Nil java.lang.Integer=0 java.lang.Integer=0 java.lang.Integer=1 java.lang.Integer=1 org.truffleruby.core.numeric.RubyBignum org.truffleruby.language.Nil java.lang.Integer=-1 java.lang.Boolean=false org.truffleruby.language.Nil"
