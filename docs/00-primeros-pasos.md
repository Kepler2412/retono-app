# Primeros pasos: poner Retoño a funcionar

Guía para ejecutar la aplicación en su máquina. No requiere repositorio ni backend.

---

## Paso 1 · Instalar Android Studio

Descargue **Android Studio Ladybug (2024.2.1) o superior** desde `developer.android.com/studio`.

Durante la instalación, deje marcados todos los componentes por defecto. Android Studio trae su propio JDK 17, así que no necesita instalar Java aparte.

**Espacio libre necesario:** unos 12 GB entre el IDE, el SDK y el emulador.

---

## Paso 2 · Descomprimir el proyecto

Descomprima `retono-app.zip` en una ruta **sin espacios ni tildes**. Gradle falla de formas confusas con rutas como `C:\Users\Yeison Gómez\Mis documentos\`.

Rutas recomendadas:

- Windows: `C:\dev\retono-app`
- macOS o Linux: `~/dev/retono-app`

Verifique que dentro de la carpeta estén `settings.gradle.kts`, `build.gradle.kts` y la carpeta `app`. Si quedó una carpeta dentro de otra (`retono-app/retono-app/`), suba el contenido un nivel.

---

## Paso 3 · Abrir el proyecto

En Android Studio: **File → Open**, seleccione la carpeta `retono-app` y confirme.

No use *New Project* ni *Import*. Abra la carpeta directamente.

Al abrirlo aparecerá un aviso de confianza del proyecto: acepte con **Trust Project**.

### El problema del wrapper

El proyecto no incluye `gradle-wrapper.jar` porque es un archivo binario. Puede pasar una de dos cosas:

**Caso A — Android Studio lo genera solo.** Es lo más probable. La sincronización arranca, descarga Gradle 8.9 y sigue. No haga nada.

**Caso B — aparece el error** `Could not find or load main class org.gradle.wrapper.GradleWrapperMain`.

Solución:

1. **File → Settings** (en macOS: *Android Studio → Settings*).
2. Vaya a **Build, Execution, Deployment → Build Tools → Gradle**.
3. En **Use Gradle from**, seleccione **'wrapper' task in Gradle build script**.
4. **Gradle JDK**: escoja el JDK 17 embebido (aparece como *jbr-17* o *Embedded JDK*).
5. Aplique y ejecute **File → Sync Project with Gradle Files**.

Si aún falla, el camino infalible es: cree un proyecto nuevo vacío en Android Studio (*Empty Activity*), copie de él la carpeta `gradle/wrapper/` completa, y péguela sobre la del proyecto Retoño.

---

## Paso 4 · Esperar la primera sincronización

La primera vez Gradle descarga todas las dependencias. **Tarda entre 5 y 15 minutos** según su conexión. La barra de progreso está abajo a la derecha.

Necesita conexión a internet en este paso. Es la única parte del proceso que la requiere.

Si el SDK Manager pide instalar la **API 34**, acepte.

---

## Paso 5 · Crear el emulador

La aplicación usa GPS a través de Google Play Services, así que **el emulador debe tener imagen de Google Play o Google APIs**. Un emulador con imagen "AOSP" no sirve: la lectura de ubicación fallará siempre.

1. **Tools → Device Manager → Create Virtual Device**.
2. Modelo: **Pixel 6**.
3. System Image: **API 34 (Android 14)**, y en la columna *Target* debe decir **Google Play** o **Google APIs**. Si aparece con una flecha de descarga, descárguela.
4. Nombre: `Pixel6_API34`. Finalice.

---

## Paso 6 · Ejecutar

Seleccione el emulador en la barra superior y presione el botón verde **Run** (o `Shift + F10`).

La primera compilación tarda unos minutos. Debería ver la aplicación con la barra inferior de tres pestañas: **Lotes**, **Registrar** y **Sincronizar**.

---

## Paso 7 · Darle ubicación al emulador

Un emulador recién creado no tiene posición GPS. Si intenta registrar sin configurarla, verá el mensaje *"No se obtuvo señal de GPS"*, y es correcto: la app está funcionando bien, el emulador es el que no tiene señal.

1. En la barra lateral del emulador, haga clic en los tres puntos (**Extended Controls**).
2. Pestaña **Location**.
3. Ingrese las coordenadas del vivero de demostración:
   - Latitude: `6.1892`
   - Longitude: `-75.2153`
4. Presione **Set Location**.

Deje esa ventana abierta: la usará de nuevo más adelante.

---

## Paso 8 · Probar el flujo completo

1. **Cree una cuenta** en la pantalla de inicio de sesión. Requiere internet solo esta vez.
2. En **Lotes** verá el lote de demostración precargado, marcado como activo con borde verde. Puede editarlo con el lápiz o crear otro con el botón **+**.
3. Vaya a **Registrar**. Escoja una especie: hay diez nativas de Antioquia precargadas.
4. Toque **Tomar ubicación**. Android pedirá el permiso: concédalo. Debe aparecer la coordenada con su precisión.
5. Opcionalmente tome una fotografía. En el emulador, la cámara virtual muestra una escena 3D; funciona igual.
6. Registre el árbol. El botón se habilita solo cuando hay especie, lote activo y precisión suficiente.
7. En **Monitorear**, toque el árbol y registre una visita. Si lo marca como vivo puede anotar altura y diámetro.
8. Vuelva a **Lotes**: ahí aparece la tasa de supervivencia calculada localmente.
9. En **Sincronizar** verá el estado de cada registro y podrá forzar un envío.

Verifique en la consola de Firebase que aparecen las colecciones `lotes`, `siembras` y `monitoreos`.

---

## Paso 9 · Demostrar el comportamiento offline

Esta es la prueba que debe grabar como evidencia para la entrega.

| Paso | Acción | Qué debe ocurrir |
|---|---|---|
| 1 | Active el modo avión en el emulador | Sin conectividad |
| 2 | Registre tres o cuatro árboles | La app responde igual de rápido, sin errores |
| 3 | Abra **Sincronizar** | Los registros aparecen como pendientes |
| 4 | Cierre la app por completo (deslizar en recientes) | — |
| 5 | Vuelva a abrirla | **Los registros siguen ahí** |
| 6 | Desactive el modo avión | `SyncWorker` intenta subir y reintenta porque no hay servidor |

El paso 5 es el más importante de la sustentación: demuestra que la persistencia local es real y no un estado en memoria.

---

## Paso 10 · Ejecutar las pruebas

Las pruebas de la lógica de supervivencia corren sin emulador, en la JVM:

```bash
./gradlew test
```

En Windows: `gradlew.bat test`.

También puede hacer clic derecho sobre `CalcularSupervivenciaLoteTest.kt` y elegir **Run**. Deben pasar las seis.

---

## Errores frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Could not find or load main class GradleWrapperMain` | Falta el wrapper. Vea el Caso B del Paso 3. |
| `Unsupported class file major version` | Gradle está usando un JDK distinto de 17. Cámbielo en *Settings → Gradle → Gradle JDK*. |
| `SDK location not found` | Falta `local.properties`. Android Studio lo crea al sincronizar; si no, créelo en la raíz con `sdk.dir=RUTA_A_SU_SDK`. |
| `Failed to resolve: androidx...` | Sin internet durante la sincronización, o un proxy bloqueando. Reintente con *Sync Project*. |
| La app instala pero se cierra al abrir | Revise el **Logcat** filtrando por `co.edu.ucn.retono` y comparta el stack trace. |
| El registro de usuario falla | Falta habilitar Correo/contraseña en **Authentication → Sign-in method**. |
| Permiso denegado al sincronizar | Las reglas de Firestore no están publicadas. Vea `firebase/firestore.rules`. |
| `No matching client found for package name` | El `applicationId` no coincide con el `package_name` del `google-services.json`. No agregue sufijos a la variante de depuración. |
| Advertencia de alineamiento a 16 KB | Proviene de SQLCipher 4.5.4. No afecta la ejecución; solo importaría al publicar en Play. |
| "No se obtuvo señal de GPS" siempre | El emulador no tiene ubicación (Paso 7) o la imagen no es de Google Play (Paso 5). |
| El emulador va lentísimo | Active la virtualización por hardware en la BIOS (Intel VT-x o AMD-V). |

---

## Repositorio

El proyecto está publicado en https://github.com/Kepler2412/retono-app con visibilidad pública.

Para clonarlo:

```bash
git clone https://github.com/Kepler2412/retono-app.git
cd retono-app
```

Recuerde que tras clonar necesita crear su propio `local.properties` (Android Studio lo genera al sincronizar) y, si desea sincronización remota, su propio `google-services.json`.

### Trabajar sobre el repositorio

```bash
git add .
git commit -m "Descripcion del cambio"
git push
```

El `.gitignore` excluye `local.properties`, las carpetas `build/` y los archivos de firma. Antes de cada `push` conviene revisar `git status --short` para confirmar que no se cuela nada que no deba versionarse.
