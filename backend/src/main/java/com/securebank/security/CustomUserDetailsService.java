package com.securebank.security;

import com.securebank.entity.Admin;
import com.securebank.entity.Customer;
import com.securebank.repository.AdminRepository;
import com.securebank.repository.CustomerRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Custom UserDetailsService that resolves credentials against both 'customers' and 'admins' tables.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository, AdminRepository adminRepository) {
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // 1. Check if identifier is a Customer Email
        Optional<Customer> customerOpt = customerRepository.findByEmail(identifier);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            Set<GrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority(customer.getRole().name())
            );
            return new User(customer.getEmail(), customer.getPasswordHash(), authorities);
        }

        // 2. Check if identifier is an Admin Username or Email
        Optional<Admin> adminOpt = adminRepository.findByUsername(identifier);
        if (adminOpt.isEmpty()) {
            adminOpt = adminRepository.findByEmail(identifier);
        }

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            Set<GrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority(admin.getRole().name())
            );
            return new User(admin.getUsername(), admin.getPasswordHash(), authorities);
        }

        throw new UsernameNotFoundException("User not found with identifier: " + identifier);
    }
}
