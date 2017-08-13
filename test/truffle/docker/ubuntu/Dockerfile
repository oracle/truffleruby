FROM ubuntu:16.04

MAINTAINER chris.seaton@oracle.com

RUN apt-get update

# We need a conventional locale for testing
RUN apt-get install -y locales
RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8

# Tools we will need to get and run our tests
RUN apt-get install -y git=1:2.7.4-0ubuntu1.2

# We need a system libssl for openssl
RUN apt-get install -y libssl-dev=1.0.2g-1ubuntu4.8

# The AOT tool uses gcc to figure out struct layouts and link
RUN apt-get install -y gcc=4:5.3.1-1ubuntu1

# Create a user and working directory
WORKDIR /test
RUN useradd -ms /bin/bash test
RUN chown test /test
USER test

# Extract the GraalVM binary tarball
ENV GRAALVM_VERSION=0.26
ADD graalvm-$GRAALVM_VERSION-linux-amd64-jdk8.tar.gz /test
ENV PATH=/test/graalvm-$GRAALVM_VERSION/bin:$PATH
ENV RUBY_HOME=/test/graalvm-$GRAALVM_VERSION/jre/languages/ruby

# Build the native image
RUN native-image -H:Name=native-ruby --ruby
ENV PATH=/test:$PATH

# Straight away, just run the ruby executable as the most basic test
RUN ruby --version
RUN native-ruby -Xhome=$RUBY_HOME --version

# Clone the TruffleRuby source code to get tests
RUN git clone --branch vm-enterprise-$GRAALVM_VERSION --depth 1 https://github.com/graalvm/truffleruby.git

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

# ...and in native
RUN ruby \
spec/mspec/bin/mspec \
--config spec/truffle.mspec \
--excl-tag slow \
--excl-tag fails \
--excl-tag graalvm \
-t native-ruby \
-T -Xhome=$RUBY_HOME \
:language
