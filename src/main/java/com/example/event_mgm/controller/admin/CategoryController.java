package com.example.event_mgm.controller.admin;

import com.example.event_mgm.dto.request.CreateCategoryRequestDto;
import com.example.event_mgm.dto.request.UpdateCategoryRequestDto;
import com.example.event_mgm.dto.response.ApiResponse;
import com.example.event_mgm.dto.response.CategoryResponseDto;
import com.example.event_mgm.service.admin.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Category Management", description = "APIs for managing categories")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @Valid @RequestBody CreateCategoryRequestDto request) {
        System.out.println("at category controller");

        log.info("Creating new category: {}", request.getName());

        CategoryResponseDto response = categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(
            @PathVariable Long id) {

        log.info("Fetching category with ID: {}", id);

        CategoryResponseDto response = categoryService.getCategoryById(id);

        return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getAllCategories() {

        log.info("Fetching all categories");
        List<CategoryResponseDto> response = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequestDto request) {

        log.info("Updating category with ID: {}", id);

        CategoryResponseDto response = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id) {

        log.info("Deleting category with ID: {}", id);

        categoryService.deleteCategory(id);

        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a category")
    public ResponseEntity<ApiResponse<Void>> activateCategory(
            @PathVariable Long id) {

        log.info("Activating category with ID: {}", id);

        categoryService.activateCategory(id);

        return ResponseEntity.ok(ApiResponse.success("Category activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a category")
    public ResponseEntity<ApiResponse<Void>> deactivateCategory(
            @PathVariable Long id) {

        log.info("Deactivating category with ID: {}", id);

        categoryService.deactivateCategory(id);

        return ResponseEntity.ok(ApiResponse.success("Category deactivated successfully"));
    }
}