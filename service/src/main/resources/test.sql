-- ========================================================================
-- 服务单查询测试 - 数据初始化脚本
-- 执行此脚本前请确保数据库连接正常
-- ========================================================================

-- 1. 清理旧数据（可选，测试环境建议执行）
DELETE FROM service_service WHERE id IN (1001, 1002, 1003, 1004);

-- 2. 插入测试数据

-- 【用例 1】正常的上门维修服务单（商铺 100）
INSERT INTO service_service (
    id, shop_id, maintainer_id, type, status,
    consignee, mobile, region_id, address,
    result, serial_no, express_id,
    maintainer_name, maintainer_mobile, product_id, description,
    creator_id, creator_name, modifier_id, modifier_name,
    gmt_create, gmt_modified
) VALUES (
             1001, 100, 1, 0, 3,
             '张三', '13900139000', 1101, '福建省厦门市思明区软件园二期望海路25号',
             NULL, NULL, NULL,
             '李四维修中心', '13900139001', 5001, '手机屏幕损坏',
             1, 'admin', NULL, NULL,
             NOW(), NOW()
         );

-- 【用例 2】寄件维修服务单（商铺 100，已完成）
INSERT INTO service_service (
    id, shop_id, maintainer_id, type, status,
    consignee, mobile, region_id, address,
    result, serial_no, express_id,
    maintainer_name, maintainer_mobile, product_id, description,
    creator_id, creator_name, modifier_id, modifier_name,
    gmt_create, gmt_modified
) VALUES (
             1002, 100, 1, 1, 5,
             '王五', '13800138000', 1102, '福建省厦门市思明区厦门大学海韵园',
             '维修完成，已寄回', 'SN202512230001', 888,
             '李四维修中心', '13900139001', 5002, '电池老化',
             1, 'admin', 1, 'admin',
             DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()
         );

-- 【用例 3】其他商铺的服务单（商铺 200，用于测试越权）
INSERT INTO service_service (
    id, shop_id, maintainer_id, type, status,
    consignee, mobile, region_id, address,
    result, serial_no, express_id,
    maintainer_name, maintainer_mobile, product_id, description,
    creator_id, creator_name, modifier_id, modifier_name,
    gmt_create, gmt_modified
) VALUES (
             1003, 200, 2, 0, 1,
             '赵六', '13700137000', 1103, '福建省厦门市湖里区五缘湾',
             NULL, NULL, NULL,
             '王麻子维修店', '13900139002', 5003, '充电口接触不良',
             2, 'shop200', NULL, NULL,
             NOW(), NOW()
         );

-- 【用例 4】待接受状态的服务单（商铺 100）
INSERT INTO service_service (
    id, shop_id, maintainer_id, type, status,
    consignee, mobile, region_id, address,
    result, serial_no, express_id,
    maintainer_name, maintainer_mobile, product_id, description,
    creator_id, creator_name, modifier_id, modifier_name,
    gmt_create, gmt_modified
) VALUES (
             1004, 100, 1, 0, 1,
             '孙七', '13600136000', 1101, '福建省厦门市思明区中山路',
             NULL, NULL, NULL,
             '李四维修中心', '13900139001', 5004, '摄像头模糊',
             1, 'admin', NULL, NULL,
             NOW(), NOW()
         );

-- 3. 验证数据是否插入成功
SELECT
    id AS '服务单ID',
        shop_id AS '商铺ID',
        maintainer_id AS '维修商ID',
        CASE type
            WHEN 0 THEN '上门维修'
            WHEN 1 THEN '寄件维修'
            ELSE '未知类型'
            END AS '服务类型',
        CASE status
            WHEN 1 THEN '待接受'
            WHEN 2 THEN '已接受'
            WHEN 3 THEN '维修中'
            WHEN 4 THEN '已取消'
            WHEN 5 THEN '已完成'
            ELSE '未知状态'
            END AS '状态',
        consignee AS '收件人',
        mobile AS '电话',
        description AS '描述',
        gmt_create AS '创建时间'
FROM service_service
WHERE id IN (1001, 1002, 1003, 1004)
ORDER BY id;

-- 4. 查看商铺 100 的所有服务单（预期 3 条）
SELECT COUNT(*) AS '商铺100的服务单数量'
FROM service_service
WHERE shop_id = 100 AND id >= 1001;

-- 5. 查看商铺 200 的服务单（预期 1 条，用于测试越权）
SELECT COUNT(*) AS '商铺200的服务单数量'
FROM service_service
WHERE shop_id = 200 AND id >= 1001;

-- ========================================================================
-- 数据初始化完成！
-- 现在可以运行 test-service-query.http 进行接口测试
-- ========================================================================