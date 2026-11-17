package com.example.event_mgm.service.admin;

import com.example.event_mgm.dto.request.CreateCategoryRequestDto;
import com.example.event_mgm.dto.request.UpdateCategoryRequestDto;
import com.example.event_mgm.dto.response.CategoryResponseDto;


import java.util.List;

public interface CategoryService {

    CategoryResponseDto createCategory(CreateCategoryRequestDto request);
    CategoryResponseDto getCategoryById(Long id);
    List<CategoryResponseDto> getAllCategories();
    CategoryResponseDto updateCategory(Long id, UpdateCategoryRequestDto request);
    void deleteCategory(Long id);
    void activateCategory(Long id);
    void deactivateCategory(Long id);


}