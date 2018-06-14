package ru.ifmo.rain.smirnova.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemotePerson extends Remote, Person {
    public String getName() throws RemoteException;

    public String getSurname() throws RemoteException;

    public String getPassNum() throws RemoteException;

    public int setToAccount(String accountId, int amount) throws RemoteException;

    public String showMoney(String key) throws RemoteException;

    public int getMoney(String key) throws RemoteException;
}