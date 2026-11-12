#!/bin/bash
rm -Rf public/bundle*
rm -Rf target/*
rm -Rf build/*
rm -Rf dist/*
source $NVM_DIR/nvm.sh
nvm use
export CI=true
yarn install --frozen-lockfile || exit 1
yarn test && yarn build
