package com.lld.solid.srp.good;

import java.util.HashMap;
import java.util.Map;

// 4. Only responsibility: store/manage the collection of users
public class UserRepository {
    private final Map<Integer, User> userData = new HashMap<>();
    private int userId = 0;

    public void save(User user) {
        userData.put(userId++, user);
    }
}