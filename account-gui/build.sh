#!/bin/bash
rm -Rf public/bundle*
rm -Rf target/*
source $NVM_DIR/nvm.sh
nvm use
export NODE_OPTIONS=--openssl-legacy-provider
yarn install --force && yarn test && yarn build
