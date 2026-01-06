// MongoDB User Data Seeder Script
//
// Run this script with:
// mongosh "mongodb://localhost:27017/myconext_performance" seed_users.js
//
// Or with custom parameters:
// mongosh "mongodb://localhost:27017/myconext_performance" --eval "const TOTAL_USERS=5000;" seed_users.js
//
// Or redirect input:
// mongosh "mongodb://localhost:27017/myconext_performance" < seed_users.js

// Configuration (can be overridden with --eval)
const DATABASE_NAME = typeof DATABASE_NAME !== 'undefined' ? DATABASE_NAME : "surf_id_test";
const USERS_COLLECTION = typeof USERS_COLLECTION !== 'undefined' ? USERS_COLLECTION : "users";
const TOTAL_USERS = typeof TOTAL_USERS !== 'undefined' ? TOTAL_USERS : 10000;

// Switch to the database (if not already specified in connection string)
if (db.getName() !== DATABASE_NAME) {
    db = db.getSiblingDB(DATABASE_NAME);
}

// Clear existing data
db[USERS_COLLECTION].deleteMany({});
print(`✅ Cleared collection: ${USERS_COLLECTION}`);

// Helper function to generate random data
function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomChoice(array) {
    return array[randomInt(0, array.length - 1)];
}

function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// Sample data for faker-like functionality
const firstNames = [
    "Emma", "Liam", "Olivia", "Noah", "Ava", "Ethan", "Sophia", "Mason",
    "Isabella", "William", "Mia", "James", "Charlotte", "Benjamin", "Amelia",
    "Lucas", "Harper", "Henry", "Evelyn", "Alexander", "Anna", "Daan", "Sophie",
    "Lars", "Julia", "Finn", "Lisa", "Max", "Eva", "Thomas"
];

const lastNames = [
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "de Vries", "Jansen", "Bakker", "Visser", "Smit",
    "Meijer", "de Boer", "Mulder", "de Groot", "Bos", "Vos", "Peters", "Hendriks"
];

// Build user document
function buildUser() {
    const now = new Date();
    const expiresAt = new Date(now.getTime() + (6 * 365 * 24 * 60 * 60 * 1000));

    // Random birthday (18-70 years ago)
    const ageYears = randomInt(18, 70);
    const ageDays = randomInt(0, 365);
    const birthday = new Date(now);
    birthday.setFullYear(birthday.getFullYear() - ageYears);
    birthday.setDate(birthday.getDate() - ageDays);
    birthday.setHours(0, 0, 0, 0);

    const issuer = {
        _id: "studielink",
        name: "studielink"
    };

    const externalLinkedAccount = {
        subjectId: generateUUID(),
        idpScoping: "studielink",
        issuer: issuer,
        verification: "Geverifieerd",
        subjectIssuer: "studielink",
        brinCodes: ["ST42"],
        affiliations: ["student@aap.nl"],
        external: true,
        preferred: true,
        createdAt: now,
        expiresAt: expiresAt
    };

    const eduIdService = {
        name: "studielink",
        nameNl: "studielink",
        institutionGuid: "ec9d6d75-0d11-e511-80d0-005056956c1a",
        logoUrl: "https://static.surfconext.nl/logos/org/ec9d6d75-0d11-e511-80d0-005056956c1a.png",
        createdAt: now,
        lastLogin: now
    };

    const eduId = {
        value: generateUUID(),
        createdAt: now,
        services: [eduIdService]
    };

    return {
        _class: "myconext.model.User",
        uid: generateUUID(),
        email: `user-${generateUUID()}@test.com`,
        givenName: randomChoice(firstNames),
        chosenName: randomChoice(firstNames),
        familyName: randomChoice(lastNames),
        preferredLanguage: "en",
        created: Math.floor(now.getTime() / 1000),
        lastLogin: 0,
        newUser: false,
        forgottenPassword: false,
        rateLimited: false,
        serviceDeskMember: false,
        schacHomeOrganization: "eduid.nl",
        dateOfBirth: birthday,
        eduIDS: [eduId],
        externalLinkedAccounts: [externalLinkedAccount],
        linkedAccounts: [],
        publicKeyCredentials: [],
        attributes: {},
        surfSecureId: {}
    };
}

// Generate and insert users in batches
const BATCH_SIZE = 1000;
let batch = [];
let totalInserted = 0;

print(`🚀 Starting to seed ${TOTAL_USERS} users...`);

for (let i = 1; i <= TOTAL_USERS; i++) {
    batch.push(buildUser());

    if (batch.length === BATCH_SIZE || i === TOTAL_USERS) {
        db[USERS_COLLECTION].insertMany(batch, { ordered: false });
        totalInserted += batch.length;
        print(`📊 Inserted ${totalInserted} users`);
        batch = [];
    }
}

print(`✅ User seeding completed! Total users: ${totalInserted}`);
print(`📈 Collection count: ${db[USERS_COLLECTION].countDocuments({})}`);