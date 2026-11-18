package com.example.lms.web;

import com.example.lms.dto.CourseDto;
import com.example.lms.dto.CourseProgramDto;
import com.example.lms.dto.CourseProgramDto.ModuleDto;
import com.example.lms.dto.ModuleRequest;
import com.example.lms.dto.CourseRequest;
import com.example.lms.mapper.CourseMapper;
import com.example.lms.model.*;
import com.example.lms.repository.CategoryRepository;
import com.example.lms.repository.TagRepository;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public CourseController(CourseService courseService, UserRepository userRepository, CategoryRepository categoryRepository, TagRepository tagRepository) {
        this.courseService = courseService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @GetMapping
    public List<CourseDto> findAll() { return courseService.listDetails(); }

    @GetMapping("/{id}")
    public CourseDto getById(@PathVariable Long id) { return courseService.details(id); }

    @GetMapping("/{id}/program")
    public CourseProgramDto program(@PathVariable Long id) {
        return courseService.program(id);
    }

    @PostMapping("/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleDto addModule(@PathVariable Long id, @Valid @RequestBody ModuleRequest request) {
        return courseService.addModule(id, request);
    }

    @PutMapping("/{id}/modules/{moduleId}")
    public ModuleDto updateModule(@PathVariable Long id, @PathVariable Long moduleId,
                                  @Valid @RequestBody ModuleRequest request) {
        return courseService.updateModule(id, moduleId, request);
    }

    @DeleteMapping("/{id}/modules/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable Long id, @PathVariable Long moduleId) {
        courseService.deleteModule(id, moduleId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDto create(@Valid @RequestBody CourseRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new NoSuchElementException("Category not found"));
        User teacher = userRepository.findById(request.getTeacherId()).orElseThrow();
        Set<Tag> tags = Optional.ofNullable(request.getTagIds()).orElseGet(Collections::emptySet).stream()
                .map(id -> tagRepository.findById(id).orElseThrow())
                .collect(Collectors.toSet());
        Course created = courseService.create(CourseMapper.fromRequest(request, category, teacher, tags));
        return CourseMapper.toDto(created);
    }

    @PutMapping("/{id}")
    public CourseDto update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new NoSuchElementException("Category not found"));
        User teacher = userRepository.findById(request.getTeacherId()).orElseThrow();
        Set<Tag> tags = Optional.ofNullable(request.getTagIds()).orElseGet(Collections::emptySet).stream()
                .map(tid -> tagRepository.findById(tid).orElseThrow())
                .collect(Collectors.toSet());
        courseService.update(id, CourseMapper.fromRequest(request, category, teacher, tags));
        return courseService.details(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { courseService.delete(id); }
}


