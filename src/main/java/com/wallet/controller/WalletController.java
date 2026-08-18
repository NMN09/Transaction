package com.wallet.controller;

import com.wallet.dto.request.AmountRequest;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponse;
import com.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public ResponseEntity<WalletResponse> getWallet(
            @AuthenticationPrincipal Long userId) {

        WalletResponse response = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-money")
    public ResponseEntity<TransactionResponse> addMoney(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AmountRequest request) {

        TransactionResponse response = walletService.addMoney(userId, request.getAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AmountRequest request) {

        TransactionResponse response = walletService.withdraw(userId, request.getAmount());
        return ResponseEntity.ok(response);
    }
}

