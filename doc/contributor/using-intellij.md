# Using the IntelliJ IDE for Development

IntelliJ is one of the best supported IDE for editing TruffleRuby.

### Build the project

First, make sure the project is already built from the command line:

```bash
$ mx build
```

### Generate the project files

```bash
$ mx intellijinit
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
