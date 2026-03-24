#!/bin/bash

set -euo pipefail

# MongoDB User Data Seeder Runner
# Usage: ./seed_users.sh

# ====== Configuration ======
MONGO_HOST="localhost"
MONGO_PORT="27017"

# Database configuration
DATABASE_NAME="surf_id_test"
USERS_COLLECTION="users"
TOTAL_USERS="${TOTAL_USERS:-1000}"
DROP_EXISTING="${DROP_EXISTING:-}"
NON_INTERACTIVE="${NON_INTERACTIVE:-false}"


# Leave empty for no authentication
MONGO_USERNAME=""
SSH_USER=""
SSH_HOST=""

# ====== Script Logic ======
if [ -n "$MONGO_USERNAME" ] && [ -n "$SSH_USER" ] && [ -n "$SSH_HOST" ]; then
    echo "🔐 Enter MongoDB password for user '$MONGO_USERNAME':"
    read -s MONGO_PASSWORD
fi

echo "⚠️  This will insert ${TOTAL_USERS} users in '${USERS_COLLECTION}'."

if [[ "${NON_INTERACTIVE}" != "true" ]]; then
    read -r -p "Drop existing users first? (y/N): " DROP_REPLY
    if [[ "${DROP_REPLY}" =~ ^[Yy]$ ]]; then
        DROP_EXISTING="true"
    else
        DROP_EXISTING="false"
    fi
else
    if [[ "${DROP_EXISTING}" =~ ^([Tt][Rr][Uu][Ee]|[Yy]|1)$ ]]; then
        DROP_EXISTING="true"
    else
        DROP_EXISTING="false"
    fi
fi

echo "ℹ️  Drop existing users: ${DROP_EXISTING}"
echo "🚀 Starting MongoDB seeder..."

# Build command based on authentication
if [ -n "$MONGO_USERNAME" ] && [ -n "$SSH_USER" ] && [ -n "$SSH_HOST" ]; then
    # With authentication
    (echo "const DATABASE_NAME='${DATABASE_NAME}'; const USERS_COLLECTION='${USERS_COLLECTION}'; const TOTAL_USERS=${TOTAL_USERS}; const CLEAR_COLLECTION=${DROP_EXISTING};" && cat seed_users.js) | \
            ssh "${SSH_USER}@${SSH_HOST}" "mongosh 'mongodb://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_HOST}:${MONGO_PORT}'"
else
    # Without authentication
    mongosh "mongodb://${MONGO_HOST}:${MONGO_PORT}" \
        --eval "const DATABASE_NAME='${DATABASE_NAME}'; const USERS_COLLECTION='${USERS_COLLECTION}'; const TOTAL_USERS=${TOTAL_USERS}; const CLEAR_COLLECTION=${DROP_EXISTING};" \
        seed_users.js
fi

# Check exit status
echo "✅ Seeding completed successfully!"
