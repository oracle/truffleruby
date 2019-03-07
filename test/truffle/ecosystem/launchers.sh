#!/usr/bin/env bash

# get the absolute path of the executable and resolve symlinks
SELF_PATH=$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")
while [ -h "$SELF_PATH" ]; do
    # 1) cd to directory of the symlink
    # 2) cd to the directory of where the symlink points
    # 3) get the pwd
    # 4) append the basename
    DIR=$(dirname "$SELF_PATH")
    SYM=$(readlink "$SELF_PATH")
    SELF_PATH=$(cd "$DIR" && cd "$(dirname "$SYM")" && pwd)/$(basename "$SYM")
done

source "$(dirname $SELF_PATH)/../common.sh.inc"

function check_launchers() {
    if [ -n "$2" ]
    then
        [[ "$(${1}truffleruby --version)" =~ truffleruby\ .*\ like\ ruby\ 2.6.1 ]]
        [[ "$(${1}ruby --version)" =~ truffleruby\ .*\ like\ ruby\ 2.6.1 ]]
    fi
    [[ "$(${1}gem --version)" =~ ^3.0.1$ ]]
    [[ "$(${1}irb --version)" =~ ^irb\ 1.0.0 ]]
    [[ "$(${1}rake --version)" =~ ^rake,\ version\ 12.3.2 ]]
    [[ "$(${1}rdoc --version)" =~ ^6.1.0$ ]]
    [[ "$(${1}ri --version)" =~ ^ri\ 6.1.0$ ]]
}

function check_in_dir() {
    cd $1
    pwd
    echo "** Check all launchers work in $1 dir"
    check_launchers "./" true
    echo "** Check all launchers work in $1 dir using -S option"
    check_launchers "./truffleruby -S "
    cd -
}


# Use the Ruby home of the `jt ruby` launcher
ruby_home="$(jt ruby -e 'puts Truffle::Boot.ruby_home')"
cd "$ruby_home"


echo '** Check all launchers work'
check_launchers bin/ true
check_in_dir bin

if [[ "$(bin/ruby -e "p Truffle::System.get_java_property('org.graalvm.home').nil?")" =~ false ]]
then
    check_in_dir ../../bin      # graalvm/jre/bin
    check_in_dir ../../../bin   # graalvm/bin
fi


echo '** Check gem executables are installed in all bin dirs'

cd "$(dirname $SELF_PATH)/hello-world"
"$ruby_home/bin/gem" build hello-world.gemspec
"$ruby_home/bin/gem" install hello-world-0.0.1.gem
cd -

version="$(bin/ruby -v)"
test "$(bin/hello-world.rb)" = "Hello world! from $version"
if [[ "$(bin/ruby -e "p Truffle::System.get_java_property('org.graalvm.home').nil?")" =~ false ]]
then
    test "$(../../bin/hello-world.rb)" = "Hello world! from $version"
    test "$(../../../bin/hello-world.rb)" = "Hello world! from $version"
fi

bin/gem uninstall hello-world -x

test ! -f "bin/hello-world.rb"
if [[ "$(bin/ruby -e "p Truffle::System.get_java_property('org.graalvm.home').nil?")" =~ false ]]
then
    test ! -f "../../bin/hello-world.rb"
    test ! -f "../../../bin/hello-world.rb"
fi


echo '** Check bundled gems'

# see doc/contributor/stdlib.md
bundled_gems=(
    "did_you_mean 1.3.0"
    "minitest 5.11.3"
    "net-telnet 0.2.0"
    "power_assert 1.1.3"
    "rake 12.3.2"
    "test-unit 3.2.9"
    "xmlrpc 0.3.0"
)
gem_list=$(bin/gem list)

for bundled_gem in "${bundled_gems[@]}"
do
    bundled_gem="${bundled_gem//./\\.}"
    bundled_gem="${bundled_gem/ /.*}"
    [[ "${gem_list}" =~ ${bundled_gem} ]]
done
