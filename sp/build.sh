#!/bin/bash
rm -Rf public/bundle*
rm -Rf target/*
#yarn install && CI=true yarn test && yarn build
yarn install && yarn build
