package com.wallet;

import com.wallet.dto.request.LoginRequest;
import com.wallet.dto.request.RegisterRequest;
import com.wallet.dto.request.TransferRequest;
import com.wallet.dto.response.LoginResponse;
import com.wallet.dto.response.TransactionResponse;
import com.wallet.dto.response.WalletResponse;
import com.wallet.entity.Role;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.exception.InsufficientBalanceException;
import com.wallet.exception.WalletFrozenException;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.AdminService;
import com.wallet.service.AuthService;
import com.wallet.service.TransactionService;
import com.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EndToEndAcceptanceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Long namanUserId;
    private Long rahulUserId;
    private Long namanWalletId;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Register user Naman
        RegisterRequest namanRegister = new RegisterRequest();
        namanRegister.setName("Naman");
        namanRegister.setEmail("naman@e2e.com");
        namanRegister.setPhone("9876543201");
        namanRegister.setPassword("Password123!");
        authService.register(namanRegister);

        User naman = userRepository.findByEmail("naman@e2e.com").orElseThrow();
        namanUserId = naman.getId();
        namanWalletId = walletRepository.findByUserId(namanUserId).orElseThrow().getId();

        // 2. Register user Rahul
        RegisterRequest rahulRegister = new RegisterRequest();
        rahulRegister.setName("Rahul");
        rahulRegister.setEmail("rahul@e2e.com");
        rahulRegister.setPhone("9876543202");
        rahulRegister.setPassword("Password123!");
        authService.register(rahulRegister);

        User rahul = userRepository.findByEmail("rahul@e2e.com").orElseThrow();
        rahulUserId = rahul.getId();
    }

    @Test
    @DisplayName("PRD Section 21: Exact 18-step End-to-End Acceptance Test")
    void executePRDSection21AcceptanceFlow() {
        // 3. Login as Naman
        LoginRequest namanLogin = new LoginRequest();
        namanLogin.setEmail("naman@e2e.com");
        namanLogin.setPassword("Password123!");
        LoginResponse namanLoginRes = authService.login(namanLogin);
        assertNotNull(namanLoginRes.getToken());

        // 4. Naman adds 10,000.00
        walletService.addMoney(namanUserId, new BigDecimal("10000.00"));

        // 5. Verify Naman balance = 10,000.00
        WalletResponse namanWallet = walletService.getWalletByUserId(namanUserId);
        assertEquals(0, new BigDecimal("10000.00").compareTo(namanWallet.getBalance()));

        // 6. Naman transfers 2,000.00 to Rahul
        TransferRequest transfer1 = new TransferRequest();
        transfer1.setReceiverEmail("rahul@e2e.com");
        transfer1.setAmount(new BigDecimal("2000.00"));
        transfer1.setRemarks("Dinner payment");
        transactionService.transfer(namanUserId, transfer1);

        // 7. Verify Naman balance = 8,000.00
        namanWallet = walletService.getWalletByUserId(namanUserId);
        assertEquals(0, new BigDecimal("8000.00").compareTo(namanWallet.getBalance()));

        // 8. Login as Rahul
        LoginRequest rahulLogin = new LoginRequest();
        rahulLogin.setEmail("rahul@e2e.com");
        rahulLogin.setPassword("Password123!");
        LoginResponse rahulLoginRes = authService.login(rahulLogin);
        assertNotNull(rahulLoginRes.getToken());

        // 9. Verify Rahul balance = 2,000.00
        WalletResponse rahulWallet = walletService.getWalletByUserId(rahulUserId);
        assertEquals(0, new BigDecimal("2000.00").compareTo(rahulWallet.getBalance()));

        // 10. Verify both users can see the transfer in their own history
        List<TransactionResponse> namanHistory = transactionService.getTransactionHistory(namanUserId);
        List<TransactionResponse> rahulHistory = transactionService.getTransactionHistory(rahulUserId);
        assertTrue(namanHistory.stream().anyMatch(tx -> tx.getAmount().compareTo(new BigDecimal("2000.00")) == 0));
        assertTrue(rahulHistory.stream().anyMatch(tx -> tx.getAmount().compareTo(new BigDecimal("2000.00")) == 0));

        // 11. Naman attempts to transfer 20,000.00 (Exceeds balance)
        TransferRequest transferExcess = new TransferRequest();
        transferExcess.setReceiverEmail("rahul@e2e.com");
        transferExcess.setAmount(new BigDecimal("20000.00"));
        assertThrows(InsufficientBalanceException.class, () -> transactionService.transfer(namanUserId, transferExcess));

        // 13. Verify Naman balance remains 8,000.00
        namanWallet = walletService.getWalletByUserId(namanUserId);
        assertEquals(0, new BigDecimal("8000.00").compareTo(namanWallet.getBalance()));

        // 14. Admin freezes Naman's wallet
        adminService.freezeWallet(namanWalletId);

        // 15 & 16. Naman attempts a transfer -> Verify operation is rejected
        TransferRequest transferFrozen = new TransferRequest();
        transferFrozen.setReceiverEmail("rahul@e2e.com");
        transferFrozen.setAmount(new BigDecimal("500.00"));
        assertThrows(WalletFrozenException.class, () -> transactionService.transfer(namanUserId, transferFrozen));

        // 17. Admin activates Naman's wallet
        adminService.activateWallet(namanWalletId);

        // 18. Verify Naman can transact again
        TransactionResponse successTx = transactionService.transfer(namanUserId, transferFrozen);
        assertNotNull(successTx.getReferenceId());
        namanWallet = walletService.getWalletByUserId(namanUserId);
        assertEquals(0, new BigDecimal("7500.00").compareTo(namanWallet.getBalance()));
    }
}
