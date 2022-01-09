package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * The {@code RedisConfiguration} class provides configuration for Redis database
 *
 * @author tahbaz
 */

@Configuration
public class RedisConfiguration {
    @Value("${spring.redis.host}")
    private String redis_host;
    @Value("${spring.redis.port}")
    private int redis_port;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        return new LettuceConnectionFactory(redis_host, redis_port);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        return template;
    }

}
