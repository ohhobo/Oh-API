-- KEYS[1]: 库存Key（seckill:stock:{activityId}）
-- KEYS[2]: 用户集合Key（seckill:users:{activityId}）
-- ARGV[1]: 用户ID
-- ARGV[2]: 每份数量
-- 检查活动状态
if redis.call('EXISTS', KEYS[1]) == 0 then
    return 0 -- 活动不存在
end

-- 检查库存
local stock = tonumber(redis.call('GET', KEYS[1]))
if stock <= 0 then
    return 1 -- 库存不足
end

-- 检查用户是否已参与
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return 2 -- 重复参与
end

-- 扣减库存并记录用户
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])

-- 返回成功标识和获得的调用次数
return {3, ARGV[2]}