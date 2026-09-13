package com.securebank.service;

import com.securebank.dto.request.LoginRequest;
import com.securebank.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse adminLogin(LoginRequest request);
}
