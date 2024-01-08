package org.prism;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Parser {

    public static class ParsingOptions {
        /** the name of the file that is currently being parsed */
        private final byte[] path;
        /** the line within the file that the parser starts on. This value is 0-indexed */
        private final int startLineNumber;
        /** the name of the encoding that the source file is in */
        private final byte[] encoding;
        /** whether or not the frozen string literal option has been set */
        private final boolean isFrozenStringLiteral;
        /** whether or not we should suppress warnings. */
        private final boolean isSuppressWarnings;
        /** code of Ruby version which syntax will be used to parse */
        private final byte syntaxVersion;
        /** scopes surrounding the code that is being parsed with local variable names defined in every scope */
        private final byte[][][] scopes;

        public ParsingOptions(
                byte[] path,
                int startLineNumber,
                byte[] encoding,
                boolean isFrozenStringLiteral,
                boolean isSuppressWarnings,
                byte syntaxVersion,
                byte[][][] scopes) {
            this.path = path;
            this.startLineNumber = startLineNumber;
            this.encoding = encoding;
            this.isFrozenStringLiteral = isFrozenStringLiteral;
            this.isSuppressWarnings = isSuppressWarnings;
            this.syntaxVersion = syntaxVersion;
            this.scopes = scopes;
        }

        public byte[] serialize() {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();

            // path
            output.writeBytes(serializeInt(path.length));
            output.writeBytes(path);

            // line number
            output.writeBytes(serializeInt(startLineNumber));

            // encoding
            output.writeBytes(serializeInt(encoding.length));
            output.writeBytes(encoding);

            // isFrozenStringLiteral
            if (isFrozenStringLiteral) {
                output.write(1);
            } else {
                output.write(0);
            }

            // isSuppressWarnings
            if (isSuppressWarnings) {
                output.write(1);
            } else {
                output.write(0);
            }

            // version
            output.write(syntaxVersion);

            // scopes

            // number of scopes
            output.writeBytes(serializeInt(scopes.length));
            // local variables in each scope
            for (byte[][] scope : scopes) {
                // number of locals
                output.writeBytes(serializeInt(scope.length));

                // locals
                for (byte[] local : scope) {
                    output.writeBytes(serializeInt(local.length));
                    output.writeBytes(local);
                }
            }

            return output.toByteArray();
        }

        private byte[] serializeInt(int n) {
            ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder());
            buffer.putInt(n);
            return buffer.array();
        }
    }

    public static void loadLibrary(String path) {
        System.load(path);
    }

    public static native byte[] parseAndSerialize(byte[] source, byte[] options);

    public static byte[] parseAndSerialize(byte[] source) {
        return parseAndSerialize(source, (byte[]) null);
    }

    public static byte[] parseAndSerialize(byte[] source, ParsingOptions options) {
        if (options != null) {
            return parseAndSerialize(source, options.serialize());
        } else {
            return parseAndSerialize(source);
        }
    }

}
