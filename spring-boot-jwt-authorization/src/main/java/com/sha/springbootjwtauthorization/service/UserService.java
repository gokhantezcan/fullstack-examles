package com.sha.springbootjwtauthorization.service;

import com.sha.springbootjwtauthorization.model.Role;
import com.sha.springbootjwtauthorization.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

     public User saveUser(User user);

     public Optional<User> findByUsername(String username);

     public void changeRole(Role role, String username);

     public List<User> findAllUsers();
}
