package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountsService {

    @Getter
    private final AccountsRepository accountsRepository;

    @Autowired
    public AccountsService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

	/*Mark the methods transactional so that any database operations performed within the marked method will be executed within a transaction. If the 	
    transaction is successful, the changes will be committed to the database. If an error occurs and the transaction is rolled back, the changes will not be 	
    persisted in the database*/

    @Transactional
    public void createAccount(Account account) {
        this.accountsRepository.createAccount(account);
    }


    @Transactional
    public void transferMoney(String accountFromId, String accountToId, BigDecimal amount) {
        Account accountFrom = accountsRepository.getAccount(accountFromId);
        Account accountTo = accountsRepository.getAccount(accountToId);

        if (accountFrom == null || accountTo == null) {
            throw new IllegalArgumentException("One or both accounts do not exist");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        synchronized (accountFromId.intern()) {
            if (accountFrom.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException("Insufficient balance in account: " + accountFromId);
            }

            accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
            accountTo.setBalance(accountTo.getBalance().add(amount));
        }

        // Notification logic to be implemented here
    }

    public Account getAccount(String accountId) {
        return this.accountsRepository.getAccount(accountId);
    }
}