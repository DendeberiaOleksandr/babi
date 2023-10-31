create table if not exists question_tree (
    question_id bigint not null,
    previous_question_id bigint,
    foreign key (question_id) references question (id),
    foreign key (previous_question_id) references question (id)
);