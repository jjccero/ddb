package edu.dlut.demo.util;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

public class RedisUtil {

    private RedisUtil() {
    }

    private static JedisPool jedisPool;
    public static SetParams EX_DAY;

    static {
        jedisPool = new JedisPool();
        EX_DAY = new SetParams();
        EX_DAY.ex(60 * 60 * 24);
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
