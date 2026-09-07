package com.librax.service;

import java.util.logging.Logger;

public class PaymentService implements ServiceInterface {

    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    @Override
    public String getServiceName() {
        return "PaymentService";
    }

    @Override
    public void handle(String operation, String payload) {
        logger.info(String.format("[PaymentService] Processing operation '%s' with payload: %s", operation, payload));
    }
}
