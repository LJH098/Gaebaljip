package com.gaebaljip.exceed.member.adapter.out.persistence;

import com.gaebaljip.exceed.common.RedisUtils;
import com.gaebaljip.exceed.member.application.port.out.TimeOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisAdapter implements TimeOutPort {

    private final RedisUtils redisUtils;

    @Value("${spring.redis.ttl}")
    private Long expiredTime;
    @Override
    public void command(String email, String code) {
        redisUtils.setData(email,code,expiredTime);
    }
    @Override
    public Optional<String> query(String email) {
        return Optional.of(redisUtils.getData(email));
    }
}