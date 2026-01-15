FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY . .

RUN javac -cp .:sqlite-jdbc.jar:slf4j-api-2.0.13.jar:slf4j-simple-2.0.13.jar \
    Main.java server/**/*.java transport/*.java concurrency/*.java http/*.java

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app /app


EXPOSE 8080

CMD ["java", "-cp", ".:sqlite-jdbc.jar:slf4j-api-2.0.13.jar:slf4j-simple-2.0.13.jar", "Main"]
