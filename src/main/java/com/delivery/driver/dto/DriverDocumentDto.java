package com.delivery.driver.dto;

import com.delivery.driver.domain.DriverDocument;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class DriverDocumentDto {
    private UUID id;
    private UUID driverId;
    private String type;
    private String fileUrl;
    private String status;
    private LocalDateTime uploadedAt;

    public static DriverDocumentDto from(DriverDocument d) {
        return DriverDocumentDto.builder()
                .id(d.getId()).driverId(d.getDriverId())
                .type(d.getType()).fileUrl(d.getFileUrl())
                .status(d.getStatus()).uploadedAt(d.getUploadedAt())
                .build();
    }
}
