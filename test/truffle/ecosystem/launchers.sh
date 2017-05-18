#!/usr/bin/env bash

source test/truffle/common.sh.inc

function check_launchers() {
    if [ -n "$2" ]
    then
        [[ "$(${1}truffleruby --version)" =~ truffleruby\ .*\ like\ ruby\ 2.3.3 ]]
        [[ "$(${1}ruby --version)" =~ truffleruby\ .*\ like\ ruby\ 2.3.3 ]]
    fi
    [[ "$(${1}gem --version)" =~ ^2.5.2$ ]]
    [[ "$(${1}irb --version)" =~ ^irb\ 0.9.6 ]]
    [[ "$(${1}rake --version)" =~ ^rake,\ version\ [0-9.]+ ]]
    # [[ "$(${1}rdoc --version)" =~ ^4.2.1$ ]] # TODO (pitr-ch 30-Apr-2017): reports 4.3.0 on CI
    # [[ "$(${1}ri --version)" =~ ^ri\ 4.2.1$ ]] # TODO (pitr-ch 30-Apr-2017): reports 4.3.0 on CI
}

echo '** Check all launchers work'
check_launchers bin/ true

cd bin

echo '** Check all launchers work from bin dir'
check_launchers "./" true


echo '** Check all launchers work from bin dir'
check_launchers "./truffleruby -S "

echo '** Check bundled gems'

# 2.3.3 bundled gems, https://github.com/ruby/ruby/blob/v2_3_3/gems/bundled_gems
bundled_gems=(
    "power_assert 0.2.6"
    "test-unit 3.1.5"
    "minitest 5.8.5"
    "rake 10.4.2"
    "net-telnet 0.1.1"
    "did_you_mean 1.0.0"
)
gem_list=$(./gem list)

for bundled_gem in "${bundled_gems[@]}"
do
    bundled_gem="${bundled_gem//./\\.}"
    bundled_gem="${bundled_gem/ /.*}"
    [[ "${gem_list}" =~ ${bundled_gem} ]]
done
