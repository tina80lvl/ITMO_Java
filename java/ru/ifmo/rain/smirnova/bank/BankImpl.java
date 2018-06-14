package ru.ifmo.rain.smirnova.bank;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class BankImpl implements Bank {
    private ConcurrentHashMap<String, RemotePerson> persons;
    private static Registry registry = null;

    public BankImpl() {
        persons = new ConcurrentHashMap<>();
        try {
            registry = LocateRegistry.createRegistry(8080);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static void main(String[] args) {
        BankImpl bank = new BankImpl();
        try {
            Remote stub =  UnicastRemoteObject.exportObject((Remote) bank, 8080);
            registry.bind("Bank", (Remote) stub);
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    public Person getPerson(String passNum, String type) throws RemoteException {
        if (!("remote".equals(type) || "serialisation".equals(type))) {
            return null;
        }

        if (!persons.containsKey(passNum))
            return null;

        if ("serialisation".equals(type)) {
            return new LocalPersonImpl(persons.get(passNum));
        }
        if ("remote".equals(type)) {
            return persons.get(passNum);
        }
        return null;
    }

    public boolean createPerson(String name, String surname, String passNum) {
        try {
            if (!persons.containsKey(passNum)) {
                persons.put(passNum, new RemotePersonImpl(name, surname, passNum));
                return true;
            } else {
                return false;
            }

        } catch (RemoteException e) {
            return false;
        }
    }
}