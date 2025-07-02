# Challenge Reactivo con Spring WebFlux

Este proyecto implementa una API REST en Spring WebFlux utilizando Java 21, que incluye cálculos con porcentajes dinámicos, gestión de usuarios autorizados, y registro histórico de llamadas. Implementado siguiendo los principios de la Arquitectura Limpia (Clean Architecture).

## Funcionalidades Implementadas

1. **Cálculo con porcentaje dinámico**
   - Endpoint: `POST /v1/calculation`
   - Suma dos números y aplica un porcentaje obtenido de un servicio externo
   - Caché del porcentaje por 30 minutos en Redis
   - Reintentos para el servicio externo (máx 3 intentos)
   - Evento Kafka cuando se agotan los reintentos

2. **API de usuarios autorizados**
   - Endpoints para alta y baja de usuarios
   - Almacenamiento en PostgreSQL con R2DBC
   - Validación de datos de entrada

3. **Historial de llamadas (Bonus)**
   - Registro asíncrono de todas las llamadas a la API mediante filtros HTTP reactivos
   - Información detallada (fecha, endpoint, parámetros, respuesta/error)
   - Almacenamiento en MongoDB reactivo
   - Acceso restringido a usuarios autorizados

4. **Manejo de errores HTTP (Bonus)**
   - Validaciones de inputs
   - Manejo adecuado de errores HTTP (4XX y 5XX)
   - Mensajes descriptivos con formato estandarizado

## Arquitectura del Proyecto

Este proyecto utiliza una arquitectura limpia (Clean Architecture) con una separación clara de responsabilidades:

```
cl.tenpo.learning.reactive.tasks.task2
├── domain           # Entidades del dominio y reglas de negocio
│   └── model        # Objetos de dominio (Calculation, CallHistory, AuthorizedUser)
├── application      # Casos de uso y lógica de negocio
│   └── services     # Servicios de aplicación (Calculation, CallHistory, AuthorizedUser)
├── infrastructure   # Adaptadores y detalles técnicos
│   ├── config       # Configuraciones (Redis, Kafka, WebClient)
│   ├── event        # Eventos y consumidores Kafka
│   ├── exception    # Manejo global de excepciones
│   ├── filter       # Filtros HTTP reactivos
│   └── persistence  # Repositorios reactivos
└── presentation     # Interfaces de usuario y API
    ├── controller   # Controladores REST
    └── dto          # Objetos de transferencia de datos
```

## Implementación de Registro de Históricos

El registro de históricos se implementa utilizando un enfoque reactivo mediante:

1. **Filtros HTTP Reactivos**: Captura todas las solicitudes HTTP y sus respuestas de forma no bloqueante
2. **Procesamiento Asíncrono**: Guarda el historial de llamadas en MongoDB sin bloquear el flujo principal
3. **Patrón Decorator**: Utilizado para leer el cuerpo de la solicitud y respuesta sin consumirlos
4. **Bounded Elastic Scheduler**: Evita bloqueos en operaciones I/O como el guardado en MongoDB

## Cómo ejecutar

1. Levanta los servicios con Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Ejecuta la aplicación con:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

3. Prueba el endpoint de comprobación:
   ```bash
   curl -X GET http://localhost:8083/learning-reactive/external-api/integrations
   ```

4. Ejemplos de uso:
   ```bash
   # Cálculo con porcentaje
   curl -X POST http://localhost:8083/learning-reactive/v1/calculation \
     -H "Content-Type: application/json" \
     -d '{"num1": 10.5, "num2": 5.5}'

   # Crear usuario autorizado
   curl -X POST http://localhost:8083/learning-reactive/v1/users \
     -H "Content-Type: application/json" \
     -d '{"username": "user1", "email": "user1@example.com"}'

   # Consultar historial (requiere autorización)
   curl -X GET http://localhost:8083/learning-reactive/v1/calculation \
     -H "Authorization: Bearer admin"
   ```

## Tests implementados

- Tests unitarios para la lógica de negocio
- Tests de integración para endpoints
- Pruebas de casos felices y no felices
