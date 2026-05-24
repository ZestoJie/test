package com.terminal71.ems.controller;

import com.terminal71.ems.dto.UserDto;
import com.terminal71.ems.service.InMemoryUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MockDataController {

    private final InMemoryUserService userService;

    public MockDataController(InMemoryUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "time", System.currentTimeMillis());
    }

    @GetMapping("/api/users")
    public List<UserDto> users() {
        return userService.getAll();
    }

    @GetMapping("/api/users/{id}")
    public UserDto userById(@PathVariable Long id) {
        return userService.getById(id).orElse(null);
    }

    @PostMapping("/api/users")
    public UserDto create(@RequestBody UserDto u) {
        return userService.add(u);
    }
}
