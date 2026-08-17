package com.wallet.dto.response;

import com.wallet.entity.WalletStatus;
import java.math.BigDecimal;

public class WalletResponse {

    private Long walletId;
    private BigDecimal balance;
    private WalletStatus status;

    public WalletResponse(Long walletId, BigDecimal balance, WalletStatus status) {
        this.walletId = walletId;
        this.balance = balance;
        this.status = status;
    }

    public Long getWalletId() {
        return walletId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public WalletStatus getStatus() {
        return status;
    }
}
