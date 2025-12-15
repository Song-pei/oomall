-- MySQL dump 10.13  Distrib 8.4.3, for Win64 (x86_64)
--
-- Host: mysql    Database: shop
-- ------------------------------------------------------
-- Server version	9.0.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP TABLE IF EXISTS `shop_shop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop_shop` (
  `id` bigint NOT NULL,
  `name` varchar(128) DEFAULT NULL,
  `deposit` bigint DEFAULT NULL,
  `deposit_threshold` bigint DEFAULT NULL,
  `status` tinyint DEFAULT '0',
  `creator_id` bigint DEFAULT NULL,
  `creator_name` varchar(128) DEFAULT NULL,
  `modifier_id` bigint DEFAULT NULL,
  `modifier_name` varchar(128) DEFAULT NULL,
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT NULL,
  `address` varchar(512) DEFAULT NULL,
  `consignee` varchar(64) DEFAULT NULL,
  `mobile` varchar(64) DEFAULT NULL,
  `free_threshold` int NOT NULL DEFAULT '0' COMMENT '默认免邮门槛',
  `region_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商铺';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop_shop`
--

LOCK TABLES `shop_shop` WRITE;
/*!40000 ALTER TABLE `shop_shop` DISABLE KEYS */;
INSERT INTO `shop_shop` VALUES (1,'OOMALL自营商铺',5000000,1000000,2,1,'admin',NULL,NULL,'2021-11-11 13:24:49',NULL,'黄图岗南街112','张三','111111',10000,23),(2,'甜蜜之旅',5000000,1000000,2,1,'admin',NULL,NULL,'2021-11-11 13:24:49',NULL,'麓山村10号','李四','22222',2000,28),(3,'向往时刻',5000000,1000000,2,1,'admin',NULL,NULL,'2021-11-11 13:24:49',NULL,'何厝2号','王五','2222',0,9994),(4,'努力向前',5000000,1000000,2,1,'admin',NULL,NULL,'2021-11-11 13:24:49',NULL,'大帽山农场9号','赵六','2222',0,9995),(5,'坚持就是胜利',5000000,1000000,1,1,'admin',NULL,NULL,'2021-11-11 13:24:49',NULL,'古塘村居委会','周器','33333',0,747229),(6,'一口气',5000000,1000000,1,1,'admin',NULL,NULL,'2021-11-11 13:24:49',NULL,'北京路100号','苏巴','33444',0,745226),(7,'商铺7',5000000,1000000,1,1,'admin',NULL,NULL,'2021-11-11 13:24:49',NULL,'五福巷199号','刘宏','556',0,2497),(8,'商铺8',5000000,1000000,0,1,'admin',NULL,NULL,'2021-11-11 13:24:49',NULL,'锁一巷7号','常遇春','66667',0,13497),(9,'商铺9',5000000,1000000,0,1,'admin',NULL,NULL,'2021-11-11 13:24:49',NULL,'涌金门19号','汪其','788888',0,13472),(10,'商铺10',5000000,1000000,0,1,'admin',NULL,NULL,'2021-11-11 13:24:49','2022-12-14 06:57:33','郑州日报社','刘才','999999',0,32495),(45,'停用商铺1',1000000,500000,3,1,'admin',NULL,NULL,'2022-11-29 10:57:53',NULL,'郑州日报社','书孤','2213233',0,34449),(71,'停用商铺2',1222222,50000,3,1,'admin',NULL,NULL,'2022-12-24 17:19:50',NULL,'山西省太原市小店区亲贤北街368号水工大厦','克强','122333',0,46501),(72,'停用商铺3',122333,60000,3,1,'admin',NULL,NULL,'2022-12-24 17:22:16',NULL,'北海大道中26号鸿海大厦210室','汪五','222222',0,53501),(73,'停用商铺4',1222343,70000,3,1,'admin',NULL,NULL,'2022-12-24 14:42:21',NULL,'厦门大学翔安校区','周轴','1223434',0,57499);
/*!40000 ALTER TABLE `shop_shop` ENABLE KEYS */;
UNLOCK TABLES;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-12-09 16:43:22
