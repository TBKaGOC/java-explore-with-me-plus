package ru.practicum.category.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryServiceImpl implements CategoryService {

    final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryDto addCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new ValidationException("Такая категория событий уже существует");
        }
        var category = categoryRepository.save(CategoryMapper.mapCategoryDto(categoryDto));
        return CategoryMapper.mapCategory(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Указанная категория не найдена " + catId));

        List<Category> categoryList = categoryRepository.findByName(categoryDto.getName());

        if (!categoryList.isEmpty()) {
            if (categoryList.getFirst().getId() != catId) {
                throw new ValidationException(String.format("Нельзя задать имя категории %s поскольку такое имя уже задействовано", categoryDto.getName()));
            }
        }

        category.setName(categoryDto.getName());
        return CategoryMapper.mapCategory(categoryRepository.save(category));
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Указанная категория не найдена " + catId));

        return CategoryMapper.mapCategory(category);
    }

    @Override
    public List<CategoryDto> getAllCategorys(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).getContent()
                .stream().map(CategoryMapper::mapCategory).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Указанная категория не найдена " + catId));
        try {
            categoryRepository.deleteById(catId);
        } catch (Exception e) {
            throw new ValidationException("Невозможно удаление используемой категории события " + e.getMessage());
        }
    }
}
