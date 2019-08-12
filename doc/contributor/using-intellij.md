# Using the IntelliJ IDE for Development

IntelliJ is one of the best supported IDE for editing TruffleRuby.

### Build the project

First, make sure the project is already built from the command line:

```bash
$ jt build
```

### Install Eclipse Code Formatter plugin

Installing the Eclipse Code Formatter plugin is recommended to have the Java
source code always formatted to match the project's guidelines.

1.  Download Eclipse version 4.5.2 (same version is used in the CI)

2.  Export path of the installed Eclipse IDE

    ```bash
    # e.g. on macOS
    export ECLIPSE_EXE=/path/to/java-mars-4.5.2/Eclipse.app/Contents/MacOS/eclipse
    ``` 

3.  Install the 'Eclipse Code Formatter' plugin in IntelliJ's Preferences >
    Plugins section.

4.  The plugin will be fully configured in next step where the project files are
    generated.
    
To format the source code use the usual idea shortcut for formatting the file.
If imports are not being optimised as well, use idea action "Show Reformat File
Dialog" and make sure the checkbox "Optimize imports" is checked.

### Install Ruby and Python plugins

To be able to see and edit properly all the files in the TruffleRuby repository
install plugins for languages Ruby and Python in Preferences > Plugins section.

Make sure you have configured Ruby and Python SDKs. When the project files are
generated it will try to use existing Python 2.7 SDK and a Ruby SDK which has
'truffleruby-jvm' in its name. If `rbenv` is used the 'truffleruby-jvm' build
should be linked automatically and the 'rbenv: truffleruby-jvm' Ruby SDK
should be created automatically on the IDE start.

### Generate the project files

```bash
$ jt mx intellijinit
```

### Import the project

From the IntelliJ launcher, select `Open` and navigate to this repository root.
There should now be the following in your workspace:

*   `com`, `org` – roots of Java packages TruffleRuby depend on
*   `mx` – the mx tool
*   `truffleruby` – Root directory which contains all TruffleRuby files 
    including the Java and Ruby source files. Syntax highlighting and project 
    navigation should work for both Java and Ruby.

## Configuring Checkstyle Plugin

Checkstyle style checks for Java code can be imported into IntelliJ via the
[CheckStyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)
plugin.

### Installation
1. Select "IntelliJ" -> "Preferences", then "Plugins".
2. Select "Browse Repositories" and search for "Checkstyle"
3. Select "Checkstyle-IDEA", then "Install".

### Configuration
1. Select "IntelliJ" -> "Preferences", then "Checkstyle".
2. Select "+" to add a local Checkstyle Configuration.
3. Enter a description, e.g. `TruffleRuby Checkstyle`
4. Enter the location of the configuration file, e.g. `/Users/myuser/Documents/truffleruby/src/main/.checkstyle_checks.xml`
5. Complete the import, then check the "Active" checkbox next to the new configuration, then select "Ok".

### Scan Usage
1. Select "View" -> "Tool Windows" -> "Checkstyle"
2. The Checkstyle tools window has options to check the current file, project, module, changelist, or only modified files.
3. The style issues will be highlighted in the tool window, on the right side of the editor or inline in the source.

### Reformat Code Usage
Using the "Code" -> "Reformat Code" tool using IntelliJ's default settings may resolve the majority of Checkstyle errors. Optionally, you can import Checkstyle styles into your code formatting as follows:

1. Select "IntelliJ" -> "Preferences", "Editor", then "Code Style".
2. Using the gear icon, next to the "Scheme", select "Import Scheme" -> "Checkstyle Configuration".
3. Select the `src/main/.checkstyle_checks.xml` file to import Checkstyle into the scheme.
4. Use "Reformat Code" tool as usual with the new Checkstyle settings imported.

## Additional Recommended Format Settings
In "IntelliJ" -> "Preferences" -> "Code Style" -> "Java":
- In "Imports" set the count to use import * to a large value to disable import *, e.g. "999"
- In "Wrapping & Braces" - "Field Annotations" select the option to not wrap after a single annotation.
