create table if not exists image (
    id bigserial primary key,
    content bytea not null,
    created_at timestamp not null default now()
);