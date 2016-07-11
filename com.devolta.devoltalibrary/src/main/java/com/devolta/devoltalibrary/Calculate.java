package com.devolta.devoltalibrary;


public class Calculate {

    public String calcTimeDiff(long then, long now) {
        if (then > now) {
            String timeIndicator;
            String timeIndicator2;
            long remainingTime1 = (then - now) / 1000;
            String remainingTime2 = "";
            long years;
            long months;
            long days;
            long hours;
            long minutes;
            long seconds;


            if (remainingTime1 >= 31556952) {
                years = remainingTime1 / 31556952;
                if (years > 1) {
                    timeIndicator = "years";
                } else {
                    timeIndicator = "year";
                }

                months = remainingTime1 / 2592000 - (years * 12);
                if (months > 1) {
                    timeIndicator2 = "months";
                } else {
                    timeIndicator2 = "month";
                }
                remainingTime2 = years + timeIndicator + " " + months + timeIndicator2;

            } else if (remainingTime1 >= 2592000) {
                months = remainingTime1 / 2592000;
                if (months > 1) {
                    timeIndicator = "months";
                } else {
                    timeIndicator = "month";
                }

                days = remainingTime1 / 86400 - (months * 30);
                if (days > 1) {
                    timeIndicator2 = "days";
                } else {
                    timeIndicator2 = "day";
                }
                remainingTime2 = months + timeIndicator + " " + days + timeIndicator2;

            } else if (remainingTime1 >= 86400) {
                days = remainingTime1 / 86400;
                if (days > 1) {
                    timeIndicator2 = "days";
                } else {
                    timeIndicator2 = "day";
                }
                hours = remainingTime1 / 3600 - (days * 24);
                remainingTime2 = days + timeIndicator2 + " " + hours + "h";

            } else if (remainingTime1 >= 3600) {
                hours = remainingTime1 / 3600;
                minutes = remainingTime1 / 60 - (hours * 60);
                remainingTime2 = hours + "h " + minutes + "min";

            } else if (remainingTime1 >= 60) {
                minutes = remainingTime1 / 60;
                seconds = remainingTime1 - (minutes * 60);
                remainingTime2 = minutes + "min " + seconds + "s";

            } else if (remainingTime1 < 60) {
                seconds = remainingTime1;
                remainingTime2 = seconds + "s";
            }

            return remainingTime2 + " remaining";
        } else
            return "error";
    }
}
