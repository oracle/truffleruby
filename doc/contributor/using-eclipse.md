# Using the Eclipse IDE for Development

Eclipse is one of the best supported IDE for editing TruffleRuby. It provides
excellent feedback from the Truffle DSL annotation processor and actually keeps
the project built at all times by rebuilding it incrementally.

### Build the project

First, make sure the project is already built from the command line:

```bash
$ mx build
```

### Generate the project files

```bash
$ mx eclipseinit
```

### Import the projects

Create a new workspace in Eclipse (>= Luna).

We can now import the projects:
* From the main menu bar, select `File` > `Import...`
* Select `General` > `Existing Projects into Workspace`
* Select this repository as root directory
* Click `Finish`

There should be now 4 projects in your workspace:
* `truffleruby`
* `truffleruby-test`
* `TRUFFLERUBY-TEST.dist`
* `TRUFFLERUBY.dist`

### Running from the Eclipse files directly

```bash
$ jt ruby -e 'p Truffle'
Truffle
```
