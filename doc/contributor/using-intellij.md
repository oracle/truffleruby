# Using the IntelliJ IDE for Development

IntelliJ is one of the best supported IDE for editing TruffleRuby.

### Build the project

First, make sure the project is already built from the command line:

```bash
$ jt build
```

### Generate the project files

```bash
$ jt mx intellijinit
```

### Import the project

From the IntelliJ launcher, select `Open` and navigate to this repository root.
There should be now 2 projects in your workspace:

* `truffleruby`
* `truffleruby-test`

To import other files that aren't part of Java projects, go to 'File', 'Project
Structure...', 'Modules', '+', 'New Module', 'Ruby', 'New...' next to 'Module
SDK', 'Ruby SDK', 'New local', select the `ruby` executable in the repository,
'Next', set the 'Module name' to 'truffleruby-support', set the 'Content root'
to the repository, select any 2.0 Ruby version, 'Finish'.

## Configuring Checkstyle Plugin
Checkstyle style checks for Java code can be imported into IntelliJ via the [CheckStyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea) plugin.

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
