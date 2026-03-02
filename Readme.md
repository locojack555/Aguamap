# Memoria del proyecto: AguaMap

## Aplicación móvil para la localización de puntos de agua potable

---

**Equipo de desarrollo**

- Jack Santiago Arévalo Montesinos
- Luis Alfredo Mariño
- Adrià González Consuegra
- Cristina Jiménez Cardoso

---

**[Fecha de entrega: 11/03/2026]**

---

## Índice

**Equipo de desarrollo**

**1. INTRODUCCIÓN**

1.1 Objetivos de la aplicación

1.2 Target (Público objetivo)

**2. DOCUMENTACIÓN TÉCNICA**

2.1 Requisitos funcionales por roles

2.2 Diseño de la base de datos NoSQL (Firestore)

2.3 Reglas de Firebase

**3. PLANIFICACIÓN Y SEGUIMIENTO DE LAS TAREAS DE DESARROLLO**

SPRINT 1

SPRINT 2

SPRINT 3

**4. CODIFICACIÓN**

Control de versiones

**5. RECURSOS DEL PROYECTO**

Video demo

Portafolios

**6. LÍNEAS FUTURAS**

1. Sistema de notificaciones inteligentes

2. Gamificación y sistema de reputación

3. Funcionalidades sociales

4. Modo offline

5. Accesibilidad e internacionalización

6. Navegación avanzada

7. Estadísticas ambientales

**7. CONCLUSIÓN**

## 1. INTRODUCCIÓN

En este apartado se presentará la aplicación AguaMap. Se expondrán los objetivos del proyecto, su contribución a los Objetivos de Desarrollo Sostenible (ODS) y el perfil de los usuarios a los que va dirigida.

### 1.1 Objetivos de la aplicación

**AguaMap** es una aplicación móvil que permitirá localizar puntos de acceso de agua potable en cualquier ciudad. Su objetivo principal es facilitar el acceso al agua de calidad, fomentar hábitos saludables y reducir el impacto ambiental asociado al consumo de agua embotellada. La app se alinea directamente con los siguientes Objetivos de Desarrollo Sostenible (ODS) de la Agenda 2030:

-   **ODS 6: “Agua limpia y saneamiento”**
    -   **Problema:** Muchas personas desconocen dónde se encuentran las fuentes públicas y puntos de agua potable. Esta falta de información dificulta el acceso al agua y fomenta el consumo de agua embotellada.
    -   **Solución:** AguaMap reunirá en una sola aplicación todos los puntos de agua potable y los mostrará de forma geolocalizada. Esto facilitará el acceso al agua, ayudará a utilizar mejor este recurso y reforzará su uso como alternativa pública y sostenible.

-   **ODS 3: “Salud y bienestar”**
    -   **Problema:** En el día a día no siempre los ciudadanos consumen suficiente agua, sobre todo durante desplazamientos o actividades al aire libre, lo que afecta al bienestar general.
    -   **Solución:** La aplicación permitirá encontrar puntos de agua cercanos, animando a consumir agua con más frecuencia y a mantener hábitos más saludables. Además, fomentará el caminar o el desplazamiento con vehículos sin motor.

-   **ODS 11: “Ciudades y comunidades sostenibles”**
    -   **Problema:** Los servicios públicos no siempre son visibles ni fáciles de localizar, lo que dificulta su uso y reduce la accesibilidad en la ciudad.
    -   **Solución:** AguaMap hará visibles las infraestructuras de agua potable existentes, ayudando a crear una ciudad más accesible, práctica y sostenible.

-   **ODS 12: “Producción y consumo responsables”**
    -   **Problema:** El uso de botellas de plástico de un solo uso sigue siendo muy alto, en gran parte porque no se conocen alternativas cercanas.
    -   **Solución:** Al facilitar el acceso al agua potable, AguaMap promoverá la reutilización de botellas y reducirá el consumo de plástico, fomentando hábitos de consumo más responsables.

-   **ODS 13: “Acción para el clima”**
    -   **Problema:** La producción y el transporte de agua embotellada generan emisiones de CO₂, lo que conlleva un impacto negativo en el medio ambiente.
    -   **Solución:** AguaMap ayudará a disminuir la huella de carbono asociada al consumo de agua embotellada al apostar por recursos locales.

### 1.2 Target (Público objetivo)

La aplicación está dirigida a toda la ciudadanía y visitantes que busquen hidratarse de forma gratuita y sostenible, reduciendo el uso de plásticos y fomentando hábitos de vida saludables. Esto incluye desde deportistas, excursionistas y ciclistas, hasta cualquier persona que se desplace por la ciudad.

## 2. DOCUMENTACIÓN TÉCNICA

En este apartado se detallarán los requisitos funcionales de la aplicación, diferenciando entre usuarios y administradores. También se describirá la estructura de la base de datos NoSQL en Firestore y las reglas de seguridad implementadas.

### 2.1 Requisitos funcionales por roles

| ID     | Requisito                                                                                               | Usuario | Administrador |
| :----- |:--------------------------------------------------------------------------------------------------------| :-----: | :-----------: |
| RF01   | Iniciar sesión con correo y contraseña.                                                                 |    ✓    |       ✓       |
| RF02   | Registrarse en la aplicación.                                                                           |    ✓    |       ✓       |
| RF03   | Recuperar contraseña mediante correo electrónico.                                                       |    ✓    |       ✓       |
| RF04   | Crear una nueva categoría (nombre, imagen, descripción).                                                |         |       ✓       |
| RF05   | Listar todas las categorías.                                                                            |    ✓    |       ✓       |
| RF06   | Filtrar categorías por nombre y estado (usando comodines * y ?).                                        |    ✓    |       ✓       |
| RF07   | Ampliar información de una categoría (nombre, imagen, descripción).                                     |    ✓    |       ✓       |
| RF08   | Modificar una categoría (nombre, imagen, descripción).                                                  |         |       ✓       |
| RF09   | Eliminar una categoría (si no tiene ítems asociados).                                                   |         |       ✓       |
| RF10   | Crear un nuevo ítem (fuente) con imagen, título, descripción, fecha, autor y GPS (manual o automático). |    ✓    |       ✓       |
| RF11   | Listar todos los ítems (imagen, título, botón de ampliar).                                              |    ✓    |       ✓       |
| RF12   | Filtrar ítems por categoría o estado.                                                                   |    ✓    |       ✓       |
| RF13   | Filtrar ítems por nombre.                                                                               |    ✓    |       ✓       |
| RF14   | Filtrar ítems por rango de distancia (mínima y máxima).                                                 |    ✓    |       ✓       |
| RF15   | Filtrar ítems por número de estrellas de valoración.                                                    |    ✓    |       ✓       |
| RF16   | Ordenar ítems por mejor/peor valoración global.                                                         |    ✓    |       ✓       |
| RF17   | Ordenar ítems por distancia (de menor a mayor o viceversa).                                             |    ✓    |       ✓       |
| RF18   | Ordenar ítems por fecha de creación (más reciente a más antiguo).                                       |    ✓    |       ✓       |
| RF19   | Ampliar información de un ítem (título, imagen, descripción, autor, fecha, mapa, distancia).            |    ✓    |       ✓       |
| RF20   | Crear una valoración (1-5 estrellas) con comentario opcional para un ítem.                              |    ✓    |       ✓       |
| RF21   | Visualizar la valoración global y las valoraciones individuales de un ítem.                             |    ✓    |       ✓       |
| RF22   | Modificar un ítem (con restricciones según validación y rol).                                           |    ✓*   |       ✓       |
| RF23   | Eliminar un ítem (manual por admin, automático por reportes).                                           |         |       ✓       |
| RF24   | Eliminar una valoración y su comentario asociado.                                                       |         |       ✓       |
| RF25   | Censurar un comentario (eliminarlo, pero no la valoración).                                             |         |       ✓       |
| RF26   | Iniciar una partida en "AguaQuest" (juego de geolocalización).                                          |    ✓    |       ✓       |
| RF27   | Visualizar el Top 10 de puntuaciones del día.                                                           |    ✓    |       ✓       |
| RF28   | Visualizar el Top 10 de puntuaciones del mes.                                                           |    ✓    |       ✓       |
| RF29   | Visualizar el Top 10 de puntuaciones del año.                                                           |    ✓    |       ✓       |
| RF30   | Gestionar el perfil propio (datos, fuentes, valoraciones, historial, favoritos, idioma).                |    ✓    |       ✓       |
| RF31   | Tener un apartado de gestión para la aplicación.                                                        |         |       ✓       |

*\*Los usuarios pueden modificar sus ítems no validados. Si el ítem está validado, sus modificaciones se convierten en sugerencias.*

### 2.2 Diseño de la base de datos NoSQL (Firestore)

A continuación, se propone la estructura de colecciones y documentos realizada en Firestore.

### Colecciónes y documentos

### **Colección `categories`**
*Documento `{categoryId}`*


    {
      "id": "string",
      "name": "string",
      "imageUrl": "string",
      "description": "string"
    }

### **Colección `fountains`**
*Documento `{fountainId}`*

    {
      "id": "string",
      "name": "string",
      "latitude": "number",
      "longitude": "number",
      "geohash": "string",
      "operational": "boolean",
      "category": {
        "id": "string",
        "name": "string",
        "imageUrl": "string",
        "description": "string"
      },
      "votedByPositive": ["string"],
      "votedByNegative": ["string"],
      "description": "string",
      "imageUrl": "string",
      "dateCreated": "timestamp",
      "ratingAverage": "number",
      "totalRatings": "number",
      "status": "string",
      "createdBy": "string",
      "positiveVotes": "number",
      "negativeVotes": "number"
    }

### **Subcolección `comments` dentro de `fountains`**
*Documento `{commentId}` dentro de `/fountains/{fountainId}/comments/`*

    {
      "id": "string",
      "userId": "string",
      "userName": "string",
      "rating": "number",
      "comment": "string",
      "censored": "boolean",
      "reported": "boolean",
      "timestamp": "number"
    }

### **Colección `gameSessions`**
*Documento `{gameSessionId}`*

    {
      "userId": "string",
      "userName": "string",
      "score": "number",
      "distance": "number",
      "date": "timestamp",
      "fountainId": "string",
      "fountainName": "string"
    }




### **Colección `reports`**
*Documento `{reportId}`*

    {
      "id": "string",
      "fountainId": "string",
      "fountainName": "string",
      "userId": "string",
      "description": "string",
      "timestamp": "number",
      "resolved": "boolean"
    }

### **Colección `reportedComments`**
*Documento `{reportedCommentsId}`*

    {
    "reportId": "string",
    "fountainId": "string",
    "commentId": "string",
    "reason": "string",
    "timestamp": "number"
    }

### **Colección `users`**
*Documento `{userId}`*

    {
    "uid": "string",
    "nom": "string",
    "email": "string",
    "role": "string" // "USER" o "ADMIN"
    }

### **Colección `user_stats`**
*Documento `{userId}`*

    {
    "userId": "string",
    "userName": "string",
    "fountainsCount": "number",
    "commentsCount": "number",
    "lastUpdated": "timestamp"
    }

### 2.3 Reglas del Firebase
    rules_version = '2';
    service cloud.firestore {
    match /databases/{database}/documents {
    
        // --- FUNCIONES DE AYUDA ---
        function isAuthenticated() {
          return request.auth != null;
        }
    
        function isAdmin() {
          return isAuthenticated() && 
                 get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'ADMIN';
        }
    
        // Valida que solo se toquen campos de votación o estado operativo
        function isOnlyVoting() {
          return request.resource.data.diff(resource.data).affectedKeys()
            .hasOnly(['positiveVotes', 'votedByPositive', 'negativeVotes', 'votedByNegative', 'status', 'operational', 'ratingAverage', 'totalRatings']);
        }
    
        // Valida que solo se toquen campos de moderación de comentarios
        function isCommentStatusChange() {
          return request.resource.data.diff(resource.data).affectedKeys()
            .hasOnly(['reports', 'reportedBy', 'status', 'censored', 'reported']);
        }
    
        // --- COLECCIONES ---
    
        // 1. CATEGORÍAS
        match /categories/{categoryId} {
          allow read: if true;
          allow write: if isAdmin();
        }
    
        // 2. FUENTES
        match /fountains/{fountainId} {
          allow read: if true;
          allow create: if isAuthenticated();
          
          // PERMITIR BORRAR: Si es Admin O si ya tiene 2 votos negativos (el 3º activa el borrado)
          allow delete: if isAdmin() || (isAuthenticated() && resource.data.negativeVotes >= 2);
          
          // ACTUALIZAR: Admin, Dueño (si está pendiente) o cualquier usuario que esté votando
          allow update: if isAdmin() 
            || (isAuthenticated() && resource.data.createdBy == request.auth.uid && resource.data.status == 'PENDING')
            || (isAuthenticated() && isOnlyVoting());
    
          // --- SUBCOLECCIÓN DE COMENTARIOS ---
          match /comments/{commentId} {
            allow read: if true;
            allow create: if isAuthenticated();
            
            // Borrar: Dueño o Admin
            allow delete: if isAdmin() || (isAuthenticated() && resource.data.userId == request.auth.uid);
            
            // Editar: Dueño (todo) o Otros (solo reportar/censurar)
            allow update: if isAdmin() || (isAuthenticated() && resource.data.userId == request.auth.uid) 
                         || (isAuthenticated() && isCommentStatusChange());
          }
        }
    
        // 3. USUARIOS
        match /users/{userId} {
          allow read: if isAuthenticated();
          allow write: if isAuthenticated() && request.auth.uid == userId;
        }
    
        // 4. OTROS (RANKINGS, SESIONES, STATS)
        // Usamos wildcards recursivos para simplificar estas colecciones
        match /gameSessions/{doc} { allow read, write: if isAuthenticated(); }
        match /monthlyRanking/{doc} { allow read, write: if isAuthenticated(); }
        match /historicRanking/{doc} { allow read, write: if isAuthenticated(); }
        match /userStats/{doc} { allow read, write: if isAuthenticated(); }
        
        // 5. REPORTES DE COMENTARIOS
        match /reports_comments/{reportId} {
          allow read: if isAdmin();
          allow create: if isAuthenticated();
          allow update: if isAdmin();
          allow delete: if isAdmin();
        }
        
        // 6. REPORTES DE FUENTES (GENERALES)
        match /reports/{reportId} {
          // Cualquier usuario logueado puede enviar un error o queja
          allow create: if isAuthenticated();
          
          // Solo el admin puede ver la lista de quejas, marcarlas como resueltas o borrarlas
          allow read, update, delete: if isAdmin();
        }
    }
    }

## 3. PLANIFICACIÓN Y SEGUIMIENTO DE LOS TAREAS DE DESARROLLO

En este apartado se explicará la organización del trabajo mediante sprints realizado en el Jira. Se presentarán los objetivos iniciales planificados para cada fase y las notas de lanzamiento, junto con los enlaces al control de versiones y a los materiales finales del proyecto.


Para garantizar un desarrollo ordenado y eficiente de AguaMap, se ha trabajado siguiendo una metodología ágil basada en **sprints**. Cada sprint ha tenido una duración definida con objetivos claros, permitiendo entregar funcionalidades completas y funcionales al final de cada ciclo.

### SPRINT 1
**Planificación inicial**
- Infraestructura: Establecimiento de la base técnica del proyecto con Firebase.
- Autenticación: Implementación del sistema de registro y acceso de usuarios.
- Localización: Desarrollo de las funcionalidades nucleares de geolocalización.
- Gamificación: Creación de las mecánicas principales de juego para la aplicación.

![Tablero de seguimiento](./assets/images/sprint1.png)

*Figura: Tareas implementadas en Jira durante el Sprint 1.*


**Release Notes**
- Core: Sistema de autenticación robusto integrado con Firebase y base del mapa interactivo.
- Mecánicas: Motor de juego con geolocalización, cálculo de distancias y sistema de rankings.
- Global: Interfaz multilingüe en tres idiomas gestionada mediante el Translations Editor.

---

### SPRINT 2
**Planificación inicial**
- Categorías: Desarrollo e implementación del sistema completo de categorías.
- Fuentes: Gestión avanzada de fuentes mediante geolocalización en Firestore.
- Administración: Implementación del panel de control para la gestión del sistema.
- Seguridad: Configuración de roles de usuario y reglas de acceso a los datos.

![Tablero de seguimiento Parte 1](./assets/images/sprint2_parte1.png)
![Tablero de seguimiento Parte 2](./assets/images/sprint2_parte2.png)

*Figuras: Tareas implementadas en Jira durante el Sprint 2.*

**Release Notes**

- Funcionalidades: Gestión de fuentes por GPS e integración completa de mapas interactivos.
- Comunidad: Sistema de validación por feedback y filtros avanzados de búsqueda.
- Control: Panel de moderación exclusivo para administradores y gestión de contenido.

---

### SPRINT 3
**Planificación inicial**
- Robustez: Gestión de excepciones en todos los módulos de la App.
- Interfaz: Optimización del diseño Responsive y la Accesibilidad.
- Documentación: Elaboración de la memoria técnica y documentación final.
- Promoción: Creación del vídeo demo y de la presentación del proyecto.

![Tablero de seguimiento Parte 1](./assets/images/sprint3_parte1.png)
![Tablero de seguimiento Parte 2](./assets/images/sprint3_parte2.png)

*Figuras: Tareas implementadas en Jira durante el Sprint 3.*

**Release Notes**

- UX: Interfaz 100% responsive, accesible y con gestión global de errores.
- Calidad: Entrega de memoria técnica, vídeo promocional y carga optimizada.
- Ajustes: Refactorización de código y pulido final de elementos gráficos.

---

## 4. CODIFICACIÓN

En este apartado se facilitará el enlace al repositorio en GitHub donde se ha alojado el código fuente del proyecto, permitiendo consultar el historial de versiones y la colaboración entre los desarrolladores.

**Control de versiones**
- Repositorio en GitHub: [ https://github.com/locojack555/Aguamap ] (enlace)

---

## 5. Recursos del proyecto

En este apartado se recopilarán los materiales complementarios, incluyendo un vídeo demostrativo de la aplicación en funcionamiento y los portafolios profesionales de los miembros del equipo de desarrollo.

**Video demo**


**Portafolio**
- Desarollador 1 (Jack Santiago Arévalo Montesinos): [ https://github.com/locojack555/Aguamap ] (enlace)
- Desarollador 2 (Luis Alfredo Mariño): [  ] (enlace)
- Desarollador 3 (Cristina Jiménez Cardoso): [ ] (enlace)
- Desarollador 4 (Adrià González Consuegra): [  ] (enlace)



## 6. LÍNEAS FUTURAS

En este apartado se plantearán las posibles mejoras y nuevas funcionalidades que podrían incorporarse en versiones posteriores de AguaMap, con el objetivo de seguir evolucionando el proyecto.

---

### 1. Sistema de notificaciones inteligentes

Este sistema mantendrá informados a los usuarios en todo momento sobre lo que sucede a su alrededor:

- **Avisos de fuentes cercanas:** Cuando un usuario pasee por la ciudad, recibirá notificaciones indicándole que hay una fuente de agua potable cerca de su ubicación actual.
- **Alertas de fuentes favoritas:** Si un usuario marca una fuente como favorita, recibirá avisos cuando esta esté averiada o vuelva a funcionar.
- **Recordatorios para beber agua:** El sistema enviará notificaciones para recordar al usuario que debe hidratarse, teniendo en cuenta su actividad física y el clima del momento.
- **Notificaciones por proximidad:** Aunque esta funcionalidad se descartó temporalmente en la versión 1.2, se prevé implementarla en el futuro para avisar de fuentes cercanas sin necesidad de tener la aplicación abierta.

---

### 2. Gamificación y sistema de reputación

Para hacer la experiencia más divertida y motivar a los usuarios a participar activamente:

- **Rankings locales y globales:** Los usuarios podrán ver su posición en clasificaciones por barrio, ciudad o a nivel mundial, compitiendo por ser los más activos.
- **Eventos especiales:** Se organizarán actividades temporales como la *"Semana de la fuente escondida"*, donde los usuarios deberán encontrar fuentes secretas para ganar premios.
- **Modo multijugador:** Los usuarios podrán jugar partidas de AguaQuest con amigos o con otros usuarios en tiempo real.
- **Niveles de dificultad:** El minijuego ofrecerá diferentes niveles (fácil, medio, difícil) para que tanto principiantes como expertos puedan disfrutar.
- **Temporadas y recompensas:** Cada cierto tiempo se renovarán los desafíos y los usuarios podrán obtener recompensas exclusivas por participar durante esa temporada.

---

### 3. Funcionalidades sociales

AguaMap quiere convertirse en una comunidad donde los usuarios puedan interactuar entre ellos:

- **Comunidades locales:** Los usuarios podrán unirse a grupos de su barrio o distrito para compartir información sobre las fuentes de su zona.
- **Sistema de amigos:** Podrán agregar a otros usuarios para seguir su actividad, ver sus logros y competir con ellos.
- **Compartir en redes sociales:** Será posible publicar puntuaciones, rutas realizadas y nuevas fuentes descubiertas en plataformas como Instagram, Twitter o Facebook.
- **Foros y chats:** Se habilitarán espacios de discusión donde los usuarios podrán comentar el estado de una fuente específica, resolver dudas o compartir experiencias.

---

### 4. Modo offline

Para que la aplicación sea útil incluso sin conexión a Internet:

- **Mapa sin conexión:** Los usuarios podrán consultar el mapa y ver la ubicación de las fuentes aunque no tengan conexión a Internet.
- **Acceso en zonas rurales:** Esta funcionalidad mejorará notablemente la experiencia en áreas con poca cobertura móvil, como montañas o parques naturales, permitiendo a excursionistas y deportistas encontrar agua fácilmente.

---

### 5. Accesibilidad e internacionalización

AguaMap quiere ser una aplicación para todos, independientemente de sus capacidades o idioma:

- **Compatibilidad con lectores de pantalla:** La aplicación funcionará correctamente con TalkBack, permitiendo a personas con discapacidad visual utilizarla sin problemas.
- **Modos de contraste alto:** Se incorporarán opciones visuales para personas con daltonismo o dificultades para distinguir colores.
- **Textos adaptables:** Los tamaños de fuente se podrán ajustar siguiendo las pautas de accesibilidad WCAG.
- **Más idiomas:** Además de catalán, castellano e inglés, se traducirá la aplicación a idiomas como árabe, chino, rumano o amazig, facilitando su uso a personas de diferentes culturas.

---

### 6. Navegación avanzada

Para facilitar el camino hasta las fuentes de agua:

- **Indicaciones paso a paso:** La aplicación se integrará con Google Maps o Waze para ofrecer rutas detalladas hasta la fuente seleccionada.
- **Mapa de calor:** Se mostrarán las zonas con mayor concentración de fuentes y también aquellas donde hay más usuarios utilizando la aplicación.
- **Rutas optimizadas:** El sistema calculará la mejor ruta para encontrar la fuente más cercana teniendo en cuenta el trayecto actual del usuario, ideal para cuando va caminando o en bicicleta.

---

### 7. Estadísticas ambientales

Para que los usuarios puedan ver el impacto positivo que generan usando AguaMap:

- **Ahorro de plástico:** La aplicación calculará cuántas botellas de plástico ha dejado de usar cada persona gracias a rellenar su botella reutilizable en las fuentes.
- **Reducción de CO₂:** Se estimará la cantidad de emisiones de dióxido de carbono que se han evitado al no producir ni transportar agua embotellada.
- **Impacto colectivo:** Se mostrarán estadísticas globales, por ciudad y por usuario, para que todos puedan ver cómo contribuyen al cuidado del medio ambiente.
- **Gráficos evolutivos:** Los usuarios podrán visualizar en gráficos cómo ha ido aumentando su ahorro de plástico y reducción de CO₂ con el paso del tiempo.

# 7. CONCLUSIÓN

En este apartado se resumirán los logros alcanzados con el desarrollo de AguaMap, destacando su utilidad práctica y el impacto positivo que genera en la sociedad y el medio ambiente.

AguaMap ya es una realidad: una aplicación que funciona y cumple con su propósito. Nace con la misión de conectar a las personas con las fuentes de agua potable, haciendo que beber agua del grifo sea más fácil, saludable y sostenible.

**¿Qué hemos conseguido?**

- Los usuarios pueden encontrar fácilmente cualquier fuente de agua potable gracias a los mapas interactivos.
- Hemos creado el minijuego AguaQuest, que hace más divertido y participativo el uso de la aplicación.
- La información de las fuentes es fiable porque administradores y usuarios trabajan juntos para mantenerla actualizada.
- La aplicación está preparada para crecer sin problemas gracias a la tecnología Firebase y Jetpack Compose.
- Cualquier persona puede usarla, independientemente de sus capacidades o del dispositivo que tenga.

**¿Qué impacto estamos generando?**

- Ahora los ciudadanos conocen y valoran más las fuentes públicas de su ciudad.
- Cada vez más gente usa botellas reutilizables en lugar de comprar agua embotellada, reduciendo el plástico.
- Los usuarios beben agua con más frecuencia, mejorando su salud e hidratación.
- Entre todos estamos ayudando a cuidar el planeta y a luchar contra el cambio climático.

**En definitiva...**

AguaMap es mucho más que un mapa de fuentes. Es una herramienta que demuestra que la tecnología puede ayudar a construir ciudades más sostenibles, personas más saludables y un mundo con menos plástico. Pequeños gestos, como rellenar una botella, pueden generar un gran cambio si los hacemos entre todos.

---