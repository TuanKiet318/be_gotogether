package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.CategoryResponseDto;
import com.vn.gotogether.entity.Category;
import com.vn.gotogether.repository.data.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAllWithParent();
        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getRootCategories() {
        List<Category> rootCategories = categoryRepository.findRootCategories();
        return rootCategories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CategoryResponseDto convertToDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .build();
    }
}