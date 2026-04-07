const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");

const DEFAULT_SQL_PATH = "C:/Users/musta/OneDrive/Masaüstü/kyk_yurtlar.sql";
const BATCH_LIMIT = 450;

function unescapeSqlString(value) {
  return value.replace(/''/g, "'");
}

function parseLocations(sql) {
  const sqlString = "'((?:''|[^'])*)'";
  const cityRegex = new RegExp(
    `INSERT\\s+INTO\\s+cities\\s*\\(name,\\s*slug\\)\\s*VALUES\\s*\\(\\s*${sqlString}\\s*,\\s*${sqlString}\\s*\\)`,
    "gi"
  );
  const dormitoryRegex = new RegExp(
    `INSERT\\s+INTO\\s+dormitories\\s*\\(city_id,\\s*name,\\s*slug\\)\\s*SELECT\\s*\\(\\s*SELECT\\s+id\\s+FROM\\s+cities\\s+WHERE\\s+slug\\s*=\\s*${sqlString}\\s*\\)\\s*,\\s*${sqlString}\\s*,\\s*${sqlString}`,
    "gi"
  );

  const cities = [];
  const cityBySlug = new Map();
  let match;
  let order = 1;

  while ((match = cityRegex.exec(sql)) !== null) {
    const city = {
      name: unescapeSqlString(match[1]),
      slug: unescapeSqlString(match[2]),
      order
    };

    if (!cityBySlug.has(city.slug)) {
      cities.push(city);
      cityBySlug.set(city.slug, city);
      order += 1;
    }
  }

  const dormitories = [];
  const dormitoryIds = new Set();

  while ((match = dormitoryRegex.exec(sql)) !== null) {
    const citySlug = unescapeSqlString(match[1]);
    const city = cityBySlug.get(citySlug);
    if (!city) {
      throw new Error(`Dormitory references unknown city slug: ${citySlug}`);
    }

    const dormitory = {
      citySlug,
      cityName: city.name,
      name: unescapeSqlString(match[2]),
      slug: unescapeSqlString(match[3])
    };
    const id = `${citySlug}_${dormitory.slug}`;

    if (!dormitoryIds.has(id)) {
      dormitories.push({ id, ...dormitory });
      dormitoryIds.add(id);
    }
  }

  return { cities, dormitories };
}

async function commitInBatches(writes) {
  let committed = 0;

  for (let i = 0; i < writes.length; i += BATCH_LIMIT) {
    const batch = admin.firestore().batch();
    const chunk = writes.slice(i, i + BATCH_LIMIT);

    for (const write of chunk) {
      batch.set(write.ref, write.data, { merge: true });
    }

    await batch.commit();
    committed += chunk.length;
    console.log(`Committed ${committed}/${writes.length}`);
  }
}

async function main() {
  const args = process.argv.slice(2);
  const apply = args.includes("--apply");
  const sqlPathArg = args.find((arg) => !arg.startsWith("--"));
  const sqlPath = path.resolve(sqlPathArg || DEFAULT_SQL_PATH);

  if (!fs.existsSync(sqlPath)) {
    throw new Error(`SQL file not found: ${sqlPath}`);
  }

  const sql = fs.readFileSync(sqlPath, "utf8");
  const { cities, dormitories } = parseLocations(sql);

  console.log(`Parsed ${cities.length} cities and ${dormitories.length} dormitories from ${sqlPath}`);
  console.log("Sample city:", cities[0]);
  console.log("Sample dormitory:", dormitories[0]);

  if (!apply) {
    console.log("Dry run only. Re-run with --apply to write to Firestore.");
    return;
  }

  admin.initializeApp({
    projectId: process.env.GCLOUD_PROJECT || process.env.GOOGLE_CLOUD_PROJECT || "yurtdolap-25d16"
  });

  const db = admin.firestore();
  const writes = [
    ...cities.map((city) => ({
      ref: db.collection("cities").doc(city.slug),
      data: city
    })),
    ...dormitories.map(({ id, ...dormitory }) => ({
      ref: db.collection("dormitories").doc(id),
      data: dormitory
    }))
  ];

  await commitInBatches(writes);
  console.log("Location import completed.");
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
