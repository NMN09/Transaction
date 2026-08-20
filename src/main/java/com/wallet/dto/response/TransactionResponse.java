package com.wallet.dto.response;

import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse {

    private String referenceId;
    private TransactionType type;
    private BigDecimal amount;
    private String direction; // "CREDIT" or "DEBIT"
    private String counterpartyEmail;
    private String counterpartyName;
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

    public TransactionResponse(String referenceId, TransactionType type, BigDecimal amount,
                               String direction, String counterpartyEmail, String counterpartyName,
                               TransactionStatus status, String remarks, Instant createdAt) {
        this.referenceId = referenceId;
        this.type = type;
        this.amount = amount;
        this.direction = direction;
        this.counterpartyEmail = counterpartyEmail;
        this.counterpartyName = counterpartyName;
        this.status = status;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getCounterpartyEmail() {
        return counterpartyEmail;
    }

    public void setCounterpartyEmail(String counterpartyEmail) {
        this.counterpartyEmail = counterpartyEmail;
    }

    public String getCounterpartyName() {
        return counterpartyName;
    }

    public void setCounterpartyName(String counterpartyName) {
        this.counterpartyName = counterpartyName;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
