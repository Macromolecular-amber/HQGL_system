# 第一阶段：构建阶段
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# 先复制 pom.xml 以利用 Docker 缓存
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制源代码并打包（跳过测试）
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 从构建阶段复制生成的 jar 包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
