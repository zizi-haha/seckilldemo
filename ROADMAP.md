# 技术债与后续计划

> 记录已知问题与改进方向，按优先级排列。完成一项就移到文末的「已完成」。

## P0：正确性

### 1. 把「查重」和「扣库存」合并成一个 Lua 脚本

- **现状**：`SeckillGoodsController#doSeckill` 先查 `order:{userId}:{goodsId}`，再执行库存扣减的 Lua 脚本，两步不是原子的。
- **风险**：同一用户并发提交时，两个请求可能同时通过查重，各自扣减一次 Redis 库存。消费者处理时，其中一条会撞上 `t_seckill_order` 的唯一索引并回滚，但 Redis 的扣减不会回补，导致库存蒸发。
- **方案**：用一段 Lua 同时完成「查重 + 判库存 + 扣减 + 预占标记」。预占标记带 TTL，消费成功后改写为真实订单号，消费失败时主动清理。

### 2. 消费失败时回补 Redis 库存

- **现状**：消费者捕获 `GlobalException` / `DuplicateKeyException` 后只记日志，已经预扣的 Redis 库存不会退回。
- **方案**：在业务失败分支里对 `seckillGoods:{goodsId}` 执行 `INCR` 回补，并记录日志便于排查。

### 3. 增加库存对账任务

- **现状**：Redis 与 MySQL 的库存可能不一致，目前没有自动发现机制。
- **方案**：定时比对两边库存，以数据库为准修复 Redis；把差异数量作为监控指标暴露出来。

## P1：工程化

### 4. Redis key 统一收敛

- **现状**：`seckillGoods:`、`order:`、`user:`、`createPath:` 等 key 散落在 controller、service、consumer 多处手拼。
- **风险**：任何一处拼错都会造成状态不一致（已经发生过 `order:{goodsId}:{goodsId}` 的 bug）。
- **方案**：抽出 `RedisKeys` 常量类或 key 构建器，禁止业务代码直接拼接字符串。

### 5. 统一 Session 过期策略

- **现状**：cookie 每次访问都会续期，但 Redis 里的 `user:{token}` 是登录时固定 TTL，两者不一致。
- **方案**：在 `UserServiceImpl#getUserByToken` 中同步调用 `redisTemplate.expire(...)`，让两边都采用滑动过期。

### 6. 敏感配置外置

- **现状**：MySQL 密码、RabbitMQ 账号密码写死在 `application.yml` 中。
- **方案**：改为环境变量占位符（如 `${DB_PASSWORD}`），README 补充配置说明。

### 7. 统一鉴权入口

- **现状**：每个使用 `User` 参数的接口都要自己判空，漏一个就出现 500。
- **方案**：增加 `@LoginRequired` 注解配合拦截器，需要登录的接口统一鉴权。

### 8. 改密码后失效该用户的所有会话

- **现状**：修改密码只删除当前 `userTicket` 对应的 Redis key，其他设备上的会话仍然有效。
- **方案**：按用户维度维护 token 集合，改密码时批量失效；或引入 token 版本号。

## P2：整洁度

### 9. 清理死代码与命名

- `RabbiteMqConfig`、`RabbitmqDriect` 两个类的内容已全部注释，只剩空的 `@Configuration`。
- `TEXT` 空接口、`textController`、`hello.html` 属于早期演示残留。
- 拼写：`ArguementResovers`、`CapcheResourceConfig`、`seckillMessage`、`textController`。
- `pom.xml` 中 `spring-boot-starter-data-redis` 重复声明。

### 10. 压测脚本可复现

- **现状**：`loadtest/seckill.jmx` 中的 CSV 路径是绝对路径 `F:/Miaosha/Config/config.txt`，换台机器就失效。
- **方案**：改为 JMeter 相对路径参数，并在 README 中说明压测前置步骤（重置库存、生成 ticket、关闭验证码）。

## 已完成

- 统一秒杀凭证 `path` 的 Redis key，并增加 5 分钟过期。
- `doSeckill` 先校验登录再校验 path，修复空指针。
- 商品页缓存与用户态解耦，新增 `/user/current` 接口。
- 秒杀消息体从完整 `User` 缩小为 `userId` + `goodsId`，避免密码哈希进入队列和日志。
- 消费端异常分类处理，新增死信队列，关闭默认重投。
- 结果查询接口补充登录判空，前端轮询分支补登录跳转。
- 修改密码增加原密码校验。
- 库存扣减改用 Lua 脚本实现原子操作，移除 JVM 内存售罄标记。
- 修复消费者写入 `order:{goodsId}:{goodsId}` 的 key 拼接错误。
- 新增 `.gitignore`，排除 `target/` 与 `.idea/`。
