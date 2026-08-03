package com.example.event_mgm.exceptions;

public class DuplicateResourceException extends RuntimeException{

    public DuplicateResourceException(String message)
    {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }


    //    public InvalidCredentialsException(String message) {
//        super(message);
//    }
//
//    public InvalidCredentialsException(String message, Throwable cause) {
//        super(message, cause);
//    }
}
