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
package org.truffleruby.launcher.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLineOptions {

    private Map<String, String> options = new HashMap<>();
    private String[] arguments = new String[]{};
    private final List<String> unknownArguments = new ArrayList<>(0);

    // TODO (pitr-ch 26-Jul-2017): move as much as possible to options

    private StringBuffer inlineScript = new StringBuffer();
    private boolean hasInlineScript;
    private boolean usePathScript;
    private String scriptFileName;
    private boolean showVersion;
    private boolean showCopyright;
    private boolean shouldRunInterpreter = true;
    private boolean shouldPrintUsage;
    private boolean shouldCheckSyntax;
    private String inPlaceBackupExtension;
    private boolean hasScriptToRun; // -e or a file
    private boolean forceStdin;
    private boolean shouldPrintShortUsage;

    // TODO (pitr-ch 26-Jul-2017): Move to Options when implementing -s option
    // Currently not used
    private Map<String, String> optionGlobals = new HashMap<>();

    public String getDisplayedFileName() {
        if (isInlineScript()) {
            if (getScriptFileName() != null) {
                return getScriptFileName();
            } else {
                return "-e";
            }
        } else if (shouldUsePathScript()) {
            return "-S";
        } else if (isForceStdin() || getScriptFileName() == null) {
            return "-";
        } else {
            return getScriptFileName();
        }
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public <T> void setOption(OptionDescription<T> key, T value) {
        setOptionRaw(key, key.toString(value));
    }

    public void setOptionRaw(OptionDescription<?> key, String value) {
        options.put(key.getName(), value);
    }

    public <T> T getOption(OptionDescription<T> key) {
        return key.checkValue(getOptionRaw(key));
    }

    public void appendOptionValue(StringArrayOptionDescription key, String newValue) {
        setOptionRaw(key, key.append(getOptionRaw(key), newValue));
    }

    public <T> String getOptionRaw(OptionDescription<T> key) {
        return options.getOrDefault(
                key.getName(),
                key.toString(key.<T>getDefaultValue()));
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public String inlineScript() {
        return inlineScript.toString();
    }

    public StringBuffer getInlineScript() {
        return inlineScript;
    }

    public void setHasInlineScript(boolean hasInlineScript) {
        this.hasScriptToRun = true;
        this.hasInlineScript = hasInlineScript;
    }

    public void setShouldPrintUsage(boolean shouldPrintUsage) {
        this.shouldPrintUsage = shouldPrintUsage;
    }

    public boolean getShouldPrintUsage() {
        return shouldPrintUsage;
    }

    public boolean isInlineScript() {
        return hasInlineScript;
    }

    public boolean isForceStdin() {
        return forceStdin;
    }

    public void setForceStdin(boolean forceStdin) {
        this.forceStdin = forceStdin;
    }

    public void setScriptFileName(String scriptFileName) {
        this.hasScriptToRun = true;
        this.scriptFileName = scriptFileName;
    }

    public String getScriptFileName() {
        return scriptFileName;
    }

    public void setShowVersion(boolean showVersion) {
        this.showVersion = showVersion;
    }

    public boolean isShowVersion() {
        return showVersion;
    }

    public void setShowCopyright(boolean showCopyright) {
        this.showCopyright = showCopyright;
    }

    public boolean isShowCopyright() {
        return showCopyright;
    }

    public void setShouldRunInterpreter(boolean shouldRunInterpreter) {
        this.shouldRunInterpreter = shouldRunInterpreter;
    }

    public boolean shouldRunInterpreter() {
        return shouldRunInterpreter && hasScriptToRun;
    }

    public void setShouldCheckSyntax(boolean shouldSetSyntax) {
        this.shouldCheckSyntax = shouldSetSyntax;
    }

    public boolean getShouldCheckSyntax() {
        return shouldCheckSyntax;
    }

    public void setInPlaceBackupExtension(String inPlaceBackupExtension) {
        this.inPlaceBackupExtension = inPlaceBackupExtension;
    }

    public String getInPlaceBackupExtension() {
        return inPlaceBackupExtension;
    }

    public Map<String, String> getOptionGlobals() {
        return optionGlobals;
    }

    public boolean doesHaveScriptToRun() {
        return hasScriptToRun;
    }

    public void setUsePathScript(String name) {
        usePathScript = true;
        hasScriptToRun = true;
        scriptFileName = name;
    }

    public boolean shouldUsePathScript() {
        return usePathScript;
    }

    public void setShouldPrintShortUsage(boolean shouldPrintShortUsage) {
        this.shouldPrintShortUsage = shouldPrintShortUsage;
    }

    public boolean getShouldPrintShortUsage() {
        return shouldPrintShortUsage;
    }

    public List<String> getUnknownArguments() {
        return unknownArguments;
    }
}
