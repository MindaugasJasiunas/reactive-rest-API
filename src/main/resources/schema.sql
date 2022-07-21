CREATE SEQUENCE if NOT EXISTS users_sequence start 1 increment 1;
CREATE SEQUENCE if NOT EXISTS roles_sequence start 1 increment 1;

CREATE TABLE if NOT EXISTS role_entity
(
    id                          integer NOT NULL DEFAULT nextval('roles_sequence'),
    role_name                   varchar (255),
    PRIMARY KEY(id)
);

ALTER SEQUENCE roles_sequence OWNED BY role_entity.id;

CREATE TABLE if NOT EXISTS user_entity
(
--     id                 integer             SERIAL PRIMARY KEY,
    id                          integer       NOT NULL DEFAULT nextval('users_sequence'),
    public_id                   uuid          NOT NULL,
    username                    varchar (255) NOT NULL,
    password                    varchar (255) NOT NULL,
    first_name                  varchar (255) NOT NULL,
    last_name                   varchar (255) NOT NULL,
    role_id                     int references role_entity(id) NOT NULL,
    created_date                timestamp,
    last_modified_date          timestamp,
    is_account_non_expired      boolean,
    is_account_non_locked       boolean,
    is_credentials_non_expired  boolean,
    is_enabled                  boolean,
    PRIMARY KEY(id),
    UNIQUE(public_id, username)
);

ALTER SEQUENCE users_sequence OWNED BY user_entity.id;

CREATE TABLE if NOT EXISTS authority
(
    id                          SERIAL PRIMARY KEY,
    authority_name              varchar (255)
);

CREATE TABLE if NOT EXISTS role_authority
(
    role_id                          int references role_entity(id),
    authority_id                     int references authority(id),
    PRIMARY KEY(role_id, authority_id)
);

CREATE TABLE if NOT EXISTS password_reset
(
    username          varchar (255) NOT NULL,
    link              varchar (255) NOT NULL,
    email             varchar (255),
    PRIMARY KEY(username, link),
    UNIQUE(username, link, email)
);