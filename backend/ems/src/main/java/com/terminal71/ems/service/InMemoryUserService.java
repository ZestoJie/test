package com.terminal71.ems.service;

import com.terminal71.ems.dto.UserDto;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;

@Service
@Primary
public class InMemoryUserService implements UserService {
    private final List<UserDto> users = new ArrayList<>();

    public InMemoryUserService() {
        users.add(new UserDto(1L, "Alice", "Employee", 12));
        users.add(new UserDto(2L, "Bob", "Admin", 90));
    }

    
    @SuppressWarnings("override")
    public List<UserDto> getAll() {
        return users;
    }

    
    @SuppressWarnings("override")
    public Optional<UserDto> getById(Long id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    @SuppressWarnings("override")
    public UserDto add(UserDto u) {
        long next = users.stream().mapToLong(u2 -> u2.getId()).max().orElse(0) + 1;
        u.setId(next);
        users.add(u);
        return u;
    }
}
