package com.example.lms.web;

import com.example.lms.model.Module;

import com.example.lms.model.*;
import com.example.lms.repository.ModuleRepository;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.QuizRepository;
import com.example.lms.repository.UserRepository;
import com.example.lms.support.PostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.jpa.properties.hibernate.default_schema=web_tests"})
@AutoConfigureMockMvc
class QuestionControllerTest extends PostgresContainerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private com.fasterxml.jackson.databind.ObjectMapper json;
    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private QuizRepository quizRepository;

    private Long quizId;

    @BeforeEach
    void setup() {
        User t = new User();
        t.setName("T");
        t.setEmail("tq@example.com");
        t.setRole(UserRole.TEACHER);
        t = userRepository.save(t);

        Course c = new Course();
        c.setTitle("C");
        c.setTeacher(t);
        c = courseRepository.save(c);

        Module m = new Module();
        m.setCourse(c);
        m.setTitle("M");
        m.setOrderIndex(1);
        m = moduleRepository.save(m);

        Quiz q = new Quiz();
        q.setModule(m);
        q.setTitle("Q");
        quizId = quizRepository.save(q).getId();
    }

    @Test
    void createQuestionAndAddOption() throws Exception {
        String qBody = "{\n" +
                "  \"quizId\": " + quizId + ",\n" +
                "  \"text\": \"2+2?\",\n" +
                "  \"type\": \"SINGLE_CHOICE\"\n" +
                "}";

        String response = mockMvc.perform(post("/api/questions").header("Authorization", TEACHER_AUTH).contentType(MediaType.APPLICATION_JSON).content(qBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text", is("2+2?")))
                .andReturn().getResponse().getContentAsString();

        long questionId = json.readTree(response).get("id").asLong();
        mockMvc.perform(post("/api/questions/{id}/options", questionId)
                        .header("Authorization", TEACHER_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"4\",\"correct\":true}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/questions/{id}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options[0].text", is("4")));
    }
}


