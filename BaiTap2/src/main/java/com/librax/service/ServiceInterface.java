package com.librax.service;

public interface ServiceInterface {
    String getServiceName();
    void handle(String operation, String payload);
}
