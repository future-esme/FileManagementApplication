package com.esempla.file.repository;

import com.esempla.file.domain.Document;
import com.esempla.file.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUserDocuments(Document document);
    User findUserByLogin(String login);
}
