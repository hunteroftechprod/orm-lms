package com.example.lms.service;

import com.example.lms.dto.CourseDto;
import com.example.lms.dto.CourseProgramDto;
import com.example.lms.dto.CourseProgramDto.ModuleDto;
import com.example.lms.dto.ModuleRequest;
import com.example.lms.mapper.CourseMapper;
import com.example.lms.mapper.LessonMapper;
import com.example.lms.model.Course;
import com.example.lms.model.Lesson;
import com.example.lms.model.Module;
import com.example.lms.model.User;
import com.example.lms.model.UserRole;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.LessonRepository;
import com.example.lms.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    public CourseService(CourseRepository courseRepository, ModuleRepository moduleRepository, LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
    }

    public List<Course> findAll() { return courseRepository.findAll(); }

    public Course getById(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Course not found"));
    }

    public List<Course> findByTeacher(User teacher) { return courseRepository.findByTeacher(teacher); }

    @Transactional(readOnly = true)
    public List<CourseDto> listDetails() {
        return courseRepository.findAllWithTags().stream().map(CourseMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CourseDto details(Long id) {
        return CourseMapper.toDto(getById(id));
    }

    @Transactional(readOnly = true)
    public CourseProgramDto program(Long id) {
        Course course = getById(id);
        return new CourseProgramDto(course.getId(), course.getTitle(),
                course.getModules().stream().map(this::moduleDto).toList());
    }

    public ModuleDto addModule(Long courseId, ModuleRequest request) {
        Module module = new Module();
        applyModuleRequest(module, request);
        return moduleDto(addModule(courseId, module));
    }

    public ModuleDto updateModule(Long courseId, Long moduleId, ModuleRequest request) {
        Module module = findModule(courseId, moduleId);
        applyModuleRequest(module, request);
        return moduleDto(module);
    }

    public void deleteModule(Long courseId, Long moduleId) {
        moduleRepository.delete(findModule(courseId, moduleId));
    }

    private Module findModule(Long courseId, Long moduleId) {
        return moduleRepository.findByIdAndCourse_Id(moduleId, courseId)
                .orElseThrow(() -> new NoSuchElementException("Module not found in course"));
    }

    private void applyModuleRequest(Module module, ModuleRequest request) {
        module.setTitle(request.title());
        module.setOrderIndex(request.orderIndex());
        module.setDescription(request.description());
    }

    private ModuleDto moduleDto(Module module) {
        return new ModuleDto(module.getId(), module.getTitle(), module.getOrderIndex(),
                module.getDescription(), module.getLessons().stream().map(LessonMapper::toDto).toList());
    }

    public Course create(Course course) {
        requireTeacher(course);
        return courseRepository.save(course);
    }

    private void requireTeacher(Course course) {
        if (course.getTeacher() == null || course.getTeacher().getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("Course instructor must have the TEACHER role");
        }
    }

    public Course update(Long id, Course updated) {
        requireTeacher(updated);
        Course existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDuration(updated.getDuration());
        existing.setStartDate(updated.getStartDate());
        existing.setCategory(updated.getCategory());
        existing.setTeacher(updated.getTeacher());
        existing.setTags(updated.getTags());
        return courseRepository.save(existing);
    }

    public Module addModule(Long courseId, Module module) {
        Course course = getById(courseId);
        module.setCourse(course);
        return moduleRepository.save(module);
    }

    public Lesson addLesson(Long moduleId, Lesson lesson) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new NoSuchElementException("Module not found"));
        lesson.setModule(module);
        return lessonRepository.save(lesson);
    }

    public void delete(Long id) { courseRepository.deleteById(id); }
}


