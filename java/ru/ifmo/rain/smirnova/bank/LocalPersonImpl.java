package ru.ifmo.rain.smirnova.bank;

import java.util.concurrent.ConcurrentHashMap;

public class LocalPersonImpl implements LocalPerson {
    private ConcurrentHashMap<String, Account> accounts;
    private String name;
    private String surname;
    private String passNum;

    public LocalPersonImpl(String name, String surname, String passNum) {
        this.name = name;
        this.surname = surname;
        this.passNum = passNum;
        accounts = new ConcurrentHashMap<>();
    }

    public LocalPersonImpl(Person p) {
        try {
            // no exception can be thrown
            this.name = p.getName();
            this.surname = p.getSurname();
            this.passNum = p.getPassNum();
            this.accounts = p.getAccounts();
        } catch (Exception e) {
            // never reached
            this.name = "";
            this.surname = "";
            this.passNum = "";
            this.accounts = new ConcurrentHashMap<String, Account>();
        }
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getPassNum() {
        return passNum;
    }

    public ConcurrentHashMap<String, Account> getAccounts() {
        return accounts;
    }

    private Account getAccount(String accountId) {
        if (!accounts.containsKey(accountId)) {
            accounts.put(accountId, new AccountImpl(accountId));
        }
        return accounts.get(accountId);
    }

    public int setToAccount(String accountId, int amount) {
        Account acc = this.getAccount(accountId);
        acc.setAmount(amount);
        accounts.put(accountId, acc);
        return acc.getAmount();
    }

    public String showMoney(String key) {
        String uid = name + "_" + surname + "_" + passNum;
        Account account = getAccount(key);
        String s = uid + ": <" + account.getId() + ", " + account.getAmount() + ">";
        return s;
    }

    public int getMoney(String key) {
        Account account = getAccount(key);
        return account.getAmount();
    }

}