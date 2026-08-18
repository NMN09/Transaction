package com.wallet.controller;

import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.UserProfileResponse;
import com.wallet.dto.response.WalletResponse;
import com.wallet.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        List<UserProfileResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> transactions = adminService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/wallets/{walletId}/freeze")
    public ResponseEntity<WalletResponse> freezeWallet(@PathVariable Long walletId) {
        WalletResponse response = adminService.freezeWallet(walletId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/wallets/{walletId}/activate")
    public ResponseEntity<WalletResponse> activateWallet(@PathVariable Long walletId) {
        WalletResponse response = adminService.activateWallet(walletId);
        return ResponseEntity.ok(response);
    }
}
