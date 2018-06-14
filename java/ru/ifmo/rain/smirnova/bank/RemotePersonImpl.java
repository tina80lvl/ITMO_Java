package ru.ifmo.rain.smirnova.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class RemotePersonImpl implements RemotePerson {
    protected ConcurrentHashMap<String, Account> accounts;
    protected String name;
    protected String surname;
    protected String passNum;

    public RemotePersonImpl(String name, String surname, String passNum) throws RemoteException {
        this.name = name;
        this.surname = surname;
        this.passNum = passNum;
        accounts = new ConcurrentHashMap<>();
        UnicastRemoteObject.exportObject(this, 0);
    }

    public String getName() throws RemoteException {
        return name;
    }

    public String getSurname() throws RemoteException {
        return surname;
    }

    public String getPassNum() throws RemoteException {
        return passNum;
    }

    public ConcurrentHashMap<String, Account> getAccounts() throws RemoteException {
        return accounts;
    }

    private Account getAccount(String accountId) throws RemoteException {
        if (!accounts.containsKey(accountId)) {
            accounts.put(accountId, new AccountImpl(accountId));
        }
        return accounts.get(accountId);
    }

    public int setToAccount(String accountId, int amount) throws RemoteException {
        Account acc = this.getAccount(accountId);
        acc.setAmount(amount);
        accounts.put(accountId, acc);
        return acc.getAmount();
    }

    public String showMoney(String key) throws RemoteException {
        String uid = name + "_" + surname + "_" + passNum;
        Account account = getAccount(key);
        String s = uid + ": <" + account.getId() + ", " + account.getAmount() + ">";
        return s;
    }

    public int getMoney(String key) throws RemoteException {
        Account account = getAccount(key);
        return account.getAmount();
    }

}