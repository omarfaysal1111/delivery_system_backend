package com.delivery.driver.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shift_bookings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"driver_id", "shift_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ShiftBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "shift_id", nullable = false)
    private UUID shiftId;

    @Column(updatable = false)
    private LocalDateTime bookedAt;

    @PrePersist
    void onPersist() { bookedAt = LocalDateTime.now(); }
}
