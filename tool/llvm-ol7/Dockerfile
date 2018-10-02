FROM oraclelinux:7-slim

ENV LLVM_VERSION=3.8.0

RUN yum-config-manager --enable ol7_optional_latest
RUN yum groupinstall -y 'Development Tools'
RUN yum install -y ruby-devel gcc make rpm-build rubygems mercurial libstdc++-static ed readline-devel zlib-devel bzip2-devel xz-devel pcre-devel libcurl-devel which
RUN gem install --no-ri --no-rdoc fpm

WORKDIR /build

RUN set -x \
  && curl -O https://cmake.org/files/v3.10/cmake-3.10.1.tar.gz \
  && tar xf cmake-3.10.1.tar.gz \
  && cd cmake-3.10.1 \
  && ./bootstrap \
  && make \
  && make install

RUN set -x \
  && curl -O http://releases.llvm.org/${LLVM_VERSION}/llvm-${LLVM_VERSION}.src.tar.xz \
  && tar xf llvm-${LLVM_VERSION}.src.tar.xz \
  && mv llvm-${LLVM_VERSION}.src llvm \
  && curl -O http://releases.llvm.org/${LLVM_VERSION}/cfe-${LLVM_VERSION}.src.tar.xz \
  && tar xf cfe-${LLVM_VERSION}.src.tar.xz \
  && mv cfe-${LLVM_VERSION}.src llvm/tools/clang \
  && curl -O http://releases.llvm.org/${LLVM_VERSION}/libcxx-${LLVM_VERSION}.src.tar.xz \
  && tar xf libcxx-${LLVM_VERSION}.src.tar.xz \
  && mv libcxx-${LLVM_VERSION}.src llvm/projects/libcxx \
  && curl -O http://releases.llvm.org/${LLVM_VERSION}/libcxxabi-${LLVM_VERSION}.src.tar.xz \
  && tar xf libcxxabi-${LLVM_VERSION}.src.tar.xz \
  && mv libcxxabi-${LLVM_VERSION}.src llvm/projects/libcxxabi \
  && mkdir -v build \
  && cd build \
  && cmake -G "Unix Makefiles" -DCMAKE_INSTALL_PREFIX=/build/llvm-${LLVM_VERSION}-ol7 \
    -DCMAKE_BUILD_TYPE=Release \
    -DLLVM_ENABLE_ASSERTIONS=ON \
    -DLLVM_OPTIMIZED_TABLEGEN=ON \
    -DLLVM_INCLUDE_DOCS=OFF \
    -DLLVM_INSTALL_UTILS=ON \
    -DLLVM_TARGETS_TO_BUILD="host" \
    -DLLVM_ENABLE_FFI=OFF \
    -DLLVM_BUILD_LLVM_DYLIB=ON \
    -DLLVM_LINK_LLVM_DYLIB=ON \
    -DLLVM_ENABLE_LIBCXX=ON \
  ../llvm \
  && make -j 2 \
  && make install

RUN tar -zcf llvm-${LLVM_VERSION}-ol7.tar.gz llvm-${LLVM_VERSION}-ol7
