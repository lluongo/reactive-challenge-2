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
    