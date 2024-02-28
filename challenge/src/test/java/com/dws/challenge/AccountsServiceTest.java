package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.lang.*;

class AccountsServiceTest {

    @Mock
    private AccountsRepository accountsRepository;

    private AccountsService accountsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountsService = new AccountsService(accountsRepository);
    }

    @Test
    void testTransferMoney_PositiveScenario() {
        Account accountFrom = new Account("1");
        accountFrom.setBalance(BigDecimal.valueOf(100));

        Account accountTo = new Account("2");
        accountTo.setBalance(BigDecimal.valueOf(50));

        when(accountsRepository.getAccount("1")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("2")).thenReturn(accountTo);

        accountsService.transferMoney("1", "2", BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(50), accountFrom.getBalance());
        assertEquals(BigDecimal.valueOf(100), accountTo.getBalance());
    }

    @Test
    void testTransferMoney_InsufficientBalance() {
        Account accountFrom = new Account("1");
        accountFrom.setBalance(BigDecimal.valueOf(100));

        Account accountTo = new Account("2");
        accountTo.setBalance(BigDecimal.valueOf(50));

        when(accountsRepository.getAccount("1")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("2")).thenReturn(accountTo);

        assertThrows(InsufficientBalanceException.class, () ->
                accountsService.transferMoney("1", "2", BigDecimal.valueOf(150))
        );
    }

    @Test
    void testTransferMoney_ConcurrentTransfers() throws InterruptedException {
        Account accountFrom = new Account("1");
        accountFrom.setBalance(BigDecimal.valueOf(100));

        Account accountTo = new Account("2");
        accountTo.setBalance(BigDecimal.valueOf(50));

        when(accountsRepository.getAccount("1")).thenReturn(accountFrom);
        when(accountsRepository.getAccount("2")).thenReturn(accountTo);

        // Create two threads performing transfers concurrently
        Thread thread1 = new Thread(() -> {
            accountsService.transferMoney("1", "2", BigDecimal.valueOf(50));
        });

        Thread thread2 = new Thread(() -> {
            accountsService.transferMoney("2", "1", BigDecimal.valueOf(30));
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Verify balances after concurrent transfers
        assertEquals(BigDecimal.valueOf(50), accountFrom.getBalance());
        assertEquals(BigDecimal.valueOf(70), accountTo.getBalance());
    }

}
