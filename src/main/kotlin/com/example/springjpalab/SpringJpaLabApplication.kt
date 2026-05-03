package com.example.springjpalab

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class SpringJpaLabApplication

fun main(args: Array<String>) {
    runApplication<SpringJpaLabApplication>(*args)
}
