package com.boha.kasietransie.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerRunner {

    static Logger logger = LoggerFactory.getLogger(ControllerRunner.class.getSimpleName());
    public static void main(String[] args) {
        logger.info(ControllerRunner.class.getSimpleName() + " running!");
    }
}
