const filePath = "./myconext-server/src/test/resources/users.json";

try {
    const fileContent = fs.readFileSync(filePath, 'utf8');
    const users = JSON.parse(fileContent);
    const result = db.users.insertMany(users);

    print(`Successfully inserted ${result.insertedCount} documents into the 'users' collection.`);
} catch (e) {
    print(`Error loading or inserting data: ${e.message}`);
}
