#!/bin/bash
set -e

cd `dirname $0`/..

if [ -z "$MAIN_SCALA_VERSION" ]; then
    >&2 echo "Environment MAIN_SCALA_VERSION is not set. Check .travis.yml."
    exit 1
elif [ -z "$TRAVIS_SCALA_VERSION" ]; then
    >&2 echo "Environment TRAVIS_SCALA_VERSION is not set."
    exit 1
else
    echo
    echo "TRAVIS_SCALA_VERSION=$TRAVIS_SCALA_VERSION"
    echo "MAIN_SCALA_VERSION=$MAIN_SCALA_VERSION"
fi

function buildJVM {
    INIT="clean"
    COMPILE="compile"
    TEST="test"

    COMMAND="$INIT;$COMPILE;$TEST"
    echo
    echo "Executing JVM tests: sbt \"$COMMAND\""
    echo
    sbt "$COMMAND"
}

# Execute everything
buildJVM