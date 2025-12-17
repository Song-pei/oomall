-- 1. 先删除 id=1 的数据（防止主键冲突）
DELETE FROM after_sales_order WHERE id = 1;

-- 2. 插入一条初始状态的数据
INSERT INTO after_sales_order (
    id,
    shop_id,
    customer_id,
    order_id,

    customer_name,
    customer_mobile,
    customer_region_id,
    customer_address,

    type,
    status,
    conclusion,
    reason,
    gmt_create,
    gmt_modified
) VALUES (
             1,      -- id
             1,      -- shop_id
             1001,   -- customer_id
             5005,   -- order_id

             '李四',  -- customer_name
             '13900139000', -- customer_mobile
             3001,   -- customer_region_id
             '北京市朝阳区某某小区A座101室', -- customer_address

             2,      -- type (维修)
             0,      -- status (0=已申请)
             NULL,   -- conclusion
             NULL,   -- reason
             NOW(),  -- gmt_create
             NOW()   -- gmt_modified
         );