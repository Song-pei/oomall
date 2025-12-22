-- 1. 必须先执行删除语句
DROP TABLE IF EXISTS aftersale_aftersaleorder;

-- 2. 再执行建表语句
CREATE TABLE aftersale_aftersaleorder(
                                         id BIGINT PRIMARY KEY COMMENT '主键ID',
                                         shop_id BIGINT NOT NULL COMMENT '店铺ID',
                                         customer_id BIGINT NOT NULL COMMENT '客户ID(账号ID)',
                                         order_id BIGINT NOT NULL COMMENT '关联的原订单ID',
                                         service_order_id BIGINT COMMENT '关联的服务单ID',
                                         customer_name VARCHAR(128) COMMENT '联系人姓名',
                                         customer_mobile VARCHAR(32) COMMENT '联系人电话',
                                         customer_region_id BIGINT COMMENT '地区ID',
                                         customer_address VARCHAR(255) COMMENT '详细地址',
                                         type INT DEFAULT 2 COMMENT '0换货 1退货 2维修',
                                         status INT DEFAULT 0 COMMENT '0已申请 1已同意 2已拒绝',
                                         conclusion VARCHAR(200) COMMENT '审核结论(同意/不同意)',
                                         reason VARCHAR(255) COMMENT '审核拒绝理由',
                                         exception_description VARCHAR(500) COMMENT '异常描述',
    -- 确认这一行被执行了
                                         express_id BIGINT DEFAULT NULL COMMENT '关联的物流单ID',
                                         refund_id BIGINT DEFAULT NULL COMMENT '关联的退款单ID',
                                         exchange_express_id BIGINT DEFAULT NULL COMMENT '关联的换货物流单ID',

                                         creator_id bigint DEFAULT NULL,
                                         creator_name varchar(128) DEFAULT NULL,
                                         modifier_id bigint DEFAULT NULL,
                                         modifier_name varchar(128) DEFAULT NULL,
                                         gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         gmt_modified datetime DEFAULT NULL,
                                         in_arbitrated tinyint NOT NULL DEFAULT '0'
) COMMENT '售后单表';

-- 3. 别忘了重新插入初始化数据（因为刚才删表了，数据也没了）
INSERT INTO aftersale_aftersaleorder (id, shop_id, customer_id, order_id, status, type, customer_name)
VALUES (1, 1, 1001, 5005, 0, 1, '李四');