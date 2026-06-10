# Bitacora

Una aplicación para llevar un registro de tus vacaciones!

## Descripción

Esta es una aplicación nativa de Android diseñada con Jetpack Compose para la materia de Aplicaciones Móviles de la Universidad Nacional Arturo Jauretche.\
Se hizo uso de Firestore para almacenar los datos, Cloudinary para las imágenes y OpenStreetMaps para el mapa.

## Integrantes del grupo
* Daisy Lizbeth Torrico
* Enzo Lihuel Sapienza

## Tecnologías utilizadas
* Android Studio
* Gradle
* Kotlin (con arquitecture de Jetpack Compose)
* Material Desing 3
* Firebase Authentication
* Firebase Firestore
* OpenStreetMap (OSMDroid)
* Cloudinary

## Requisitos
* Android Studio
* JDK 17
* Android SDK API 36
* Dispositivo Android con API 36 o superior

## Configuración del proyecto
### 1. Clonar repositorio
`git clone https://github.com/daisytorrico/Bitacora`

### 2. Abrir el proyecto
Abrir Android Studio y seleccionar la carpeta del proyecto.

### 3. Sincronizar dependencias
Esperar a que Gradle descargue y sincronice todas las dependencias necesarias.

### 4. Configurar Firebase
Por razones de seguridad, el archivo google-services.json no se incluye en este repositorio.\
Para ejecutar el proyecto:
* Crear un proyecto en Firebase Console.
* Registrar una aplicación Android con el package name: `com.catedra.bitacora`
* Descargar el archivo "google-services.json" desde la configuración.
* Copiarlo dentro de: `app/google-services.json`

### 5. Habilitar los servicios necesarios:
* Firebase Authentication
* Cloud Firestore

## Configuración alternativa para la cátedra

Si la cátedra necesita ejecutar exactamente la misma base de datos utilizada durante el desarrollo, solicitar el archivo google-services.json y acceso al proyecto Firebase al grupo desarrollador.

## Ejecutar el proyecto
Una vez abierto el proyecto, ejecutar un emulador de Android o enchufar un dispositivo físico. Luego seleccionar "Run" o ingresar "Shift + F10" para compilar y cargar el proyecto.
