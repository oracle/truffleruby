FROM ubuntu:16.04

MAINTAINER chris.seaton@oracle.com

RUN apt-get update

# We need a conventional locale for testing
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8

# Dependencies for the rebuild-images command
RUN apt-get install -y gcc

# Dependencies for using openssl
RUN apt-get install -y libssl-dev

# Dependencies for building C
RUN apt-get install -y make clang llvm

# Dependencies for building and using C++ extensions
RUN apt-get install -y libc++-dev

# Tools we will need to get and run our tests
RUN apt-get install -y git

# Create a user and working directory
WORKDIR /test
RUN useradd -ms /bin/bash test
RUN chown test /test
USER test

# Install GraalVM and TruffleRuby
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

# Straight away, just run the ruby executables as the most basic test
RUN ruby --version
RUN ruby --native --version
RUN ruby --jvm --version

# Clone the TruffleRuby source code to get tests
RUN git clone --branch $TEST_BRANCH --depth 1 https://github.com/oracle/truffleruby.git

# We just want specs and tests - we don't want to accidentally use anything else so we're actually going to delete the rest
RUN cp -r truffleruby/spec .
RUN cp -r truffleruby/test .
RUN rm -rf truffleruby

# Run language specs as another basic test

RUN ruby \
spec/mspec/bin/mspec \
--config spec/truffle.mspec \
--excl-tag slow \
--excl-tag fails \
--excl-tag graalvm \
-t ruby \
:language

RUN ruby \
spec/mspec/bin/mspec \
--config spec/truffle.mspec \
--excl-tag slow \
--excl-tag fails \
--excl-tag graalvm \
-t ruby \
-T--jvm \
:language

# Check we can make a real-world openssl request

RUN ruby \
-ropen-uri \
-e 'open("https://rubygems.org/") { |f| raise unless f.read.include?("Find, install, and publish RubyGems") }'

RUN ruby \
--jvm \
-ropen-uri \
-e 'open("https://rubygems.org/") { |f| raise unless f.read.include?("Find, install, and publish RubyGems") }'

# Setup to install gems
ENV GEM_HOME=/test/gems
RUN mkdir /test/gems

# Check we can install and run a gem
RUN gem install color
RUN ruby -rcolor -e 'raise unless defined?(Color)'

# Check we can install and run a C extension gem
RUN gem install oily_png
RUN ruby -roily_png -e 'raise unless defined?(OilyPNG::Color)'

# Check we can install and run a C++ extension gem
RUN gem install unf
RUN ruby -runf -e 'raise unless defined?(UNF)'
