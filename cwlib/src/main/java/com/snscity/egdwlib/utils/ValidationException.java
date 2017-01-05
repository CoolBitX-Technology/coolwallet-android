package com.snscity.egdwlib.utils;

public class ValidationException  extends Exception {

    public ValidationException (Throwable cause)
    {
        super (cause);
    }

    public ValidationException (String message, Throwable cause)
    {
        super (message, cause);
    }

    public ValidationException (String message)
    {
        super (message);
    }

    public void myException(){

    }

}
