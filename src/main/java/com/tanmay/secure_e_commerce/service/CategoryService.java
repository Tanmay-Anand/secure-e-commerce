package com.tanmay.secure_e_commerce.service;

import com.tanmay.secure_e_commerce.dto.CategoryDTO;
import com.tanmay.secure_e_commerce.entity.Category;
import com.tanmay.secure_e_commerce.entity.User;
import com.tanmay.secure_e_commerce.enums.Role;
import com.tanmay.secure_e_commerce.exception.ForbiddenException;
import com.tanmay.secure_e_commerce.exception.ResourceNotFoundException;
import com.tanmay.secure_e_commerce.repository.CategoryRepository;
import com.tanmay.secure_e_commerce.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private void validateAdminRole() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDetailsService.getUserByUsername(username);

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only ADMIN can perform this operation");
        }
    }

    //Create
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        validateAdminRole();

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    //Update
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        validateAdminRole();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    //Delete
    @Transactional
    public void deleteCategory(Long id) {
        validateAdminRole();

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.delete(category);
    }

    //Read
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //Read
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return convertToDTO(category);
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}