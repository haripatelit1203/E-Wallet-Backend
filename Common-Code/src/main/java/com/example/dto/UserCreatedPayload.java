package com.example.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class UserCreatedPayload implements Serializable {

    private static final long serialVersionUID = 1l;
    private Long userId;
    private String userName;
    private String userEmail;
    private String requestId;
}
