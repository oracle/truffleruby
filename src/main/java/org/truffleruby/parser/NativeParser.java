package org.truffleruby.parser;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.RubyContext;
import org.truffleruby.platform.TruffleNFIPlatform;

public class NativeParser {

    private final Object parse_and_pack;

    public NativeParser(RubyContext context) {
        final Object parserLibrary = context
                .getEnv()
                .parseInternal(Source.newBuilder("nfi", "load '/Users/chrisseaton/Documents/ruby-parser/build/libparse.dylib'", "native").build())
                .call();
        final Object symbol = context.getTruffleNFI().lookup(parserLibrary, "parse_and_pack");
        parse_and_pack = TruffleNFIPlatform.bind(context, symbol, String.format("(string):string"));
    }

    public void runParseAndPack(String source) {
        try {
            System.err.println(">>");
            System.err.println(InteropLibrary.getUncached().execute(parse_and_pack, source));
            System.err.println("<<");
        } catch (InteropException e) {
            throw CompilerDirectives.shouldNotReachHere(e);
        }
    }

}
