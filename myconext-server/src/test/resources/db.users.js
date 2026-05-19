db.users.find({
  $and: [
    { linkedAccounts: { $exists: true, $ne: [] } },
    { linkedAccounts: { $not: { $elemMatch: { preferred: { $exists: true } } } } }
  ]
})

db.users.find({
  $and: [
    { $or: [{ linkedAccounts: { $exists: true, $ne: [] } }, { externalLinkedAccounts: { $exists: true, $ne: [] } } ] },
    { linkedAccounts: { $not: { $elemMatch: { preferred: true } } } },
    { externalLinkedAccounts: { $not: { $elemMatch: { preferred: true } } } }
  ]
})

db.users.count({
  $and: [
    { $or: [{ linkedAccounts: { $exists: true, $ne: [] } }, { externalLinkedAccounts: { $exists: true, $ne: [] } } ] },
    { linkedAccounts: { $not: { $elemMatch: { preferred: true } } } },
    { externalLinkedAccounts: { $not: { $elemMatch: { preferred: true } } } }
  ]
})

const cursor = db.users.find({
  $and: [
    {
      $or: [
        { linkedAccounts: { $exists: true, $ne: [] } },
        { externalLinkedAccounts: { $exists: true, $ne: [] } }
      ]
    },
    { linkedAccounts: { $not: { $elemMatch: { preferred: true } } } },
    { externalLinkedAccounts: { $not: { $elemMatch: { preferred: true } } } }
  ]
});

cursor.forEach(user => {
  let allAccounts = [];

  if (user.linkedAccounts) {
    allAccounts = allAccounts.concat(user.linkedAccounts.map(acc => ({ ...acc, source: 'linkedAccounts' })));
  }

  if (user.externalLinkedAccounts) {
    allAccounts = allAccounts.concat(user.externalLinkedAccounts.map(acc => ({ ...acc, source: 'externalLinkedAccounts' })));
  }

  if (allAccounts.length === 0) return;

  // Find the account with the smallest createdAt
  const earliest = allAccounts.reduce((min, acc) =>
    acc.createdAt < min.createdAt ? acc : min
  );

  // Build query and update
  const updateQuery = { _id: user._id };
  const updateOp = {
    $set: {}
  };

  if (earliest.source === 'linkedAccounts') {
    const index = user.linkedAccounts.findIndex(acc => acc.createdAt.getTime() === earliest.createdAt.getTime());
    if (index >= 0) {
      updateOp.$set[`linkedAccounts.${index}.preferred`] = true;
    }
  } else if (earliest.source === 'externalLinkedAccounts') {
    const index = user.externalLinkedAccounts.findIndex(acc => acc.createdAt.getTime() === earliest.createdAt.getTime());
    if (index >= 0) {
      updateOp.$set[`externalLinkedAccounts.${index}.preferred`] = true;
    }
  }

  db.users.updateOne(updateQuery, updateOp);
});
