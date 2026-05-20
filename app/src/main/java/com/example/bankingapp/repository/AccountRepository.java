package com.example.bankingapp.repository;

import com.example.bankingapp.model.Account;

import java.util.List;

public interface AccountRepository {

    Account findByNumber(String accountNumber);

    List<Account> findByOwner(int customerId);

    void save(Account account);

    List<Account> findAll();
}
