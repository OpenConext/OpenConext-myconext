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

## How to run the preformance Test (Database Growth Benchmark)
There is a script to run the performance test. This script will insert users in the database and run the performance test. It testing the database growth.
1. It wil insert x amount of users in the database.
2. It will run the performance test and generate a report.

It will repeat step 1 and 2 with the following amount of users 1000 10000 100000 1000000 2000000 4000000.

For `DatabaseGrowthBenchmarkSimulation`, a `target/gatling/compare.html` page is generated automatically after each run.

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
