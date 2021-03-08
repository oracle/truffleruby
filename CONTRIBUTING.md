Thank you for thinking about contributing something to TruffleRuby!

## Building

See the [building instructions](doc/contributor/workflow.md) to build TruffleRuby from source.

## Contributor Agreement

You will need to sign the [Oracle Contributor Agreement](https://www.oracle.com/technical-resources/oracle-contributor-agreement.html)
(using an online form) for us to able to review and merge your work.

See [Contribute to GraalVM](https://www.graalvm.org/community/contributors/#ii-sign-the-oracle-contributor-agreement)
for more details.

We also have a [code of conduct for contributors](http://www.graalvm.org/community/conduct/).

## Contributor Documentation

And finally, we have some more technical contributor documentation, which might
be useful to someone working on TruffleRuby.

https://github.com/oracle/truffleruby/tree/master/doc/contributor

## Slack

You are welcome to join the channel `#truffleruby` of the
[GraalVM Slack](https://www.graalvm.org/community/#community-support)
for any help related to contributing to TruffleRuby.

## Style

We use various lint tools to keep the style consistent.
The most important checks can be run locally with `jt lint fast`.
You can use `tool/hooks/lint-check.sh` as a git hook to run `jt lint fast` automatically, see instructions in that file.

## ChangeLog

When opening a Pull Request, please add a ChangeLog entry with the format:

```
* Description (#PR number, @author).
```

See the [the ChangeLog](CHANGELOG.md) for examples.
