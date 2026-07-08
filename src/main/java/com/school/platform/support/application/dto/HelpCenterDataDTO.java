package com.school.platform.support.application.dto;

import java.util.List;

public record HelpCenterDataDTO(
        List<HelpCategoryDTO> categories,
        List<HelpArticleDTO> articles
) {
}
