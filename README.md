# 🚀 Reactive Challenge 2 - Spring WebFlux Application

## 📋 Descripción del Proyecto

Aplicación reactiva desarrollada con **Spring WebFlux** que implementa un sistema de cálculos con porcentajes dinámicos, gestión de usuarios autorizados, y manejo avanzado de errores con patrones de resilencia.

La aplicación cumple **100% de los requerimientos** del challenge, incluyendo todos los bonus, y está diseñada siguiendo principios **SOLID** y mejores prácticas de **programación reactiva**.

## 🧩 Arquitectura

El proyecto implementa una **arquitectura hexagonal** con las siguientes capas:

```
src/main/java/cl/tenpo/learning/reactive/tasks/task2/
├── application/          # Servicios de aplicación
├── domain/               # Modelos de dominio
├── infrastructure/       # Adaptadores (cache, client, config)
└── presentation/         # Controladores y handlers
```

### 🏗️ Patrones de Diseño Implementados

- **Factory Pattern**: Para creación de objetos Pageable, respuestas HTTP
- **Strategy Pattern**: Para configuración de retry y paginación
- **Bulkhead Pattern**: Schedulers dedicados para diferentes tipos de operaciones
- **Patrones de Resilencia**: Reintentos con backoff, fallbacks, publicación de eventos

## 🛠️ Cómo levantar la aplicación

### Prerrequisitos

- Java 21+
- Docker y Docker Compose

### Pasos para iniciar la aplicación

1. **Iniciar los servicios requeridos**:

```bash
docker-compose up -d
```

2. **Ejecutar la aplicación**:

```bash
./gradlew bootRun
```

La aplicación estará disponible en `http://localhost:8083/learning-reactive/`

## 🔍 Endpoints principales

### 🧮 Endpoint de cálculo

```
POST /learning-reactive/calculation
```

Recibe dos números, los suma y aplica un porcentaje dinámico.

**Request body**:
```json
{
    "number_1": 10,
    "number_2": 20
}
```

**Response (200 OK)**:
```json
{
    "result": 33.0,
    "operation_detail": "(10 + 20) + 10% = 33.0"
}
```

### 👥 API de usuarios

```
POST /learning-reactive/users
GET /learning-reactive/users
GET /learning-reactive/users/{id}
DELETE /learning-reactive/users/{id}
POST /learning-reactive/users/{username}/activate
```

### 📜 API de historial

```
GET /learning-reactive/history?username={username}
```

> ⚠️ Requiere un usuario autorizado para acceder.

## ✨ Características principales

### 📝 Programación reactiva pura

- Uso de **Flux** y **Mono** de Project Reactor
- **Pipelines declarativos** con operadores reactivos (filter, switchIfEmpty, onErrorResume)
- **Context propagation** para trazabilidad de solicitudes

### 💾 Persistencia reactiva

- **MongoDB reactivo** para el historial de llamadas
- **PostgreSQL con R2DBC** para usuarios autorizados
- **Redis reactivo** para caché de porcentaje

### 📊 Resiliencia y rendimiento

- **Reintentos configurables** con backoff exponencial
- **Caché distribuida** con Redis para valores de porcentaje
- **Circuit breaker** para protección ante fallos de API externa
- **Publicación de eventos** en Kafka cuando se agotan los reintentos

## 🧪 Cómo probar los requerimientos

### 1️⃣ Cálculo con porcentaje dinámico

```bash
curl -X POST -H "Content-Type: application/json" -d '{"number_1": 10, "number_2": 20}' http://localhost:8083/learning-reactive/calculation
```

### 2️⃣ Probando mecanismos de resiliencia: caché y reintentos

La aplicación incluye una funcionalidad especial para probar los mecanismos de resiliencia a través de la propiedad `force-external-api-error` en `application.yml`.

#### 🔄 Prueba de reintentos y fallback a caché

1. **Configura el comportamiento de error**:

   Modifica en `application.yml`:
   ```yaml
   app:
     testing:
       force-external-api-error: true  # Forzar error en API externa
   ```

2. **Primer intento**: La aplicación intentará obtener el porcentaje de la API externa, fallará (3 reintentos), y como no hay valor en caché, responderá con un error:

   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"number_1": 10, "number_2": 20}' http://localhost:8083/learning-reactive/calculation
   ```

   **Respuesta esperada**: Error 503 Service Unavailable

3. **Ahora cambia la configuración** para que la API externa funcione:

   ```yaml
   app:
     testing:
       force-external-api-error: false  # API externa responde correctamente
   ```

4. **Reinicia la aplicación** y haz una solicitud:

   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"number_1": 10, "number_2": 20}' http://localhost:8083/learning-reactive/calculation
   ```

   **Respuesta esperada**: Resultado exitoso (el porcentaje se almacena en caché)

5. **Vuelve a configurar** para forzar errores:

   ```yaml
   app:
     testing:
       force-external-api-error: true  # Forzar error nuevamente
   ```

6. **Reinicia y haz otra solicitud**:

   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"number_1": 10, "number_2": 20}' http://localhost:8083/learning-reactive/calculation
   ```

   **Respuesta esperada**: Resultado exitoso usando el valor en caché

#### 📝 Verificación del historial de eventos

Para verificar que se están publicando eventos en Kafka cuando se agotan los reintentos:

1. **Verifica el historial de llamadas**:

   ```bash
   # Primero crea un usuario
   curl -X POST -H "Content-Type: application/json" -d '{"username":"testuser","email":"test@example.com"}' http://localhost:8083/learning-reactive/users

   # Activa el usuario
   curl -X POST http://localhost:8083/learning-reactive/users/testuser/activate

   # Consulta el historial
   curl -X GET "http://localhost:8083/learning-reactive/history?username=testuser"
   ```

2. **Observa los logs** de la aplicación para ver los mensajes de Kafka relacionados con eventos de reintentos agotados.

### 3️⃣ Probando la API de usuarios autorizados

```bash
# Crear usuario
curl -X POST -H "Content-Type: application/json" -d '{"username":"newuser","email":"new@example.com"}' http://localhost:8083/learning-reactive/users

# Listar usuarios
curl -X GET http://localhost:8083/learning-reactive/users

# Obtener un usuario por ID
curl -X GET http://localhost:8083/learning-reactive/users/1

# Desactivar un usuario
curl -X DELETE http://localhost:8083/learning-reactive/users/1
```

### 4️⃣ Probando rutas funcionales

La aplicación también implementa rutas funcionales como bonus:

```bash
# Endpoint de ping
curl -X GET http://localhost:8083/learning-reactive/functional/ping

# Endpoint alternativo de cálculo
curl -X POST -H "Content-Type: application/json" -d '{"number_1": 10, "number_2": 20}' http://localhost:8083/learning-reactive/v2/calc
```

## 📊 Métricas y monitoreo

La aplicación expone endpoints de Actuator para monitoreo y diagnóstico en tiempo real:

### 🩺 Health Check

```bash
curl -X GET "http://localhost:8083/learning-reactive/actuator/health"
```

**Respuesta:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP", ...},
    "mongo": {"status": "UP", ...},
    "ping": {"status": "UP"},
    "r2dbc": {"status": "UP", ...},
    "redis": {"status": "UP", ...}
  }
}
```

El estado `UP` confirma que todos los componentes están funcionando correctamente.

### 📈 Métricas disponibles

```bash
curl -X GET "http://localhost:8083/learning-reactive/actuator/metrics"
```

Para consultar una métrica específica:

```bash
# Ejemplo: Uso de memoria JVM
curl -X GET "http://localhost:8083/learning-reactive/actuator/metrics/jvm.memory.used"

# Ejemplo: Solicitudes HTTP
curl -X GET "http://localhost:8083/learning-reactive/actuator/metrics/http.server.requests"

# Ejemplo: Comandos de Redis
curl -X GET "http://localhost:8083/learning-reactive/actuator/metrics/lettuce.command.completion"
```

### 🔍 Métricas útiles para los mecanismos de resiliencia

- **Pool de conexiones R2DBC**: `r2dbc.pool.acquired`, `r2dbc.pool.pending`
- **Comandos MongoDB**: `mongodb.driver.commands`
- **Tiempos de respuesta de caché**: `lettuce.command.completion`
- **Estado de hilos**: `jvm.threads.states`

> ℹ️ **Nota**: Para usar Actuator, asegúrate de incluir la dependencia `spring-boot-starter-actuator` en tu `build.gradle`.

## 🧠 Decisiones técnicas

- **Pipelines declarativos**: Eliminación de if/else por operadores reactivos
- **Schedulers dedicados**: Para operaciones de DB, API, caché y logging
- **Externalización de configuración**: Timeouts, retry, schedulers configurables
- **Manejo de errores avanzado**: Respuestas HTTP estandarizadas y context propagation
- **Logging estructurado**: Con contexto reactivo y no bloqueante

## 📚 Referencias

- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [R2DBC Documentation](https://r2dbc.io/)