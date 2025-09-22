-- 1) BOOTH
INSERT INTO booth (name, location, created_at)
VALUES
    ('핫도그부스', 'A동 앞', CURRENT_TIMESTAMP),
    ('분식부스',   'B동 앞', CURRENT_TIMESTAMP),
    ('주점부스',   'C동 앞', CURRENT_TIMESTAMP);

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

-- 3) MENU_ITEM
-- 부스1
INSERT INTO menu_item (booth_id, name, category, price, available, description, model_url, preview_image, created_at)
VALUES
    (1, '핫도그', 'FOOD', 4000, TRUE, NULL,
     'https://upload.wikimedia.org/wikipedia/commons/b/b1/Hot_dog_with_mustard.png',
     'https://upload.wikimedia.org/wikipedia/commons/b/b1/Hot_dog_with_mustard.png',
     CURRENT_TIMESTAMP),
    (1, '치즈핫도그', 'FOOD', 5000, TRUE, NULL,
     'https://sahubconn001.blob.core.windows.net/ct-sahubconn001/img/newshop/goods/028573/028573_1.jpg',
     'https://sahubconn001.blob.core.windows.net/ct-sahubconn001/img/newshop/goods/028573/028573_1.jpg',
     CURRENT_TIMESTAMP),
    (1, '콜라', 'DRINK', 2000, TRUE, NULL,
     'https://img.danawa.com/prod_img/500000/492/722/img/1722492_1.jpg?_v=20200819161846',
     'https://img.danawa.com/prod_img/500000/492/722/img/1722492_1.jpg?_v=20200819161846',
     CURRENT_TIMESTAMP);

-- 부스2
INSERT INTO menu_item (booth_id, name, category, price, available, description, model_url, preview_image, created_at)
VALUES
    (2, '떡볶이', 'FOOD', 5000, TRUE, NULL,
     'https://static.wtable.co.kr/image-resize/production/service/recipe/2167/4x3/c9d9173f-d3e1-43cd-871d-339614b0dbac.jpg',
     'https://static.wtable.co.kr/image-resize/production/service/recipe/2167/4x3/c9d9173f-d3e1-43cd-871d-339614b0dbac.jpg',
     CURRENT_TIMESTAMP),
    (2, '오뎅', 'FOOD', 3000, TRUE, NULL,
     'https://foodjatr5229.cdn-nhncommerce.com/New/04/221500311/221500311_b_1.jpg',
     'https://foodjatr5229.cdn-nhncommerce.com/New/04/221500311/221500311_b_1.jpg',
     CURRENT_TIMESTAMP),
    (2, '순대', 'FOOD', 4000, TRUE, NULL,
     'https://img.daily.co.kr/@files/www.daily.co.kr/content/food/2019/20191113/9b80e50ae6873098d106fc2f0cfdc4b1.jpg',
     'https://img.daily.co.kr/@files/www.daily.co.kr/content/food/2019/20191113/9b80e50ae6873098d106fc2f0cfdc4b1.jpg',
     CURRENT_TIMESTAMP);

-- 부스3
INSERT INTO menu_item (booth_id, name, category, price, available, description, model_url, preview_image, created_at)
VALUES
    (3, '소야', 'FOOD', 6000, TRUE, NULL,
     'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSWG8S6ywUgmPsigK8MmB-vkeLbPytUVqmRAg&s',
     'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSWG8S6ywUgmPsigK8MmB-vkeLbPytUVqmRAg&s',
     CURRENT_TIMESTAMP),
    (3, '제육', 'FOOD', 7000, TRUE, NULL,
     'https://recipe1.ezmember.co.kr/cache/recipe/2024/08/19/550016efb46d2565e4745b9bb13f67fc1.jpg',
     'https://recipe1.ezmember.co.kr/cache/recipe/2024/08/19/550016efb46d2565e4745b9bb13f67fc1.jpg',
     CURRENT_TIMESTAMP),
    (3, '황도', 'FOOD', 3000, TRUE, NULL,
     'https://img.dongwonmall.com/dwmall/static_root/model_img/main/151/15131_1_a.jpg?f=webp&q=80',
     'https://img.dongwonmall.com/dwmall/static_root/model_img/main/151/15131_1_a.jpg?f=webp&q=80',
     CURRENT_TIMESTAMP);

-- 4) MANAGER_USER
INSERT INTO manager_user (booth_id, username, password_hash, role, account_bank, account_no, account_holder, created_at)
VALUES
    (1, '핫도그운영자', '$2a$10$dummyhashdummyhashdummyhashdum', 'MANAGER',
     '카카오뱅크', '1234-323432-123', '홍길동', CURRENT_TIMESTAMP),
    (2, '분식운영자', '$2a$10$dummyhashdummyhashdummyhashdum', 'MANAGER',
     '신한은행', '123-456-789', '김철수', CURRENT_TIMESTAMP),
    (3, '주점운영자', '$2a$10$dummyhashdummyhashdummyhashdum', 'MANAGER',
     '국민은행', '987-654-321', '이영희', CURRENT_TIMESTAMP);
