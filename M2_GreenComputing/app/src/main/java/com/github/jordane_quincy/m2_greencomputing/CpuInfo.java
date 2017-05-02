package com.github.jordane_quincy.m2_greencomputing;

/**
 * Created by jordane on 02/05/17.
 */

public class CpuInfo {
    private int minFreq;
    private int maxFreq;
    private int curFreq;

    public CpuInfo(int minFreq, int maxFreq, int curFreq) {
        this.minFreq = minFreq;
        this.maxFreq = maxFreq;
        this.curFreq = curFreq;
    }

    public void setMinFreq(int minFreq) {
        this.minFreq = minFreq;
    }

    public void setMaxFreq(int maxFreq) {
        this.maxFreq = maxFreq;
    }

    public void setCurFreq(int curFreq) {
        this.curFreq = curFreq;
    }

    @Override
    public String toString() {
        return "CpuInfo{" +
                "minFreq=" + minFreq +
                ", maxFreq=" + maxFreq +
                ", curFreq=" + curFreq + " (" + getCpuUsage() + "%)" +
                '}';
    }


    private int getCpuUsage() {
        return maxFreq == 0 ? 0 : curFreq * 100 / maxFreq;
    }
}
