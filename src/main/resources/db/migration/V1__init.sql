SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `aaa`
-- ----------------------------
DROP TABLE IF EXISTS `aaa`;
CREATE TABLE `aaa` (
  `id` int(11) NOT NULL,
  `configration` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

SET FOREIGN_KEY_CHECKS = 1;
