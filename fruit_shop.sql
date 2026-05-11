/*
 Navicat Premium Data Transfer

 Source Server         : db
 Source Server Type    : MySQL
 Source Server Version : 50726 (5.7.26)
 Source Host           : localhost:3308
 Source Schema         : fruit_shop

 Target Server Type    : MySQL
 Target Server Version : 50726 (5.7.26)
 File Encoding         : 65001

 Date: 24/03/2026 23:44:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for addresses
-- ----------------------------
DROP TABLE IF EXISTS `addresses`;
CREATE TABLE `addresses`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人电话',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区县',
  `detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `postal_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮编',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认地址：0-否，1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收货地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of addresses
-- ----------------------------
INSERT INTO `addresses` VALUES (1, 1, '老李', '17838273987', '广东省', '广州市', '从化区', '广州市从化区温泉镇温泉大道882号广州南方学院', NULL, 1, '2026-01-31 18:13:41', '2026-01-31 18:13:41');
INSERT INTO `addresses` VALUES (2, 2, '零零七', '13246745786', '广东省', '广州市', '从化区', '广州市从化区温泉镇温泉大道882号广州南方学院', NULL, 0, '2026-01-31 23:05:12', '2026-01-31 23:05:12');
INSERT INTO `addresses` VALUES (3, 3, '香菜来来', '14256378645', '广东省', '广州市', '从化区', '翻斗花园', NULL, 1, '2026-02-20 20:56:01', '2026-02-20 20:56:01');
INSERT INTO `addresses` VALUES (4, 4, '312312', '14256470390', '广东省', '广州市', '白云区', '白云一号', NULL, 1, '2026-03-23 19:46:57', '2026-03-23 19:46:57');
INSERT INTO `addresses` VALUES (5, 5, '??', '18800003727', '??', '??', '??', '???', '518000', 1, '2026-03-23 23:45:12', '2026-03-23 23:45:12');

-- ----------------------------
-- Table structure for admins
-- ----------------------------
DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'admin' COMMENT '角色：admin-管理员，super_admin-超级管理员',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admins
-- ----------------------------
INSERT INTO `admins` VALUES (1, 'admin', '123456', '系统管理员', NULL, NULL, 'super_admin', 1, '2026-03-24 20:49:37', '2026-01-31 14:48:36', '2026-01-31 15:09:01');

-- ----------------------------
-- Table structure for after_sales
-- ----------------------------
DROP TABLE IF EXISTS `after_sales`;
CREATE TABLE `after_sales`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '售后ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `order_item_id` bigint(20) NULL DEFAULT NULL COMMENT '订单项ID（部分退款）',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `type` tinyint(1) NOT NULL COMMENT '售后类型：1-退货，2-退款',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请原因',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详细描述',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '凭证图片（JSON数组）',
  `refund_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '退款金额',
  `return_logistics_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货物流公司',
  `return_logistics_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货物流单号',
  `return_logistics_time` datetime NULL DEFAULT NULL COMMENT '退货物流填写时间',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已同意，2-已驳回，3-已完成',
  `merchant_reply` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家回复',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `process_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '售后申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of after_sales
-- ----------------------------
INSERT INTO `after_sales` VALUES (1, 2, NULL, 1, 2, 2, '商品质量问题', '不新鲜', NULL, 22.00, NULL, NULL, NULL, 2, '1', '2026-01-31 20:02:21', '2026-01-31 20:02:21', '2026-01-31 20:01:50', '2026-01-31 20:01:50');
INSERT INTO `after_sales` VALUES (2, 2, NULL, 1, 2, 2, '商品与描述不符', '', NULL, 22.00, NULL, NULL, NULL, 1, '', '2026-01-31 20:03:44', '2026-01-31 20:03:44', '2026-01-31 20:03:07', '2026-01-31 20:03:07');
INSERT INTO `after_sales` VALUES (3, 13, NULL, 1, 2, 2, '其他原因', '不喜欢', NULL, 44.00, NULL, NULL, NULL, 1, '', '2026-02-18 20:32:40', '2026-02-18 20:32:40', '2026-02-18 00:13:52', '2026-02-18 00:13:52');
INSERT INTO `after_sales` VALUES (4, 13, NULL, 1, 2, 1, '商品质量问题', '', NULL, 44.00, NULL, NULL, NULL, 2, '1', '2026-02-18 20:36:17', '2026-02-18 20:36:17', '2026-02-18 20:32:57', '2026-02-18 20:32:57');
INSERT INTO `after_sales` VALUES (5, 15, NULL, 1, 2, 1, '收到商品损坏', 'f41234', NULL, 212.00, NULL, NULL, NULL, 2, '21312', '2026-02-18 20:38:14', '2026-02-18 20:38:14', '2026-02-18 20:37:58', '2026-02-18 20:37:58');
INSERT INTO `after_sales` VALUES (6, 15, NULL, 1, 2, 1, '商品质量问题', '', NULL, 212.00, NULL, NULL, NULL, 1, '', '2026-02-18 20:53:04', '2026-02-18 20:53:04', '2026-02-18 20:52:49', '2026-02-18 20:52:49');
INSERT INTO `after_sales` VALUES (7, 15, NULL, 1, 2, 2, '商品与描述不符', '33\n【用户申诉】1111\n【管理员处理结果】同意退款', NULL, 212.00, NULL, NULL, NULL, 3, '456', '2026-02-18 20:53:39', '2026-02-18 20:53:39', '2026-02-18 20:53:29', '2026-02-18 21:01:19');
INSERT INTO `after_sales` VALUES (8, 16, NULL, 2, 2, 1, '商品质量问题', '21313', NULL, 113.00, NULL, NULL, '2026-02-18 21:20:21', 3, '', '2026-02-18 21:19:49', '2026-02-18 21:19:49', '2026-02-18 21:19:08', '2026-02-18 21:20:21');
INSERT INTO `after_sales` VALUES (9, 17, NULL, 2, 2, 2, '其他原因', '1231231231\n【用户申诉】【申诉原因】112\n【联系电话】1231231223414\n【管理员处理结果】同意退款', NULL, 110.00, NULL, NULL, NULL, 3, '231231', '2026-02-18 21:19:54', '2026-02-18 21:19:54', '2026-02-18 21:19:22', '2026-02-18 21:27:08');
INSERT INTO `after_sales` VALUES (10, 18, NULL, 2, 2, 2, '收到商品损坏', '有些坏了\n【用户申诉】【申诉原因】明明坏了，为什么不退\n【联系电话】122121\n【管理员处理结果】同意退款', NULL, 25.00, NULL, NULL, NULL, 3, '不行', '2026-02-18 21:29:10', '2026-02-18 21:29:10', '2026-02-18 21:28:23', '2026-02-18 21:47:08');
INSERT INTO `after_sales` VALUES (11, 19, NULL, 2, 2, 1, '发错货', '烂了', '/uploads/20260218214555_5fda8385.png', 106.00, NULL, NULL, '2026-02-18 21:48:24', 3, '', '2026-02-18 21:47:38', '2026-02-18 21:47:38', '2026-02-18 21:45:56', '2026-02-18 21:48:24');
INSERT INTO `after_sales` VALUES (12, 20, NULL, 2, 2, 2, '收到商品损坏', '不好吃\n【用户申诉】【申诉原因】不好吃\n【管理员处理结果】同意退款', '/uploads/20260218214627_9e5ae4e1.png', 226.00, NULL, NULL, NULL, 3, '不好吃不接受退款', '2026-02-18 21:47:52', '2026-02-18 21:47:52', '2026-02-18 21:46:28', '2026-02-18 22:09:17');
INSERT INTO `after_sales` VALUES (13, 21, NULL, 2, 2, 1, '商品质量问题', '有烂的', '/uploads/20260218221042_45bd6e2a.jpg', 113.00, NULL, NULL, '2026-02-18 22:19:55', 3, '', '2026-02-18 22:19:41', '2026-02-18 22:19:41', '2026-02-18 22:10:43', '2026-02-18 22:19:55');
INSERT INTO `after_sales` VALUES (14, 21, NULL, 2, 2, 1, '商品质量问题', '1111\n【用户申诉】【申诉原因】333\n【管理员处理结果】同意退款', '/uploads/20260218222114_f7efab43.jpg', 113.00, NULL, NULL, NULL, 3, '22', '2026-02-18 22:21:32', '2026-02-18 22:21:32', '2026-02-18 22:21:15', '2026-02-18 22:22:27');
INSERT INTO `after_sales` VALUES (15, 21, NULL, 2, 2, 1, '商品质量问题', '11\n【用户申诉】【申诉原因】21312', '/uploads/20260218222533_585b1701.jpg', 113.00, NULL, NULL, NULL, 4, '1221', '2026-02-18 22:25:58', '2026-02-18 22:25:58', '2026-02-18 22:25:34', '2026-02-18 22:26:25');
INSERT INTO `after_sales` VALUES (16, 22, NULL, 2, 2, 2, '商品与描述不符', '23423', '/uploads/20260218223906_e4cce0a4.jpg', 25.00, NULL, NULL, NULL, 3, '', '2026-02-18 22:55:54', '2026-02-18 22:55:54', '2026-02-18 22:39:07', '2026-02-18 22:39:07');

-- ----------------------------
-- Table structure for announcements
-- ----------------------------
DROP TABLE IF EXISTS `announcements`;
CREATE TABLE `announcements`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `type` int(11) NOT NULL DEFAULT 1 COMMENT '公告类型：1-系统公告，2-活动公告，3-维护公告',
  `target` int(11) NOT NULL DEFAULT 0 COMMENT '目标用户：0-全部，1-用户，2-商家',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已下线',
  `is_top` int(11) NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `admin_id` bigint(20) NOT NULL COMMENT '发布管理员ID',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_target`(`target`) USING BTREE,
  INDEX `idx_publish_time`(`publish_time`) USING BTREE,
  INDEX `idx_is_top`(`is_top`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcements
-- ----------------------------
INSERT INTO `announcements` VALUES (1, '关于遵守网络法律法规，营造健康文明上网环境的倡议', '为共建清朗网络空间，维护飘香水果商城全体用户的合法权益，现向全体用户发出如下倡议：\n1.遵守网络法律法规，不发布、不传播违法违规、虚假谣言类信息；\n2.践行网络文明公约，不辱骂、不诋毁、不骚扰，理性互动、文明交流；\n3.坚守诚信底线，商家诚信经营，用户理性消费，杜绝恶意行为；\n4.保护个人信息安全，妥善保管账号，共同抵制信息泄露行为。\n网络空间，法治护航；文明上网，你我同行。让我们携手共建健康、有序的平台网络环境！', 1, 0, 1, 1, 1, '2026-02-22 21:25:32', '2026-02-22 21:25:31', '2026-02-22 21:26:25');
INSERT INTO `announcements` VALUES (2, '飘香水果商城新用户欢迎公告：新人礼遇，诚意相迎', '亲爱的新用户：\n您好！欢迎您加入飘香水果商城大家庭！🎉\n感谢您选择我们的平台，我们致力于为您提供新鲜、优质、实惠的水果产品与便捷、安心的购物体验。为了让您更快熟悉平台、享受更多权益，现将新用户专属福利及平台服务说明公告如下：\n1.新人专享福利，首单立享优惠新用户注册后，即可领取平台新人专属礼包：\n首单满 39 元立减 10 元优惠券；\n免费领取 1 张 “满 59 元免配送费” 券；\n首次下单可享指定爆款水果（如当季时令水果）新人特惠价。\n优惠券将自动发放至您的 “个人中心 - 我的优惠券”，有效期 7 天，快来选购吧！\n2.平台服务承诺，购物更安心\n新鲜保障：所有水果均为产地直采，严格筛选，坏果包赔；\n快速配送：支持同城当日 / 次日达，偏远地区高效配送；\n售后无忧：7 天无理由退换（生鲜类按平台规则执行），客服全程在线响应。\n3.快速上手指南，玩转平台\n浏览首页推荐，发现当季热销与时令水果；\n进入 “店铺列表”，关注优质商家，享受店铺专属活动；\n完善个人信息与收货地址，下单更便捷；\n如有任何疑问，可通过 “个人中心 - 客服中心” 联系我们。\n我们期待与您一同开启新鲜、健康的水果生活，也欢迎您对平台提出宝贵建议，帮助我们不断优化服务。祝您购物愉快！', 1, 0, 1, 0, 1, '2026-02-22 22:53:03', '2026-02-22 22:53:03', '2026-02-22 22:53:03');

-- ----------------------------
-- Table structure for cart_items
-- ----------------------------
DROP TABLE IF EXISTS `cart_items`;
CREATE TABLE `cart_items`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID（登录状态）',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话ID（未登录状态）',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `spec_id` bigint(20) NULL DEFAULT NULL COMMENT '规格ID',
  `quantity` int(11) NOT NULL DEFAULT 1 COMMENT '数量',
  `is_selected` tinyint(1) NULL DEFAULT 1 COMMENT '是否选中：0-否，1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product_spec`(`user_id`, `product_id`, `spec_id`) USING BTREE,
  UNIQUE INDEX `uk_session_product_spec`(`session_id`, `product_id`, `spec_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_session_id`(`session_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart_items
-- ----------------------------

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称（如：热带、温带、浆果等）',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序顺序',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '苹果类', 0, 2, 1, '2026-01-31 14:48:36', '2026-01-31 14:48:36');
INSERT INTO `categories` VALUES (2, '核果类', 0, 4, 1, '2026-01-31 14:48:36', '2026-01-31 14:48:36');
INSERT INTO `categories` VALUES (3, '浆果类', 0, 3, 1, '2026-01-31 14:48:36', '2026-01-31 14:48:36');
INSERT INTO `categories` VALUES (4, '柑橘类', 0, 1, 1, '2026-01-31 14:48:36', '2026-01-31 14:48:36');
INSERT INTO `categories` VALUES (5, '瓜类', 0, 5, 1, '2026-01-31 14:48:36', '2026-01-31 14:48:36');
INSERT INTO `categories` VALUES (6, '热带水果', 0, 6, 1, '2026-02-14 16:02:32', '2026-02-14 16:02:32');

-- ----------------------------
-- Table structure for chat_conversation
-- ----------------------------
DROP TABLE IF EXISTS `chat_conversation`;
CREATE TABLE `chat_conversation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `merchant_id` bigint(20) NOT NULL,
  `last_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `last_time` datetime NULL DEFAULT NULL,
  `user_unread_count` int(11) NULL DEFAULT 0,
  `merchant_unread_count` int(11) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_merchant`(`user_id`, `merchant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_conversation
-- ----------------------------
INSERT INTO `chat_conversation` VALUES (1, 1, 3, '11', '2026-02-25 03:14:10', 0, 1, '2026-02-25 03:14:02', '2026-02-25 18:03:38');
INSERT INTO `chat_conversation` VALUES (2, 1, 2, '[订单]', '2026-02-25 17:26:33', 0, 0, '2026-02-25 03:15:23', '2026-03-24 20:35:51');
INSERT INTO `chat_conversation` VALUES (3, 2, 2, '能发实拍图吗？', '2026-02-25 18:45:39', 0, 0, '2026-02-25 18:44:59', '2026-03-24 20:35:31');
INSERT INTO `chat_conversation` VALUES (4, 0, 2, '22', '2026-02-25 20:32:54', 0, 0, '2026-02-25 20:32:44', '2026-03-24 21:08:45');
INSERT INTO `chat_conversation` VALUES (5, 1, 4, NULL, NULL, 0, 0, '2026-03-16 21:54:14', '2026-03-16 21:54:14');

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `merchant_id` bigint(20) NOT NULL,
  `sender_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `is_read_for_user` tinyint(4) NULL DEFAULT 0,
  `is_read_for_merchant` tinyint(4) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_chat_conversation`(`conversation_id`) USING BTREE,
  CONSTRAINT `fk_chat_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `chat_conversation` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (1, 1, 1, 3, 'USER', '11', '', 1, 0, '2026-02-25 03:14:10');
INSERT INTO `chat_message` VALUES (2, 2, 1, 2, 'USER', '1', '', 1, 1, '2026-02-25 03:15:26');
INSERT INTO `chat_message` VALUES (6, 2, 1, 2, 'MERCHANT', '1', '', 1, 1, '2026-02-25 03:41:59');
INSERT INTO `chat_message` VALUES (7, 2, 1, 2, 'USER', '能发实拍图吗？', '', 1, 1, '2026-02-25 03:47:56');
INSERT INTO `chat_message` VALUES (8, 2, 1, 2, 'MERCHANT', '', '/uploads/20260225034807_a7edd3e6.jpg', 1, 1, '2026-02-25 03:48:08');
INSERT INTO `chat_message` VALUES (9, 2, 1, 2, 'MERCHANT', '🍇', '', 1, 1, '2026-02-25 03:48:13');
INSERT INTO `chat_message` VALUES (10, 2, 1, 2, 'MERCHANT', '__PRODUCT_CARD__{\"id\":5,\"name\":\"猕猴桃\",\"price\":80,\"image\":\"/uploads/20260131165251_24be70e3.jpg\"}', '', 1, 1, '2026-02-25 16:43:12');
INSERT INTO `chat_message` VALUES (11, 2, 1, 2, 'USER', '什么时候发货？', '', 1, 1, '2026-02-25 17:03:01');
INSERT INTO `chat_message` VALUES (12, 2, 1, 2, 'USER', '__ORDER_CARD__{\"id\":26,\"orderNo\":\"ORD20260225014234283AF5\",\"payAmount\":339,\"status\":1,\"createTime\":\"2026-02-25T01:42:34\"}', '', 1, 1, '2026-02-25 17:03:13');
INSERT INTO `chat_message` VALUES (13, 2, 1, 2, 'USER', '__ORDER_CARD__{\"id\":26,\"orderNo\":\"ORD20260225014234283AF5\",\"payAmount\":339,\"status\":1,\"createTime\":\"2026-02-25T01:42:34\",\"image\":\"/uploads/20260131165251_24be70e3.jpg\",\"name\":\"猕猴桃\"}', '', 1, 1, '2026-02-25 17:21:50');
INSERT INTO `chat_message` VALUES (14, 2, 1, 2, 'USER', '__ORDER_CARD__{\"id\":25,\"orderNo\":\"ORD20260222220541CA5417\",\"payAmount\":10,\"status\":5,\"createTime\":\"2026-02-22T22:05:41\",\"image\":\"/uploads/20260220202703_bf8cb8e7.jpg\",\"name\":\"西瓜\"}', '', 1, 1, '2026-02-25 17:26:28');
INSERT INTO `chat_message` VALUES (15, 2, 1, 2, 'USER', '__ORDER_CARD__{\"id\":26,\"orderNo\":\"ORD20260225014234283AF5\",\"payAmount\":339,\"status\":1,\"createTime\":\"2026-02-25T01:42:34\",\"image\":\"/uploads/20260131165251_24be70e3.jpg\",\"name\":\"猕猴桃\"}', '', 1, 1, '2026-02-25 17:26:33');
INSERT INTO `chat_message` VALUES (16, 3, 2, 2, 'USER', '多少钱一斤？\n多少钱一斤？', '', 1, 1, '2026-02-25 18:45:31');
INSERT INTO `chat_message` VALUES (17, 3, 2, 2, 'USER', '🍊🍊', '', 1, 1, '2026-02-25 18:45:36');
INSERT INTO `chat_message` VALUES (18, 3, 2, 2, 'USER', '能发实拍图吗？', '', 1, 1, '2026-02-25 18:45:39');
INSERT INTO `chat_message` VALUES (19, 4, 0, 2, 'MERCHANT', '你好', '', 1, 1, '2026-02-25 20:32:46');
INSERT INTO `chat_message` VALUES (20, 4, 0, 2, 'MERCHANT', '22', '', 1, 1, '2026-02-25 20:32:54');

-- ----------------------------
-- Table structure for contact_messages
-- ----------------------------
DROP TABLE IF EXISTS `contact_messages`;
CREATE TABLE `contact_messages`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '留言类型：咨询问题、意见建议、投诉反馈、合作洽谈、其他',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '留言内容',
  `status` int(11) NULL DEFAULT 0 COMMENT '状态：0-未处理，1-处理中，2-已处理',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '回复内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `user_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '在线留言表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of contact_messages
-- ----------------------------
INSERT INTO `contact_messages` VALUES (1, '张飞', '14245346785', '', '咨询问题', '13231234124234', 2, '好', '2026-02-22 20:38:20', '2026-02-22 23:25:06', NULL);

-- ----------------------------
-- Table structure for coupons
-- ----------------------------
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券名称',
  `type` int(11) NOT NULL DEFAULT 1 COMMENT '优惠券类型：1-满减券，2-折扣券',
  `discount_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '优惠金额（满减券使用）',
  `discount_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '折扣率（折扣券使用，如0.9表示9折）',
  `min_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '最低使用金额',
  `max_discount_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '最大优惠金额（折扣券使用）',
  `total_count` int(11) NOT NULL DEFAULT 0 COMMENT '发放总数，0表示不限制',
  `received_count` int(11) NOT NULL DEFAULT 0 COMMENT '已领取数量',
  `used_count` int(11) NOT NULL DEFAULT 0 COMMENT '已使用数量',
  `valid_start_time` datetime NOT NULL COMMENT '有效期开始时间',
  `valid_end_time` datetime NOT NULL COMMENT '有效期结束时间',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '状态：0-已下架，1-进行中，2-已结束',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '优惠券描述',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商家ID，NULL表示平台优惠券（全平台可用），有值表示商家优惠券（仅该商家可用）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_valid_time`(`valid_start_time`, `valid_end_time`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of coupons
-- ----------------------------
INSERT INTO `coupons` VALUES (1, '新用户无门槛20优惠卷', 1, 20.00, NULL, 0.01, NULL, 10, 1, 1, '2026-02-21 00:00:00', '2026-02-23 00:00:00', 1, '感谢使用', NULL, '2026-02-22 21:52:57', '2026-02-22 21:52:57');
INSERT INTO `coupons` VALUES (2, '1', 1, 10.00, NULL, 2.00, NULL, 0, 0, 0, '2026-02-21 00:00:00', '2026-02-23 00:00:00', 1, '22', NULL, '2026-02-22 21:53:22', '2026-02-22 21:53:22');
INSERT INTO `coupons` VALUES (3, '水果大王10元优惠卷', 1, 10.00, NULL, 20.00, NULL, 10, 1, 0, '2026-02-21 00:00:00', '2026-02-24 00:00:00', 1, '111', 2, '2026-02-22 22:06:29', '2026-02-22 22:06:29');
INSERT INTO `coupons` VALUES (4, 'zzz', 1, 10.00, NULL, 30.00, NULL, 10, 1, 0, '2026-02-21 00:00:00', '2026-02-24 00:00:00', 1, '1321231', 2, '2026-02-22 22:15:44', '2026-02-22 22:15:44');
INSERT INTO `coupons` VALUES (5, '11', 2, NULL, 0.60, 1.00, 100.00, 2, 1, 0, '2026-02-21 00:00:00', '2026-02-24 00:00:00', 1, '', 2, '2026-02-22 22:22:56', '2026-02-22 22:22:56');
INSERT INTO `coupons` VALUES (6, '周年庆优惠卷', 1, 30.00, NULL, 30.01, NULL, 10, 0, 0, '2026-03-04 00:00:00', '2026-03-31 00:00:00', 1, '感谢各位的使用', NULL, '2026-03-05 23:56:33', '2026-03-05 23:56:33');
INSERT INTO `coupons` VALUES (7, '满20减10', 1, 100.00, NULL, 20.00, NULL, 100, 0, 0, '2026-03-03 00:00:00', '2026-03-31 00:00:00', 1, '新店开业，优惠卷大放送', 2, '2026-03-05 23:58:05', '2026-03-05 23:58:05');
INSERT INTO `coupons` VALUES (8, '88折劵', 2, NULL, 0.88, 50.00, 200.00, 100, 0, 0, '2026-03-03 00:00:00', '2026-03-31 00:00:00', 1, '新店开业，88折', 2, '2026-03-05 23:59:02', '2026-03-05 23:59:02');

-- ----------------------------
-- Table structure for favorite_merchants
-- ----------------------------
DROP TABLE IF EXISTS `favorite_merchants`;
CREATE TABLE `favorite_merchants`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_merchant`(`user_id`, `merchant_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏店铺表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite_merchants
-- ----------------------------
INSERT INTO `favorite_merchants` VALUES (6, 1, 2, '2026-02-18 17:46:21');
INSERT INTO `favorite_merchants` VALUES (8, 2, 3, '2026-02-20 22:59:49');
INSERT INTO `favorite_merchants` VALUES (12, 4, 4, '2026-03-16 23:49:27');
INSERT INTO `favorite_merchants` VALUES (13, 5, 5, '2026-03-23 19:43:41');
INSERT INTO `favorite_merchants` VALUES (15, 4, 5, '2026-03-23 19:43:46');

-- ----------------------------
-- Table structure for favorite_products
-- ----------------------------
DROP TABLE IF EXISTS `favorite_products`;
CREATE TABLE `favorite_products`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product`(`user_id`, `product_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite_products
-- ----------------------------
INSERT INTO `favorite_products` VALUES (10, 2, 3, '2026-02-14 20:03:37');
INSERT INTO `favorite_products` VALUES (11, 1, 5, '2026-02-17 17:23:08');
INSERT INTO `favorite_products` VALUES (12, 2, 1, '2026-02-18 23:06:48');
INSERT INTO `favorite_products` VALUES (13, 1, 21, '2026-03-23 22:47:02');
INSERT INTO `favorite_products` VALUES (14, 4, 26, '2026-03-23 22:49:25');

-- ----------------------------
-- Table structure for merchants
-- ----------------------------
DROP TABLE IF EXISTS `merchants`;
CREATE TABLE `merchants`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商家ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '店铺名称',
  `shop_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '店铺描述',
  `shop_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店铺地址',
  `business_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业时间',
  `business_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '营业状态：0-自动判断，1-强制营业中，2-强制休息中',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `business_license` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业执照号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商家头像URL',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝，3-已禁用',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_admin_id` bigint(20) NULL DEFAULT NULL COMMENT '审核管理员ID',
  `register_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商家表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of merchants
-- ----------------------------
INSERT INTO `merchants` VALUES (1, '水果大王', '123456', '水果大王', '每天都是新鲜便宜水果', NULL, NULL, 0, '张三', '1221132144145', '123131@ssaya.com', '1322312', NULL, 2, '2026-01-31 15:34:29', 1, '2026-01-31 15:32:06', '2026-01-31 15:32:06', '2026-01-31 15:32:06');
INSERT INTO `merchants` VALUES (2, 'B001', '123456', '水果大王', '每天都是新鲜的便宜水果', '广州市从化区温泉镇温泉大道882号广州南方学院', '9:00-23:00', 0, '张三', '13232141412', '123131@ssaya.com', '213124', '/uploads/20260217171529_94a5becc.png', 1, '2026-01-31 15:37:07', 1, '2026-01-31 15:35:58', '2026-01-31 15:35:58', '2026-01-31 15:35:58');
INSERT INTO `merchants` VALUES (3, 'B002', '123456', '生鲜水果', '新鲜水果', '翻斗花园', '8:00-20:00', 0, '李四', '13256234766', '123123231@ssaya.com', '12342', '/uploads/20260220205952_22ca4922.jpg', 1, '2026-01-31 15:52:36', 1, '2026-01-31 15:52:11', '2026-01-31 15:52:10', '2026-01-31 15:52:10');
INSERT INTO `merchants` VALUES (4, 'B003', '123456', '高端水果', '新鲜便宜的进口水果', '', '8：00-22:00', 1, '张青', '13673934857', '2341234@qwqe.com', '2341231', '/uploads/20260316215152_29b47960.png', 1, '2026-03-16 20:05:26', 1, '2026-03-16 20:05:08', '2026-03-16 20:05:08', '2026-03-16 20:05:08');
INSERT INTO `merchants` VALUES (5, 'B004', '123456', '水果有点甜', '欢迎品尝，包你满意', '广州市天河区', '9:00-21:00', 0, '李云', '13465342768', '232412@123.com', '12342342', '/uploads/20260323192816_74c04476.png', 1, '2026-03-23 19:27:07', 1, '2026-03-23 19:26:53', '2026-03-23 19:26:52', '2026-03-23 19:26:52');

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` tinyint(1) NOT NULL COMMENT '消息类型：1-订单发货，2-商家回复评论，3-消费者回复评论',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (1, 2, 3, '用户回复了评价', '用户回复了对 草莓 的评价：谢谢额', '/merchant/reviews', 1, '2026-02-01 01:19:36', '2026-02-01 02:01:33');
INSERT INTO `messages` VALUES (2, 2, 3, '用户回复了评价', '用户回复了对 草莓 的评价：谢谢额', '/merchant/reviews', 1, '2026-02-01 01:19:50', '2026-02-01 02:01:33');
INSERT INTO `messages` VALUES (3, 2, 3, '用户回复了评价', '用户回复了对 草莓 的评价：谢谢', '/merchant/reviews', 1, '2026-02-01 01:22:52', '2026-02-01 02:01:33');
INSERT INTO `messages` VALUES (4, 2, 3, '用户回复了评价', '用户回复了对 猕猴桃 的评价：还行啦', '/merchant/reviews', 1, '2026-02-01 01:23:33', '2026-02-01 02:01:33');
INSERT INTO `messages` VALUES (5, 2, 3, '用户回复了评价', '用户回复了对 猕猴桃 的评价：1212', '/merchant/reviews', 1, '2026-02-01 01:23:41', '2026-02-01 01:23:58');
INSERT INTO `messages` VALUES (6, 2, 3, '用户回复了评价', '用户回复了对 猕猴桃 的评价：1', '/merchant/reviews', 1, '2026-02-01 01:24:32', '2026-02-01 01:25:06');
INSERT INTO `messages` VALUES (7, 2, 3, '有用户回复了您的评价', '有用户回复了您对 猕猴桃 的评价：咋样', '/product/5', 1, '2026-02-01 01:41:32', '2026-02-01 01:41:59');
INSERT INTO `messages` VALUES (8, 2, 3, '用户回复了评价', '用户回复了对 猕猴桃 的评价：咋样', '/merchant/reviews', 1, '2026-02-01 01:41:32', '2026-02-01 02:01:33');
INSERT INTO `messages` VALUES (9, 1, 3, '有用户回复了您的评价', '有用户回复了您对 猕猴桃 的评价：121', '/product/5', 1, '2026-02-01 01:57:16', '2026-02-14 15:37:46');
INSERT INTO `messages` VALUES (10, 1, 1, '您的订单已发货', '您的订单 ORD20260214171709DD2E0F 已发货，物流公司：顺丰速运，物流单号：124241412', '/order', 1, '2026-02-14 17:17:37', '2026-02-20 23:08:38');
INSERT INTO `messages` VALUES (11, 2, 3, '用户回复了评价', '用户回复了对 苹果 的评价：欢迎下次再来', '/merchant/reviews', 0, '2026-02-14 17:18:27', NULL);
INSERT INTO `messages` VALUES (12, 1, 3, '有用户回复了您的评价', '有用户回复了您对 苹果 的评价：水果新鲜不', '/product/1', 1, '2026-02-14 17:20:15', '2026-02-14 21:02:43');
INSERT INTO `messages` VALUES (13, 2, 3, '有用户回复了您的评价', '有用户回复了您对 葡萄 的评价：@水果大王 噢哦', '/product/4', 0, '2026-02-14 21:00:59', NULL);
INSERT INTO `messages` VALUES (14, 2, 3, '用户回复了评价', '用户回复了对 葡萄 的评价：@水果大王 噢哦', '/merchant/reviews', 0, '2026-02-14 21:00:59', NULL);
INSERT INTO `messages` VALUES (15, 2, 3, '有用户回复了您的评价', '有用户回复了您对 葡萄 的评价：111 11', '/product/4', 0, '2026-02-14 21:01:09', NULL);
INSERT INTO `messages` VALUES (16, 2, 3, '用户回复了评价', '用户回复了对 葡萄 的评价：111 11', '/merchant/reviews', 0, '2026-02-14 21:01:09', NULL);
INSERT INTO `messages` VALUES (17, 2, 3, '有用户回复了您的评价', '有用户回复了您对 葡萄 的评价：11111', '/product/4', 0, '2026-02-14 21:01:12', NULL);
INSERT INTO `messages` VALUES (18, 2, 3, '用户回复了评价', '用户回复了对 葡萄 的评价：11111', '/merchant/reviews', 0, '2026-02-14 21:01:12', NULL);
INSERT INTO `messages` VALUES (19, 2, 3, '有用户回复了您的评价', '有用户回复了您对 葡萄 的评价：22222@我是奶龙', '/product/4', 0, '2026-02-14 21:01:24', NULL);
INSERT INTO `messages` VALUES (20, 2, 3, '用户回复了评价', '用户回复了对 葡萄 的评价：22222@我是奶龙', '/merchant/reviews', 0, '2026-02-14 21:01:24', NULL);
INSERT INTO `messages` VALUES (21, 2, 3, '有用户回复了您的评价', '有用户回复了您对 葡萄 的评价：@水果大王 还好', '/product/4', 0, '2026-02-14 21:01:45', NULL);
INSERT INTO `messages` VALUES (22, 2, 3, '用户回复了评价', '用户回复了对 葡萄 的评价：@水果大王 还好', '/merchant/reviews', 0, '2026-02-14 21:01:45', NULL);
INSERT INTO `messages` VALUES (23, 2, 3, '用户回复了评价', '用户回复了对 苹果 的评价：你你你】', '/merchant/reviews', 0, '2026-02-14 21:02:55', NULL);
INSERT INTO `messages` VALUES (24, 2, 3, '有用户回复了您的评价', '有用户回复了您对 苹果 的评价：好吃不', '/product/1', 0, '2026-02-14 21:03:12', NULL);
INSERT INTO `messages` VALUES (25, 2, 3, '用户回复了评价', '用户回复了对 苹果 的评价：好吃不', '/merchant/reviews', 0, '2026-02-14 21:03:12', NULL);
INSERT INTO `messages` VALUES (26, 1, 2, '商家回复了您的评价', '商家回复了您对 苹果 的评价：谢谢', '/product/1', 1, '2026-02-15 20:34:59', '2026-02-20 23:08:25');
INSERT INTO `messages` VALUES (27, 2, 3, '有用户回复了您的评价', '有用户回复了您对 苹果 的评价：@我是奶龙 1111', '/product/1', 0, '2026-02-15 20:45:49', NULL);
INSERT INTO `messages` VALUES (28, 2, 3, '用户回复了评价', '用户回复了对 苹果 的评价：@我是奶龙 1111', '/merchant/reviews', 0, '2026-02-15 20:45:49', NULL);
INSERT INTO `messages` VALUES (29, 2, 3, '有用户回复了您的评价', '有用户回复了您对 猕猴桃 的评价：1', '/product/5', 0, '2026-02-15 20:46:39', NULL);
INSERT INTO `messages` VALUES (30, 2, 3, '用户回复了评价', '用户回复了对 猕猴桃 的评价：1', '/merchant/reviews', 0, '2026-02-15 20:46:39', NULL);
INSERT INTO `messages` VALUES (31, 2, 3, '有用户回复了您的评价', '有用户回复了您对 猕猴桃 的评价：@我是奶龙 11', '/product/5', 0, '2026-02-15 20:46:56', NULL);
INSERT INTO `messages` VALUES (32, 2, 3, '用户回复了评价', '用户回复了对 猕猴桃 的评价：@我是奶龙 11', '/merchant/reviews', 0, '2026-02-15 20:46:57', NULL);
INSERT INTO `messages` VALUES (33, 2, 3, '用户回复了评价', '用户回复了对 苹果 的评价：@水果大王 4444', '/merchant/reviews', 0, '2026-02-15 23:42:15', NULL);
INSERT INTO `messages` VALUES (34, 2, 3, '用户回复了评价', '用户回复了对 苹果 的评价：@我是奶龙 zzzz', '/merchant/reviews', 0, '2026-02-15 23:42:36', NULL);
INSERT INTO `messages` VALUES (35, 2, 3, '用户回复了评价', '用户回复了对 猕猴桃 的评价：@我是奶龙 111', '/merchant/reviews', 0, '2026-02-17 17:23:04', NULL);
INSERT INTO `messages` VALUES (36, 1, 2, '商家回复了您的评价', '商家回复了您对 猕猴桃 的评价：111', '/product/5', 1, '2026-02-17 17:23:53', '2026-02-25 00:25:57');
INSERT INTO `messages` VALUES (37, 1, 1, '您的订单已发货', '您的订单 ORD20260218001018B154D1 已发货，物流公司：圆通速递，物流单号：2425354', '/order', 1, '2026-02-18 00:10:34', '2026-02-20 23:08:17');
INSERT INTO `messages` VALUES (38, 1, 2, '商家回复了您的评价', '商家回复了您对 猕猴桃 的评价：欢迎下次再来', '/product/5', 1, '2026-02-18 15:07:35', '2026-02-18 20:05:15');
INSERT INTO `messages` VALUES (39, 2, 3, '用户回复了评价', '用户回复了对 猕猴桃 的评价：@水果大王 111', '/merchant/reviews', 0, '2026-02-18 15:13:57', NULL);
INSERT INTO `messages` VALUES (40, 1, 2, '商家回复了您的评价', '商家回复了您对 猕猴桃 的评价：11', '/product/5', 1, '2026-02-18 15:15:28', '2026-02-18 20:05:06');
INSERT INTO `messages` VALUES (41, 1, 1, '您的订单已发货', '您的订单 ORD202602182037252783C1 已发货，物流公司：圆通速递，物流单号：12312342', '/order', 1, '2026-02-18 20:37:46', '2026-02-20 23:02:02');
INSERT INTO `messages` VALUES (42, 2, 1, '您的订单已发货', '您的订单 ORD202602182117504BDDCF 已发货，物流公司：圆通速递，物流单号：14334214', '/order', 0, '2026-02-18 21:18:42', NULL);
INSERT INTO `messages` VALUES (43, 2, 1, '您的订单已发货', '您的订单 ORD20260218211821F69678 已发货，物流公司：顺丰速运，物流单号：3123213', '/order', 0, '2026-02-18 21:18:47', NULL);
INSERT INTO `messages` VALUES (44, 2, 1, '您的订单已发货', '您的订单 ORD202602182127320FEDCE 已发货，物流公司：中通快递，物流单号：2112123', '/order', 1, '2026-02-18 21:27:50', '2026-02-18 21:29:22');
INSERT INTO `messages` VALUES (45, 2, 1, '您的订单已发货', '您的订单 ORD20260218214343088E66 已发货，物流公司：中通快递，物流单号：667878434', '/order', 0, '2026-02-18 21:44:22', NULL);
INSERT INTO `messages` VALUES (46, 2, 1, '您的订单已发货', '您的订单 ORD202602182144466005EA 已发货，物流公司：中通快递，物流单号：7557', '/order', 0, '2026-02-18 21:45:30', NULL);
INSERT INTO `messages` VALUES (47, 2, 1, '您的订单已发货', '您的订单 ORD20260218220935A4067B 已发货，物流公司：韵达快递，物流单号：2313123', '/order', 0, '2026-02-18 22:10:00', NULL);
INSERT INTO `messages` VALUES (48, 2, 1, '您的订单已发货', '您的订单 ORD2026021822380525D1D7 已发货，物流公司：顺丰速运，物流单号：24324', '/order', 0, '2026-02-18 22:38:52', NULL);
INSERT INTO `messages` VALUES (49, 3, 1, '您的订单已发货', '您的订单 ORD2026022020560409C2B4 已发货，物流公司：圆通速递，物流单号：34678564', '/order', 0, '2026-02-20 20:56:33', NULL);
INSERT INTO `messages` VALUES (50, 1, 1, '您的订单已发货', '您的订单 ORD20260225014234283AF5 已发货，物流公司：申通快递，物流单号：123456', '/order?orderId=26', 1, '2026-02-25 17:55:07', '2026-02-25 17:55:14');
INSERT INTO `messages` VALUES (51, 1, 2, '商家回复了您的评价', '商家回复了您对 猕猴桃 的评价：谢谢支持', '/product/5?reviewId=9', 1, '2026-02-25 17:56:36', '2026-02-25 17:56:45');
INSERT INTO `messages` VALUES (52, 1, 3, '有用户回复了您的评价', '有用户回复了您对 猕猴桃 的评价：味道怎么样', '/product/5?reviewId=9', 1, '2026-02-25 17:57:37', '2026-02-25 17:58:11');
INSERT INTO `messages` VALUES (53, 4, 1, '您的订单已发货', '您的订单 ORD20260323225233147EC0 已发货，物流公司：中通快递，物流单号：21312312', '/order?orderId=54', 0, '2026-03-23 23:00:13', NULL);
INSERT INTO `messages` VALUES (54, 4, 1, '您的订单已发货', '您的订单 ORD20260323224948559F59 已发货，物流公司：圆通速递，物流单号：2131231', '/order?orderId=53', 0, '2026-03-23 23:00:18', NULL);
INSERT INTO `messages` VALUES (55, 5, 3, '用户回复了评价', '用户回复了对 蓝莓 的评价：真的很不错，推荐', '/merchant/reviews?reviewId=11', 0, '2026-03-23 23:01:48', NULL);
INSERT INTO `messages` VALUES (56, 4, 3, '有用户回复了您的评价', '有用户回复了您对 蓝莓 的评价：水果新鲜不', '/product/22?reviewId=11', 0, '2026-03-23 23:02:17', NULL);
INSERT INTO `messages` VALUES (57, 5, 3, '用户回复了评价', '用户回复了对 蓝莓 的评价：水果新鲜不', '/merchant/reviews?reviewId=11', 0, '2026-03-23 23:02:17', NULL);

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` bigint(20) NULL DEFAULT NULL COMMENT '管理员ID',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id`) USING BTREE,
  INDEX `idx_operation_type`(`operation_type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_logs
-- ----------------------------

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称（快照）',
  `product_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品图片（快照）',
  `spec_id` bigint(20) NULL DEFAULT NULL COMMENT '规格ID',
  `spec_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格名称（快照）',
  `price` decimal(10, 2) NOT NULL COMMENT '单价（快照）',
  `quantity` int(11) NOT NULL COMMENT '购买数量',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计金额',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_items
-- ----------------------------
INSERT INTO `order_items` VALUES (1, 1, 5, '猕猴桃', '/uploads/20260131165251_24be70e3.jpg', NULL, NULL, 113.00, 1, 113.00, '2026-01-31 18:13:56');
INSERT INTO `order_items` VALUES (2, 2, 4, '葡萄', '/uploads/20260131165217_413a55cf.jpg', NULL, NULL, 22.00, 1, 22.00, '2026-01-31 18:15:47');
INSERT INTO `order_items` VALUES (3, 3, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 53.00, 3, 159.00, '2026-01-31 20:00:38');
INSERT INTO `order_items` VALUES (4, 4, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 53.00, 2, 106.00, '2026-01-31 20:00:58');
INSERT INTO `order_items` VALUES (5, 5, 5, '猕猴桃', '/uploads/20260131165251_24be70e3.jpg', NULL, NULL, 113.00, 3, 339.00, '2026-01-31 23:05:16');
INSERT INTO `order_items` VALUES (6, 6, 4, '葡萄', '/uploads/20260131165217_413a55cf.jpg', NULL, NULL, 22.00, 3, 66.00, '2026-01-31 23:16:41');
INSERT INTO `order_items` VALUES (7, 7, 1, '苹果', '/uploads/20260131154851_97f49677.png', NULL, NULL, 14.00, 5, 70.00, '2026-01-31 23:27:47');
INSERT INTO `order_items` VALUES (8, 8, 5, '猕猴桃', '/uploads/20260131165251_24be70e3.jpg', NULL, NULL, 113.00, 1, 113.00, '2026-01-31 23:50:01');
INSERT INTO `order_items` VALUES (9, 9, 5, '猕猴桃', '/uploads/20260131165251_24be70e3.jpg', NULL, NULL, 113.00, 1, 113.00, '2026-02-01 01:00:22');
INSERT INTO `order_items` VALUES (10, 10, 4, '葡萄', '/uploads/20260131165217_413a55cf.jpg', NULL, NULL, 22.00, 1, 22.00, '2026-02-01 01:07:10');
INSERT INTO `order_items` VALUES (11, 11, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 53.00, 2, 106.00, '2026-02-01 01:15:52');
INSERT INTO `order_items` VALUES (12, 12, 1, '苹果', '/uploads/20260131154851_97f49677.png', NULL, NULL, 14.00, 1, 14.00, '2026-02-14 17:17:09');
INSERT INTO `order_items` VALUES (13, 13, 4, '葡萄', '/uploads/20260131165217_413a55cf.jpg', NULL, NULL, 22.00, 2, 44.00, '2026-02-18 00:10:18');
INSERT INTO `order_items` VALUES (14, 14, 2, '蓝莓', '/uploads/20260131164738_97b8183e.jpg', NULL, NULL, 25.00, 2, 50.00, '2026-02-18 00:11:17');
INSERT INTO `order_items` VALUES (15, 15, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 53.00, 4, 212.00, '2026-02-18 20:37:25');
INSERT INTO `order_items` VALUES (16, 16, 5, '猕猴桃', '/uploads/20260131165251_24be70e3.jpg', NULL, NULL, 113.00, 1, 113.00, '2026-02-18 21:17:50');
INSERT INTO `order_items` VALUES (17, 17, 4, '葡萄', '/uploads/20260131165217_413a55cf.jpg', NULL, NULL, 22.00, 5, 110.00, '2026-02-18 21:18:21');
INSERT INTO `order_items` VALUES (18, 18, 2, '蓝莓', '/uploads/20260131164738_97b8183e.jpg', NULL, NULL, 25.00, 1, 25.00, '2026-02-18 21:27:32');
INSERT INTO `order_items` VALUES (19, 19, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 53.00, 2, 106.00, '2026-02-18 21:43:43');
INSERT INTO `order_items` VALUES (20, 20, 5, '猕猴桃', '/uploads/20260131165251_24be70e3.jpg', NULL, NULL, 113.00, 2, 226.00, '2026-02-18 21:44:46');
INSERT INTO `order_items` VALUES (21, 21, 5, '猕猴桃', '/uploads/20260131165251_24be70e3.jpg', NULL, NULL, 113.00, 1, 113.00, '2026-02-18 22:09:35');
INSERT INTO `order_items` VALUES (22, 22, 2, '蓝莓', '/uploads/20260131164738_97b8183e.jpg', NULL, NULL, 25.00, 1, 25.00, '2026-02-18 22:38:05');
INSERT INTO `order_items` VALUES (23, 23, 4, '葡萄', '/uploads/20260131165217_413a55cf.jpg', NULL, NULL, 22.00, 1, 22.00, '2026-02-18 23:05:23');
INSERT INTO `order_items` VALUES (24, 24, 6, '西瓜', '/uploads/20260220202703_bf8cb8e7.jpg', NULL, NULL, 15.00, 1, 15.00, '2026-02-20 20:56:04');
INSERT INTO `order_items` VALUES (25, 25, 6, '西瓜', '/uploads/20260220202703_bf8cb8e7.jpg', NULL, NULL, 15.00, 2, 30.00, '2026-02-22 22:05:41');
INSERT INTO `order_items` VALUES (26, 26, 5, '猕猴桃', '/uploads/20260131165251_24be70e3.jpg', NULL, NULL, 113.00, 3, 339.00, '2026-02-25 01:42:34');
INSERT INTO `order_items` VALUES (27, 27, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 92.00, 1, 92.00, '2026-02-25 21:23:08');
INSERT INTO `order_items` VALUES (28, 27, 4, '葡萄', '/uploads/20260131165217_413a55cf.jpg', NULL, NULL, 12.00, 1, 12.00, '2026-02-25 21:23:08');
INSERT INTO `order_items` VALUES (29, 27, 7, '22', '/uploads/20260225144023_d2d19d05.png', 3, NULL, 20.00, 3, 60.00, '2026-02-25 21:23:08');
INSERT INTO `order_items` VALUES (30, 28, 7, '22', '/uploads/20260225144023_d2d19d05.png', 2, NULL, 20.00, 2, 40.00, '2026-02-25 21:39:25');
INSERT INTO `order_items` VALUES (31, 29, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 92.00, 1, 92.00, '2026-02-25 21:39:53');
INSERT INTO `order_items` VALUES (32, 30, 6, '西瓜', '/uploads/20260220202703_bf8cb8e7.jpg', NULL, NULL, 15.00, 2, 30.00, '2026-02-25 21:40:47');
INSERT INTO `order_items` VALUES (33, 31, 6, '西瓜', '/uploads/20260220202703_bf8cb8e7.jpg', NULL, NULL, 15.00, 1, 15.00, '2026-02-25 21:54:15');
INSERT INTO `order_items` VALUES (34, 32, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 92.00, 1, 92.00, '2026-02-25 21:54:42');
INSERT INTO `order_items` VALUES (35, 33, 2, '蓝莓', '/uploads/20260131164738_97b8183e.jpg', NULL, NULL, 25.00, 1, 25.00, '2026-02-25 21:57:46');
INSERT INTO `order_items` VALUES (36, 34, 2, '蓝莓', '/uploads/20260131164738_97b8183e.jpg', NULL, NULL, 25.00, 1, 25.00, '2026-02-25 22:12:08');
INSERT INTO `order_items` VALUES (37, 35, 6, '西瓜', '/uploads/20260220202703_bf8cb8e7.jpg', NULL, NULL, 15.00, 1, 15.00, '2026-02-25 23:18:02');
INSERT INTO `order_items` VALUES (38, 36, 6, '西瓜', '/uploads/20260220202703_bf8cb8e7.jpg', NULL, NULL, 15.00, 2, 30.00, '2026-02-26 00:23:37');
INSERT INTO `order_items` VALUES (39, 37, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 92.00, 1, 92.00, '2026-02-26 00:24:40');
INSERT INTO `order_items` VALUES (40, 38, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 92.00, 1, 92.00, '2026-03-05 23:49:15');
INSERT INTO `order_items` VALUES (41, 39, 1, '苹果', '/uploads/20260131154851_97f49677.png', NULL, NULL, 15.00, 1, 15.00, '2026-03-16 15:59:37');
INSERT INTO `order_items` VALUES (42, 39, 4, '葡萄', '/uploads/20260131165217_413a55cf.jpg', NULL, NULL, 12.00, 1, 12.00, '2026-03-16 15:59:37');
INSERT INTO `order_items` VALUES (43, 40, 7, '22', '/uploads/20260225144023_d2d19d05.png', NULL, NULL, 20.00, 3, 60.00, '2026-03-16 16:54:18');
INSERT INTO `order_items` VALUES (44, 40, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 92.00, 3, 276.00, '2026-03-16 16:54:18');
INSERT INTO `order_items` VALUES (45, 40, 2, '蓝莓', '/uploads/20260131164738_97b8183e.jpg', NULL, NULL, 25.00, 3, 75.00, '2026-03-16 16:54:18');
INSERT INTO `order_items` VALUES (46, 40, 1, '苹果', '/uploads/20260131154851_97f49677.png', NULL, NULL, 15.00, 3, 45.00, '2026-03-16 16:54:18');
INSERT INTO `order_items` VALUES (47, 41, 15, '新疆香梨', '/uploads/20260316211740_9fb7898a.jpg', NULL, NULL, 8.00, 1, 8.00, '2026-03-16 21:26:42');
INSERT INTO `order_items` VALUES (48, 41, 8, '赣南脐橙', '/uploads/20260316205257_19228226.png', NULL, NULL, 7.00, 1, 7.00, '2026-03-16 21:26:43');
INSERT INTO `order_items` VALUES (49, 42, 17, '车厘子 (JJ 级)	', '/uploads/20260316212244_fa2c4fc4.jpg', 30, NULL, 30.00, 1, 30.00, '2026-03-16 21:27:04');
INSERT INTO `order_items` VALUES (50, 42, 11, '阳山水蜜桃', '/uploads/20260316210429_df44669c.jpg', NULL, NULL, 9.00, 1, 9.00, '2026-03-16 21:27:04');
INSERT INTO `order_items` VALUES (51, 43, 17, '车厘子 (JJ 级)	', '/uploads/20260316212244_fa2c4fc4.jpg', 30, NULL, 30.00, 1, 30.00, '2026-03-16 21:33:33');
INSERT INTO `order_items` VALUES (52, 44, 17, '车厘子 (JJ 级)	', '/uploads/20260316212244_fa2c4fc4.jpg', 29, NULL, 30.00, 1, 30.00, '2026-03-16 21:34:00');
INSERT INTO `order_items` VALUES (53, 45, 15, '新疆香梨', '/uploads/20260316211740_9fb7898a.jpg', 23, NULL, 8.00, 1, 8.00, '2026-03-16 21:34:42');
INSERT INTO `order_items` VALUES (54, 46, 16, '丹东红颜草莓', '/uploads/20260316211951_cbb011bd.jpg', 27, NULL, 15.00, 1, 15.00, '2026-03-16 21:35:15');
INSERT INTO `order_items` VALUES (55, 47, 1, '苹果', '/uploads/20260131154851_97f49677.png', 6, NULL, 12.00, 1, 12.00, '2026-03-16 21:36:29');
INSERT INTO `order_items` VALUES (56, 48, 17, '车厘子 (JJ 级)	', '/uploads/20260316212244_fa2c4fc4.jpg', 30, NULL, 30.00, 1, 30.00, '2026-03-16 21:48:37');
INSERT INTO `order_items` VALUES (57, 49, 17, '车厘子 (JJ 级)	', '/uploads/20260316212244_fa2c4fc4.jpg', 30, '4盒', 99.00, 1, 99.00, '2026-03-16 21:50:17');
INSERT INTO `order_items` VALUES (58, 49, 9, '烟台红富士苹果', '/uploads/20260316205727_5a104ca6.png', 11, '2kg', 35.00, 1, 35.00, '2026-03-16 21:50:17');
INSERT INTO `order_items` VALUES (59, 50, 11, '阳山水蜜桃', '/uploads/20260316210429_df44669c.jpg', 16, '2kg', 28.00, 1, 28.00, '2026-03-16 21:50:40');
INSERT INTO `order_items` VALUES (60, 51, 16, '丹东红颜草莓', '/uploads/20260316211951_cbb011bd.jpg', 27, '4盒', 48.00, 1, 48.00, '2026-03-16 21:50:52');
INSERT INTO `order_items` VALUES (61, 52, 24, '红心火龙果', '/uploads/20260323194025_31475c48.png', NULL, NULL, 7.00, 1, 7.00, '2026-03-23 19:47:00');
INSERT INTO `order_items` VALUES (62, 53, 22, '蓝莓', '/uploads/20260323193733_1234460f.png', 43, '4盒', 36.00, 1, 36.00, '2026-03-23 22:49:48');
INSERT INTO `order_items` VALUES (63, 54, 26, '徐香猕猴桃', '/uploads/20260323194232_ee563c79.png', NULL, NULL, 13.00, 2, 26.00, '2026-03-23 22:52:33');
INSERT INTO `order_items` VALUES (64, 55, 24, '红心火龙果', '/uploads/20260323194025_31475c48.png', NULL, NULL, 7.00, 2, 14.00, '2026-03-23 23:32:09');
INSERT INTO `order_items` VALUES (65, 55, 26, '徐香猕猴桃', '/uploads/20260323194232_ee563c79.png', NULL, NULL, 13.00, 2, 26.00, '2026-03-23 23:32:09');
INSERT INTO `order_items` VALUES (66, 56, 24, '红心火龙果', '/uploads/20260323194025_31475c48.png', NULL, NULL, 7.00, 2, 14.00, '2026-03-23 23:32:30');
INSERT INTO `order_items` VALUES (67, 57, 26, '徐香猕猴桃', '/uploads/20260323194232_ee563c79.png', NULL, NULL, 13.00, 2, 26.00, '2026-03-23 23:45:12');
INSERT INTO `order_items` VALUES (68, 58, 3, '草莓', '/uploads/20260131165156_08c78542.jpg', NULL, NULL, 92.00, 1, 92.00, '2026-03-24 14:43:13');
INSERT INTO `order_items` VALUES (69, 58, 2, '蓝莓', '/uploads/20260131164738_97b8183e.jpg', NULL, NULL, 25.00, 1, 25.00, '2026-03-24 14:43:13');
INSERT INTO `order_items` VALUES (70, 59, 11, '阳山水蜜桃', '/uploads/20260316210429_df44669c.jpg', 14, '500g', 9.00, 1, 9.00, '2026-03-24 15:16:41');
INSERT INTO `order_items` VALUES (71, 60, 11, '阳山水蜜桃', '/uploads/20260316210429_df44669c.jpg', 14, '500g', 9.00, 1, 9.00, '2026-03-24 15:23:08');
INSERT INTO `order_items` VALUES (72, 61, 1, '苹果', '/uploads/20260131154851_97f49677.png', NULL, NULL, 12.00, 1, 12.00, '2026-03-24 15:53:15');
INSERT INTO `order_items` VALUES (73, 61, 7, '22', '/uploads/20260225144023_d2d19d05.png', NULL, NULL, 20.00, 1, 20.00, '2026-03-24 15:53:15');
INSERT INTO `order_items` VALUES (74, 62, 6, '西瓜', '/uploads/20260220202703_bf8cb8e7.jpg', NULL, NULL, 15.00, 4, 60.00, '2026-03-24 15:54:03');
INSERT INTO `order_items` VALUES (75, 63, 19, '泰国山竹', '/uploads/20260323193450_bc0a46d1.png', 34, '500g', 22.00, 1, 22.00, '2026-03-24 16:07:11');
INSERT INTO `order_items` VALUES (76, 64, 22, '蓝莓', '/uploads/20260323193733_1234460f.png', 41, '1盒125g', 12.00, 1, 12.00, '2026-03-24 20:28:31');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `address_id` bigint(20) NOT NULL COMMENT '收货地址ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式：alipay-支付宝',
  `payment_status` tinyint(1) NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-支付失败，3-已退款',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `order_status` tinyint(1) NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-待发货，2-待收货，3-待评价，4-已完成，5-已取消',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `ship_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `logistics_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物流公司',
  `logistics_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物流单号',
  `receive_time` datetime NULL DEFAULT NULL COMMENT '收货时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_order_status`(`order_status`) USING BTREE,
  INDEX `idx_payment_status`(`payment_status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'ORD202601311813562A28E6', 1, 2, 1, 113.00, 113.00, 'alipay', 1, '2026-01-31 18:14:06', 4, NULL, NULL, '2026-01-31 20:00:11', '圆通速递', '21454', '2026-01-31 20:01:32', NULL, '要新鲜的', '2026-01-31 18:13:56', '2026-01-31 19:53:26');
INSERT INTO `orders` VALUES (2, 'ORD20260131181547B9E631', 1, 2, 1, 22.00, 22.00, 'alipay', 3, '2026-01-31 18:15:57', 2, NULL, NULL, '2026-01-31 20:00:05', '顺丰速运', '12132132', NULL, NULL, '111', '2026-01-31 18:15:47', '2026-01-31 19:53:26');
INSERT INTO `orders` VALUES (3, 'ORD20260131200038BC6BCA', 1, 2, 1, 159.00, 159.00, NULL, 0, NULL, 5, '2026-01-31 20:00:46', '1', NULL, NULL, NULL, NULL, NULL, '', '2026-01-31 20:00:38', '2026-01-31 20:00:38');
INSERT INTO `orders` VALUES (4, 'ORD20260131200058A138DE', 1, 2, 1, 106.00, 106.00, 'alipay', 1, '2026-01-31 20:01:03', 4, NULL, NULL, '2026-01-31 23:05:37', '韵达快递', '1234123423', '2026-02-25 17:55:36', '2026-02-26 23:31:54', '', '2026-01-31 20:00:58', '2026-02-26 23:31:54');
INSERT INTO `orders` VALUES (5, 'ORD20260131230516975DD3', 2, 2, 2, 339.00, 339.00, 'alipay', 1, '2026-01-31 23:05:19', 4, NULL, NULL, '2026-01-31 23:05:32', '顺丰速运', '123212345314', '2026-01-31 23:05:50', NULL, '', '2026-01-31 23:05:16', '2026-01-31 23:05:16');
INSERT INTO `orders` VALUES (6, 'ORD202601312316419DC459', 2, 2, 2, 66.00, 66.00, 'alipay', 1, '2026-01-31 23:16:45', 4, NULL, NULL, '2026-01-31 23:17:20', '申通快递', '121323134', '2026-01-31 23:17:28', NULL, '', '2026-01-31 23:16:41', '2026-01-31 23:16:41');
INSERT INTO `orders` VALUES (7, 'ORD20260131232747CE04C4', 2, 2, 2, 70.00, 70.00, 'alipay', 1, '2026-01-31 23:27:51', 4, NULL, NULL, '2026-01-31 23:28:55', '中通快递', '23123312', '2026-02-01 01:02:51', NULL, '早发货', '2026-01-31 23:27:47', '2026-01-31 23:27:47');
INSERT INTO `orders` VALUES (8, 'ORD20260131235001B5F748', 2, 2, 2, 113.00, 113.00, 'alipay', 1, '2026-02-01 01:02:57', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-01-31 23:50:01', '2026-01-31 23:50:01');
INSERT INTO `orders` VALUES (9, 'ORD202602010100213CC41C', 1, 2, 1, 113.00, 113.00, 'alipay', 1, '2026-02-01 01:00:25', 4, NULL, NULL, '2026-02-01 01:00:39', '中通快递', '3121243251', '2026-02-01 01:01:55', NULL, '', '2026-02-01 01:00:21', '2026-02-01 01:00:21');
INSERT INTO `orders` VALUES (10, 'ORD2026020101071005F195', 2, 2, 2, 22.00, 22.00, 'alipay', 1, '2026-02-01 01:07:25', 4, NULL, NULL, '2026-02-01 01:07:42', '中通快递', '1143245', '2026-02-01 01:08:05', NULL, '', '2026-02-01 01:07:10', '2026-02-01 01:07:10');
INSERT INTO `orders` VALUES (11, 'ORD202602010115526D48D2', 2, 2, 2, 106.00, 106.00, 'alipay', 1, '2026-02-01 01:16:03', 4, NULL, NULL, '2026-02-01 01:16:13', '顺丰速运', '2312324', '2026-02-01 01:19:20', NULL, '', '2026-02-01 01:15:52', '2026-02-01 01:15:52');
INSERT INTO `orders` VALUES (12, 'ORD20260214171709DD2E0F', 1, 2, 1, 14.00, 14.00, 'alipay', 1, '2026-02-14 17:17:13', 4, NULL, NULL, '2026-02-14 17:17:37', '顺丰速运', '124241412', '2026-02-14 17:17:51', NULL, '', '2026-02-14 17:17:09', '2026-02-14 17:17:09');
INSERT INTO `orders` VALUES (13, 'ORD20260218001018B154D1', 1, 2, 1, 44.00, 44.00, 'alipay', 3, '2026-02-18 00:10:21', 4, NULL, NULL, '2026-02-18 00:10:34', '圆通速递', '2425354', '2026-02-18 00:10:56', '2026-02-20 18:47:47', '', '2026-02-18 00:10:18', '2026-02-20 18:47:47');
INSERT INTO `orders` VALUES (14, 'ORD20260218001117D05883', 1, 2, 1, 50.00, 50.00, 'alipay', 1, '2026-02-18 00:11:19', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-02-18 00:11:17', '2026-02-18 00:11:17');
INSERT INTO `orders` VALUES (15, 'ORD202602182037252783C1', 1, 2, 1, 212.00, 212.00, 'alipay', 3, '2026-02-18 20:37:28', 2, NULL, NULL, '2026-02-18 20:37:46', '圆通速递', '12312342', NULL, NULL, '', '2026-02-18 20:37:25', '2026-02-18 20:37:25');
INSERT INTO `orders` VALUES (16, 'ORD202602182117504BDDCF', 2, 2, 2, 113.00, 113.00, 'alipay', 3, '2026-02-18 21:18:25', 4, NULL, NULL, '2026-02-18 21:18:42', '圆通速递', '14334214', '2026-02-18 21:18:59', '2026-02-20 18:47:47', '', '2026-02-18 21:17:50', '2026-02-20 18:47:47');
INSERT INTO `orders` VALUES (17, 'ORD20260218211821F69678', 2, 2, 2, 110.00, 110.00, 'alipay', 3, '2026-02-18 21:18:27', 4, NULL, NULL, '2026-02-18 21:18:47', '顺丰速运', '3123213', '2026-02-18 21:19:12', '2026-02-20 18:47:47', '', '2026-02-18 21:18:21', '2026-02-20 18:47:47');
INSERT INTO `orders` VALUES (18, 'ORD202602182127320FEDCE', 2, 2, 2, 25.00, 25.00, 'alipay', 3, '2026-02-18 21:27:35', 4, NULL, NULL, '2026-02-18 21:27:50', '中通快递', '2112123', '2026-02-18 21:27:58', '2026-02-20 18:47:47', '', '2026-02-18 21:27:32', '2026-02-20 18:47:47');
INSERT INTO `orders` VALUES (19, 'ORD20260218214343088E66', 2, 2, 2, 106.00, 106.00, 'alipay', 3, '2026-02-18 21:43:46', 4, NULL, NULL, '2026-02-18 21:44:22', '中通快递', '667878434', '2026-02-18 21:46:09', '2026-02-20 18:47:47', '', '2026-02-18 21:43:43', '2026-02-20 18:47:47');
INSERT INTO `orders` VALUES (20, 'ORD202602182144466005EA', 2, 2, 2, 226.00, 226.00, 'alipay', 3, '2026-02-18 21:44:50', 4, NULL, NULL, '2026-02-18 21:45:30', '中通快递', '7557', '2026-02-18 21:46:15', '2026-02-20 18:47:47', '', '2026-02-18 21:44:46', '2026-02-20 18:47:47');
INSERT INTO `orders` VALUES (21, 'ORD20260218220935A4067B', 2, 2, 2, 113.00, 113.00, 'alipay', 3, '2026-02-18 22:09:38', 4, NULL, NULL, '2026-02-18 22:10:00', '韵达快递', '2313123', '2026-02-18 22:10:20', '2026-02-20 18:47:47', '', '2026-02-18 22:09:35', '2026-02-20 18:47:47');
INSERT INTO `orders` VALUES (22, 'ORD2026021822380525D1D7', 2, 2, 2, 25.00, 25.00, 'alipay', 3, '2026-02-18 22:38:08', 4, NULL, NULL, '2026-02-18 22:38:52', '顺丰速运', '24324', '2026-02-18 22:38:59', '2026-02-20 18:47:47', '', '2026-02-18 22:38:05', '2026-02-20 18:47:47');
INSERT INTO `orders` VALUES (23, 'ORD20260218230523D62E03', 2, 2, 2, 22.00, 22.00, NULL, 0, NULL, 5, '2026-02-20 18:47:46', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-02-18 23:05:23', '2026-02-20 18:47:46');
INSERT INTO `orders` VALUES (24, 'ORD2026022020560409C2B4', 3, 3, 3, 15.00, 15.00, 'alipay', 1, '2026-02-20 20:56:20', 4, NULL, NULL, '2026-02-20 20:56:33', '圆通速递', '34678564', '2026-02-20 20:56:40', '2026-02-22 16:12:02', '', '2026-02-20 20:56:04', '2026-02-22 16:12:02');
INSERT INTO `orders` VALUES (25, 'ORD20260222220541CA5417', 1, 3, 1, 30.00, 10.00, NULL, 0, NULL, 5, '2026-02-22 22:16:34', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-02-22 22:05:41', '2026-02-22 22:16:34');
INSERT INTO `orders` VALUES (26, 'ORD20260225014234283AF5', 1, 2, 1, 339.00, 339.00, 'alipay', 1, '2026-02-25 01:42:37', 4, NULL, NULL, '2026-02-25 17:55:07', '申通快递', '123456', '2026-02-25 17:55:23', NULL, '', '2026-02-25 01:42:34', '2026-02-25 01:42:34');
INSERT INTO `orders` VALUES (27, 'ORD20260225212308319716', 2, 2, 2, 164.00, 164.00, NULL, 0, NULL, 5, '2026-02-25 21:33:13', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '无', '2026-02-25 21:23:08', '2026-02-25 21:33:13');
INSERT INTO `orders` VALUES (28, 'ORD20260225213925C477EC', 2, 2, 2, 40.00, 40.00, 'alipay', 1, '2026-02-25 21:39:34', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-02-25 21:39:25', '2026-02-25 21:39:25');
INSERT INTO `orders` VALUES (29, 'ORD20260225213952E9F38F', 2, 2, 2, 92.00, 92.00, 'alipay', 1, '2026-02-25 21:40:11', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-02-25 21:39:52', '2026-02-25 21:39:52');
INSERT INTO `orders` VALUES (30, 'ORD20260225214046AAADF5', 2, 3, 2, 30.00, 30.00, NULL, 0, NULL, 5, '2026-02-25 21:51:14', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-02-25 21:40:46', '2026-02-25 21:51:14');
INSERT INTO `orders` VALUES (31, 'ORD2026022521541578460E', 2, 3, 2, 15.00, 15.00, 'alipay', 1, '2026-02-25 21:54:28', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-02-25 21:54:15', '2026-02-25 21:54:15');
INSERT INTO `orders` VALUES (32, 'ORD202602252154421D2BC6', 2, 2, 2, 92.00, 92.00, 'alipay', 1, '2026-02-25 21:54:50', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-02-25 21:54:42', '2026-02-25 21:54:42');
INSERT INTO `orders` VALUES (33, 'ORD202602252157465C809F', 2, 2, 2, 25.00, 25.00, 'alipay', 1, '2026-02-25 21:57:58', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-02-25 21:57:46', '2026-02-25 21:57:46');
INSERT INTO `orders` VALUES (34, 'ORD202602252212088C6E70', 2, 2, 2, 25.00, 25.00, 'alipay', 0, NULL, 5, '2026-02-25 22:22:14', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-02-25 22:12:08', '2026-02-25 22:22:14');
INSERT INTO `orders` VALUES (35, 'ORD20260225231802E4FD5F', 2, 3, 2, 15.00, 15.00, 'alipay', 0, NULL, 5, '2026-02-26 00:16:24', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-02-25 23:18:02', '2026-02-26 00:16:24');
INSERT INTO `orders` VALUES (36, 'ORD202602260023379D314A', 1, 3, 1, 30.00, 30.00, 'mock', 1, '2026-02-26 00:23:42', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-02-26 00:23:37', '2026-02-26 00:23:37');
INSERT INTO `orders` VALUES (37, 'ORD20260226002440A800A3', 1, 2, 1, 92.00, 92.00, 'mock', 2, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-02-26 00:24:40', '2026-02-26 00:24:40');
INSERT INTO `orders` VALUES (38, 'ORD20260305234915C60EEC', 1, 2, 1, 92.00, 92.00, NULL, 0, NULL, 5, '2026-03-05 23:59:58', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-05 23:49:15', '2026-03-05 23:59:58');
INSERT INTO `orders` VALUES (39, 'ORD202603161559378800B4', 1, 2, 1, 27.00, 27.00, NULL, 0, NULL, 5, '2026-03-16 16:10:21', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 15:59:37', '2026-03-16 16:10:21');
INSERT INTO `orders` VALUES (40, 'ORD202603161654184AC319', 1, 2, 1, 456.00, 456.00, NULL, 0, NULL, 5, '2026-03-16 17:04:41', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 16:54:18', '2026-03-16 17:04:41');
INSERT INTO `orders` VALUES (41, 'ORD20260316212642165B13', 1, 4, 1, 15.00, 15.00, 'mock', 1, '2026-03-16 21:26:49', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:26:42', '2026-03-16 21:26:42');
INSERT INTO `orders` VALUES (42, 'ORD20260316212704E8163D', 1, 4, 1, 39.00, 39.00, 'mock', 1, '2026-03-16 21:27:09', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:27:04', '2026-03-16 21:27:04');
INSERT INTO `orders` VALUES (43, 'ORD20260316213333D4962C', 1, 4, 1, 30.00, 30.00, NULL, 0, NULL, 5, '2026-03-16 21:33:48', '1', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:33:33', '2026-03-16 21:33:33');
INSERT INTO `orders` VALUES (44, 'ORD202603162134006568A3', 1, 4, 1, 30.00, 30.00, NULL, 0, NULL, 5, '2026-03-16 21:44:03', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:34:00', '2026-03-16 21:44:03');
INSERT INTO `orders` VALUES (45, 'ORD202603162134423AF5AD', 1, 4, 1, 8.00, 8.00, NULL, 0, NULL, 5, '2026-03-16 21:45:03', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:34:42', '2026-03-16 21:45:03');
INSERT INTO `orders` VALUES (46, 'ORD20260316213515A58794', 1, 4, 1, 15.00, 15.00, NULL, 0, NULL, 5, '2026-03-16 21:46:03', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:35:15', '2026-03-16 21:46:03');
INSERT INTO `orders` VALUES (47, 'ORD20260316213629202AE5', 1, 2, 1, 12.00, 12.00, NULL, 0, NULL, 5, '2026-03-16 21:47:03', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:36:29', '2026-03-16 21:47:03');
INSERT INTO `orders` VALUES (48, 'ORD20260316214837D635B6', 1, 4, 1, 30.00, 30.00, NULL, 0, NULL, 5, '2026-03-16 22:30:04', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:48:37', '2026-03-16 22:30:04');
INSERT INTO `orders` VALUES (49, 'ORD20260316215017FDFAF1', 1, 4, 1, 134.00, 134.00, NULL, 0, NULL, 5, '2026-03-16 22:30:04', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:50:17', '2026-03-16 22:30:04');
INSERT INTO `orders` VALUES (50, 'ORD2026031621504007E99A', 1, 4, 1, 28.00, 28.00, NULL, 0, NULL, 5, '2026-03-16 22:30:04', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:50:40', '2026-03-16 22:30:04');
INSERT INTO `orders` VALUES (51, 'ORD202603162150526C759C', 1, 4, 1, 48.00, 48.00, NULL, 0, NULL, 5, '2026-03-16 22:30:04', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-16 21:50:52', '2026-03-16 22:30:04');
INSERT INTO `orders` VALUES (52, 'ORD2026032319470031EFC6', 4, 5, 4, 7.00, 7.00, NULL, 0, NULL, 5, '2026-03-23 19:57:04', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-23 19:47:00', '2026-03-23 19:57:04');
INSERT INTO `orders` VALUES (53, 'ORD20260323224948559F59', 4, 5, 4, 36.00, 36.00, 'mock', 1, '2026-03-23 22:49:51', 4, NULL, NULL, '2026-03-23 23:00:18', '圆通速递', '2131231', '2026-03-23 23:00:34', NULL, '', '2026-03-23 22:49:48', '2026-03-23 22:49:48');
INSERT INTO `orders` VALUES (54, 'ORD20260323225233147EC0', 4, 5, 4, 26.00, 26.00, 'mock', 1, '2026-03-23 22:52:38', 4, NULL, NULL, '2026-03-23 23:00:13', '中通快递', '21312312', '2026-03-23 23:00:24', NULL, '', '2026-03-23 22:52:33', '2026-03-23 22:52:33');
INSERT INTO `orders` VALUES (55, 'ORD20260323233209E97372', 3, 5, 3, 40.00, 40.00, NULL, 0, NULL, 5, '2026-03-23 23:32:17', '1', NULL, NULL, NULL, NULL, NULL, '', '2026-03-23 23:32:09', '2026-03-23 23:32:09');
INSERT INTO `orders` VALUES (56, 'ORD20260323233230275E7F', 3, 5, 3, 14.00, 14.00, NULL, 0, NULL, 5, '2026-03-23 23:42:37', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, '', '2026-03-23 23:32:30', '2026-03-23 23:42:37');
INSERT INTO `orders` VALUES (57, 'ORD202603232345124719CD', 5, 5, 5, 26.00, 26.00, NULL, 0, NULL, 5, '2026-03-24 13:32:55', '超时未支付，系统自动取消', NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-23 23:45:12', '2026-03-24 13:32:55');
INSERT INTO `orders` VALUES (58, 'ORD20260324144313EA13F8', 1, 2, 1, 117.00, 117.00, 'mock', 1, '2026-03-24 14:43:30', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-03-24 14:43:13', '2026-03-24 14:43:13');
INSERT INTO `orders` VALUES (59, 'ORD20260324151641DF86D8', 1, 4, 1, 9.00, 9.00, 'mock', 1, '2026-03-24 15:17:16', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-03-24 15:16:41', '2026-03-24 15:16:41');
INSERT INTO `orders` VALUES (60, 'ORD20260324152308A95C67', 1, 4, 1, 9.00, 9.00, 'mock', 1, '2026-03-24 15:23:11', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-03-24 15:23:08', '2026-03-24 15:23:08');
INSERT INTO `orders` VALUES (61, 'ORD20260324155315B6F636', 1, 2, 1, 32.00, 32.00, NULL, 0, NULL, 5, '2026-03-24 15:53:33', '1', NULL, NULL, NULL, NULL, NULL, '', '2026-03-24 15:53:15', '2026-03-24 15:53:15');
INSERT INTO `orders` VALUES (62, 'ORD202603241554029E7F80', 1, 3, 1, 60.00, 60.00, 'mock', 1, '2026-03-24 16:03:35', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-03-24 15:54:02', '2026-03-24 15:54:02');
INSERT INTO `orders` VALUES (63, 'ORD202603241607119040F1', 1, 5, 1, 22.00, 22.00, 'mock', 2, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-03-24 16:07:11', '2026-03-24 16:07:11');
INSERT INTO `orders` VALUES (64, 'ORD20260324202831ED5241', 1, 5, 1, 12.00, 12.00, 'mock', 1, '2026-03-24 20:29:47', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '2026-03-24 20:28:31', '2026-03-24 20:28:31');

-- ----------------------------
-- Table structure for payments
-- ----------------------------
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `payment_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付流水号',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付方式：alipay-支付宝',
  `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款',
  `failure_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '失败原因',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_no`(`payment_no`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_order_no`(`order_no`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payments
-- ----------------------------
INSERT INTO `payments` VALUES (1, 1, 'ORD202601311813562A28E6', 'PAY202601311814064B2971D7', 'alipay', 113.00, 1, NULL, '2026-01-31 18:14:06', '2026-01-31 18:14:06', '2026-01-31 18:14:06');
INSERT INTO `payments` VALUES (2, 2, 'ORD20260131181547B9E631', 'PAY202601311815572D89F05A', 'alipay', 22.00, 1, NULL, '2026-01-31 18:15:57', '2026-01-31 18:15:57', '2026-01-31 18:15:57');
INSERT INTO `payments` VALUES (3, 4, 'ORD20260131200058A138DE', 'PAY202601312001025CD2F641', 'alipay', 106.00, 1, NULL, '2026-01-31 20:01:03', '2026-01-31 20:01:02', '2026-01-31 20:01:02');
INSERT INTO `payments` VALUES (4, 5, 'ORD20260131230516975DD3', 'PAY202601312305190F563F53', 'alipay', 339.00, 1, NULL, '2026-01-31 23:05:19', '2026-01-31 23:05:19', '2026-01-31 23:05:19');
INSERT INTO `payments` VALUES (5, 6, 'ORD202601312316419DC459', 'PAY20260131231645B43723F9', 'alipay', 66.00, 1, NULL, '2026-01-31 23:16:45', '2026-01-31 23:16:45', '2026-01-31 23:16:45');
INSERT INTO `payments` VALUES (6, 7, 'ORD20260131232747CE04C4', 'PAY20260131232750E0C154BE', 'alipay', 70.00, 1, NULL, '2026-01-31 23:27:51', '2026-01-31 23:27:50', '2026-01-31 23:27:50');
INSERT INTO `payments` VALUES (7, 9, 'ORD202602010100213CC41C', 'PAY202602010100257E5727F1', 'alipay', 113.00, 1, NULL, '2026-02-01 01:00:25', '2026-02-01 01:00:25', '2026-02-01 01:00:25');
INSERT INTO `payments` VALUES (8, 8, 'ORD20260131235001B5F748', 'PAY20260201010257BD831239', 'alipay', 113.00, 1, NULL, '2026-02-01 01:02:57', '2026-02-01 01:02:57', '2026-02-01 01:02:57');
INSERT INTO `payments` VALUES (9, 10, 'ORD2026020101071005F195', 'PAY20260201010725606204D1', 'alipay', 22.00, 1, NULL, '2026-02-01 01:07:25', '2026-02-01 01:07:25', '2026-02-01 01:07:25');
INSERT INTO `payments` VALUES (10, 11, 'ORD202602010115526D48D2', 'PAY20260201011603813759A5', 'alipay', 106.00, 1, NULL, '2026-02-01 01:16:03', '2026-02-01 01:16:03', '2026-02-01 01:16:03');
INSERT INTO `payments` VALUES (11, 12, 'ORD20260214171709DD2E0F', 'PAY20260214171712C4957E13', 'alipay', 14.00, 1, NULL, '2026-02-14 17:17:13', '2026-02-14 17:17:12', '2026-02-14 17:17:12');
INSERT INTO `payments` VALUES (12, 13, 'ORD20260218001018B154D1', 'PAY202602180010218E851643', 'alipay', 44.00, 1, NULL, '2026-02-18 00:10:21', '2026-02-18 00:10:21', '2026-02-18 00:10:21');
INSERT INTO `payments` VALUES (13, 14, 'ORD20260218001117D05883', 'PAY202602180011183AB522C7', 'alipay', 50.00, 1, NULL, '2026-02-18 00:11:19', '2026-02-18 00:11:18', '2026-02-18 00:11:18');
INSERT INTO `payments` VALUES (14, 15, 'ORD202602182037252783C1', 'PAY20260218203728F860C15B', 'alipay', 212.00, 1, NULL, '2026-02-18 20:37:28', '2026-02-18 20:37:28', '2026-02-18 20:37:28');
INSERT INTO `payments` VALUES (15, 16, 'ORD202602182117504BDDCF', 'PAY2026021821182594EBB23A', 'alipay', 113.00, 1, NULL, '2026-02-18 21:18:25', '2026-02-18 21:18:25', '2026-02-18 21:18:25');
INSERT INTO `payments` VALUES (16, 17, 'ORD20260218211821F69678', 'PAY2026021821182710256390', 'alipay', 110.00, 1, NULL, '2026-02-18 21:18:27', '2026-02-18 21:18:27', '2026-02-18 21:18:27');
INSERT INTO `payments` VALUES (17, 18, 'ORD202602182127320FEDCE', 'PAY20260218212734B2FD987B', 'alipay', 25.00, 1, NULL, '2026-02-18 21:27:35', '2026-02-18 21:27:34', '2026-02-18 21:27:34');
INSERT INTO `payments` VALUES (18, 19, 'ORD20260218214343088E66', 'PAY2026021821434602F78ECD', 'alipay', 106.00, 1, NULL, '2026-02-18 21:43:46', '2026-02-18 21:43:46', '2026-02-18 21:43:46');
INSERT INTO `payments` VALUES (19, 20, 'ORD202602182144466005EA', 'PAY202602182144500C67B0C2', 'alipay', 226.00, 1, NULL, '2026-02-18 21:44:50', '2026-02-18 21:44:50', '2026-02-18 21:44:50');
INSERT INTO `payments` VALUES (20, 21, 'ORD20260218220935A4067B', 'PAY20260218220937E32ADE4A', 'alipay', 113.00, 1, NULL, '2026-02-18 22:09:38', '2026-02-18 22:09:37', '2026-02-18 22:09:37');
INSERT INTO `payments` VALUES (21, 22, 'ORD2026021822380525D1D7', 'PAY2026021822380844ECC026', 'alipay', 25.00, 1, NULL, '2026-02-18 22:38:08', '2026-02-18 22:38:08', '2026-02-18 22:38:08');
INSERT INTO `payments` VALUES (22, 24, 'ORD2026022020560409C2B4', 'PAY20260220205620606421FF', 'alipay', 15.00, 1, NULL, '2026-02-20 20:56:20', '2026-02-20 20:56:20', '2026-02-20 20:56:20');
INSERT INTO `payments` VALUES (23, 26, 'ORD20260225014234283AF5', 'PAY2026022501423678D137BC', 'alipay', 339.00, 1, NULL, '2026-02-25 01:42:37', '2026-02-25 01:42:36', '2026-02-25 01:42:36');
INSERT INTO `payments` VALUES (24, 28, 'ORD20260225213925C477EC', 'PAY2026022521393150F514C5', 'alipay', 40.00, 1, NULL, '2026-02-25 21:39:34', '2026-02-25 21:39:31', '2026-02-25 21:39:31');
INSERT INTO `payments` VALUES (25, 29, 'ORD20260225213952E9F38F', 'PAY20260225214008DD30B205', 'alipay', 92.00, 1, NULL, '2026-02-25 21:40:11', '2026-02-25 21:40:08', '2026-02-25 21:40:08');
INSERT INTO `payments` VALUES (26, 31, 'ORD2026022521541578460E', 'PAY202602252154180C2616DA', 'alipay', 15.00, 1, NULL, '2026-02-25 21:54:28', '2026-02-25 21:54:18', '2026-02-25 21:54:18');
INSERT INTO `payments` VALUES (27, 32, 'ORD202602252154421D2BC6', 'PAY2026022521544598CF08A6', 'alipay', 92.00, 1, NULL, '2026-02-25 21:54:50', '2026-02-25 21:54:45', '2026-02-25 21:54:45');
INSERT INTO `payments` VALUES (28, 33, 'ORD202602252157465C809F', 'PAY20260225215748EF826F4D', 'alipay', 25.00, 1, NULL, '2026-02-25 21:57:58', '2026-02-25 21:57:49', '2026-02-25 21:57:49');
INSERT INTO `payments` VALUES (29, 34, 'ORD202602252212088C6E70', 'PAY20260225221212FF2A5E84', 'alipay', 25.00, 0, NULL, NULL, '2026-02-25 22:12:12', '2026-02-25 22:12:12');
INSERT INTO `payments` VALUES (30, 35, 'ORD20260225231802E4FD5F', 'PAY20260225231805286E1862', 'alipay', 15.00, 0, NULL, NULL, '2026-02-25 23:18:05', '2026-02-25 23:18:05');
INSERT INTO `payments` VALUES (31, 36, 'ORD202602260023379D314A', 'PAY202602260023425678BC38', 'mock', 30.00, 1, NULL, '2026-02-26 00:23:42', '2026-02-26 00:23:42', '2026-02-26 00:23:42');
INSERT INTO `payments` VALUES (32, 37, 'ORD20260226002440A800A3', 'PAY20260226002445F0FD7B35', 'mock', 92.00, 2, NULL, NULL, '2026-02-26 00:24:45', '2026-02-26 00:24:45');
INSERT INTO `payments` VALUES (33, 41, 'ORD20260316212642165B13', 'PAY2026031621264908933473', 'mock', 15.00, 1, NULL, '2026-03-16 21:26:49', '2026-03-16 21:26:49', '2026-03-16 21:26:49');
INSERT INTO `payments` VALUES (34, 42, 'ORD20260316212704E8163D', 'PAY2026031621270823DA99F0', 'mock', 39.00, 1, NULL, '2026-03-16 21:27:09', '2026-03-16 21:27:08', '2026-03-16 21:27:08');
INSERT INTO `payments` VALUES (35, 53, 'ORD20260323224948559F59', 'PAY202603232249515EA26163', 'mock', 36.00, 1, NULL, '2026-03-23 22:49:51', '2026-03-23 22:49:51', '2026-03-23 22:49:51');
INSERT INTO `payments` VALUES (36, 54, 'ORD20260323225233147EC0', 'PAY2026032322523880EC8E51', 'mock', 26.00, 1, NULL, '2026-03-23 22:52:38', '2026-03-23 22:52:38', '2026-03-23 22:52:38');
INSERT INTO `payments` VALUES (37, 58, 'ORD20260324144313EA13F8', 'PAY20260324144330FE0CA25A', 'mock', 117.00, 1, NULL, '2026-03-24 14:43:30', '2026-03-24 14:43:30', '2026-03-24 14:43:30');
INSERT INTO `payments` VALUES (38, 59, 'ORD20260324151641DF86D8', 'PAY202603241517161ACCA9E7', 'mock', 9.00, 1, NULL, '2026-03-24 15:17:16', '2026-03-24 15:17:16', '2026-03-24 15:17:16');
INSERT INTO `payments` VALUES (39, 60, 'ORD20260324152308A95C67', 'PAY20260324152311859917CD', 'mock', 9.00, 1, NULL, '2026-03-24 15:23:11', '2026-03-24 15:23:11', '2026-03-24 15:23:11');
INSERT INTO `payments` VALUES (40, 62, 'ORD202603241554029E7F80', 'PAY2026032416033454C4C7AA', 'mock', 60.00, 1, NULL, '2026-03-24 16:03:35', '2026-03-24 16:03:34', '2026-03-24 16:03:34');
INSERT INTO `payments` VALUES (41, 63, 'ORD202603241607119040F1', 'PAY20260324160720BFC47418', 'mock', 22.00, 2, '余额不足', NULL, '2026-03-24 16:07:20', '2026-03-24 16:07:20');
INSERT INTO `payments` VALUES (42, 64, 'ORD20260324202831ED5241', 'PAY202603242029465FA05CD7', 'mock', 12.00, 1, NULL, '2026-03-24 20:29:47', '2026-03-24 20:29:46', '2026-03-24 20:29:46');

-- ----------------------------
-- Table structure for product_images
-- ----------------------------
DROP TABLE IF EXISTS `product_images`;
CREATE TABLE `product_images`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_images
-- ----------------------------

-- ----------------------------
-- Table structure for product_specs
-- ----------------------------
DROP TABLE IF EXISTS `product_specs`;
CREATE TABLE `product_specs`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规格ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `spec_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规格名称（如：500g装、1kg装）',
  `spec_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '规格值',
  `price` decimal(10, 2) NOT NULL COMMENT '规格价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价（用于显示折扣）',
  `stock` int(11) NULL DEFAULT 0 COMMENT '规格库存',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品规格表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product_specs
-- ----------------------------
INSERT INTO `product_specs` VALUES (1, 7, '500g', NULL, 20.00, NULL, 20, 0, '2026-02-25 15:14:23', '2026-02-25 15:14:23');
INSERT INTO `product_specs` VALUES (2, 7, '1kg', NULL, 38.00, NULL, 18, 1, '2026-02-25 15:14:23', '2026-02-25 15:14:23');
INSERT INTO `product_specs` VALUES (3, 7, '2kg', NULL, 70.00, NULL, 20, 2, '2026-02-25 15:14:23', '2026-02-25 15:14:23');
INSERT INTO `product_specs` VALUES (4, 1, '500g', NULL, 15.00, NULL, 90, 0, '2026-03-16 18:03:15', '2026-03-16 18:03:15');
INSERT INTO `product_specs` VALUES (5, 1, '1kg', NULL, 28.00, NULL, 99, 1, '2026-03-16 18:03:15', '2026-03-16 18:03:15');
INSERT INTO `product_specs` VALUES (6, 1, '2kg', NULL, 50.00, NULL, 100, 2, '2026-03-16 18:03:15', '2026-03-16 18:03:15');
INSERT INTO `product_specs` VALUES (7, 8, '500g', NULL, 7.00, NULL, 100, 0, '2026-03-16 20:55:34', '2026-03-16 20:55:34');
INSERT INTO `product_specs` VALUES (8, 8, '1kg', NULL, 12.00, NULL, 100, 1, '2026-03-16 20:55:34', '2026-03-16 20:55:34');
INSERT INTO `product_specs` VALUES (9, 9, '500g', NULL, 12.00, NULL, 100, 0, '2026-03-16 20:58:24', '2026-03-16 20:58:24');
INSERT INTO `product_specs` VALUES (10, 9, '1kg', NULL, 20.00, NULL, 80, 1, '2026-03-16 20:58:24', '2026-03-16 20:58:24');
INSERT INTO `product_specs` VALUES (11, 9, '2kg', NULL, 35.00, NULL, 90, 2, '2026-03-16 20:58:24', '2026-03-16 20:58:24');
INSERT INTO `product_specs` VALUES (12, 10, '500g', NULL, 18.00, NULL, 100, 0, '2026-03-16 21:01:20', '2026-03-16 21:01:20');
INSERT INTO `product_specs` VALUES (13, 10, '100g', NULL, 32.00, NULL, 0, 1, '2026-03-16 21:01:20', '2026-03-16 21:01:20');
INSERT INTO `product_specs` VALUES (14, 11, '500g', NULL, 9.00, NULL, 98, 0, '2026-03-16 21:05:15', '2026-03-16 21:05:15');
INSERT INTO `product_specs` VALUES (15, 11, '1kg', NULL, 15.00, NULL, 100, 1, '2026-03-16 21:05:15', '2026-03-16 21:05:15');
INSERT INTO `product_specs` VALUES (16, 11, '2kg', NULL, 28.00, NULL, 0, 2, '2026-03-16 21:05:15', '2026-03-16 21:05:15');
INSERT INTO `product_specs` VALUES (17, 12, '500g', NULL, 5.00, NULL, 0, 0, '2026-03-16 21:08:53', '2026-03-16 21:08:53');
INSERT INTO `product_specs` VALUES (18, 13, '500g', NULL, 18.00, NULL, 1000, 0, '2026-03-16 21:09:39', '2026-03-16 21:09:39');
INSERT INTO `product_specs` VALUES (19, 14, '500g', NULL, 7.00, NULL, 0, 0, '2026-03-16 21:12:47', '2026-03-16 21:12:47');
INSERT INTO `product_specs` VALUES (20, 14, '1kg', NULL, 13.00, NULL, 0, 1, '2026-03-16 21:12:47', '2026-03-16 21:12:47');
INSERT INTO `product_specs` VALUES (21, 14, '2kg', NULL, 20.00, NULL, 0, 2, '2026-03-16 21:12:47', '2026-03-16 21:12:47');
INSERT INTO `product_specs` VALUES (22, 15, '500g', NULL, 8.00, NULL, 200, 0, '2026-03-16 21:18:34', '2026-03-16 21:18:34');
INSERT INTO `product_specs` VALUES (23, 15, '1kg', NULL, 15.00, NULL, 300, 1, '2026-03-16 21:18:34', '2026-03-16 21:18:34');
INSERT INTO `product_specs` VALUES (24, 15, '2kg', NULL, 25.00, NULL, 200, 2, '2026-03-16 21:18:34', '2026-03-16 21:18:34');
INSERT INTO `product_specs` VALUES (25, 16, '1盒', NULL, 15.00, NULL, 100, 0, '2026-03-16 21:21:08', '2026-03-16 21:21:08');
INSERT INTO `product_specs` VALUES (26, 16, '2盒', NULL, 25.00, NULL, 200, 1, '2026-03-16 21:21:08', '2026-03-16 21:21:08');
INSERT INTO `product_specs` VALUES (27, 16, '4盒', NULL, 48.00, NULL, 300, 2, '2026-03-16 21:21:08', '2026-03-16 21:21:08');
INSERT INTO `product_specs` VALUES (28, 17, '1盒', NULL, 30.00, NULL, 100, 0, '2026-03-16 21:23:33', '2026-03-16 21:23:33');
INSERT INTO `product_specs` VALUES (29, 17, '2盒', NULL, 56.00, NULL, 200, 1, '2026-03-16 21:23:33', '2026-03-16 21:23:33');
INSERT INTO `product_specs` VALUES (30, 17, '4盒', NULL, 99.00, NULL, 299, 2, '2026-03-16 21:23:33', '2026-03-16 21:23:33');
INSERT INTO `product_specs` VALUES (31, 18, '一个', NULL, 15.00, NULL, 100, 0, '2026-03-23 22:49:07', '2026-03-23 22:49:07');
INSERT INTO `product_specs` VALUES (32, 18, '二个', NULL, 29.00, NULL, 122, 1, '2026-03-23 22:49:07', '2026-03-23 22:49:07');
INSERT INTO `product_specs` VALUES (33, 18, '三个', NULL, 40.00, NULL, 122, 2, '2026-03-23 22:49:07', '2026-03-23 22:49:07');
INSERT INTO `product_specs` VALUES (34, 19, '500g', NULL, 22.00, NULL, 333, 0, '2026-03-23 22:48:59', '2026-03-23 22:48:59');
INSERT INTO `product_specs` VALUES (35, 19, '1kg', NULL, 40.00, NULL, 220, 1, '2026-03-23 22:48:59', '2026-03-23 22:48:59');
INSERT INTO `product_specs` VALUES (36, 19, '2kg', NULL, 70.00, NULL, 120, 2, '2026-03-23 22:48:59', '2026-03-23 22:48:59');
INSERT INTO `product_specs` VALUES (37, 20, '500g', NULL, 14.00, NULL, 333, 0, '2026-03-23 22:51:41', '2026-03-23 22:51:41');
INSERT INTO `product_specs` VALUES (38, 20, '1kg', NULL, 25.00, NULL, 212, 1, '2026-03-23 22:51:41', '2026-03-23 22:51:41');
INSERT INTO `product_specs` VALUES (39, 21, '500g', NULL, 16.00, NULL, 100, 0, '2026-03-23 22:48:38', '2026-03-23 22:48:38');
INSERT INTO `product_specs` VALUES (40, 21, '1kg', NULL, 30.00, NULL, 100, 1, '2026-03-23 22:48:38', '2026-03-23 22:48:38');
INSERT INTO `product_specs` VALUES (41, 22, '1盒125g', NULL, 12.00, NULL, 223, 0, '2026-03-23 22:48:34', '2026-03-23 22:48:34');
INSERT INTO `product_specs` VALUES (42, 22, '2盒', NULL, 20.00, NULL, 211, 1, '2026-03-23 22:48:34', '2026-03-23 22:48:34');
INSERT INTO `product_specs` VALUES (43, 22, '4盒', NULL, 36.00, NULL, 110, 2, '2026-03-23 22:48:34', '2026-03-23 22:48:34');
INSERT INTO `product_specs` VALUES (44, 23, '500g', NULL, 9.00, NULL, 100, 0, '2026-03-23 22:48:18', '2026-03-23 22:48:18');
INSERT INTO `product_specs` VALUES (45, 23, '1kg', NULL, 17.00, NULL, 222, 1, '2026-03-23 22:48:18', '2026-03-23 22:48:18');
INSERT INTO `product_specs` VALUES (46, 25, '500g', NULL, 23.00, NULL, 100, 0, '2026-03-23 22:48:03', '2026-03-23 22:48:03');
INSERT INTO `product_specs` VALUES (47, 25, '1kg', NULL, 40.00, NULL, 100, 1, '2026-03-23 22:48:03', '2026-03-23 22:48:03');

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商品详情描述',
  `main_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主图URL',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价（用于显示折扣）',
  `stock` int(11) NULL DEFAULT 0 COMMENT '库存数量',
  `sales_count` int(11) NULL DEFAULT 0 COMMENT '销量',
  `freshness_level` tinyint(1) NULL DEFAULT 5 COMMENT '新鲜度：1-5级',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已上架，2-已下架，3-审核拒绝',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `audit_admin_id` bigint(20) NULL DEFAULT NULL COMMENT '审核管理员ID',
  `audit_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审核备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `special_enabled` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否特价：0否 1是',
  `special_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '特价方式：DISCOUNT / BUY_N_GET_M',
  `special_discount` decimal(6, 4) NULL DEFAULT NULL COMMENT '折扣(0~1)，如 0.8 表示 8折',
  `special_buy` int(11) NULL DEFAULT NULL COMMENT '买N送M：N',
  `special_free` int(11) NULL DEFAULT NULL COMMENT '买N送M：M',
  `special_label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展示标签：如 8折 / 买2送1',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_sales_count`(`sales_count`) USING BTREE,
  INDEX `idx_price`(`price`) USING BTREE,
  INDEX `idx_products_special_enabled`(`special_enabled`) USING BTREE,
  FULLTEXT INDEX `ft_name_desc`(`name`, `description`)
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (1, 2, 3, '苹果', '美味的苹果', '/uploads/20260131154851_97f49677.png', 12.00, 15.00, 500, 6, 5, 1, '2026-01-31 15:57:14', 1, '', '2026-01-31 15:48:57', '2026-01-31 15:48:57', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (2, 2, 2, '蓝莓', '新鲜的蓝莓', '/uploads/20260131164738_97b8183e.jpg', 25.00, 45.00, 227, 6, 5, 1, '2026-01-31 16:53:56', 1, '', '2026-01-31 16:47:49', '2026-01-31 16:47:49', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (3, 2, 1, '草莓', '1111111', '/uploads/20260131165156_08c78542.jpg', 92.00, 92.00, 1110, 11, 5, 1, '2026-01-31 16:53:53', 1, '', '2026-01-31 16:52:00', '2026-01-31 16:52:00', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (4, 2, 1, '葡萄', '1231231232', '/uploads/20260131165217_413a55cf.jpg', 12.00, 30.00, 102, 8, 4, 1, '2026-01-31 16:53:52', 1, '', '2026-01-31 16:52:40', '2026-01-31 16:52:40', 1, 'DISCOUNT', 0.4000, NULL, NULL, '4折');
INSERT INTO `products` VALUES (5, 2, 5, '猕猴桃', '1123123', '/uploads/20260131165251_24be70e3.jpg', 80.00, 200.00, 114, 9, 5, 1, '2026-01-31 16:53:48', 1, '', '2026-01-31 16:53:27', '2026-01-31 16:53:27', 1, 'DISCOUNT', 0.4000, 2, 1, '4折');
INSERT INTO `products` VALUES (6, 3, 5, '西瓜', '无子西瓜，人工大棚种植', '/uploads/20260220202703_bf8cb8e7.jpg', 15.00, 20.00, 292, 8, 5, 1, '2026-02-20 20:28:40', 1, '', '2026-02-20 20:28:24', '2026-02-20 20:28:24', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (7, 2, 4, '22', '', '/uploads/20260225144023_d2d19d05.png', 20.00, 20.00, 109, 2, 5, 1, '2026-02-25 14:42:06', 1, '', '2026-02-25 14:41:25', '2026-02-25 14:41:25', 0, 'DISCOUNT', 0.8000, NULL, NULL, '8折');
INSERT INTO `products` VALUES (8, 4, 4, '赣南脐橙', '产地直供赣南脐橙，果皮薄易剥，果肉饱满多汁，酸甜比例适中，富含维生素 C，现摘现发不打蜡', '/uploads/20260316205257_19228226.png', 7.00, 7.00, 299, 1, 5, 1, '2026-03-16 21:10:17', 1, '', '2026-03-16 20:55:20', '2026-03-16 20:55:20', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (9, 4, 1, '烟台红富士苹果', '正宗烟台红富士，脆甜多汁，果肉细腻无渣，果形端正，皮薄耐放，家庭日常食用优选', '/uploads/20260316205727_5a104ca6.png', 12.00, 12.00, 1000, 0, 5, 1, '2026-03-16 21:10:15', 1, '', '2026-03-16 20:58:15', '2026-03-16 20:58:15', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (10, 4, 3, '巨峰葡萄', '巨峰葡萄果粒饱满紧实，果皮薄韧有果香，果肉软糯多汁，自带玫瑰香气，甜度高达 18 度', '/uploads/20260316210057_c3bf2e3c.jpg', 18.00, 18.00, 1000, 0, 5, 1, '2026-03-16 21:10:13', 1, '', '2026-03-16 21:01:20', '2026-03-16 21:01:20', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (11, 4, 2, '阳山水蜜桃', '正宗阳山水蜜桃，果肉软糯细腻，汁水丰盈，入口即化，果香浓郁，夏季限定鲜品', '/uploads/20260316210429_df44669c.jpg', 9.00, 9.00, 1997, 3, 5, 1, '2026-03-16 21:10:11', 1, '', '2026-03-16 21:05:15', '2026-03-16 21:05:15', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (12, 4, 5, '麒麟西瓜', '麒麟西瓜，瓜形圆润，瓜皮薄韧不易裂，果肉鲜红沙脆，甜度高，夏季消暑必备	单果', '/uploads/20260316210819_cc4c78a4.jpg', 5.00, 5.00, 3000, 0, 5, 1, '2026-03-16 21:10:09', 1, '', '2026-03-16 21:08:53', '2026-03-16 21:08:53', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (13, 4, 6, '海南金钻凤梨', '海南金钻凤梨，果肉金黄细腻，无纤维丝，甜度高果香浓郁，去皮即食无需泡盐水', '/uploads/20260316210919_df982175.jpg', 18.00, 18.00, 1000, 0, 4, 1, '2026-03-16 21:10:07', 1, '', '2026-03-16 21:09:39', '2026-03-16 21:09:39', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (14, 4, 4, '沃柑', '广西沃柑，果皮易剥，果肉细嫩多汁，清甜爽口，富含膳食纤维，老少皆宜', '/uploads/20260316211212_2c856765.jpg', 7.00, 7.00, 2000, 0, 3, 1, '2026-03-16 21:24:02', 1, '', '2026-03-16 21:12:47', '2026-03-16 21:12:47', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (15, 4, 1, '新疆香梨', '新疆香梨，果形小巧精致，果肉脆甜无渣，皮薄肉厚，自带淡淡花香，便携装优选', '/uploads/20260316211740_9fb7898a.jpg', 8.00, 8.00, 1999, 1, 5, 1, '2026-03-16 21:24:00', 1, '', '2026-03-16 21:18:34', '2026-03-16 21:18:34', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (16, 4, 3, '丹东红颜草莓', '丹东红颜草莓，果形饱满红润，果肉香甜软糯，汁水充足，富含花青素，精品礼盒装', '/uploads/20260316211951_cbb011bd.jpg', 15.00, 15.00, 1000, 0, 5, 1, '2026-03-16 21:23:58', 1, '', '2026-03-16 21:21:08', '2026-03-16 21:21:08', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (17, 4, 2, '车厘子 (JJ 级)	', '智利进口 JJ 级车厘子，果径 28-30mm，果肉紧实脆甜，果皮深红有光泽，冷链保鲜', '/uploads/20260316212244_fa2c4fc4.jpg', 30.00, 30.00, 999, 1, 5, 1, '2026-03-16 21:23:56', 1, '', '2026-03-16 21:23:33', '2026-03-16 21:23:33', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (18, 5, 5, '玉菇甜瓜', '海南玉菇甜瓜，瓜皮翠绿薄韧，果肉洁白软糯，口感清甜如蜜，果香清新', '/uploads/20260323193226_4366580e.jpg', 15.00, 15.00, 1000, 0, 4, 1, '2026-03-23 19:42:59', 1, '', '2026-03-23 19:33:16', '2026-03-23 19:33:16', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (19, 5, 6, '泰国山竹', '泰国进口山竹，果壳厚实有弹性，果肉白嫩细腻，清甜多汁，被称为 “水果皇后”', '/uploads/20260323193450_bc0a46d1.png', 22.00, 22.00, 999, 0, 5, 1, '2026-03-23 19:42:59', 1, '', '2026-03-23 19:35:17', '2026-03-23 19:35:17', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (20, 5, 4, '耙耙柑', '四川春见耙耙柑，果皮软趴易剥，果肉饱满无籽，口感清甜化渣，汁水超足', '/uploads/20260323193537_b62dd0be.png', 11.20, 14.00, 999, 0, 5, 1, '2026-03-23 19:42:59', 1, '', '2026-03-23 19:36:08', '2026-03-23 19:36:08', 1, 'DISCOUNT', 0.8000, NULL, NULL, '8折');
INSERT INTO `products` VALUES (21, 5, 1, '雪花梨', '河北雪花梨，果肉洁白细腻，汁水丰盈，清甜爽口，适合炖煮、榨汁，家庭常备', '/uploads/20260323193622_37ffd868.png', 16.00, 16.00, 3000, 0, 5, 1, '2026-03-23 19:42:58', 1, '', '2026-03-23 19:36:56', '2026-03-23 19:36:56', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (22, 5, 3, '蓝莓', '国产精品蓝莓，果粒饱满紧实，果皮蓝润有光泽，果肉酸甜适口，富含花青素', '/uploads/20260323193733_1234460f.png', 12.00, 12.00, 8998, 2, 5, 1, '2026-03-23 19:42:58', 1, '', '2026-03-23 19:38:28', '2026-03-23 19:38:28', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (23, 5, 2, '贵妃芒', '海南贵妃芒，果形修长，果肉金黄细腻，无纤维丝，甜度高，成熟后软糯香甜', '/uploads/20260323193854_45a52ddd.png', 9.00, 9.00, 5555, 0, 5, 1, '2026-03-23 19:42:58', 1, '', '2026-03-23 19:39:22', '2026-03-23 19:39:22', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (24, 5, 6, '红心火龙果', '越南红心火龙果，果肉鲜红饱满，汁水丰盈，甜度适中，富含膳食纤维，低脂健康', '/uploads/20260323194025_31475c48.png', 7.00, 7.00, 700, 0, 5, 1, '2026-03-23 19:42:58', 1, '', '2026-03-23 19:40:40', '2026-03-23 19:40:40', 1, 'BUY_N_GET_M', NULL, 2, 1, '买2送1');
INSERT INTO `products` VALUES (25, 5, 4, '砂糖橘', '广东砂糖橘，果形小巧饱满，果皮薄易剥，果肉清甜无渣，甜度高达 16 度，年节必备', '/uploads/20260323194114_e1455831.png', 23.00, 23.00, 6000, 0, 5, 1, '2026-03-23 19:42:58', 1, '', '2026-03-23 19:41:49', '2026-03-23 19:41:49', 0, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `products` VALUES (26, 5, 3, '徐香猕猴桃', '陕西眉县徐香猕猴桃，果肉翠绿，酸甜适口，富含维生素 C，果肉细腻无籽	', '/uploads/20260323194232_ee563c79.png', 13.00, 13.00, 10, 2, 3, 1, '2026-03-23 19:42:58', 1, '', '2026-03-23 19:42:35', '2026-03-23 19:42:35', 1, 'BUY_N_GET_M', NULL, 2, 1, '买2送1');

-- ----------------------------
-- Table structure for promotion_products
-- ----------------------------
DROP TABLE IF EXISTS `promotion_products`;
CREATE TABLE `promotion_products`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `promotion_id` bigint(20) NOT NULL COMMENT '活动ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `promotion_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '活动价格',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序顺序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_promotion_id`(`promotion_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动商品关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of promotion_products
-- ----------------------------

-- ----------------------------
-- Table structure for promotions
-- ----------------------------
DROP TABLE IF EXISTS `promotions`;
CREATE TABLE `promotions`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `type` tinyint(1) NOT NULL COMMENT '活动类型：1-特价，2-季节性推荐',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动描述',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-已结束，1-进行中，2-未开始',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_time`(`start_time`, `end_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '营销活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of promotions
-- ----------------------------

-- ----------------------------
-- Table structure for review_likes
-- ----------------------------
DROP TABLE IF EXISTS `review_likes`;
CREATE TABLE `review_likes`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `review_id` bigint(20) NOT NULL COMMENT '评价ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '点赞类型：1-认同，2-不认同',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_review_user`(`review_id`, `user_id`) USING BTREE,
  INDEX `idx_review_id`(`review_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评价点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review_likes
-- ----------------------------
INSERT INTO `review_likes` VALUES (2, 1, 1, 1, '2026-02-15 23:08:20');
INSERT INTO `review_likes` VALUES (3, 2, 1, 1, '2026-02-15 23:08:28');
INSERT INTO `review_likes` VALUES (4, 7, 1, 1, '2026-02-25 14:39:01');
INSERT INTO `review_likes` VALUES (5, 9, 1, 1, '2026-02-25 18:03:06');
INSERT INTO `review_likes` VALUES (6, 4, 1, 1, '2026-02-25 18:03:09');
INSERT INTO `review_likes` VALUES (7, 8, 1, 2, '2026-03-16 17:12:23');

-- ----------------------------
-- Table structure for review_replies
-- ----------------------------
DROP TABLE IF EXISTS `review_replies`;
CREATE TABLE `review_replies`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `review_id` bigint(20) NOT NULL COMMENT '评价ID',
  `merchant_id` bigint(20) NULL DEFAULT NULL COMMENT '商家ID（消费者回复时为NULL，商家回复时填写）',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID（商家回复时为NULL，消费者回复时填写）',
  `reply_type` tinyint(1) NULL DEFAULT 1 COMMENT '回复类型：1-商家回复，2-消费者回复',
  `reply_to_id` bigint(20) NULL DEFAULT NULL COMMENT '回复目标ID（回复某个回复时填写，直接回复评价时为NULL）',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_id`(`review_id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_reply_to_id`(`reply_to_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评价回复表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review_replies
-- ----------------------------
INSERT INTO `review_replies` VALUES (1, 3, 2, NULL, 1, NULL, '故意黑吧\n', '2026-01-31 23:18:38');
INSERT INTO `review_replies` VALUES (2, 7, NULL, 2, 2, NULL, '谢谢', '2026-02-01 01:22:51');
INSERT INTO `review_replies` VALUES (3, 2, NULL, 2, 2, NULL, '还行啦', '2026-02-01 01:23:33');
INSERT INTO `review_replies` VALUES (4, 2, NULL, 2, 2, NULL, '1212', '2026-02-01 01:23:40');
INSERT INTO `review_replies` VALUES (5, 4, NULL, 1, 2, NULL, '1', '2026-02-01 01:24:32');
INSERT INTO `review_replies` VALUES (6, 2, NULL, 1, 2, NULL, '咋样', '2026-02-01 01:41:31');
INSERT INTO `review_replies` VALUES (7, 6, NULL, 2, 2, NULL, '谢谢您的评价', '2026-02-01 01:43:07');
INSERT INTO `review_replies` VALUES (8, 7, NULL, 2, 2, NULL, '111', '2026-02-01 01:44:15');
INSERT INTO `review_replies` VALUES (9, 6, NULL, 2, 2, NULL, '21341', '2026-02-01 01:44:18');
INSERT INTO `review_replies` VALUES (10, 4, NULL, 2, 2, NULL, '121', '2026-02-01 01:57:16');
INSERT INTO `review_replies` VALUES (11, 2, NULL, 2, 2, NULL, '231234', '2026-02-01 01:57:22');
INSERT INTO `review_replies` VALUES (12, 7, NULL, 2, 2, NULL, '123412', '2026-02-01 01:57:35');
INSERT INTO `review_replies` VALUES (13, 7, NULL, 2, 2, NULL, '12423啊飒飒的', '2026-02-01 02:01:25');
INSERT INTO `review_replies` VALUES (14, 5, NULL, 2, 2, NULL, '谢谢', '2026-02-01 02:03:58');
INSERT INTO `review_replies` VALUES (15, 8, NULL, 1, 2, NULL, '欢迎下次再来', '2026-02-14 17:18:26');
INSERT INTO `review_replies` VALUES (16, 8, NULL, 2, 2, NULL, '水果新鲜不', '2026-02-14 17:20:14');
INSERT INTO `review_replies` VALUES (17, 3, NULL, 1, 2, NULL, '@水果大王 噢哦', '2026-02-14 21:00:59');
INSERT INTO `review_replies` VALUES (18, 6, NULL, 1, 2, NULL, '111 11', '2026-02-14 21:01:09');
INSERT INTO `review_replies` VALUES (19, 6, NULL, 1, 2, NULL, '11111', '2026-02-14 21:01:12');
INSERT INTO `review_replies` VALUES (20, 6, NULL, 1, 2, NULL, '22222@我是奶龙', '2026-02-14 21:01:23');
INSERT INTO `review_replies` VALUES (21, 3, NULL, 1, 2, NULL, '@水果大王 还好', '2026-02-14 21:01:44');
INSERT INTO `review_replies` VALUES (22, 8, NULL, 1, 2, NULL, '你你你】', '2026-02-14 21:02:55');
INSERT INTO `review_replies` VALUES (23, 5, NULL, 1, 2, NULL, '好吃不', '2026-02-14 21:03:11');
INSERT INTO `review_replies` VALUES (24, 8, 2, NULL, 1, NULL, '谢谢', '2026-02-15 20:34:58');
INSERT INTO `review_replies` VALUES (25, 5, NULL, 1, 2, NULL, '@我是奶龙 1111', '2026-02-15 20:45:48');
INSERT INTO `review_replies` VALUES (26, 2, NULL, 1, 2, NULL, '1', '2026-02-15 20:46:39');
INSERT INTO `review_replies` VALUES (27, 2, NULL, 1, 2, NULL, '@我是奶龙 11', '2026-02-15 20:46:56');
INSERT INTO `review_replies` VALUES (28, 8, NULL, 1, 2, 24, '@水果大王 4444', '2026-02-15 23:42:14');
INSERT INTO `review_replies` VALUES (29, 8, NULL, 1, 2, 16, '@我是奶龙 zzzz', '2026-02-15 23:42:36');
INSERT INTO `review_replies` VALUES (30, 4, NULL, 1, 2, 10, '@我是奶龙 111', '2026-02-17 17:23:03');
INSERT INTO `review_replies` VALUES (31, 4, 2, NULL, 1, NULL, '111', '2026-02-17 17:23:53');
INSERT INTO `review_replies` VALUES (32, 1, 2, NULL, 1, NULL, '欢迎下次再来', '2026-02-18 15:07:34');
INSERT INTO `review_replies` VALUES (33, 4, NULL, 1, 2, 31, '@水果大王 111', '2026-02-18 15:13:57');
INSERT INTO `review_replies` VALUES (34, 4, 2, NULL, 1, 33, '11', '2026-02-18 15:15:28');
INSERT INTO `review_replies` VALUES (35, 9, 2, NULL, 1, NULL, '谢谢支持', '2026-02-25 17:56:35');
INSERT INTO `review_replies` VALUES (36, 9, NULL, 2, 2, NULL, '味道怎么样', '2026-02-25 17:57:37');
INSERT INTO `review_replies` VALUES (37, 11, NULL, 4, 2, NULL, '真的很不错，推荐', '2026-03-23 23:01:47');
INSERT INTO `review_replies` VALUES (38, 11, NULL, 3, 2, NULL, '水果新鲜不', '2026-03-23 23:02:16');

-- ----------------------------
-- Table structure for reviews
-- ----------------------------
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `merchant_id` bigint(20) NOT NULL COMMENT '商家ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `rating` tinyint(1) NOT NULL COMMENT '评分：1-5星',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价图片（JSON数组）',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_merchant_id`(`merchant_id`) USING BTREE,
  INDEX `idx_product_id`(`product_id`) USING BTREE,
  INDEX `idx_rating`(`rating`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reviews
-- ----------------------------
INSERT INTO `reviews` VALUES (1, 1, 1, 2, 5, 5, '好吃', NULL, 1, '2026-01-31 20:01:38', '2026-01-31 20:01:38');
INSERT INTO `reviews` VALUES (2, 5, 2, 2, 5, 4, '挺不错的，但是物流有点慢', NULL, 1, '2026-01-31 23:06:05', '2026-01-31 23:06:05');
INSERT INTO `reviews` VALUES (3, 6, 2, 2, 4, 3, '一般把 ', NULL, 1, '2026-01-31 23:17:46', '2026-01-31 23:17:46');
INSERT INTO `reviews` VALUES (4, 9, 1, 2, 5, 5, '还行', NULL, 1, '2026-02-01 01:02:03', '2026-02-01 01:02:03');
INSERT INTO `reviews` VALUES (5, 7, 2, 2, 1, 5, '1332', NULL, 1, '2026-02-01 01:02:55', '2026-02-01 01:02:55');
INSERT INTO `reviews` VALUES (6, 10, 2, 2, 4, 5, '111111', NULL, 1, '2026-02-01 01:08:08', '2026-02-01 01:08:08');
INSERT INTO `reviews` VALUES (7, 11, 2, 2, 3, 5, '1111111', NULL, 1, '2026-02-01 01:19:23', '2026-02-01 01:19:23');
INSERT INTO `reviews` VALUES (8, 12, 1, 2, 1, 5, '好吃', NULL, 1, '2026-02-14 17:17:56', '2026-02-14 17:17:56');
INSERT INTO `reviews` VALUES (9, 26, 1, 2, 5, 5, '蛮不错，下次再来', NULL, 1, '2026-02-25 17:56:17', '2026-02-25 17:56:17');
INSERT INTO `reviews` VALUES (10, 54, 4, 5, 26, 5, '还不错', NULL, 1, '2026-03-23 23:00:31', '2026-03-23 23:00:31');
INSERT INTO `reviews` VALUES (11, 53, 4, 5, 22, 5, '好吃，蓝莓有大又甜', NULL, 1, '2026-03-23 23:01:05', '2026-03-23 23:01:05');

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置值',
  `config_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'after_sale_deadline', '7', '售后时效（天）', '2026-01-31 14:48:36', '2026-01-31 14:48:36');
INSERT INTO `system_config` VALUES (2, 'default_delivery_time', '3', '默认配送时效（天）', '2026-01-31 14:48:36', '2026-01-31 14:48:36');

-- ----------------------------
-- Table structure for user_coupons
-- ----------------------------
DROP TABLE IF EXISTS `user_coupons`;
CREATE TABLE `user_coupons`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `coupon_id` bigint(20) NOT NULL COMMENT '优惠券ID',
  `status` int(11) NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '使用的订单ID',
  `used_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `receive_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_expire_time`(`expire_time`) USING BTREE,
  CONSTRAINT `fk_user_coupons_coupon` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_coupons_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户优惠券表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_coupons
-- ----------------------------
INSERT INTO `user_coupons` VALUES (1, 1, 1, 1, 25, '2026-02-22 22:05:42', '2026-02-22 22:01:29', '2026-02-23 00:00:00');
INSERT INTO `user_coupons` VALUES (2, 1, 3, 2, NULL, NULL, '2026-02-22 22:06:46', '2026-02-24 00:00:00');
INSERT INTO `user_coupons` VALUES (3, 1, 4, 2, NULL, NULL, '2026-02-22 22:15:55', '2026-02-24 00:00:00');
INSERT INTO `user_coupons` VALUES (4, 1, 5, 2, NULL, NULL, '2026-02-22 22:23:03', '2026-02-24 00:00:00');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（加密存储）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `register_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  INDEX `idx_phone`(`phone`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消费者用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'user', '123456', '嚣张', '13145678375', '', '/uploads/20260214211719_0bab12b3.jpg', 1, '2026-01-31 15:21:18', '2026-03-24 20:23:17', '2026-01-31 15:21:18', '2026-01-31 15:21:18');
INSERT INTO `users` VALUES (2, 'A001', '123456', '我是奶龙', '13141245362', '', '/uploads/20260214221726_cbf4bc75.png', 1, '2026-01-31 23:03:19', '2026-02-25 21:30:41', '2026-01-31 23:03:18', '2026-01-31 23:03:18');
INSERT INTO `users` VALUES (3, 'A002', '123456', '不吃香菜', '13246387645', '', '/uploads/20260220185004_ee7e462a.jpg', 1, '2026-02-20 18:48:38', '2026-03-24 20:33:00', '2026-02-20 18:48:37', '2026-02-20 18:48:37');
INSERT INTO `users` VALUES (4, 'A003', '123456', 'A003', '14528053768', '', '/uploads/20260323193038_9a94cec9.png', 1, '2026-03-23 19:29:29', '2026-03-24 20:35:03', '2026-03-23 19:29:28', '2026-03-23 19:29:28');
INSERT INTO `users` VALUES (5, 'test46005', '123456', 'test46005', '18800003727', NULL, NULL, 1, '2026-03-23 23:45:13', '2026-03-23 23:45:13', '2026-03-23 23:45:12', '2026-03-23 23:45:12');

-- ----------------------------
-- Table structure for verification_codes
-- ----------------------------
DROP TABLE IF EXISTS `verification_codes`;
CREATE TABLE `verification_codes`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码',
  `type` tinyint(1) NOT NULL COMMENT '类型：1-注册，2-登录，3-找回密码',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `is_used` tinyint(1) NULL DEFAULT 0 COMMENT '是否已使用：0-未使用，1-已使用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_phone`(`phone`) USING BTREE,
  INDEX `idx_code`(`code`) USING BTREE,
  INDEX `idx_expire_time`(`expire_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '验证码表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of verification_codes
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
