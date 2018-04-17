FROM ubuntu:16.04

MAINTAINER chris.seaton@oracle.com

RUN apt-get update

# We need a conventional locale
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8

# Tools we will need to install RVM
RUN apt-get install -y git

# Dependencies for using openssl
RUN apt-get install -y libssl-dev

# Create a user and working directory
WORKDIR /test
RUN useradd -ms /bin/bash test
RUN chown test /test
USER test

# Extract the GraalVM binary tarball
ARG GRAALVM_TARBALL
ARG GRAALVM_VERSION
ARG TRUFFLERUBY_JAR
ARG REBUILD
RUN test -n "$GRAALVM_TARBALL"
RUN test -n "$GRAALVM_VERSION"
RUN test -n "$TRUFFLERUBY_JAR"
ARG TEST_BRANCH=vm-enterprise-$GRAALVM_VERSION
COPY $GRAALVM_TARBALL $TRUFFLERUBY_JAR /test/
RUN tar -C /test -zxf /test/$GRAALVM_TARBALL
ENV PATH=/test/graalvm-$GRAALVM_VERSION/bin:$PATH
RUN gu install /test/$TRUFFLERUBY_JAR
RUN if [ -n "$REBUILD" ]; then /test/graalvm-$GRAALVM_VERSION/jre/lib/svm/bin/rebuild-images ruby; fi
ENV RUBY_HOME=/test/graalvm-$GRAALVM_VERSION/jre/languages/ruby

# Install RVM
RUN git clone https://github.com/rvm/rvm.git
RUN bash -c "source /test/rvm/scripts/rvm && rvm --version"

# Install GraalVM to chruby
RUN bash -c "source /test/rvm/scripts/rvm && rvm mount /test/graalvm-$GRAALVM_VERSION/jre/languages/ruby -n truffleruby"

# List rubies
RUN bash -c "source /test/rvm/scripts/rvm && rvm list"

# Select and run TruffleRuby
RUN bash -c "source /test/rvm/scripts/rvm && rvm use ext-truffleruby && ruby --version"

# Check we can install a gem
RUN mkdir /test/gems
RUN bash -c "source /test/rvm/scripts/rvm && rvm use ext-truffleruby && GEM_HOME=/test/gems gem install color && ruby -v -rcolor"
