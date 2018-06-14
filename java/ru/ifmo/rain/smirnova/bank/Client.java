package ru.ifmo.rain.smirnova.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private static final String USAGE = "usage: \n Client name surname passportNum account'sId modification wayToShareObject {remote, serialisation})";
    private static final String PASS_ERROR = "this pass number is already registered for other person.";
    private static final String CREATE_ERR = "error while creation.";

    private static boolean checkPerson(Person p, String name, String surname) {
        try {
            if (p.getName().equals(name) && p.getSurname().equals(surname))
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        Bank bank = null;

        if (args.length < 6) {
            System.out.println(USAGE);
            System.exit(1);
        }
        if (!("remote".equals(args[5]) || "serialisation".equals(args[5]))) {
            System.out.println(USAGE);
            System.exit(1);
        }

        try {
            Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println(USAGE);
            System.exit(1);
        }


        try {
            Registry registry = LocateRegistry.getRegistry(null, 8080);
            bank = (Bank) registry.lookup("Bank");
        } catch (NotBoundException e) {
            System.out.println("not bound");
            System.exit(1);
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        if ("remote".equals(args[5])) {
            try {
                RemotePerson person = (RemotePerson) bank.getPerson(args[2], args[5]);
                if (person == null) {
                    if (!bank.createPerson(args[0], args[1], args[2])) {
                        System.out.println(CREATE_ERR);
                        System.exit(1);
                    }
                } else if (!checkPerson(person, args[0], args[1])) {
                    System.out.println(PASS_ERROR);
                    System.exit(1);
                }

                person = (RemotePerson) bank.getPerson(args[2], args[5]);
                System.out.println("created person: " + person.getName());

                System.out.println(person.showMoney(args[3]));
                person.setToAccount(args[3], Integer.parseInt(args[4]) + person.getMoney(args[3]));
                System.out.println("changed! " + person.showMoney(args[3]));


                person = (RemotePerson) bank.getPerson(args[2], args[5]);
                System.out.println("retrieved person: " + person.getName());

                System.out.println(person.showMoney(args[3]));

            } catch (RemoteException e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        } else {

            try {
                LocalPerson person = (LocalPerson) bank.getPerson(args[2], args[5]);
                if (person == null) {
                    if (!bank.createPerson(args[0], args[1], args[2])) {
                        System.out.println(CREATE_ERR);
                        System.exit(1);
                    }
                } else if (!checkPerson(person, args[0], args[1])) {
                    System.out.println(PASS_ERROR);
                    System.exit(1);
                }

                person = (LocalPerson) bank.getPerson(args[2], args[5]);
                System.out.println("local person: " + person.getName());

                System.out.println(person.showMoney(args[3]));
                person.setToAccount(args[3], Integer.parseInt(args[4]) + person.getMoney(args[3]));
                System.out.println("changed! " + person.showMoney(args[3]));


                person = (LocalPerson) bank.getPerson(args[2], args[5]);
                System.out.println("retreaved person: " + person.getName());

                System.out.println(person.showMoney(args[3]));

            } catch (RemoteException e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }

        System.out.println(bank.getPerson("224671", "remote").getName());
        System.out.println(bank.getPerson("3485", "remote").getSurname());
        System.out.println(bank.getPerson("224671", "remote").getAccounts());
        System.out.println(bank.getPerson("224671", "remote").showMoney("140014"));
        System.out.println(bank.getPerson("93764", "serialisation").showMoney("777"));
        bank.getPerson("93764", "serialisation").setToAccount("777", 700);
        System.out.println(bank.getPerson("93764", "serialisation").showMoney("777"));
    }

}