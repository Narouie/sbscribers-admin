package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.dto;

import java.io.Serializable;

/**
 * @author tahbaz
 */

public class RateConfigDto implements Serializable {

     private static final long serialVersionUID = 7139277731040588070L;

     private int second;

     private int minute;

     private int hour;

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }


}
