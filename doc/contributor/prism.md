# Prism

## Update Prism

* Clone `prism` as a sibling of `truffleruby-ws`.
* Run `tool/import-prism.sh` in the truffleruby repo.
* Commit the result with message `Import ruby/prism@COMMIT`

## Print Detailed Prism AST

```bash
cd prism
bundle exec rake
bin/parse -e '1&.itself'
```

We can also see what the AST as Java nodes and without extra location fields looks like on TruffleRuby with:
```bash
cd truffleruby
jt -q ruby -e 'puts Truffle::Debug.yarp_parse(ARGV[0])' -- '1&.itself'
```
