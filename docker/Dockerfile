FROM eclipse-temurin:21-jdk-alpine
WORKDIR /
COPY target/*.jar app.jar
COPY ./target/classes/attributeAuthorities-devconf.yml /files/attributeAuthorities.yml
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
