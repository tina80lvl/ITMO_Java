package ru.ifmo.rain.smirnova.bank;

import java.rmi.RemoteException;
import java.rmi.Remote;

public interface Bank extends Remote {
    Person getPerson(String passNum, String type) throws RemoteException;

    boolean createPerson(String name, String surname, String passNum) throws RemoteException;
}