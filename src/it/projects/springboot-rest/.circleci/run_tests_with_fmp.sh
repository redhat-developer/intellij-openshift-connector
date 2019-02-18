#!/usr/bin/env bash

SCRIPT_ABSOLUTE_DIR="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"
PROJECT_ABSOLUTE_DIR=$(dirname ${SCRIPT_ABSOLUTE_DIR})

pushd ${PROJECT_ABSOLUTE_DIR} > /dev/null

./mvnw clean verify -Popenshift,openshift-it "$@"

popd > /dev/null


