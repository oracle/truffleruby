FROM ubuntu:16.04

MAINTAINER chris.seaton@oracle.com

RUN apt-get update

# We need a conventional locale for testing
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8

# To clone source repositories
RUN apt-get install -y git mercurial

# To bootstrap our own JVMCI-comptaible JDK we need a JDK
RUN apt-get install -y openjdk-8-jdk openjdk-8-source

# Other dependencies for building a JDK
RUN apt-get install -y make gcc g++

# To build Sulong and TruffleRuby's C extensions
RUN apt-get install -y clang llvm libc++-dev libc++abi-dev

# To build TruffleRuby
RUN apt-get install -y ruby libssl-dev

# Create a user and working directory
WORKDIR /build
RUN useradd -ms /bin/bash build
RUN chown build /build
USER build

# Get the mx build tool
RUN git clone https://github.com/graalvm/mx.git
ENV PATH=$PATH:/build/mx

# Build a JDK with JVMCI
RUN git clone https://github.com/graalvm/graal-jvmci-8.git
RUN cd graal-jvmci-8 && mx build
ENV JAVA_HOME=/build/graal-jvmci-8/openjdk1.8.0_151/linux-amd64/product
ENV JVMCI_VERSION_CHECK=ignore
RUN $JAVA_HOME/bin/java -version

# Build the Graal compiler
RUN git clone https://github.com/oracle/graal.git
RUN cd graal/compiler && mx build
ENV GRAAL_HOME=/build/graal/compiler

# Build Sulong
RUN git clone https://github.com/graalvm/sulong.git
RUN cd sulong && mx build

# Build TruffleRuby
RUN git clone https://github.com/oracle/truffleruby.git
RUN cd truffleruby && mx build

# Run a basic set of tests
RUN cd truffleruby && ruby tool/jt.rb test fast :language
RUN cd truffleruby && ruby tool/jt.rb test :library_cext
RUN cd truffleruby && ruby tool/jt.rb test compiler

# Check we can make a real-world openssl request, testing quite a few things end-to-end
RUN cd truffleruby && ruby tool/jt.rb run \
-ropen-uri \
-e 'open("https://rubygems.org/") { |f| raise unless f.read.include?("Find, install, and publish RubyGems") }'
