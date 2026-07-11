# Etapa 1: Fase de compilacion
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiar el wrapper de Maven y el archivo pom.xml para cachear dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# Descargar dependencias en modo offline para aprovechar la cache de Docker
RUN ./mvnw dependency:go-offline -B

# Copiar el codigo fuente y compilar el JAR omitiendo los tests
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Fase de ejecucion (Runtime ligero)
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copiar el JAR compilado desde la etapa 1
COPY --from=build /app/target/avalon-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto configurado en application.properties (8900)
EXPOSE 8900

# Variables de entorno recomendadas de la JVM para contenedores
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
