SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for config_properties
-- ----------------------------
DROP TABLE IF EXISTS `config_properties`;
CREATE TABLE `config_properties` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `key1` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '配置的key',
  `value1` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT '配置的value',
  `application` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '应用名字',
  `profile` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '由client端选择用哪个profile',
  `label` varchar(50) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '由server端选择哪个lable',
  `editable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可编辑',
  `desc` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '属性描述',
  `unit` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '属性单位',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

SET FOREIGN_KEY_CHECKS = 1;
INSERT INTO `config_jdbc`.`config_properties`(`id`, `key1`, `value1`, `application`, `profile`, `label`, `editable`, `desc`, `unit`) VALUES (1, 'server.port', '8083', 'config-client', 'dev', 'master', 1, '', '');
INSERT INTO `config_jdbc`.`config_properties`(`id`, `key1`, `value1`, `application`, `profile`, `label`, `editable`, `desc`, `unit`) VALUES (2, 'foo', 'bar-jdbc12', 'config-client', 'dev', 'master', 1, '', '');