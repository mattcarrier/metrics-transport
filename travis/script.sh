#!/bin/bash
if [[ -n "$TRAVIS_TAG" ]]; then 
  dobi release
elif [[ "$TRAVIS_BRANCH" == "master" && "$TRAVIS_PULL_REQUEST" == "false" ]]; then
  dobi deploy
else
  dobi build
fi
