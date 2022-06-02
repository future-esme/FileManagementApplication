package com.esempla.file.controller;

import com.esempla.file.domain.Document;
import com.esempla.file.domain.User;
import com.esempla.file.repository.DocumentRepository;
import com.esempla.file.repository.UserRepository;
import com.esempla.file.service.MinioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {
    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final MinioService minioService;

    public UserController(UserRepository userRepository, DocumentRepository documentRepository, MinioService minioService) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.minioService = minioService;
    }


    @GetMapping("/users")
    public List<User> getAllUsers() {
        log.debug("REST request to GET all users");
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public Optional<User> getUsers(@PathVariable Long id) {
        log.debug("REST request to get User : {}", id);
        Optional<User> user = userRepository.findById(id);
        return user;
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.debug("REST request to DELETE a user and its informations");
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "entity not found"
            );
        }
        User user = userRepository.findById(id).get();
        List<Document> documentList = new ArrayList<>(user.getUserDocuments());
        Iterator<Document> iterator = documentList.iterator();
        while (iterator.hasNext()) {
            var document = iterator.next();
            if (minioService.deleteFile(document.getUuidDoc(), document.getPath())) {
                user.getUserDocuments().remove(document);
                documentRepository.deleteById(document.getId());
            } else {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Unsuccessful deletion!"
                );
            }
        }
        userRepository.deleteById(id);
    }

}
