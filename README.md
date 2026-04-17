# flashcards

./mvnw clean install

start postgre service
net start postgresql-x64-18
net stop postgresql-x64-18

set project to use local db configuration in application-local.properties (must run per session)
(powershell) $env:SPRING_PROFILES_ACTIVE="local"; ./mvnw spring-boot:run

./mvnw spring-boot:run

./mvnw test