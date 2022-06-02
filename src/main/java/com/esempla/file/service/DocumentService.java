package com.esempla.file.service;

import com.esempla.file.domain.Document;
import com.esempla.file.repository.DocumentRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class DocumentService {

    private final MinioService minioService;
    private final DocumentRepository documentRepository;

    public DocumentService(MinioService minioService, DocumentRepository documentRepository) {
        this.minioService = minioService;
        this.documentRepository = documentRepository;
    }

    public ByteArrayInputStream getFileById(Long id) {

        byte[] byteFile = null;
        try {
            Document doc = documentRepository.findById(id).get();
            byteFile = IOUtils.toByteArray(minioService.getObject(doc.getUuidDoc(), doc.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(byteFile);
    }
}
