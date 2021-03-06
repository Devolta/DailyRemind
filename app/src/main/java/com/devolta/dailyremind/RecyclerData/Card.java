package com.devolta.dailyremind.RecyclerData;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Card implements Serializable {
    public int drawable;
    private String cardText;
    private String cardDate;
    private String cardTime;
    private String cardRemainingTime;
    private Drawable cardCheck;

    public void cardText(String cardText) {
        this.cardText = cardText;
    }

    public void cardDate(String cardDate) {
        this.cardDate = cardDate;
    }

    public void cardTime(String cardTime) {
        this.cardTime = cardTime;
    }

    public void cardRemainingTime(String cardRemainingTime) {
        this.cardRemainingTime = cardRemainingTime;
    }

    public void cardCheck(Drawable cardCheck) {
        this.cardCheck = cardCheck;
    }

    public String getCardText() {
        return cardText;
    }

    public String getCardDate() {
        return cardDate;
    }

    public String getCardTime() {
        return cardTime;
    }

    public String getCardRemainingTime() {
        return cardRemainingTime;
    }

    public Drawable getCardCheck() {
        return cardCheck;
    }
}
