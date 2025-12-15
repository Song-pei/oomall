-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: mysql    Database: freight
-- ------------------------------------------------------
-- Server version	9.2.0

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

--
-- Table structure for table `freight_region`
--

DROP TABLE IF EXISTS `freight_region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `freight_region` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `region_id` int unsigned NOT NULL,
  `region_template_id` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `freight_region_region_id_region_template_id_uindex` (`region_id`,`region_template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `freight_region`
--

LOCK TABLES `freight_region` WRITE;
/*!40000 ALTER TABLE `freight_region` DISABLE KEYS */;
INSERT INTO `freight_region` VALUES (2,0,106),(12,0,139),(14,0,142),(5,191020,109),(9,191020,133),(17,191020,146),(3,247478,107),(7,247478,131),(15,247478,144),(1,248059,105),(4,248059,108),(8,248059,132),(11,248059,138),(13,248059,141),(16,248059,145),(6,251197,110),(10,251197,134),(18,251197,147);
/*!40000 ALTER TABLE `freight_region` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `freight_region_template`
--

DROP TABLE IF EXISTS `freight_region_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `freight_region_template` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `template_id` int unsigned DEFAULT NULL,
  `creator` varchar(128) DEFAULT NULL,
  `modifier` varchar(128) DEFAULT NULL,
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` timestamp NULL DEFAULT NULL,
  `object_id` varchar(128) DEFAULT NULL,
  `unit` int NOT NULL DEFAULT '1',
  `upper_limit` int NOT NULL DEFAULT '10000',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=184 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='地区运费';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `freight_region_template`
--

LOCK TABLES `freight_region_template` WRITE;
/*!40000 ALTER TABLE `freight_region_template` DISABLE KEYS */;
INSERT INTO `freight_region_template` VALUES (105,2,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fb',2,10),(106,2,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fc',2,10),(107,1,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fd',100,500000),(108,1,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fe',100,500000),(109,1,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420ff',100,500000),(110,1,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d942100',100,500000),(131,16,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fd',100,500000),(132,16,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fe',100,500000),(133,16,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420ff',100,500000),(134,16,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d942100',100,500000),(138,17,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fb',2,10),(139,17,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fc',2,10),(141,19,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fb',2,10),(142,19,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fc',2,10),(144,18,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fd',100,500000),(145,18,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420fe',100,500000),(146,18,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d9420ff',100,500000),(147,18,'admin',NULL,'2022-12-09 18:35:36',NULL,'63930f78d4f468435d942100',100,500000);
/*!40000 ALTER TABLE `freight_region_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `freight_template`
--

DROP TABLE IF EXISTS `freight_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `freight_template` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `shop_id` int unsigned NOT NULL,
  `name` varchar(128) DEFAULT NULL,
  `default_model` tinyint DEFAULT '0',
  `creator` varchar(128) DEFAULT NULL,
  `modifier` varchar(128) DEFAULT NULL,
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` timestamp NULL DEFAULT NULL,
  `template_bean` varchar(64) NOT NULL,
  `divide_strategy` varchar(64) DEFAULT NULL,
  `pack_algorithm` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运费模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `freight_template`
--

LOCK TABLES `freight_template` WRITE;
/*!40000 ALTER TABLE `freight_template` DISABLE KEYS */;
INSERT INTO `freight_template` VALUES (1,1,'最大简单分包计重模板',1,'admin11',NULL,'2022-11-15 17:59:20',NULL,'weightTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.MaxDivideStrategy','cn.edu.xmu.oomall.freight.domain.bo.divide.SimpleAlgorithm'),(2,1,'平均背包分包计件模板',0,'admin11',NULL,'2022-11-15 17:58:24',NULL,'pieceTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.AverageDivideStrategy','cn.edu.xmu.oomall.freight.domain.bo.divide.BackPackAlgorithm'),(16,2,'最大背包分包计重模板',0,'admin22',NULL,'2023-11-29 21:40:35',NULL,'weightTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.MaxDivideStrategy','cn.edu.xmu.oomall.freight.domain.bo.divide.BackPackAlgorithm'),(17,2,'平均简单分包计件模板',0,'admin22',NULL,'2023-11-29 21:40:35',NULL,'pieceTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.AverageDivideStrategy','cn.edu.xmu.oomall.freight.domain.bo.divide.SimpleAlgorithm'),(18,3,'贪心计重模板',0,'admin33',NULL,'2023-11-29 21:42:42',NULL,'weightTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.GreedyAverageDivideSt',NULL),(19,3,'优费计件模板',0,'admin33',NULL,'2023-11-29 21:43:52',NULL,'pieceTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.OptimalDivideStrategy',NULL),(20,3,'贪心计件模板',0,'admin33',NULL,'2024-12-01 12:50:45',NULL,'pieceTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.GreedyAverageDivideSt',NULL),(21,3,'优费计重模板',0,'admin33',NULL,'2024-12-04 12:53:08',NULL,'weightTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.OptimalDivideStrategy',NULL),(22,2,'平均简单分包计重模板',0,'admin22',NULL,'2024-12-04 12:53:08',NULL,'weightTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.AverageDivideStrategy','cn.edu.xmu.oomall.freight.domain.bo.divide.SimpleAlgorithm'),(23,1,'最大简单分包计件模板',0,'admin11',NULL,'2024-12-04 12:53:08',NULL,'pieceTemplateDao','cn.edu.xmu.oomall.freight.domain.bo.divide.MaxDivideStrategy','cn.edu.xmu.oomall.freight.domain.bo.divide.SimpleAlgorithm');
/*!40000 ALTER TABLE `freight_template` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-18 11:31:26
