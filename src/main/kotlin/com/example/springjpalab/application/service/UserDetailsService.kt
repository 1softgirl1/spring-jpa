package com.example.springjpalab.application.service

import com.example.springjpalab.adapter.output.jpa.repository.UserJpaRepository
import org.springframework.stereotype.Service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException


@Service
class CustomUserDetailsService(
    private val userRepository: UserJpaRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("Пользователь не найден: $username")
    }
}