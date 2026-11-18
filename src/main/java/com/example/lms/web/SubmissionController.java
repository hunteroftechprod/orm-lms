package com.example.lms.web;

import com.example.lms.config.StudentAccess;
import com.example.lms.dto.SubmissionDto;
import com.example.lms.dto.SubmissionRequest;
import com.example.lms.mapper.SubmissionMapper;
import com.example.lms.model.Assignment;
import com.example.lms.model.Submission;
import com.example.lms.model.User;
import com.example.lms.repository.AssignmentRepository;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.lms.dto.GradeRequest;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final StudentAccess studentAccess;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public SubmissionController(SubmissionService submissionService, AssignmentRepository assignmentRepository, UserRepository userRepository, StudentAccess studentAccess) {
        this.submissionService = submissionService;
        this.studentAccess = studentAccess;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping(params = {"!assignmentId", "!studentId"})
    public List<SubmissionDto> findAll(Authentication auth) {
        studentAccess.requireAccess(auth, null);
        return submissionService.findAll().stream().map(SubmissionMapper::toDto).collect(Collectors.toList()); }

    @GetMapping("/{id}")
    public SubmissionDto getById(@PathVariable Long id, Authentication auth) {
        SubmissionDto result = SubmissionMapper.toDto(submissionService.getById(id));
        studentAccess.requireAccess(auth, result.getStudentId());
        return result;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDto create(@Valid @RequestBody SubmissionRequest request, Authentication auth) {
        studentAccess.requireAccess(auth, request.getStudentId());
        Assignment assignment = assignmentRepository.findById(request.getAssignmentId()).orElseThrow();
        User student = userRepository.findById(request.getStudentId()).orElseThrow();
        Submission created = submissionService.create(SubmissionMapper.fromRequest(request, assignment, student));
        return SubmissionMapper.toDto(created);
    }

    @GetMapping(params = {"assignmentId", "!studentId"})
    public List<SubmissionDto> byAssignment(@RequestParam Long assignmentId, Authentication auth) {
        studentAccess.requireAccess(auth, null);
        return submissionService.getByAssignment(assignmentId).stream().map(SubmissionMapper::toDto).toList();
    }

    @GetMapping(params = {"studentId", "!assignmentId"})
    public List<SubmissionDto> byStudent(@RequestParam Long studentId, Authentication auth) {
        studentAccess.requireAccess(auth, studentId);
        return submissionService.getByStudent(studentId).stream().map(SubmissionMapper::toDto).toList();
    }

    @PatchMapping("/{id}/grade")
    public SubmissionDto grade(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return SubmissionMapper.toDto(submissionService.grade(id, request.score(), request.feedback()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { submissionService.delete(id); }
}


