#!/bin/bash
rm -Rf public/bundle*
rm -Rf target/*
source $NVM_DIR/nvm.sh
nvm use
yarn install --force && yarn build
