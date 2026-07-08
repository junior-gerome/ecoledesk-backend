package com.school.platform.support.application.dto;

import java.util.List;

public record HelpArticleDTO(
        long id,
        String title,
        String content,
        String category,
        List<String> tags
) {
}
