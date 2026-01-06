# MyConext Performance Testing

This project is used to test performance scenarios for **MyConext**.

It includes:
- **Gatling performance tests** for load and flow validation
- A **data seeder** to insert fake data into the `users` collection

## Current flow

At the moment, the project supports a single test flow:

1. **Check if an email exists**
2. **Create a new EduID** with status **Ongeverifieerd**
3. **Update the EduID** using data from step 2:
    - Change the verification status

## System Requirements
- Java 21
- Maven 3

## Run UserDataSeeder
```shell
mvn clean && mvn test-compile exec:java \
  -Dexec.mainClass=myconext.UserDataSeeder \
  -DdbUrl=mongodb://localhost:27017 \
  -DdbName=myconext_performance \
  -DusersCollection=users
```

## How to run Gatling tests
- Run the command `mvn gatling:test -Dgatling.simulationClass=...........`
- The results can be found in the logging and there is a nice html page generated in the *target/gatling* folder
- Tip: if you want to keep past results don't run `mvn clean`

- Check the example commands below.

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

