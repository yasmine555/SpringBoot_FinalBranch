package com.example.ProjetSpringGestionDocuments.business.services;

import com.example.ProjetSpringGestionDocuments.DAO.models.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {
    List<Document> getAllDocuments();
    Document getDocumentById(Long id);
    void saveDocument(Document document, MultipartFile file) throws IOException;
    void updateDocument(Long id, Document updatedDocument, MultipartFile file) throws IOException;
    void deleteDocument(Long id);
    List<Document> searchDocumentsByTitleOrAuthor(String title, String author);
}