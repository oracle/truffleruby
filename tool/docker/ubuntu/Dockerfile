FROM ubuntu:17.10

MAINTAINER chris.seaton@oracle.com

# We need a conventional locale for testing
ENV LANG=en_US.UTF-8

RUN apt-get update

# To clone source repositories
RUN apt-get install -y git=1:2.11.0-4 mercurial=3.9.1-1

# To bootstrap our own JVMCI-comptaible JDK we need a JDK
RUN apt-get install -y openjdk-8-jdk=8u131-b11-2ubuntu1 openjdk-8-source=8u131-b11-2ubuntu1

# Other dependencies for building a JDK
RUN apt-get install -y make=4.1-9.1 gcc=4:6.3.0-2ubuntu1 g++=4:6.3.0-2ubuntu1

# To build Sulong and TruffleRuby's C extensions
RUN apt-get install -y clang=1:4.0-36ubuntu3 llvm-dev=1:4.0-36ubuntu3 libc++-dev=3.9.1-2 libc++abi-dev=3.9.1-2

# To build TruffleRuby
RUN apt-get install -y ruby=1:2.3.3 libssl-dev=1.0.2g-1ubuntu13

# Create a user and working directory
WORKDIR /build
RUN useradd -ms /bin/bash build
RUN chown build /build
USER build

# Get the mx build tool
RUN git clone https://github.com/graalvm/mx.git
ENV PATH=$PATH:/build/mx

# Build a JDK with JVMCI
RUN hg clone http://hg.openjdk.java.net/graal/graal-jvmci-8
RUN cd graal-jvmci-8 && mx build
ENV JAVA_HOME=/build/graal-jvmci-8/jdk1.8.0_131/product

# Build the Graal compiler
RUN git clone https://github.com/graalvm/graal.git
RUN cd graal/compiler && mx build
ENV GRAAL_HOME=/build/graal/compiler

# Build Sulong
RUN git clone https://github.com/graalvm/sulong.git
RUN cd sulong && mx build
ENV SULONG_HOME=/build/sulong

# Build TruffleRuby
RUN git clone https://github.com/graalvm/truffleruby.git
RUN cd truffleruby && mx build
RUN cd truffleruby && ruby tool/jt.rb build cexts

# Run a basic set of tests
RUN cd truffleruby && ruby tool/jt.rb test fast :language
RUN cd truffleruby && ruby tool/jt.rb test --sulong :openssl
RUN cd truffleruby && ruby tool/jt.rb test compiler
