package ru.ifmo.rain.smirnova.bank;

import java.util.concurrent.ConcurrentHashMap;

public interface Person {
    public String getName() throws Exception;

    public String getSurname() throws Exception;

    public String getPassNum() throws Exception;

    public ConcurrentHashMap<String, Account> getAccounts() throws Exception;

    public int setToAccount(String accountId, int amount) throws Exception;

    public String showMoney(String key) throws Exception;

    public int getMoney(String key) throws Exception;

}