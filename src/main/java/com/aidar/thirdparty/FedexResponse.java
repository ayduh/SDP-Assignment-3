package com.aidar.thirdparty;

/** Third-party status object. Failures are signalled by statusCode, not by exceptions. */
public record FedexResponse(int statusCode, String message, String trackingId, int costCents) { }
