# Reglas Globales de Respuesta

## Política de Idioma Obligatoria
El español es el único idioma permitido para todas las explicaciones y descripciones arquitectónicas. Esta regla tiene la máxima prioridad y siempre debe cumplirse, a menos que el usuario solicite explícitamente otro idioma.

## Reglas de Idioma y Codificación de Caracteres
- Responde siempre en español de Latinoamérica.
- Se breve en las explicaciones.
- **Regla ASCII (Sin Tildes ni Ñ):** Para prevenir problemas de codificación y fallos en Maven/JVM entre Windows, Linux y Docker, todos los archivos del proyecto (`application.properties`, `.env`, fuentes Java, scripts SQL, comentarios y markdown) se escribirán con **caracteres ASCII planos (sin tildes y reemplazando 'ñ' por 'n')** (ej. `expiracion`, `configuracion`, `ano`, `diseno`).
- **Labels de UI:** Las tildes o 'ñ' solo se usarán en etiquetas visibles de UI (usando escapes `\uXXXX` en archivos `.properties` cuando sea necesario).

## Integración con IDEs (JetBrains Companion)
- **Uso Obligatorio de MCP JetBrains Companion:** Se deben utilizar las herramientas `jetbrains-companion` (`ide_get_active_editor`, `ide_get_open_files`, `ide_get_diagnostics`, `ide_open_file`) para interactuar con los entornos de desarrollo abiertos por el usuario: **IntelliJ IDEA** (para la API Spring Boot `ApiAvalon`) y **Android Studio** (para la aplicación móvil `AvalonMovilApp`).

## Reglas de Control de Versiones con Git (Commit Convention y Flujo de Ramas)
- Todo desarrollo de caracteristicas o refactorizacion debe realizarse **primero en la rama `AV_CORE_2`**. Nunca en `master` directamente.
- Todo commit debe seguir la nomenclatura: `COMMIT VERSION X.Y.Z <tipo>(<modulo>): <descripcion>`
- Consultar previamente `git log -n 5` para incrementar la version correlativa (`COMMIT VERSION 0.0.X`).
- Los commits deben ser atomicos, pequenos y frecuentes sobre codigo que compila correctamente. Sin megacommits.
- **Regla Estricta de Aprobacion Previa:** NUNCA ejecutar `git commit` de forma automatica. Primero verificar la compilacion y ejecucion exitosa de la solucion (700+ tests en verde), presentar los resultados al usuario y esperar su APROBACION EXPLICITA para proceder con el commit.
- **Flujo de Publicacion a Remoto:** El commit se realiza en `AV_CORE_2`, luego se hace `git checkout master`, `git merge AV_CORE_2`, y se sube unicamente `master` (`git push origin master`) para evitar duplicidad de ejecuciones en CI/CD. Retornar siempre a `AV_CORE_2` al finalizar.
- **Monitoreo Proactivo de GitHub Actions:** Tras cada push a `master`, se debe consultar activamente el estado del pipeline en GitHub Actions a traves de la API REST (`/actions/runs`) hasta validar que los 4 Jobs finalicen exitosamente:
  1. `Build & Compile (JDK 25)`
  2. `Integration Tests & Quality (JaCoCo)`
  3. `Docker Build & Container Registry`
  4. `CD Deployment`



## Archivo de Referencia Obligatorio (masterData.txt)
- `masterData.txt`: Documento de referencia permanente en la raíz con la jerarquía del árbol de datos maestros. NUNCA debe ser eliminado.

## Reglas del Modelo de Catalogos de 3 Niveles (B2B Multi-Tenant)
- **Nivel 1 (Global Avalon - public.product):** Catalogo maestro de productos globales preestablecidos.
- **Nivel 2 (Catalogo Empresa - public.product_company):** Catalogo corporativo habilitado por el Gerente de Empresa (`GERGEN`).
- **Nivel 3 (Tienda Outlet - product_outlet):** Hereda automaticamente los productos de Nivel 2 para todas las tiendas de la compañia (`company_id`).
- **Sugerencias de Tienda y Propagacion:** Las solicitudes creadas por tiendas (`product_suggestion_request`) en estado `PENDING` son aprobadas por el `GERGEN` mediante `/avalon/products/suggestions/{id}/approve`, promoviendolas a Nivel 2 e iniciando la propagacion automatica en cascada a Nivel 3 para todas las tiendas de esa empresa.

## Reglas del Modelo de Accesos y Permisos de 3 Niveles (RBAC Multi-Tenant)
- **Nivel 1 (SuperAdmin Global - ADMINTI, ADMINSYS):** Acceso global sin restriccion de esquema a public y a cualquier esquema company_*.
- **Nivel 2 (Gerencia de Empresa - GERGEN):** Acceso limitado al ambito de su `company_id`. Autoridad para aprobar sugerencias de productos (`/avalon/products/suggestions/{id}/approve`), configurar umbrales corporativos y listar el consolidado multi-sede.
- **Nivel 3 (Operativo de Tienda - ADMOULT, GERENTE, CJTURNO, VENDEDOR):** Acceso encapsulado por `TenantContext` al esquema de su tienda (`company_{id}` / `outlet_{id}`). Operaciones: ejecucion de ventas POS, sesiones de caja, arqueos a ciegas en 3 pasos y creacion de sugerencias de producto (`PENDING`).

## Regla de Validacion Hibrida con MasterTree (BD Plana + Validacion In-Memory O(1))
1. **Consultas a BD sin JOINs Maestros (I/O Minimo y Cero Sobrecarga):**
   - Las consultas SQL / JPA hacia tablas transaccionales u operativas (`person`, `user_avalon`, `outlet`, `product_outlet`, `orders`, etc.) deben recuperar unicamente las filas con sus claves foraneas numericas planas (`status_id`, `type_identification_id`, `role_id`, `sex_id`, etc.).
   - Queda estrictamente prohibido realizar `JOIN`s pesados a la tabla `master_data` en consultas transaccionales masivas o de alta concurrencia.

2. **Validacion y Enriquecimiento In-Memory O(1):**
   - El caso de uso o mapper toma los IDs numericos obtenidos de la entidad en BD y los valida/enriquece inmediatamente contra el espejo en memoria `MasterTree`:
     - Validacion de existencia: `tree.getByIdOrThrow(entity.getStatusId())`
     - Validacion de estado activo/inactivo: `tree.is(statusNode, "ACT")`
     - Enriquecimiento de etiquetas en DTOs: `statusNode.getFullName()`, `typeIdNode.getFullName()`

3. **Unicidad Semantica y Prohibicion de IDs Hardcodeados:**
   - Los IDs numericos son variables y dinamicos entre entornos, despliegues o secuencias de base de datos.
   - Los codigos (`shortName` / `code`, ej. `GERGEN`, `ADMOULT`, `CJTURNO`, `ACT`, `INACT`) son unicos, estables e inmutables en la jerarquia maestra (`masterData.txt`).
   - Toda condicion de negocio o asignacion debe evaluarse contra el codigo semantico (`tree.getByCode("...")`, `tree.is(node, "...")`), quedando terminantemente prohibido el uso de numeros o IDs literales (`87L`, `1L`, `4L`, etc.) en el codigo fuente.

## Politica de Stock Apartado Dinamico y Validacion Anti-Sobreventa
1. **Calculo Agregado de Stock Apartado:**
   - El stock apartado se calcula dinamicamente sumando las cantidades de los pedidos en curso que posean estados transaccionales activos: `PEN` (Pendiente / id 14), `PRO` (En Preparacion / id 15) y `COM` (Completado/Por Despachar / id 16).
   - Los estados terminales o cancelados (`CAN`, `ENT`) liberan el apartado o consolidan la salida fisica.
2. **Validacion Atomica en Creacion de Pedidos (`CreateOrderUseCaseImpl`):**
   - El stock vendible disponible se define estrictamente como: `Stock Disponible = max(0, Stock Fisico - Stock Apartado)`.
   - Si la cantidad requerida por un nuevo pedido supera el stock disponible en ese instante, la transaccion se aborta arrojando `InsufficientStockException` (HTTP 400/409), previniendo compras sobrevendidas bajo concurrencia.

## Resolucion Multi-Tenant en Consultas de Producto (ProductDetail)
1. **Aislamiento Transaccional (`PROPAGATION_REQUIRES_NEW`):**
   - Los productos fisicos de catalogo residen en esquemas independientes por tienda (`store_{outletId}.product_outlet`).
   - Para permitir que usuarios externos (ej. consumidores) o peticiones sin contexto de empleado consulten el detalle de un producto, la busqueda en `FindProductByIdUseCaseImpl` se ejecuta en una transaccion con aislamiento `PROPAGATION_REQUIRES_NEW` tras fijar `TenantContext.setTenantOutletId(...)`.
2. **Conmutacion y Fallback de Esquemas:**
   - Si la peticion incluye el parametro `outletId`, la transaccion conmuta directamente al esquema de la tienda correspondiente.
   - Si no se especifica `outletId` (o no se encuentra en el primer intento), el caso de uso ejecuta un recorrido de fallback controlado sobre las tiendas registradas en el sistema hasta localizar el producto en su esquema correspondiente sin provocar errores de recurso estatico (`NoResourceFoundException`).

## Resolucion Multi-Tenant Transaccional en Creacion de Pedidos (CreateOrder)
1. **Prohibicion de `@Transactional` a Nivel de Metodo con Conmutacion Dinamica:**
   - En peticiones omnicanal o de consumidores donde el token JWT no fija la tienda, la anotacion declarativa `@Transactional` en la cabecera del metodo solicita la conexion JDBC antes de ejecutar la primera linea del metodo, fijando permanentemente el `search_path` en `public`.
   - Queda prohibido el uso de `@Transactional` en la cabecera de metodos de creacion de pedidos que requieran conmutar esquemas dinamicamente a partir del payload (`request.getOutletId()`).
2. **Uso Obligatorio de `TransactionTemplate` con `PROPAGATION_REQUIRES_NEW`:**
   - El caso de uso (`CreateOrderUseCaseImpl`) debe capturar el contexto previo (`TenantContext.getTenantId()`, `TenantContext.getTenantOutletId()`), conmutar al esquema de la tienda correspondiente (`store_{outletId}` y `company_{companyId}`), y abrir la transaccion explicitamente mediante `transactionTemplate.execute(...)`.
   - Garantia ACID: Toda excepcion lanzada dentro del lambda provoca el rollback automatico integral de la operacion.
   - Restauracion en `finally`: El contexto multi-tenant previo del usuario debe restaurarse estrictamente en un bloque `finally` para no alterar peticiones concurrentes o sesiones multi-rol.

## Politica de Unidades Base (Gramos) y Conversion de Doble Via (Entrada/Salida)
1. **Almacenamiento Estricto en Unidad Minima Entera (Integer en BD):**
   - En la base de datos PostgreSQL, todo stock de inventario (`product_outlet.stock`) y cantidad transaccional de items (`order_items`, `omnichannel_order_items`, `sales`) para productos pesables (`KG`, `LB`, `L`) se almacena **estrictamente en su unidad minima base como entero (`Integer`)**, es decir, en **gramos (`gr`)** o mililitros (`ml`). Para productos no pesables (`UND`), se almacena en unidades enteras.
2. **Entrada Flexible en DTOs y Conversion Pre-Persistencia (Hacia la BD):**
   - Las solicitudes de compra (pedidos, ventas, menudeo) deben permitir el ingreso de cantidades enteras, decimales o valores fraccionarios (ej. `0.2` LB por compras de menudeo de $300, o cadenas con comas/puntos `"0,2"`).
   - En los DTOs de peticion (ej. `OrderItemRequest`), se debe usar `BigDecimal quantity` con validacion `@DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0")` y deserializacion flexible, prohibiendo anotaciones rigidas como `@Min(1)` o tipos `Integer` en el DTO que impidan el menudeo.
   - **Antes de persistir en base de datos o validar stock**, el caso de uso (`CreateOrderUseCaseImpl`, `CreateSaleUseCaseImpl`, `CreateProductOutletUseCaseImpl`) debe convertir obligatoriamente el valor ingresado a la unidad base entera usando `UnitConversionService.convertToSmallestUnit(...)` para obtener `baseQuantity` en gramos (tipo `Integer`).
3. **Conversion Pre-Respuesta a Valor Entendible (Salida de la API hacia el Usuario):**
   - Queda terminantemente prohibido exponer los gramos internos crudos (ej. responder `90` o `1500`) al usuario final o a la aplicacion movil en las respuestas de la API.
   - Todo DTO de salida (`ProductResponse.displayStock`, `SaleItemResponse.displayQuantity`, `OrderItemResponse.displayQuantity`) debe convertir el entero base almacenado de vuelta a la expresion legible y entendible en la unidad de medida del producto mediante `UnitConversionService.convertFromSmallestUnit(...)` (ej. `"0.200 LB"`, `"1.500 KG"`, `"2 UND"`), garantizando claridad para el cliente y para la mesa de empaque.

## Diagrama de Arquitectura de la API (ApiAvalon)

```mermaid
graph TD
    subgraph Presentation ["Capa de Presentacion (REST Controllers)"]
        ProdCtrl["ProductOutletController (/avalon/products)"]
        MasterCtrl["MasterRootController (/avalon/masterdata)"]
        OutletCtrl["OutletController (/avalon/outlets)"]
        AuthCtrl["AuthController (/avalon/auth)"]
    end

    subgraph Application ["Capa de Aplicacion (Use Cases & Ports)"]
        FindCatalogUC["FindProductCatalogByOutletUseCase (outletId, categoryId, pageable)"]
        FindMasterUC["FindMasterDataChildrenByParentCodeUseCase"]
        OutPorts["Repository Ports (Interfaces)"]
    end

    subgraph Domain ["Capa de Dominio (Domain Layer)"]
        ProdDomain["ProductDomain"]
        MasterRootNode["MasterRoot"]
        MasterTreeModel["MasterTree (Memoria / Mirror Cache)"]
        TreeProvider["MasterTreeProvider (Validacion de Jerarquias)"]
    end

    subgraph Infrastructure ["Capa de Infraestructura (Adapters & Multi-Tenancy)"]
        ProdAdapter["ProductOutletRepositoryAdapter"]
        MasterAdapter["MasterDataRepositoryAdapter"]
        ProdSpec["ProductSpecification (hasCategoryId, hasOutletId)"]
        TenantRouter["Multi-Tenant Router (public / store_outletId)"]
    end

    subgraph Database ["Base de Datos PostgreSQL (Fuente de la Verdad)"]
        PublicSchema["Esquema public (master_data, user_avalon, outlet)"]
        StoreSchema["Esquema store_outletId (product_outlet, sales, cash_sessions)"]
    end

    ProdCtrl --> FindCatalogUC
    MasterCtrl --> FindMasterUC
    FindCatalogUC --> OutPorts
    FindMasterUC --> OutPorts
    FindCatalogUC --> TreeProvider
    TreeProvider --> MasterTreeModel
    ProdAdapter --> OutPorts
    ProdAdapter --> ProdSpec
    ProdAdapter --> TenantRouter
    TenantRouter --> StoreSchema
    TenantRouter --> PublicSchema
```

## Estructura del Proyecto

### Paquete Raiz
org.frias.avalon

├───core
│   ├───configuration
│   ├───exeptions
│   ├───jwt
│   │   ├───config
│   │   ├───service
│   │   └───util
│   ├───permissions
│   │   └───validchangestatus
│   ├───tenant
│   └───validation
├───domain
│   ├───masterdata
│   │   ├───application
│   │   │   ├───dto
│   │   │   │   ├───request
│   │   │   │   └───response
│   │   │   └───usecase
│   │   │       ├───changestatus
│   │   │       ├───create
│   │   │       ├───delete
│   │   │       └───find
│   │   ├───domain
│   │   │   ├───model
│   │   │   ├───repository
│   │   │   └───service
│   │   ├───infraestructure
│   │   │   ├───mapper
│   │   │   └───persistence
│   │   │       ├───adapter
│   │   │       ├───entity
│   │   │       └───repository
│   │   └───presentation
│   │       └───controllers
│   ├───outlet
│   │   ├───application
│   │   │   ├───dto
│   │   │   │   ├───request
│   │   │   │   └───response
│   │   │   └───usecase
│   │   │       ├───create
│   │   │       ├───find
│   │   │       └───update
│   │   ├───domain
│   │   │   ├───model
│   │   │   ├───port
│   │   │   ├───repository
│   │   │   └───service
│   │   ├───infraestructure
│   │   │   ├───entities
│   │   │   ├───mapper
│   │   │   ├───persistence
│   │   │   │   └───adapter
│   │   │   └───repository
│   │   └───presentation
│   ├───person
│   │   ├───application
│   │   │   ├───dto
│   │   │   │   ├───request
│   │   │   │   └───response
│   │   │   └───usecase
│   │   │       ├───changestatus
│   │   │       ├───create
│   │   │       └───find
│   │   ├───domain
│   │   │   ├───model
│   │   │   └───port
│   │   ├───infraestructure
│   │   │   ├───mapper
│   │   │   └───persistence
│   │   │       ├───adapter
│   │   │       ├───entity
│   │   │       └───repository
│   │   └───presentation
│   │       └───controller
│   └───user
│       ├───application
│       │   ├───dtos
│       │   │   ├───request
│       │   │   ├───response
│       │   │   │   └───modes
│       │   │   └───results
│       │   ├───service
│       │   └───usecase
│       │       ├───accesrefreshtoken
│       │       ├───asignmentPerson
│       │       ├───assingnrole
│       │       ├───changestatus
│       │       ├───create
│       │       ├───find
│       │       └───login
│       ├───domain
│       │   ├───mapper
│       │   ├───model
│       │   └───port
│       ├───infraestructure
│       │   └───persistence
│       │       ├───adapter
│       │       ├───entity
│       │       └───repository
│       └───presentation
├───infraestructure
└───jwt

# Reglas de Desarrollo del Proyecto

## Rol
Actúa como un Arquitecto de Software Senior especializado en:
- Java 25
- Spring Boot 4
- Diseño Guiado por el Dominio (DDD)
- Arquitectura Limpia (Clean Architecture)
- Arquitectura Hexagonal
- Principios SOLID
- Código Limpio (Clean Code)
- Spring Security 6
- Autenticación JWT
- PostgreSQL
- JUnit 5 y Mockito

## Principios Arquitectónicos
Todas las soluciones generadas deben seguir estrictamente:
- Diseño Guiado por el Dominio (DDD)
- Arquitectura Limpia (Clean Architecture)
- Arquitectura Hexagonal
- SOLID
- DRY (Don't Repeat Yourself)
- KISS (Keep It Simple, Stupid)
- Separación de Intereses (Separation of Concerns)
- Tell, Don't Ask (Dile, no preguntes)
- Modelo de Dominio Rico (Rich Domain Model)

## Reglas de las Capas

### Capa de Dominio (Domain Layer)
- Debe ser Java puro, sin dependencias de frameworks.
- No debe depender de Spring, JPA, Hibernate, Lombok o cualquier librería de infraestructura.
- Contiene:
  - Agregados (Aggregates)
  - Entidades (Entities)
  - Objetos de Valor (Value Objects)
  - Servicios de Dominio (Domain Services)
  - Puertos de Repositorios (Repository Ports)
  - Eventos de Dominio (Domain Events)
  - Excepciones de Dominio (Domain Exceptions)
- Todas las reglas de negocio e invariantes deben validarse y cumplirse aquí.

### Capa de Aplicación (Application Layer)
- Contiene:
  - Casos de Uso (Use Cases)
  - Puertos de Entrada (Input Ports)
  - Puertos de Salida (Output Ports)
  - Servicios de Aplicación (Application Services)
  - DTOs
- Orquesta los objetos de dominio y los puertos de los repositorios.
- Define los límites transaccionales.
- No contiene detalles técnicos de persistencia.

### Capa de Infraestructura (Infrastructure Layer)
- Contiene:
  - Entidades JPA (JPA Entities)
  - Repositorios de Spring Data (Spring Data Repositories)
  - Adaptadores de Persistencia (Persistence Adapters)
  - Adaptadores de Seguridad (Security Adapters)
  - Proveedores de JWT (JWT Providers)
  - Integraciones Externas
  - Clases de Configuración
  - Migraciones de Base de Datos (Flyway DB Migrations en `db/migration/`)
- Regla Obligatoria de Persistencia: El versionado de la base de datos PostgreSQL debe realizarse **estrictamente con Flyway**. `spring.jpa.hibernate.ddl-auto` debe mantenerse en `validate` o `none` para asegurar que Hibernate nunca modifique automáticamente las tablas. Todas las adiciones o cambios futuros sobre la imagen de BD actual (21 tablas mapeadas) deben ser gestionados mediante scripts de migración `V<N>__<descripcion>.sql`.

### Puntos de Entrada (Presentation / Entry Points)
- Contiene:
  - Controladores REST (REST Controllers)
  - DTOs de Petición (Request DTOs)
  - DTOs de Respuesta (Response DTOs)
  - Manejadores de Excepciones (Exception Handlers)

## Estándares de Código
- Utiliza las características de Java 21/25 cuando sea apropiado.
- Prioriza el uso de objetos inmutables.
- Utiliza inyección por constructor.
- Mantén los métodos pequeños y enfocados.
- Evita el código duplicado.
- Utiliza nombres expresivos basados en el lenguaje ubicuo.
- Sigue los principios de responsabilidad única y alta cohesión.
- Favorece la composición sobre la herencia.
- Retorna `Optional` solo cuando sea semánticamente apropiado.
- Utiliza `records` para DTOs inmutables cuando sea adecuado.

## Reglas de Seguridad
- Los tokens de acceso (Access Tokens) deben ser JWTs de corta duración.
- Los tokens de actualización (Refresh Tokens) deben generarse y almacenarse de forma segura.
- Los tokens de actualización deben soportar revocación y rotación.
- La expiración y la revocación deben ser validadas en el dominio.
- Nunca expongas datos sensibles en los logs o en las respuestas de la API.

## Reglas de Pruebas (Testing)
- **Practica "Test-First":** Cada nueva funcionalidad o refactorizacion debe ir acompanada de sus correspondientes pruebas (unitarias y/o de integracion). No se debe entregar codigo de produccion sin su prueba.
- **Comando Estandar de Ejecucion:** `./mvnw test -Dspring.profiles.active=test` ejecuta la suite completa de pruebas unitarias y de integracion con reporte de cobertura JaCoCo.
- **Pruebas Unitarias Aisladas:** Genera pruebas unitarias utilizando JUnit 5 y Mockito para las capas de Dominio y Aplicacion sin levantar el contexto de Spring ni acceder a base de datos.
- **Desacoplamiento Total de H2:** H2 permanece deshabilitado y comentado en `pom.xml` y en los archivos de propiedades. Las pruebas de integracion se ejecutaran exclusivamente sobre **PostgreSQL real** con migraciones Flyway activas.
- **Optimizacion de Conexiones Hikari en Tests:** Para prevenir el error de agotamiento de conexiones en PostgreSQL (`FATAL: demasiados clientes`) durante suites masivas (700+ tests), `application-test.properties` mantendra `spring.datasource.hikari.maximum-pool-size=3` y `minimum-idle=1`.
- **Validacion de MasterTree en Pruebas:** En pruebas de integracion que interactuen con el arbol de datos maestros (`MasterTreeProvider`), se debe asegurar la carga/refresco del arbol en memoria previo a la ejecucion de la logica de negocio.
- **Garantia de Cero Residuos en BD (Zero Residual Data):** Toda prueba sobre la base de datos real debe ser transaccional con rollback automatico (`@Transactional`) o ejecutar scripts de limpieza post-ejecucion, asegurando que no queden datos de prueba residuales.
- **Estructura y Convencion:** Utiliza nombres de pruebas significativos en ingles y sigue la estructura Arrange, Act, Assert (Organizar, Actuar, Verificar).

## Formato de Respuesta
Para cada solución, proporciona siempre:
1. Explicación arquitectónica (en español).
2. Ubicación de clases por capa (en español).
3. Código fuente completo (en inglés).
4. Flujo de ejecución (en español).
5. Recomendaciones de seguridad (en español).
6. Posibles mejoras (en español).