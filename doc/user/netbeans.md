# NetBeans integration

You can debug Ruby programs running in TruffleRuby using the NetBeans IDE.

## Setup

* Install NetBeans 8.2
* Via Tools, Plugins, install 'Truffle Debugging Support'
* [Download GraalVM](using-graalvm.md)

To make GraalVM appear as a conventional JVM to NetBeans, you need to create
some new symlinks. In the `bin` directory, run:

```
$ ln -s ../jdk/bin/javac .
$ ln -s ../jdk/bin/javadoc .
```

We then need a project to debug. An example project that works well and demonstrates features is available on GitHub:

```
$ git clone https://github.com/jtulach/sieve.git
```

Open `ruby+js/fromjava` as a NetBeans project.

* Right click on the project
* Open Properties, Build, Compile, Java Platform
* Manage Java Platforms, Add Platform, and select the GraalVM directory
* Now we can set a breakpoint in Ruby by opening `sieve.rb` and clicking on the line in `Natural#next`
* Finally, 'Debug Project'

You will be able to debug the Ruby program as normal, and if you look at the
call stack you'll see that there are also Java and JavaScript frames that you
can debug as well, all inside the same virtual machine and debugger.
