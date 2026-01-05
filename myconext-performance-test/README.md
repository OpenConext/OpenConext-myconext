# Run UserDataSeeder
```shell
mvn clean && mvn test-compile exec:java \
  -Dexec.mainClass=myconext.UserDataSeeder \
  -DdbUrl=mongodb://localhost:27017 \
  -DdbName=myconext_performance \
  -DusersCollection=users
```
# Run Gatling with props
```shell
mvn gatling:test \
-Dgatling.simulationClass=myconext.simulations.SingleRunSimulation \
-DbaseUrl=http://localhost:8081 \
-Dusername=studielink \
-Dpassword=secret
```
# Run Gatling SingelRun (8 minutes)
```shell
mvn gatling:test -Dgatling.simulationClass=myconext.simulations.SingleRunSimulation
```
# Run Gatling Gradual Ramp Up (8 minutes)
```shell
mvn gatling:test -Dgatling.simulationClass=myconext.simulations.GradualRampUpSimulation
```
# Run Gatling Stress Test (5 minutes)
```shell
mvn gatling:test -Dgatling.simulationClass=myconext.simulations.StressTestSimulation
```
# Run Gatling Soak Test (35 minutes)
```shell
mvn gatling:test -Dgatling.simulationClass=myconext.simulations.SoakTestSimulation
```

