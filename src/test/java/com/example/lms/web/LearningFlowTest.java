package com.example.lms.web;

import com.example.lms.model.User;
import com.example.lms.model.UserRole;
import com.example.lms.repository.UserRepository;
import com.example.lms.support.PostgresContainerTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.default_schema=web_tests")
@AutoConfigureMockMvc
class LearningFlowTest extends PostgresContainerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private UserRepository users;

    @Test
    void enrolsStudentAndCompletesLearningFlow() throws Exception {
        long teacherId = user("flow-teacher@example.com", UserRole.TEACHER);
        long studentId = user("student1@example.com", UserRole.STUDENT);
        long categoryId = create("/api/categories", """
                {"name":"ORM flow test"}
                """, TEACHER_AUTH).get("id").asLong();
        long courseId = create("/api/courses", """
                {"title":"Hibernate in practice","teacherId":%d,"categoryId":%d}
                """.formatted(teacherId, categoryId), null).get("id").asLong();

        String modulePath = "/api/courses/" + courseId + "/modules";
        create(modulePath, """
                {"title":"Transactions","orderIndex":2}
                """, TEACHER_AUTH);
        long moduleId = create(modulePath, """
                {"title":"Entity mapping","orderIndex":1}
                """, TEACHER_AUTH).get("id").asLong();
        long lessonId = create("/api/lessons", """
                {"moduleId":%d,"title":"Associations","scheduledAt":"2026-12-01T15:00:00Z"}
                """.formatted(moduleId), null).get("id").asLong();

        mvc.perform(get("/api/courses/{id}/program", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules[0].title").value("Entity mapping"))
                .andExpect(jsonPath("$.modules[0].lessons[0].scheduledAt").value("2026-12-01T15:00:00Z"));

        String enrollment = """
                {"courseId":%d,"studentId":%d}
                """.formatted(courseId, studentId);
        long enrollmentId = create("/api/enrollments", enrollment, null).get("id").asLong();
        mvc.perform(post("/api/enrollments").contentType(MediaType.APPLICATION_JSON).content(enrollment))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/enrollments").param("courseId", String.valueOf(courseId)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(enrollmentId));

        long assignmentId = create("/api/assignments", """
                {"lessonId":%d,"title":"Create a mapping","maxScore":20}
                """.formatted(lessonId), null).get("id").asLong();
        String learnerAuth = basic("student:test-student");
        long submissionId = create("/api/submissions", """
                {"assignmentId":%d,"studentId":%d,"content":"One-to-many mapping"}
                """.formatted(assignmentId, studentId), learnerAuth).get("id").asLong();

        mvc.perform(patch("/api/submissions/{id}/grade", submissionId)
                        .header("Authorization", basic("teacher:test-teacher"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":18}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.score").value(18));
        mvc.perform(get("/api/submissions/{id}", submissionId).header("Authorization", learnerAuth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.score").value(18));

        long quizId = create("/api/quizzes", """
                {"moduleId":%d,"title":"Associations quiz"}
                """.formatted(moduleId), null).get("id").asLong();
        long questionId = create("/api/questions", """
                {"quizId":%d,"text":"Which side owns the foreign key?","type":"SINGLE_CHOICE"}
                """.formatted(quizId), null).get("id").asLong();
        long optionId = create("/api/questions/" + questionId + "/options", """
                {"text":"The many-to-one side","correct":true}
                """, TEACHER_AUTH).get("id").asLong();
        long attemptId = create("/api/quiz-submissions", """
                {"quizId":%d,"studentId":%d,"answers":{"%d":[%d]}}
                """.formatted(quizId, studentId, questionId, optionId), learnerAuth).get("id").asLong();

        mvc.perform(get("/api/quiz-submissions/{id}", attemptId).header("Authorization", learnerAuth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.score").value(100));
        mvc.perform(delete("/api/enrollments/{id}", enrollmentId)).andExpect(status().isNoContent());
    }

    private long user(String email, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setName(role.name());
        user.setRole(role);
        return users.save(user).getId();
    }

    private JsonNode create(String path, String body, String auth) throws Exception {
        var request = post(path).contentType(MediaType.APPLICATION_JSON).content(body);
        request.header("Authorization", auth == null ? TEACHER_AUTH : auth);
        return json.readTree(mvc.perform(request).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private String basic(String credentials) {
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
