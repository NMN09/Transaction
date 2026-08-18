package com.wallet.service;

import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.UserProfileResponse;
import com.wallet.dto.response.WalletResponse;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletStatus;
import com.wallet.exception.ResourceNotFoundException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public AdminService(UserRepository userRepository,
                        WalletRepository walletRepository,
                        TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserProfileResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole(),
                        user.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(tx -> new TransactionResponse(
                        tx.getReferenceId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getStatus(),
                        tx.getRemarks(),
                        tx.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public WalletResponse freezeWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));

        wallet.setStatus(WalletStatus.FROZEN);
        Wallet saved = walletRepository.save(wallet);

        return new WalletResponse(
                saved.getId(),
                saved.getBalance(),
                saved.getStatus()
        );
    }

    @Transactional
    public WalletResponse activateWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));

        wallet.setStatus(WalletStatus.ACTIVE);
        Wallet saved = walletRepository.save(wallet);

        return new WalletResponse(
                saved.getId(),
                saved.getBalance(),
                saved.getStatus()
        );
    }
}
