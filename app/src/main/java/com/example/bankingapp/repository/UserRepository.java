package com.example.bankingapp.repository;

import com.example.bankingapp.model.User;

import java.util.List;

public interface UserRepository {

    User findById(int customerId);

    User findByUsername(String username);

    User findByEmail(String email);

    User findByIdNumber(String idNumber);

    void save(User user);

    List<User> findAll();
}
