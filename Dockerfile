# Etapa 1: Fase de compilación
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copiar el wrapper de Maven y el archivo pom.xml para cachear dependencias
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Copiar el código fuente y compilar el JAR omitiendo los tests
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Fase de ejecución (Runtime ligero con hardening de seguridad)
FROM eclipse-temurin:25-jre
WORKDIR /app

# Crear usuario sin privilegios root
RUN groupadd -g 1000 avalon && useradd -u 1000 -g avalon -s /bin/sh avalon

# Copiar el JAR compilado desde la etapa 1 y asignar permisos
COPY --from=build /app/target/avalon-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R avalon:avalon /app

# Cambiar a usuario no root
USER 1000:1000

# Exponer el puerto configurado (8900)
EXPOSE 8900

# Variables de entorno recomendadas de la JVM para contenedores
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8900/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
