# Evidencias del proyecto

> Plantilla de registro. Complete cada sección con las capturas y mediciones reales antes de la entrega. Las tablas incluyen las métricas comprometidas en los objetivos específicos del documento maestro.

---

## 1. Prototipo UX/UI (objetivo específico 1)

| Pantalla | Captura | Enlace en Figma |
|---|---|---|
| Inicio de sesión | `diagramas/ux-00-login.png` | |
| Listado de lotes | `diagramas/ux-01-lotes.png` | |
| Formulario de lote | `diagramas/ux-02-lote-form.png` | |
| Registro de siembra | `diagramas/ux-03-registro.png` | |
| Monitoreo de individuo | `diagramas/ux-04-monitoreo.png` | |
| Estado de sincronización | `diagramas/ux-05-sync.png` | |

### Evaluación heurística de Nielsen

| # | Heurística | Cumple | Evidencia en la aplicación |
|---|---|---|---|
| 1 | Visibilidad del estado del sistema | | Contador de pendientes en la barra inferior; etiqueta de estado en cada registro |
| 2 | Correspondencia con el mundo real | | Vocabulario del dominio: lote, siembra, individuo, vivo/muerto/no hallado |
| 3 | Control y libertad del usuario | | Descartar y repetir la fotografía; cancelar en todos los formularios |
| 4 | Consistencia y estándares | | Los mismos tres colores de estado en todas las pantallas |
| 5 | Prevención de errores | | Guardado deshabilitado sin lote activo o con precisión de GPS insuficiente |
| 6 | Reconocer antes que recordar | | Especie preseleccionada entre registros consecutivos; lote activo con borde visible |
| 7 | Flexibilidad y eficiencia de uso | | Botón para usar la ubicación actual como centroide del lote |
| 8 | Diseño estético y minimalista | | Las medidas se ocultan si el individuo no está vivo |
| 9 | Ayuda a reconocer y recuperarse de errores | | Mensajes de Firebase traducidos a lenguaje accionable |
| 10 | Ayuda y documentación | | Texto explicativo del comportamiento sin red en registro y sincronización |

> Aplique la evaluación con tres usuarios del contexto real y anote los ajustes derivados.

---

## 2. Funcionamiento offline (objetivo específico 2)

Procedimiento central de la sustentación. Grábelo en video además de capturarlo.

| Paso | Acción | Resultado esperado | Captura |
|---|---|---|---|
| 1 | Iniciar sesión con conectividad | Sesión activa | |
| 2 | Activar modo avión | Sin conectividad | |
| 3 | Registrar tres o cuatro árboles con GPS y fotografía | Responde igual de rápido, sin errores | |
| 4 | Abrir la pantalla de sincronización | Los registros aparecen como pendientes | |
| 5 | **Cerrar la aplicación por completo y reabrirla** | Los registros persisten | |
| 6 | Registrar monitoreos de esos individuos | Se guardan también sin red | |
| 7 | Desactivar el modo avión | La cola se vacía sola, sin intervención | |
| 8 | Verificar en la consola de Firebase | Documentos en `lotes`, `siembras` y `monitoreos` | |

El paso 5 es el más contundente: demuestra que la persistencia es real y no un estado en memoria.

### Tasa de sincronización exitosa

| Registros capturados sin red | Sincronizados | Conflictos | Perdidos | Tasa |
|---|---|---|---|---|
| | | | | |

> Meta comprometida: superior al 98 %.

### Orden de sincronización

Verifique en la consola que los documentos de `lotes` tengan marca temporal anterior o igual a las `siembras` que los referencian. Es la evidencia de que el orden de dependencias de ADR-010 se respeta.

---

## 3. Cobertura de pruebas

```bash
./gradlew testDebugUnitTest
```

| Archivo de prueba | Pruebas | Reglas verificadas |
|---|---|---|
| `CalcularSupervivenciaLoteTest` | 6 | Una observación por individuo; exclusión de no hallados del denominador; umbral de cobertura confiable; división por cero |
| `RegistrarMonitoreoTest` | 7 | Coherencia entre estado vital y medidas; estado inicial pendiente; unicidad del identificador |
| **Total** | **13** | |

| Paquete | Líneas cubiertas | Meta |
|---|---|---|
| `domain.usecase` | | 70 % |
| `domain.model` | | 70 % |

---

## 4. Rendimiento en dispositivos reales (objetivo específico 3)

| Dispositivo | Gama | Android | Arranque en frío (ms) | Batería en 4 h (%) | Tamaño APK (MB) |
|---|---|---|---|---|---|
| | Alta | | | | |
| | Media | | | | |
| | Baja | | | | |

```bash
adb shell am start-activity -W -n co.edu.ucn.retono/.MainActivity
```

### Efecto del procesamiento de imágenes

| Métrica | Antes | Después |
|---|---|---|
| Tamaño medio por fotografía | | |
| Metadatos EXIF presentes | Sí | No |

Para verificar la eliminación del EXIF, extraiga una imagen del dispositivo y ejecute `exiftool` sobre ella. No deben aparecer etiquetas GPS ni identificadores del equipo.

---

## 5. Usabilidad — escala SUS

| Participante | Rol | Puntaje SUS |
|---|---|---|
| P1 | | |
| P2 | | |
| P3 | | |
| **Promedio** | | |

> Meta comprometida: superior a 75. Un puntaje de 68 corresponde al promedio de la industria; por encima de 80 se considera excelente.

---

## 6. Seguridad

| Verificación | Método | Resultado |
|---|---|---|
| La base de datos está cifrada | Extraer `retono.db` con `adb` e intentar abrirla con un visor SQLite | Debe ser ilegible |
| Las reglas de Firestore están publicadas | Consola → Firestore → Reglas | |
| Escritura sin sesión denegada | Simulador de reglas de Firebase | Debe denegar |
| Escritura suplantando a otro autor denegada | Simulador con `registradoPor` distinto del `uid` | Debe denegar |
| Tráfico en texto plano bloqueado | Configurar un dominio HTTP en `API_BASE_URL` | La conexión debe fallar |

---

## 7. Limitaciones conocidas

Declararlas es parte de la evidencia, no una omisión de ella.

| Limitación | Efecto | Vía de solución |
|---|---|---|
| Las fotografías no suben a Cloud Storage | Solo la ruta local viaja al servidor | Subida en `FirestoreFuenteRemota` antes de escribir el documento |
| SQLCipher 4.5.4 sin alineamiento a 16 KB | Advertencia en compilación; relevante solo para publicar en Play | Actualizar a SQLCipher 4.6.1 o superior |
| Fijación de certificado desactivada | Documentada con su procedimiento en `network_security_config.xml` | Obtener los pines del dominio real |
| Alta de usuario requiere conectividad | El primer inicio de sesión necesita red | Alta previa a la salida a campo |

---

## 8. Repositorio

| Elemento | Estado |
|---|---|
| Visibilidad pública verificada | ☐ |
| README con diagramas Mermaid renderizados | ☐ |
| Presentación en `/presentacion` | ☐ |
| Reglas de Firestore versionadas en `/firebase` | ☐ |
| Historial de commits por sprint | ☐ |
| Licencia declarada | ☐ |
| `google-services.json` **no** versionado | ☐ |
