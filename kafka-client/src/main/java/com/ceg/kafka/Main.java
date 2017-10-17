package com.ceg.kafka;

import org.apache.kafka.clients.producer.Producer;

import java.io.IOException;

/**
 * Created by Sean on 10/17/2017.
 * The main header for the java class.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Must have either 'publisher' or 'consumer' as argument");
        }
        switch (args[0]) {
            case "publisher":
                Publisher.main(args);
                break;
            case "consumer":
                //Consumer.main(args);
                break;
            default:
                throw new IllegalArgumentException("Don't know how to do " + args[0]);
        }
    }

}
