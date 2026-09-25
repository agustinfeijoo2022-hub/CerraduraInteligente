# 🔐 Cerradura Inteligente IoT

Aplicación Android para el control y monitoreo remoto de una cerradura inteligente mediante comunicación WiFi entre dos dispositivos.

## 📋 Descripción

Este proyecto implementa un sistema IoT que permite controlar una cerradura inteligente desde un dispositivo Android (cliente), mientras otro dispositivo Android (servidor) actúa como receptor de comandos.

La comunicación se realiza mediante **sockets TCP sobre WiFi**, con **cifrado AES-128** y **token de sesión** para garantizar la seguridad según la norma **ISO 27400**.

## 🛠️ Tecnologías utilizadas

- **Kotlin** — Lenguaje de programación
- **Jetpack Compose** — UI moderna declarativa
- **Android Studio** — IDE
- **Firebase Authentication** — Autenticación
- **Sockets TCP** — Comunicación WiFi
- **Cifrado AES-128** — Seguridad
- **Navigation Compose** — Navegación

## 🏗️ Arquitectura
## 🔒 Seguridad ISO 27400

1. Autenticación de usuarios con Firebase Auth
2. Token de sesión compartido entre cliente y servidor
3. Cifrado AES-128 de comandos
4. Validación de comandos (UNLOCK, LOCK, STATUS)
5. Control de acceso por token

## 📱 Funcionalidades

- ModoSeleccionScreen — Cliente o Servidor
- LoginScreen — Login con Firebase
- RegisterScreen — Registro de usuarios
- MonitoreoScreen — Estado de la cerradura
- ControlScreen — Panel de control
- ServidorScreen — Receptor de comandos

## 🚀 Cómo probar

1. Tablet: modo Servidor → anotar IP
2. Teléfono: modo Cliente → IP de tablet → login
3. Control → botones desbloquear/bloquear

## 👤 Autor

- **Nombre:** Agustín Feljoo
- **Asignatura:** TI3042 — Aplicaciones Móviles para IoT
- **Año:** 2026
