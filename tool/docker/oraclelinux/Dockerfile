FROM oraclelinux:7.3

MAINTAINER chris.seaton@oracle.com

# We need a conventional locale for testing
ENV LANG=en_US.UTF-8

# To clone source repositories
RUN yum install -y git-1.8.3.1 mercurial-2.6.2

# To bootstrap our own JVMCI-comptaible JDK we need a JDK
RUN yum install -y java-1.8.0-openjdk-devel-1.8.0.131

# Other dependencies for building a JDK
RUN yum install -y make-3.82 gcc-4.8.5 gcc-c++-4.8.5

# We need also need libstdc++-static, which isn't available by default
RUN yum-config-manager --add-repo http://public-yum.oracle.com/repo/OracleLinux/OL7/optional/latest/x86_64/
RUN yum install -y libstdc++-static-4.8.5

# To build Sulong and TruffleRuby's C extensions
RUN yum install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
RUN yum install -y clang-3.4.2

# To build TruffleRuby
RUN yum install -y ruby-2.0.0.648 openssl-devel-1.0.1e which-2.20

# Create a user and working directory
WORKDIR /build
RUN useradd -ms /bin/bash build
RUN chown build /build
USER build

# Get the mx build tool
RUN git clone https://github.com/graalvm/mx.git
ENV PATH=$PATH:/build/mx

# Build a JDK with JVMCI
RUN hg clone http://hg.openjdk.java.net/graal/graal-jvmci-8
RUN cd graal-jvmci-8 && mx build
ENV JAVA_HOME=/build/graal-jvmci-8/jdk1.8.0_131/product

# Build the Graal compiler
RUN git clone https://github.com/graalvm/graal.git
RUN cd graal/compiler && mx build
ENV GRAAL_HOME=/build/graal/compiler

# Build Sulong
RUN git clone https://github.com/graalvm/sulong.git
RUN cd sulong && mx build
ENV SULONG_HOME=/build/sulong

# Build TruffleRuby
RUN git clone https://github.com/graalvm/truffleruby.git
RUN cd truffleruby && git checkout system-libssl
RUN cd truffleruby && mx build
RUN cd truffleruby && ruby tool/jt.rb build cexts

# Run a basic set of tests
RUN cd truffleruby && ruby tool/jt.rb test fast :language
RUN cd truffleruby && ruby tool/jt.rb test --sulong :openssl
RUN cd truffleruby && ruby tool/jt.rb test compiler
