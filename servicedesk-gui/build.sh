#!/bin/bash
rm -Rf public/bundle*
rm -Rf target/*
source $NVM_DIR/nvm.sh
nvm use
export CI=true
yarn install --force && yarn test && yarn build
