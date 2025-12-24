-- 清理数据
TRUNCATE TABLE `service_service`;

-- 1. 正常上门单 (ID=10, Type=0, Status=3)
INSERT INTO `service_service` (id, type, status, maintainer_id, shop_id, consignee, mobile, gmt_create)
VALUES (10, 0, 3, 1, 100, 'sean', '13888888888', NOW());

-- 2. 正常寄件单 (ID=11, Type=1, Status=3)
INSERT INTO `service_service` (id, type, status, maintainer_id, shop_id, consignee, mobile, gmt_create)
VALUES (11, 1, 3, 1, 100, 'sean', '13888888888', NOW());

-- 3. 状态错误单 (ID=12, Status=0)
INSERT INTO `service_service` (id, type, status, maintainer_id, shop_id, gmt_create)
VALUES (12, 0, 0, 1, 100, NOW());

-- 4. 未配置策略的单子 (ID=13, Type=2)
INSERT INTO `service_service` (id, type, status, maintainer_id, shop_id, gmt_create)
VALUES (13, 2, 3, 1, 100, NOW());