# Cerradura Inteligente IoT

Aplicación Android desarrollada en Kotlin para controlar y monitorear
una cerradura mediante comunicación WiFi entre dos dispositivos
Android, con seguridad basada en el estándar ISO 27400.

---

## Descripción

El proyecto corresponde a una solución IoT orientada al control
remoto de una cerradura. La comunicación entre dispositivos se
realiza mediante sockets TCP sobre red WiFi. El acceso a la
aplicación utiliza Firebase Authentication, el historial de accesos
se almacena en Firebase Firestore, y la comunicación se protege
con cifrado AES-128/GCM más un token de autenticación.

El sistema permite:

- Bloquear la cerradura (`LOCK`).
- Desbloquear la cerradura (`UNLOCK`).
- Consultar el estado de la cerradura (`STATUS`).
- Mantener sincronizado el estado entre los dispositivos.
- Registrar el historial de accesos en Firestore.

---

## Tecnologías utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- Firebase Authentication
- Firebase Firestore
- Navigation Compose
- WiFi
- TCP Sockets
- AES-128/GCM

---

## Funcionamiento

El sistema utiliza dos dispositivos Android:

### Cliente

- Iniciar sesión con Firebase Authentication.
- Conectarse al Servidor mediante su dirección IP.
- Consultar el estado de la cerradura.
- Enviar comandos de bloqueo y desbloqueo.

### Servidor

- Esperar conexiones TCP en el puerto 8080.
- Recibir solicitudes cifradas.
- Validar el token de autenticación.
- Validar el comando contra la lista blanca.
- Procesar los comandos recibidos.
- Actualizar el estado de la cerradura.
- Responder al Cliente.
- Registrar cada operación en Firestore.

---

## Comunicación

Los dos dispositivos deben estar conectados a la misma red WiFi.
El Cliente necesita conocer la dirección IP del dispositivo que
está funcionando como Servidor para establecer la conexión TCP.

Esquema general:
Cliente → [AES-128/GCM] → WiFi/TCP → Servidor
Servidor → [AES-128/GCM] → WiFi/TCP → Cliente


La dirección IP puede cambiar dependiendo de la red utilizada,
por lo que debe revisarse antes de cada prueba.

---

## Seguridad (ISO 27400)

El proyecto implementa defensa en profundidad con cuatro capas:

### 1. Autenticación con Firebase

Registro e inicio de sesión mediante Firebase Authentication.
Sin credenciales válidas, no hay acceso a la aplicación.

### 2. Token de autenticación

Cada mensaje incluye un token compartido que el Servidor valida
antes de ejecutar cualquier acción. Formato:

TOKEN:COMANDO
Ejemplo: CERRA-2026-IOT-ABC123:UNLOCK


### 3. Lista blanca de comandos

Solo se aceptan los comandos `LOCK`, `UNLOCK`, `STATUS`, `INFO`
y `AUTH`. Cualquier otro es rechazado con `ERROR:COMANDO_DESCONOCIDO`.

### 4. Cifrado AES-128/GCM

- Modo GCM (Galois/Counter Mode): cifrado autenticado.
- IV único de 12 bytes generado con `SecureRandom` por mensaje.
- Tag de autenticación de 128 bits que detecta alteraciones.
- Si el cifrado o descifrado falla, el mensaje se rechaza y NO
  se envía sin protección.

### Historial de accesos en Firestore

### Historial de accesos en Firestore

Cada login, selección de modo y comando ejecutado se registra en
la colección `historial_accesos` con los campos:

-"uid": "abc123...",

-"email": "usuario@ejemplo.com",

-"accion": "UNLOCK",

-"rol": "CLIENTE",

-"resultado": "OK",

-"detalle": "Respuesta: OK:DESBLOQUEADO | IP: 192.168.0.103",

-"fecha": "2026-10-05 21:15:32",

-"timestamp": 1790626883722


Pantallas
ModoSeleccionScreen
Permite seleccionar si el dispositivo funcionará como Cliente
o Servidor.

LoginScreen
Permite iniciar sesión con una cuenta registrada.

RegisterScreen
Permite registrar un nuevo usuario mediante Firebase Authentication.

MonitoreoScreen
Muestra el estado actual de la cerradura.

ControlScreen
Permite enviar comandos para controlar la cerradura.

ServidorScreen
Gestiona la recepción y procesamiento de las solicitudes
provenientes del Cliente.

Flujo de uso
Conectar ambos dispositivos a la misma red WiFi.

Abrir la aplicación en el primer dispositivo.

Seleccionar el modo Servidor.

Revisar la dirección IP del Servidor.

Abrir la aplicación en el segundo dispositivo.

Seleccionar el modo Cliente.

Ingresar la dirección IP del Servidor.

Iniciar sesión.

Entrar a la sección de monitoreo.

Acceder al panel de control.

Enviar los comandos LOCK, UNLOCK o STATUS.

Verificar la respuesta del Servidor.

Requisitos
Android Studio (versión reciente).

Kotlin.

Dos dispositivos Android o uno + emulador.

Ambos dispositivos conectados a la misma red WiFi.

Proyecto de Firebase configurado con Authentication y Firestore.

Ejecución
Clonar el repositorio:
git clone https://github.com/agustinfeljoo/CerraduraInteligente.git
Abrir el proyecto en Android Studio.

Esperar la sincronización de Gradle.

Verificar que google-services.json esté en la carpeta app/.

Ejecutar la aplicación en los dispositivos.

Configurar un dispositivo como Servidor.

Configurar el segundo dispositivo como Cliente.

Utilizar la dirección IP del Servidor para establecer la conexión.

Realizar las pruebas desde el panel de control.
Estructura del proyecto
CerraduraInteligente/
├── app/
│   ├── google-services.json
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/cerradura/iot/
│       │   ├── MainActivity.kt
│       │   ├── ModoSeleccionScreen.kt
│       │   ├── LoginScreen.kt
│       │   ├── RegisterScreen.kt
│       │   ├── MonitoreoScreen.kt
│       │   ├── ControlScreen.kt
│       │   ├── ServidorScreen.kt
│       │   ├── Cifrado.kt
│       │   ├── Seguridad.kt
│       │   ├── WifiClient.kt
│       │   ├── WifiServer.kt
│       │   ├── ArduinoSimulator.kt
│       │   └── HistorialDB.kt
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md

Autor
Agustín Feljoo

Asignatura: TI3042 — Aplicaciones Móviles para IoT

Año: 2026
