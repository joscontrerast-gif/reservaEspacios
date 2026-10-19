# reservaEspacios

Aplicación móvil Android para **reservar y administrar los espacios compartidos de un instituto** (teatro, estudio de grabación, talleres, salas comunes, etc.).

Hoy la coordinación de estos espacios depende de correos, mensajes y de una sola persona. Esta app la centraliza: los usuarios consultan los espacios y solicitan reservas, y un administrador las **aprueba o rechaza**, evitando choques de horario y dejando registro de lo que ocurrió.

> Proyecto de la **Evaluación Parcial 2** (avance intermedio). Desarrollado en Kotlin con Jetpack Compose.

---

## Contenido

1. [Integrantes](#1-integrantes)
2. [¿Qué hace la app?](#2-qué-hace-la-app)
3. [Reglas de negocio](#3-reglas-de-negocio)
4. [Tecnologías](#4-tecnologías)
5. [Arquitectura](#5-arquitectura)
6. [Almacenamiento local](#6-almacenamiento-local)
7. [Recursos nativos](#7-recursos-nativos)
8. [Cómo ejecutar el proyecto](#8-cómo-ejecutar-el-proyecto)
9. [Cuentas y datos de prueba](#9-cuentas-y-datos-de-prueba)
10. [Pruebas unitarias](#10-pruebas-unitarias)
11. [Cumplimiento de la evaluación](#11-cumplimiento-de-la-evaluación)
12. [Supuestos y puntos por definir](#12-supuestos-y-puntos-por-definir)
13. [Limitaciones conocidas y próximos pasos](#13-limitaciones-conocidas-y-próximos-pasos)

---

## 1. Integrantes

| Integrante | Responsabilidad principal |
|---|---|
| **[Completar: nombre 1]** | Formularios validados, gestión de estado (`UiState` + `StateFlow`), validaciones y reglas de negocio |
| **[Completar: nombre 2]** | Persistencia local (SQLite y SharedPreferences) y recursos nativos *(confirmar)* |

- **Repositorio:** [Completar: enlace a GitHub]
- **Planificación:** [Completar: enlace al tablero de Trello]

---

## 2. ¿Qué hace la app?

La app tiene **cuatro perfiles**. Estudiante, Docente y Administrativo se eligen al registrarse y tienen las mismas funciones; el **Administrador** no se puede elegir al registrarse (lo crea la institución).

### Para quien solicita (Estudiante, Docente, Administrativo)

| Pestaña | Qué permite |
|---|---|
| **Solicitar** | Pedir un espacio indicando actividad, cantidad de asistentes, fecha y hora de inicio y término (con calendario y reloj). Valida capacidad, fechas pasadas, horarios y choques con clases. |
| **Mis solicitudes** | Ver el estado de cada solicitud (pendiente, aprobada, rechazada o cancelada) y el motivo si fue rechazada. Cancelar con confirmación. En las aprobadas: **agregar al calendario del teléfono** y **compartir** el detalle. |

### Para el Administrador

| Pestaña | Qué permite |
|---|---|
| **Solicitudes** | Revisar las pendientes y **aprobar** o **rechazar** (el rechazo exige un motivo). Ver las ya resueltas. |
| **Espacios** | Registrar espacios (código, nombre, capacidad, ubicación), su **equipamiento** con estado, y una **foto tomada con la cámara**. |
| **Horarios** | Cargar la **planificación académica** (asignatura, docente, espacio, día, horario, estudiantes). Las clases bloquean el espacio todas las semanas. |

### Para todos

- Registro e inicio de sesión con validación en cada campo.
- **La sesión se conserva** al cerrar la app (no se pide el login cada vez) y se precarga el último correo usado.
- **Notificaciones locales** al iniciar sesión: el solicitante recibe aviso de sus solicitudes aprobadas o rechazadas, y el administrador recibe cuántas tiene pendientes.

---

## 3. Reglas de negocio

1. **Toda solicitud requiere aprobación.** Que un espacio se vea libre no significa que se reserve automáticamente. Una solicitud pendiente no bloquea el horario.
2. **Prioridad académica.** Las clases del semestre tienen preferencia: no se puede solicitar un horario ocupado por una clase (el formulario avisa de inmediato) ni aprobar una solicitud que choque con ella.
3. **La disponibilidad se verifica al aprobar**, no solo al solicitar: dos reservas aprobadas no pueden ocupar el mismo espacio a la vez.
4. **El rechazo exige un motivo** (mínimo 10 caracteres), que el solicitante puede ver.
5. **Cancelar libera el horario.** Si la reserva estaba aprobada, otra solicitud en conflicto ya se puede aprobar.
6. **Capacidad:** los asistentes (o estudiantes de una clase) no pueden superar la capacidad del espacio.
7. **Responsable identificable:** cada solicitud queda asociada a quien la envió.
8. **Seguridad básica:** las contraseñas se guardan como hash **PBKDF2 con sal aleatoria**, nunca en texto plano. El perfil Administrador no se puede autoasignar.

---

## 4. Tecnologías

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| Interfaz | Jetpack Compose + Material 3 |
| Estado y asincronía | ViewModel, `StateFlow`, corrutinas |
| Persistencia | SQLite nativo (`SQLiteOpenHelper`) y SharedPreferences |
| Recursos nativos | Cámara, notificaciones, intents del sistema (calendario y compartir) |
| Build | Android Gradle Plugin 9.4.1, Gradle 9.6.0, JDK 25 |
| SDK | `minSdk` 26, `compileSdk` 37 |
| Pruebas | JUnit 4 |

---

## 5. Arquitectura

Patrón **MVVM** por capas. La interfaz no contiene reglas: solo muestra el estado y envía eventos.

```
Pantalla (Compose)  ──evento──▶  ViewModel  ──▶  Repositorio (interfaz)  ──▶  SQLite
        ▲                           │                      │
        └──────── UiState ◀─────────┘                      └─ validation/ (reglas puras)
```

- **Un `UiState` único por pantalla**, expuesto con `StateFlow`. La pantalla es *stateless*: recibe el estado y emite eventos.
- **Validaciones como funciones puras** (paquete `validation`), sin dependencias de Android, por lo que se prueban sin emulador. Los errores se muestran solo después de que el usuario interactúa con el campo o intenta enviar.
- **Repositorios detrás de interfaces.** La app usa las implementaciones SQLite; las versiones `Fake*` (en memoria) se conservan para las pruebas unitarias.
- **Estado compartido y reactivo:** los repositorios exponen `StateFlow`, así que lo que hace el administrador (aprobar, crear un espacio, cargar una clase) se refleja solo en las pantallas del solicitante y viceversa.
- **`AppContainer`** crea e inyecta las dependencias una sola vez, en `ReservaApp.onCreate`.

### Estructura de carpetas

```
app/src/main/java/com/example/reservaespacios/
├── ReservaApp.kt, MainActivity.kt, AppContainer.kt
├── model/            Modelos: User, Space, Equipment, Reservation, AcademicClass
├── data/             Interfaces de repositorio, SessionStore y versiones Fake* (para tests)
│   └── local/        SQLite: AppDatabaseHelper, repositorios Sqlite*, AppPreferences, PasswordHasher
├── validation/       Validadores y reglas de negocio (funciones puras)
├── nativeres/        Recursos nativos: notificaciones, almacenamiento de fotos, intents
├── util/             Formatos y conversiones de fecha y hora
└── ui/
    ├── AppRoot.kt    Decide qué mostrar según la sesión
    ├── auth/         Login y registro
    ├── home/         Pantalla principal y barra de navegación por perfil
    ├── reservation/  Formulario de solicitud
    ├── myreservations/  Mis solicitudes
    ├── review/       Aprobar y rechazar (administrador)
    ├── spaces/       Registro de espacios y equipamiento (administrador)
    ├── schedule/     Planificación académica (administrador)
    └── components/   Campos reutilizables con validación visual, selectores, imagen local
```

---

## 6. Almacenamiento local

**SQLite** (`reserva_espacios.db`, almacenamiento interno de la app). Se crea la primera vez y se llena con datos de ejemplo.

| Tabla | Contenido |
|---|---|
| `users` | Nombre, correo (único), hash de contraseña, sal y perfil |
| `spaces` | Código (único), nombre, capacidad, ubicación y ruta de la foto |
| `equipment` | Equipos de cada espacio con su estado (se borran junto con el espacio) |
| `academic_classes` | Clases del semestre: asignatura, docente, espacio, día, horario y estudiantes |
| `reservations` | Solicitudes con su estado, motivo de rechazo y si el solicitante ya fue notificado |

**SharedPreferences** (`reserva_prefs`) guarda la sesión activa (solo el id del usuario) y el último correo usado. **Nunca se guarda la contraseña.**

Las fotos de los espacios son archivos en `filesDir/space_photos`; en SQLite solo se guarda su ruta.

---

## 7. Recursos nativos

| Recurso | Dónde se usa | Detalle |
|---|---|---|
| **Cámara** | Pestaña *Espacios* → «Tomar foto» | Contrato `TakePicture` con permiso `CAMERA` pedido en tiempo de ejecución. La foto se escribe mediante un `FileProvider` y se corrige su rotación (EXIF). La cámara es opcional: la app funciona sin ella. |
| **Notificaciones** | Al iniciar sesión | Canal de notificación y permiso `POST_NOTIFICATIONS` (Android 13+). Avisa al solicitante de solicitudes aprobadas o rechazadas, y al administrador de las pendientes. Al tocarla abre la app. |
| **Intents del sistema** | *Mis solicitudes* → reservas aprobadas | «Agregar al calendario» abre el Calendario del teléfono con el evento ya rellenado, y «Compartir» abre el selector del sistema (WhatsApp, correo, etc.). No requieren permisos. |

---

## 8. Cómo ejecutar el proyecto

### Requisitos

- **Android Studio** reciente, compatible con Android Gradle Plugin **9.4.1** (se recomienda la última versión estable).
- **JDK 25**: el JetBrains Runtime que trae Android Studio (`jbr-25`).
- **Android SDK 37** (se descarga desde el SDK Manager si falta).
- Un emulador o dispositivo con **Android 8.0 (API 26) o superior**.
- Conexión a internet la primera vez, para que Gradle descargue las dependencias.

### Pasos

1. Clona el repositorio:
   ```bash
   git clone <URL-del-repositorio>
   ```
2. Abre la carpeta en **Android Studio** (*File ▸ Open*) y espera a que termine la sincronización de Gradle.
3. Elige un emulador o dispositivo y presiona **Run ▸ Run 'app'**.

También por terminal:

```bash
./gradlew installDebug        # compila e instala en el dispositivo conectado
./gradlew testDebugUnitTest   # corre las pruebas unitarias
```

> Si necesitas empezar de cero con los datos de ejemplo, desinstala la app o usa *Borrar datos* en los ajustes del teléfono: la base de datos se vuelve a crear al abrirla.

---

## 9. Cuentas y datos de prueba

| Perfil | Correo | Contraseña |
|---|---|---|
| **Administrador** | `admin@instituto.cl` | `Admin1234` |
| **Docente** | `docente@instituto.cl` | `Docente123` |

También puedes **registrar** cuentas nuevas de Estudiante, Docente o Administrativo desde la pantalla de registro (la contraseña debe tener al menos 8 caracteres, con letras y números).

**Datos de ejemplo** incluidos:
- 4 espacios: Teatro (con proyector y micrófonos), Estudio de grabación, Taller de diseño y Sala común.
- 2 clases: *Diseño Gráfico I* (lunes, Taller de diseño) y *Producción Audiovisual* (miércoles, Estudio de grabación).
- 2 solicitudes pendientes para el Teatro **mañana** que se solapan entre sí (11:00–12:00), pensadas para probar la regla de aprobación.

### Recorrido de prueba sugerido

1. Entra como **administrador**. Si concedes el permiso de notificaciones, verás el aviso de solicitudes pendientes. En *Solicitudes*, **aprueba** «Ensayo de obra».
2. Intenta **aprobar** «Charla de bienvenida»: debe fallar, porque el Teatro ya está reservado a esa hora.
3. **Rechaza** esa solicitud: prueba dejar el motivo vacío o muy corto (debe mostrar el error) y luego uno válido.
4. En *Horarios*, registra una clase en el Teatro, el día de la semana de mañana, de 10:00 a 11:00. Revisa que el formulario valide capacidad y solapamientos.
5. En *Espacios*, registra un espacio nuevo con equipamiento y **toma una foto**.
6. Cierra sesión y entra como **docente**: recibirás la notificación de tus solicitudes resueltas. En *Mis solicitudes* verás el estado, el motivo del rechazo, y en las aprobadas podrás **agregar al calendario** y **compartir**.
7. En *Solicitar*, intenta reservar el Teatro en el horario de la clase del paso 4: el campo «Término» avisa que choca con la clase.
8. Cierra y vuelve a abrir la app: la **sesión y los datos se conservan**.

---

## 10. Pruebas unitarias

Unas **80 pruebas unitarias** (JUnit 4) que no necesitan emulador:

| Área | Qué comprueban |
|---|---|
| Validadores | Autenticación, reservas, espacios y equipamiento, horarios y motivo de rechazo |
| Reglas de negocio | Cancelación, solapamiento de horarios y prioridad académica |
| Repositorio en memoria | Aprobación, rechazo, cancelación y conflictos de horario |
| Utilidades | Formato y conversión de fechas y horas |
| Seguridad | Hash de contraseñas (`PasswordHasher`) |

**No hay aún pruebas** de los repositorios SQLite ni de la interfaz (solo el ejemplo generado por Android Studio en `androidTest`).

---

## 11. Cumplimiento de la evaluación

| Requisito | Estado | Dónde |
|---|---|---|
| Interfaz organizada y navegación clara | ✅ | Material 3, barra de navegación inferior distinta por perfil |
| Formularios validados con íconos y mensajes visuales | ✅ | 6 formularios: login, registro, solicitud de reserva, espacio y equipamiento, planificación académica y motivo de rechazo. Ícono de error o de éxito y mensaje por campo |
| Validaciones manejadas desde la lógica | ✅ | Paquete `validation` (funciones puras) + `ViewModel`s |
| Animaciones funcionales | ❌ **Pendiente** | Aún no hay animaciones implementadas |
| Estructura modular, MVVM y persistencia local | ✅ | Carpetas por capa y por pantalla; SQLite y SharedPreferences |
| Repositorio en GitHub y planificación en Trello | ⏳ | Completar los enlaces de la sección 1 |
| Al menos dos recursos nativos integrados en la UI | ✅ | Cámara, notificaciones e intents (calendario y compartir) |

**Indicadores de logro:** IL2.1 (interfaces y formularios validados), IL2.2 (estado y lógica de control), IL2.3 (almacenamiento local y arquitectura) e IL2.4 (recursos nativos).

---

## 12. Supuestos y puntos por definir

Estas reglas quedaron abiertas en la reunión con el cliente; mientras tanto, la app asume lo siguiente:

- **Cancelación:** se puede cancelar una solicitud pendiente o una reserva aprobada que aún no ocurrió. El plazo mínimo y quién puede hacerlo están por definir.
- **Clases del semestre:** se repiten todas las semanas, sin fecha de inicio ni de término (aún no se definió el formato de carga de la planificación).
- **Permisos por perfil:** Estudiante, Docente y Administrativo tienen las mismas funciones de solicitud. Las diferencias de permisos están por acordar.
- **Notificaciones:** son locales y se muestran al iniciar sesión. El cliente mencionó correos, pero no quedó como obligación.
- **Prioridad entre solicitudes** que compiten por el mismo espacio: decide el administrador al aprobar (no hay criterio automático).

---

## 13. Limitaciones conocidas y próximos pasos

- [ ] **Animaciones** (transiciones, retroalimentación al aprobar o rechazar, etc.).
- [ ] **Check-in** del responsable y liberación automática si no llega.
- [ ] **Invitados externos** y servicios de apoyo (audiovisual, aseo).
- [ ] **Estadísticas e historial** de uso de los espacios.
- [ ] **Notificaciones por correo** y avisos mientras la app está cerrada.
- [ ] **Migraciones de base de datos:** hoy `onUpgrade` está vacío (versión 1).
- [ ] **Pruebas** de los repositorios SQLite y de la interfaz.
- [ ] **Textos en `strings.xml`:** los textos de la interfaz están escritos directamente en el código.
- [ ] **Autenticación con credenciales institucionales.**
