package com.example.ProjetSpringGestionDocuments.DAO.Entity;

import java.util.Date;

import com.example.ProjetSpringGestionDocuments.Web.model.FileFormat;
import com.example.ProjetSpringGestionDocuments.Web.model.Language;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "infodocument")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Document_id")
    private Long id;

    @Column(name = "title", length = 550, nullable = false)
    private String title;
    
    @ManyToOne(cascade = CascadeType.ALL) 
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToOne(cascade = CascadeType.ALL) 
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    
    @Column(name = "Theme", length = 250, nullable = false)
    private String theme;

    @Column(name = "language", length = 250, nullable = true)
    @Enumerated(EnumType.STRING)
    private Language language;


    @Column(name = "Summary" , nullable = true)
    private String summary;

    @Column(name = "Publishdate")
    @Temporal(TemporalType.DATE)
    private Date publishDate;


    @Column(name = "File_Format", length = 250, nullable = false)
    @Enumerated(EnumType.STRING)
    private FileFormat fileFormat;

    @Column(name = "file_path", length = 250, nullable = false)
    private String filePath;

    @Column(name = "Keywords", length = 500 , nullable = true )
    private String keywords;

}
