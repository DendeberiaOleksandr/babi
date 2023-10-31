create table if not exists usr (
    id bigserial primary key,
    username varchar(255),
    email varchar(255) not null unique,
    email_verified bool not null default false,
    is_service_terms_accepted bool not null default false,
    password varchar(255) not null,
    provider_id varchar(255),
    registration_date timestamp not null default CURRENT_TIMESTAMP,
    last_modified_date timestamp not null default CURRENT_TIMESTAMP
);