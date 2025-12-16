-- 1. 先删除 id=1 的数据（防止主键冲突）
DELETE FROM after_sales_order WHERE id = 1;

-- 2. 插入一条初始状态的数据
-- 状态(status)=0 (已申请/未审核)
-- 类型(type)=2 (维修)
-- 理由(reason)=NULL
INSERT INTO after_sales_order (
    id,
    shop_id,
    customer_id,
    order_id,
    type,
    status,
    conclusion,
    reason,
    create_time,
    update_time
) VALUES (
             1,      -- id
             1,      -- shop_id
             1001,   -- customer_id
             5005,   -- order_id
             2,      -- type (维修)
             0,      -- status (0=已申请)
             NULL,   -- conclusion
             NULL,   -- reason
             NOW(),  -- create_time
             NOW()   -- update_time
         );