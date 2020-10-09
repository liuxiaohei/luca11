DROP TABLE IF EXISTS job;

CREATE TABLE `job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `host` varchar(64) NOT NULL DEFAULT '',
  `port` int(11) NOT NULL,
  `bean_name` varchar(32) NOT NULL DEFAULT '',
  `method_name` varchar(64) NOT NULL DEFAULT '',
  `name` varchar(32) NOT NULL DEFAULT '',
  `cron_expression` varchar(32) NOT NULL DEFAULT '',
  `params` varchar(256) NOT NULL DEFAULT '',
  `status` int(11) NOT NULL,
  `deleted` smallint(1) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `service_name` varchar(11) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;