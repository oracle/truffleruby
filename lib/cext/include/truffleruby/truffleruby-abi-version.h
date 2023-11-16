#ifndef TRUFFLERUBY_ABI_VERSION_H
#define TRUFFLERUBY_ABI_VERSION_H

// The TruffleRuby ABI version must be of the form:
// * For releases, i.e. on a release/graal-vm/X.Y branch:
//   $RUBY_VERSION.$GRAALVM_VERSION.$ABI_NUMBER e.g. 3.2.2.23.1.0.1
// * For non-release:
//   $RUBY_VERSION.$ABI_NUMBER e.g. 3.2.2.1
//
// $RUBY_VERSION must be the same as TruffleRuby.LANGUAGE_VERSION.
// $ABI_NUMBER starts at 1 and is incremented for every ABI-incompatible change.

#define TRUFFLERUBY_ABI_VERSION "3.2.2.5"

#endif
