# Prism

## Update Prism

* Clone `prism` as a sibling of `truffleruby-ws`.
* Run `tool/import-prism.sh` in the truffleruby repo.
* Commit the result with message `Import ruby/prism@COMMIT`

## Print Detailed Prism AST

```bash
cd prism
chruby 3.2.
bundle exec rake
bundle exec ruby -Ilib -rprism -e 'pp Prism.parse("1&.itself")'
```
