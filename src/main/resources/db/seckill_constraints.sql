ALTER TABLE t_seckill_goods
    ADD UNIQUE KEY uk_seckill_goods_goods_id (goods_id);

ALTER TABLE t_seckill_order
    ADD UNIQUE KEY uk_seckill_order_user_goods (user_id, goods_id);
