package com.example.service;

import com.example.dto.TransactionInitPayLoad;
import com.example.dto.TxnCompletedPayLoad;
import com.example.dto.WalletBalanceDto;
import com.example.dto.WalletUpdatedPayload;
import com.example.entity.Wallet;
import com.example.repo.WalletRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class WalletService {

    private static Logger LOGGER = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;

    @Value("${txn.completed.topic}")
    private String txnCompletedTopic;

    @Value("${wallet.updated.topic}")
    private String walletUpdatedTopic;

    public WalletBalanceDto walletBalanceDto(Long userId){
        Wallet wallet = walletRepo.findByUserId(userId);
        WalletBalanceDto walletBalanceDto = new WalletBalanceDto();
        walletBalanceDto.setBalance(wallet.getBalance());
        return walletBalanceDto;
    }

    @Transactional
    public void walletTxn(TransactionInitPayLoad initPayLoad) throws ExecutionException, InterruptedException {
        Wallet fromWallet = walletRepo.findByUserId(initPayLoad.getFromUserId());
        TxnCompletedPayLoad txnCompletedPayLoad = new TxnCompletedPayLoad();
        txnCompletedPayLoad.setRequestId(initPayLoad.getRequestId());
        txnCompletedPayLoad.setId(initPayLoad.getId());

        if(fromWallet.getBalance() < initPayLoad.getAmount()){
            txnCompletedPayLoad.setSuccess(false);
            txnCompletedPayLoad.setReason("Insufficient Funds");
        }
        else {
            Wallet toWallet = walletRepo.findByUserId(initPayLoad.getToUserId());
            fromWallet.setBalance(fromWallet.getBalance() - initPayLoad.getAmount());
            toWallet.setBalance(toWallet.getBalance() + initPayLoad.getAmount());
            txnCompletedPayLoad.setSuccess(true);

            WalletUpdatedPayload walletUpdatedPayload = new WalletUpdatedPayload(
                    fromWallet.getUserEmail(),
                    fromWallet.getBalance(),
                    initPayLoad.getRequestId()
            );

            WalletUpdatedPayload walletUpdatedPayload1 = new WalletUpdatedPayload(
                    fromWallet.getUserEmail(),
                    fromWallet.getBalance(),
                    initPayLoad.getRequestId()
            );
        }
        Future<SendResult<String,Object>> future = kafkaTemplate.send(txnCompletedTopic,initPayLoad.getFromUserId().toString(),txnCompletedPayLoad);
        LOGGER.info("Pushed TxnCompleted to Kafka: {}", future.get());
    }
}
