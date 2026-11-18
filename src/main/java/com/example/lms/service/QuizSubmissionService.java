package com.example.lms.service;

import com.example.lms.model.Quiz;
import com.example.lms.model.QuizSubmission;
import com.example.lms.model.User;
import com.example.lms.model.UserRole;
import com.example.lms.model.EnrollmentStatus;
import com.example.lms.repository.EnrollmentRepository;
import com.example.lms.repository.QuizRepository;
import com.example.lms.repository.QuizSubmissionRepository;
import com.example.lms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.NoSuchElementException;

@Service
@Transactional
public class QuizSubmissionService {
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;

    private final EnrollmentRepository enrollmentRepository;

    public QuizSubmissionService(QuizSubmissionRepository quizSubmissionRepository, QuizRepository quizRepository,
                                 UserRepository userRepository, EnrollmentRepository enrollmentRepository) {
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public List<QuizSubmission> findAll() { return quizSubmissionRepository.findAll(); }

    public QuizSubmission getById(Long id) { return quizSubmissionRepository.findById(id).orElseThrow(() -> new NoSuchElementException("QuizSubmission not found")); }

    public List<QuizSubmission> byQuiz(Long quizId) {
        return quizSubmissionRepository.findByQuiz(quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found")));
    }

    public List<QuizSubmission> byStudent(Long studentId) {
        return quizSubmissionRepository.findByStudent(userRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found")));
    }

    public QuizSubmission takeQuiz(Long quizId, Long studentId, Map<Long, Set<Long>> answers) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new NoSuchElementException("Quiz not found"));
        User student = userRepository.findById(studentId).orElseThrow(() -> new NoSuchElementException("Student not found"));
        if (quiz.getModule() == null || student.getRole() != UserRole.STUDENT
                || enrollmentRepository.findByStudentAndCourse(student, quiz.getModule().getCourse())
                        .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).isEmpty()) {
            throw new IllegalArgumentException("An active enrollment is required to take this quiz");
        }

        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setScore(QuizScoring.percentage(quiz.getQuestions(), answers));
        return quizSubmissionRepository.save(submission);
    }

    public void delete(Long id) { quizSubmissionRepository.deleteById(id); }
}


