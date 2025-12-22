-- 第一步：暴力删除旧表（这会清除所有旧结构和旧数据）
DROP TABLE IF EXISTS aftersale_aftersaleorder;

-- 第二步：创建带有新字段 express_id 的表
CREATE TABLE aftersale_aftersaleorder(
                                         id BIGINT PRIMARY KEY COMMENT '主键ID',
                                         shop_id BIGINT NOT NULL COMMENT '店铺ID',
                                         customer_id BIGINT NOT NULL COMMENT '客户ID(账号ID)',
                                         order_id BIGINT NOT NULL COMMENT '关联的原订单ID',
                                         service_order_id BIGINT COMMENT '关联的服务单ID',
                                         customer_name VARCHAR(128),
                                         customer_mobile VARCHAR(32),
                                         customer_region_id BIGINT,
                                         customer_address VARCHAR(255),
                                         type INT DEFAULT 2,
                                         status INT DEFAULT 0,
                                         conclusion VARCHAR(200),
                                         reason VARCHAR(255),
                                         express_id BIGINT DEFAULT NULL, -- 确保这行在
                                         creator_id bigint,
                                         creator_name varchar(128),
                                         modifier_id bigint,
                                         modifier_name varchar(128),
                                         gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         gmt_modified datetime DEFAULT NULL,
                                         in_arbitrated tinyint NOT NULL DEFAULT '0'
) COMMENT '售后单表';

-- 第三步：插入初始数据
INSERT INTO aftersale_aftersaleorder (
    id, shop_id, customer_id, order_id, customer_name, customer_mobile,
    customer_region_id, customer_address, type, status, express_id, gmt_create, gmt_modified
) VALUES (
             1, 1, 1001, 5005, '李四', '13900139000',
             3001, '北京市朝阳区某某小区A座101室', 1, 0, NULL, NOW(), NOW()
         );
INSERT INTO aftersale_aftersaleorder (
    id, shop_id, customer_id, order_id, customer_name, customer_mobile,
    customer_region_id, customer_address, type, status, express_id, gmt_create, gmt_modified
) VALUES (
             2, 1, 1001, 5010, '赵三', '13900139000',
             3005, '北京市海淀区某某小区B座101室', 2, 0, NULL, NOW(), NOW()
         );