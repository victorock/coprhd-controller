#!/bin/bash

for arg in "$@"; do
        [ "$arg" != "${arg#if=}" ] && infile="${arg#if=}"
        [ "$arg" != "${arg#count=}" ] && bcount="${arg#count=}"
        [ "$arg" != "${arg#bs=}" ] && bs="${arg#bs=}"
        [ "$arg" != "${arg#ibs=}" ] && bs="${arg#ibs=}"
done

echo -n " " >&2
dd "$1" "$2" &
ddpid="$!"
echo "ddpid = " $ddpid

exit $ddpid