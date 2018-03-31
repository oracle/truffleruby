FROM fedora:26

MAINTAINER chris.seaton@oracle.com

# We need a conventional locale for testing
ENV LANG=en_US.UTF-8

# The native image tool uses gcc and zlib.h to figure out struct layouts and link
RUN dnf install -y gcc zlib-devel

# Dependencies for using openssl (compat-openssl10-devel so we get the /usr/lib64/libssl.so symlink)
RUN dnf install -y compat-openssl10-devel

# Dependencies for building C extensions
RUN dnf install -y clang llvm

# Dependencies for building and using C++ extensions
RUN dnf install -y libcxx-devel
RUN dnf install -y libcxx

# Tools we will need to get and run our tests
RUN dnf install -y git

# Used in spec/truffle.mspec
RUN dnf install -y which

# Create a user and working directory
WORKDIR /test
RUN useradd -ms /bin/bash test
RUN chown test /test
USER test

# Extract the GraalVM binary tarball
ARG GRAALVM_VERSION
ARG TEST_BRANCH=vm-enterprise-$GRAALVM_VERSION
RUN test -n "$GRAALVM_VERSION"
ADD graalvm-$GRAALVM_VERSION-linux-amd64-jdk8.tar.gz /test
ENV PATH=/test/graalvm-$GRAALVM_VERSION/bin:$PATH
ENV RUBY_HOME=/test/graalvm-$GRAALVM_VERSION/jre/languages/ruby

# Build our own native image
RUN native-image --no-server -H:Name=native-ruby --Language:ruby
ENV PATH=/test:$PATH

# Straight away, just run the ruby executables as the most basic test
RUN ruby --version
RUN ruby --native --version
RUN native-ruby -Xhome=$RUBY_HOME --version

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

# ...and in native mode
RUN ruby \
spec/mspec/bin/mspec \
--config spec/truffle.mspec \
--excl-tag slow \
--excl-tag fails \
--excl-tag graalvm \
-t ruby \
-T --native \
:language

# ...and in the native image we just built
RUN ruby \
spec/mspec/bin/mspec \
--config spec/truffle.mspec \
--excl-tag slow \
--excl-tag fails \
--excl-tag graalvm \
-t native-ruby \
-T -Xhome=$RUBY_HOME \
:language

# Check we can make a real-world openssl request
RUN ruby \
-ropen-uri \
-e 'open("https://rubygems.org/") { |f| raise unless f.read.include?("Find, install, and publish RubyGems") }'

# ...and in native mode
RUN ruby \
--native \
-ropen-uri \
-e 'open("https://rubygems.org/") { |f| raise unless f.read.include?("Find, install, and publish RubyGems") }'

# Setup to install gems
ENV GEM_HOME=/test/gems
ENV TRUFFLERUBY_CEXT_ENABLED=true
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
