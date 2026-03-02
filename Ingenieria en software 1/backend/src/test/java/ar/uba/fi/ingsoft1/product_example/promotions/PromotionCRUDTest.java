
package ar.uba.fi.ingsoft1.product_example.promotions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PromotionCRUDTest {

    private static final String PROMOTIONS_ENDPOINT = "/promotions";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String DEFAULT_DESCRIPTION = "Description long enough for validation";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String validExpression;
    private String fromDate;
    private String toDate;

    @BeforeEach
    void setup() {
        validExpression = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}";
        LocalDate today = LocalDate.now();
        fromDate = today.minusDays(1).format(DateTimeFormatter.ISO_DATE);
        toDate = today.plusDays(30).format(DateTimeFormatter.ISO_DATE);
    }

    private ObjectNode createPromotionJson(String name, String description, Integer priority) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("name", name);
        json.put("description", description != null ? description : DEFAULT_DESCRIPTION);
        json.put("fromDate", fromDate);
        json.put("toDate", toDate);
        json.put("expression", validExpression);
        if (priority != null) {
            json.put("priority", priority);
        }
        return json;
    }

    private long createPromotionAndGetId(String name, String description, Integer priority) throws Exception {
        ObjectNode json = createPromotionJson(name, description, priority);
        var result = mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(json)))
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        var responseJson = objectMapper.readTree(responseBody);
        return responseJson.get("id").asLong();
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreateModifyAndDeletePromotion() throws Exception {
        ObjectNode createJson = createPromotionJson("Test Promotion",
                "A test promotion description that is long enough", null);
        createJson.put("base64Image", "");
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        var createResult = mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Promotion"))
                .andExpect(jsonPath("$.description").value("A test promotion description that is long enough"))
                .andExpect(jsonPath("$.fromDate").value(fromDate))
                .andExpect(jsonPath("$.toDate").value(toDate))
                .andExpect(jsonPath("$.expression").value(validExpression))
                .andExpect(jsonPath("$.priority").exists())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        var responseJson = objectMapper.readTree(responseBody);
        long promotionId = responseJson.get("id").asLong();

        String newFromDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String newToDate = LocalDate.now().plusDays(60).format(DateTimeFormatter.ISO_DATE);
        String updatePromotionJson = String.format("""
                {
                    "name": "Test Promotion Modified",
                    "description": "Updated description of promotion",
                    "fromDate": "%s",
                    "toDate": "%s"
                }
                """, newFromDate, newToDate);

        mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePromotionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Promotion Modified"))
                .andExpect(jsonPath("$.description").value("Updated description of promotion"))
                .andExpect(jsonPath("$.fromDate").value(newFromDate))
                .andExpect(jsonPath("$.toDate").value(newToDate));

        mockMvc.perform(get(PROMOTIONS_ENDPOINT + "/" + promotionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(promotionId))
                .andExpect(jsonPath("$.name").value("Test Promotion Modified"));

        mockMvc.perform(delete(PROMOTIONS_ENDPOINT + "/" + promotionId))
                .andExpect(status().isOk());

        mockMvc.perform(get(PROMOTIONS_ENDPOINT + "/" + promotionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdateNonExistentPromotion() throws Exception {
        String updatePromotionJson = """
                {
                    "name": "non-existent Promotion",
                    "description": "This promotion does not exist"
                }
                """;

        mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePromotionJson))
                .andExpect(status().isNotModified());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testDeleteNonExistentPromotion() throws Exception {
        mockMvc.perform(delete(PROMOTIONS_ENDPOINT + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testGetNonExistentPromotion() throws Exception {
        mockMvc.perform(get(PROMOTIONS_ENDPOINT + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithPriority() throws Exception {
        ObjectNode createJson = createPromotionJson("Priority Promotion",
                "A promotion with priority that has enough characters", 1);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Priority Promotion"))
                .andExpect(jsonPath("$.priority").value(1));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testGetActivePromotions() throws Exception {
        long promotionId = createPromotionAndGetId("Active Promotion",
                "An active promotion with sufficient description length", null);

        mockMvc.perform(get(PROMOTIONS_ENDPOINT + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == " + promotionId + ")]").exists());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testGetAllPromotions() throws Exception {
        long promotionId = createPromotionAndGetId("List Promotion",
                "A promotion for listing with enough description text", null);

        mockMvc.perform(get(PROMOTIONS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.id == " + promotionId + ")]").exists());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePromotionPartialFields() throws Exception {
        long promotionId = createPromotionAndGetId("Partial Update Test",
                "Original description that is long enough to pass validation", null);

        String updatePromotionJson = """
                {
                    "name": "Updated Name Only"
                }
                """;

        mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePromotionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name Only"))
                .andExpect(
                        jsonPath("$.description").value("Original description that is long enough to pass validation"));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePriorities() throws Exception {
        long id1 = createPromotionAndGetId("Promotion 1", "First promotion with sufficient description", null);
        long id2 = createPromotionAndGetId("Promotion 2", "Second promotion with sufficient description", null);

        String updatePrioritiesJson = String.format("""
                {
                    "orderedPromotionIds": [%d, %d]
                }
                """, id2, id1);

        mockMvc.perform(put(PROMOTIONS_ENDPOINT + "/priorities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePrioritiesJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(id2))
                .andExpect(jsonPath("$[1].id").value(id1));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithInvalidDates() throws Exception {
        ObjectNode createJson = objectMapper.createObjectNode();
        createJson.put("name", "Invalid Dates");
        createJson.put("description", "Promotion with invalid date range description");
        createJson.put("fromDate", toDate);
        createJson.put("toDate", fromDate);
        createJson.put("expression", validExpression);
        String invalidPromotionJson = objectMapper.writeValueAsString(createJson);

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidPromotionJson))
                    .andReturn();
        });
        String exceptionMessage = exception.getCause() != null
                ? exception.getCause().getMessage()
                : exception.getMessage();
        assertTrue(exceptionMessage.contains("fromDate must be before toDate"),
                "El mensaje de excepción debería indicar que fromDate debe ser anterior a toDate");
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePromotionWithInvalidDateRange() throws Exception {
        long promotionId = createPromotionAndGetId("Date Range Test", "Test date validation with enough characters",
                null);

        String invalidUpdateJson = String.format("""
                {
                    "fromDate": "%s",
                    "toDate": "%s"
                }
                """, toDate, fromDate);

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidUpdateJson))
                    .andReturn();
        });

        String exceptionMessage = exception.getCause() != null
                ? exception.getCause().getMessage()
                : exception.getMessage();
        assertTrue(exceptionMessage.contains("fromDate must be before toDate"),
                "El mensaje de excepción debería indicar que fromDate debe ser anterior a toDate");
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testGetPromotionByName() throws Exception {
        createPromotionAndGetId("Unique Name Promotion", "A promotion with a unique name for testing", null);

        mockMvc.perform(get(PROMOTIONS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.name == 'Unique Name Promotion')]").exists());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testGetPromotionsWithPagination() throws Exception {
        createPromotionAndGetId("Pagination Test", "Test promotion for pagination", null);

        mockMvc.perform(get(PROMOTIONS_ENDPOINT + "?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithoutPriority() throws Exception {
        ObjectNode createJson = createPromotionJson("No Priority Promotion",
                "A promotion created without specifying priority", null);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").exists())
                .andExpect(jsonPath("$.priority").isNumber());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePromotionOnlyFromDate() throws Exception {
        long promotionId = createPromotionAndGetId("Update FromDate Test", "Test updating only fromDate", null);

        String newFromDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String updateJson = String.format("""
                {
                    "fromDate": "%s"
                }
                """, newFromDate);

        mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromDate").value(newFromDate));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePromotionOnlyToDate() throws Exception {
        long promotionId = createPromotionAndGetId("Update ToDate Test", "Test updating only toDate", null);

        String newToDate = LocalDate.now().plusDays(60).format(DateTimeFormatter.ISO_DATE);
        String updateJson = String.format("""
                {
                    "toDate": "%s"
                }
                """, newToDate);

        mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toDate").value(newToDate));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePromotionWithInvalidDateFormat() throws Exception {
        long promotionId = createPromotionAndGetId("Invalid Format Test", "Test with invalid date format", null);

        String invalidUpdateJson = """
                {
                    "fromDate": "invalid-date-format"
                }
                """;

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidUpdateJson))
                    .andReturn();
        });

        String exceptionMessage = exception.getCause() != null
                ? exception.getCause().getMessage()
                : exception.getMessage();
        assertTrue(exceptionMessage.contains("Formato de fecha inválido"),
                "El mensaje de excepción debería indicar que el formato de fecha es inválido");
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePromotionExpression() throws Exception {
        long promotionId = createPromotionAndGetId("Expression Update Test", "Test updating expression field", null);

        String newExpression = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":2000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":15}}";
        ObjectNode updateJson = objectMapper.createObjectNode();
        updateJson.put("expression", newExpression);
        String updatePromotionJson = objectMapper.writeValueAsString(updateJson);

        mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePromotionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expression").value(newExpression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePromotionBase64Image() throws Exception {
        long promotionId = createPromotionAndGetId("Image Update Test", "Test updating base64Image field", null);

        String newImage = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        ObjectNode updateJson = objectMapper.createObjectNode();
        updateJson.put("base64Image", newImage);
        String updatePromotionJson = objectMapper.writeValueAsString(updateJson);

        mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePromotionJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base64Image").value(newImage));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePrioritiesWithEmptyList() throws Exception {
        ObjectNode updatePrioritiesJson = objectMapper.createObjectNode();
        updatePrioritiesJson.putArray("orderedPromotionIds");
        String updatePrioritiesRequest = objectMapper.writeValueAsString(updatePrioritiesJson);

        mockMvc.perform(put(PROMOTIONS_ENDPOINT + "/priorities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePrioritiesRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePrioritiesWithDuplicateIds() throws Exception {
        long promotionId = createPromotionAndGetId("Duplicate Test", "Test for duplicate IDs in priorities", null);

        ObjectNode updatePrioritiesJson = objectMapper.createObjectNode();
        var idsArray = updatePrioritiesJson.putArray("orderedPromotionIds");
        idsArray.add(promotionId);
        idsArray.add(promotionId);
        String updatePrioritiesRequest = objectMapper.writeValueAsString(updatePrioritiesJson);

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(put(PROMOTIONS_ENDPOINT + "/priorities")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updatePrioritiesRequest))
                    .andReturn();
        });

        String exceptionMessage = exception.getCause() != null
                ? exception.getCause().getMessage()
                : exception.getMessage();
        assertTrue(exceptionMessage.contains("no pueden repetirse"),
                "El mensaje de excepción debería indicar que los IDs no pueden repetirse");
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePrioritiesWithNonExistentIds() throws Exception {
        ObjectNode updatePrioritiesJson = objectMapper.createObjectNode();
        var idsArray = updatePrioritiesJson.putArray("orderedPromotionIds");
        idsArray.add(99999L);
        String updatePrioritiesRequest = objectMapper.writeValueAsString(updatePrioritiesJson);

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(put(PROMOTIONS_ENDPOINT + "/priorities")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updatePrioritiesRequest))
                    .andReturn();
        });

        assertTrue(exception.getCause().getMessage().contains("inexistentes"));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePrioritiesWithPromotionsNotInList() throws Exception {
        long id1 = createPromotionAndGetId("Priority Test 1", "First promotion for priority test", null);
        long id2 = createPromotionAndGetId("Priority Test 2", "Second promotion for priority test", null);

        ObjectNode updatePrioritiesJson = objectMapper.createObjectNode();
        var idsArray = updatePrioritiesJson.putArray("orderedPromotionIds");
        idsArray.add(id1);
        String updatePrioritiesRequest = objectMapper.writeValueAsString(updatePrioritiesJson);

        mockMvc.perform(put(PROMOTIONS_ENDPOINT + "/priorities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePrioritiesRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(id1))
                .andExpect(jsonPath("$[1].id").value(id2));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testPromotionWithNullPriority() throws Exception {
        long promotionId = createPromotionAndGetId("Null Priority Test", "Test promotion with null priority", null);

        mockMvc.perform(get(PROMOTIONS_ENDPOINT + "/" + promotionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").exists())
                .andExpect(jsonPath("$.priority").isNumber());
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePromotionFromDateAfterExistingToDate() throws Exception {
        long promotionId = createPromotionAndGetId("Date Validation Test", "Test fromDate after existing toDate", null);

        String invalidFromDate = LocalDate.now().plusDays(100).format(DateTimeFormatter.ISO_DATE);
        String updateJson = String.format("""
                {
                    "fromDate": "%s"
                }
                """, invalidFromDate);

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson))
                    .andReturn();
        });

        String exceptionMessage = exception.getCause() != null
                ? exception.getCause().getMessage()
                : exception.getMessage();
        assertTrue(exceptionMessage.contains("fromDate must be before existing toDate"),
                "El mensaje de excepción debería indicar que fromDate debe ser anterior a toDate existente");
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testUpdatePromotionToDateBeforeExistingFromDate() throws Exception {
        long promotionId = createPromotionAndGetId("Date Validation Test 2", "Test toDate before existing fromDate",
                null);

        String invalidToDate = LocalDate.now().minusDays(10).format(DateTimeFormatter.ISO_DATE);
        String updateJson = String.format("""
                {
                    "toDate": "%s"
                }
                """, invalidToDate);

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(patch(PROMOTIONS_ENDPOINT + "/" + promotionId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateJson))
                    .andReturn();
        });

        String exceptionMessage = exception.getCause() != null
                ? exception.getCause().getMessage()
                : exception.getMessage();
        assertTrue(exceptionMessage.contains("toDate must be after existing fromDate"),
                "El mensaje de excepción debería indicar que toDate debe ser posterior a fromDate existente");
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithDayOfWeekExpression() throws Exception {
        String expression = "{\"condition\":{\"type\":\"dayOfWeek\",\"day\":\"MONDAY\"},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":15}}";
        ObjectNode createJson = createPromotionJson("Monday Promotion", "Promotion for Mondays", null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithProductInCartExpression() throws Exception {
        String expression = "{\"condition\":{\"type\":\"productInCart\",\"productId\":1},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":100}}";
        ObjectNode createJson = createPromotionJson("Product In Cart Promotion", "Promotion when product is in cart",
                null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithQuantityExpression() throws Exception {
        String expression = "{\"condition\":{\"type\":\"quantity\",\"productId\":1,\"minQuantity\":3},\"action\":{\"type\":\"quantityDiscount\",\"buyQuantity\":3,\"payQuantity\":2}}";
        ObjectNode createJson = createPromotionJson("Quantity Promotion", "Promotion based on quantity", null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithProductNameExpression() throws Exception {
        String expression = "{\"condition\":{\"type\":\"productName\",\"productName\":\"Pizza\"},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"product\",\"targetItemId\":1,\"percentage\":20}}";
        ObjectNode createJson = createPromotionJson("Product Name Promotion", "Promotion for specific product name",
                null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithProductTypeCategoryExpression() throws Exception {
        String expression = "{\"condition\":{\"type\":\"productType\",\"filterType\":\"category\",\"category\":\"Food\"},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"category\",\"targetCategory\":\"Food\",\"amount\":50}}";
        ObjectNode createJson = createPromotionJson("Category Promotion", "Promotion for category", null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithProductTypeTypeExpression() throws Exception {
        String expression = "{\"condition\":{\"type\":\"productType\",\"filterType\":\"type\",\"productType\":\"Principal\"},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"type\",\"targetProductType\":\"Principal\",\"percentage\":10}}";
        ObjectNode createJson = createPromotionJson("Product Type Promotion", "Promotion for product type", null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithAndExpression() throws Exception {
        String expression = "{\"condition\":{\"type\":\"and\",\"left\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"right\":{\"type\":\"dayOfWeek\",\"day\":\"FRIDAY\"}},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":25}}";
        ObjectNode createJson = createPromotionJson("And Expression Promotion", "Promotion with AND condition", null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithOrExpression() throws Exception {
        String expression = "{\"condition\":{\"type\":\"or\",\"left\":{\"type\":\"productInCart\",\"productId\":1},\"right\":{\"type\":\"productInCart\",\"productId\":2}},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER\",\"amount\":200}}";
        ObjectNode createJson = createPromotionJson("Or Expression Promotion", "Promotion with OR condition", null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithFreeProductAction() throws Exception {
        String expression = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":2000},\"action\":{\"type\":\"freeProduct\",\"targetType\":\"ORDER\",\"productId\":1,\"quantity\":2}}";
        ObjectNode createJson = createPromotionJson("Free Product Promotion", "Promotion with free product action",
                null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithTotalAmountOperators() throws Exception {
        String[] operators = { ">", ">=", "<", "<=", "==" };
        for (String operator : operators) {
            String expression = String.format(
                    "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\"%s\",\"value\":1500},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}",
                    operator);
            ObjectNode createJson = createPromotionJson("Operator Test " + operator, "Test with operator " + operator,
                    null);
            createJson.put("expression", expression);
            String createPromotionJson = objectMapper.writeValueAsString(createJson);

            mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createPromotionJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.expression").value(expression));
        }
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithFixedDiscountOrderItem() throws Exception {
        String expression = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"fixedDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"product\",\"targetItemId\":1,\"amount\":100}}";
        ObjectNode createJson = createPromotionJson("Fixed Discount Item Promotion", "Fixed discount for specific item",
                null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithPercentageDiscountOrderItem() throws Exception {
        String expression = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER_ITEM\",\"targetFilterType\":\"category\",\"targetCategory\":\"Food\",\"percentage\":15}}";
        ObjectNode createJson = createPromotionJson("Percentage Discount Item Promotion",
                "Percentage discount for category", null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }

    @Test
    @WithMockUser(roles = ADMIN_ROLE)
    void testCreatePromotionWithComplexNestedExpression() throws Exception {
        String expression = "{\"condition\":{\"type\":\"and\",\"left\":{\"type\":\"dayOfWeek\",\"day\":\"FRIDAY\"},\"right\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":2000}},\"action\":{\"type\":\"freeProduct\",\"targetType\":\"ORDER\",\"productId\":1,\"quantity\":1}}";
        ObjectNode createJson = createPromotionJson("Complex Nested Promotion", "Promotion with nested conditions",
                null);
        createJson.put("expression", expression);
        String createPromotionJson = objectMapper.writeValueAsString(createJson);

        mockMvc.perform(post(PROMOTIONS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPromotionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value(expression));
    }
}
