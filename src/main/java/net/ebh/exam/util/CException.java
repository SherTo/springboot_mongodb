package net.ebh.exam.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by xh on 2016/5/27.
 */
public class CException extends Exception {
    private Logger logger = LoggerFactory.getLogger(CException.class);

    public CException() {
    }

    public CException(String message) {
        super(message);
    }

}
