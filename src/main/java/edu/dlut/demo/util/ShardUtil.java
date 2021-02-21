package edu.dlut.demo.util;

import redis.clients.jedis.*;
import redis.clients.jedis.util.Hashing;

import java.util.ArrayList;
import java.util.List;

public class ShardUtil {
    private ShardUtil() {
    }

    private static ShardedJedisPool pool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        String host = "127.0.0.1";
        List<JedisShardInfo> infos = new ArrayList<>();
        infos.add(new JedisShardInfo(host, 6379));
        infos.add(new JedisShardInfo(host, 6380));
        infos.add(new JedisShardInfo(host, 6381));
        pool = new ShardedJedisPool(config, infos);
    }

    public static ShardedJedis getJedis() {
        return pool.getResource();
    }
}
