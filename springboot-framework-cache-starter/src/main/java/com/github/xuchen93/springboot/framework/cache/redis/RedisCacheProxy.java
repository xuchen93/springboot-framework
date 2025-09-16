package com.github.xuchen93.springboot.framework.cache.redis;

import com.github.xuchen93.springboot.framework.cache.RedisCache;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public final class RedisCacheProxy implements RedisCache {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedissonClient redissonClient;

	//=======================================================通用RedisCache操作=======================================================
	@Override
	public boolean expire(@NotBlank String key, long timeout, TimeUnit unit) {
		return Optional.of(timeout)
				.filter(tim -> tim > 0)
				.map(t -> redisTemplate.expire(key, t, unit))
				.orElse(false);
	}

	@Override
	public long ttl(@NotBlank String key, TimeUnit unit) {
		return redisTemplate.getExpire(key, unit);
	}

	@Override
	public long delete(String... keys) {
		return Optional.ofNullable(keys)
				.map(ks -> redisTemplate.delete(Arrays.asList(keys)))
				.orElse(0L);
	}

	@Override
	public long deleteByPattern(@NotBlank String pattern) {
		return redisTemplate.delete(scan(pattern));
	}

	@SneakyThrows
	@Override
	public Set<String> scan(@NotBlank String pattern) {
		return redisTemplate.execute((RedisCallback<? extends Set<String>>) conn -> {
			Set<String> keys = new HashSet<>();
			Cursor<byte[]> cursor = conn.scan(ScanOptions.scanOptions().match(pattern).count(1000).build());
			while (cursor.hasNext()) {
				keys.add(new String(cursor.next()));
			}
			return keys;
		});
	}

	@Override
	public <T> void put(String key, T t, long expireTime, TimeUnit timeUnit) {
		redisTemplate.opsForValue().set(key, t, expireTime, timeUnit);
	}

	@Override
	public <T> T get(@NotBlank String key) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(key))
				.map(v -> (T) v)
				.orElse(null);
	}

	@Override
	public <T> T getSet(@NotBlank String key, Object newValue) {

		return Optional.ofNullable(redisTemplate.opsForValue().getAndSet(key, newValue))
				.map(v -> (T) v)
				.orElse(null);
	}

	@Override
	public boolean setNx(@NotBlank String key, Object value, long expireTime, TimeUnit timeUnit) {
		return Optional.ofNullable(key)
				.map(k -> redisTemplate.opsForValue().setIfAbsent(k, value, expireTime, timeUnit))
				.orElse(false);
	}

	@Override
	public Long incr(@NotBlank String key) {
		return redisTemplate.opsForValue().increment(key, 1l);
	}

	@Override
	public <T extends Number> Double incr(@NotBlank String key, T increment) {
		return redisTemplate.opsForValue().increment(key, increment.doubleValue());
	}

	@Override
	public Long decr(@NotBlank String key) {
		return redisTemplate.opsForValue().decrement(key);
	}

	@Override
	public <T extends Number> Long decr(@NotBlank String key, T increment) {
		return redisTemplate.opsForValue().decrement(key, increment.longValue());
	}

	@Override
	public void hSet(@NotBlank String key, String field, Object value) {
		redisTemplate.opsForHash().put(key, field, value);
	}

	@Override
	public void hSet(@NotBlank String key, Map<String, ?> map) {
		redisTemplate.opsForHash().putAll(key, map);
	}

	@Override
	public <T> T hGet(@NotBlank String key, String field) {
		return Optional.ofNullable(field)
				.map(f -> (T) redisTemplate.opsForHash().get(key, f))
				.orElse(null);
	}

	@Override
	public <T> List<T> hMGet(@NotBlank String key, String... fields) {
		return Optional.ofNullable(fields)
				.map(f -> (List<T>) redisTemplate.opsForHash().multiGet(key, Arrays.asList(fields)))
				.orElse(null);
	}

	@Override
	public <T> Map<String, T> hGetAll(@NotBlank String key) {
		return redisTemplate.<String, T>opsForHash().entries(key);
	}

	@Override
	public long hDel(@NotBlank String key, String field) {
		return redisTemplate.opsForHash().delete(key, field);
	}

	@Override
	public long hIncr(@NotBlank String key, String field) {
		return hIncr(key, field, 1l);
	}

	@Override
	public long hIncr(@NotBlank String key, String field, long increment) {
		return redisTemplate.opsForHash().increment(key, field, increment);
	}

	@Override
	public boolean hExists(@NotBlank String key, String field) {
		return redisTemplate.opsForHash().hasKey(key, field);
	}

	@Override
	public boolean hSetNx(@NotBlank String key, String field, Object value) {
		return redisTemplate.opsForHash().putIfAbsent(key, field, value);
	}

	@Override
	public long hLen(@NotBlank String key) {
		return redisTemplate.opsForHash().size(key);
	}

	@Override
	public Set<String> hKeys(@NotBlank String key) {
		return redisTemplate.<String, Object>opsForHash().keys(key);
	}

	@Override
	public <T> List<T> hVals(@NotBlank String key) {
		return Optional.ofNullable(redisTemplate.opsForHash().values(key))
				.map(v -> (List<T>) v)
				.orElse(null);
	}

	@Override
	public <T> boolean zAdd(@NotBlank String key, T value, double score) {
		return redisTemplate.opsForZSet().add(key, value, score);
	}

	@Override
	public long zAdd(@NotBlank String key, Set<ZSetOperations.TypedTuple<Object>> tuples) {
		return redisTemplate.opsForZSet().add(key, tuples);
	}

	@Override
	public long zRem(@NotBlank String key, long start, long end) {
		return redisTemplate.opsForZSet().removeRange(key, start, end);
	}

	@Override
	public long zRem(@NotBlank String key, Object... values) {
		return redisTemplate.opsForZSet().remove(key, values);
	}

	@Override
	public long zSize(@NotBlank String key) {
		return redisTemplate.opsForZSet().size(key);
	}

	@Override
	public <T> boolean zAddNx(@NotBlank String key, T value, double score) {
		return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
	}

	@Override
	public <T> boolean zExists(@NotBlank String key, T value) {
		return redisTemplate.opsForZSet().rank(key, value) != null;
	}

	@Override
	public <T> double zIncr(@NotBlank String key, T value, double score) {
		return redisTemplate.opsForZSet().incrementScore(key, value, score);
	}

	@Override
	public <T> Set<T> zAll(@NotBlank String key) {
		return Optional.ofNullable(redisTemplate.opsForZSet().range(key, 0, -1))
				.map(v -> (Set<T>) v).orElse(null);
	}

	@Override
	public <T> Set<T> zRange(@NotBlank String key, long start, long end) {
		return Optional.ofNullable(redisTemplate.opsForZSet().range(key, start, end))
				.map(v -> (Set<T>) v).orElse(null);
	}

	@Override
	public <T> Set<T> zRangeByScore(@NotBlank String key, double start, double end, long offset, long count) {
		return Optional.ofNullable(redisTemplate.opsForZSet().rangeByScore(key, start, end, offset, count))
				.map(v -> (Set<T>) v).orElse(null);
	}

	@Override
	public <T> T zGet(@NotBlank String key, long index) {
		return Optional.ofNullable(redisTemplate.opsForZSet().range(key, index, index + 1))
				.map(v -> (T) v.iterator().next()).orElse(null);
	}

	@Override
	public Long zRank(@NotBlank String key, Object value) {
		return redisTemplate.opsForZSet().rank(key, value);
	}

	@Override
	public double zScore(@NotBlank String key, Object value) {
		return redisTemplate.opsForZSet().score(key, value);
	}

	@Override
	public long zRevRank(@NotBlank String key, Object value) {
		return redisTemplate.opsForZSet().reverseRank(key, value);
	}

	@Override
	public <T> Set<T> zRangeByScore(@NotBlank String key, double min, double max) {
		return Optional.ofNullable(redisTemplate.opsForZSet().rangeByScore(key, min, max))
				.map(v -> (Set<T>) v)
				.orElse(null);
	}

	@Override
	public long zRemRangeByScore(@NotBlank String key, double min, double max) {
		return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
	}

	@Override
	public long zCount(@NotBlank String key, double min, double max) {
		return redisTemplate.opsForZSet().count(key, min, max);
	}

	@Override
	public <T> Set<ZSetOperations.TypedTuple<T>> zRevRangeByScoreWithScores(@NotBlank String key, double min, double max, long offset, long count) {
		return Optional.ofNullable(((RedisTemplate<String, T>) redisTemplate).opsForZSet().reverseRangeByScoreWithScores(key, min, max, offset, count))
				.orElse(null);
	}

	@Override
	public <T> long sAdd(@NotBlank String key, T value) {
		return redisTemplate.opsForSet().add(key, value);
	}

	@Override
	public <T> long sAdd(@NotBlank String key, T... values) {
		return redisTemplate.opsForSet().add(key, values);
	}

	@Override
	public <T> Set<T> sMembers(@NotBlank String key) {
		return Optional.ofNullable(redisTemplate.opsForSet().members(key))
				.map(v -> (Set<T>) v)
				.orElse(null);
	}

	@Override
	public <T> long sRemove(@NotBlank String key, T... values) {
		return redisTemplate.opsForSet().remove(key, values);
	}

	@Override
	public long sSize(@NotBlank String key) {
		return redisTemplate.opsForSet().size(key);
	}

	@Override
	public boolean sExists(@NotBlank String key, Object value) {
		return redisTemplate.opsForSet().isMember(key, value);
	}

	@Override
	public <T> T sPop(@NotBlank String key) {
		return Optional.ofNullable(redisTemplate.opsForSet().pop(key))
				.map(v -> (T) v)
				.orElse(null);
	}

	@Override
	public <T> Set<T> sRandomMem(@NotBlank String key, int count) {
		return Optional.ofNullable(redisTemplate.opsForSet().randomMembers(key, count))
				.map(v -> Set.<T>copyOf((Collection<? extends T>) v))
				.orElse(null);
	}

	@Override
	public <T> Set<T> sDiff(@NotBlank String key, String... keys) {
		return Optional.ofNullable(redisTemplate.opsForSet().difference(key, Arrays.asList(keys)))
				.map(v -> (Set<T>) v)
				.orElse(null);
	}

	@Override
	public <T> Set<T> sUnion(@NotBlank String key, String... keys) {
		return Optional.ofNullable(redisTemplate.opsForSet().union(key, Arrays.asList(keys)))
				.map(v -> (Set<T>) v)
				.orElse(null);
	}

	@Override
	public <T> Set<T> sInter(@NotBlank String key, String... keys) {
		return Optional.ofNullable(redisTemplate.opsForSet().intersect(key, Arrays.asList(keys)))
				.map(v -> (Set<T>) v)
				.orElse(null);
	}

	@Override
	public <T> T lIndex(@NotBlank String key, long index) {
		return Optional.ofNullable(redisTemplate.opsForList().index(key, index))
				.map(v -> (T) v)
				.orElse(null);
	}

	@Override
	public <T> long lPush(@NotBlank String key, T... values) {
		return redisTemplate.opsForList().leftPushAll(key, values);
	}

	@Override
	public <T> long lPush(@NotBlank String key, Collection<T> values) {
		return redisTemplate.opsForList().leftPushAll(key, (Collection<Object>) values);
	}

	@Override
	public <T> long rPush(@NotBlank String key, T... values) {
		return redisTemplate.opsForList().rightPushAll(key, values);
	}

	@Override
	public <T> long rPush(@NotBlank String key, Collection<T> values) {
		return redisTemplate.opsForList().rightPushAll(key, (Collection<Object>) values);
	}

	@Override
	public <T> T lPop(@NotBlank String key) {
		return Optional.ofNullable(redisTemplate.opsForList().leftPop(key)).map(v -> (T) v)
				.orElse(null);
	}

	@Override
	public <T> List<T> lPop(@NotBlank String key, long count) {
		return (List<T>) redisTemplate.opsForList().leftPop(key, count);

	}

	@Override
	public <T> T rPop(@NotBlank String key) {
		return Optional.ofNullable(redisTemplate.opsForList().rightPop(key)).map(v -> (T) v)
				.orElse(null);
	}

	@Override
	public <T> List<T> rPop(@NotBlank String key, long count) {
		return (List<T>) redisTemplate.opsForList().rightPop(key, count);
	}

	@Override
	public long lRemove(@NotBlank String key, Object value) {
		return redisTemplate.opsForList().remove(key, 0, value);
	}

	@Override
	public void rRemove(@NotBlank String key, long count) {
		redisTemplate.opsForList().trim(key, 0, lSize(key) - count - 1);
	}

	@Override
	public long lSize(@NotBlank String key) {
		return redisTemplate.opsForList().size(key);
	}

	@Override
	public <T> List<T> lAll(@NotBlank String key) {
		return Optional.ofNullable(redisTemplate.opsForList().range(key, 0, -1))
				.map(v -> (List<T>) v)
				.orElse(null);
	}

	@Override
	public void lTrim(@NotBlank String key, long start, long end) {
		redisTemplate.opsForList().trim(key, start, end);
	}

	@Override
	public void lSet(@NotBlank String key, long index, Object value) {
		redisTemplate.opsForList().set(key, index, value);
	}

	@Override
	public RLock getLock(String key) {
		return redissonClient.getLock(key);
	}

	@Override
	public <T> List<T> pipelined(Consumer<RedisOperations<String, T>> consumer) {
		return (List<T>) redisTemplate.executePipelined(new SessionCallback<T>() {
			@Override
			public T execute(RedisOperations operations) throws DataAccessException {
				consumer.accept(operations);
				return null;
			}
		});
	}

	@Override
	public void pfAdd(@NotBlank String key, Object... values) {
		redisTemplate.opsForHyperLogLog().add(key, values);
	}

	@Override
	public long pfCount(@NotBlank String key) {
		return redisTemplate.opsForHyperLogLog().size(key);
	}

	@Override
	public long pfMerge(@NotBlank String destkey, @NotBlank String sourcekey) {
		return redisTemplate.opsForHyperLogLog().union(destkey, sourcekey);
	}

	@Override
	public boolean setBit(@NotBlank String key, long offset, boolean value) {
		return redisTemplate.opsForValue().setBit(key, offset, value);
	}

	@Override
	public boolean getBit(@NotBlank String key, long offset) {
		return redisTemplate.opsForValue().getBit(key, offset);
	}

	@Override
	public long bitCount(@NotBlank String key, long start, long end) {
		return redisTemplate.execute((RedisCallback<Long>) con -> con.bitCount(key.getBytes(), start, end));
	}

	@Override
	public List<Long> bitField(@NotBlank String key, BitFieldSubCommands bitFieldSubCommands) {
		return redisTemplate.opsForValue().bitField(key, bitFieldSubCommands);
	}
}
