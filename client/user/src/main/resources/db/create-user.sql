-- MASTER 계정
INSERT INTO users.p_user (
    user_id, username, password, email, nickname, alarm_consent, user_role, approved, created_at, updated_at, deleted_status
)
VALUES (
           '00000000-0000-0000-0000-000000000001',
           'master',
           '$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu',
           'master@example.com',
           '마스터계정',
           true,
           'MASTER',
           true,
           now(),
           now(),
           false
       )
    ON CONFLICT (username) DO NOTHING;

-- SELLER 계정
INSERT INTO users.p_user (
    user_id, username, password, email, nickname, alarm_consent, user_role, approved, created_at, updated_at, deleted_status
)
VALUES (
           '00000000-0000-0000-0000-000000000002',
           'seller',
           '$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu',
           'seller@example.com',
           '판매자계정',
           true,
           'SELLER',
           true,
           now(),
           now(),
           false
       )
    ON CONFLICT (username) DO NOTHING;

-- SHOW_HOST 계정
INSERT INTO users.p_user (
    user_id, username, password, email, nickname, alarm_consent, user_role, approved, created_at, updated_at, deleted_status
)
VALUES (
           '00000000-0000-0000-0000-000000000003',
           'showhost',
           '$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu',
           'showhost@example.com',
           '쇼호스트계정',
           true,
           'SHOW_HOST',
           true,
           now(),
           now(),
           false
       )
    ON CONFLICT (username) DO NOTHING;

-- CUSTOMER 계정
INSERT INTO users.p_user (
    user_id, username, password, email, nickname, alarm_consent, user_role, approved, created_at, updated_at, deleted_status
)
VALUES (
           '00000000-0000-0000-0000-000000000004',
           'customer',
           '$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu',
           'customer@example.com',
           '일반사용자',
           true,
           'CUSTOMER',
           true,
           now(),
           now(),
           false
       )
    ON CONFLICT (username) DO NOTHING;
