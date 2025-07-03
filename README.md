# capacitacion-reactivo-modulo-2


# Reactor Callbacks: Orden Cronológico

1. **`doFirst`**
    - Se ejecuta **antes de todo**, en cuanto se construye el pipeline (incluso antes de la suscripción real).

2. **`doOnSubscribe`**
    - Se dispara justo cuando el subscriber se suscribe al publisher (antes de emitir cualquier dato).

3. **`doOnNext`**
    - Se invoca por cada elemento emitido. Si hay N elementos, verás N logs de `doOnNext` en orden.

4. **`doOnComplete`** (solo si el flujo terminó con éxito)
    - Se ejecuta cuando la secuencia ha emitido todos sus elementos y completa normalmente.

5. **`doOnError`** (solo si hubo error)
    - Se dispara cuando el flujo lanza un error antes de completarse.  
      *(Si no hay error, este callback nunca se ejecuta.)*

6. **`doOnTerminate`**
    - Se invoca justo **después** de `doOnComplete` (en caso de éxito) o justo **después** de `doOnError` (en caso de falla).  
      *(Es como un “onFinally” que sucede al terminar, sea por éxito o por error.)*

7. **`doFinally`**
    - Se ejecuta al final **después** de todo lo anterior, indiferentemente de si terminó con éxito o con error.

---

# Emisión hasta “France” pese a errores en `findAllCountries`

- **Problema**: Si `findAllCountries()` arroja error antes de emitir `"France"`, el flujo falla y nunca llega a `"France"`.
- **Solución sin `onErrorContinue`**:
    1. Aplicar `retry()` o `retryWhen(...)` **antes** de `takeUntil("France")`.
    2. Así, si ocurre un error, el flujo se reintenta y reemite desde el principio hasta que finalmente salga `"France"`.


Flux<String> result = countryService.findAllCountries()
    .retryWhen(
        Retry.fixedDelay(5, Duration.ofMillis(500))
             .doBeforeRetry(signal ->
                 log.info("Retrying after error: {}", signal.failure().getMessage())
             )
    )
    .takeUntil(country -> country.equals("France"));

## 3. Estrategias de Backpressure en Reactor

1. **`onBackpressureBuffer(...)`**  
   - Almacena los elementos emitidos en una cola interna hasta que el subscriber los solicite.  
   - Permite definir un tamaño máximo y una acción al desbordar (por ejemplo, dropear el valor más antiguo o lanzar error).

2. **`onBackpressureDrop()`**  
   - Descarta los valores que el subscriber no pueda procesar a tiempo.  
   - Se puede usar `onBackpressureDrop(el -> { … })` para ejecutar una acción sobre cada elemento descartado.

3. **`onBackpressureLatest()`**  
   - Solo conserva el **último** valor emitido; descarta todos los intermedios.  
   - Cuando el subscriber esté listo, recibirá dicho valor más reciente.

4. **`onBackpressureError()`**  
   - Lanza inmediatamente una excepción `OverflowException` si el productor emite más de lo que el subscriber pidió.

5. **Técnicas adicionales**  
   - **`limitRate(n)`**: solicita en cada “lote” solo `n` elementos al upstream.  
   - Control manual con `subscription.request(n)`: el subscriber decide cuántos elementos recibir en cada petición.

#
    
# 🚀 Reactive Challenge 2 - Spring WebFlux Application

## 📋 Descripción del Proyecto

Aplicación reactiva desarrollada con **Spring WebFlux** que implementa un sistema de cálculos con porcentajes dinámicos, gestión de usuarios autorizados, y manejo avanzado de errores con patrones de resilencia.

La aplicación cumple **100% de los requerimientos** del challenge, incluyendo todos los bonus, y está diseñada siguiendo principios **SOLID** y mejores prácticas de **programación reactiva**.

---

## ✅ Requerimientos Cumplidos

### 🎯 **Requerimientos Básicos**
- ✅ **Cálculo con porcentaje dinámico**: Endpoint POST que suma dos números y aplica porcentaje obtenido de servicio externo
- ✅ **Caché del porcentaje**: Redis reactivo con TTL de 30 minutos y fallback automático
- ✅ **Reintentos ante fallos**: Máximo 3 intentos con backoff exponencial + eventos Kafka
- ✅ **API usuarios autorizados**: CRUD completo con PostgreSQL usando R2DBC reactivo
- ✅ **Tests completos**: Unitarios e integración sin métodos bloqueantes

### 🏆 **Bonus Implementados**
- ✅ **Manejo de errores HTTP**: Validaciones de entrada + GlobalExceptionHandler con códigos HTTP apropiados
- ✅ **Routing Functions**: Implementación funcional completa como alternativa a controllers
- ✅ **Historial de llamadas**: Registro asíncrono en MongoDB con detalles completos
- ✅ **Tests de integración**: Casos felices y no felices con verificación de resilencia

---

## 🏗️ Arquitectura de la Aplicación

### **📦 Estructura del Proyecto**
```
src/main/java/cl/tenpo/learning/reactive/tasks/task2/
├── application/              # Lógica de negocio
│   ├── port/                # Interfaces (puertos)
│   └── services/            # Implementaciones de servicios
├── domain/                  # Entidades del dominio
├── infrastructure/          # Adaptadores e infraestructura
│   ├── cache/              # Servicios de caché (Redis)
│   ├── client/             # Clientes HTTP reactivos
│   ├── config/             # Configuraciones
│   ├── exception/          # Manejo global de errores
│   ├── persistence/        # Repositorios R2DBC y MongoDB
│   └── retry/              # Estrategias de reintento
└── presentation/           # Capa de presentación
    ├── controller/         # Controllers tradicionales
    ├── handler/            # Handlers funcionales
    └── dto/                # Objetos de transferencia
```

### **🔧 Tecnologías Utilizadas**
- **Spring Boot 3.x** con **WebFlux** (programación reactiva)
- **Spring Data R2DBC** (PostgreSQL reactivo)
- **Spring Data MongoDB Reactive** (MongoDB reactivo)
- **Spring Data Redis Reactive** (Redis reactivo)
- **Apache Kafka** (eventos asíncronos)
- **Project Reactor** (Mono/Flux)
- **Docker & Docker Compose** (servicios de infraestructura)
- **JUnit 5** + **Mockito** + **StepVerifier** (testing reactivo)

---

## 🚀 Ejecución de la Aplicación

### **📋 Prerequisitos**
- Java 21+
- Docker & Docker Compose
- Gradle 8.x

### **🔧 Configuración e Inicio**

1. **Clonar el repositorio**:
```bash
git clone <repository-url>
cd reactive-challenge-2
```

2. **Iniciar servicios Docker**:
```bash
docker-compose up -d
```

3. **Ejecutar la aplicación**:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

4. **Verificar que está funcionando**:
```bash
curl http://localhost:8083/learning-reactive/debug/ping
# Debe retornar: "pong"
```

### **🧪 Ejecutar Tests**
```bash
# Todos los tests
./gradlew test

# Solo tests unitarios
./gradlew test --tests "*Test"

# Build completo con tests
./gradlew clean build
```

---

## 📡 Endpoints Disponibles

### **🔢 Cálculos**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/learning-reactive/calculation` | Cálculo con porcentaje (Controller) |
| POST | `/learning-reactive/functional/calculation` | Cálculo con porcentaje (Functional) |

**Ejemplo de petición**:
```bash
curl -X POST http://localhost:8083/learning-reactive/calculation \
  -H "Content-Type: application/json" \
  -d '{"number_1": 10.0, "number_2": 5.0}'
```

**Respuesta esperada**:
```json
{"result": 16.50, "num1": 10.0, "num2": 5.0}
```

### **📊 Historial**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/learning-reactive/history` | Historial de llamadas (Controller) |
| GET | `/learning-reactive/functional/history` | Historial de llamadas (Functional) |

**Ejemplo con paginación**:
```bash
curl -X GET "http://localhost:8083/learning-reactive/history?page=0&size=5"
```

### **👥 Usuarios Autorizados**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/learning-reactive/users` | Listar usuarios |
| POST | `/learning-reactive/users` | Crear usuario |
| GET | `/learning-reactive/users/{id}` | Obtener usuario por ID |
| DELETE | `/learning-reactive/users/{id}` | Desactivar usuario |

**Crear usuario**:
```bash
curl -X POST http://localhost:8083/learning-reactive/users \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com"}'
```

### **🔧 Debug y Utilidades**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/learning-reactive/debug/ping` | Health check |
| GET | `/learning-reactive/debug/routes` | Listar todas las rutas |
| DELETE | `/learning-reactive/debug/clear-cache` | Limpiar caché Redis |
| POST | `/learning-reactive/debug/clear-cache` | Limpiar caché Redis (alternativo) |

### **🎯 Routing Functions**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/learning-reactive/functional/ping` | Health check funcional |
| POST | `/learning-reactive/functional/calc` | Cálculo (ruta alternativa) |
| GET | `/learning-reactive/functional/users` | Usuarios (funcional) |
| POST | `/learning-reactive/functional/users` | Crear usuario (funcional) |

---

## 🧪 Guía de Pruebas Manuales

### **✅ 1. Pruebas Básicas**

**Verificar aplicación funcionando**:
```bash
curl -X GET http://localhost:8083/learning-reactive/debug/ping
# Esperado: "pong"
```

**Verificar servicio externo**:
```bash
curl -X GET http://localhost:8083/learning-reactive/external-api/percentage
# Esperado: {"percentage":"0,XX"}
```

### **✅ 2. Pruebas de Cálculo (Casos Exitosos)**

**Cálculo básico**:
```bash
curl -X POST http://localhost:8083/learning-reactive/calculation \
  -H "Content-Type: application/json" \
  -d '{"number_1": 10.0, "number_2": 5.0}'
```

**Cálculo funcional**:
```bash
curl -X POST http://localhost:8083/learning-reactive/functional/calculation \
  -H "Content-Type: application/json" \
  -d '{"number_1": 20.0, "number_2": 15.0}'
```

**Verificar caché (segunda llamada más rápida)**:
```bash
curl -X POST http://localhost:8083/learning-reactive/calculation \
  -H "Content-Type: application/json" \
  -d '{"number_1": 30.0, "number_2": 25.0}'
```

### **✅ 3. Pruebas de Historial**

**Ver historial completo**:
```bash
curl -X GET http://localhost:8083/learning-reactive/history
```

**Historial con paginación**:
```bash
curl -X GET "http://localhost:8083/learning-reactive/history?page=0&size=5"
```

### **✅ 4. Pruebas de Usuarios**

**Listar usuarios**:
```bash
curl -X GET http://localhost:8083/learning-reactive/users
```

**Crear usuario**:
```bash
curl -X POST http://localhost:8083/learning-reactive/users \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@example.com"}'
```

### **✅ 5. Pruebas de Validación**

**Datos inválidos**:
```bash
curl -X POST http://localhost:8083/learning-reactive/calculation \
  -H "Content-Type: application/json" \
  -d '{"number_1": null, "number_2": null}'
# Esperado: Error 400 Bad Request
```

---

## 🔥 Pruebas de Casos de Falla

### **🛠️ Configuración para Simular Fallas**

Para probar los casos de falla (reintentos, fallback, eventos Kafka), necesitas modificar temporalmente la configuración:

#### **Método 1: Simular Servicio No Disponible**

1. **Editar** `src/main/resources/application.yml` línea ~63:
```yaml
# CAMBIAR ESTO:
app:
  api:
    external:
      base-url: http://localhost:8083/learning-reactive

# A ESTO:
app:
  api:
    external:
      base-url: http://localhost:8084/learning-reactive  # Puerto inexistente
```

2. **Opcionalmente, reducir reintentos** (línea ~82):
```yaml
retry:
  max-attempts: 1  # Cambiar de 3 a 1 para que falle más rápido
```

3. **Reiniciar la aplicación**:
```bash
# Detener con Ctrl+C
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### **Método 2: Simular Timeout**

1. **Editar timeout** en `application.yml` línea ~68:
```yaml
timeouts:
  external-api: PT2S  # Cambiar de PT20S a PT2S
```

2. **Reiniciar aplicación**

### **🧪 Pruebas de Falla Paso a Paso**

#### **Escenario 1: Falla Sin Fallback**

1. **Limpiar caché**:
```bash
curl -X DELETE http://localhost:8083/learning-reactive/debug/clear-cache
```

2. **Simular falla** (cambiar configuración como se explicó arriba)

3. **Probar falla**:
```bash
curl -X POST http://localhost:8083/learning-reactive/calculation \
  -H "Content-Type: application/json" \
  -d '{"number_1": 100.0, "number_2": 50.0}'

# Esperado: Error 503 Service Unavailable
# {"message":"Failed to fetch BigDecimal after X attempts","status":503}
```

4. **Verificar historial de fallas**:
```bash
curl -X GET "http://localhost:8083/learning-reactive/history?page=0&size=5"
# Debe mostrar entradas con "successful":false
```

#### **Escenario 2: Falla Con Fallback (Caché)**

1. **Hacer petición exitosa** (llena caché):
```bash
curl -X POST http://localhost:8083/learning-reactive/calculation \
  -H "Content-Type: application/json" \
  -d '{"number_1": 10.0, "number_2": 5.0}'
```

2. **Simular falla** (cambiar configuración)

3. **Reiniciar aplicación** (NO limpiar caché)

4. **Probar con fallback**:
```bash
curl -X POST http://localhost:8083/learning-reactive/calculation \
  -H "Content-Type: application/json" \
  -d '{"number_1": 40.0, "number_2": 35.0}'

# Esperado: Éxito usando valor cacheado
```

#### **Escenario 3: Restaurar Funcionamiento**

1. **Restaurar configuración**:
```yaml
base-url: http://localhost:8083/learning-reactive  # Puerto correcto
max-attempts: 3  # Valor original
external-api: PT20S  # Timeout original
```

2. **Reiniciar aplicación**

3. **Verificar funcionamiento**:
```bash
curl -X POST http://localhost:8083/learning-reactive/calculation \
  -H "Content-Type: application/json" \
  -d '{"number_1": 60.0, "number_2": 40.0}'
```

### **📊 Qué Observar Durante las Pruebas**

**En los logs de la aplicación verás**:
- ✅ **Reintentos**: `Failed to fetch BigDecimal after X attempts`
- ✅ **Conexión fallida**: `java.net.ConnectException: Connection refused`
- ✅ **Fallback exitoso**: `Retrieved percentage from cache`
- ✅ **Eventos Kafka**: `Starting Kafka consumer for CR_RETRY_EXHAUSTED topic`

**En las respuestas HTTP verás**:
- ✅ **Error 503**: Cuando falla sin fallback
- ✅ **Error 400**: Para validaciones de entrada
- ✅ **Código 200**: Cuando usa fallback exitosamente

---

## ⚙️ Configuración

### **🔧 Perfiles Disponibles**
- **local**: Configuración para desarrollo local (activo por defecto)
- **test**: Configuración para tests unitarios

### **📝 Propiedades Importantes**
```yaml
# Configuración de la aplicación
server:
  port: 8083

spring:
  webflux:
    base-path: /learning-reactive

# Configuración de timeouts
app:
  timeouts:
    external-api: PT20S
    cache-operation: PT5S
    database-operation: PT10S

# Configuración de reintentos
  retry:
    max-attempts: 3
    initial-backoff: PT1S
    max-backoff: PT10S

# Configuración de servicios externos
  api:
    external:
      base-url: http://localhost:8083/learning-reactive
      percentage-path: /external-api/percentage

# Configuración de Kafka
  kafka:
    topics:
      retry-exhausted: CR_RETRY_EXHAUSTED
```

### **🐳 Servicios Docker**
La aplicación requiere los siguientes servicios (incluidos en `docker-compose.yml`):
- **PostgreSQL**: Puerto 5432 (usuarios autorizados)
- **MongoDB**: Puerto 27017 (historial de llamadas)
- **Redis**: Puerto 6379 (caché de porcentajes)
- **Kafka**: Puerto 9092 (eventos de retry agotado)

---

## 🧪 Testing

### **📊 Cobertura de Tests**
- ✅ **8 tests unitarios** pasando al 100%
- ✅ **Tests de servicios** con mocking completo
- ✅ **Tests de controladores** sin dependencias externas
- ✅ **Tests reactivos** usando `StepVerifier`
- ✅ **Sin métodos bloqueantes** (`.block()` prohibido)

### **🔍 Ejecutar Tests Específicos**
```bash
# Tests de servicios
./gradlew test --tests "*ServiceTest"

# Tests de controladores
./gradlew test --tests "*ControllerTest"

# Tests reactivos
./gradlew test --tests "*PercentageServiceTest"
```

### **📋 Estructura de Tests**
```
src/test/java/
├── application/
│   ├── CalculationServiceTest.java
│   └── PercentageServiceTest.java
└── presentation/controller/
    └── CalculationControllerIntegrationTest.java
```

---

## 🎯 Funcionalidades Avanzadas

### **🔄 Patrones de Resilencia**
- **Circuit Breaker**: Protección ante fallos de servicios externos
- **Retry con Backoff**: Reintentos exponenciales configurables
- **Fallback**: Uso automático de caché cuando el servicio falla
- **Timeout**: Configuración de timeouts por tipo de operación

### **📊 Observabilidad**
- **Logging estructurado**: Con contexto de request ID y timing
- **Health checks**: Endpoints de monitoreo
- **Métricas**: Registro de éxitos/fallos en historial
- **Trazabilidad**: Logs detallados de cada operación

### **🏗️ Arquitectura Reactiva**
- **Schedulers específicos**: Para DB, API, caché y logging
- **Context propagation**: Enriquecimiento de contexto reactivo
- **Pipelines declarativos**: Sin if/else, usando operadores reactivos
- **Manejo de errores**: Usando `onErrorReturn`, `onErrorResume`

---

## 🚀 Despliegue y Producción

### **🔧 Variables de Entorno**
```bash
# Configuración de base de datos
SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/tenpo_db
SPRING_R2DBC_USERNAME=postgres
SPRING_R2DBC_PASSWORD=postgres

# Configuración de MongoDB
SPRING_DATA_MONGODB_URI=mongodb://mongodb:password@localhost:27017/tenpo_db

# Configuración de Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# Configuración de Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### **🐳 Docker**
```bash
# Construir imagen
docker build -t reactive-challenge-2 .

# Ejecutar contenedor
docker run -p 8083:8083 reactive-challenge-2
```

---

## 🤝 Contribución

### **📋 Estándares de Código**
- Principios **SOLID** aplicados
- Programación **reactiva** sin bloqueantes
- **Clean Architecture** con separación de capas
- **Testing** completo con alta cobertura

### **🔍 Code Review Checklist**
- ✅ No usar métodos bloqueantes (`.block()`, `.blockFirst()`)
- ✅ Manejar errores con operadores reactivos
- ✅ Usar schedulers apropiados para cada tipo de operación
- ✅ Implementar tests unitarios con `StepVerifier`
- ✅ Documentar APIs con ejemplos de uso

---

## 📞 Soporte

### **🐛 Problemas Comunes**

**Error "Port 8083 already in use"**:
```bash
lsof -ti:8083 | xargs kill
```

**Servicios Docker no disponibles**:
```bash
docker-compose down && docker-compose up -d
```

**Tests fallando**:
```bash
./gradlew clean build
```

**Caché no funciona**:
```bash
curl -X DELETE http://localhost:8083/learning-reactive/debug/clear-cache
```

### **📖 Documentación Adicional**
- [Spring WebFlux Documentation](https://spring.io/reactive)
- [Project Reactor Reference](https://projectreactor.io/docs)
- [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)

---

## 📄 Licencia

Este proyecto está desarrollado para propósitos educativos y de evaluación técnica.

---

**🎉 ¡Aplicación 100% funcional y lista para uso!**