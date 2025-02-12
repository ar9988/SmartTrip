package com.travel.together.TravelTogether;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;

public class RedisStandaloneTest {
    public static void main(String[] args) {
        // Redis 연결 정보 설정
        String redisHost = "43.202.51.112"; // EC2의 Redis 퍼블릭 IP
        int redisPort = 6379;

        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            jedis.set("testKey", "testValue");
            String value = jedis.get("testKey");
            System.out.println("✅ Redis 연결 성공: " + value);
        } catch (Exception e) {
            System.err.println("❌ Redis 연결 실패: " + e.getMessage());
        }
    }
}
