package com.example.ordersystem.common.configs;

import com.example.ordersystem.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

//    연결빈 객체 (레디스에 대한 연결정보 담겨져있음)
    @Bean
//    Qualifier : 같은 Bean객체가 여러개 있을경우 Bean 객체를 구분하기 위한 어노테이션
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host); //IP정보 , 유동적으로 바뀔수 있음 -> 야믈파일에 정의
        configuration.setPort(port); //포트번호, 유동적으로 바뀔수있음 -> 야믈파일에 정의
        configuration.setDatabase(0); //최근 트렌드는 0번 db만 사용 , 레디스 컨테이너를 10개 띄워놓는게 트렌드
        return new LettuceConnectionFactory(configuration);
    }
//    템플릿빈 객체 (자료구조- String, hash, set 등 설계)
    @Bean
    @Qualifier("rtInventory")
//    모든 template중에 무조건 redisTemplate이라는 메서드명이 반드시 1개는 있어야함.
//    bean객체 생성 시, bean 객체간에 DI(의존성 주입)은 "메서드 파라미터 주입"이 가능
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer()); //key를 String으로 만들어서 저장한다는 뜻
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory)                                           ;
        return redisTemplate;
    }


    @Bean
//    Qualifier : 같은 Bean객체가 여러개 있을경우 Bean 객체를 구분하기 위한 어노테이션
    @Qualifier("stockInventory")
    public RedisConnectionFactory stockConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host); //IP정보 , 유동적으로 바뀔수 있음 -> 야믈파일에 정의
        configuration.setPort(port); //포트번호, 유동적으로 바뀔수있음 -> 야믈파일에 정의
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);
    }
    //    템플릿빈 객체 (자료구조- String, hash, set 등 설계)
    @Bean
    @Qualifier("stockInventory")
    public RedisTemplate<String, String> stockRedisTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer()); //key를 String으로 만들어서 저장한다는 뜻
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory)                                           ;
        return redisTemplate;
    }


    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory ssePubSubConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
//        redis pub/sub기능은 db에 값을 저장하는 기능이 아니므로, 특정db에 의존적이지 않음
        return new LettuceConnectionFactory(configuration);
    }
    @Bean
    @Qualifier("ssePubSub") //템플릿빈 객체 -> 메시지 publish 할것
    public RedisTemplate<String, String> ssePubSubRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory)                                           ;
        return redisTemplate;
    }
//    pub/sub은 빈 객체가 두개 더 필요
//    redis 리스너(subscribe) 객체
//    호출구조 : RedisMessageListenerContainer -> MessageListenerAdapter->SseAlarmService(MessageListener)

    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,@Qualifier("ssePubSub") MessageListenerAdapter messageListenerAdapter){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter,new PatternTopic("order-channel")); //왼쪽에는 메시지 처리하는 객체 ,오른쪽에는 채널명
//        만약에 여러 채널을 구독해야 하는 경우, 여러개의 PatternTopic을 add하거나, 별도의 Listener Bean 객체 생성
        return container;
    }

//    redis에서 수신된 메시지를 처리하는 객체
//    sseAlarmService에서 onMessage메서드 추가
    @Bean
    @Qualifier("ssePubSub")
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService){
//        채널로부터 수신되는 메시지 처리를 SseAlarmService에 onMessage메서드로 위임
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }
}
