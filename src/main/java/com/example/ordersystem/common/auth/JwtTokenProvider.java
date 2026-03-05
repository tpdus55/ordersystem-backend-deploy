package com.example.ordersystem.common.auth;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String st_secret_key;
    @Value("${jwt.expiration}")
    private int expiration;

    private Key secret_key;


    @Value("${jwt.secretKeyRt}")
    private String st_secret_key_rt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    private Key secret_key_rt;

    private final RedisTemplate<String,String> redisTemplate;
    private final MemberRepository memberRepository;

    @Autowired
    public JwtTokenProvider(@Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate, MemberRepository memberRepository) {
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
    }

    @PostConstruct
    public void init(){
        secret_key = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key), SignatureAlgorithm.HS512.getJcaName());
        secret_key_rt = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key), SignatureAlgorithm.HS512.getJcaName());

    }
    public String createToken(Member member){

        Claims claims = Jwts.claims().setSubject(member.getEmail());
//        주된 키값을 제외한 나머지 정보는 put을 사용하여 key:value로 세팅
        claims.put("role",member.getRole().toString());
//        ex) claims.put("age",author.getAge()); 형태가능

        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration*60*1000L))
                .signWith(secret_key)
                .compact();
        return token;
    }

    public String createRtToken(Member member){

        Claims claims = Jwts.claims().setSubject(member.getEmail());
//        주된 키값을 제외한 나머지 정보는 put을 사용하여 key:value로 세팅
        claims.put("role",member.getRole().toString());
//        ex) claims.put("age",author.getAge()); 형태가능

        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L))
                .signWith(secret_key_rt)
                .compact();

//        rt토큰을 redis에 저장
//        opsForValue는 일반 string 자료구조
//        opsForSet(또는 Zset 또는 List 등) 존재
//        redisTemplate.opsForValue().set(member.getEmail(), token); //키값을 이메일로, value값을 token으로
        redisTemplate.opsForValue().set(member.getEmail(), token, expirationRt, TimeUnit.MINUTES); //3000분 ttl
        return token;
    }


    public Member validateRt(String refreshToken){
        Claims claims = null;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(st_secret_key_rt)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
        }catch (Exception e){
            throw new IllegalArgumentException("잘못된 토큰입니다."); //400에러를 던져주기 위함
        }
//        rt토큰 그 자체를 검증
        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new EntityNotFoundException("entity is not found"));

//        redis rt와 비교 검증
        String redisRt = redisTemplate.opsForValue().get(email);
        if(!redisRt.equals(refreshToken)){
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        }
        return member;
    }



}
