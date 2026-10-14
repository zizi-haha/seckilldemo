SET NAMES utf8mb4;

DROP TABLE IF EXISTS `t_seckill_order`;
DROP TABLE IF EXISTS `t_order`;
DROP TABLE IF EXISTS `t_seckill_goods`;
DROP TABLE IF EXISTS `t_user`;
DROP TABLE IF EXISTS `goods`;

CREATE TABLE `goods` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品 ID',
    `goods_name` varchar(16) DEFAULT NULL COMMENT '商品名称',
    `goods_title` varchar(64) DEFAULT NULL COMMENT '商品标题',
    `goods_img` varchar(64) DEFAULT NULL COMMENT '商品图片',
    `goods_detail` longtext COMMENT '商品详情',
    `goods_price` decimal(10, 2) DEFAULT '0.00' COMMENT '商品价格',
    `goods_stock` int DEFAULT '0' COMMENT '商品库存，-1 表示不限制',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO `goods`
    (`id`, `goods_name`, `goods_title`, `goods_img`, `goods_detail`, `goods_price`, `goods_stock`)
VALUES
    (1, 'IPHONE 12', 'IPHONE 12 64GB', 'img/iphone12.png', 'IPHONE', 6299.00, 100),
    (2, 'IPHONE 12 PRO', 'IPHONE 12 PRO 64GB', 'img/iphone12pro.png', 'IPHONE', 9299.00, 100);

CREATE TABLE `t_user` (
    `id` bigint NOT NULL COMMENT '用户 ID，使用手机号',
    `nickname` varchar(255) NOT NULL COMMENT '昵称',
    `password` varchar(32) DEFAULT NULL COMMENT 'MD5(MD5(pass 明文 + 固定 salt) + salt)',
    `salt` varchar(10) DEFAULT NULL COMMENT '盐值',
    `regist_date` datetime DEFAULT NULL COMMENT '注册时间',
    `last_login_date` datetime DEFAULT NULL COMMENT '最后一次登录时间',
    `login_count` int DEFAULT '0' COMMENT '登录次数',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO `t_user`
    (`id`, `nickname`, `password`, `salt`, `regist_date`, `last_login_date`, `login_count`)
VALUES
    (13669255555, 'hh', '6f04dda43ff57ca161ec17880bd43731', '123456', NULL, NULL, 0),
    (13669255556, 'admin', '6f04dda43ff57ca161ec17880bd43731', '123456', '2026-09-16 23:01:26', NULL, 0);

CREATE TABLE `t_seckill_goods` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '秒杀商品 ID',
    `goods_id` bigint DEFAULT NULL COMMENT '商品 ID',
    `seckill_price` decimal(10, 2) DEFAULT '0.00' COMMENT '秒杀价格',
    `stock_count` int DEFAULT NULL COMMENT '秒杀库存数量',
    `start_date` datetime DEFAULT NULL COMMENT '秒杀开始时间',
    `end_date` datetime DEFAULT NULL COMMENT '秒杀结束时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seckill_goods_goods_id` (`goods_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO `t_seckill_goods`
    (`id`, `goods_id`, `seckill_price`, `stock_count`, `start_date`, `end_date`)
VALUES
    (1, 1, 699.00, 9, '2026-09-21 08:00:00', '2026-09-25 08:00:00'),
    (2, 2, 799.00, 9, '2026-09-21 08:00:00', '2026-09-25 08:00:00');

CREATE TABLE `t_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单 ID',
    `user_id` bigint DEFAULT NULL COMMENT '用户 ID',
    `goods_id` bigint DEFAULT NULL COMMENT '商品 ID',
    `delivery_addr_id` bigint DEFAULT NULL COMMENT '收货地址 ID',
    `goods_name` varchar(16) DEFAULT NULL COMMENT '冗余商品名称',
    `goods_count` int DEFAULT '0' COMMENT '商品数量',
    `goods_price` decimal(10, 2) DEFAULT '0.00' COMMENT '商品单价',
    `order_channel` tinyint DEFAULT '0' COMMENT '订单渠道：1 PC，2 Android，3 iOS',
    `status` tinyint DEFAULT '0' COMMENT '订单状态：0 新建未支付，1 已支付，2 已发货，3 已收货，4 已退款，5 已完成',
    `create_date` datetime DEFAULT NULL COMMENT '订单创建时间',
    `pay_date` datetime DEFAULT NULL COMMENT '支付时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 14
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO `t_order`
    (`id`, `user_id`, `goods_id`, `delivery_addr_id`, `goods_name`, `goods_count`,
     `goods_price`, `order_channel`, `status`, `create_date`, `pay_date`)
VALUES
    (12, 13669255555, 1, NULL, NULL, 1, 699.00, 1, 0, '2026-09-21 17:33:28', NULL),
    (13, 13669255555, 2, NULL, NULL, 1, 799.00, 1, 0, '2026-09-21 17:33:42', NULL);

CREATE TABLE `t_seckill_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '秒杀订单 ID',
    `user_id` bigint DEFAULT NULL COMMENT '用户 ID',
    `order_id` bigint DEFAULT NULL COMMENT '订单 ID',
    `goods_id` bigint DEFAULT NULL COMMENT '商品 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_seckill_order_user_goods` (`user_id`, `goods_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO `t_seckill_order`
    (`id`, `user_id`, `order_id`, `goods_id`)
VALUES
    (3, 13669255555, 12, 1),
    (4, 13669255555, 13, 2);
