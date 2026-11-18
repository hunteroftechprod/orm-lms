package com.example.lms.web;

import com.example.lms.model.Category;
import com.example.lms.model.Tag;
import com.example.lms.repository.CategoryRepository;
import com.example.lms.repository.TagRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final CategoryRepository categories;
    private final TagRepository tags;

    public CatalogController(CategoryRepository categories, TagRepository tags) {
        this.categories = categories;
        this.tags = tags;
    }

    @GetMapping("/categories")
    public List<Entry> categories() {
        return categories.findAll().stream().map(category -> new Entry(category.getId(), category.getName())).toList();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Entry addCategory(@Valid @RequestBody NameRequest request) {
        Category category = new Category();
        category.setName(request.name());
        return new Entry(categories.save(category).getId(), category.getName());
    }

    @GetMapping("/tags")
    public List<Entry> tags() {
        return tags.findAll().stream().map(tag -> new Entry(tag.getId(), tag.getName())).toList();
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public Entry addTag(@Valid @RequestBody NameRequest request) {
        Tag tag = new Tag();
        tag.setName(request.name());
        return new Entry(tags.save(tag).getId(), tag.getName());
    }

    public record NameRequest(@NotBlank @Size(max = 255) String name) {}
    public record Entry(Long id, String name) {}
}
