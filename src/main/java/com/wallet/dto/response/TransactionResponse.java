package com.wallet.dto.response;

import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse {

    private String referenceId;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private String remarks;
    private Instant createdAt;

    public TransactionResponse() {
    }

    public TransactionResponse(String referenceId, TransactionType type, BigDecimal amount,
                               TransactionStatus status, String remarks, Instant createdAt) {
        this.referenceId = referenceId;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getRemarks() {
        return remarks;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
