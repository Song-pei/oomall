-- 第一步：暴力删除旧表（这会清除所有旧结构和旧数据）
DROP TABLE IF EXISTS aftersale_aftersaleorder;
DROP TABLE IF EXISTS aftersale_express;
DROP TABLE IF EXISTS aftersale_refund;

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
                                         exception_description VARCHAR(500),

                                         customer_express_id BIGINT DEFAULT NULL, -- 确保这行在
                                         refund_id BIGINT DEFAULT NULL,
                                         shop_express_id BIGINT DEFAULT NULL,

                                         creator_id bigint,
                                         creator_name varchar(128),
                                         modifier_id bigint,
                                         modifier_name varchar(128),
                                         gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         gmt_modified datetime DEFAULT NULL,
                                         in_arbitrated tinyint NOT NULL DEFAULT '0'
) COMMENT '售后单表';
CREATE TABLE aftersale_express(
                                id BIGINT PRIMARY KEY COMMENT '主键ID',
                                aftersale_order_id BIGINT NOT NULL COMMENT '售后单ID',
                                express_id BIGINT NOT NULL COMMENT '运单ID',
                                bill_code VARCHAR(64) NOT NULL COMMENT '运单号',
                                direction INT NOT NULL COMMENT '0退回商家 1寄回客户',
                                type INT NOT NULL COMMENT '0退货 1换货 2验收不通过退回',
                                status INT NOT NULL COMMENT '0待发货 1已发货 2已签收',

                                creator_id bigint,
                                creator_name varchar(128),
                                modifier_id bigint,
                                modifier_name varchar(128),
                                gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                gmt_modified datetime DEFAULT NULL
)COMMENT '售后运单表';

CREATE TABLE aftersale_refund(
                                id BIGINT PRIMARY KEY COMMENT '主键ID',
                                aftersale_order_id BIGINT NOT NULL COMMENT '售后单ID',
                                refund_id BIGINT NOT NULL COMMENT '退款单ID',
                                amount BIGINT NOT NULL COMMENT '退款金额（分）',
                                div_amount BIGINT NOT NULL COMMENT '分账金额（分）',
                                status INT NOT NULL COMMENT '退款状态 0未退款 1已退款 2退款失败',

                                creator_id bigint,
                                creator_name varchar(128),
                                modifier_id bigint,
                                modifier_name varchar(128),
                                gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                gmt_modified datetime DEFAULT NULL
)COMMENT '售后退款单表';
-- 第三步：插入初始数据
INSERT INTO aftersale_aftersaleorder (
    id, shop_id, customer_id, order_id, customer_name, customer_mobile,
    customer_region_id, customer_address, type, status, customer_express_id, gmt_create, gmt_modified
) VALUES (
             1, 1, 1001, 5005, '李四', '13900139000',
             3001, '北京市朝阳区某某小区A座101室', 1, 0, NULL, NOW(), NOW()
         );
INSERT INTO aftersale_aftersaleorder (
    id, shop_id, customer_id, order_id, customer_name, customer_mobile,
    customer_region_id, customer_address, type, status, customer_express_id, gmt_create, gmt_modified
) VALUES (
             2, 1, 1001, 5010, '赵三', '13900139000',
             3005, '北京市海淀区某某小区B座101室', 2, 0, NULL, NOW(), NOW()
         );
INSERT INTO aftersale_aftersaleorder (
    id, shop_id, customer_id, order_id, customer_name, customer_mobile,
    customer_region_id, customer_address, type, status, customer_express_id, gmt_create, gmt_modified
) VALUES (
             3, 1, 1001, 5015, '赵三', '13900139000',
             3005, '北京市海淀区某某小区B座101室', 0, 1, NULL, NOW(), NOW()
         );
INSERT INTO aftersale_aftersaleorder (
    id, shop_id, customer_id, order_id, customer_name, customer_mobile,
    customer_region_id, customer_address, type, status, customer_express_id, gmt_create, gmt_modified
) VALUES (
             4, 1, 1001, 5020, '赵三', '13900139000',
             3005, '北京市海淀区某某小区B座101室', 1, 1, NULL, NOW(), NOW()
         );