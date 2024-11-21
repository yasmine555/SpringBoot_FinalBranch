package com.example.ProjetSpringGestionDocuments.DAO.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ProjetSpringGestionDocuments.DAO.models.Document;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findTop10ByOrderByCreationDateDesc();
    List<Document> findByTitleContaining(String title); 
    List<Document> findByAuthorContaining(String author);
    List<Document> findByKeywordsContaining(String keyword);


}