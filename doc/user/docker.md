# Using Docker

Get the [GraalVM tarball](using-graalvm.md) (the Linux version, with Labs JDK) and put it in
the current directory along with this `Dockerfile`:

```dockerfile
FROM oracle/oraclelinux:7

WORKDIR /opt
ENV LANG C.UTF-8

# Update the 0.nn version number to the correct number
ADD graalvm-0.nn-linux-amd64-jdk8.tar.gz .

# Update the 0.nn version number to the correct number
ENV PATH /opt/graalvm-0.nn/bin:$PATH

# Do whatever you need with GraalVM here
CMD irb
```

```
$ docker build -t graalvm .
$ docker run -it graalvm
```

Make sure that you read the OTN licence agreement when you download GraalVM and keep
it in mind when using a Docker image.
