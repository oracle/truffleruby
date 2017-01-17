# Using the IntelliJ IDE for Development

IntelliJ is one of the best supported IDE for editing TruffleRuby.

### Get Mx

If you do not already have mx, clone it and add it to `$PATH`:
```bash
$ git clone https://github.com/graalvm/mx.git ../mx
$ export PATH="../mx:$PATH"
```

### Build the project

First, make sure the project is already built from the command line:
```bash
$ mx update
# Consider the truffle framework as a binary dependency
$ echo MX_BINARY_SUITES=truffle >> mx.jruby/env
$ mx build
```

### Generate the project files

```bash
$ mx intellijinit
```

### Import the project

From the IntelliJ launcher, select `Open` and navigate to this repository root.

You shall be set!
There should be now 2 projects in your workspace:
* `jruby-truffle`
* `jruby-truffle-test`
