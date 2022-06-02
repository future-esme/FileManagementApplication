package com.esempla.file.controller;

import com.esempla.file.domain.Document;
import com.esempla.file.domain.Role;
import com.esempla.file.domain.response.ResponseMessage;
import com.esempla.file.repository.DocumentRepository;
import com.esempla.file.repository.UserRepository;
import com.esempla.file.security.AuthoritiesConstants;
import com.esempla.file.service.DocumentService;
import com.esempla.file.service.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentController {
    private final DocumentRepository documentRepository;
    private final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;
    private final UserRepository userRepository;
    private final MinioService minioService;

    public DocumentController(DocumentRepository documentRepository, DocumentService documentService, UserRepository userRepository, MinioService minioService) {
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.userRepository = userRepository;
        this.minioService = minioService;
    }

    @GetMapping("/documents")
    public List<Document> getAllUsers() {
        log.debug("REST request to GET all documents");
        return documentRepository.findAll();
    }

    @GetMapping(value = "/file/{id}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var document = documentRepository.findById(id).get();
        var name = authentication.getName();
        if (userRepository.findUserByLogin(name).getUserRoles().contains(new Role(AuthoritiesConstants.ADMIN)) || (userRepository.findUserByUserDocuments(document).equals(userRepository.findUserByLogin(name)))) {
            log.debug("REST request to GET file by id");
            if (documentRepository.findById(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            } else {
                var headers = new HttpHeaders();
                headers.add("Content-Disposition", "inline; filename=" + document.getTitle());
                return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType(document.getMimeType()))
                        .body(new InputStreamResource(documentService.getFileById(id)));
            }
        } else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/myfiles")
    public List<Document> getMyFiles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new ArrayList<>(userRepository.findUserByLogin(authentication.getName()).getUserDocuments());
    }

    @PostMapping(value = "/file/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        var message = "";
        try {
            minioService.uploadFile(file, login);
            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    @DeleteMapping(value = "/file/{id}")
    public void deleteFile(@PathVariable Long id) {
        log.debug("REST request to delete file by id");
        var document = documentRepository.findById(id).get();
        var user = userRepository.findUserByUserDocuments(document);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().contains(AuthoritiesConstants.ADMIN) || (user.equals(userRepository.findUserByLogin(authentication.getName())))) {
            if (!documentRepository.existsById(id)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "entity not found"
                );
            }
            if (minioService.deleteFile(document.getUuidDoc(), document.getPath())) {
                user.getUserDocuments().remove(document);
                documentRepository.deleteById(id);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Unsuccessful deletion!"
                );
            }
        }
    }
}
