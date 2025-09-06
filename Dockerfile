# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw -B package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Xms8g","-Xmx8g","-Xss512k","-XX:+UseG1GC","-XX:MaxGCPauseMillis=200","-XX:ParallelGCThreads=4","-XX:ConcGCThreads=2","-XX:InitiatingHeapOccupancyPercent=25","-XX:G1HeapRegionSize=8m","-XX:+AlwaysPreTouch","-XX:+ParallelRefProcEnabled","-XX:+ExplicitGCInvokesConcurrent","-XX:+UseStringDeduplication","-XX:+HeapDumpOnOutOfMemoryError","-XX:HeapDumpPath=/heapdumps","-XX:+ExitOnOutOfMemoryError","-Djava.security.egd=file:/dev/./urandom","-Djdk.attach.allowAttachSelf=true","-jar","/app/app.jar"]
