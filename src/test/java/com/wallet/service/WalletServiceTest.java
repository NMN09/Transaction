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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet activeWallet;
    private Wallet frozenWallet;

    @BeforeEach
    void setUp() {
        activeWallet = new Wallet();
        ReflectionTestUtils.setField(activeWallet, "id", 10L);
        activeWallet.setBalance(new BigDecimal("5000.00"));
        activeWallet.setStatus(WalletStatus.ACTIVE);

        frozenWallet = new Wallet();
        ReflectionTestUtils.setField(frozenWallet, "id", 20L);
        frozenWallet.setBalance(new BigDecimal("5000.00"));
        frozenWallet.setStatus(WalletStatus.FROZEN);
    }

    @Test
    @DisplayName("PRD Test 7: User can view their own wallet")
    void getWalletByUserId_Success() {
        // Arrange
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(activeWallet));

        // Act
        WalletResponse response = walletService.getWalletByUserId(1L);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getWalletId());
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
        assertEquals(WalletStatus.ACTIVE, response.getStatus());
    }

    @Test
    @DisplayName("Wallet not found throws ResourceNotFoundException")
    void getWalletByUserId_NotFound_ThrowsException() {
        when(walletRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> walletService.getWalletByUserId(99L));
    }

    @Test
    @DisplayName("PRD Test 8: Add money increases balance and creates transaction")
    void addMoney_Success() {
        // Arrange
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(activeWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction savedTx = new Transaction();
        savedTx.setReferenceId("TX-ADD123456789");
        savedTx.setType(TransactionType.ADD_MONEY);
        savedTx.setAmount(new BigDecimal("1000.00"));
        savedTx.setStatus(TransactionStatus.SUCCESS);
        savedTx.setRemarks("Added money to wallet");

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        // Act
        TransactionResponse response = walletService.addMoney(1L, new BigDecimal("1000.00"));

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("6000.00"), activeWallet.getBalance()); // 5000 + 1000
        assertEquals(TransactionType.ADD_MONEY, response.getType());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());

        // Verify transaction fields
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction capturedTx = txCaptor.getValue();
        assertNull(capturedTx.getSenderWallet());
        assertEquals(activeWallet, capturedTx.getReceiverWallet());
        assertEquals(new BigDecimal("1000.00"), capturedTx.getAmount());
    }

    @Test
    @DisplayName("PRD Test 9: Withdraw decreases balance and creates transaction")
    void withdraw_Success() {
        // Arrange
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(activeWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction savedTx = new Transaction();
        savedTx.setReferenceId("TX-WTH123456789");
        savedTx.setType(TransactionType.WITHDRAW);
        savedTx.setAmount(new BigDecimal("2000.00"));
        savedTx.setStatus(TransactionStatus.SUCCESS);
        savedTx.setRemarks("Withdrew money from wallet");

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        // Act
        TransactionResponse response = walletService.withdraw(1L, new BigDecimal("2000.00"));

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("3000.00"), activeWallet.getBalance()); // 5000 - 2000
        assertEquals(TransactionType.WITHDRAW, response.getType());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());

        // Verify transaction fields
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction capturedTx = txCaptor.getValue();
        assertEquals(activeWallet, capturedTx.getSenderWallet());
        assertNull(capturedTx.getReceiverWallet());
        assertEquals(new BigDecimal("2000.00"), capturedTx.getAmount());
    }

    @Test
    @DisplayName("PRD Test 10: Withdraw above balance fails and leaves balance unchanged")
    void withdraw_InsufficientBalance_ThrowsExceptionAndLeavesBalanceUnchanged() {
        // Arrange
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(activeWallet));

        // Act & Assert
        BigDecimal attemptAmount = new BigDecimal("10000.00");
        assertThrows(InsufficientBalanceException.class, () -> walletService.withdraw(1L, attemptAmount));

        // Verify balance remained 5000.00 and no saves occurred
        assertEquals(new BigDecimal("5000.00"), activeWallet.getBalance());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PRD Test 16: Frozen wallet cannot add money")
    void addMoney_FrozenWallet_ThrowsWalletFrozenException() {
        // Arrange
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(frozenWallet));

        // Act & Assert
        assertThrows(WalletFrozenException.class, () -> walletService.addMoney(2L, new BigDecimal("100.00")));
        assertEquals(new BigDecimal("5000.00"), frozenWallet.getBalance());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PRD Test 16: Frozen wallet cannot withdraw money")
    void withdraw_FrozenWallet_ThrowsWalletFrozenException() {
        // Arrange
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(frozenWallet));

        // Act & Assert
        assertThrows(WalletFrozenException.class, () -> walletService.withdraw(2L, new BigDecimal("100.00")));
        assertEquals(new BigDecimal("5000.00"), frozenWallet.getBalance());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}
