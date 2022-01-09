package com.db.awmd.challenge.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferObject {
    private String accountFromId;
    private String accountToId;
    private BigDecimal amount;
}
