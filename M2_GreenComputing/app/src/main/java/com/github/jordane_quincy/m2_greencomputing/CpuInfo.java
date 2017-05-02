package com.github.jordane_quincy.m2_greencomputing;

/**
 * Created by jordane on 02/05/17.
 */

public class CpuInfo {
    int min_freq;
    int max_freq;
    int cur_freq;


    public CpuInfo(int min_freq, int max_freq, int cur_freq) {
        this.min_freq = min_freq;
        this.max_freq = max_freq;
        this.cur_freq = cur_freq;
    }
}
