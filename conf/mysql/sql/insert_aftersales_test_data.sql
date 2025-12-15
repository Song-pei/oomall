-- 插入一条售后单测试数据
INSERT INTO `aftersale_aftersale` (
    `order_item_id`, `customer_id`, `shop_id`, `aftersale_sn`, `type`, `reason`, `conclusion`,
    `quantity`, `region_id`, `address`, `consignee`, `mobile`, `status`, `service_id`, `serial_no`,
    `name`, `creator_id`, `creator_name`, `modifier_id`, `modifier_name`, `gmt_create`, `gmt_modified`, `in_arbitrated`
) VALUES (
    101, 201, 301, 'aftersale-sn-001', 1, '商品质量问题', '同意退货',
    1, 401, '福建省厦门市思明区某某大学', '张三', '13800138000', 0, 501, 'serial-no-001',
    '测试商品', 601, '测试创建者', 601, '测试修改者', NOW(), NOW(), 0
);

