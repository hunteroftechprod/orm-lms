package com.example.lms.service;

import com.example.lms.model.Assignment;
import com.example.lms.model.Submission;
import com.example.lms.model.User;
import com.example.lms.model.UserRole;
import com.example.lms.model.Course;
import com.example.lms.model.EnrollmentStatus;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.AssignmentRepository;
import com.example.lms.repository.SubmissionRepository;
import com.example.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    private final EnrollmentRepository enrollmentRepository;

    public SubmissionService(SubmissionRepository submissionRepository, AssignmentRepository assignmentRepository,
                             UserRepository userRepository, EnrollmentRepository enrollmentRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<Submission> findAll() { return submissionRepository.findAll(); }

    public Submission getById(Long id) { return submissionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Submission not found")); }

    public List<Submission> getByAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> new NoSuchElementException("Assignment not found"));
        return submissionRepository.findByAssignment(assignment);
    }

    public List<Submission> getByStudent(Long studentId) {
        User student = userRepository.findById(studentId).orElseThrow(() -> new NoSuchElementException("Student not found"));
        return submissionRepository.findByStudent(student);
    }

    public Submission create(Submission s) {
        Assignment assignment = assignmentRepository.findById(s.getAssignment().getId())
                .orElseThrow(() -> new NoSuchElementException("Assignment not found"));
        Course course = assignment.getLesson().getModule().getCourse();
        if (s.getStudent().getRole() != UserRole.STUDENT
                || enrollmentRepository.findByStudentAndCourse(s.getStudent(), course)
                        .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).isEmpty()) {
            throw new IllegalArgumentException("An active enrollment is required to submit work");
        }
        s.setAssignment(assignment);
        if (submissionRepository.findByStudentAndAssignment(s.getStudent(), s.getAssignment()).isPresent()) {
            throw new IllegalStateException("Student has already submitted this assignment");
        }
        return submissionRepository.save(s);
    }

    public Submission grade(Long id, Integer score, String feedback) {
        Submission s = getById(id);
        if (score == null || score < 0 || score > s.getAssignment().getMaxScore()) {
            throw new IllegalArgumentException("Score must be between 0 and the assignment maximum");
        }
        s.setScore(score);
        s.setFeedback(feedback);
        return submissionRepository.save(s);
    }

    public void delete(Long id) { submissionRepository.deleteById(id); }
}


