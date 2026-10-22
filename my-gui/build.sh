#!/bin/bash
rm -Rf build
rm -Rf target/*
if [ -n "$NVM_DIR" ] && [ -f "$NVM_DIR/nvm.sh" ] && [ -f .nvmrc ]; then
  source $NVM_DIR/nvm.sh
  nvm use
fi
yarn install --frozen-lockfile && yarn check && yarn build
