package com.example.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TxnCompletedPayLoad {
    private Long id;
    private Boolean success;
    private String reason;
    private String requestId;
}
