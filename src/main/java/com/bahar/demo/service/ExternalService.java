package com.bahar.demo.service;

public interface ExternalService {
    String call();
    String callExternalApi();
    String fallbackMethod(Exception e);

}
