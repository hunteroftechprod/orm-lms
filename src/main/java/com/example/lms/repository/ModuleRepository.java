package com.example.lms.repository;

import com.example.lms.model.Course;
import com.example.lms.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByIdAndCourse_Id(Long id, Long courseId);
    List<Module> findByCourseOrderByOrderIndexAsc(Course course);
}


