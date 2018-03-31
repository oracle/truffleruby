FROM ubuntu:16.04

MAINTAINER chris.seaton@oracle.com

RUN apt-get update

# We need a conventional locale for testing
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8

# To clone source repositories
RUN apt-get install -y git

# To bootstrap our own JVMCI-comptaible JDK we need a JDK
RUN apt-get install -y openjdk-8-jdk openjdk-8-source

# Other dependencies for building a JDK
RUN apt-get install -y make gcc g++

# To build TruffleRuby
RUN apt-get install -y python ruby

# Dependencies for using openssl
RUN apt-get install -y libssl-dev

# Dependencies for building C
RUN apt-get install -y make clang llvm

# Dependencies for building and using C++ extensions
RUN apt-get install -y libc++-dev

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

# Build TruffleRuby
RUN git clone https://github.com/oracle/truffleruby.git
RUN cd truffleruby && mx build

# Build the Graal compiler
RUN cd graal/compiler && mx build
ENV GRAAL_HOME=/build/graal/compiler

# Run a basic set of tests
RUN cd truffleruby && ruby tool/jt.rb test fast :language
RUN cd truffleruby && ruby tool/jt.rb test :library_cext
RUN cd truffleruby && ruby tool/jt.rb test compiler

# Check we can make a real-world openssl request
RUN cd truffleruby && ruby tool/jt.rb run \
-ropen-uri \
-e 'open("https://rubygems.org/") { |f| raise unless f.read.include?("Find, install, and publish RubyGems") }'
