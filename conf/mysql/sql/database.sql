CREATE USER 'demouser'@'localhost' IDENTIFIED BY 'JavaEE2025-10-27';
CREATE USER 'demouser'@'%' IDENTIFIED BY 'JavaEE2025-10-27';
GRANT PROCESS ON *.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS payment;
GRANT ALL ON payment.* TO 'demouser'@'localhost';
GRANT ALL ON payment.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS product;
GRANT ALL ON product.* TO 'demouser'@'localhost';
GRANT ALL ON product.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS shop;
GRANT ALL ON shop.* TO 'demouser'@'localhost';
GRANT ALL ON shop.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS freight;
GRANT ALL ON freight.* TO 'demouser'@'localhost';
GRANT ALL ON freight.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS aftersale;
GRANT ALL ON aftersale.* TO 'demouser'@'localhost';
GRANT ALL ON aftersale.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS privilege;
GRANT ALL ON privilege.* TO 'demouser'@'localhost';
GRANT ALL ON privilege.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS service;
GRANT ALL ON service.* TO 'demouser'@'localhost';
GRANT ALL ON service.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS prodorder;
GRANT ALL ON prodorder.* TO 'demouser'@'localhost';
GRANT ALL ON prodorder.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS customer;
GRANT ALL ON customer.* TO 'demouser'@'localhost';
GRANT ALL ON customer.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS region;
GRANT ALL ON region.* TO 'demouser'@'localhost';
GRANT ALL ON region.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS nacos;
GRANT ALL ON nacos.* TO 'demouser'@'localhost';
GRANT ALL ON nacos.* TO 'demouser'@'%';

CREATE DATABASE IF NOT EXISTS logistics;
GRANT ALL ON logistics.* TO 'demouser'@'localhost';
GRANT ALL ON logistics.* TO 'demouser'@'%';

FLUSH PRIVILEGES;