package com.snscity.egdwlib.cmd;

public enum CmdPriority {
    NONE, // lowest
    TOP, // insert to the front
    EXCLUSIVE // insert to the front and delete others
}  
