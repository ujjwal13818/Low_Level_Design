package com.lld.solid.srp.bad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Users {
    Map<Integer, List<String>> userData;
    Integer userId = 0;
    private void storeData(String userName, String password, String name) {
        if(validatePassword(password)) {
            userData.put(userId, new ArrayList<>(List.of(userName, password, name)));
            sendNotification(userName);
            userId++;
        }
    }

    private Boolean validatePassword(String password) {
        if(password.length() < 5 || password.length() > 10) {
            return false;
        }
        return true;
    }

    private void sendNotification(String userName) {
        System.out.println("Sending notification to " + userName);
    }


}


//Single Responsibility Principle
//It is bad because of the following reasons:
//1. changing in one functionality will lead to redeploy whole class which means now other functionality will come in scope of testing.
//2. Engineers working on different functionalities will now have more merge conflicts.
//3. Tightly coupled.
