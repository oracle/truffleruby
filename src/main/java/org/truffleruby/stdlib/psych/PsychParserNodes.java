/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * This code is modified from the Psych JRuby extension module
 * implementation with the following header:
 *
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2010 Charles O Nutter <headius@headius.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 */
package org.truffleruby.stdlib.psych;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.adapters.InputStreamAdapter;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringOperations;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.DocumentEndEvent;
import org.yaml.snakeyaml.events.DocumentStartEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.Event.ID;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;
import org.yaml.snakeyaml.parser.Parser;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.parser.ParserImpl;
import org.yaml.snakeyaml.reader.ReaderException;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.scanner.ScannerException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@CoreClass("Psych::Parser")
public abstract class PsychParserNodes {

    private static final int STYLE_PLAIN = 1;
    private static final int STYLE_SINGLE_QUOTED = 2;
    private static final int STYLE_DOUBLE_QUOTED = 3;
    private static final int STYLE_LITERAL = 4;
    private static final int STYLE_FOLDED = 5;
    private static final int STYLE_ANY = 0;
    private static final int STYLE_FLOW = 2;
    private static final int STYLE_NOT_FLOW = 1;

    private static int translateStyle(Character style) {
        switch (style) {
            case 0:
                return STYLE_PLAIN;
            case '\'':
                return STYLE_SINGLE_QUOTED;
            case '"':
                return STYLE_DOUBLE_QUOTED;
            case '|':
                return STYLE_LITERAL;
            case '>':
                return STYLE_FOLDED;
            default:
                return STYLE_ANY;
        }
    }

    private static int translateFlowStyle(Boolean flowStyle) {
        if (flowStyle == null) {
            return STYLE_ANY;
        } else if (flowStyle) {
            return STYLE_FLOW;
        } else {
            return STYLE_NOT_FLOW;
        }
    }

    private static DynamicObject stringOrNilFor(RubyContext context, String value) {
        if (value == null) {
            return context.getCoreLibrary().getNil();
        } else {
            return stringFor(context, value);
        }
    }

    @TruffleBoundary
    private static DynamicObject stringFor(RubyContext context, String value) {
        Encoding encoding = context.getEncodingManager().getDefaultInternalEncoding();
        if (encoding == null) {
            encoding = UTF8Encoding.INSTANCE;
        }

        return StringOperations.createString(context, StringOperations.encodeRope(value, encoding));
    }

    @Primitive(name = "pysch_create_parser", needsSelf = false)
    public abstract static class CreateParserNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(input)")
        protected Parser createParserString(DynamicObject input) {
            final StreamReader reader = newStringReader(StringOperations.rope(input));
            return new ParserImpl(reader);
        }

        @TruffleBoundary
        @Specialization(guards = "!isRubyString(input)")
        protected Parser createParserIO(DynamicObject input) {
            final StreamReader reader = newStreamReader(input);
            return new ParserImpl(reader);
        }

        private StreamReader newStringReader(Rope rope) {
            return new StreamReader(new InputStreamReader(
                    new ByteArrayInputStream(rope.getBytes()), rope.getEncoding().getCharset()));
        }

        private StreamReader newStreamReader(DynamicObject io) {
            final Encoding enc = UTF8Encoding.INSTANCE;
            return new StreamReader(new InputStreamReader(
                    new InputStreamAdapter(getContext(), io), enc.getCharset()));
        }

    }

    @CoreMethod(names = "get_event", needsSelf = false, required = 1)
    public abstract static class GetEventNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Event getEvent(Parser parser) {
            return parser.getEvent();
        }
    }

    @CoreMethod(names = "event?", needsSelf = false, required = 2)
    public abstract static class IsEventNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization(guards = "isRubySymbol(eventName)")
        protected boolean isEvent(Event event, DynamicObject eventName) {
            final ID eventID = Event.ID.valueOf(Layouts.SYMBOL.getString(eventName));
            return event.is(eventID);
        }
    }

    @CoreMethod(names = "doc_start_info", needsSelf = false, required = 1, needsBlock = true)
    public abstract static class DocStartInfoNode extends YieldingCoreMethodNode {
        @TruffleBoundary
        @Specialization
        protected DynamicObject docStartInfo(DocumentStartEvent startEvent, DynamicObject block) {
            final DynamicObject versionArray;

            if (startEvent.getVersion() == null) {
                versionArray = createArray(null, 0);
            } else {
                versionArray = createArray(new int[]{ startEvent.getVersion().major(), startEvent.getVersion().minor() }, 2);
            }

            final Map<String, String> tagsMap = startEvent.getTags();
            if (tagsMap != null && !tagsMap.isEmpty()) {
                for (Map.Entry<String, String> tag : tagsMap.entrySet()) {
                    final Object key = stringFor(getContext(), tag.getKey());
                    final Object value = stringFor(getContext(), tag.getValue());
                    yield(block, key, value);
                }
            }

            boolean explicit = startEvent.getExplicit();
            return createArray(new Object[]{ versionArray, explicit }, 2);
        }
    }

    @CoreMethod(names = "doc_end_explicit?", needsSelf = false, required = 1)
    public abstract static class DocEndExplicitNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean docEndExplicit(DocumentEndEvent event) {
            return event.getExplicit();
        }
    }

    @CoreMethod(names = "alias_anchor", needsSelf = false, required = 1)
    public abstract static class AliasAnchorNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected DynamicObject aliasAnchor(AliasEvent event) {
            return stringOrNilFor(getContext(), event.getAnchor());
        }
    }

    @CoreMethod(names = "scalar_info", needsSelf = false, required = 1)
    public abstract static class ScalarInfoNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected DynamicObject scalarInfo(ScalarEvent scalarEvent) {
            final Object anchor = stringOrNilFor(getContext(), scalarEvent.getAnchor());
            final Object tag = stringOrNilFor(getContext(), scalarEvent.getTag());
            final Object plain_implicit = scalarEvent.getImplicit().canOmitTagInPlainScalar();
            final Object quoted_implicit = scalarEvent.getImplicit().canOmitTagInNonPlainScalar();
            final Object style = translateStyle(scalarEvent.getStyle());
            final Object value = stringFor(getContext(), scalarEvent.getValue());

            final Object[] store = new Object[]{ value, anchor, tag, plain_implicit, quoted_implicit, style };
            return createArray(store, store.length);
        }
    }

    @CoreMethod(names = "seq_start_info", needsSelf = false, required = 1)
    public abstract static class SeqStartInfoNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected DynamicObject seqStartInfo(SequenceStartEvent sequenceStartEvent) {
            final Object anchor = stringOrNilFor(getContext(), sequenceStartEvent.getAnchor());
            final Object tag = stringOrNilFor(getContext(), sequenceStartEvent.getTag());
            final Object implicit = sequenceStartEvent.getImplicit();
            final Object style = translateFlowStyle(sequenceStartEvent.getFlowStyle());

            final Object[] store = new Object[]{ anchor, tag, implicit, style };
            return createArray(store, store.length);
        }
    }

    @CoreMethod(names = "mapping_start_info", needsSelf = false, required = 1)
    public abstract static class MappingStartInfoNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected DynamicObject mappingStartInfo(MappingStartEvent mappingStartEvent) {
            final Object anchor = stringOrNilFor(getContext(), mappingStartEvent.getAnchor());
            final Object tag = stringOrNilFor(getContext(), mappingStartEvent.getTag());
            final Object implicit = mappingStartEvent.getImplicit();
            final Object style = translateFlowStyle(mappingStartEvent.getFlowStyle());

            final Object[] store = new Object[]{ anchor, tag, implicit, style };
            return createArray(store, store.length);
        }
    }

    @CoreMethod(names = "parse_exception_info", needsSelf = false, required = 1)
    public abstract static class ParseExceptionInfoNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected DynamicObject parseExceptionInfo(DynamicObject exception) {
            final Throwable throwable = Layouts.EXCEPTION.getBacktrace(exception).getJavaThrowable();
            if (throwable instanceof ParserException || throwable instanceof ScannerException) {
                final MarkedYAMLException pe = (MarkedYAMLException) throwable;
                final Mark mark = pe.getProblemMark();
                final DynamicObject problem = stringOrNilFor(getContext(), pe.getProblem());
                final DynamicObject context = stringOrNilFor(getContext(), pe.getContext());

                final Object[] store = new Object[]{ mark.getLine(), mark.getColumn(), mark.getIndex(), problem, context };
                return createArray(store, store.length);
            } else if (throwable instanceof ReaderException) {
                final ReaderException re = (ReaderException) throwable;
                final DynamicObject problem = stringOrNilFor(getContext(), re.getName());
                final DynamicObject context = stringOrNilFor(getContext(), re.toString());

                final Object[] store = new Object[]{ 0, 0, re.getPosition(), problem, context };
                return createArray(store, store.length);
            } else {
                return nil();
            }
        }
    }

}
