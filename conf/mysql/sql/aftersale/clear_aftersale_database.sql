-- 警告：此脚本将删除 aftersale 数据库中所有表的数据！
-- 执行前请确保已备份重要数据。

-- 暂时禁用外键约束检查
SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有相关表的数据
TRUNCATE TABLE `aftersale_aftersale`;
TRUNCATE TABLE `aftersale_arbitration`;
TRUNCATE TABLE `aftersale_history`;
TRUNCATE TABLE `aftersale_order`;
TRUNCATE TABLE `aftersale_package`;
TRUNCATE TABLE `after_sales_order`;
DROP TABLE IF EXISTS `aftersale_aftersale`;
DROP TABLE IF EXISTS `aftersale_arbitration`;
DROP TABLE IF EXISTS `aftersale_history`;
DROP TABLE IF EXISTS `aftersale_order`;
DROP TABLE IF EXISTS `aftersale_package`;
DROP TABLE IF EXISTS `after_sales_order`;

-- 重新启用外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 提示操作完成
SELECT 'Aftersale database tables have been cleared.' AS status;

