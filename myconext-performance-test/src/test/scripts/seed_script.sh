#!/bin/bash

# MongoDB User Data Seeder Runner
# Usage: ./seed_users.sh

# ====== Configuration ======
MONGO_HOST="localhost"
MONGO_PORT="27017"

# Leave empty for no authentication
MONGO_USERNAME=""
MONGO_AUTH_DB=""

# Database configuration
DATABASE_NAME="myconext_performance"
USERS_COLLECTION="users"
TOTAL_USERS=10000

# ====== Script Logic ======

echo "📋 Configuration:"
echo "   Host: ${MONGO_HOST}:${MONGO_PORT}"
echo "   Database: ${DATABASE_NAME}"
echo "   Collection: ${USERS_COLLECTION}"
echo "   Total Users: ${TOTAL_USERS}"

if [ -n "$MONGO_USERNAME" ]; then
    echo "   Username: ${MONGO_USERNAME}"
    echo "   Auth DB: ${MONGO_AUTH_DB}"
    echo ""
    echo "🔐 Enter MongoDB password for user '$MONGO_USERNAME':"
    read -s MONGO_PASSWORD
    echo ""
fi

echo ""
echo "⚠️  This will DELETE all existing data in '${USERS_COLLECTION}' collection!"
echo ""
read -p "Continue? (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Cancelled."
    exit 1
fi

echo ""
echo "🚀 Starting MongoDB seeder..."
echo ""

# Build command based on authentication
if [ -n "$MONGO_USERNAME" ]; then
    # With authentication
    MONGO_URI="mongodb://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_HOST}:${MONGO_PORT}/${DATABASE_NAME}"

    mongosh "${MONGO_URI}" \
        --username "${MONGO_USERNAME}" \
        --authenticationDatabase "${MONGO_AUTH_DB}" \
        --eval "const DATABASE_NAME='${DATABASE_NAME}'; const USERS_COLLECTION='${USERS_COLLECTION}'; const TOTAL_USERS=${TOTAL_USERS};" \
        seed_users.js
else
    # Without authentication
    MONGO_URI="mongodb://${MONGO_HOST}:${MONGO_PORT}"

    mongosh "${MONGO_URI}" \
        --eval "const DATABASE_NAME='${DATABASE_NAME}'; const USERS_COLLECTION='${USERS_COLLECTION}'; const TOTAL_USERS=${TOTAL_USERS};" \
        seed_users.js
fi

# Check exit status
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Seeding completed successfully!"
else
    echo ""
    echo "❌ Seeding failed. Check the error messages above."
    exit 1
fi