package com.aidar.model;

import java.time.LocalDate;

/** What the Abstraction sends to any courier (the Implementor's input type). */
public record ShipmentRequest(Parcel parcel, ServiceLevel level, LocalDate pickupDate) { }
