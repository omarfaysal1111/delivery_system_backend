package com.delivery.driver.controller;

import com.delivery.driver.domain.Shift;
import com.delivery.driver.dto.DriverDocumentDto;
import com.delivery.driver.dto.ShiftDto;
import com.delivery.driver.service.DriverDocumentService;
import com.delivery.driver.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDriverController {

    private final ShiftService shiftService;
    private final DriverDocumentService driverDocumentService;

    @PostMapping("/shifts")
    public ResponseEntity<ShiftDto> createShift(@RequestBody Map<String, Object> body) {
        Shift shift = Shift.builder()
                .date(LocalDate.parse((String) body.get("date")))
                .startTime(LocalTime.parse((String) body.get("startTime")))
                .endTime(LocalTime.parse((String) body.get("endTime")))
                .maxDrivers((Integer) body.get("maxDrivers"))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftService.createShift(shift));
    }

    @PatchMapping("/documents/{id}/status")
    public ResponseEntity<DriverDocumentDto> updateDocumentStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(driverDocumentService.updateStatus(id, status));
    }
}
