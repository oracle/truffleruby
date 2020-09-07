#!/bin/bash -li

if [[ "$1" == "help" ]]; then
    echo "tool/bench.sh (igv|time) <path_to_bench> [compile] {jvm-ce | jvm-ee | jvm | native}"
    echo "  * igv mode: dumps to e.g. graal_dumps/micro/dispatch/dispatch-bi-jvm-ce/"
    echo "  * time mode: appends [timestamp] mean median to ./perf.txt"
    echo "    (requires datamash to be on path)"
    echo "  * if no configuration is specified, defaults to jvm-ce"
    echo "  * run from the root of the repo"
    echo "  * you can pass a *quoted* wildcarded path, e.g. \"bench/micro/dispatch/*\""
    echo "  * no spaces in the path! (can't quote in script to support wildcarded paths)"
    echo "  * config variable:"
    echo "      - \$BENCH_SLEEP -- time to sleep between benchmark runs in time mode (default: 30)"
    echo "      - \$BENCH_TIME -- time to run the benchmarks for (default: 30)"
    echo "      - \$BENCH_TAIL -- number of trailing numbers to compute stats on (default: 15)"
    exit 0
fi

BENCH_SLEEP=${BENCH_SLEEP:-30}
BENCH_TIME=${BENCH_TIME:-30}
BENCH_TAIL=${BENCH_TAIL:-15}

bench_mode=$1; shift
bench_path=$1; shift

contains() {
    local item=$1; shift
    # shellcheck disable=SC2199,SC2076
    [[ " $@ " =~ " $item " ]]
}

bench_envs=()
contains jvm "$@" && bench_envs+=(jvm)
contains native "$@" && bench_envs+=(native)
contains jvm-ce "$@" && bench_envs+=(jvm-ce)
contains jvm-ee "$@" && bench_envs+=(jvm-ee)
# shellcheck disable=SC2199
[[ "${bench_envs[@]}" == "" ]] && bench_envs+=(jvm-ce)

if [[ "$1" == "compile" ]]; then
    echo "--- compiling ---------------------------------------------------------"
    for conf in "${bench_envs[@]}"; do
        jt build --env "$conf" --sforceimports
    done
    echo "--- / compiling -------------------------------------------------------"
fi

format_time() {
    local name=$1
    while read -r line; do
        echo "[$(date +"%Y-%m-%d %T") $name] $line"
    done
}

bench() {
    local file=$1
    local config=$2

    local bench_name=$file
    bench_name=${bench_name%.rb}
    bench_name=${bench_name#bench/}

    if [[ $bench_mode == igv ]]; then
        local dump_path=graal_dumps/$bench_name-$config
        mkdir -p "$dump_path"
        jt -u "$config" benchmark "$file" --time "$BENCH_TIME" -- --igv --vm.Dgraal.DumpPath="$dump_path"
    elif [[ $bench_mode == time ]]; then
        jt -u "$config" benchmark "$file" --time "$BENCH_TIME" | tee /dev/tty | tail -n "$BENCH_TAIL" | \
            datamash mean 1 median 1 | format_time "$bench_name-$config" >> perf.txt
        sleep "$BENCH_SLEEP"
    else
        echo "Unrecognized mode: $bench_mode"
        exit 1
    fi
}


for file in $bench_path; do
    for config in "${bench_envs[@]}"; do
        bench "$file" "$config"
    done
done
