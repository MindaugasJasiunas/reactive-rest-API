CREATE SEQUENCE if NOT EXISTS users_sequence start 1 increment 1;

CREATE TABLE if NOT EXISTS user_entity
(
--     id                 integer             SERIAL PRIMARY KEY,
    id                 integer             NOT NULL DEFAULT nextval('users_sequence'),
    public_id          uuid         NOT NULL,
    username           varchar (255),
    password           varchar (255),
    first_name         varchar (255),
    last_name          varchar (255),
    created_date       timestamp,
    last_modified_date timestamp
);

ALTER SEQUENCE users_sequence OWNED BY user_entity.id;