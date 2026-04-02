package com.zorvyn.financebackend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.financebackend.model.FinancialRecord;
import com.zorvyn.financebackend.model.User;
import com.zorvyn.financebackend.repository.FinancialRecordRepository;
import com.zorvyn.financebackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialRecordRepository financialRecordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        financialRecordRepository.deleteAll();
        userRepository.deleteAll();

        createUser("Admin Test", "admin@zorvyn.local", "admin123", User.Role.ADMIN);
        createUser("Analyst Test", "analyst@zorvyn.local", "analyst123", User.Role.ANALYST);
        createUser("Viewer Test", "viewer@zorvyn.local", "viewer123", User.Role.VIEWER);
    }

    @Test
    void shouldReturn401WhenNoCredentialsProvided() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminShouldAccessUsersList() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(httpBasic("admin@zorvyn.local", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    void viewerShouldBeForbiddenFromUsersList() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(httpBasic("viewer@zorvyn.local", "viewer123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystShouldCreateRecordAndViewerShouldNotDeleteIt() throws Exception {
        User analyst = userRepository.findByEmail("analyst@zorvyn.local")
            .orElseThrow(() -> new IllegalStateException("Analyst test user missing"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", new BigDecimal("1500.00"));
        payload.put("type", "INCOME");
        payload.put("category", "Salary");
        payload.put("description", "Integration test record");
        payload.put("date", LocalDate.now().toString());
        payload.put("userId", analyst.getId());

        MvcResult createResult = mockMvc.perform(post("/api/records")
                        .with(httpBasic("analyst@zorvyn.local", "analyst123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        JsonNode createdNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long recordId = createdNode.get("id").asLong();

        mockMvc.perform(delete("/api/records/{id}", recordId)
                        .with(httpBasic("viewer@zorvyn.local", "viewer123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRecordPayloadShouldReturn400() throws Exception {
        User analyst = userRepository.findByEmail("analyst@zorvyn.local")
            .orElseThrow(() -> new IllegalStateException("Analyst test user missing"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", new BigDecimal("1500.00"));
        payload.put("type", "EXPENSE");
        payload.put("category", "Utilities");
        payload.put("description", "");
        payload.put("date", LocalDate.now().toString());
        payload.put("userId", analyst.getId());

        mockMvc.perform(post("/api/records")
                        .with(httpBasic("analyst@zorvyn.local", "analyst123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Description is required"));
    }

    @Test
    void missingRecordShouldReturn404ForAuthorizedRole() throws Exception {
        mockMvc.perform(get("/api/records/{id}", 999999L)
                        .with(httpBasic("analyst@zorvyn.local", "analyst123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Record not found with id 999999"));
    }

        @Test
        void shouldIssueTokenAndAllowBearerAccess() throws Exception {
        String token = issueToken("admin@zorvyn.local", "admin123");

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin@zorvyn.local"));
        }

        @Test
        void shouldFilterRecordsByDateCategoryAndType() throws Exception {
        User analyst = userRepository.findByEmail("analyst@zorvyn.local")
            .orElseThrow(() -> new IllegalStateException("Analyst test user missing"));

        LocalDate today = LocalDate.now();
        seedRecord(analyst, "3000.00", FinancialRecord.Type.INCOME, "Salary", "Monthly salary", today);
        seedRecord(analyst, "250.00", FinancialRecord.Type.EXPENSE, "Food", "Groceries", today);
        seedRecord(analyst, "120.00", FinancialRecord.Type.EXPENSE, "Travel", "Cab", today);

        mockMvc.perform(get("/api/records/filter")
                .with(httpBasic("viewer@zorvyn.local", "viewer123"))
                .param("date", today.toString())
                .param("category", "Food")
                .param("type", "EXPENSE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].category").value("Food"))
            .andExpect(jsonPath("$[0].type").value("EXPENSE"));
        }

        @Test
        void dashboardSummaryShouldReturnIncomeExpenseAndTrends() throws Exception {
        User analyst = userRepository.findByEmail("analyst@zorvyn.local")
            .orElseThrow(() -> new IllegalStateException("Analyst test user missing"));

        seedRecord(analyst, "5000.00", FinancialRecord.Type.INCOME, "Salary", "Salary credit", LocalDate.now());
        seedRecord(analyst, "900.00", FinancialRecord.Type.EXPENSE, "Rent", "Rent payment", LocalDate.now());
        seedRecord(analyst, "300.00", FinancialRecord.Type.EXPENSE, "Food", "Food spending", LocalDate.now());

        mockMvc.perform(get("/api/dashboard/summary")
                .with(httpBasic("analyst@zorvyn.local", "analyst123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalIncome").value(5000))
            .andExpect(jsonPath("$.totalExpense").value(1200))
            .andExpect(jsonPath("$.netBalance").value(3800))
            .andExpect(jsonPath("$.categoryTotals").isArray())
            .andExpect(jsonPath("$.recentActivity").isArray())
            .andExpect(jsonPath("$.monthlyTrends").isArray())
            .andExpect(jsonPath("$.weeklyTrends").isArray());
        }

        @Test
        void viewerShouldBeForbiddenFromAnalystInsights() throws Exception {
        mockMvc.perform(get("/api/analyst/insights")
                .with(httpBasic("viewer@zorvyn.local", "viewer123")))
            .andExpect(status().isForbidden());
        }

        @Test
        void analystShouldAccessInsightsEndpoint() throws Exception {
        User analyst = userRepository.findByEmail("analyst@zorvyn.local")
            .orElseThrow(() -> new IllegalStateException("Analyst test user missing"));

        seedRecord(analyst, "4200.00", FinancialRecord.Type.INCOME, "Salary", "Salary", LocalDate.now());
        seedRecord(analyst, "700.00", FinancialRecord.Type.EXPENSE, "Rent", "Rent", LocalDate.now());
        seedRecord(analyst, "350.00", FinancialRecord.Type.EXPENSE, "Food", "Food", LocalDate.now());

        mockMvc.perform(get("/api/analyst/insights")
                .with(httpBasic("analyst@zorvyn.local", "analyst123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.averageIncomeTransaction").exists())
            .andExpect(jsonPath("$.averageExpenseTransaction").exists())
            .andExpect(jsonPath("$.savingsRatePercent").exists())
            .andExpect(jsonPath("$.topExpenseCategories").isArray())
            .andExpect(jsonPath("$.largestExpenses").isArray());
        }

        private String issueToken(String email, String password) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);

        MvcResult tokenResult = mockMvc.perform(post("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

        JsonNode tokenNode = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        return tokenNode.get("token").asText();
        }

        private void seedRecord(User user,
                    String amount,
                    FinancialRecord.Type type,
                    String category,
                    String description,
                    LocalDate date) {
        FinancialRecord record = FinancialRecord.builder()
            .amount(new BigDecimal(amount))
            .type(type)
            .category(category)
            .description(description)
            .date(date)
            .user(user)
            .build();

        financialRecordRepository.save(record);
        }

    private void createUser(String name, String email, String rawPassword, User.Role role) {
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .status(User.Status.ACTIVE)
                .build();
        userRepository.save(user);
    }
}
