#!/bin/bash
if [[ -n "$TRAVIS_TAG" || ("$TRAVIS_BRANCH" == "master" && "$TRAVIS_PULL_REQUEST" == "false") ]]; then
  dobi deploy
else
  dobi coverage
fi
