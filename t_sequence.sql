/*
Navicat MySQL Data Transfer

Source Server         : 妈妈资本测试环境root
Source Server Version : 50621
Source Host           : 192.168.10.33:3306
Source Database       : member

Target Server Type    : MYSQL
Target Server Version : 50621
File Encoding         : 65001

Date: 2016-01-23 16:52:27
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_sequence
-- ----------------------------
DROP TABLE IF EXISTS `t_sequence`;
CREATE TABLE "t_sequence" (
  "NAME" varchar(50) NOT NULL COMMENT '名称',
  "CURRENT_VALUE" int(11) NOT NULL DEFAULT '1' COMMENT '当前值',
  "INCREMENT" smallint(6) NOT NULL DEFAULT '1' COMMENT '增量',
  "TOTAL" smallint(6) NOT NULL DEFAULT '10000' COMMENT '单次取值总量，更新总量需重启应用',
  "THRESHOLD" smallint(6) NOT NULL DEFAULT '10000' COMMENT '刷新阀值，更新阀值需重启应用',
  PRIMARY KEY ("NAME")
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='序列';

-- ----------------------------
-- Records of t_sequence
-- ----------------------------
INSERT INTO `t_sequence` VALUES ('seq_contact_id', '5760001', '1', '20000', '4000');
INSERT INTO `t_sequence` VALUES ('seq_friend_id', '6960', '1', '10', '0');
INSERT INTO `t_sequence` VALUES ('seq_member_id', '5760001', '1', '20000', '4000');
INSERT INTO `t_sequence` VALUES ('seq_merchant_id', '5760001', '1', '20000', '4000');
INSERT INTO `t_sequence` VALUES ('seq_operator_id', '5760001', '1', '20000', '4000');
INSERT INTO `t_sequence` VALUES ('seq_organization_id', '5760001', '1', '20000', '4000');
INSERT INTO `t_sequence` VALUES ('seq_remote_request_no', '5760001', '1', '20000', '4000');
