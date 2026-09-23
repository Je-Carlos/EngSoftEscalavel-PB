create table eventos_outbox (
    event_id uuid primary key,
    aggregate_id bigint not null,
    routing_key varchar(100) not null,
    occurred_at timestamptz not null,
    payload text not null,
    published_at timestamptz
);
create index ix_eventos_outbox_pendentes on eventos_outbox (occurred_at) where published_at is null;

create table eventos_auditoria (
    event_id uuid primary key,
    aggregate_id bigint not null,
    occurred_at timestamptz not null,
    payload text not null,
    received_at timestamptz not null default now()
);
create index ix_eventos_auditoria_comanda on eventos_auditoria (aggregate_id, occurred_at);
