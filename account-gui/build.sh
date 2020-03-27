#!/bin/bash
rm -Rf public/bundle*
rm -Rf target/*
#yarn install && yarn test && yarn build
yarn install && yarn build
