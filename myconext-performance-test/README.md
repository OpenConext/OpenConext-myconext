# MyConext Performance Testing

This project is used to test performance scenarios for **MyConext**.

It includes:
- **Gatling performance tests** for load and flow validation
- A **data seeder** to insert fake data into the `users` collection

## Current flow

At the moment, the project supports a single test flow:

1. **Check if an email exists**
2. **Create a new myconext user** with status **Ongeverifieerd**
3. **Update the myconext user** using data from step 2:
    - Change the verification status

## System Requirements
- Java 21
- Maven 3
- mongoese 5.0.3

## How to Run the Performance Test (Database Growth Benchmark)

There is a script that runs the performance test. It inserts users into the database and measures performance as the database grows.

For each run it will:
1. Insert x amount of users into the database
2. Run the performance test and generate a report

This repeats for the following database sizes: 1000, 10000, 100000, 1000000, 2000000, 4000000.

After each run, a `target/gatling/compare.html` page is generated automatically for `DatabaseGrowthBenchmarkSimulation`.

## How to run the Performance Test github action

The performance test is a manually triggered GitHub Actions workflow that benchmarks backend performance across different database sizes.

### How to trigger

1. Go to the **Actions** tab in the repository
2. Select **Performance test** from the workflow list
3. Click **Run workflow**
4. Optionally adjust the inputs and click **Run workflow** to confirm

### Inputs

| Input | Default | Description |
|---|---|---|
| `benchmark_sizes` | `1000,10000,...,4000000` | Comma-separated list of database sizes to test |
| `benchmark_users` | `100` | Number of virtual users per Gatling run |
| `benchmark_ramp_seconds` | `30` | Ramp-up duration in seconds per Gatling run |

### Results

After the workflow completes, download the `database-growth-benchmark` artifact from the workflow run. The key files are:

- `myconext-performance-test/target/gatling/compare.html` — visual comparison of results across database sizes
- `myconext-performance-test/src/test/scripts/benchmark_database_growth.log` — benchmark script output
- `myconext-performance-test/target/backend.log` — backend logs

### Run the full database growth preformance test
```bash
cd ./src/test/scripts
bash benchmark_database_growth.sh
```

## How to Run MongoDB User Data Seeder
Tip: This is simple command to count users:
``` shell
mongosh "mongodb://localhost:27017" --eval "db.getSiblingDB('surf_id_test').users.countDocuments()"
```

1. Navigate to the scripts directory
```bash
cd ./src/test/scripts
```
2. Configure the connection (edit `seed_script.sh`)
**Local MongoDB (no authentication):**
```bash
MONGO_HOST="localhost"
MONGO_PORT="27017"
MONGO_USERNAME=""
MONGO_AUTH_DB=""
```
**Remote MongoDB (with authentication):**
```bash
MONGO_HOST="your-server.com"
MONGO_PORT="27017"
MONGO_USERNAME="admin"
MONGO_AUTH_DB="admin"
```

3. Run the script
```bash
bash seed_script.sh
```
Press `Enter` at the drop prompt to continue without clearing the collection first.
Use `TOTAL_USERS=10000 bash seed_script.sh` to seed a different amount.

## How to run Gatling tests
- Run the command `mvn gatling:test -Dgatling.simulationClass=...........`
- The results can be found in the logging and there is a nice html page generated in the *target/gatling* folder
- Tip: if you want to keep past results don't run `mvn clean`
- For `DatabaseGrowthBenchmarkSimulation`, a `target/gatling/compare.html` page is generated automatically after each run
- You can store the database size for that run with `-Damountusers=1000` so the compare page shows headers like `20260317135617983 (1000 users)`

### Run Gatling with props
```shell
mvn gatling:test \
-Dgatling.simulationClass=myconext.simulations.SingleRunSimulation \
-DbaseUrl=http://localhost:8081 \
-Dusername=studielink \
-Dpassword=secret
```

### Run Gatling SingelRun (8 minutes)
```shell
mvn gatling:test -Dgatling.simulationClass=myconext.simulations.SingleRunSimulation
```

### Run Database Growth Benchmark with database size label
```shell
mvn gatling:test \
-Dgatling.simulationClass=myconext.simulations.DatabaseGrowthBenchmarkSimulation \
-Damountusers=1000
```

### Run the full database growth series
```bash
cd ./src/test/scripts
bash benchmark_database_growth.sh
```

### Run Gatling Stress Test (5 minutes)
```shell
mvn gatling:test -Dgatling.simulationClass=myconext.simulations.StressTestSimulation
```

### Run Gatling Stress Test Gentle (5 minutes)
```shell 
mvn gatling:test -Dgatling.simulationClass=myconext.simulations.StressTestSimulationGentle 
```

### Run Gatling Gradual Ramp Up (8 minutes)
```shell
mvn gatling:test -Dgatling.simulationClass=myconext.simulations.GradualRampUpSimulation
```

### Run Gatling Soak Test (35 minutes)
```shell
mvn gatling:test -Dgatling.simulationClass=myconext.simulations.SoakTestSimulation
```
