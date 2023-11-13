create table if not exists place (
    id bigserial primary key,
    name varchar(255) not null,
    adding_date timestamp not null default now(),
    page_link varchar(255),
    longitude decimal,
    latitude decimal
);
create table if not exists place_image (
    place_id bigint not null,
    image_id bigint not null,
    foreign key (place_id) references place (id),
    foreign key (image_id) references image (id)
);
create table if not exists place_category (
    place_id bigint not null,
    category_id bigint not null,
    foreign key (place_id) references place (id),
    foreign key (category_id) references category (id)
);