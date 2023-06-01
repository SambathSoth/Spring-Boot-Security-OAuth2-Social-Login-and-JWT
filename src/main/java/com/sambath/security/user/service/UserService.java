package com.sambath.security.user.service;

import com.sambath.security.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void enableUser(String email) {
        userRepository.enableUser(email);
    }
}
