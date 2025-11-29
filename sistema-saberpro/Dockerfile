# 1. Fase de Construcción (Build Stage)
# Usa una imagen base con Maven y Java para compilar la aplicación.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el pom.xml y descarga las dependencias (para aprovechar el cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia el código fuente y compila la aplicación
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Fase de Ejecución (Runtime Stage)
# Usa una imagen base más ligera (solo Java JRE) para ejecutar la aplicación.
FROM eclipse-temurin:21-jre-alpine

# Establece el argumento para el nombre del JAR
ARG JAR_FILE=target/sistema-saberpro-1.00.jar

# Establece el puerto por defecto (Render inyectará la variable PORT)
EXPOSE 8080

# Copia el JAR compilado de la fase de construcción
COPY --from=build /app/${JAR_FILE} app.jar

# Comando de entrada para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
