#!/bin/bash
#
# Travis ci script for releasing gradle project
# Adapted from http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

# Change this
REPO="permissions-dispatcher-plugin"

# Change if necessary
USER="shiraji"
JDK="oraclejdk7"
BRANCH="master"

# Not handling errors
set -e

if [ "$TRAVIS_REPO_SLUG" != "$USER/$REPO" ]; then
  # Check repo
  echo "TRAVIS_REPO_SLUG: '$TRAVIS_REPO_SLUG' USER/REPO: '$USER/$REPO'"
elif [ "$TRAVIS_JDK_VERSION" != "$JDK" ]; then
  # Check JDK
  echo "TRAVIS_JDK_VERSION: '$TRAVIS_JDK_VERSION' JDK: '$JDK"
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  # Check how to run this script
  echo "It's pull request!"
elif [ "$TRAVIS_BRANCH" != "$BRANCH" ]; then
  # Check branch
  echo "TRAVIS_BRANCH: '$TRAVIS_BRANCH' BRANCH: '$BRANCH'"
elif [ "$IDEA_VERSION" != "LATEST-EAP-SNAPSHOT" ]; then
  # Check IDEA version
  echo "IDEA_VERSION: '$IDEA_VERSION'"
else
  # Without snapshot
  if [ -f .travis/release ]; then
    echo "Start releasing..."
    ./gradlew publishPlugin
  else
    echo "No .travis/release file"
  fi
fi
