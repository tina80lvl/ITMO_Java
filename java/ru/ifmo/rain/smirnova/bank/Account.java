package ru.ifmo.rain.smirnova.bank;

import java.io.Serializable;

public interface Account extends Serializable {
    String getId();

    int getAmount();

    void setAmount(int amount);
}