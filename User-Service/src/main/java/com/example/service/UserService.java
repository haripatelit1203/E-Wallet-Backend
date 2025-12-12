package com.example.service;

import com.example.UserApp;
import com.example.client.WalletServiceClient;
import com.example.dto.UserCreatedPayload;
import com.example.dto.UserDto;
import com.example.dto.UserProfileDto;
import com.example.dto.WalletBalanceDto;
import com.example.entity.User;
import com.example.repo.UserRepo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class UserService {

    private Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    @Value("${user.created.topic}")
    private String userCreatedTopic;

    @Autowired
    private WalletServiceClient walletServiceClient;

    @Autowired
    private RedisTemplate<String, UserDto> redisTemplate;

    @Transactional
    public Long createUser(UserDto userDto) throws ExecutionException, InterruptedException {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setKycNumber(userDto.getKycNumber());
        user = userRepo.save(user);

        UserCreatedPayload userCreatedPayload = new UserCreatedPayload();
        userCreatedPayload.setUserEmail(user.getEmail());
        userCreatedPayload.setUserId(user.getId());
        userCreatedPayload.setUserName(user.getName());
        userCreatedPayload.setRequestId(MDC.get("requestId"));
        Future<SendResult<String,Object>> future = kafkaTemplate.send(userCreatedTopic,userCreatedPayload.getUserEmail(),userCreatedPayload);
        LOGGER.info("Pushed userCreatedPayload to Kafka: {}",future.get());
        String key = "user:"+user.getId();
        LOGGER.info("Putting User Details in Redis");
        redisTemplate.opsForValue().set(key,userDto);
        return user.getId();
    }

    public UserProfileDto userProfileDto(Long id){
        UserProfileDto userProfileDto = new UserProfileDto();
        String key = "user:"+id;
        LOGGER.info("Getting User Details in Redis");
        UserDto userDto = (UserDto) redisTemplate.opsForValue().get(key);
        if( userDto != null){
            userProfileDto.setUserDetail(userDto);
        }
        else {
            User user = userRepo.findById(id).get();
            userDto = new UserDto();
            userDto.setEmail(user.getEmail());
            userDto.setName(user.getName());
            userDto.setKycNumber(user.getKycNumber());
            userDto.setPhone(user.getPhone());
            LOGGER.info("Putting User Details in Redis");
            redisTemplate.opsForValue().set(key,userDto);
            userProfileDto.setUserDetail(userDto);

        }

        // Call Wallet Service API
        WalletBalanceDto walletBalanceDto = walletServiceClient.getBalance(id).getBody();
        userProfileDto.setWalletBalance(walletBalanceDto.getBalance());
        return userProfileDto;
    }
}
