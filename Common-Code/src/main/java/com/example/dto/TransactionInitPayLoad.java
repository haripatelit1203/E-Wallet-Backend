package com.example.dto;

import lombok.*;

@Setter
@Getter
@ToString
public class TransactionInitPayLoad {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Double amount;
    private String requestId;
}
