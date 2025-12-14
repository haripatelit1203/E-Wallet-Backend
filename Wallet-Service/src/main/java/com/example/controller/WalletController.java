package com.example.controller;

import com.example.dto.AddMoneyRequest;
import com.example.dto.AddMoneyResponse;
import com.example.dto.WalletBalanceDto;
import com.example.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/wallet-service")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/balance/{id}")
    public ResponseEntity<WalletBalanceDto> getBalance(@PathVariable Long id){
        WalletBalanceDto walletBalanceDto = walletService.walletBalanceDto(id);
        return ResponseEntity.ok(walletBalanceDto);
    }

    @PostMapping("/add-money")
    public ResponseEntity<AddMoneyResponse> addMoney(@RequestBody AddMoneyRequest addMoneyRequest){
        addMoneyRequest.setMerchantId(1l);
        AddMoneyResponse addMoneyResponse = restTemplate.postForObject("http://localhost:9090/pg-service/init-payment", addMoneyRequest, AddMoneyResponse.class);
        return ResponseEntity.ok(addMoneyResponse);
    }

    @GetMapping("/add-money-status/{pgnTxnId}")
    public ResponseEntity<String> addMoneyStatus(@PathVariable String pgnTxnId){
        return ResponseEntity.ok(walletService.processPgTxnId(pgnTxnId));
    }

}
