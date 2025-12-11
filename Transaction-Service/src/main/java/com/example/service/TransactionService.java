package com.example.service;

import com.example.dto.TransactionInitPayLoad;
import com.example.dto.TxnRequestDto;
import com.example.dto.TxnStatusDto;
import com.example.entity.Transaction;
import com.example.entity.TxnStatusEnum;
import com.example.repo.TxnRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class TransactionService {

    private static Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TxnRepo txnRepo;

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    @Value("${txn.init.topic}")
    private String txnInitTopic;

    @Transactional
    public String initTransaction(TxnRequestDto txnRequestDto) throws ExecutionException, InterruptedException {
        Transaction transaction = new Transaction();
        transaction.setFromUserId(txnRequestDto.getFromUserId());
        transaction.setToUserId(txnRequestDto.getToUserId());
        transaction.setAmount(txnRequestDto.getAmount());
        transaction.setComment(txnRequestDto.getComment());
        transaction.setTxnId(UUID.randomUUID().toString());
        transaction.setStatus(TxnStatusEnum.PENDING);
        transaction = txnRepo.save(transaction);

        TransactionInitPayLoad initPayLoad = new TransactionInitPayLoad();
        initPayLoad.setId(transaction.getId());
        initPayLoad.setFromUserId(transaction.getFromUserId());
        initPayLoad.setToUserId(transaction.getToUserId());
        initPayLoad.setAmount(transaction.getAmount());
        initPayLoad.setRequestId(MDC.get("requestId"));
        Future<SendResult<String,Object>> future = kafkaTemplate.send(txnInitTopic,transaction.getFromUserId().toString(),initPayLoad);
        LOGGER.info("Pushed TxnInitPayLoad to Kafka: {}", future.get());


        return transaction.getTxnId();
    }

    public TxnStatusDto getStatus(String txnId){
        Transaction transaction = txnRepo.findByTxnId(txnId);
        TxnStatusDto txnStatusDto = new TxnStatusDto();
        if(transaction != null){
            txnStatusDto.setReason(transaction.getReason());
            txnStatusDto.setStatus(transaction.getStatus().toString());
        }
        return txnStatusDto;
    }
}
