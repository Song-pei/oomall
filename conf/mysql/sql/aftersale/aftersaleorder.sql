-- 1. 如果表已存在，先删除
DROP TABLE IF EXISTS aftersale_aftersaleorder;

-- 2. 建立新表
CREATE TABLE aftersale_aftersaleorder(
                                  id BIGINT PRIMARY KEY COMMENT '主键ID',
                                  shop_id BIGINT NOT NULL COMMENT '店铺ID',
                                  customer_id BIGINT NOT NULL COMMENT '客户ID(账号ID)',
                                  order_id BIGINT NOT NULL COMMENT '关联的原订单ID',

    -- 增加 customer信息
                                  customer_name VARCHAR(128) COMMENT '联系人姓名',
                                  customer_mobile VARCHAR(32) COMMENT '联系人电话',
                                  customer_region_id BIGINT COMMENT '地区ID',
                                  customer_address VARCHAR(255) COMMENT '详细地址',


                                  type INT DEFAULT 2 COMMENT '0换货 1退货 2维修',
                                  status INT DEFAULT 0 COMMENT '0已申请 1已同意 2已拒绝',
                                  conclusion VARCHAR(200) COMMENT '审核结论(同意/不同意)',
                                  reason VARCHAR(255) COMMENT '审核拒绝理由',

    -- 审计字段
                                  creator_id bigint DEFAULT NULL,
                                  creator_name varchar(128) DEFAULT NULL,
                                  modifier_id bigint DEFAULT NULL,
                                  modifier_name varchar(128) DEFAULT NULL,
                                  gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  gmt_modified datetime DEFAULT NULL,
                                  in_arbitrated tinyint NOT NULL DEFAULT '0'
) COMMENT '售后单表';