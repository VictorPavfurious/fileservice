FROM gradle:8.5
WORKDIR /opt/fileservice
COPY ./build.gradle .
COPY ./src ./src
RUN gradle clean build -x test

FROM openjdk:17-jdk-slim
COPY --from=0 /opt/fileservice/build/libs/*.jar /opt/fileservice/*.jar
ENTRYPOINT ["java","-jar","/opt/fileservice/*.jar"]