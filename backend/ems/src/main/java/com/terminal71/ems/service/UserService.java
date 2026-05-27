package com.terminal71.ems.service;

import com.terminal71.ems.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> getAll();
    Optional<UserDto> getById(Long id);
    UserDto add(UserDto u);
}
