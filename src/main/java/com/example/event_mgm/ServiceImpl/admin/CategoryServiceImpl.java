package com.example.event_mgm.ServiceImpl.admin;

import com.example.event_mgm.Repository.admin.CategoryRepository;
import com.example.event_mgm.dto.request.CreateCategoryRequestDto;
import com.example.event_mgm.dto.request.UpdateCategoryRequestDto;
import com.example.event_mgm.dto.response.CategoryResponseDto;
import com.example.event_mgm.exceptions.DuplicateResourceException;
import com.example.event_mgm.exceptions.ResourceNotFoundException;
import com.example.event_mgm.model.Category;

import com.example.event_mgm.service.admin.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public CategoryResponseDto createCategory(CreateCategoryRequestDto request) {
        log.info("Creating new category: {}", request.getName());

        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .isActive(true)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());

        return mapToResponseDto(savedCategory);
    }

    @Override
    public CategoryResponseDto getCategoryById(Long id) {
        log.info("Fetching category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        return mapToResponseDto(category);
    }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        log.info("Fetching all categories");

        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponseDto updateCategory(Long id, UpdateCategoryRequestDto request) {
        log.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check if new name already exists (excluding current category)
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
            }
            category.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());

        return mapToResponseDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check if category has events
        if (!category.getEvents().isEmpty()) {
            throw new IllegalStateException("Cannot delete category as it has associated events");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }

    @Override
    public void activateCategory(Long id) {
        log.info("Activating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        category.setIsActive(true);
        categoryRepository.save(category);
        log.info("Category activated successfully with ID: {}", id);
    }

    @Override
    public void deactivateCategory(Long id) {
        log.info("Deactivating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Category deactivated successfully with ID: {}", id);
    }

    private CategoryResponseDto mapToResponseDto(Category category) {
//        private Long id;
//        private String name;
//        private String description;
//        private Boolean isActive;
//

//        Category category = Category.builder()
//                .name(request.getName().trim())
//                .description(request.getDescription())
//                .isActive(true)
//                .build();


        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .build();

    }
}