INSERT INTO role_entity (id, role_name) VALUES (1, 'ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO role_entity (id, role_name) VALUES (2, 'ROLE_MANAGER') ON CONFLICT DO NOTHING;
INSERT INTO role_entity (id, role_name) VALUES (3, 'ROLE_ADMIN') ON CONFLICT DO NOTHING;

ALTER SEQUENCE roles_sequence RESTART WITH 4;

INSERT INTO authority (id, authority_name) VALUES (1, 'user:create') ON CONFLICT DO NOTHING;
INSERT INTO authority (id, authority_name) VALUES (2, 'user:read') ON CONFLICT DO NOTHING;
INSERT INTO authority (id, authority_name) VALUES (3, 'user:update') ON CONFLICT DO NOTHING;
INSERT INTO authority (id, authority_name) VALUES (4, 'user:delete') ON CONFLICT DO NOTHING;

INSERT INTO role_authority (role_id, authority_id) VALUES (1, 2) ON CONFLICT DO NOTHING;

INSERT INTO role_authority (role_id, authority_id) VALUES (2, 2) ON CONFLICT DO NOTHING;
INSERT INTO role_authority (role_id, authority_id) VALUES (2, 3) ON CONFLICT DO NOTHING;

INSERT INTO role_authority (role_id, authority_id) VALUES (3, 1) ON CONFLICT DO NOTHING;
INSERT INTO role_authority (role_id, authority_id) VALUES (3, 2) ON CONFLICT DO NOTHING;
INSERT INTO role_authority (role_id, authority_id) VALUES (3, 3) ON CONFLICT DO NOTHING;
INSERT INTO role_authority (role_id, authority_id) VALUES (3, 4) ON CONFLICT DO NOTHING;

-- INSERT INTO user_entity (id, public_id, username, password, first_name, last_name, role_id, created_date, last_modified_date, is_account_non_expired, is_account_non_locked, is_credentials_non_expired, is_enabled)
-- VALUES (1, 'd51f1234-3d7d-4100-8846-468f38e14a4f', 'johnd', '$2a$12$L61SNM2qG1YPyD4.bG02OOUBO.oW8QOT51CwMlgQ/7HibB8bhXXuO', 'John', 'Doe', 1, '2022-07-13 12:00:00.000000', '2022-07-13 12:00:00.000000', true, true, true, true) ON CONFLICT DO NOTHING;

-- (!ERROR!) "No parameters have been bound" when saving hashed password. When updating in DB console - no errors.
-- UPDATE user_entity
-- SET password = '$2a$12$L61SNM2qG1YPyD4.bG02OOUBO.oW8QOT51CwMlgQ/7HibB8bhXXuO'
-- WHERE id = 1;

-- INSERT INTO user_entity (id, public_id, username, password, first_name, last_name, role_id, created_date, last_modified_date, is_account_non_expired, is_account_non_locked, is_credentials_non_expired, is_enabled)
-- VALUES (2, '07db1b55-714b-432e-af3b-5ad587a359e0', 'janed', '$2a$12$JVpKQOwi6gxeVlp6oUWlQuObzeyM8SFfWJtCxTOIJgn/TT4PCTle6', 'Jane', 'Doe', 2, '2022-07-13 12:00:00.000000', '2022-07-13 12:00:00.000000', true, true, true, true)
-- ON CONFLICT DO NOTHING;

-- INSERT INTO user_entity (id, public_id, username, password, first_name, last_name, role_id, created_date, last_modified_date, is_account_non_expired, is_account_non_locked, is_credentials_non_expired, is_enabled)
-- VALUES (3, 'd4c1ca44-e996-4ed9-80c9-d5d0a1f4b2ff', 'admin', '$2a$12$TpOiIo8Th4AwND6vxCgs.e.QNOka.m4hux9hVH4iz1DxIiUBrHzXe', 'Tom', 'Doe', 3, '2022-07-13 12:00:00.000000', '2022-07-13 12:00:00.000000', true, true, true, true)
-- ON CONFLICT DO NOTHING;

ALTER SEQUENCE users_sequence RESTART WITH 4;
