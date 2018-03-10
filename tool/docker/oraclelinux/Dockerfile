FROM oraclelinux-llvm

# We need a conventional locale for testing
ENV LANG=en_US.UTF-8

# To clone source repositories
RUN yum install -y git mercurial

# To bootstrap our own JVMCI-comptaible JDK we need a JDK
RUN yum install -y java-1.8.0-openjdk-devel java-1.8.0-openjdk-openjfx java-atk-wrapper

# Other dependencies for building a JDK
RUN yum install -y gcc libstdc++-static

# To build Sulong and TruffleRuby's C extensions
RUN yum install -y clang llvm libcxx libcxxabi libcxx-devel

# To build TruffleRuby
RUN yum install -y ruby rubygem-json openssl-devel

# Used in spec/truffle.mspec
RUN yum install -y which

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
ENV JAVA_HOME=/build/graal-jvmci-8/openjdk1.8.0_161/linux-amd64/product
ENV JVMCI_VERSION_CHECK=ignore
RUN $JAVA_HOME/bin/java -version

# Build the Graal compiler
RUN git clone --depth 1 https://github.com/oracle/graal.git
RUN cd graal/compiler && mx build
ENV GRAAL_HOME=/build/graal/compiler

# Build TruffleRuby
RUN git clone https://github.com/oracle/truffleruby.git
RUN cd truffleruby && mx build

# Run a basic set of tests
RUN cd truffleruby && ruby tool/jt.rb test fast :language
RUN cd truffleruby && ruby tool/jt.rb test :library_cext
RUN cd truffleruby && ruby tool/jt.rb test compiler

# Check we can make a real-world openssl request
RUN cd truffleruby && ruby tool/jt.rb run \
-ropen-uri \
-e 'open("https://rubygems.org/") { |f| raise unless f.read.include?("Find, install, and publish RubyGems") }'
