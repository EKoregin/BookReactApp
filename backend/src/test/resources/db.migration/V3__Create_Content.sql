create table if not exists content
(
    id    BIGSERIAL PRIMARY KEY,
    media_type varchar(32),
    size int not null,
    content bytea
);