#!/bin/bash
rm -Rf target/*
rm -Rf dist/*
source $NVM_DIR/nvm.sh
nvm use
export CI=true
yarn install --force && yarn test && yarn lint && yarn build
