package com.terminal71.ems.service;

import com.terminal71.ems.dto.UserDto;
import com.google.firebase.database.DataSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service("firebaseUserService")
@ConditionalOnBean(FirebaseRealtimeService.class)
public class FirebaseUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseUserService.class);
    private final FirebaseRealtimeService rtdb;

    public FirebaseUserService(FirebaseRealtimeService rtdb) {
        this.rtdb = rtdb;
    }

    @Override
    public List<UserDto> getAll() {
        try {
            DataSnapshot snapshot = rtdb.readData("users").get();
            List<UserDto> list = new ArrayList<>();
            if (snapshot != null && snapshot.exists()) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    Object val = child.getValue();
                    if (val instanceof Map m) {
                        UserDto u = mapToUserDto(m);
                        list.add(u);
                    }
                }
            }
            return list;
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Failed to read users from RTDB, falling back to empty list", e);
            return List.of();
        }
    }

    @Override
    public Optional<UserDto> getById(Long id) {
        try {
            DataSnapshot snapshot = rtdb.readData("users/" + id).get();
            if (snapshot != null && snapshot.exists()) {
                Object val = snapshot.getValue();
                if (val instanceof Map map) {
                    return Optional.of(mapToUserDto(map));
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Failed to read user {} from RTDB", id, e);
        }
        return Optional.empty();
    }

    @Override
    public UserDto add(UserDto u) {
        try {
            // compute next id
            List<UserDto> all = getAll();
            long next = all.stream().mapToLong(uu -> uu.getId() == null ? 0 : uu.getId()).max().orElse(0) + 1;
            u.setId(next);
            rtdb.writeData("users/" + next, u).get();
            return u;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to write user to RTDB", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private UserDto mapToUserDto(Map m) {
        UserDto u = new UserDto();
        Object idObj = m.get("id");
        if (idObj instanceof Number number) u.setId(number.longValue());
        else if (idObj != null) u.setId(Long.valueOf(String.valueOf(idObj)));
        u.setName((String) m.getOrDefault("name", ""));
        u.setRole((String) m.getOrDefault("role", "Employee"));
        Object stars = m.get("stars");
        if (stars instanceof Number number) u.setStars(number.intValue());
        else if (stars != null) u.setStars(Integer.parseInt(String.valueOf(stars)));
        return u;
    }
}
