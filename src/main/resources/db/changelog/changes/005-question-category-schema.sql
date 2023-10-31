create table if not exists question_category (
    question_id bigint not null,
    category_id bigint not null,
    foreign key (question_id) references question(id),
    foreign key (category_id) references category(id)
);