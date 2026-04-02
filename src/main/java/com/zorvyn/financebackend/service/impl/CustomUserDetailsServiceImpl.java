package com.zorvyn.financebackend.service.impl;

import com.zorvyn.financebackend.model.User;
import com.zorvyn.financebackend.repository.UserRepository;
import com.zorvyn.financebackend.security.CustomUserDetails;
import com.zorvyn.financebackend.service.CustomUserDetailsServiceInterface;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsServiceImpl implements CustomUserDetailsServiceInterface {

    private final UserRepository userRepo;

    public CustomUserDetailsServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new CustomUserDetails(user);
    }
}