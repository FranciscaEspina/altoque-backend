# Al Toque — Backend (Spring Boot)

API REST de la tienda **Al Toque**: catálogo, usuarios, órdenes y autenticación con **JWT y roles**.
Stack: Spring Boot 4.1.2 · Java 21 · Maven · Spring Data JPA · Spring Security · H2 (archivo) · springdoc (Swagger).

## Cómo ejecutarlo

Requisitos: **JDK 21**. No hace falta instalar Maven (se incluye el wrapper).

```bash
# Windows
mvnw.cmd spring-boot:run
# Linux / Mac
./mvnw spring-boot:run
```

Queda disponible en `http://localhost:8080`.

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Consola H2 | http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/altoque`, usuario `sa`, sin contraseña) |

La base de datos se guarda en la carpeta `data/` (se conserva al reiniciar). Para empezar de cero, detén la app y borra esa carpeta.

## Usuarios de demostración (se crean al primer arranque)

| Rol | Correo | Contraseña |
|---|---|---|
| ADMINISTRADOR | admin@duoc.cl | admin123 |
| VENDEDOR | vendedor@duoc.cl | vend123 |
| CLIENTE | cliente@duoc.cl | cli123 |

## Arquitectura

```
controller/   Endpoints REST (reciben y devuelven DTO)
service/      Lógica de negocio (validaciones, stock, órdenes, autenticación)
repository/   Acceso a datos con Spring Data JPA
entity/       Modelo de datos (Usuario, Categoria, Producto, Orden, DetalleOrden, RefreshToken)
dto/          Objetos de entrada/salida con validaciones (Bean Validation)
security/     JwtService (genera/valida tokens) y JwtAuthFilter (autentica cada request)
config/       SecurityConfig (reglas de acceso, CORS), OpenApiConfig (Swagger), DataInitializer (datos iniciales)
exception/    GlobalExceptionHandler (errores con formato uniforme)
```

### Modelo de datos

`Categoria 1—N Producto` · `Usuario 1—N Orden` · `Orden 1—N DetalleOrden N—1 Producto` · `Usuario 1—N RefreshToken`

### Lógica de negocio destacada

- **Crear orden** (`POST /api/ordenes`): une ítems repetidos, valida stock, calcula precios y total **desde la base de datos** (no confía en los precios que envía el frontend), aplica el descuento de ofertas, descuenta el stock con bloqueo pesimista y guarda la orden con número `AT-######`. Todo en una transacción: si algo falla no se descuenta nada.
- **Registro público**: siempre crea usuarios con rol `CLIENTE`; solo un ADMINISTRADOR puede asignar otros roles.
- **Contraseñas** guardadas con BCrypt. Al editar un usuario, contraseña vacía = se conserva la actual.
- Reglas de validación: RUT sin puntos ni guión (8–9 caracteres), correo `@duoc.cl`, `@profesor.duoc.cl` o `@gmail.com`, contraseña de 4 a 10 caracteres.

## Seguridad y despliegue (AWS)

- **Secreto JWT**: en `application.properties` hay un valor por defecto solo para desarrollo local (la app avisa en el log si lo usas). Al desplegar **define `JWT_SECRET`** (mínimo 32 caracteres, aleatorio) y activa el perfil de producción:

```bash
export JWT_SECRET="un-texto-largo-y-aleatorio-de-al-menos-32-caracteres"
export CORS_ORIGINS="http://IP_PUBLICA:5173"
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

  Con el perfil `prod` la app **no arranca** si falta `JWT_SECRET` o `CORS_ORIGINS`, y la consola H2 queda desactivada.
- **Tokens**: access token de 15 minutos (`app.jwt.access-expiration-ms=900000`) y refresh token de 7 días que se revoca al cerrar sesión.

## Endpoints

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/registro` | Público |
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/refresh` | Público (requiere refresh token) |
| POST | `/api/auth/logout` | Público (requiere refresh token) |
| GET | `/api/auth/me` | Autenticado |
| GET | `/api/productos`, `/api/productos/{id}` | Público |
| GET | `/api/productos/criticos` | ADMINISTRADOR, VENDEDOR |
| POST / PUT / DELETE | `/api/productos[/{id}]` | ADMINISTRADOR |
| GET | `/api/categorias`, `/api/categorias/{id}` | Público |
| POST / PUT / DELETE | `/api/categorias[/{id}]` | ADMINISTRADOR |
| GET | `/api/usuarios`, `/api/usuarios/{id}` | ADMINISTRADOR, VENDEDOR |
| POST / PUT / DELETE | `/api/usuarios[/{id}]` | ADMINISTRADOR |
| POST | `/api/ordenes` | Público (visitante o cliente) |
| GET | `/api/ordenes` | ADMINISTRADOR, VENDEDOR |
| GET | `/api/ordenes/mis-ordenes` | Autenticado |
| GET | `/api/ordenes/{id}` | Personal o dueño de la orden |

Filtros de `GET /api/productos`: `?categoriaId=1` y `?enOferta=true`.

## Autenticación con JWT

1. `POST /api/auth/login` con `{ "correo": "...", "password": "..." }` devuelve `accessToken` (JWT, dura 15 min), `refreshToken` (dura 7 días, guardado en la base de datos) y los datos del usuario.
2. El frontend envía `Authorization: Bearer <accessToken>` en cada request protegida.
3. `JwtAuthFilter` valida la firma (HS256) y la fecha de vencimiento, carga al usuario con su **rol actual desde la base de datos** y deja la request autenticada. Si el token es inválido o falta, responde **401**; si el rol no alcanza, **403**.
4. Cuando el access token vence, el frontend llama a `POST /api/auth/refresh`. El refresh token es de **un solo uso (rotación)**: se invalida y se entrega uno nuevo.
5. `POST /api/auth/logout` elimina el refresh token.

Los permisos se declaran con `@PreAuthorize` en los controladores y con reglas por ruta en `SecurityConfig`.

### Probar en Swagger
1. En `POST /api/auth/login` usa **Try it out** y copia el `accessToken`.
2. Pulsa **Authorize** (arriba a la derecha), pega el token y confirma.
3. Los endpoints protegidos quedan habilitados según el rol del usuario.

## Configuración (`application.properties`)

| Propiedad | Descripción |
|---|---|
| `app.jwt.secret` | Clave de firma del JWT. En producción definir la variable de entorno `JWT_SECRET` (mínimo 32 caracteres) |
| `app.jwt.access-expiration-ms` | Duración del access token (por defecto 900000 = 15 min) |
| `app.jwt.refresh-expiration-days` | Duración del refresh token (por defecto 7) |
| `app.cors.allowed-origins` | Orígenes del frontend permitidos. Variable de entorno `CORS_ORIGINS` |

## Despliegue en AWS (EC2)

```bash
sudo dnf install -y java-21-amazon-corretto-headless
./mvnw clean package -DskipTests
export JWT_SECRET="una-clave-larga-y-aleatoria-de-mas-de-32-caracteres"
export CORS_ORIGINS="http://IP_PUBLICA:5173"
java -jar target/altoque-backend-1.0.0.jar
```

Abrir el puerto **8080** en el grupo de seguridad de la instancia. En producción se recomienda restringir el acceso, usar HTTPS y una base de datos gestionada (por ejemplo RDS).
