# Manuales

PDF estáticos que el backend sirve desde `/api/v1/manuales`. Viajan dentro del JAR, así que
publicar o corregir un manual requiere build y deploy.

## Estructura

```
manuales/
├── tecnicos/    manuales técnicos (Auth0, Azure, Telegram)
└── usuario/     manuales de usuario final (uno por módulo)
```

## Publicar o actualizar un manual

1. Dejar el PDF en la carpeta que corresponda, con **exactamente** el nombre declarado en
   `Manual.java` (por ejemplo `manual-usuario-clientes.pdf`).
2. En la constante del enum `Manual`, completar `version` y `fechaActualizacion`.

Los dos pasos son obligatorios: `ManualCatalogoTest` falla si hay un PDF sin metadata
declarada o metadata declarada sin PDF, para que no queden versiones desactualizadas.

Un manual declarado en el enum pero todavía sin PDF aparece en el listado con
`disponible: false`, y pedir su descarga devuelve `404 MANUAL_NO_DISPONIBLE`.

## Alta de un manual nuevo

Agregar una constante en `Manual.java` con su categoría, clave (la que va en la URL),
título visible y nombre de archivo. No hace falta tocar nada más.
