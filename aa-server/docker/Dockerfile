FROM eclipse-temurin:11-jdk-alpine
WORKDIR /
COPY target/*.jar app.jar
COPY ./target/classes/attributeAuthorities-devconf.yml /files/attributeAuthorities.yml
ENTRYPOINT ["java","-jar","/app.jar"]
