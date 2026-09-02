# Especificación de diseño UX/UI

Sistema de diseño y maquetas de las seis pantallas de Retoño.

**Archivos de este documento**

| Archivo | Contenido |
|---|---|
| `mockups/mockups.html` | Las seis pantallas renderizadas. Ábralo en un navegador |
| `mockups/ux-00-login.png` … `ux-05-sync.png` | Cada pantalla exportada por separado |

> **Nota de honestidad metodológica.** Esta especificación documenta el diseño de una aplicación que ya está implementada. No se presenta como un prototipo previo al desarrollo, porque no lo fue. Su función es dejar registrado el sistema de diseño, servir de base para reconstruir el prototipo interactivo en Figma, y respaldar la evaluación heurística del objetivo específico 1.

---

## 1. Sistema de diseño

### 1.1 Paleta

| Rol | Token | Valor | Uso |
|---|---|---|---|
| Primario | `Foliage` | `#2F6B3F` | Botones principales, indicadores de éxito, lote activo |
| Acento | `Moss` | `#7FB069` | Estado sincronizado, acentos sobre fondo oscuro |
| Fondo | `Paper` | `#EDF0E8` | Fondo de pantalla |
| Superficie | `PaperDim` | `#DDE2D6` | Tarjetas y contenedores |
| Texto | `Ink` | `#0E1811` | Texto principal |
| Texto secundario | `TextMute` | `#4C5A50` | Descripciones y metadatos |
| Estado pendiente | `Ochre` | `#C9A227` | Registros sin sincronizar, advertencias |
| Estado conflicto | `Clay` | `#B4553A` | Conflictos, errores, individuos no hallados |

**Regla de color.** Los tres estados de sincronización tienen color fijo y consistente en toda la aplicación. No se usa color dinámico de Material You: que el ocre de "pendiente" cambiara según el fondo de pantalla del técnico sería un defecto, no una función.

**Accesibilidad.** El color nunca comunica solo. Cada etiqueta de estado combina punto de color, texto y posición. Un usuario con daltonismo distingue igual entre pendiente, sincronizado y en conflicto.

### 1.2 Tipografía

| Estilo | Tamaño | Peso | Uso |
|---|---|---|---|
| `headlineMedium` | 26 sp | Bold | Título de pantalla |
| `titleLarge` | 20 sp | SemiBold | Cifra de supervivencia |
| `titleMedium` | 17 sp | SemiBold | Título de tarjeta, encabezado de sección |
| `bodyLarge` | 16 sp | Regular | Texto de formulario |
| `bodyMedium` | 14 sp | Regular | Descripciones |
| `labelSmall` | 11 sp | Medium, monoespaciada | Identificadores, coordenadas, estados |

**Justificación del tamaño mínimo.** Los cuerpos usan 16 sp y los controles principales 18 sp. La aplicación se opera al aire libre, con luz directa y a veces con guantes. Reducir el tamaño por estética costaría legibilidad en el contexto real.

### 1.3 Componentes

| Componente | Especificación |
|---|---|
| Tarjeta | Radio 12 dp, relleno 16 dp, fondo `PaperDim` |
| Tarjeta activa | Igual, más borde de 2 dp en `Foliage` |
| Botón primario | Radio 9 dp, alto 48 dp, fondo `Foliage`, texto `Paper` |
| Botón secundario | Contorno de 1,4 dp en `Foliage`, fondo transparente |
| Campo de texto | Contorno, radio 9 dp, etiqueta flotante en `Foliage` |
| Chip de selección | Radio completo, 32 dp de alto; seleccionado invierte a `Foliage` |
| Etiqueta de estado | Radio completo, punto de 8 dp más texto monoespaciado de 9 sp |
| Barra inferior | 4 destinos, 56 dp de alto, contador sobre el icono de sincronización |
| Botón flotante | 50 × 50 dp, radio 16 dp, esquina inferior derecha |

### 1.4 Rejilla y espaciado

Escala de 4 dp. Márgenes laterales de 16 dp. Separación entre tarjetas de 12 dp, entre secciones de un formulario 16 dp.

---

## 2. Pantallas

| Código | Pantalla | Propósito |
|---|---|---|
| UX-00 | Inicio de sesión | Autenticar. Advierte que solo el primer acceso requiere red |
| UX-01 | Listado de lotes | Elegir el lote activo y consultar la supervivencia por lote |
| UX-02 | Crear o editar lote | Alta y corrección de lotes, con captura del centroide por GPS |
| UX-03 | Registro de siembra | Capturar un individuo: especie, coordenada, fotografía |
| UX-04 | Monitoreo | Registrar la visita de seguimiento de un individuo |
| UX-05 | Sincronización | Ver qué información no ha llegado al servidor |

### Flujo principal

```
Login ──▶ Lotes ──┬──▶ [+] Crear lote ──▶ vuelve a Lotes con el nuevo activo
                  │
                  ├──▶ Registrar ──▶ especie ──▶ GPS ──▶ foto ──▶ guardar
                  │
                  ├──▶ Monitorear ──▶ elegir individuo ──▶ estado ──▶ guardar
                  │
                  └──▶ Sincronizar ──▶ ver pendientes y conflictos
```

El lote activo es estado compartido: se elige en UX-01 y condiciona a UX-03 y UX-04. Se persiste entre sesiones, porque una jornada de campo transcurre entera en el mismo lote.

---

## 3. Decisiones de interacción

Estas son las que conviene poder defender en la sustentación, porque ninguna es arbitraria.

**El lote activo se marca con borde, no solo con color de fondo.** Debe ser evidente de un vistazo dónde se van a guardar los registros. Equivocarse de lote produce datos que parecen correctos y no lo son.

**La especie permanece seleccionada entre registros consecutivos.** En una jornada real se siembran varios individuos de la misma especie seguidos. Reelegirla cada vez sería trabajo repetido sin motivo.

**Las medidas desaparecen si el individuo no está vivo.** No se deshabilitan: se ocultan. Deshabilitarlas dejaría al usuario preguntándose por qué no puede escribir; ocultarlas comunica que ese dato no aplica.

**El botón de guardar se deshabilita, en vez de mostrar un error después.** Sin lote activo, sin especie o con precisión de GPS peor que 15 m, el guardado no está disponible. En campo, rehacer un registro cuesta caminar de vuelta: prevenir el error es más barato que reportarlo.

**El contador de pendientes vive en la barra inferior.** El técnico debe poder ver cuánto trabajo suyo falta por subir sin entrar a mirar. Ocultarlo produciría la peor falla posible en este dominio: creer que la jornada está a salvo cuando no lo está.

**Los permisos se piden en el momento de uso.** Al tocar "Tomar ubicación", no al abrir la aplicación. Así el usuario entiende para qué se solicitan.

---

## 4. Evaluación heurística de Nielsen

| # | Heurística | Cómo la atiende el diseño |
|---|---|---|
| 1 | Visibilidad del estado del sistema | Contador de pendientes en la barra; etiqueta de estado en cada registro; indicador de precisión del GPS |
| 2 | Correspondencia con el mundo real | Vocabulario del dominio: lote, siembra, individuo, vivo / muerto / no hallado |
| 3 | Control y libertad | "Quitar y repetir" en la fotografía; "Repetir lectura" en el GPS; cancelar en todos los diálogos |
| 4 | Consistencia y estándares | Los tres colores de estado son iguales en todas las pantallas; Material 3 como base |
| 5 | Prevención de errores | Guardado deshabilitado sin lote, sin especie o con precisión insuficiente; medidas ocultas si el individuo no está vivo |
| 6 | Reconocer antes que recordar | Especie preseleccionada; lote activo con borde; catálogo de especies visible, no escrito a mano |
| 7 | Flexibilidad y eficiencia | "Usar mi ubicación" rellena el centroide; sincronización manual disponible además de la automática |
| 8 | Diseño estético y minimalista | Formularios sin campos irrelevantes; texto explicativo solo donde el comportamiento no es obvio |
| 9 | Reconocer y recuperarse de errores | Mensajes de Firebase traducidos a lenguaje accionable, distinguiendo el fallo de red por ser el más probable |
| 10 | Ayuda y documentación | Nota sobre el comportamiento sin red en registro, sincronización e inicio de sesión |

> **Pendiente.** La validación con tres usuarios del contexto real, comprometida en el objetivo específico 1, aún no se ha ejecutado. Sus resultados van en `04-evidencias.md`.

---

## 5. Reconstruir el prototipo en Figma

Con esta especificación, rehacerlo toma alrededor de una hora.

1. **Marco.** Cree un archivo nuevo y añada seis frames de **360 × 800** (Android Compact).
2. **Estilos de color.** Cree los ocho estilos de la sección 1.1 con sus nombres de token. Hacerlo primero evita repintar después.
3. **Estilos de texto.** Cree los seis estilos de la sección 1.2. Use Roboto, la tipografía del sistema Android.
4. **Componentes.** Construya tarjeta, botón primario, botón secundario, campo de texto, chip y etiqueta de estado. Conviértalos en componentes con `Ctrl+Alt+K` y aplique variantes para los estados.
5. **Pantallas.** Reproduzca cada una tomando `mockups/mockups.html` como referencia visual. Ábralo en el navegador junto a Figma.
6. **Prototipo.** En la pestaña *Prototype*, conecte según el flujo de la sección 2. La barra inferior debe navegar entre las cuatro pantallas principales; el botón `+` abre el formulario de lote; tocar un individuo abre el diálogo de monitoreo.
7. **Compartir.** *Share → Anyone with the link → can view*. Pegue la URL en `01-formulario-entrega1.md`, sección 1.5.

**Atajo si va con poco tiempo:** importe los PNG de `mockups/` como fondo de cada frame y coloque encima zonas transparentes con los enlaces de navegación. Obtiene un prototipo navegable en unos veinte minutos, aunque sin componentes editables.
