FROM oraclelinux:7-slim

MAINTAINER chris.seaton@oracle.com

# Install LLVM from source
ENV LANG=en_US.UTF-8 WORK_DIR=/tmp/llvm-build LLVM_VERSION=4.0.1
ENV LLVM_INSTALL_PREFIX=/opt/llvm-${LLVM_VERSION} LLVM_BUILD_DIR=${WORK_DIR}/llvm-${LLVM_VERSION}
ENV PATH ${LLVM_INSTALL_PREFIX}/bin:${PATH}
RUN mkdir $WORK_DIR
WORKDIR ${WORK_DIR}
RUN yum-config-manager --enable ol7_optional_latest
RUN rpm -i https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
RUN yum update -y && yum install -y wget curl libedit-devel libxml2-devel ncurses-devel python-devel swig cmake3
RUN yum group install -y "Development Tools"
RUN yum clean all
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/llvm-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/cfe-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/clang-tools-extra-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/compiler-rt-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/libcxx-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/libcxxabi-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/libunwind-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/lld-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/lldb-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/openmp-${LLVM_VERSION}.src.tar.xz"
RUN wget "https://releases.llvm.org/${LLVM_VERSION}/polly-${LLVM_VERSION}.src.tar.xz"
RUN mkdir -p ${LLVM_BUILD_DIR} && tar -xf llvm-${LLVM_VERSION}.src.tar.xz -C llvm-${LLVM_VERSION} --strip-components=1
WORKDIR ${LLVM_BUILD_DIR}/tools
RUN mkdir clang && tar -xf ${WORK_DIR}/cfe-${LLVM_VERSION}.src.tar.xz -C clang --strip-components=1
RUN mkdir -p clang/tools/extra && tar -xf ${WORK_DIR}/clang-tools-extra-${LLVM_VERSION}.src.tar.xz -C clang/tools/extra --strip-components=1
RUN mkdir lld && tar -xf ${WORK_DIR}/lld-${LLVM_VERSION}.src.tar.xz -C lld --strip-components=1
RUN mkdir lldb && tar -xf ${WORK_DIR}/lldb-${LLVM_VERSION}.src.tar.xz -C lldb --strip-components=1
RUN mkdir polly && tar -xf ${WORK_DIR}/polly-${LLVM_VERSION}.src.tar.xz -C polly --strip-components=1
WORKDIR ${LLVM_BUILD_DIR}/projects
RUN mkdir compiler-rt && tar -xf ${WORK_DIR}/compiler-rt-${LLVM_VERSION}.src.tar.xz -C compiler-rt --strip-components=1
RUN mkdir libcxx && tar -xf ${WORK_DIR}/libcxx-${LLVM_VERSION}.src.tar.xz -C libcxx --strip-components=1
RUN mkdir libcxxabi && tar -xf ${WORK_DIR}/libcxxabi-${LLVM_VERSION}.src.tar.xz -C libcxxabi --strip-components=1
RUN mkdir libunwind && tar -xf ${WORK_DIR}/libunwind-${LLVM_VERSION}.src.tar.xz -C libunwind --strip-components=1
RUN mkdir openmp && tar -xf ${WORK_DIR}/openmp-${LLVM_VERSION}.src.tar.xz -C openmp --strip-components=1
RUN mkdir ${LLVM_BUILD_DIR}/build
WORKDIR ${LLVM_BUILD_DIR}/build
RUN cmake3 -G "Unix Makefiles" \
  -DCMAKE_BUILD_TYPE=Release \
  -DCMAKE_INSTALL_PREFIX=${LLVM_INSTALL_PREFIX} \
  -DLLVM_LIBDIR_SUFFIX=64 \
  -DLLVM_OPTIMIZED_TABLEGEN=ON \
  -DLLVM_INCLUDE_DOCS=OFF \
  -DLLVM_ENABLE_RTTI=ON \
  -DLLVM_ENABLE_EH=ON \
  -DLLVM_INSTALL_UTILS=ON \
  -DWITH_POLLY=ON \
  -DLINK_POLLY_INTO_TOOLS=ON \
  -DLLVM_TARGETS_TO_BUILD=all \
  -DLIBOMP_ARCH=x86_64 \
  -DLLVM_BUILD_EXTERNAL_COMPILER_RT=ON \
  -DLLVM_ENABLE_LIBCXX=ON \
  ${LLVM_BUILD_DIR}
RUN make -j $(( $(nproc) - 2 ))
RUN make install
RUN echo "/opt/llvm-${LLVM_VERSION}/lib64" >> /etc/ld.so.conf.d/llvm-x86_64.conf && ldconfig
WORKDIR /
RUN rm -rf ${WORK_DIR}

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

# Build Sulong
RUN git clone https://github.com/graalvm/sulong.git
RUN cd sulong && mx build

# Build TruffleRuby
RUN git clone https://github.com/oracle/truffleruby.git
RUN cd truffleruby && mx build

# Run a basic set of tests
RUN cd truffleruby && ruby tool/jt.rb test fast :language
RUN cd truffleruby && ruby tool/jt.rb test :library_cext
RUN cd truffleruby && ruby tool/jt.rb test compiler

# Check we can make a real-world openssl request, testing quite a few things end-to-end
RUN cd truffleruby && ruby tool/jt.rb run \
-ropen-uri \
-e 'open("https://rubygems.org/") { |f| raise unless f.read.include?("Find, install, and publish RubyGems") }'
