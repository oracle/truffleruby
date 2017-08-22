FROM oraclelinux:7.3

MAINTAINER chris.seaton@oracle.com

# We need a conventional locale for testing
ENV LANG=en_US.UTF-8

# Tools we will need to get and run our tests
RUN yum install -y git-1.8.3.1 which-2.20

# We need a system libssl for openssl
RUN yum install -y openssl-devel-1.0.1e

# The AOT tool uses gcc to figure out struct layouts and link
RUN yum install -y gcc-4.8.5

# Create a user and working directory
WORKDIR /test
RUN useradd -ms /bin/bash test
RUN chown test /test
USER test

# Extract the GraalVM binary tarball
ENV GRAALVM_VERSION=0.27
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

# Run language specs with compilation disabled as another basic test
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
