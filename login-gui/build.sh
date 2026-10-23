#!/bin/bash
rm -Rf build
rm -Rf target/*
if [ -n "$NVM_DIR" ] && [ -f "$NVM_DIR/nvm.sh" ] && [ -f .nvmrc ]; then
  source $NVM_DIR/nvm.sh
  nvm use
fi
yarn install --frozen-lockfile && yarn check || exit 1

# The SPA index.html is rendered through a short-lived local preview server. A request to it occasionally
# hangs and times out, which a second attempt does not suffer from.
for attempt in 1 2 3; do
  yarn build && exit 0
  echo "yarn build failed (attempt $attempt of 3)"
  rm -Rf build
done
exit 1