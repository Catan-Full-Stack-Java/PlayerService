package com.dzieger.dtos;

public class WalletDTO {

    private int balance;
    private int changeAmount;

    public WalletDTO() {
    }

    public WalletDTO(int balance, int changeAmount) {
        this.balance = balance;
        this.changeAmount = changeAmount;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(int changeAmount) {
        this.changeAmount = changeAmount;
    }

}
