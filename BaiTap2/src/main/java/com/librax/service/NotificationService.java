package com.librax.service;

import java.util.logging.Logger;

public class NotificationService implements ServiceInterface {

    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());

    @Override
    public String getServiceName() {
        return "NotificationService";
    }

    @Override
    public void handle(String operation, String payload) {
        logger.info(String.format("[NotificationService] Processing operation '%s' with payload: %s", operation, payload));
        if ("notifyOverdue".equals(operation)) {
            sendOverdueNotification(payload);
        } else {
            logger.warning(String.format("[NotificationService] Unsupported operation: %s", operation));
        }
    }

    private void sendOverdueNotification(String payload) {
        logger.info("[NotificationService] SUCCESS: Notification email/SMS sent to member for overdue book! Payload: " + payload);
    }
}
