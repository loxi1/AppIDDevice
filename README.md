# 📱 AppGenerateMac

**AppGenerateMac** es una aplicación Android desarrollada en Kotlin con Jetpack Compose que:

- Muestra una lista de **máquinas** obtenidas desde un servidor por medio de una API REST.
- Permite **seleccionar una máquina** y guardarla junto al identificador del dispositivo (`ANDROID_ID`) en un servidor remoto.
- Ejecuta un **servidor HTTP embebido (NanoHTTPD)** que responde en el puerto `8080` con el `deviceId` e IP del cliente.
- Soporta **reinicio automático al encender el dispositivo**, manteniendo el servidor activo.
- Solicita al usuario **excluir la app de optimización de batería** para permitir su ejecución en segundo plano.

---

## 🚀 Funcionalidades

- ✅ Dropdown con buscador para seleccionar máquina.
- ✅ Botón **Guardar** que hace `POST` del `deviceId` y `id_maquina` al backend.
- ✅ Servidor HTTP interno que responde a:
  - `GET /device → {"deviceId": "..."}`
  - `GET /ip → {"ip": "..."}`
- ✅ Servicio `Foreground` persistente con notificación.
- ✅ Soporte para `BOOT_COMPLETED`: el servidor inicia al reiniciar el dispositivo.
- ✅ Detección de batería optimizada con alerta y acceso directo a configuración.
- ✅ Cierre automático de la app cuando se abre la configuración, para que el estado se actualice al volver.

---

## 🔧 Tecnologías

- **Kotlin** + **Jetpack Compose**
- **Retrofit** + **Coroutine**
- **ViewModel** + **StateFlow**
- **NanoHTTPD** (servidor HTTP embebido)
- **Foreground Service**
- **BroadcastReceiver** (`BOOT_COMPLETED`)

---

## 📦 Instalación y prueba

1. Clonar o importar el proyecto en Android Studio.
2. Configurar permisos si usas Android 10+ (por ejemplo, acceso a almacenamiento, exclusión de optimización de batería).
3. Conectar al backend configurado en `RetrofitClient`.
4. Ejecutar en un dispositivo físico (recomendado por el uso de `Settings.Secure.ANDROID_ID` y servicios).

---