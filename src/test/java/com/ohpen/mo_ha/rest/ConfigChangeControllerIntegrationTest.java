package com.ohpen.mo_ha.rest;

import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ohpen.mo_ha.domain.ConfigChangeRepository;
import com.ohpen.mo_ha.domain.ConfigChangeType;
import com.ohpen.mo_ha.domain.Severity;
import com.ohpen.mo_ha.rest.dto.ConfigChangeRequest;
import com.ohpen.mo_ha.service.notification.NotificationService;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigChangeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ConfigChangeRepository repository;

    @BeforeEach
    void clearRepository() {
        // We don't have a transactional DB so we can't do a rollback to clean-up
        // after each test case.
        repository.purge();
    }

    @Test
    void recordsNonCriticalChange() throws Exception {
        ConfigChangeRequest request = new ConfigChangeRequest(
                "feature.flag.enabled",
                ConfigChangeType.UPDATE,
                Severity.NON_CRITICAL,
                "false",
                "true",
                "peter",
                "Enabling for release"
        );

        mockMvc.perform(post("/config-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.configKey").value("feature.flag.enabled"))
                .andExpect(jsonPath("$.type").value("UPDATE"))
                .andExpect(jsonPath("$.severity").value("NON_CRITICAL"))
                .andExpect(jsonPath("$.changedBy").value("peter"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(notificationService);
    }

    @Test
    void criticalChangeTriggersNotification() throws Exception {
        ConfigChangeRequest request = new ConfigChangeRequest(
                "db.password",
                ConfigChangeType.UPDATE,
                Severity.CRITICAL,
                "old",
                "new",
                "peter",
                "Rotation"
        );

        mockMvc.perform(post("/config-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(notificationService, timeout(1000)).notifyCriticalChange(any());
    }

    @Test
    void rejectsMissingRequiredFields() throws Exception {
        String json = """
                {
                  "type": "UPDATE",
                  "severity": "CRITICAL"
                }
                """;

        mockMvc.perform(post("/config-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.configKey").exists())
                .andExpect(jsonPath("$.fieldErrors.changedBy").exists());

        verifyNoInteractions(notificationService);
    }

    @Test
    void rejectsUnknownEnumValue() throws Exception {
        String json = """
                {
                  "configKey": "x",
                  "type": "NOT_A_TYPE",
                  "severity": "CRITICAL",
                  "changedBy": "peter"
                }
                """;

        mockMvc.perform(post("/config-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listReturnsEmptyWhenNoChanges() throws Exception {
        mockMvc.perform(get("/config-changes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void filtersListByType() throws Exception {
        postChange("created-1", ConfigChangeType.CREATE);
        postChange("updated-1", ConfigChangeType.UPDATE);
        postChange("updated-2", ConfigChangeType.UPDATE);

        mockMvc.perform(get("/config-changes").param("type", "UPDATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].configKey", hasItems("updated-1", "updated-2")));
    }

    @Test
    void rejectsInvalidTypeFilter() throws Exception {
        mockMvc.perform(get("/config-changes").param("type", "NOT_A_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdReturnsCreatedChange() throws Exception {
        String id = postChange("feature.x", ConfigChangeType.UPDATE);

        mockMvc.perform(get("/config-changes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.configKey").value("feature.x"))
                .andExpect(jsonPath("$.type").value("UPDATE"))
                .andExpect(jsonPath("$.severity").value("NON_CRITICAL"));
    }

    private String postChange(String configKey, ConfigChangeType type) throws Exception {
        String previousValue = type == ConfigChangeType.CREATE ? null : "old";
        String newValue = type == ConfigChangeType.DELETE ? null : "new";
        ConfigChangeRequest request = new ConfigChangeRequest(
                configKey, type, Severity.NON_CRITICAL, previousValue, newValue, "peter", "test"
        );
        String responseBody = mockMvc.perform(post("/config-changes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("id").asText();
    }
}