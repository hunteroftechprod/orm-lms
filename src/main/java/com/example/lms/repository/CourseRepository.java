package com.example.lms.repository;

import com.example.lms.model.Course;
import com.example.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("select distinct c from Course c left join fetch c.tags order by c.id")
    List<Course> findAllWithTags();

    boolean existsByTitle(String title);
    List<Course> findByTeacher(User teacher);
    List<Course> findByCategory_Name(String name);
}


