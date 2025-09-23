-- 1) BOOTH
INSERT INTO booth (name, location, created_at)
VALUES
    ('핫도그부스', 'A동 앞', CURRENT_TIMESTAMP),
    ('분식부스',   'B동 앞', CURRENT_TIMESTAMP),
    ('주점부스',   'C동 앞', CURRENT_TIMESTAMP),
    ('롯타부스', '8 번 신양관 앞', CURRENT_TIMESTAMP);

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

-- 2) BOOTH_TABLE (부스4 → 21개)
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

-- 3) MENU_ITEM (부스4)
-- 메인 메뉴 3개
INSERT INTO menu_item (booth_id, name, category, price, available, description, model_url, preview_image, created_at)
VALUES
    (4, '강백호~돌뼈', 'FOOD', 15000, TRUE, NULL,
     'https://modney.shop/uploads/fa4c19f0-9059-4cc0-9570-32aeabb1ebe4_lota_memu1.jpg',
     'https://modney.shop/uploads/fa4c19f0-9059-4cc0-9570-32aeabb1ebe4_lota_memu1.jpg',
     CURRENT_TIMESTAMP),
    (4, '안자이 센세의 오코노미야끼', 'FOOD', 13000, TRUE, NULL,
     'https://modney.shop/uploads/7db95bd0-bbff-4f9d-a1b4-d59c42f80d70_lota_menu2.jpg',
     'https://modney.shop/uploads/7db95bd0-bbff-4f9d-a1b4-d59c42f80d70_lota_menu2.jpg',
     CURRENT_TIMESTAMP),
    (4, '강백호 입문 치킨너켓& 만두', 'FOOD', 12000, TRUE, NULL,
     'https://modney.shop/uploads/8bc2dc42-648d-4a7a-95f0-eb49f3c1eb57_lota_menu3.jpg',
     'https://modney.shop/uploads/8bc2dc42-648d-4a7a-95f0-eb49f3c1eb57_lota_menu3.jpg',
     CURRENT_TIMESTAMP);

-- 사이드 메뉴 3개
INSERT INTO menu_item (booth_id, name, category, price, available, description, model_url, preview_image, created_at)
VALUES
    (4, '서태웅의 3점슛 타코야키', 'FOOD', 8900, TRUE, NULL,
     'https://modney.shop/uploads/777bd084-d3f4-4719-843c-b0f9b2292f34_lota_side1.jpg',
     'https://modney.shop/uploads/777bd084-d3f4-4719-843c-b0f9b2292f34_lota_side1.jpg',
     CURRENT_TIMESTAMP),
    (4, '채치수 어묵꼬치', 'FOOD', 2000, TRUE, NULL,
     'https://modney.shop/uploads/bb7806a5-7db8-4375-b54f-08c9fb58c7e0_lota_side2.jpg',
     'https://modney.shop/uploads/bb7806a5-7db8-4375-b54f-08c9fb58c7e0_lota_side2.jpg',
     CURRENT_TIMESTAMP),
    (4, '하프타임 나쵸', 'FOOD', 5900, TRUE, NULL,
     'https://modney.shop/uploads/407b6013-0b6d-4970-bfe8-d0564453f7c2_lota_side3.jpg',
     'https://modney.shop/uploads/407b6013-0b6d-4970-bfe8-d0564453f7c2_lota_side3.jpg',
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
     '우리은행', '444-555-666', '서태웅', CURRENT_TIMESTAMP);
