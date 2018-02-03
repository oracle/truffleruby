FROM ubuntu:16.04

MAINTAINER chris.seaton@oracle.com

RUN apt-get update

# We need a conventional locale for testing
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8

# Tools we will need to install rbenv
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

# Install rbenv
RUN git clone https://github.com/rbenv/rbenv.git /home/test/.rbenv
RUN mkdir /home/test/.rbenv/versions
ENV PATH=/home/test/.rbenv/bin:$PATH
RUN rbenv --version

# Install GraalVM to chruby
RUN ln -s /test/graalvm-$GRAALVM_VERSION/jre/languages/ruby /home/test/.rbenv/versions/truffleruby

# List rubies
RUN eval "$(rbenv init -)" && rbenv versions

# Select and run TruffleRuby
RUN eval "$(rbenv init -)" && rbenv global truffleruby && ruby --version
