package com.example.dto;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WalletUpdatedPayload {
    private String userEmail;
    private Double balance;
    private String requestId;
}
