package com.myprojects.corso.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {

    private String email;
    private boolean grantedAccess;
    private boolean isCoffeeShopAdmin;
    private HashMap<String, Integer> weekleyCoffees = new HashMap<>();
    private Long todaysCoffees;
    private java.util.Date lastAccessDate;
    private static  User user = new User();

    public static User getUser() {
        return user;
    }

    public void setEmail(String email) {
        user.email = email;
    }

    public void setGrantedAccess(boolean grantedAccess) {
        user.grantedAccess = grantedAccess;
    }

    public void setCoffeeShopAdmin(boolean coffeeShopAdmin) {
        user.isCoffeeShopAdmin = coffeeShopAdmin;
    }

    public void setWeekleyCoffees(HashMap<String, Long> weekleyCoffees) {
        for(String key : weekleyCoffees.keySet()) {
            user.weekleyCoffees.put(key, weekleyCoffees.get(key).intValue());
        }
    }

    public void setTodaysCoffees(Long todaysCoffees) {
        user.todaysCoffees = todaysCoffees;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        user.lastAccessDate = lastAccessDate;
    }

    public String getEmail() {
        return email;
    }
    public boolean isGrantedAccess() {
        return grantedAccess;
    }

    public boolean isCoffeeShopAdmin() {
        return isCoffeeShopAdmin;
    }

    public HashMap<String, Integer> getWeekleyCoffees() {
        return weekleyCoffees;
    }

    public Integer getTodaysCoffees() {
        return todaysCoffees.intValue();
    }

    public java.util.Date getLastAccessDate() {
        return lastAccessDate;
    }
}
