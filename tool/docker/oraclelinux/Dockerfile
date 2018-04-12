FROM oraclelinux-llvm

# We need a conventional locale for testing
ENV LANG=en_US.UTF-8

# To clone source repositories
RUN yum install -y git

# To bootstrap our own JVMCI-compatible JDK we need a JDK
RUN yum install -y java-1.8.0-openjdk-devel java-1.8.0-openjdk-openjfx java-atk-wrapper

# Other dependencies for building a JDK
RUN yum install -y gcc libstdc++-static

# Dependencies for using openssl
RUN yum install -y openssl-devel

# Dependencies for building and using C++ extensions
RUN yum install -y libcxx-devel
RUN yum install -y libcxx

# To build TruffleRuby
RUN yum install -y python ruby rubygem-json openssl-devel

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
