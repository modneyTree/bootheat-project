-- 1) BOOTH
INSERT INTO booth (name, location, created_at)
VALUES
    ('핫도그부스', 'A동 앞', CURRENT_TIMESTAMP),
    ('분식부스',   'B동 앞', CURRENT_TIMESTAMP),
    ('주점부스',   'C동 앞', CURRENT_TIMESTAMP),
    ('롯타부스', '8 번 신양관 앞', CURRENT_TIMESTAMP),
    ('SSCC', '5 번 건물 앞', CURRENT_TIMESTAMP);

-- 2) BOOTH_TABLE
-- 부스1 (3개)
INSERT INTO booth_table (booth_id, table_number, active, created_at)
VALUES
    (1, 1, TRUE, CURRENT_TIMESTAMP),
    (1, 2, TRUE, CURRENT_TIMESTAMP),
    (1, 3, TRUE, CURRENT_TIMESTAMP);

-- 부스2 (3개)
INSERT INTO booth_table (booth_id, table_number, active, created_at)
VALUES
    (2, 1, TRUE, CURRENT_TIMESTAMP),
    (2, 2, TRUE, CURRENT_TIMESTAMP),
    (2, 3, TRUE, CURRENT_TIMESTAMP);

-- 부스3 (21개)
INSERT INTO booth_table (booth_id, table_number, active, created_at)
VALUES
    (3,  1, TRUE, CURRENT_TIMESTAMP),
    (3,  2, TRUE, CURRENT_TIMESTAMP),
    (3,  3, TRUE, CURRENT_TIMESTAMP),
    (3,  4, TRUE, CURRENT_TIMESTAMP),
    (3,  5, TRUE, CURRENT_TIMESTAMP),
    (3,  6, TRUE, CURRENT_TIMESTAMP),
    (3,  7, TRUE, CURRENT_TIMESTAMP),
    (3,  8, TRUE, CURRENT_TIMESTAMP),
    (3,  9, TRUE, CURRENT_TIMESTAMP),
    (3, 10, TRUE, CURRENT_TIMESTAMP),
    (3, 11, TRUE, CURRENT_TIMESTAMP),
    (3, 12, TRUE, CURRENT_TIMESTAMP),
    (3, 13, TRUE, CURRENT_TIMESTAMP),
    (3, 14, TRUE, CURRENT_TIMESTAMP),
    (3, 15, TRUE, CURRENT_TIMESTAMP),
    (3, 16, TRUE, CURRENT_TIMESTAMP),
    (3, 17, TRUE, CURRENT_TIMESTAMP),
    (3, 18, TRUE, CURRENT_TIMESTAMP),
    (3, 19, TRUE, CURRENT_TIMESTAMP),
    (3, 20, TRUE, CURRENT_TIMESTAMP),
    (3, 21, TRUE, CURRENT_TIMESTAMP);

-- 부스4 (21개)
INSERT INTO booth_table (booth_id, table_number, active, created_at)
VALUES
    (4,  1, TRUE, CURRENT_TIMESTAMP),
    (4,  2, TRUE, CURRENT_TIMESTAMP),
    (4,  3, TRUE, CURRENT_TIMESTAMP),
    (4,  4, TRUE, CURRENT_TIMESTAMP),
    (4,  5, TRUE, CURRENT_TIMESTAMP),
    (4,  6, TRUE, CURRENT_TIMESTAMP),
    (4,  7, TRUE, CURRENT_TIMESTAMP),
    (4,  8, TRUE, CURRENT_TIMESTAMP),
    (4,  9, TRUE, CURRENT_TIMESTAMP),
    (4, 10, TRUE, CURRENT_TIMESTAMP),
    (4, 11, TRUE, CURRENT_TIMESTAMP),
    (4, 12, TRUE, CURRENT_TIMESTAMP),
    (4, 13, TRUE, CURRENT_TIMESTAMP),
    (4, 14, TRUE, CURRENT_TIMESTAMP),
    (4, 15, TRUE, CURRENT_TIMESTAMP),
    (4, 16, TRUE, CURRENT_TIMESTAMP),
    (4, 17, TRUE, CURRENT_TIMESTAMP),
    (4, 18, TRUE, CURRENT_TIMESTAMP),
    (4, 19, TRUE, CURRENT_TIMESTAMP),
    (4, 20, TRUE, CURRENT_TIMESTAMP),
    (4, 21, TRUE, CURRENT_TIMESTAMP);

-- 부스5 (21개)
INSERT INTO booth_table (booth_id, table_number, active, created_at)
VALUES
    (5,  1, TRUE, CURRENT_TIMESTAMP),
    (5,  2, TRUE, CURRENT_TIMESTAMP),
    (5,  3, TRUE, CURRENT_TIMESTAMP),
    (5,  4, TRUE, CURRENT_TIMESTAMP),
    (5,  5, TRUE, CURRENT_TIMESTAMP),
    (5,  6, TRUE, CURRENT_TIMESTAMP),
    (5,  7, TRUE, CURRENT_TIMESTAMP),
    (5,  8, TRUE, CURRENT_TIMESTAMP),
    (5,  9, TRUE, CURRENT_TIMESTAMP),
    (5, 10, TRUE, CURRENT_TIMESTAMP),
    (5, 11, TRUE, CURRENT_TIMESTAMP),
    (5, 12, TRUE, CURRENT_TIMESTAMP),
    (5, 13, TRUE, CURRENT_TIMESTAMP);

-- 3) MENU_ITEM (부스5)
-- 메인 메뉴
INSERT INTO menu_item (booth_id, name, category, price, available, description, model_url, preview_image, created_at)
VALUES
    (5, '두부김치', 'FOOD', 16000, TRUE, NULL,
     'https://modney.shop/uploads/90414f00-b8f8-411e-9178-bc4021e973ae_sscc_menu1.png',
     'https://modney.shop/uploads/90414f00-b8f8-411e-9178-bc4021e973ae_sscc_menu1.png',
     CURRENT_TIMESTAMP),
    (5, '순대볶음', 'FOOD', 16000, TRUE, NULL,
     'https://modney.shop/uploads/f6f0490b-8cea-42c0-beba-c6cdd5b3c400_sscc_menu2.png',
     'https://modney.shop/uploads/f6f0490b-8cea-42c0-beba-c6cdd5b3c400_sscc_menu2.png',
     CURRENT_TIMESTAMP),
    (5, '어묵탕', 'FOOD', 14000, TRUE, NULL,
     'https://modney.shop/uploads/f0348bea-c738-482d-a242-392ae32fd5d4_sscc_menu3.png',
     'https://modney.shop/uploads/f0348bea-c738-482d-a242-392ae32fd5d4_sscc_menu3.png',
     CURRENT_TIMESTAMP),
    (5, '치즈 김치전', 'FOOD', 10000, TRUE, NULL,
     'https://modney.shop/uploads/a89cbd3a-3142-4e97-a3e4-1b6eb22c1d30_sscc_menu4.png',
     'https://modney.shop/uploads/a89cbd3a-3142-4e97-a3e4-1b6eb22c1d30_sscc_menu4.png',
     CURRENT_TIMESTAMP);

-- 사이드 메뉴
INSERT INTO menu_item (booth_id, name, category, price, available, description, model_url, preview_image, created_at)
VALUES
    (5, '황도', 'FOOD', 8000, TRUE, NULL,
     'https://modney.shop/uploads/c6df302a-c4e6-414d-9cfe-c166cfa36808_sscc_side2.png',
     'https://modney.shop/uploads/c6df302a-c4e6-414d-9cfe-c166cfa36808_sscc_side2.png',
     CURRENT_TIMESTAMP),
    (5, '치즈 계란말이', 'FOOD', 10000, TRUE, NULL,
     'https://modney.shop/uploads/d43187d7-4e56-405f-ad16-4c54946762d4_sscc_side1.png',
     'https://modney.shop/uploads/d43187d7-4e56-405f-ad16-4c54946762d4_sscc_side1.png',
     CURRENT_TIMESTAMP);

-- 4) MANAGER_USER
INSERT INTO manager_user (booth_id, username, password_hash, role, account_bank, account_no, account_holder, created_at)
VALUES
    (1, '핫도그운영자', '$2a$10$dummyhashdummyhashdummyhashdum', 'MANAGER',
     '카카오뱅크', '1234-323432-123', '홍길동', CURRENT_TIMESTAMP),
    (2, '분식운영자', '$2a$10$dummyhashdummyhashdummyhashdum', 'MANAGER',
     '신한은행', '123-456-789', '김철수', CURRENT_TIMESTAMP),
    (3, '주점운영자', '$2a$10$dummyhashdummyhashdummyhashdum', 'MANAGER',
     '국민은행', '987-654-321', '이영희', CURRENT_TIMESTAMP),
    (4, '롯타운영자', '$2a$10$dummyhashdummyhashdummyhashdum', 'MANAGER',
     '우리은행', '444-555-666', '서태웅', CURRENT_TIMESTAMP),
    (5, 'SSCC운영자', '$2a$10$dummyhashdummyhashdummyhashdum', 'MANAGER',
     '토스뱅크', '100220704241', '김지성', CURRENT_TIMESTAMP);
