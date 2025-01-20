/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : localhost:3306
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 20/01/2025 19:44:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '姓名',
  `username` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '密码',
  `phone` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '手机号',
  `sex` varchar(2) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '性别',
  `id_number` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NULL DEFAULT NULL COMMENT '身份证号',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态 0:禁用，1:启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `create_user` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_user` bigint NULL DEFAULT NULL COMMENT '修改人',
  `email` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL COMMENT '邮箱地址',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_bin COMMENT = '员工信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13812312312', '1', '110101199001010047', 1, '2022-02-15 15:51:20', '2022-02-17 09:16:20', 10, 1, '3116430062@qq.com');
INSERT INTO `users` VALUES (2, 'test', '李白', 'e10adc3949ba59abbe56e057f20f883e', '12345678922', '1', '', 1, '2025-01-08 22:46:31', '2025-01-08 22:49:27', NULL, NULL, '3116430062@qq.com');
INSERT INTO `users` VALUES (3, 'test', '刘禹锡', 'e10adc3949ba59abbe56e057f20f883e', '12345678911', '0', '', 0, '2025-01-08 22:50:02', '2025-01-08 22:52:59', NULL, NULL, '3116430062@qq.com');
INSERT INTO `users` VALUES (7, NULL, '1', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, 1, '2025-01-20 18:39:46', '2025-01-20 18:39:46', 1, 1, '3116430062@qq.com');
INSERT INTO `users` VALUES (15, NULL, '2', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, 1, '2025-01-20 18:48:57', '2025-01-20 18:48:57', 1, 1, '3116430062@qq.com');

SET FOREIGN_KEY_CHECKS = 1;
