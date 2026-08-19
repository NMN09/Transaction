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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User sender;
    private User receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        sender = new User();
        ReflectionTestUtils.setField(sender, "id", 1L);
        sender.setEmail("naman@gmail.com");

        senderWallet = new Wallet();
        ReflectionTestUtils.setField(senderWallet, "id", 101L);
        senderWallet.setUser(sender);
        senderWallet.setBalance(new BigDecimal("10000.00"));
        senderWallet.setStatus(WalletStatus.ACTIVE);

        receiver = new User();
        ReflectionTestUtils.setField(receiver, "id", 2L);
        receiver.setEmail("rahul@gmail.com");

        receiverWallet = new Wallet();
        ReflectionTestUtils.setField(receiverWallet, "id", 102L);
        receiverWallet.setUser(receiver);
        receiverWallet.setBalance(new BigDecimal("2000.00"));
        receiverWallet.setStatus(WalletStatus.ACTIVE);

        transferRequest = new TransferRequest();
        transferRequest.setReceiverEmail("rahul@gmail.com");
        transferRequest.setAmount(new BigDecimal("2000.00"));
        transferRequest.setRemarks("Dinner payment");
    }

    @Test
    @DisplayName("PRD Test 11 & 12: Transfer decreases sender balance, increases receiver balance, and creates exactly one transaction")
    void transfer_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("rahul@gmail.com")).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(receiverWallet));

        Transaction savedTx = new Transaction();
        savedTx.setReferenceId("TX-TRF123456789");
        savedTx.setType(TransactionType.TRANSFER);
        savedTx.setAmount(new BigDecimal("2000.00"));
        savedTx.setStatus(TransactionStatus.SUCCESS);
        savedTx.setRemarks("Dinner payment");

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        // Act
        TransactionResponse response = transactionService.transfer(1L, transferRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("8000.00"), senderWallet.getBalance());   // 10000 - 2000
        assertEquals(new BigDecimal("4000.00"), receiverWallet.getBalance()); // 2000 + 2000
        assertEquals(TransactionType.TRANSFER, response.getType());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());

        // Verify wallets were updated
        verify(walletRepository).save(senderWallet);
        verify(walletRepository).save(receiverWallet);

        // Verify exactly one transaction record was persisted
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        Transaction persistedTx = txCaptor.getValue();
        assertEquals(senderWallet, persistedTx.getSenderWallet());
        assertEquals(receiverWallet, persistedTx.getReceiverWallet());
        assertEquals(new BigDecimal("2000.00"), persistedTx.getAmount());
        assertEquals("Dinner payment", persistedTx.getRemarks());
    }

    @Test
    @DisplayName("PRD Test 13: Failed transfer due to insufficient funds leaves both balances unchanged")
    void transfer_InsufficientBalance_LeavesBalancesUnchanged() {
        // Arrange
        transferRequest.setAmount(new BigDecimal("50000.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("rahul@gmail.com")).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(receiverWallet));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> transactionService.transfer(1L, transferRequest));

        assertEquals(new BigDecimal("10000.00"), senderWallet.getBalance());
        assertEquals(new BigDecimal("2000.00"), receiverWallet.getBalance());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PRD Test 14: Transfer to non-existent receiver fails")
    void transfer_NonExistentReceiver_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("rahul@gmail.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> transactionService.transfer(1L, transferRequest));
        assertEquals(new BigDecimal("10000.00"), senderWallet.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PRD Test 15: Transfer to self fails")
    void transfer_SelfTransfer_ThrowsSelfTransferException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("naman@gmail.com")).thenReturn(Optional.of(sender));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));

        transferRequest.setReceiverEmail("naman@gmail.com");

        // Act & Assert
        assertThrows(SelfTransferException.class, () -> transactionService.transfer(1L, transferRequest));
        assertEquals(new BigDecimal("10000.00"), senderWallet.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PRD Test 16: Frozen sender wallet cannot send transfer")
    void transfer_FrozenSender_ThrowsWalletFrozenException() {
        // Arrange
        senderWallet.setStatus(WalletStatus.FROZEN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("rahul@gmail.com")).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(receiverWallet));

        // Act & Assert
        assertThrows(WalletFrozenException.class, () -> transactionService.transfer(1L, transferRequest));
        assertEquals(new BigDecimal("10000.00"), senderWallet.getBalance());
        assertEquals(new BigDecimal("2000.00"), receiverWallet.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PRD Test 16: Frozen receiver wallet cannot receive transfer")
    void transfer_FrozenReceiver_ThrowsWalletFrozenException() {
        // Arrange
        receiverWallet.setStatus(WalletStatus.FROZEN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));
        when(userRepository.findByEmail("rahul@gmail.com")).thenReturn(Optional.of(receiver));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(receiverWallet));

        // Act & Assert
        assertThrows(WalletFrozenException.class, () -> transactionService.transfer(1L, transferRequest));
        assertEquals(new BigDecimal("10000.00"), senderWallet.getBalance());
        assertEquals(new BigDecimal("2000.00"), receiverWallet.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PRD Test: User transaction history returns all transactions involving user's wallet")
    void getTransactionHistory_Success() {
        // Arrange
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(senderWallet));

        Transaction tx1 = new Transaction();
        tx1.setReferenceId("TX-111");
        tx1.setType(TransactionType.ADD_MONEY);
        tx1.setAmount(new BigDecimal("10000.00"));
        tx1.setStatus(TransactionStatus.SUCCESS);

        Transaction tx2 = new Transaction();
        tx2.setReferenceId("TX-222");
        tx2.setType(TransactionType.TRANSFER);
        tx2.setAmount(new BigDecimal("2000.00"));
        tx2.setStatus(TransactionStatus.SUCCESS);

        when(transactionRepository.findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc(101L, 101L))
                .thenReturn(List.of(tx2, tx1));

        // Act
        List<TransactionResponse> history = transactionService.getTransactionHistory(1L);

        // Assert
        assertEquals(2, history.size());
        assertEquals("TX-222", history.get(0).getReferenceId());
        assertEquals("TX-111", history.get(1).getReferenceId());
    }
}
