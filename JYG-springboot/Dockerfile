FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# 复制项目文件（路径相对于仓库根目录）
COPY JYG-springboot/pom.xml .
RUN mvn dependency:go-offline

COPY JYG-springboot/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
