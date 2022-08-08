package org.truffleruby.core.klass;

public class WithSingletonClass implements ClassLike {

    private final ClassLike underlying;

    public WithSingletonClass(ClassLike underlying) {
        this.underlying = underlying;
    }

    @Override
    public RubyClass reify() {
        throw new UnsupportedOperationException();
    }

}
