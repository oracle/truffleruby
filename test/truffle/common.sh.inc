set -e
set -x

PORT=14873

# You must call `jt check_test_port` before starting the server and calling this
# function, to avoid hanging if the port is already taken.
function test_server {
  serverpid=$!

  local cmd="curl -s http://localhost:$PORT/"
  local response=""
  while ! response=$($cmd) || [[ -z response ]];
  do
    sleep 1
  done
  jobs -l
  pkill -P $serverpid
  wait $serverpid || true
  if [[ $response != *"Hello"* ]]
  then
    echo Response not expected:
    echo $response
    exit 1
  fi
}

shopt -s expand_aliases
alias jt="tool/jt.rb"
