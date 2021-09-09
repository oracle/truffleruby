# Using the Eclipse IDE for Development

Eclipse is one of the best supported IDE for editing TruffleRuby. It provides
excellent feedback from the Truffle DSL annotation processor and actually keeps
the project built at all times by rebuilding it incrementally.

The downside is this incremental build might not always work and in that case
there is nothing else to do but clean all and build all, which is slow and makes
Eclipse unresponsive. The Eclipe compiler also has multiple bugs around
annotation processing which cause the incremental build to fail regularly and
need a full rebuild. For these reason, we recommend
[IntelliJ](using-intellij.md) nowadays.

## Basic Setup

### Build TruffleRuby

First, make sure the project is already built from the command line:

```bash
jt build
```

### Generate the Project Files

```bash
jt mx eclipseinit
```

### Import the Projects

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

## Advanced Setup

**Note:** This setup is not widely tested.

When building using specific env files, with `JT_ENV` or `--env`, mx will
need to be told about it explicitly. For instance:

```bash
jt mx --env jvm-ce eclipseinit
```

When working on the Graal/Truffle code itself, adding the following two lines
to the used env file, for instance `mx.truffleruby/jvm-ce` will
configure the build so that incremental compilation and even hot code
replacement works:

```bash
MX_BUILD_EXPLODED=true
LINKY_LAYOUT=*.jar
```

These settings must not be used for producing distributable artifacts, but
make development much smoother.

With these settings, after changes are made, one can simply run for instance
the following, without having to trigger a build again:

```bash
jt ruby -e 'p Truffle'
Truffle
```

