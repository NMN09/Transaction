package com.wallet.service;

import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponse;
import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletStatus;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.exception.ResourceNotFoundException;
import com.wallet.exception.WalletFrozenException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user id: " + userId));

        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getStatus()
        );
    }

    @Transactional
    public TransactionResponse addMoney(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user id: " + userId));

        if (wallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Wallet is frozen");
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setSenderWallet(null);
        transaction.setReceiverWallet(wallet);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.ADD_MONEY);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId("TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        transaction.setRemarks("Added money to wallet");

        Transaction savedTx = transactionRepository.save(transaction);

        return new TransactionResponse(
                savedTx.getReferenceId(),
                savedTx.getType(),
                savedTx.getAmount(),
                savedTx.getStatus(),
                savedTx.getRemarks(),
                savedTx.getCreatedAt()
        );
    }

    @Transactional
    public TransactionResponse withdraw(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user id: " + userId));

        if (wallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Wallet is frozen");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setSenderWallet(wallet);
        transaction.setReceiverWallet(null);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId("TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        transaction.setRemarks("Withdrew money from wallet");

        Transaction savedTx = transactionRepository.save(transaction);

        return new TransactionResponse(
                savedTx.getReferenceId(),
                savedTx.getType(),
                savedTx.getAmount(),
                savedTx.getStatus(),
                savedTx.getRemarks(),
                savedTx.getCreatedAt()
        );
    }
}

