package com.example.ProjetSpringGestionDocuments.Web.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ProjetSpringGestionDocuments.DAO.Entity.Author;
import com.example.ProjetSpringGestionDocuments.DAO.Entity.Category;
import com.example.ProjetSpringGestionDocuments.DAO.Entity.Document;
import com.example.ProjetSpringGestionDocuments.DAO.Repository.AuthorRepository;
import com.example.ProjetSpringGestionDocuments.DAO.Repository.CategoryRepository;
import com.example.ProjetSpringGestionDocuments.DAO.Repository.DocumentRepository;
import com.example.ProjetSpringGestionDocuments.Web.model.DocumentForm;
import com.example.ProjetSpringGestionDocuments.Web.model.FileFormat;
import com.example.ProjetSpringGestionDocuments.Web.model.Language;
import com.example.ProjetSpringGestionDocuments.business.services.AuthorService;
import com.example.ProjetSpringGestionDocuments.business.services.CategoryService;
import com.example.ProjetSpringGestionDocuments.business.services.DocumentService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/documents") 
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private AuthorService authorService;

    @GetMapping("/create")
    public String showAddDocumentPage(Model model) {
        model.addAttribute("documentForm", new DocumentForm());
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "AddDocument";
    }

    @PostMapping("/create")
    public String addDocument(
            @Valid @ModelAttribute DocumentForm documentForm,
            BindingResult bindingResult,
            @RequestParam MultipartFile documentFile,
            RedirectAttributes redirectAttributes) {
                if (bindingResult.hasErrors()) {
                    if (bindingResult.hasFieldErrors("title")) {
                        redirectAttributes.addFlashAttribute("titleError", bindingResult.getFieldError("title").getDefaultMessage());
                    }
                    if (bindingResult.hasFieldErrors("author_id")) {
                        redirectAttributes.addFlashAttribute("authorError", bindingResult.getFieldError("author_id").getDefaultMessage());
                    }
                    if (bindingResult.hasFieldErrors("category_id")) {
                        redirectAttributes.addFlashAttribute("categoryError", bindingResult.getFieldError("category_id").getDefaultMessage());
                    }
                    if (bindingResult.hasFieldErrors("theme")) {
                        redirectAttributes.addFlashAttribute("themeError", bindingResult.getFieldError("theme").getDefaultMessage());
                    }
                    if (bindingResult.hasFieldErrors("fileFormat")) {
                        redirectAttributes.addFlashAttribute("fileFormatError", bindingResult.getFieldError("fileFormat").getDefaultMessage());
                    }
                    if (bindingResult.hasFieldErrors("publishDate")) {
                        redirectAttributes.addFlashAttribute("publishDateError", bindingResult.getFieldError("publishDate").getDefaultMessage());
                    }
                    if (bindingResult.hasFieldErrors("language")) {
                        redirectAttributes.addFlashAttribute("languageError", bindingResult.getFieldError("language").getDefaultMessage());
                    }
                    if (bindingResult.hasFieldErrors("summary")) {
                        redirectAttributes.addFlashAttribute("summaryError", bindingResult.getFieldError("summary").getDefaultMessage());
                    }
                    if (bindingResult.hasFieldErrors("keywords")) {
                        redirectAttributes.addFlashAttribute("keywordsError", bindingResult.getFieldError("keywords").getDefaultMessage());
                    }
                    return "redirect:/documents/create";
                }

        try {
            Author author = authorRepository.findById(documentForm.getAuthor_id())
                    .orElseThrow(() -> new RuntimeException("Auteur introuvable : " + documentForm.getAuthor_id()));
            Category category = categoryRepository.findById(documentForm.getCategory_id())
                    .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + documentForm.getCategory_id()));

            Document document = new Document();
            document.setTitle(documentForm.getTitle());
            document.setAuthor(author);
            document.setCategory(category);
            document.setTheme(documentForm.getTheme());
            document.setLanguage(Language.valueOf(documentForm.getLanguage()));
            document.setFileFormat(FileFormat.valueOf(documentForm.getFileFormat()));
            document.setPublishDate(java.sql.Date.valueOf(documentForm.getPublishDate()));
            document.setFilePath(documentFile.getOriginalFilename());

            documentRepository.save(document);
            redirectAttributes.addFlashAttribute("successMessage", "Document ajouté avec succès !");
            return "redirect:/documents";

        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout du document", e);
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/documents/create";
        }
    }

    @GetMapping
public String listDocuments(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "4") int pageSize,
    @RequestParam(required = false) String searchQuery,
    @RequestParam(required = false) String sortByCategory,
    @RequestParam(required = false) FileFormat sortByFileFormat,
    @RequestParam(required = false, defaultValue = "id") String sortBy,
    Model model) {

    // Préparer la pagination
    PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(sortBy));

    // Logique de recherche et de filtrage
    Page<Document> documentPage;
    if (searchQuery != null && !searchQuery.isEmpty()) {
        // Recherche par titre
        documentPage = documentService.searchDocumentsByTitle(searchQuery, pageRequest);
    } else if (sortByCategory != null && !sortByCategory.isEmpty()) {
        // Trier par catégorie
        documentPage = documentService.getDocumentsSortedByCategory(sortByCategory, pageRequest);
    } else if (sortByFileFormat != null) {
        // Trier par format de fichier
        documentPage = documentService.getDocumentsSortedByFileFormat(sortByFileFormat, pageRequest);
    } else {
        // Récupérer tous les documents
        documentPage = documentService.getAllDocumentPagination(pageRequest);
    }

    // Liste des fichiers (optionnel, peut être conservé si nécessaire)
    List<String> fileNames = new ArrayList<>();
    File directory = new File(uploadDir);
    if (directory.exists() && directory.isDirectory()) {
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                fileNames.add(file.getName());
            }
        }
    }

    // Ajouter les attributs au modèle
    model.addAttribute("documents", documentPage.getContent());
    model.addAttribute("fileNames", fileNames);
    model.addAttribute("pageSize", pageSize);
    model.addAttribute("currentPage", page);
    model.addAttribute("documentCount", documentRepository.count());
    model.addAttribute("totalPages", documentPage.getTotalPages());
    model.addAttribute("searchQuery", searchQuery);
    model.addAttribute("categories", categoryRepository.findAll());

    return "documents";
}



@GetMapping("/edit/{id}")
public String showEditDocumentForm(@PathVariable Long id, Model model) {
    try {
        Document document = documentService.getDocumentById(id);
        if (document == null) {
            model.addAttribute("error", "Document introuvable.");
            return "redirect:/documents";
        }

        DocumentForm documentForm = new DocumentForm();
        documentForm.setId(document.getId()); 
        documentForm.setTitle(document.getTitle());
        documentForm.setSummary(document.getSummary());
        documentForm.setKeywords(document.getKeywords());
        documentForm.setPublishDate(new java.sql.Date(document.getPublishDate().getTime()).toLocalDate());
        documentForm.setAuthor_id(document.getAuthor().getId());
        documentForm.setCategory_id(document.getCategory().getId());
        documentForm.setTheme(document.getTheme());
        documentForm.setLanguage(document.getLanguage().name());
        documentForm.setFileFormat(document.getFileFormat().name());

        model.addAttribute("documentForm", documentForm);
        model.addAttribute("document", document);
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());

        return "editDocument";
    } catch (Exception e) {
        logger.error("Error loading document for editing", e);
        model.addAttribute("error", "Erreur interne : " + e.getMessage());
        return "redirect:/documents";
    }
}

@PostMapping("/edit/{id}")
public String editDocument(
    @PathVariable Long id,
    @Valid @ModelAttribute("documentForm") DocumentForm documentForm,
    BindingResult result,
    @RequestParam("documentFile") MultipartFile documentFile,
    Model model) {
    try {
        if (result.hasErrors()) {
            model.addAttribute("authors", authorRepository.findAll());
            model.addAttribute("categories", categoryRepository.findAll());
            
            return "editDocument";
        }

        Document document = documentService.getDocumentById(id);
        if (document == null) {
            model.addAttribute("error", "Document introuvable.");
            return "redirect:/documents";
        }

        document.setTitle(documentForm.getTitle());
        document.setSummary(documentForm.getSummary());
        document.setKeywords(documentForm.getKeywords());
        document.setPublishDate(java.sql.Date.valueOf(documentForm.getPublishDate()));
        document.setAuthor(authorService.getAuthorById(documentForm.getAuthor_id()));
        document.setCategory(categoryRepository.findById(documentForm.getCategory_id()).orElse(null));
        

        if (documentFile != null && !documentFile.isEmpty()) {
            documentService.updateDocument(id, document, documentFile);
        }
        if (authorService.getAuthorById(documentForm.getAuthor_id()) == null) {
            model.addAttribute("error", "Auteur introuvable.");
            return "editDocument";
        }
        
        if (categoryRepository.findById(documentForm.getCategory_id()).isEmpty()) {
            model.addAttribute("error", "Catégorie introuvable.");
            return "editDocument";
        }
        

        documentRepository.save(document);

        return "redirect:/documents";

    } catch (Exception e) {
        logger.error("Error while updating document", e);
        model.addAttribute("error", "Erreur lors de la mise à jour du document : " + e.getMessage());
        return "editDocument";
    }
}



    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id) {
        documentRepository.deleteById(id);
        return "redirect:/documents";
    }

    @GetMapping("/view/{id}")
    public String viewDocument(@PathVariable Long id, Model model) {
        Document document = documentService.getDocumentById(id);
        model.addAttribute("document", document);

        return "DetailDocumentUser";
    }
}
