-- Esquema de la línea base (diseño detallado §4.1, ER revisado por el experto en persistencia el 9-nov).
-- La auditoría no vive aquí: va al Registro de auditoría (ADR-012).

create table usuario (
    id              uuid primary key,              -- oid del Identity Provider (no se genera aquí)
    nombre          text        not null,
    correo          text        not null,
    telefono        text,                          -- sin teléfono se omite el canal de WhatsApp
    rol             text        not null check (rol in ('ADMINISTRADOR_SISTEMA', 'RECTOR', 'COORDINADOR_ACADEMICO',
                                                       'COORDINADOR_FACULTAD', 'COORDINADOR_PROGRAMA', 'DOCENTE',
                                                       'ESTUDIANTE')),
    actualizado_en  timestamptz not null
);

create table categoria (
    id      integer primary key,
    nombre  text    not null unique,
    activa  boolean not null default true
);

create table kudo (
    id               uuid primary key,             -- UUID v7, ordenado en el tiempo
    autor_id         uuid        not null references usuario (id),
    destinatario_id  uuid        not null references usuario (id),
    categoria_id     integer     not null references categoria (id),
    contenido        text        not null,
    fecha            timestamptz not null,
    constraint kudo_no_autorreconocimiento check (autor_id <> destinatario_id)
);

create table adjunto (
    id             uuid primary key,
    kudo_id        uuid   not null references kudo (id),
    clave_archivo  text   not null unique,         -- nombre en el servidor, no el del cliente
    tipo           text   not null,
    tamano         bigint not null check (tamano > 0)
);

-- Sin FK: el relevo lo lee sin tocar las demás tablas; el payload lleva el kudoId.
create table outbox_evento (
    id          uuid primary key,                  -- id del evento (idempotencia, ADR-009)
    tipo        text        not null,
    payload     jsonb       not null,
    creado_en   timestamptz not null,
    enviado_en  timestamptz
);

create index kudo_feed_idx on kudo (fecha desc, id desc);
create index kudo_autor_idx on kudo (autor_id);
create index kudo_destinatario_idx on kudo (destinatario_id);
create index kudo_categoria_idx on kudo (categoria_id);
create index adjunto_kudo_idx on adjunto (kudo_id);
create index outbox_pendientes_idx on outbox_evento (creado_en) where enviado_en is null;
