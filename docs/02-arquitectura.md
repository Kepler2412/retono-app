# Decisiones arquitectónicas (ADR)

Cada registro documenta una decisión, su contexto, las alternativas descartadas y las consecuencias asumidas. El formato sigue la convención de Architecture Decision Records de Michael Nygard.

---

## ADR-001 · Arquitectura offline-first con la base local como fuente única de verdad

**Estado:** Aceptada · Sprint 1

**Contexto.** La captura de datos ocurre en lotes de restauración en zonas rurales de Antioquia, donde la conectividad es intermitente o inexistente. Una arquitectura convencional que consulte la red en cada operación produciría pantallas de error, estados de carga indefinidos y pérdida de trabajo.

**Decisión.** La interfaz nunca consulta la red. Observa Room mediante `Flow`. La red es un detalle de infraestructura que un componente en segundo plano gestiona por detrás.

**Alternativas descartadas.**

| Alternativa | Motivo del descarte |
|---|---|
| Caché de red con invalidación por tiempo | Sigue asumiendo que la red es la verdad; el dato local se trata como copia desechable |
| Sincronización manual iniciada por el usuario | Traslada al técnico una responsabilidad operativa que el sistema puede asumir; alto riesgo de olvido al final de la jornada |

**Consecuencias.** El comportamiento de la aplicación es idéntico con y sin señal, y se elimina toda una clase de estados de error de la interfaz. A cambio, se asume la complejidad de mantener estado de sincronización por registro y de resolver conflictos, complejidad que se concentra en la capa de datos y no se filtra hacia la interfaz.

---

## ADR-002 · Identificadores UUID generados en el cliente

**Estado:** Aceptada · Sprint 1

**Contexto.** Varios dispositivos registran siembras simultáneamente en campo, sin coordinación previa y sin conectividad. Si el identificador lo asigna el servidor, crear un registro requeriría red, lo que contradice ADR-001.

**Decisión.** Cada registro recibe un UUID v4 generado en el dispositivo en el momento de la creación. La clave primaria es la misma en local y en el servidor.

**Alternativas descartadas.**

| Alternativa | Motivo del descarte |
|---|---|
| Autoincremental local + id del servidor | Obliga a mantener dos identidades por registro y a reescribir referencias tras sincronizar |
| Id compuesto por dispositivo y contador | Requiere registro previo del dispositivo; frágil ante reinstalaciones |

**Consecuencias.** Colisión estadísticamente despreciable y creación de registros totalmente independiente de la red. El costo es un identificador de 36 caracteres en lugar de un entero, con impacto marginal en el tamaño de la base para el volumen previsto.

---

## ADR-003 · WorkManager para la sincronización diferida

**Estado:** Aceptada · Sprint 3

**Contexto.** Los registros pendientes deben subir cuando aparezca conectividad, aunque la aplicación esté cerrada o el dispositivo se haya reiniciado. Una jornada de campo puede terminar con la batería agotada.

**Decisión.** Se usa WorkManager con restricción `NetworkType.CONNECTED`, política `KEEP` para el trabajo único y reintento exponencial desde 30 segundos.

**Alternativas descartadas.**

| Alternativa | Motivo del descarte |
|---|---|
| Servicio en primer plano | Consumo de batería inaceptable en jornadas largas; notificación permanente intrusiva |
| Corrutina en el ámbito de la aplicación | Muere con el proceso; no sobrevive al reinicio |
| `JobScheduler` directo | WorkManager ya lo abstrae y añade compatibilidad hacia versiones anteriores |

**Consecuencias.** El sistema operativo decide el momento de ejecución, lo que preserva batería. Se pierde control sobre el instante exacto de la sincronización, compensado con un botón de sincronización manual en la pantalla correspondiente.

---

## ADR-004 · Resolución de conflictos por última escritura, con revisión manual

**Estado:** Aceptada · Sprint 3

**Contexto.** Dos dispositivos pueden modificar el mismo registro sin conocerse. Los datos sustentan verificación de ejecución ante entidades financiadoras.

**Decisión.** El servidor compara marcas temporales. Los registros rechazados no se descartan ni se sobrescriben: se marcan como `CONFLICTO` y se exponen en la interfaz para decisión humana.

**Alternativas descartadas.**

| Alternativa | Motivo del descarte |
|---|---|
| Última escritura silenciosa | Pérdida invisible de información en un dominio donde la trazabilidad es el propósito del sistema |
| CRDT | Complejidad desproporcionada para el modelo de datos y el volumen del proyecto |

**Consecuencias.** Ningún dato se pierde sin que alguien lo sepa. A cambio, existe una cola de conflictos que requiere atención de una persona.

---

## ADR-005 · Cifrado en reposo con SQLCipher y Android Keystore

**Estado:** Aceptada · Sprint 3

**Contexto.** La base local almacena coordenadas precisas de predios privados y evidencia de ejecución de proyectos financiados. Los dispositivos de campo se pierden o se roban.

**Decisión.** Room se instancia sobre SQLCipher mediante `SupportFactory`, con clave de 256 bits generada con `SecureRandom` y custodiada en `EncryptedSharedPreferences` respaldada por Android Keystore. La aplicación declara `allowBackup="false"`.

**Alternativas descartadas.**

| Alternativa | Motivo del descarte |
|---|---|
| Sin cifrado, confiando en el sandbox de Android | El sandbox no protege ante acceso root ni extracción física del almacenamiento |
| Cifrado campo a campo | Impide consultar y ordenar por los campos cifrados; rompe los índices |

**Consecuencias.** El archivo de base de datos es ilegible fuera de la aplicación. El costo es una penalización de rendimiento en lectura y escritura, aceptable para el volumen de registros de una jornada de campo.

---

## ADR-006 · Clean Architecture con dominio independiente de Android

**Estado:** Aceptada · Sprint 1

**Contexto.** El objetivo específico 2 compromete una cobertura de pruebas del 70 % sobre la lógica de negocio. Las pruebas instrumentadas requieren emulador y su ciclo de retroalimentación se mide en minutos.

**Decisión.** La capa de dominio se escribe en Kotlin puro, sin importar una sola clase de Android. Las interfaces de repositorio se declaran en el dominio y se implementan en la capa de datos.

**Consecuencias.** La lógica central se prueba con JUnit en la JVM en segundos. El costo es la duplicación entre entidades de persistencia y modelos de dominio, con mapeadores explícitos entre ambos: verbosidad a cambio de que un cambio de esquema en la base no se propague al negocio.

---

## ADR-007 · Room como fuente de verdad pese a que Firestore tiene caché offline

**Estado:** Aceptada · Sprint 3

**Contexto.** Cloud Firestore incluye persistencia offline activada por defecto en Android: encola escrituras y sirve lecturas desde caché sin conexión. Cabe preguntarse por qué el proyecto mantiene entonces una base Room propia. Es la objeción más razonable que puede recibir esta arquitectura y merece respuesta explícita.

**Decisión.** Room sigue siendo la fuente única de verdad. Firestore actúa exclusivamente como receptor de sincronización.

**Razones.**

La caché de Firestore es **opaca por registro**. No expone si un documento concreto está pendiente de subir; solo hay un indicador global de escrituras pendientes. El proyecto necesita mostrar al técnico exactamente cuáles de sus registros no han llegado al servidor, y eso exige un campo de estado propio por fila.

La **resolución de conflictos de Firestore es de última escritura y silenciosa**. En datos que sustentan verificación ante entidades financiadoras, perder información sin que nadie se entere es inaceptable (ver ADR-004). Con control propio del estado se puede marcar `CONFLICTO` y exigir decisión humana.

Las **consultas locales complejas** —el cálculo de supervivencia agrupa monitoreos por individuo y toma el más reciente— se resuelven en SQL sobre Room. Hacerlo contra la caché de Firestore obligaría a traer todos los documentos y agregar en memoria.

El **cifrado en reposo** (ADR-005) se aplica sobre el archivo SQLite mediante SQLCipher. La caché interna de Firestore no ofrece control equivalente.

Por último, **desacopla del proveedor**. `SyncWorker` depende de la interfaz `FuenteRemotaSiembras`, no de Firestore. Migrar a una API propia implica una implementación nueva sin tocar dominio ni interfaz.

**Consecuencias.** Se mantienen dos almacenamientos locales, con el costo en espacio que eso implica. A cambio, el estado de sincronización es explícito y auditable, y el backend es sustituible.

---

## ADR-008 · Autenticación por correo con sesión persistente

**Estado:** Aceptada · Sprint 3

**Contexto.** Firestore necesita identificar quién escribe para aplicar reglas de seguridad y trazar la autoría de cada dato. Pero exigir autenticación choca de frente con el requisito central del proyecto: la aplicación debe funcionar sin red.

**Decisión.** Autenticación con correo y contraseña mediante Firebase Authentication. La sesión se persiste en el dispositivo tras el primer inicio exitoso, de modo que solo el primer acceso requiere conectividad.

**Alternativas descartadas.**

| Alternativa | Motivo del descarte |
|---|---|
| Autenticación anónima | No permite trazar autoría entre reinstalaciones ni recuperar la cuenta; la evidencia perdería atribución |
| Inicio de sesión con Google | Requiere configurar cliente OAuth y huella SHA-1; complejidad innecesaria para el pilotaje |
| Sin autenticación | Obligaría a dejar Firestore abierto a internet, con la clave de API visible en el APK |

**Consecuencias.** El técnico se autentica una vez con conectividad y luego trabaja días en campo sin red, conservando la autoría de sus registros. El costo es que un usuario nuevo no puede empezar a trabajar en zona sin cobertura. Se mitiga documentando que el alta debe hacerse antes de la salida a campo.

Cerrar sesión **no** borra los datos locales: un registro pendiente no puede desaparecer porque alguien salió de la cuenta.

---

## ADR-009 · Procesamiento de fotografías en el dispositivo

**Estado:** Aceptada · Sprint 2

**Contexto.** La fotografía documenta el estado del individuo y respalda la evidencia de ejecución. Una imagen sin procesar de un móvil actual ronda los 4 MB e incluye metadatos EXIF con coordenadas GPS, modelo del equipo y número de serie.

**Decisión.** Toda imagen capturada se reescala a 1600 px en el lado mayor, se corrige su orientación y se recomprime en JPEG al 80 % de calidad antes de persistirse en el almacenamiento privado de la aplicación.

**Razones.** El **ancho de banda** es el factor dominante: una jornada de cincuenta árboles produciría 200 MB que habría que subir por una conexión rural intermitente; tras el procesamiento son unos 10 MB. La **privacidad** es la segunda: el proyecto ya almacena la coordenada de forma explícita y controlada, de modo que duplicarla oculta dentro del archivo expondría información sin conocimiento del usuario. Recomprimir desde el *bitmap* descarta el EXIF completo, lo que resuelve ambas cosas en una sola operación.

**Consecuencia no evidente.** Al eliminar el EXIF se pierde también la etiqueta de rotación, así que hay que leerla y aplicarla a los píxeles **antes** de descartarla. Omitir ese paso deja todas las fotografías de lado, y es un error que solo se detecta mirando el resultado.

**Alternativas descartadas.**

| Alternativa | Motivo del descarte |
|---|---|
| Guardar el original y comprimir al subir | El dispositivo acumularía gigabytes durante los días sin conectividad |
| Borrar solo las etiquetas GPS del EXIF | Conserva identificadores del equipo; recomprimir es más simple y más completo |
| Almacenamiento externo compartido | Cualquier aplicación con permiso de lectura podría ver las imágenes de campo |

**Consecuencias.** Se pierde resolución original de forma irreversible. Es aceptable: la fotografía sirve para identificar la especie y el estado del individuo, no para análisis fotogramétrico.

---

## ADR-010 · Sincronización en orden de dependencias del modelo

**Estado:** Aceptada · Sprint 3

**Contexto.** Tres entidades se sincronizan: lotes, siembras y monitoreos. Entre ellas existe una cadena de referencias: un monitoreo pertenece a una siembra, y una siembra pertenece a un lote. En campo se crean en cualquier orden y todas quedan pendientes a la vez.

**Decisión.** `SyncWorker` envía siempre en el orden lote → siembra → monitoreo, en bloques separados, y solo continúa con el siguiente nivel tras procesar el anterior.

**Razones.** Enviar un monitoreo antes que la siembra que lo origina dejaría un documento que apunta a un identificador inexistente en el servidor. Firestore no impone integridad referencial, así que nada impediría la escritura: el resultado sería un conjunto de datos silenciosamente corrupto, que es peor que un error visible.

**Consecuencias.** La sincronización de una jornada completa puede requerir varias ejecuciones del *worker*, ya que cada nivel se limita a cien registros por lote. Se acepta: el reencolado es automático y el usuario ve el progreso en el contador de pendientes.
