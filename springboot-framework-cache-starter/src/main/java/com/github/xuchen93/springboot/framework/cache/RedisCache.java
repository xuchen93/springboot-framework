package com.github.xuchen93.springboot.framework.cache;

import jakarta.validation.constraints.NotBlank;
import org.redisson.api.RLock;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface RedisCache {
    /**
     * 设置过期时间
     *
     * @param key
     * @param timeout
     * @param unit
     * @return
     */
    boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 添加缓存
     *
     * @param key
     * @param t
     * @param expireTime
     * @param timeUnit
     * @param <T>
     */
    <T> void put(String key, T t, long expireTime, TimeUnit timeUnit);

    /**
     * 获取过期时间
     *
     * @param key
     * @return
     */
    long ttl(String key, TimeUnit unit);

    /**
     * 批量删除缓存
     *
     * @param keys
     * @return
     */
    long delete(String... keys);

    /**
     * 根据表达式批量删除缓存
     *
     * @param pattern
     * @return
     */
    long deleteByPattern(String pattern);

    /**
     * 根据表达式筛选出满足条件的keys
     *
     * @param pattern
     * @return
     */
    Set<String> scan(String pattern);

    //=====================================================String 类型操作==================================//

    /**
     * GET 命令
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T get(String key);

    /**
     * GETSET 命令
     *
     * @param key
     * @param newValue
     * @param <T>
     * @return
     */
    <T> T getSet(String key, Object newValue);

    /**
     * SETNX 命令,如果key不存在则设置并返回true，如果存在则返回false
     *
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit
     * @return
     */
    boolean setNx(String key, Object value, long expireTime, TimeUnit timeUnit);

    /**
     * INCR 命令，自增1
     *
     * @param key
     * @return
     */
    Long incr(String key);

    /**
     * INCR 命令，自增指定值
     *
     * @param key
     * @param increment
     * @param <T>
     * @return
     */
    <T extends Number> Double incr(String key, T increment);

    /**
     * DECR 命令，自减1
     *
     * @param key
     * @return
     */
    Long decr(String key);

    /**
     * DECR 命令，自减指定值
     *
     * @param key
     * @param increment
     * @param <T>
     * @return
     */
    <T extends Number> Long decr(String key, T increment);

    //======================================================= 操作Map类型======================================//

    /**
     * HSET 命令,添加一对键值对，如果没有则新增
     *
     * @param key   Map对应的Key
     * @param field Map中的某个键
     * @param value 值
     */
    void hSet(String key, String field, Object value);

    /**
     * HSET 命令，往某个Map中添加新的键值对
     *
     * @param key Map对应的key
     * @param map 需要添加的键值对
     */
    void hSet(String key, Map<String, ?> map);

    /**
     * HGET 命令,获取某个Map中的某个值
     *
     * @param key   Map对应的key
     * @param field Map中的某个键
     */
    <T> T hGet(String key, String field);

    /**
     * HMGET 命令，获取某个Map中的多个值
     *
     * @param key
     * @param fields
     * @param <T>
     * @return
     */
    <T> List<T> hMGet(String key, String... fields);

    /**
     * HGETALL 命令，获取某个Map中的所有键值对
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> Map<String, T> hGetAll(String key);

    /**
     * HDEL 命令，删除某个Map中的某个键值对
     *
     * @param key
     * @param field
     * @return
     */
    long hDel(String key, String field);

    /**
     * HINCRBY 命令，某个Map中的某个键的值自增1
     *
     * @param key
     * @param field
     * @return
     */
    long hIncr(String key, String field);

    /**
     * HINCRBY 命令，某个Map中的某个键的值自增指定值
     *
     * @param key
     * @param field
     * @param increment
     * @return
     */
    long hIncr(String key, String field, long increment);

    /**
     * HEXISTS 命令，判断某个Map中是否存在某个键
     *
     * @param key
     * @param field
     * @return
     */
    boolean hExists(String key, String field);

    /**
     * HSETNX 命令，如果某个Map中某个键不存在，则设置该键值对且返回true，如果已存在，则返回false
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    boolean hSetNx(String key, String field, Object value);

    /**
     * HLEN 命令，获取某个Map的长度
     *
     * @param key
     * @return
     */
    long hLen(String key);

    /**
     * HKEYS 命令，获取某个Map的所有键
     *
     * @param key
     * @return
     */
    Set<String> hKeys(String key);

    /**
     * HVALS 命令，获取某个Map的所有值
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> List<T> hVals(String key);

    //===============================================ZSet操作========================================//

    /**
     * ZADD 命令，添加一个元素到有序集合中,如果集合中已存在该元素，则覆盖
     *
     * @param key   集合对应的key
     * @param value 值
     * @param score 分值
     * @param <T>
     * @return
     */
    <T> boolean zAdd(String key, T value, double score);

    /**
     * ZADD 命令，批量添加元素到有序集合中，如果集合中已存在该元素，则覆盖
     *
     * @param key
     * @param tuples
     * @return
     */
    long zAdd(String key, Set<ZSetOperations.TypedTuple<Object>> tuples);

    /**
     * ZREMRANGEBYRANK 命令，删除有序集合中指定范围的元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    long zRem(String key, long start, long end);

    /**
     * ZREMRANGEBYVALUE 命令，删除有序集合中指定值的元素
     *
     * @param key
     * @param values
     * @return
     */
    long zRem(String key, Object... values);

    /**
     * ZCARD 命令，获取有序集合的长度
     *
     * @param key
     * @return
     */
    long zSize(String key);

    /**
     * ZADDNX 命令，添加一个元素到有序集合中，如果不存在则添加并返回true，如果集合中已存在该元素，则返回false
     *
     * @param key
     * @param value
     * @param score
     * @param <T>
     * @return
     */
    <T> boolean zAddNx(String key, T value, double score);

    /**
     * ZEXISTS 命令，判断是否存在某个元素
     *
     * @param key
     * @param value
     * @return
     */
    <T> boolean zExists(String key, T value);

    /**
     * ZINCRBY 命令，给有序集合中某个元素的分值增加指定值
     *
     * @param key
     * @param value
     * @param score
     * @param <T>
     * @return
     */
    <T> double zIncr(String key, T value, double score);

    /**
     * ZRANGE 命令，获取有序集合中所有元素
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> Set<T> zAll(String key);

    /**
     * ZRANGE 命令，获取有序集合中指定范围的元素
     *
     * @param key
     * @param start
     * @param end
     * @param <T>
     * @return
     */
    <T> Set<T> zRange(String key, long start, long end);

    /**
     * ZRANGEBYSCORE 命令，获取有序集合中指定分数范围内的元素
     * @param key
     * @param start
     * @param end
     * @param offset
     * @param count
     * @return
     * @param <T>
     */
    <T> Set<T> zRangeByScore(@NotBlank String key, double start, double end, long offset, long count);

    /**
     * ZRANGE 命令，获取某个index对应的元素
     *
     * @param key
     * @param index
     * @param <T>
     * @return
     */
    <T> T zGet(String key, long index);

    /**
     * ZRANK 命令，获取某个元素的索引
     *
     * @param key
     * @param value
     * @return
     */
    Long zRank(String key, Object value);

    /**
     * ZSCORE 命令，获取某个元素的分值
     *
     * @param key
     * @param value
     * @return
     */
    double zScore(String key, Object value);

    /**
     * ZREVRANK 命令，获取某个元素的索引（可以用作排序获取排名）
     *
     * @param key
     * @param value
     * @return
     */
    long zRevRank(String key, Object value);

    /**
     * ZRANGEBYSCORE 命令，获取有序集合中指定分值区间的元素
     *
     * @param key
     * @param min
     * @param max
     * @param <T>
     * @return
     */
    <T> Set<T> zRangeByScore(String key, double min, double max);

    /**
     * ZREMRANGEBYSCORE 命令，删除有序集合中指定分值区间的元素
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    long zRemRangeByScore(String key, double min, double max);

    /**
     * ZCOUNT 命令，获取有序集合中指定分值区间的元素个数
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    long zCount(String key, double min, double max);

    /**
     * ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
     * 获取有序集合中指定分值区间的元素，倒序返回
     * 时间复杂度： O(log(N)+M)， N 为有序集合元素数量， M 为返回元素数量
     *
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * @param <T>
     * @return
     */
    <T> Set<ZSetOperations.TypedTuple<T>> zRevRangeByScoreWithScores(String key, double min, double max, long offset, long count);

    //=========================================================Set操作========================================================//

    /**
     * SADD命令，新增一个元素到集合中
     *
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    <T> long sAdd(String key, T value);

    /**
     * SADD命令，新增多个元素到集合中
     *
     * @param key
     * @param values
     * @param <T>
     */
    <T> long sAdd(String key, T... values);

    /**
     * SMEMBERS命令，获取集合中所有元素
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> Set<T> sMembers(String key);

    /**
     * SREM命令 , 删除集合中的某些元素
     *
     * @param key
     * @param values
     * @param <T>
     * @return
     */
    <T> long sRemove(String key, T... values);

    /**
     * size命令，获取集合的长度
     *
     * @param key
     * @return
     */
    long sSize(String key);

    /**
     * SISMEMBER命令，判断集合中是否存在某个元素
     *
     * @param key
     * @param value
     * @return
     */
    boolean sExists(String key, Object value);

    /**
     * SPOP命令，随机删除并返回集合中的一个元素
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T sPop(String key);

    /**
     * SRANDOMMEMBER命令，随机取出集合中的一个或多个元素
     *
     * @param key
     * @param count
     * @param <T>
     * @return
     */
    <T> Set<T> sRandomMem(String key, int count);

    /**
     * SDIFF命令，获取第一个Set 和其他（一个或者多个）集合的差集
     *
     * @param key
     * @param keys
     * @param <T>
     * @return
     */
    <T> Set<T> sDiff(String key, String... keys);

    /**
     * SUnion命令，获取第一个集合和其他（一个或者多个）集合的并集
     *
     * @param key
     * @param keys
     * @param <T>
     * @return
     */
    <T> Set<T> sUnion(String key, String... keys);

    /**
     * SInter命令，获取第一个集合和其他（一个或者多个）集合的交集
     *
     * @param key
     * @param keys
     * @param <T>
     * @return
     */
    <T> Set<T> sInter(String key, String... keys);

    //==========================================================List操作========================================================//

    /**
     * LINDEX命令，获取list中某个索引位置的元素
     *
     * @param key
     * @param index
     * @param <T>
     * @return
     */
    <T> T lIndex(String key, long index);


    /**
     * LPUSH命令，将一个或多个值插入到list头部
     *
     * @param key
     * @param values
     * @param <T>
     * @return
     */
    <T> long lPush(String key, T... values);


    /**
     * LPUSH命令，将一个或多个值插入到list头部
     *
     * @param key
     * @param values
     * @param <T>
     * @return
     */
    <T> long lPush(String key, Collection<T> values);

    /**
     * RPUSH命令，将一个或多个值插入到list尾部
     *
     * @param key
     * @param values
     * @param <T>
     * @return
     */
    <T> long rPush(String key, T... values);

    /**
     * RPUSH命令，将一个或多个值插入到list尾部
     *
     * @param key
     * @param values
     * @param <T>
     * @return
     */
    <T> long rPush(String key, Collection<T> values);

    /**
     * LPOP命令，移除并获取list的第一个元素
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T lPop(String key);

    /**
     * LPOP命令，移除并获取list的前面N个元素
     * 若剩余数量M<N，则返回剩余M个元素
     * @param key
     * @param count
     * @return
     * @param <T>
     */
    <T> List<T> lPop(@NotBlank String key, long count);

    /**
     * RPOP命令，移除并获取list的最后一个元素
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> T rPop(String key);

    /**
     * RPOP命令，移除并获取list的最后N个元素
     * 若剩余数量M<N，则返回剩余M个元素
     * @param key
     * @param count
     * @return
     * @param <T>
     */
    <T> List<T> rPop(@NotBlank String key, long count);

    /**
     * TRIM 命令，删除list中某个值的元素（不返回）
     *
     * @param key
     * @param value
     */
    long lRemove(String key, Object value);

    /**
     * TRIM 命令，从右侧删除指定数量的元素（不返回）
     *
     * @param key
     * @param count
     */
    void rRemove(String key, long count);

    /**
     * LSIZE命令，获取list的长度
     *
     * @param key
     * @return
     */
    long lSize(String key);

    /**
     * LALL(LRANGE key,0,-1)命令，获取list的所有元素
     *
     * @param key
     * @param <T>
     * @return
     */
    <T> List<T> lAll(String key);

    /**
     * LTRIM命令，截取list的部分元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    void lTrim(String key, long start, long end);

    /**
     * SET命令，设置list中某个索引位置的值
     *
     * @param key
     * @param index
     * @param value
     */
    void lSet(String key, long index, Object value);

    //==========================================================LOCK操作========================================================//

    /**
     * 使用Redisson 实现分布式锁
     *
     * @param key
     * @return
     */
    RLock getLock(String key);


    //==========================================================Piplined操作========================================================//
    <T> List<T> pipelined(Consumer<RedisOperations<String, T>> consumer);

    //==========================================================HyperLogLog操作========================================================//
    //HyperLogLog 是基于误差计数的，不过这里的误差比较低，误差率在0.81% 左右。所以使用用来做去重的非精确统计，比如统计pv和uv的数据

    /**
     * PFADD命令，添加一个或者多个元素到HyperLogLog中
     *
     * @param key
     * @param values
     */
    void pfAdd(String key, Object... values);

    /**
     * PFCOUNT命令，获取HyperLogLog中元素的个数
     *
     * @param key
     * @return
     */
    long pfCount(String key);

    /**
     * PFMERGE命令，合并多个HyperLogLog
     *
     * @param destkey
     * @param sourcekey
     * @return
     */
    long pfMerge(String destkey, String sourcekey);

    //==========================================================BitMap操作========================================================//

    /**
     * SETBIT命令，设置bitmap中某个位置的值,设置为1或者0
     *
     * @param key
     * @param offset
     * @param value
     * @return
     */
    boolean setBit(String key, long offset, boolean value);

    /**
     * GETBIT命令，获取bitmap中某个位置的值
     *
     * @param key
     * @param offset
     * @return
     */
    boolean getBit(String key, long offset);

    /**
     * BITCOUNT命令，统计bitmap中从start到end区间内数据为1的个数
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    long bitCount(String key, long start, long end);

    /**
     * BITFIELD命令，一次调用执行多个bitmap操作
     *
     * @param key
     * @param bitFieldSubCommands
     * @return
     */
    List<Long> bitField(String key, BitFieldSubCommands bitFieldSubCommands);
}
