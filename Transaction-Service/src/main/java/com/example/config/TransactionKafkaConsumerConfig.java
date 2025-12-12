package com.example.config;

import com.example.dto.TxnCompletedPayLoad;
import com.example.entity.Transaction;
import com.example.entity.TxnStatusEnum;
import com.example.repo.TxnRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class TransactionKafkaConsumerConfig {

    private static Logger LOGGER = LoggerFactory.getLogger(TransactionKafkaConsumerConfig.class);

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private TxnRepo txnRepo;

    @KafkaListener(topics = "${txn.completed.topic}", groupId = "txn")
    public void consumeTxnInitTopic(ConsumerRecord payload) throws JsonProcessingException {
        TxnCompletedPayLoad completedPayLoad = OBJECT_MAPPER.readValue(payload.value().toString(), TxnCompletedPayLoad.class);
        MDC.put("requestId", completedPayLoad.getRequestId());
        LOGGER.info("Read from Kafka: {}", completedPayLoad);
        Transaction transaction = txnRepo.findById(completedPayLoad.getId()).get();
        if (!completedPayLoad.getSuccess()){
            transaction.setStatus(TxnStatusEnum.FAILED);
            transaction.setReason(completedPayLoad.getReason());
        }
        else {
            transaction.setStatus(TxnStatusEnum.SUCCESS);
        }
        txnRepo.save(transaction);
        MDC.clear();
    }

}
