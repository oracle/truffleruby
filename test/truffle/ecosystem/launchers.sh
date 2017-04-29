#!/usr/bin/env bash

set -e
set -x

function check_launchers() {
    if [ -n "$2" ]
    then
        echo "$(${1}truffleruby --version)" | grep -e 'truffleruby .* like ruby 2.3.3'
        echo "$(${1}ruby --version)" | grep -e 'truffleruby .* like ruby 2.3.3'
    fi
    echo "$(${1}gem --version)" | grep -e '^2.5.2$'
    echo "$(${1}irb --version)" | grep -e '^irb 0.9.6'
    echo "$(${1}rake --version)" | grep -e '^rake, version [0-9.]\+'
    echo "$(${1}rdoc --version)" | grep -e '^4.2.1$'
    echo "$(${1}ri --version)" | grep -e '^ri 4.2.1$'
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
    grep -e "${bundled_gem}" <<< "${gem_list}"
done

