/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.shared.options;

import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionDescriptor;
import org.graalvm.options.OptionKey;
import org.graalvm.options.OptionType;

public abstract class OptionDescription<T> {

    private final String name;
    private final String description;
    private final String[] rubyOptions;

    public OptionDescription(String name, String description, String[] rubyOptions) {
        this.name = name;
        this.description = description;
        this.rubyOptions = rubyOptions;
    }

    public String getName() {
        return name;
    }

    public String getPropertyName() {
        return String.format("polyglot.%s", name);
    }

    public String getDescription() {
        return description;
    }

    public boolean hasRubyOptions() {
        return rubyOptions != null && rubyOptions.length > 0;
    }

    public String[] getRubyOptions() {
        return rubyOptions;
    }

    public abstract T getDefaultValue();

    public abstract T checkValue(Object value);

    @SuppressWarnings("unchecked")
    public T cast(Object value) {
        return (T) value;
    }

    public String valueToString(Object value) {
        if (value == null) {
            return "null";
        } else {
            return value.toString();
        }
    }

    public OptionDescriptor toDescriptor() {
        return OptionDescriptor
                .newBuilder(new OptionKey<>(getDefaultValue(), getOptionType()), getName())
                .help(getDescription())
                .category(hasRubyOptions() ? OptionCategory.USER : OptionCategory.DEBUG)
                .build();
    }

    protected abstract OptionType<T> getOptionType();
}
