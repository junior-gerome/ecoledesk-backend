package com.school.platform.shared.web.compat;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> content;           // La liste des objets (par exemple, les étudiants de la page)
    private int pageNo;                // Le numéro de la page actuelle (commence à 0)
    private int pageSize;              // Le nombre d’éléments par page
    private long totalElements;        // Le nombre total d’éléments (toutes pages confondues)
    private int totalPages;            // Le nombre total de pages
    private boolean last;              // True si c’est la dernière page
    
    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.pageNo = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }
}