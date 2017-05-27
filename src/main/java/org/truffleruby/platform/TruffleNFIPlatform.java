package org.truffleruby.platform;

import java.lang.reflect.Field;

import org.truffleruby.RubyContext;
import org.truffleruby.language.control.JavaException;

import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;

import sun.misc.Unsafe;

public class TruffleNFIPlatform {

    final TruffleObject defaultLibrary;

    private final Node readNode = Message.READ.createNode();
    private final Node asPointerNode = Message.AS_POINTER.createNode();

    public TruffleNFIPlatform(RubyContext context) {
        defaultLibrary = (TruffleObject) context.getEnv().parse(Source.newBuilder("default").mimeType("application/x-native").name("native").build()).call();
    }

    public TruffleObject getDefaultLibrary() {
        return defaultLibrary;
    }

    public TruffleObject lookup(TruffleObject library, String name) {
        try {
            return (TruffleObject) ForeignAccess.sendRead(readNode, library, name);
        } catch (UnknownIdentifierException | UnsupportedMessageException e) {
            throw new JavaException(e);
        }
    }

    public Object execute(TruffleObject function, Object... args) {
        try {
            return ForeignAccess.sendExecute(Message.createExecute(args.length).createNode(), function, args);
        } catch (UnsupportedMessageException | UnsupportedTypeException | ArityException e) {
            throw new JavaException(e);
        }
    }

    public Object invoke(TruffleObject receiver, String identifier, Object... args) {
        try {
            return ForeignAccess.sendInvoke(Message.createInvoke(args.length).createNode(), receiver, identifier, args);
        } catch (UnsupportedMessageException | UnsupportedTypeException | ArityException | UnknownIdentifierException e) {
            throw new JavaException(e);
        }
    }

    public long asPointer(TruffleObject function) {
        try {
            return ForeignAccess.sendAsPointer(asPointerNode, function);
        } catch (UnsupportedMessageException e) {
            throw new JavaException(e);
        }
    }

    public long allocate(long bytes) {
        final long address = UNSAFE.allocateMemory(bytes);
        UNSAFE.setMemory(address, bytes, (byte) 0);
        return address;
    }

    public void free(long address) {
        UNSAFE.freeMemory(address);
    }

    public void putLong(long address, long value) {
        UNSAFE.putLong(address, value);
    }

    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    private static final Unsafe UNSAFE = getUnsafe();

}
