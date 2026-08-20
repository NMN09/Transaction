package com.wallet.service;

import com.wallet.dto.response.AdminUserResponse;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponse;
import com.wallet.entity.*;
import com.wallet.exception.ResourceNotFoundException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AdminService adminService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet();
        ReflectionTestUtils.setField(wallet, "id", 10L);
        wallet.setBalance(new BigDecimal("5000.00"));
        wallet.setStatus(WalletStatus.ACTIVE);
    }

    @Test
    @DisplayName("Admin can view all users with their wallet status and wallet ID")
    void getAllUsers_Success() {
        User user1 = new User();
        ReflectionTestUtils.setField(user1, "id", 1L);
        user1.setName("Naman");
        user1.setEmail("naman@gmail.com");
        user1.setRole(Role.USER);

        User user2 = new User();
        ReflectionTestUtils.setField(user2, "id", 2L);
        user2.setName("Admin");
        user2.setEmail("admin@gmail.com");
        user2.setRole(Role.ADMIN);

        Wallet wallet1 = new Wallet();
        ReflectionTestUtils.setField(wallet1, "id", 101L);
        wallet1.setStatus(WalletStatus.ACTIVE);

        Wallet wallet2 = new Wallet();
        ReflectionTestUtils.setField(wallet2, "id", 102L);
        wallet2.setStatus(WalletStatus.FROZEN);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(wallet1));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(wallet2));

        List<AdminUserResponse> users = adminService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("Naman", users.get(0).getName());
        assertEquals(101L, users.get(0).getWalletId());
        assertEquals(WalletStatus.ACTIVE, users.get(0).getWalletStatus());
        assertEquals("Admin", users.get(1).getName());
        assertEquals(102L, users.get(1).getWalletId());
        assertEquals(WalletStatus.FROZEN, users.get(1).getWalletStatus());
    }

    @Test
    @DisplayName("Admin can view all transactions platform-wide")
    void getAllTransactions_Success() {
        Transaction tx = new Transaction();
        tx.setReferenceId("TX-GLOBAL-01");
        tx.setType(TransactionType.ADD_MONEY);
        tx.setAmount(new BigDecimal("5000.00"));
        tx.setStatus(TransactionStatus.SUCCESS);

        when(transactionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(tx));

        List<TransactionResponse> transactions = adminService.getAllTransactions();

        assertEquals(1, transactions.size());
        assertEquals("TX-GLOBAL-01", transactions.get(0).getReferenceId());
    }

    @Test
    @DisplayName("PRD Test 14: Admin freezes wallet successfully")
    void freezeWallet_Success() {
        when(walletRepository.findById(10L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = adminService.freezeWallet(10L);

        assertEquals(WalletStatus.FROZEN, response.getStatus());
        assertEquals(WalletStatus.FROZEN, wallet.getStatus());
        verify(walletRepository).save(wallet);
    }

    @Test
    @DisplayName("PRD Test 17: Admin activates frozen wallet successfully")
    void activateWallet_Success() {
        wallet.setStatus(WalletStatus.FROZEN);

        when(walletRepository.findById(10L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = adminService.activateWallet(10L);

        assertEquals(WalletStatus.ACTIVE, response.getStatus());
        assertEquals(WalletStatus.ACTIVE, wallet.getStatus());
        verify(walletRepository).save(wallet);
    }

    @Test
    @DisplayName("Freeze wallet throws ResourceNotFoundException when wallet does not exist")
    void freezeWallet_NotFound() {
        when(walletRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.freezeWallet(999L));
    }
}
