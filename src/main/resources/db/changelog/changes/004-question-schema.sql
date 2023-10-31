create table if not exists question (
    id bigserial primary key,
    text varchar(255) not null,
    icon_id bigint,
    foreign key (icon_id) references image (id)
);