/*
SQLyog Ultimate v10.42 
MySQL - 5.7.40 : Database - kol_test
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`kol_test` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `kol_test`;

/*Table structure for table `account` */

DROP TABLE IF EXISTS `account`;

CREATE TABLE `account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `email` varchar(50) NOT NULL,
  `head_img` varchar(200) DEFAULT NULL,
  `invite_code` varchar(10) NOT NULL,
  `last_login_ip` varchar(20) DEFAULT NULL,
  `last_login_time` datetime(6) DEFAULT NULL,
  `password` varchar(256) NOT NULL,
  `role` varchar(20) NOT NULL,
  `token` varchar(256) DEFAULT NULL,
  `wallet_addr` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_q0uja26qgu1atulenwup9rxyr` (`email`),
  UNIQUE KEY `UK_tikoaq46uay8ed3pq6gbu9wq5` (`invite_code`),
  UNIQUE KEY `UK_j7vxqmwpgpii4tpxcu5vtjmmt` (`wallet_addr`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `account_info` */

DROP TABLE IF EXISTS `account_info`;

CREATE TABLE `account_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `follow_cost` decimal(20,2) NOT NULL,
  `helper_id` bigint(20) DEFAULT NULL,
  `invite_code` varchar(8) DEFAULT NULL,
  `invite_rebate` decimal(20,2) NOT NULL,
  `inviter_id` bigint(20) DEFAULT NULL,
  `partner_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_rgvpox2nifnul0el1gw1aqgoj` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `account_rebate` */

DROP TABLE IF EXISTS `account_rebate`;

CREATE TABLE `account_rebate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL COMMENT '账户id',
  `rebate` decimal(20,2) NOT NULL COMMENT '返佣比例',
  `valid` bit(1) NOT NULL COMMENT '是否有效',
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_id` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COMMENT='账户返佣表';

/*Table structure for table `account_rebate_relation` */

DROP TABLE IF EXISTS `account_rebate_relation`;

CREATE TABLE `account_rebate_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `rebate` decimal(20,2) NOT NULL,
  `rebate_id` bigint(20) NOT NULL,
  `serial_num` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_accountId_rebateId` (`account_id`,`rebate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `account_relation` */

DROP TABLE IF EXISTS `account_relation`;

CREATE TABLE `account_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL COMMENT '账户id',
  `p_id` bigint(20) DEFAULT NULL COMMENT '上级id',
  `relation_tree` varchar(200) DEFAULT NULL COMMENT '关系树结构',
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_id` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COMMENT='账户关系表';

/*Table structure for table `config` */

DROP TABLE IF EXISTS `config`;

CREATE TABLE `config` (
  `k` varchar(255) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `v` varchar(5000) DEFAULT NULL,
  PRIMARY KEY (`k`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `message` */

DROP TABLE IF EXISTS `message`;

CREATE TABLE `message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_id` bigint(20) NOT NULL,
  `title` varchar(100) NOT NULL COMMENT '标题',
  `msg_content` text COMMENT '消息内容',
  `msg_type` smallint(2) NOT NULL COMMENT '消息类型',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `unread` bit(1) NOT NULL COMMENT '是否未读',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `money_chain_record` */

DROP TABLE IF EXISTS `money_chain_record`;

CREATE TABLE `money_chain_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `amount` decimal(20,10) NOT NULL,
  `chain` varchar(20) NOT NULL,
  `from_addr` varchar(256) NOT NULL,
  `money_record_id` bigint(20) NOT NULL,
  `to_addr` varchar(256) NOT NULL,
  `txid` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKh2rkm3syadwv7iuq9g0ntku8a` (`account_id`,`chain`,`txid`),
  UNIQUE KEY `UK_k4rrcgo2ofqf5946u1ufugqlx` (`txid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*Table structure for table `money_record` */

DROP TABLE IF EXISTS `money_record`;

CREATE TABLE `money_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `amount` decimal(20,10) NOT NULL,
  `comment` varchar(300) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `type` varchar(50) NOT NULL,
  `wallet_address` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=368 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `notice` */

DROP TABLE IF EXISTS `notice`;

CREATE TABLE `notice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `notice_content` varchar(400) NOT NULL COMMENT '公告内容',
  `status` int(11) NOT NULL COMMENT '状态 0：状态 1：正常',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `rebate_rule` */

DROP TABLE IF EXISTS `rebate_rule`;

CREATE TABLE `rebate_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `is_default` bit(1) DEFAULT NULL,
  `level` bigint(20) NOT NULL,
  `rebate` decimal(20,2) NOT NULL,
  `rule` decimal(20,2) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `strategy_snapshot` */

DROP TABLE IF EXISTS `strategy_snapshot`;

CREATE TABLE `strategy_snapshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `balance` decimal(20,10) NOT NULL,
  `loss_count` int(11) NOT NULL,
  `maximum_drawdown` decimal(20,10) NOT NULL,
  `principal` decimal(20,10) NOT NULL,
  `profit_rate` decimal(20,5) NOT NULL,
  `strategy_id` bigint(20) NOT NULL,
  `win_count` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1275 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `symbol` */

DROP TABLE IF EXISTS `symbol`;

CREATE TABLE `symbol` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `symbol_title` varchar(30) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `trade_invest_snapshot` */

DROP TABLE IF EXISTS `trade_invest_snapshot`;

CREATE TABLE `trade_invest_snapshot` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `balance` decimal(20,10) NOT NULL,
  `invest_id` bigint(20) NOT NULL,
  `principal` decimal(20,10) NOT NULL,
  `profit_rate` decimal(10,5) NOT NULL,
  `strategy_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1641 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `trade_investment` */

DROP TABLE IF EXISTS `trade_investment`;

CREATE TABLE `trade_investment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `api_key` varchar(1000) NOT NULL,
  `api_key_id` bigint(20) NOT NULL,
  `balance` decimal(20,10) NOT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `is_end` bit(1) NOT NULL,
  `is_pause` bit(1) NOT NULL,
  `is_real` bit(1) NOT NULL,
  `pause_reason` varchar(500) DEFAULT NULL,
  `principal` decimal(20,10) NOT NULL,
  `strategy_id` bigint(20) NOT NULL,
  `use_balance` decimal(20,10) NOT NULL,
  `strategy_fusing` decimal(20,3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `trade_key` */

DROP TABLE IF EXISTS `trade_key`;

CREATE TABLE `trade_key` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `api_key` varchar(256) NOT NULL,
  `exchange` varchar(20) NOT NULL,
  `name` varchar(20) NOT NULL,
  `passphrase` varchar(100) DEFAULT NULL,
  `secret_key` varchar(256) NOT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4oyyl9xy6hqb8jn6vkk2w4n50` (`api_key`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `trade_order` */

DROP TABLE IF EXISTS `trade_order`;

CREATE TABLE `trade_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `avg_price` decimal(20,10) NOT NULL,
  `exchange` varchar(20) NOT NULL,
  `exchange_order_id` varchar(1000) NOT NULL,
  `fee` decimal(20,10) NOT NULL,
  `investment_id` bigint(20) DEFAULT NULL,
  `is_real` bit(1) NOT NULL,
  `leverage` decimal(20,0) NOT NULL,
  `market` varchar(20) NOT NULL,
  `position_no` bigint(20) NOT NULL,
  `strategy_id` bigint(20) NOT NULL,
  `symbol` varchar(20) NOT NULL,
  `trade_type` varchar(30) NOT NULL,
  `vol` decimal(20,10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7713 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `trade_order_error` */

DROP TABLE IF EXISTS `trade_order_error`;

CREATE TABLE `trade_order_error` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `api_key_id` bigint(20) NOT NULL,
  `error_message` varchar(1000) DEFAULT NULL,
  `invest_str` varchar(1000) DEFAULT NULL,
  `position_no` bigint(20) NOT NULL,
  `request_cmd` varchar(1000) DEFAULT NULL,
  `strategy_id` bigint(20) NOT NULL,
  `trade_type` varchar(30) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6442 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `trade_position` */

DROP TABLE IF EXISTS `trade_position`;

CREATE TABLE `trade_position` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `entry_price` decimal(20,10) NOT NULL,
  `entry_time` datetime(6) NOT NULL,
  `entry_vol` decimal(20,10) NOT NULL,
  `exchange` varchar(20) NOT NULL,
  `exit_price` decimal(20,10) NOT NULL,
  `exit_time` datetime(6) DEFAULT NULL,
  `exit_vol` decimal(20,10) NOT NULL,
  `fee` decimal(20,10) NOT NULL,
  `follow_cost` decimal(20,5) DEFAULT NULL,
  `investment_id` bigint(20) DEFAULT NULL,
  `is_real` bit(1) NOT NULL,
  `is_settlement` bit(1) DEFAULT NULL,
  `leverage` decimal(20,0) NOT NULL,
  `market` varchar(20) NOT NULL,
  `position_no` bigint(20) NOT NULL,
  `position_side` varchar(20) NOT NULL,
  `profit` decimal(20,10) NOT NULL,
  `profit_rate` decimal(10,5) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `strategy_id` bigint(20) NOT NULL,
  `symbol` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1618 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `trade_strategy` */

DROP TABLE IF EXISTS `trade_strategy`;

CREATE TABLE `trade_strategy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `already_follow_count` int(11) NOT NULL,
  `balance` decimal(20,10) NOT NULL,
  `big_follow_count` int(11) NOT NULL,
  `description` varchar(300) DEFAULT NULL,
  `exchange` varchar(255) DEFAULT NULL,
  `leverage` int(11) NOT NULL,
  `loss_count` int(11) NOT NULL,
  `max_amount` decimal(20,2) NOT NULL,
  `max_balance` decimal(20,10) NOT NULL,
  `maximum_drawdown` decimal(20,5) NOT NULL,
  `min_amount` decimal(20,2) NOT NULL,
  `name` varchar(20) NOT NULL,
  `principal` decimal(20,10) NOT NULL,
  `profit_rate` decimal(20,5) NOT NULL,
  `rebate` decimal(20,2) NOT NULL,
  `secret_key` varchar(500) NOT NULL,
  `status` varchar(20) NOT NULL,
  `symbol` varchar(20) NOT NULL,
  `tags` varchar(100) DEFAULT NULL,
  `win_count` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `wallet` */

DROP TABLE IF EXISTS `wallet`;

CREATE TABLE `wallet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `balance` decimal(20,5) NOT NULL,
  `cash_out_amount` decimal(20,2) NOT NULL,
  `follow_cost_amount` decimal(20,5) NOT NULL,
  `give_amount` decimal(20,2) NOT NULL,
  `rebate_amount` decimal(20,5) NOT NULL,
  `recharge_amount` decimal(20,2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_q29lp8vy75l04mpr87j5j62mf` (`account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;

/*Table structure for table `withdraw_review` */

DROP TABLE IF EXISTS `withdraw_review`;

CREATE TABLE `withdraw_review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `account_id` bigint(20) NOT NULL,
  `email` varchar(50) NOT NULL,
  `wallet_addr` varchar(256) NOT NULL COMMENT '提现地址',
  `wallet_amount` decimal(20,10) NOT NULL COMMENT '提现金额',
  `review` bit(1) DEFAULT NULL COMMENT '是否已审核',
  `pass` bit(1) DEFAULT NULL COMMENT '是否通过',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `handle_user` varchar(50) DEFAULT NULL COMMENT '处理账号',
  `handle_date` datetime DEFAULT NULL COMMENT '处理时间',
  `withdraw_addr` varchar(256) DEFAULT NULL COMMENT '提现地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='提现审核';

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
