# Despliegue GRATUITO con Railway

## Por que Railway?
- Completamente GRATIS para tu aplicacion
- Deploy automatico desde GitHub
- PostgreSQL incluida sin costo
- SSL y dominio automaticos
- Zero config - Railway detecta todo automaticamente

## PASOS EXACTOS (10 minutos total)

### PASO 1: Preparar el Codigo (2 minutos)

```
git add .
git commit -m "Configure for Railway deployment"
git push origin main
```

### PASO 2: Crear Cuenta en Railway (2 minutos)

1. Ir a: https://railway.app
2. Sign up with GitHub (usar tu cuenta de GitHub)
3. Autorizar Railway para acceder a tus repositorios

### PASO 3: Importar Proyecto (3 minutos)

1. Click "New Project"
2. Deploy from GitHub repo
3. Seleccionar: Karen-H/2023-messismo
4. Railway detectara automaticamente:
   - Spring Boot backend
   - React frontend
   - Necesidad de PostgreSQL

### PASO 4: Configurar Base de Datos (1 minuto)

1. Railway preguntara si quieres PostgreSQL
2. Click "Add PostgreSQL" 
3. Railway creara automaticamente la base de datos

### PASO 5: Configurar Variables de Entorno (2 minutos)

En la dashboard de Railway:

Backend:
```
JWT_SECRET=mi-super-secreto-jwt-de-32-caracteres-railway
SPRING_PROFILES_ACTIVE=railway
```

Frontend:
```
REACT_APP_API_URL_PROD=https://tu-backend-url.up.railway.app
```

## LO QUE RAILWAY HACE AUTOMATICAMENTE

### Backend (Spring Boot):
- Detecta pom.xml y compila con Maven
- Ejecuta java -jar target/*.jar
- Conecta automaticamente a PostgreSQL
- Asigna dominio: https://tu-backend.up.railway.app

### Frontend (React):
- Detecta package.json y hace npm install
- Ejecuta npm run build y npm start
- Asigna dominio: https://tu-frontend.up.railway.app

### Base de Datos:
- PostgreSQL 15 completamente gestionada
- Backups automaticos
- Variables de entorno automaticas (DATABASE_URL)

## Tu Aplicacion en Railway

### Configuracion Incluida:
- Usuario Admin: admin@mail.com / Password1
- Productos: Todos los productos del bar cargados
- Usuarios: Empleados y clientes de muestra
- Ordenes: Historial completo con puntos
- Goals: Objetivos preconfigurados
- Benefits: Beneficios con sistema de puntos

### URLs Finales:
```
Frontend: https://messismo-frontend.up.railway.app
Backend API: https://messismo-backend.up.railway.app
Admin Panel: https://messismo-frontend.up.railway.app (login con admin)
```

## Costos Railway

| Recurso | Costo | Limite Gratuito |
|---------|--------|-----------------|
| Backend | $0 | 512MB RAM, 1GB storage |
| Frontend | $0 | Ilimitado static hosting |
| PostgreSQL | $0 | 100MB storage |
| Bandwidth | $0 | 100GB/mes |
| Builds | $0 | 500 horas/mes |

Total: $0/mes para tu aplicacion

## Troubleshooting

### Si el Backend no arranca:
```
railway logs --service backend
```

### Si el Frontend no conecta:
1. Verificar que REACT_APP_API_URL_PROD apunte al backend
2. Verificar CORS en el backend

### Si la DB no conecta:
- Railway asigna automaticamente DATABASE_URL
- Verificar que application-railway.properties este configurado

## Despues del Deploy

### 1. Verificar Funcionamiento:
- Frontend carga correctamente
- Login con admin@mail.com funciona
- Productos se muestran
- Se pueden crear ordenes

### 2. Configurar Dominio Personalizado (Opcional):
1. Railway Settings - Custom Domain
2. Agregar tu dominio
3. Configurar DNS segun las instrucciones

### 3. Monitoreo:
- Railway Dashboard muestra CPU, RAM, y logs
- Metrics en tiempo real
- Alertas automaticas si algo falla

## Acceso a tu Aplicacion

Una vez desplegado:

1. Admin Dashboard: Gestion completa del bar
2. Employee Portal: Tomar ordenes y gestionar productos  
3. Client Portal: Ver ordenes y puntos acumulados
4. Manager Tools: Reportes y configuracion

## Listo!

Tu aplicacion Messismo estara disponible 24/7 de forma gratuita en Railway

Tiempo total: 10 minutos
Costo: $0
Mantenimiento: Automatico