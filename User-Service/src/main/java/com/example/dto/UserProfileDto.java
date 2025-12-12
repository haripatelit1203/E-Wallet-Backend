package com.example.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class UserProfileDto implements Serializable {

    private static final long serialVersionUID = 1l;

    private UserDto userDetail;
    private Double walletBalance;
}
