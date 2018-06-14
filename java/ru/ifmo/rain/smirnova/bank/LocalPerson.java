package ru.ifmo.rain.smirnova.bank;

import java.io.Serializable;

public interface LocalPerson extends Serializable, Person {
    public String getName();

    public String getSurname();

    public String getPassNum();

    public int setToAccount(String accountId, int amount);

    public String showMoney(String key);

    public int getMoney(String key);
}