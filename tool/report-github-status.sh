#!/usr/bin/env bash

set -e
set -x

report=$1
commit=$2
build_name=$3
build_number=$4
github_status_token=$5
status=$6

if [ "${report}" = "true" ]; then
    case "${status}" in
        pending)    description="${build_number} is being executed." ;;
        success)    description="${build_number} passed." ;;
        failure)    description="${build_number} failed." ;;
        *)          echo "Bad status: ${status}"; exit 1 ;;
    esac

    # Each build has to have its context, since only the last status posted for a given context is considered
    # in the GitHub UI
    # TODO add an url, "target_url": "${BUILD_URL}/builds/${BUILD_NUMBER}",
    curl -X POST "https://api.github.com/repos/oracle/truffleruby/statuses/${commit}" \
        -H "authorization: token ${github_status_token}" \
        -H "cache-control: no-cache" \
        -H "content-type: application/json" \
        -d "{\"state\": \"${status}\", \"description\": \"${description}\", \"context\": \"CI/${build_name}\"}"
else
    echo "GitHub status reporting not enabled."
fi
