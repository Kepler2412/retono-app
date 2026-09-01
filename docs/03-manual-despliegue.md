# Manual de instalación y despliegue

## 1. Requisitos

| Componente | Versión mínima | Verificación |
|---|---|---|
| Android Studio | Ladybug 2024.2.1 | `Help → About` |
| JDK | 17 | `java -version` |
| Android SDK | API 34 | SDK Manager |
| Git | 2.30 | `git --version` |
| Dispositivo o emulador | Android 8.0 (API 26) | Device Manager |

Espacio en disco recomendado: 10 GB entre el SDK, el emulador y la caché de Gradle.

## 2. Obtener el proyecto

```bash
git clone https://github.com/<usuario>/retono-app.git
cd retono-app
```

Abra la carpeta en Android Studio con `File → Open` y espere a que termine la sincronización de Gradle. La primera vez descarga las dependencias y puede tardar varios minutos.

## 3. Configurar el entorno local

Cree el archivo `local.properties` en la raíz del proyecto. Este archivo está excluido en `.gitignore` y no debe subirse al repositorio.

```properties
sdk.dir=/ruta/a/su/Android/Sdk
API_BASE_URL="https://api.retono.example.com/api/v1/"
```

## 4. Configurar Firebase

La aplicación compila con o sin credenciales. Sin `app/google-services.json` corre en modo local y los registros quedan en `PENDIENTE`; con él, se activa la sincronización.

### 4.1 Crear el proyecto

1. Entre a la consola de Firebase y cree un proyecto.
2. **Agregar app → Android.**
3. Nombre del paquete: **`co.edu.ucn.retono`**, exactamente así.
4. Descargue `google-services.json` y colóquelo en la carpeta `app/`.

> El `applicationId` de depuración no lleva sufijo. Si lo agrega, la compilación fallará con *"No matching client found for package name"*.

### 4.2 Habilitar autenticación

**Authentication → Get started → Sign-in method → Correo electrónico/contraseña → Habilitar.**

Sin este paso, el registro de usuarios falla con un error de operación no permitida.

### 4.3 Crear la base de datos

**Firestore Database → Crear base de datos.** Escoja **modo de producción** y la región `nam5` o `southamerica-east1`.

No use el modo de prueba: deja la base abierta a internet durante 30 días y luego cierra todo de golpe, rompiendo la app sin aviso.

### 4.4 Publicar las reglas de seguridad

**Firestore Database → Reglas.** Reemplace el contenido por el de `firebase/firestore.rules` del repositorio y publique.

Las reglas exigen sesión activa, verifican autoría, validan que las coordenadas estén en rango y prohíben el borrado desde la aplicación.

### 4.5 Verificar

Compile y ejecute. Debe aparecer la pantalla de inicio de sesión. Cree una cuenta, registre un árbol y compruebe en la consola de Firebase que el documento aparece en la colección `siembras`.

## 5. Crear el emulador

`Tools → Device Manager → Create Device`. Seleccione **Pixel 6**, imagen de sistema **API 34** con Google Play y 2 GB de RAM asignada.

Si el asistente de Android Studio falla al instalar el emulador porque ya existía una instalación previa, elimine `~/AppData/Local/Android/Sdk/emulator` en Windows o `~/Android/Sdk/emulator` en Linux y macOS, y reinstálelo desde el SDK Manager.

## 6. Compilar y ejecutar

```bash
./gradlew assembleDebug          # Compila la variante de depuración
./gradlew installDebug           # Instala en el dispositivo conectado
./gradlew test                   # Pruebas unitarias de dominio (sin emulador)
./gradlew connectedAndroidTest   # Pruebas instrumentadas (requiere dispositivo)
```

En Windows use `gradlew.bat` en lugar de `./gradlew`.

## 7. Verificar el comportamiento offline

Este es el procedimiento que valida el núcleo del proyecto y el que debe grabarse como evidencia.

| Paso | Acción | Resultado esperado |
|---|---|---|
| 1 | Instalar e iniciar sesión con conectividad | Sesión activa |
| 2 | Activar modo avión | Sin conexión |
| 3 | Registrar un lote y tres siembras | La aplicación responde de inmediato, sin errores ni pantallas de carga |
| 4 | Abrir la pantalla de sincronización | Los tres registros aparecen como pendientes |
| 5 | Reiniciar el dispositivo con el modo avión activo | Los registros persisten tras el reinicio |
| 6 | Desactivar el modo avión | WorkManager sincroniza sin intervención; el contador de pendientes llega a cero |

El paso 5 es el que demuestra la garantía real de WorkManager y conviene destacarlo en la sustentación.

## 8. Generar el APK de publicación

```bash
./gradlew assembleRelease
```

Antes necesita una clave de firma:

```bash
keytool -genkey -v -keystore retono-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias retono
```

Configure la firma en `app/build.gradle.kts` leyendo las credenciales desde `local.properties`. **Nunca escriba la contraseña del almacén de claves en un archivo versionado.**

## 9. Publicar en Google Play

1. Cree la cuenta de desarrollador en Google Play Console (pago único de 25 USD).
2. Genere un Android App Bundle: `./gradlew bundleRelease`.
3. Suba `app/build/outputs/bundle/release/app-release.aab`.
4. Complete la ficha de la tienda, la declaración de seguridad de datos y la política de privacidad.
5. Publique primero en un canal de pruebas internas antes de producción.

## 10. Desplegar la landing page

```bash
cd landing
# Con Netlify CLI
netlify deploy --prod --dir=.
```

También puede arrastrar la carpeta `landing/` en el panel de Netlify o conectar el repositorio para despliegue continuo indicando `landing` como directorio de publicación.
