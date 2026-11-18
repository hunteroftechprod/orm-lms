package com.example.lms.web;

import com.example.lms.config.StudentAccess;
import com.example.lms.dto.QuizSubmissionDto;
import com.example.lms.dto.QuizSubmissionRequest;
import com.example.lms.mapper.QuizSubmissionMapper;
import com.example.lms.model.QuizSubmission;
import com.example.lms.service.QuizSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz-submissions")
public class QuizSubmissionController {

    private final QuizSubmissionService quizSubmissionService;
    private final StudentAccess studentAccess;
    public QuizSubmissionController(QuizSubmissionService quizSubmissionService, StudentAccess studentAccess) {
        this.quizSubmissionService = quizSubmissionService;
        this.studentAccess = studentAccess;
    }

    @GetMapping(params = {"quizId", "!studentId"})
    public List<QuizSubmissionDto> byQuiz(@RequestParam Long quizId, Authentication auth) {
        studentAccess.requireAccess(auth, null);
        return quizSubmissionService.byQuiz(quizId).stream().map(QuizSubmissionMapper::toDto).toList();
    }

    @GetMapping(params = {"studentId", "!quizId"})
    public List<QuizSubmissionDto> byStudent(@RequestParam Long studentId, Authentication auth) {
        studentAccess.requireAccess(auth, studentId);
        return quizSubmissionService.byStudent(studentId).stream().map(QuizSubmissionMapper::toDto).toList();
    }

    @GetMapping(params = {"!quizId", "!studentId"})
    public List<QuizSubmissionDto> findAll(Authentication auth) {
        studentAccess.requireAccess(auth, null);
        return quizSubmissionService.findAll().stream().map(QuizSubmissionMapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public QuizSubmissionDto getById(@PathVariable Long id, Authentication auth) {
        QuizSubmissionDto result = QuizSubmissionMapper.toDto(quizSubmissionService.getById(id));
        studentAccess.requireAccess(auth, result.getStudentId());
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizSubmissionDto create(@Valid @RequestBody QuizSubmissionRequest request, Authentication auth) {
        studentAccess.requireAccess(auth, request.studentId());
        QuizSubmission created = quizSubmissionService.takeQuiz(request.quizId(), request.studentId(), request.answers());
        return QuizSubmissionMapper.toDto(created);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { quizSubmissionService.delete(id); }
}


