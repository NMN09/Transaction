package com.wallet.service;

import com.wallet.dto.request.TransferRequest;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.entity.Transaction;
import com.wallet.entity.TransactionStatus;
import com.wallet.entity.TransactionType;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletStatus;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.exception.ResourceNotFoundException;
import com.wallet.exception.SelfTransferException;
import com.wallet.exception.WalletFrozenException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(UserRepository userRepository,
                              WalletRepository walletRepository,
                              TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse transfer(Long senderUserId, TransferRequest request) {
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        Wallet senderWallet = walletRepository.findByUserId(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender wallet not found"));

        String normalizedReceiverEmail = request.getReceiverEmail().trim().toLowerCase();
        User receiver = userRepository.findByEmail(normalizedReceiverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with email: " + normalizedReceiverEmail));

        Wallet receiverWallet = walletRepository.findByUserId(receiver.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver wallet not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new SelfTransferException("Cannot transfer money to yourself");
        }

        if (senderWallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Sender wallet is frozen");
        }

        if (receiverWallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Receiver wallet is frozen");
        }

        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transaction transaction = new Transaction();
        transaction.setSenderWallet(senderWallet);
        transaction.setReceiverWallet(receiverWallet);
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId("TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        transaction.setRemarks(request.getRemarks() != null ? request.getRemarks() : "Money transfer");

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

    @Transactional(readOnly = true)
    public java.util.List<TransactionResponse> getTransactionHistory(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user id: " + userId));

        java.util.List<Transaction> transactions = transactionRepository
                .findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc(wallet.getId(), wallet.getId());

        return transactions.stream()
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
}

