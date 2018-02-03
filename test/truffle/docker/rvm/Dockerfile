FROM ubuntu:16.04

MAINTAINER chris.seaton@oracle.com

RUN apt-get update

# We need a conventional locale for testing
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8

# Tools we will need to install RVM
RUN apt-get install -y git

# These are the actual runtime GraalVM dependencies
RUN apt-get install -y libc++-dev libc++abi1 libssl-dev

# Create a user and working directory
WORKDIR /test
RUN useradd -ms /bin/bash test
RUN chown test /test
USER test

# Extract the GraalVM binary tarball
ARG GRAALVM_VERSION
RUN test -n "$GRAALVM_VERSION"
ADD graalvm-$GRAALVM_VERSION-linux-amd64-jdk8.tar.gz /test

# Install RVM
RUN git clone https://github.com/rvm/rvm.git
RUN bash -c "source /test/rvm/scripts/rvm && rvm --version"

# Install GraalVM to chruby
RUN bash -c "source /test/rvm/scripts/rvm && rvm mount /test/graalvm-$GRAALVM_VERSION/jre/languages/ruby -n truffleruby"

# List rubies
RUN bash -c "source /test/rvm/scripts/rvm && rvm list"

# Select and run TruffleRuby
RUN bash -c "source /test/rvm/scripts/rvm && rvm use ext-truffleruby && ruby --version"
