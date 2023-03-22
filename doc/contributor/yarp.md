# YARP

## Update YARP

* Clone `yarp` as a sibling of `truffleruby-ws`.
* Run `tool/import-yarp.sh` in the truffleruby repo.
* Commit the result with message `Import Shopify/yarp@COMMIT`

## Print Detailed YARP AST

```bash
cd yarp
chruby 3.2.
bundle exec rake
bundle exec ruby -Ilib -ryarp -e 'pp YARP.parse("1&.itself")'
```
