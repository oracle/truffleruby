FROM ubuntu:16.04

MAINTAINER chris.seaton@oracle.com

RUN apt-get update

# We need a conventional locale for testing
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8

# To clone source repositories
RUN apt-get install -y git=1:2.7.4-0ubuntu1.1 mercurial=3.7.3-1ubuntu1

# To bootstrap our own JVMCI-comptaible JDK we need a JDK
RUN apt-get install -y openjdk-8-jdk=8u131-b11-0ubuntu1.16.04.2 openjdk-8-source=8u131-b11-0ubuntu1.16.04.2

# Other dependencies for building a JDK
RUN apt-get install -y make=4.1-6 gcc=4:5.3.1-1ubuntu1 g++=4:5.3.1-1ubuntu1

# To build Sulong and TruffleRuby's C extensions
RUN apt-get install -y clang=1:3.8-33ubuntu3.1 llvm-dev=1:3.8-33ubuntu3.1 libc++-dev=3.7.0-1 libc++abi-dev=3.7.0-1

# To build TruffleRuby
RUN apt-get install -y ruby=1:2.3.0+1 libssl-dev=1.0.2g-1ubuntu4.8

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

# Build TruffleRuby
RUN git clone https://github.com/graalvm/truffleruby.git
RUN cd truffleruby && mx build

# Run a basic set of tests
RUN cd truffleruby && ruby tool/jt.rb test fast :language
RUN cd truffleruby && ruby tool/jt.rb test :openssl
RUN cd truffleruby && ruby tool/jt.rb test compiler
