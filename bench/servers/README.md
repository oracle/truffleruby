# Server Benchmarks

## Problems in benchmarking servers

A big problem with benchmarking severs is the limited number of ephemeral ports
for the client side of the benchmark. This limits the number of connections that
can be made from the client in a time period. It is particularly limiting
when the time to handle the request is very small in the case of
micro-benchmarks.

The practical limitation is that you can only make around 5,000 requests
before you have to wait for ephemeral ports to time out.

It's possible to configure your system to reduce the period of time before
ports are released, but we'd rather avoid custom configuration for simplicity.

http://stackoverflow.com/questions/1216267/ab-program-freezes-after-lots-of-requests-why/1217100#1217100

To get around this, we need to run in batches of at most 5,000 requests, and
then pause for 30 seconds before running another bench.

Unfortunately this makes it hard to show a warmup curve.

Another problem is that variance for particular fast requests seem to be
particularly high. You should run for a long time to get a stable result.

## Using Apache Bench

For basic experimentation you can use
[ApacheBench](https://httpd.apache.org/docs/2.4/programs/ab.html).

For example:

```
$ ab -t 60 -n 5000 http://0.0.0.0:14873/
```

Note that you want the `-t` before the `-n`, otherwise it overwrites it.

## Using our harness

We have also written a Ruby harness, which runs `ab` with the batches described
above. You give the harness a time budget and it will run as many batches with
pauses as it can within that time period. It limits batches to a tenth of the
time you have budgeted, or 5,000 requests, whichever comes first. There is a
pause of 30s between each batch.

For each batch the number of iterations per second are reported.

```
ruby harness.rb 60 http://0.0.0.0:14873/
835.99
```
