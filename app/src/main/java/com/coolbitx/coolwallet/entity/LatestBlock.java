package com.coolbitx.coolwallet.entity;

/**
 * Created by ShihYi on 2016/1/29.
 */
public class LatestBlock {
    public int height;
    public int block_index;
    public String hash;
    public int time;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getBlock_index() {
        return block_index;
    }

    public void setBlock_index(int block_index) {
        this.block_index = block_index;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}