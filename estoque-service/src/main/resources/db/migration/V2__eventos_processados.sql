create table eventos_processados (
    event_id uuid primary key,
    processed_at timestamptz not null default now()
);
