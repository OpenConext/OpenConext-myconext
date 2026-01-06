#!/bin/bash

# MongoDB User Data Seeder Runner
# Usage: ./seed_users.sh

# ====== Configuration ======
MONGO_HOST="localhost"
MONGO_PORT="27017"

# Database configuration
DATABASE_NAME="surf_id_test"
USERS_COLLECTION="users"
TOTAL_USERS=1000


# Leave empty for no authentication
MONGO_USERNAME=""
SSH_USER=""
SSH_HOST=""

# ====== Script Logic ======
if [ -n "$MONGO_USERNAME" ] && [ -n "$SSH_USER" ] && [ -n "$SSH_HOST" ]; then
    echo "🔐 Enter MongoDB password for user '$MONGO_USERNAME':"
    read -s MONGO_PASSWORD
fi

echo "⚠️  This will DELETE all existing data in '${USERS_COLLECTION}' collection!"
read -p "Continue? (y/n): " -n 1 -r

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Cancelled."
    exit 1
fi

echo "🚀 Starting MongoDB seeder..."

# Build command based on authentication
if [ -n "$MONGO_USERNAME" ] && [ -n "$SSH_USER" ] && [ -n "$SSH_HOST" ]; then
    # With authentication
    (echo "const DATABASE_NAME='${DATABASE_NAME}'; const USERS_COLLECTION='${USERS_COLLECTION}'; const TOTAL_USERS=${TOTAL_USERS};" && cat seed_users.js) | \
            ssh "${SSH_USER}@${SSH_HOST}" "mongosh 'mongodb://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_HOST}:${MONGO_PORT}'"
else
    # Without authentication
    mongosh "mongodb://${MONGO_HOST}:${MONGO_PORT}" \
        --eval "const DATABASE_NAME='${DATABASE_NAME}'; const USERS_COLLECTION='${USERS_COLLECTION}'; const TOTAL_USERS=${TOTAL_USERS};" \
        seed_users.js
fi

# Check exit status
if [ $? -eq 0 ]; then
    echo "✅ Seeding completed successfully!"
else
    echo "❌ Seeding failed. Check the error messages above."
    exit 1
fi