/***** BEGIN LICENSE BLOCK *****
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
 * Copyright (C) 2007-2011 Nick Sieger <nicksieger@gmail.com>
 * Copyright (C) 2009 Joseph LaFata <joe@quibb.org>
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
 ***** END LICENSE BLOCK *****/
package org.truffleruby.shared.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandLineOptions {

    private Map<String, String> options;
    private String[] arguments;
    private final List<String> unknownArguments;
    private boolean irbInsteadOfInputUsed;

    public CommandLineOptions(Map<String, String> options) {
        this.options = options;
        this.arguments = new String[]{};
        this.unknownArguments = new ArrayList<>(0);
        this.irbInsteadOfInputUsed = false;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public <T> void setOption(OptionDescription<T> key, T value) {
        setOptionRaw(key, key.valueToString(value));
    }

    public <T> T getOption(OptionDescription<T> key) {
        return key.checkValue(getOptionRaw(key));
    }

    public <T> void appendOptionValue(AppendableOptionDescription<T> key, String newValue) {
        setOptionRaw(
                key,
                key.append(getOptionRaw(key), newValue));
    }

    private void setOptionRaw(OptionDescription<?> key, String value) {
        options.put(key.getName(), value);
    }

    private <T> String getOptionRaw(OptionDescription<T> key) {
        return options.getOrDefault(
                key.getName(),
                key.valueToString(key.getDefaultValue()));
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public List<String> getUnknownArguments() {
        return unknownArguments;
    }

    public boolean isIrbInsteadOfInputUsed() {
        return irbInsteadOfInputUsed;
    }

    public void setIrbInsteadOfInputUsed(boolean irbInsteadOfInputUsed) {
        this.irbInsteadOfInputUsed = irbInsteadOfInputUsed;
    }
}
