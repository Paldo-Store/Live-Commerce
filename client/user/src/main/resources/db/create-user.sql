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
       ),
       ('fd842b12-8f16-4171-b634-19dcb25c5f94','customer4','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer4@example.com','고객4',true,'CUSTOMER',true,now(),now(),false),
       ('9c8bec72-767e-44b3-bfe7-a5cbbb3bab91','customer5','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer5@example.com','고객5',true,'CUSTOMER',true,now(),now(),false),
       ('fbe89627-f3d7-42cf-8a67-1ffe9f0e97b3','customer6','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer6@example.com','고객6',true,'CUSTOMER',true,now(),now(),false),
       ('bc2bb62e-f767-4bb9-9e95-265d1559973c','customer7','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer7@example.com','고객7',true,'CUSTOMER',true,now(),now(),false),
       ('44e1a41c-8d3c-4e0f-9dc8-5109022520e2','customer8','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer8@example.com','고객8',true,'CUSTOMER',true,now(),now(),false),
       ('62d9585c-91c0-4cae-81fe-75a64c7e66e7','customer9','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer9@example.com','고객9',true,'CUSTOMER',true,now(),now(),false),
       ('49e1d005-2745-454e-89d9-76141d25e498','customer10','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer10@example.com','고객10',true,'CUSTOMER',true,now(),now(),false),
       ('15abf2d1-dc7b-4d55-b885-f04b2053a18d','customer11','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer11@example.com','고객11',true,'CUSTOMER',true,now(),now(),false),
       ('1b568cc1-043c-4b82-bb81-b3a21f52fafb','customer12','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer12@example.com','고객12',true,'CUSTOMER',true,now(),now(),false),
       ('c02f5f9d-474c-41b2-841b-8d5429b23d91','customer13','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer13@example.com','고객13',true,'CUSTOMER',true,now(),now(),false),
       ('1dd99b4d-0dbb-44bb-ae70-d0fc5dde54aa','customer14','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer14@example.com','고객14',true,'CUSTOMER',true,now(),now(),false),
       ('87510aba-2a35-4dad-859a-2087c5c0c83c','customer15','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer15@example.com','고객15',true,'CUSTOMER',true,now(),now(),false),
       ('17a1bec0-97ea-49f3-b409-ba850db3e0bf','customer16','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer16@example.com','고객16',true,'CUSTOMER',true,now(),now(),false),
       ('419b4e9f-8776-49e7-a295-d90895c62ffb','customer17','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer17@example.com','고객17',true,'CUSTOMER',true,now(),now(),false),
       ('a4f3a47f-d7b8-4258-93ab-a1219cdeee92','customer18','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer18@example.com','고객18',true,'CUSTOMER',true,now(),now(),false),
       ('a65afbe4-941b-415e-8cba-6cc0f23adab1','customer19','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer19@example.com','고객19',true,'CUSTOMER',true,now(),now(),false),
       ('018d0e7a-e632-48ef-ae35-d40b9c5a9296','customer20','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer20@example.com','고객20',true,'CUSTOMER',true,now(),now(),false),
       ('f18e0c6b-ccd3-4c6e-827d-710ed2c71efa','customer21','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer21@example.com','고객21',true,'CUSTOMER',true,now(),now(),false),
       ('23245d0c-0f0e-48b5-9903-9d78eb014941','customer22','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer22@example.com','고객22',true,'CUSTOMER',true,now(),now(),false),
       ('4266df65-a53d-4805-9b32-0e6833c0b7e1','customer23','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer23@example.com','고객23',true,'CUSTOMER',true,now(),now(),false),
       ('b870292f-f78f-4d9c-a1e0-78399ad303d1','customer24','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer24@example.com','고객24',true,'CUSTOMER',true,now(),now(),false),
       ('8ae52683-53f6-4ed7-aa59-7193486c9d39','customer25','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer25@example.com','고객25',true,'CUSTOMER',true,now(),now(),false),
       ('cd2f2351-0f8a-43de-84d0-84844fe6130a','customer26','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer26@example.com','고객26',true,'CUSTOMER',true,now(),now(),false),
       ('1c3d4c94-12bb-4d5a-b5d4-06d075ee1cb8','customer27','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer27@example.com','고객27',true,'CUSTOMER',true,now(),now(),false),
       ('dafcef54-6863-49d4-9767-4924b683ed8c','customer28','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer28@example.com','고객28',true,'CUSTOMER',true,now(),now(),false),
       ('67a6ac99-ab14-4e39-9de4-b185be8bcc3f','customer29','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer29@example.com','고객29',true,'CUSTOMER',true,now(),now(),false),
       ('a62d1b7d-60fa-44be-8c68-8242dab9cfcd','customer30','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer30@example.com','고객30',true,'CUSTOMER',true,now(),now(),false),
       ('40e5db8e-181b-4f1a-a31a-4b5f58a0a53d','customer31','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer31@example.com','고객31',true,'CUSTOMER',true,now(),now(),false),
       ('4d8614b7-aa95-464d-925f-fdc47cabce27','customer32','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer32@example.com','고객32',true,'CUSTOMER',true,now(),now(),false),
       ('68b1897f-b769-418b-b0ef-4c3fec434dd3','customer33','$2a$10$ImHOQUPjwhosYCFjU0hUQO4SMx3NFds.sP5hlXOtFsjWtMJxK43Iu','customer33@example.com','고객33',true,'CUSTOMER',true,now(),now(),false)
    ON CONFLICT (username) DO NOTHING;
