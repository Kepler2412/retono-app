# Taller ABP — Entrega 1: Ecosistema Interactivo Mobile (20 %)

**Curso:** Diseño de Aplicaciones Móviles
**Programa:** Facultad de Ingeniería y Ciencias Ambientales — Fundación Universitaria Católica del Norte
**Estudiante:** Yeison Alberto Gómez Amaya
**Modalidad:** Individual
**Metodología:** Aprender Haciendo (ABP)
**Fecha de entrega:** Septiembre de 2026

---

## 1. Datos del proyecto y asistente metodológico móvil

### 1.1 Título formal del proyecto móvil

> **Retoño: aplicación móvil nativa Android *offline-first* para el inventario forestal georreferenciado y el seguimiento de supervivencia de siembras en viveros comunitarios de Antioquia.**

El título delimita cuatro elementos exigidos por la rúbrica: la **tecnología de cliente** (Android nativo), el **atributo arquitectónico diferenciador** (*offline-first*), el **dominio funcional** (inventario forestal y supervivencia de siembras) y el **contexto de aplicación** (viveros comunitarios de Antioquia).

### 1.2 Integrantes del equipo

| Integrante | Rol Scrum | Responsabilidades |
|---|---|---|
| Yeison Alberto Gómez Amaya | Product Owner, Scrum Master y Development Team | Al ser un proyecto individual, se asumen los tres roles de forma secuencial por ceremonia: definición y priorización del *product backlog*, facilitación de las revisiones de sprint y ejecución técnica completa (UX/UI, desarrollo Android, pruebas y despliegue). |

> **Nota metodológica.** Scrum está diseñado para equipos de 3 a 9 personas. En una ejecución individual no se "simula" un equipo: se adopta un **Scrum adaptado a un solo desarrollador**, conservando los artefactos (*product backlog*, *sprint backlog*, incremento) y los eventos (planificación, revisión y retrospectiva de sprint), pero sustituyendo el *daily scrum* por una bitácora diaria escrita en el repositorio. Esta adaptación se documenta explícitamente porque presentar Scrum puro en solitario sería metodológicamente incorrecto.

### 1.3 Selección de metodología de desarrollo móvil

**Metodología seleccionada:** Ágil móvil — Scrum adaptado + Sprints + prototipado UX/UI bajo Design Thinking, orientada a la construcción de un MVP.

**Descripción extendida del marco metodológico**

El proyecto se desarrolla bajo un marco ágil híbrido que combina tres capas complementarias. La primera es **Design Thinking**, aplicada en la fase de descubrimiento: se parte de la empatía con el usuario final (el viverista y el técnico de campo), se define el problema desde su experiencia real, se idean alternativas de solución y se materializan en prototipos de baja y alta fidelidad construidos en Figma antes de escribir una sola línea de código. Esta capa evita el error frecuente de programar sobre supuestos no validados.

La segunda capa es **Scrum adaptado**, que organiza la ejecución en sprints de dos semanas con un incremento funcional verificable al cierre de cada uno. El *product backlog* se prioriza por valor de negocio y riesgo técnico, de modo que las historias que atacan la incertidumbre mayor —la sincronización diferida y la resolución de conflictos— se abordan en los primeros sprints y no al final, cuando ya no habría margen de corrección.

La tercera capa es el enfoque **Lean Mobile / MVP**, que restringe deliberadamente el alcance de la primera versión al conjunto mínimo de funcionalidades que permiten validar la hipótesis central del proyecto: que un registro móvil que funciona sin conectividad reduce la pérdida de información de campo frente al registro en papel. Todo lo que no contribuya a validar esa hipótesis se difiere al *backlog* de versiones posteriores.

La articulación de las tres capas produce un ciclo de retroalimentación corto: prototipo → incremento funcional → prueba en dispositivo real → ajuste. Este ciclo es el que sostiene la coherencia entre el problema planteado, los objetivos SMART y la arquitectura seleccionada.

### 1.4 Planificación de sprints

| Sprint | Semanas | Objetivo del incremento | Entregable verificable | Estado |
|---|---|---|---|---|
| Sprint 0 | 1–2 | Descubrimiento y diseño | Mapa de empatía, *user journey*, wireframes y prototipo interactivo en Figma | Completado |
| Sprint 1 | 3–4 | Núcleo de persistencia local | Base Room cifrada operativa, administración de lotes y registro de siembras sin red | Completado |
| Sprint 2 | 5–6 | Captura enriquecida en campo | Georreferenciación GPS con validación de precisión, fotografía procesada, formulario de monitoreo | Completado |
| Sprint 3 | 7–8 | Sincronización y endurecimiento | Autenticación, motor de sincronización con WorkManager y Firestore, reglas de seguridad, pruebas | Completado |

**Estado funcional al momento de la entrega.** La aplicación compila, se instala y opera de extremo a extremo: autenticación con sesión persistente, administración de lotes (creación, edición y selección del lote activo), registro georreferenciado con fotografía, monitoreo de supervivencia por individuo, cálculo local del indicador y sincronización con Cloud Firestore. Trece pruebas unitarias cubren las reglas de negocio de la capa de dominio.

### 1.5 Enlaces del proyecto

| Recurso | Enlace | Estado |
|---|---|---|
| Repositorio GitHub | https://github.com/Kepler2412/retono-app | Publicado, visibilidad pública verificada |
| Landing page del proyecto | `landing/index.html` | Código listo en `landing/index.html` |
| Especificación de diseño | `docs/06-diseno-ux.md` | Sistema de diseño y seis maquetas de alta fidelidad |
| Prototipo  | `mockups\mockups.html` | Prototipo con todas sus definiciones |

El repositorio es el entregable que se califica y ya está en línea con visibilidad pública. La landing page está construida y lista para desplegar en Netlify apuntando al directorio `landing/`; el prototipo de Figma corresponde al Sprint 0, aún en curso.

---

## 2. Planteamiento del problema y pregunta problema

### 2.1 Contexto y diagnóstico del problema móvil

Antioquia es uno de los departamentos con mayor pérdida de bosque del país fuera de la Amazonía. Según el Sistema de Monitoreo de Bosques y Carbono del IDEAM, en 2023 registró 8.139 hectáreas deforestadas, la quinta cifra nacional, y en 2024 registró 7.197 hectáreas. Es importante señalar la otra mitad del dato: en 2024 Antioquia figuró entre los departamentos con **mayores reducciones** de deforestación del país. La conclusión correcta no es que la situación empeore, sino que existe un esfuerzo sostenido de restauración cuyo resultado hay que poder medir.

Ese esfuerzo se materializa en programas concretos. Bajo el Plan de Desarrollo *Por Antioquia Firme 2024–2027*, la Gobernación ejecuta a través de la Secretaría de Ambiente y en alianza con la Reforestadora Integral de Antioquia (RIA) el proyecto **Cuencas Abastecedoras de Acueductos**, que entre 2024 y 2025 reforestó 306,4 hectáreas mediante la siembra de 236.339 árboles en 24 municipios, con una inversión cercana a los 5.000 millones de pesos y 256 empleos generados. Para 2025 el programa contempló la restauración de 267 hectáreas adicionales y —dato central para este proyecto— **el mantenimiento de 305 hectáreas ya intervenidas**.

Esa distinción entre *sembrar* y *mantener lo sembrado* es precisamente el vacío que este proyecto aborda. Existe además un antecedente directo del modelo de vivero comunitario en el oriente antioqueño: en 2021 el Ministerio de Ambiente y la corporación Masbosques firmaron acuerdos con 650 familias vinculadas a BancO2, esquema de Pago por Servicios Ambientales, para restaurar 1.116 hectáreas y sembrar 589.000 árboles nativos en 26 municipios, estableciendo tres viveros temporales —dos de ellos en Granada y San Francisco— y capacitando a 30 personas en viveros forestales.

Restaurar no termina con la siembra. La **tasa de supervivencia por lote y por especie** es el indicador que determina si una intervención fue efectiva y el que las entidades financiadoras necesitan para verificar la ejecución.

El problema aparece exactamente en el punto donde ese dato se origina. La captura ocurre en campo —predios dispersos, laderas, zonas de ronda hídrica— y allí la conectividad es el factor limitante.

Colombia ha reducido su brecha digital de forma sostenida, pero esta persiste. Según la Encuesta Nacional de Calidad de Vida del DANE, en 2024 el 65,6 % de los hogares del país tenía acceso a internet, con 72,5 % en las cabeceras municipales. En cuanto a la frecuencia de uso, el 80,5 % de las personas de cinco años y más en cabeceras usó internet todos los días, frente al 59,6 % en centros poblados y rural disperso: una diferencia cercana a veintiún puntos porcentuales.

Conviene ser preciso sobre qué prueba y qué no prueba esa cifra. Mide conectividad de hogares y frecuencia de uso personal, no cobertura de señal en un lote de ladera. Sirve como indicador de contexto de la desigualdad territorial, no como medición directa del escenario del proyecto, porque el técnico no trabaja en un hogar: trabaja en el predio, donde la señal es intermitente o inexistente. La verificación directa de esa condición corresponde al trabajo de campo del pilotaje.

La consecuencia operativa es un flujo de trabajo fragmentado en tres momentos y tres soportes distintos:

| Momento | Soporte actual | Falla asociada |
|---|---|---|
| Captura en el lote | Formato en papel | Deterioro por lluvia, pérdida de planillas, letra ilegible, ausencia de coordenadas exactas |
| Transcripción | Hoja de cálculo en la oficina | Reproceso manual, errores de digitación, retraso de días o semanas |
| Consolidación | Archivos dispersos por proyecto | Imposibilidad de calcular supervivencia por lote en tiempo oportuno, versiones inconsistentes del mismo dato |

A esto se suma un problema de **identidad del individuo sembrado**: sin una referencia georreferenciada estable, el monitoreo de la visita 2 no puede vincularse con certeza al mismo árbol registrado en la visita 1, lo que degrada por completo la validez del cálculo de supervivencia.

**Síntesis del problema.** El registro y monitoreo de siembras en viveros comunitarios de Antioquia depende de procesos manuales en papel que operan en zonas de conectividad intermitente, lo que produce pérdida y duplicación de registros, ausencia de georreferenciación confiable del individuo sembrado y retrasos en la consolidación de la tasa de supervivencia, indicador del cual depende la verificación de las metas de restauración ante las entidades financiadoras.

> **Trazabilidad de fuentes.** Todas las cifras de esta sección fueron verificadas en fuentes primarias u oficiales el 1 de septiembre de 2026, y aparecen con su URL completa en `docs/05-referencias.md`. Las de deforestación provienen del IDEAM y del Ministerio de Ambiente; las de brecha digital, de la Encuesta Nacional de Calidad de Vida del DANE; las de los programas de restauración, de comunicados de la Gobernación de Antioquia y del Ministerio de Ambiente.

### 2.2 Formulación de la pregunta problema

> **¿De qué manera la implementación de una aplicación móvil nativa Android con arquitectura *offline-first*, persistencia local en Room y sincronización diferida mediante WorkManager permite reducir la pérdida de registros de campo y disminuir a menos de 24 horas el tiempo de consolidación de la tasa de supervivencia por lote, en viveros comunitarios de restauración ecológica de Antioquia que operan en zonas de conectividad intermitente, durante un pilotaje de ocho semanas?**

La pregunta integra los siete componentes que exige una formulación rigurosa en ingeniería:

| Componente | Elemento en la pregunta |
|---|---|
| **Qué** (objeto) | Aplicación móvil nativa Android *offline-first* |
| **Cómo** (medio técnico) | Room + WorkManager, sincronización diferida |
| **Qué se busca mejorar** | Pérdida de registros y tiempo de consolidación |
| **Meta cuantificable** | Consolidación en menos de 24 horas |
| **Dónde** (contexto) | Viveros comunitarios de Antioquia, conectividad intermitente |
| **Quién** (población) | Viveristas y técnicos de campo de proyectos de restauración |
| **Cuándo se valida** | Pilotaje de ocho semanas |

---

## 3. Constructor asistido de objetivos SMART

### 3.1 Objetivo general

> **Desarrollar** una aplicación móvil nativa Android con arquitectura *offline-first* que garantice el registro georreferenciado y la sincronización diferida del inventario forestal en viveros comunitarios de Antioquia, alcanzando una tasa de sincronización exitosa superior al 98 % de los registros capturados sin conectividad y un tiempo de consolidación de la tasa de supervivencia por lote inferior a 24 horas, verificada mediante pilotaje en dispositivos Android reales durante ocho semanas.

**Verificación del criterio SMART**

| Criterio | Elemento verificable |
|---|---|
| **S** — Específico | Aplicación nativa Android *offline-first* para inventario forestal georreferenciado |
| **M** — Medible | ≥ 98 % de sincronización exitosa; consolidación < 24 h |
| **A** — Alcanzable | Alcance MVP, stack maduro y gratuito, ejecución individual planificada en 4 sprints |
| **R** — Relevante | Responde a una necesidad documentada de los programas de restauración del departamento |
| **T** — Temporal | Pilotaje de ocho semanas |

**Nota sobre el verbo.** Se emplea un **único verbo rector** —*desarrollar*—, ubicado en el nivel de *creación* de la taxonomía de Bloom revisada, que es el nivel cognitivo más alto y el que corresponde a un proyecto de construcción de software. La instrucción del docente fue explícita: un solo verbo, nunca dos.

### 3.2 Objetivos específicos (estructura metodológica móvil de 3 pasos)

**Paso 1 — Investigación de usuario y prototipado UX/UI**

> **Diseñar** el prototipo interactivo de alta fidelidad en Figma para los flujos de registro de lote, siembra individual y monitoreo de supervivencia, validando un mínimo de ocho heurísticas de usabilidad de Nielsen con tres usuarios representativos del contexto de vivero, en la semana 3.

*Nivel Bloom: aplicar/crear. Métrica: 8 heurísticas verificadas con 3 usuarios. Plazo: semana 3.*

**Paso 2 — Desarrollo frontend móvil e integración API/Backend**

> **Implementar** los módulos de captura y persistencia local en Kotlin con Jetpack Compose y Room, junto con el motor de sincronización diferida basado en WorkManager contra la API REST del backend, alcanzando una cobertura de pruebas unitarias del 70 % sobre la capa de dominio, en la semana 6.

*Nivel Bloom: aplicar. Métrica: 70 % de cobertura en la capa de dominio. Plazo: semana 6.*

**Paso 3 — Pruebas en dispositivos reales y medición de rendimiento**

> **Evaluar** el rendimiento de la aplicación en al menos tres dispositivos Android físicos de gamas distintas, midiendo tiempo de arranque en frío, consumo de batería durante una jornada de captura de cuatro horas, tamaño final del APK y usabilidad mediante la escala SUS con una meta de puntaje superior a 75, en la semana 8.

*Nivel Bloom: evaluar. Métricas: SUS > 75, arranque en frío, consumo de batería, tamaño de APK. Plazo: semana 8.*

### 3.3 Matriz de coherencia

Esta matriz es el instrumento que demuestra la trazabilidad exigida por la rúbrica: cada objetivo responde a un componente del problema y se materializa en un elemento arquitectónico concreto.

| Componente del problema | Objetivo específico | Componente técnico que lo resuelve | Métrica de verificación |
|---|---|---|---|
| Formatos en papel ilegibles y de difícil diligenciamiento en campo | Paso 1 | UI en Jetpack Compose con formularios optimizados para uso a una mano y con guantes | SUS > 75 |
| Pérdida de registros por ausencia de conectividad | Paso 2 | Room como fuente única de verdad + WorkManager con reintentos | ≥ 98 % de sincronización |
| Imposibilidad de vincular monitoreos al mismo individuo | Paso 2 | UUID generado en cliente + coordenadas GPS por individuo | 100 % de monitoreos vinculados |
| Retraso en la consolidación de la tasa de supervivencia | Paso 2 | Cálculo del indicador en el dispositivo, sin dependencia del servidor | Consolidación < 24 h |
| Desconocimiento del comportamiento en equipos de gama baja | Paso 3 | Pruebas en tres gamas, medición de batería y APK | Informe de rendimiento |

---

## 4. Arquitectura y simulador de costos móviles (Stores + Backend)

### 4.1 Enfoque tecnológico seleccionado (stack)

**Enfoque:** Nativo Android — Kotlin + Jetpack Compose.

**Justificación técnica de la decisión**

La selección del enfoque nativo no responde a preferencia sino a tres restricciones del problema.

La primera es la **exigencia de persistencia y sincronización robustas**. El núcleo del proyecto es el comportamiento *offline-first*, y Room sobre SQLite junto con WorkManager constituyen la combinación con mayor madurez, mejor documentación oficial y garantías de ejecución diferida más sólidas del ecosistema móvil. WorkManager persiste el trabajo pendiente a través de reinicios del dispositivo, lo que en un escenario de jornada de campo prolongada es una garantía difícil de replicar con la misma fiabilidad desde una capa de abstracción multiplataforma.

La segunda es el **acceso a hardware en condiciones adversas**. El proyecto requiere GPS de alta precisión, cámara con control de compresión y gestión fina del consumo de batería. El acceso nativo elimina la capa intermedia de *plugins* y reduce tanto el riesgo de incompatibilidad como el tamaño final del artefacto, factor relevante para usuarios con dispositivos de gama baja y almacenamiento limitado. La implementación confirmó esta previsión: la lectura de posición usa `getCurrentLocation` con prioridad de alta precisión en lugar de la última posición conocida, y el procesamiento de imagen manipula el *bitmap* directamente para reescalar, corregir orientación y descartar metadatos.

La tercera es la **restricción de alcance del proyecto**. Al ejecutarse de forma individual, un enfoque multiplataforma obligaría a justificar y sostener un despliegue en iOS que exigiría la suscripción anual al Apple Developer Program y hardware macOS para compilación, sin que la población objetivo —técnicos de campo en zona rural de Antioquia, donde Android domina ampliamente el parque de dispositivos— lo demande. Concentrar el esfuerzo en una sola plataforma permite alcanzar mayor profundidad técnica en el aspecto que realmente diferencia la solución.

**Alternativas evaluadas y descartadas**

| Enfoque | Ventaja | Razón del descarte |
|---|---|---|
| Flutter | Base de código única para Android e iOS, buen rendimiento de UI | El *plugin* de trabajo en segundo plano es una abstracción sobre WorkManager; añade una capa de indirección justo en el componente más crítico del proyecto. La ventaja multiplataforma no se aprovecha con un solo desarrollador y sin población iOS |
| React Native | Reutilización de conocimiento en JavaScript, ecosistema amplio | El puente nativo añade latencia en operaciones intensivas sobre SQLite; la gestión de tareas en segundo plano depende de librerías de terceros con soporte desigual |
| Aplicación web progresiva (PWA) | Despliegue inmediato, sin tienda de aplicaciones | El acceso a GPS en segundo plano y la persistencia garantizada son limitados; contradice directamente el requisito *offline-first* de la solución |

### 4.2 Arquitectura de la solución

La aplicación implementa **Clean Architecture con patrón MVVM**, organizada en tres capas con dependencias dirigidas hacia el dominio.

```
┌──────────────────────────────────────────────────────┐
│  CAPA DE PRESENTACIÓN                                │
│  Jetpack Compose · ViewModel · StateFlow             │
│  Login · Lotes · Registro · Monitoreo · Sincronización│
└──────────────────────────────────────────────────────┘
                         │ observa estado
                         ▼
┌──────────────────────────────────────────────────────┐
│  CAPA DE DOMINIO  (Kotlin puro, sin Android)         │
│  Modelos · Casos de uso · Interfaces de repositorio  │
│  GuardarLote · RegistrarSiembra                      │
│  RegistrarMonitoreo · CalcularSupervivenciaLote      │
└──────────────────────────────────────────────────────┘
                         ▲ implementa
                         │
┌──────────────────────────────────────────────────────┐
│  CAPA DE DATOS                                       │
│  Room + SQLCipher (fuente única de verdad)           │
│  WorkManager → FuenteRemotaSiembras (interfaz)       │
│                     ↳ Firestore (implementación)     │
│  Firebase Auth · GestorFotografias · ProveedorGPS    │
└──────────────────────────────────────────────────────┘
```

Los cuatro casos de uso implementados concentran las reglas de negocio: `GuardarLote` valida nombre, área y coordenadas; `RegistrarSiembra` rechaza lecturas de GPS con precisión peor que 15 m; `RegistrarMonitoreo` impide que un individuo muerto registre medidas; y `CalcularSupervivenciaLote` deriva el indicador contando una sola observación por individuo.

El motor de sincronización depende de la interfaz `FuenteRemotaSiembras`, no de Firestore. Cambiar de proveedor implicaría escribir otra implementación sin tocar el dominio ni la interfaz de usuario.

La capa de dominio no depende de Android, lo que permite probarla con pruebas unitarias puras en la JVM sin emulador y sustenta la meta de cobertura del 70 % del objetivo específico 2.

**Estrategia offline-first**

El principio rector es que **la base de datos local es la única fuente de verdad**. La interfaz nunca consulta la red directamente: observa Room mediante `Flow`, de modo que la aplicación se comporta de forma idéntica con y sin conectividad. La red es un detalle de infraestructura que actúa por detrás.

Cada entidad sincronizable incorpora tres campos de control: un identificador **UUID generado en el cliente** —que elimina las colisiones de clave primaria al sincronizar registros creados por varios dispositivos sin coordinación previa—, un estado de sincronización (`PENDIENTE`, `SINCRONIZADO`, `CONFLICTO`) y una marca temporal de última modificación.

Cuando el usuario guarda un registro, la escritura ocurre en Room y la interfaz responde de inmediato. En paralelo se encola un trabajo en WorkManager con la restricción `NetworkType.CONNECTED` y política de reintento exponencial. Al recuperar conectividad, el sistema operativo ejecuta el trabajo, que envía por lotes los registros pendientes y actualiza su estado.

El envío respeta el **orden de dependencias del modelo**: primero los lotes, luego las siembras y por último los monitoreos. Enviar un monitoreo antes que el individuo que lo origina, o una siembra antes que su lote, dejaría registros huérfanos en el servidor.

La detección de conflictos se ejecuta dentro de una transacción de Firestore que compara la marca temporal local contra la remota. Si el servidor tiene una versión más reciente, el registro no se sobrescribe: se marca como `CONFLICTO` y se expone en la pantalla de sincronización para resolución manual. Es una decisión deliberada: en datos ambientales que sustentan verificación ante financiadores, descartar información en silencio es inaceptable.

**Modelo de datos**

```
Vivero 1──N Lote 1──N Siembra N──1 Especie
                          │
                          └──1──N Monitoreo
```

| Entidad | Descripción | Campos clave |
|---|---|---|
| `Vivero` | Unidad productora de material vegetal | id, nombre, municipio, responsable |
| `Lote` | Área georreferenciada de intervención | id, viveroId, polígono, área en hectáreas |
| `Especie` | Catálogo de especies nativas | id, nombre científico, nombre común, hábito |
| `Siembra` | Individuo sembrado, unidad mínima de trazabilidad | id (UUID), loteId, especieId, latitud, longitud, fecha, foto, estadoSync |
| `Monitoreo` | Visita de seguimiento a un individuo | id, siembraId, fecha, estadoVital, altura, diámetro, observaciones, estadoSync |

Las tres entidades sincronizables —`Lote`, `Siembra` y `Monitoreo`— llevan los mismos tres campos de control: identificador UUID generado en el cliente, estado de sincronización y marca temporal de última modificación.

### 4.3 Simulador de costos de infraestructura y servicios móviles

| Concepto / servicio móvil | Proveedor | Frecuencia / estimación | Costo USD | Subtotal USD |
|---|---|---|---|---|
| Google Play Console (cuenta de desarrollador) | Google | Pago único de por vida | 25,00 | 25,00 |
| Firebase Authentication | Google Cloud | Implementado. Plan Spark, gratuito en el volumen del pilotaje | 0,00 | 0,00 |
| Cloud Firestore | Google Cloud | Implementado. Plan Blaze, estimado mensual para el pilotaje | 15,00 | 15,00 |
| Firebase Cloud Storage (fotografías) | Google Cloud | Previsto. Estimado mensual con imágenes ya comprimidas a ~200 KB | 5,00 | 5,00 |
| Firebase Crashlytics | Google Cloud | Previsto. Sin costo | 0,00 | 0,00 |
| Repositorio y CI/CD | GitHub | GitHub Free + Actions, 2 000 min/mes | 0,00 | 0,00 |
| Despliegue de la landing page | Netlify | Plan Starter | 0,00 | 0,00 |
| Figma (diseño y prototipado) | Figma | Plan Education | 0,00 | 0,00 |
| Dispositivo Android de pruebas | — | Equipo propio | 0,00 | 0,00 |
| **Costo total estimado del proyecto móvil** | | | | **45,00** |

**Presupuesto asignado:** 300,00 USD
**Eficiencia presupuestal:** 85,0 % disponible

**Análisis del presupuesto.** El costo del MVP se concentra en un único pago no recurrente (Play Console) y en un costo mensual variable de backend estimado en 20 USD para el pilotaje. Los 255 USD de holgura cubren tres escenarios previstos: crecimiento del almacenamiento de fotografías si el volumen de siembras supera lo proyectado, contratación temporal de un servicio de mapas si se requiere cartografía base sin conexión, y adquisición de un segundo dispositivo de gama baja para las pruebas del objetivo 3. Se descarta deliberadamente el Apple Developer Program (99 USD anuales) por coherencia con el enfoque nativo Android justificado en 4.1.

**Efecto del procesamiento de imágenes sobre el costo.** El reescalado a 1600 px y la recompresión al 80 % reducen una fotografía típica de unos 4 MB a cerca de 200 KB, un factor cercano a veinte. Sobre una proyección de 5 000 individuos fotografiados, la diferencia entre almacenar los originales y las imágenes procesadas es del orden de 20 GB frente a 1 GB. La decisión de comprimir, tomada por razones de ancho de banda en zona rural, resulta ser también la que mantiene el costo de almacenamiento dentro del nivel gratuito.

> **Advertencia sobre la ampliación a producción.** El cálculo anterior corresponde a un pilotaje. Un despliegue departamental multiplicaría el costo de Firestore y Storage de forma no lineal con el número de fotografías. La ruta de mitigación documentada es migrar el almacenamiento de imágenes a un bucket de objetos con política de ciclo de vida y conservar Firestore únicamente para datos estructurados.

### 4.4 Estrategia de seguridad en el dispositivo

La aplicación gestiona datos de localización precisa de predios y ejecuciones que sustentan verificación ante entidades financiadoras. La estrategia se organiza en cinco frentes.

**Cifrado de la base de datos local.** Room se instancia sobre **SQLCipher** mediante `SupportFactory`, de modo que el archivo de base de datos permanece cifrado en reposo con AES-256. Sin esta medida, en un dispositivo con acceso root o extraído físicamente, el archivo `.db` sería legible en texto plano con cualquier visor de SQLite.

**Custodia de la clave de cifrado.** La clave no se codifica en el fuente ni se almacena en `SharedPreferences` planas. Se genera en el **Android Keystore**, respaldado por hardware seguro cuando el dispositivo lo provee, y la referencia se persiste mediante `EncryptedSharedPreferences` de la librería Jetpack Security.

**Autenticación y manejo de tokens.** La autenticación se delega en Firebase Authentication con correo y contraseña. El SDK gestiona los tokens JWT y su renovación, y persiste la credencial en el almacenamiento privado de la aplicación. Esta persistencia es la que permite conciliar autenticación con operación sin red: el técnico inicia sesión una vez con conectividad y luego trabaja días en campo conservando la autoría de sus registros. Cerrar sesión no borra los datos locales, porque un registro pendiente no puede desaparecer porque alguien salió de la cuenta.

**Reglas de seguridad del servidor.** La validación del cliente se puede eludir, así que se repite en el servidor. Las reglas publicadas en Cloud Firestore exigen sesión activa para toda operación, verifican que quien escribe sea el autor del registro, comprueban que las coordenadas estén dentro de rango válido, impiden que una actualización retroceda la marca temporal y prohíben el borrado desde la aplicación: un registro de campo es evidencia de ejecución, y su eliminación es una decisión administrativa que se toma desde la consola. Una regla de cierre deniega todo lo no declarado explícitamente. El archivo está versionado en `firebase/firestore.rules`.

**Seguridad del transporte.** Todo el tráfico usa HTTPS con TLS 1.2 o superior. Se aplica una **configuración de seguridad de red** (`network_security_config.xml`) que prohíbe el tráfico en texto plano en toda la aplicación, de modo que cualquier petición sin TLS falla en tiempo de ejecución en lugar de enviar datos legibles.

La **fijación de certificado** queda documentada y desactivada. Es una decisión consciente: un bloque `pin-set` con valores de ejemplo no protege nada y hace fallar toda conexión al dominio. El archivo incluye el procedimiento para obtener los pines reales y la advertencia de que se requiere un pin de respaldo, sin el cual la renovación del certificado dejaría la aplicación incomunicada.

**Permisos y minimización de datos.** Los permisos de ubicación y cámara se solicitan en tiempo de ejecución y en el momento de uso, no al abrir la aplicación, de modo que el usuario entiende para qué se piden. No se declara `ACCESS_BACKGROUND_LOCATION`: la aplicación no rastrea a nadie, solo toma lecturas puntuales durante la captura.

**Tratamiento de las fotografías.** Los archivos JPEG guardan metadatos EXIF que incluyen coordenadas GPS, modelo del equipo y número de serie. Como el proyecto ya almacena la coordenada de forma explícita y controlada, duplicarla oculta dentro de la imagen sería exponer información sin que el usuario lo sepa. El procesamiento recomprime desde el *bitmap*, lo que descarta todo el EXIF de origen. Antes de descartarlo se lee la etiqueta de orientación y se aplica la rotación a los píxeles: omitir ese paso dejaría todas las fotografías de lado. Las imágenes se guardan en el almacenamiento privado de la aplicación, no en almacenamiento compartido, y se exponen a la cámara mediante `FileProvider` con permiso temporal acotado a esa carpeta.

La aplicación declara `allowBackup="false"` y reglas de extracción de datos que excluyen la base de datos y las preferencias, para impedir que copias de seguridad automáticas trasladen información a almacenamiento no controlado.

---

## 5. Autoevaluación frente a la rúbrica

| Criterio | Ponderación | Evidencia en esta entrega |
|---|---|---|
| Análisis del problema y justificación tecnológica | 20 % | Sección 2 con contexto documentado y fuentes trazables; sección 4.1 con tres alternativas evaluadas y descartadas con argumento técnico |
| Arquitectura de la solución y buenas prácticas | 25 % | Clean Architecture + MVVM con dominio en Kotlin puro; diez registros de decisión arquitectónica; cifrado en reposo y en tránsito; reglas de seguridad del servidor versionadas; aplicación funcional de extremo a extremo |
| Calidad y estructura del repositorio GitHub | 20 % | README con diagramas Mermaid, estructura por capas, guía de despliegue, `.gitignore`, licencia y 46 archivos Kotlin documentados |
| Calidad del material de soporte | 15 % | Presentación ejecutiva en PDF y PPTX en `/presentacion`, con diagramas técnicos acoplados a la solución; landing page con demostración interactiva del comportamiento *offline* |
| Rigor técnico y potencial de investigación | 20 % | Trece pruebas unitarias sobre las reglas de negocio; métricas verificables; matriz de coherencia; instrumento de medición (SUS); limitaciones conocidas declaradas con su vía de solución |

### 5.1 Limitaciones conocidas

Declararlas es preferible a omitirlas: una limitación identificada con su vía de solución demuestra criterio; una limitación oculta que aparece en la sustentación resta credibilidad al conjunto.

| Limitación | Efecto | Vía de solución |
|---|---|---|
| Las fotografías no suben a Cloud Storage | Solo la ruta local viaja al servidor; la imagen permanece en el dispositivo | Añadir la subida en `FirestoreFuenteRemota` antes de escribir el documento y guardar la URL de descarga |
| SQLCipher 4.5.4 no alinea sus bibliotecas nativas a 16 KB | Advertencia en compilación; sin efecto en dispositivos actuales, relevante solo para publicar en Google Play | Actualizar SQLCipher a 4.6.1 o superior |
| Fijación de certificado desactivada | Sin protección adicional frente a intermediarios con autoridad certificadora comprometida | Obtener los pines del dominio y activar el bloque documentado en `network_security_config.xml` |
| Un usuario nuevo no puede darse de alta sin conectividad | El primer inicio de sesión requiere red | Documentado en el protocolo: el alta se realiza antes de la salida a campo |
| Resolución de conflictos manual, sin fusión asistida | El usuario decide, pero sin ver ambas versiones lado a lado | Pantalla de comparación en una versión posterior |
