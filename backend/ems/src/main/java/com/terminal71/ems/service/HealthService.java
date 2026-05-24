package com.terminal71.ems.service;

import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public String getStatus() {
        return "OK";
    }
}
