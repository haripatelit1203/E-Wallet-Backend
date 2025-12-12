package com.example.controller;

import com.example.dto.WalletBalanceDto;
import com.example.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet-service")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/balance/{id}")
    public ResponseEntity<WalletBalanceDto> getBalance(@PathVariable Long id){
        WalletBalanceDto walletBalanceDto = walletService.walletBalanceDto(id);
        return ResponseEntity.ok(walletBalanceDto);
    }
}
