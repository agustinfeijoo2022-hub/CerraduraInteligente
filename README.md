# Cerradura Inteligente IoT

Aplicación Android desarrollada en Kotlin para controlar y monitorear una cerradura mediante comunicación WiFi entre dos dispositivos Android.

El sistema utiliza un dispositivo como Cliente y otro como Servidor. El Cliente permite enviar comandos y consultar el estado de la cerradura, mientras que el Servidor recibe y procesa las solicitudes.

## Descripción

El proyecto corresponde a una solución IoT orientada al control remoto de una cerradura.

La comunicación entre los dispositivos se realiza mediante sockets TCP sobre una red WiFi. Para el acceso a la aplicación se utiliza Firebase Authentication y para proteger la comunicación se implementa cifrado AES-128 junto con un token de sesión.

El sistema permite realizar las siguientes acciones:

- Bloquear la cerradura.
- Desbloquear la cerradura.
- Consultar el estado de la cerradura.
- Mantener sincronizado el estado entre los dispositivos.

## Tecnologías utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- Firebase Authentication
- Navigation Compose
- WiFi
- TCP Sockets
- AES-128

## Funcionamiento

El sistema utiliza dos dispositivos Android:

### Cliente

El dispositivo Cliente es utilizado por el usuario para controlar la cerradura.

Desde el Cliente se puede:

- Iniciar sesión.
- Conectarse al Servidor mediante su dirección IP.
- Consultar el estado de la cerradura.
- Enviar comandos de bloqueo y desbloqueo.

### Servidor

El dispositivo Servidor recibe las solicitudes enviadas por el Cliente.

Sus principales funciones son:

- Esperar conexiones TCP.
- Recibir solicitudes.
- Validar el token de sesión.
- Procesar los comandos recibidos.
- Actualizar el estado de la cerradura.
- Responder al Cliente.

## Comunicación

Los dos dispositivos deben estar conectados a la misma red WiFi.

El Cliente necesita conocer la dirección IP del dispositivo que está funcionando como Servidor para establecer la conexión TCP.

El funcionamiento general es:

Cliente → WiFi → TCP → Servidor

La dirección IP puede cambiar dependiendo de la red utilizada, por lo que debe revisarse antes de realizar una prueba.

## Seguridad

El proyecto incorpora diferentes mecanismos para proteger el acceso y la comunicación.

### Firebase Authentication

Se utiliza Firebase Authentication para gestionar el registro e inicio de sesión de los usuarios.

### Token de sesión

Se utiliza un token de sesión para validar las comunicaciones entre Cliente y Servidor.

### Cifrado AES-128

Los comandos enviados entre los dispositivos son protegidos mediante cifrado AES-128 antes de ser transmitidos.

### Validación de comandos

El Servidor valida las solicitudes recibidas antes de procesarlas.

Los comandos utilizados son:

- `LOCK`
- `UNLOCK`
- `STATUS`

## Pantallas

La aplicación está organizada en las siguientes pantallas:

### ModoSeleccionScreen

Permite seleccionar si el dispositivo funcionará como Cliente o Servidor.

### LoginScreen

Permite iniciar sesión utilizando una cuenta registrada.

### RegisterScreen

Permite registrar un nuevo usuario mediante Firebase Authentication.

### MonitoreoScreen

Permite consultar el estado actual de la cerradura.

### ControlScreen

Permite enviar comandos para controlar la cerradura.

### ServidorScreen

Gestiona la recepción y procesamiento de las solicitudes provenientes del Cliente.

## Flujo de uso

1. Conectar ambos dispositivos a la misma red WiFi.
2. Abrir la aplicación en el primer dispositivo.
3. Seleccionar el modo Servidor.
4. Revisar la dirección IP del Servidor.
5. Abrir la aplicación en el segundo dispositivo.
6. Seleccionar el modo Cliente.
7. Ingresar la dirección IP del Servidor.
8. Iniciar sesión.
9. Entrar a la sección de monitoreo.
10. Acceder al panel de control.
11. Enviar los comandos `LOCK`, `UNLOCK` o `STATUS`.
12. Verificar la respuesta del Servidor.

## Requisitos

Para ejecutar el proyecto se necesita:

- Android Studio.
- Kotlin.
- Dos dispositivos Android o un dispositivo y un emulador.
- Ambos dispositivos conectados a la misma red WiFi.
- Proyecto configurado con Firebase Authentication.

## Ejecución

1. Clonar el repositorio.
2. Abrir el proyecto en Android Studio.
3. Esperar la sincronización de Gradle.
4. Configurar Firebase.
5. Ejecutar la aplicación en los dispositivos.
6. Configurar un dispositivo como Servidor.
7. Configurar el segundo dispositivo como Cliente.
8. Utilizar la dirección IP del Servidor para establecer la conexión.
9. Realizar las pruebas desde el panel de control.

## Estructura de la aplicación

```text
CerraduraInteligente
│
├── MainActivity
│
├── ModoSeleccionScreen
├── LoginScreen
├── RegisterScreen
├── MonitoreoScreen
├── ControlScreen
└── ServidorScreenara IoT
- **Año:** 2026
Autor

Agustín Feljoo

Asignatura: TI3042 — Aplicaciones Móviles para IoT

Año: 2026
