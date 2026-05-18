package com.delivery.driver.service;

import com.delivery.common.exception.ResourceNotFoundException;
import com.delivery.driver.domain.DriverDocument;
import com.delivery.driver.dto.DriverDocumentDto;
import com.delivery.driver.repository.DriverDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverDocumentService {

    private final DriverDocumentRepository documentRepository;

    @Value("${app.uploads.base-path:./uploads}")
    private String basePath;

    @Transactional
    public DriverDocumentDto upload(UUID driverId, String type, MultipartFile file) {
        String ext = getExtension(file);
        String filename = type + "-" + UUID.randomUUID() + ext;
        Path dir = Paths.get(basePath, "documents", driverId.toString());
        String fileUrl;
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(filename), file.getBytes());
            fileUrl = basePath + "/documents/" + driverId + "/" + filename;
        } catch (IOException e) {
            log.warn("[DOCUMENT UPLOAD] failed: {}", e.getMessage());
            fileUrl = "uploads/documents/" + driverId + "/" + filename;
        }

        DriverDocument doc = DriverDocument.builder()
                .driverId(driverId).type(type).fileUrl(fileUrl).status("pending").build();
        return DriverDocumentDto.from(documentRepository.save(doc));
    }

    public List<DriverDocumentDto> getDocuments(UUID driverId) {
        return documentRepository.findAllByDriverId(driverId)
                .stream().map(DriverDocumentDto::from).toList();
    }

    @Transactional
    public DriverDocumentDto updateStatus(UUID documentId, String status) {
        DriverDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        doc.setStatus(status);
        return DriverDocumentDto.from(documentRepository.save(doc));
    }

    private String getExtension(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) return name.substring(name.lastIndexOf('.'));
        return ".bin";
    }
}
