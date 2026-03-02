package ar.uba.fi.ingsoft1.product_example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springdoc.core.customizers.OpenApiCustomizer;

import static ar.uba.fi.ingsoft1.product_example.config.security.SecurityConfig.PUBLIC_ENDPOINTS;
import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigurationTest {

    private OpenApiConfiguration configuration;
    private OpenApiCustomizer customizer;

    @BeforeEach
    void setUp() {
        configuration = new OpenApiConfiguration();
        customizer = configuration.customerGlobalHeaderOpenApiCustomizer();
    }

    @Test
    void customerGlobalHeaderOpenApiCustomizerReturnsCustomizer() {
        assertNotNull(customizer);
    }

    @Test
    void customizerAddsSecurityToNonPublicEndpoints() {
        OpenAPI openApi = createOpenAPIWithPath("/products/1", false);
        customizer.customise(openApi);

        Operation operation = openApi.getPaths().get("/products/1").getGet();
        assertNotNull(operation.getSecurity());
        assertFalse(operation.getSecurity().isEmpty());
    }

    @Test
    void customizerHandlesPublicEndpoints() {
        OpenAPI openApi = createOpenAPIWithPath(PUBLIC_ENDPOINTS[0], true);
        customizer.customise(openApi);

        Operation operation = openApi.getPaths().get(PUBLIC_ENDPOINTS[0]).getPost();
        ApiResponses responses = operation.getResponses();
        assertNull(responses.get("403"));
        assertTrue(operation.getSecurity() == null || operation.getSecurity().isEmpty());
    }

    @Test
    void customizerReplacesFilterWithComponentSearchDTO() {
        OpenAPI openApi = createOpenAPIWithPath("/products", false);
        Operation operation = openApi.getPaths().get("/products").getGet();

        Parameter filterParam = new Parameter().name("filter").in("query");
        operation.addParametersItem(filterParam);

        customizer.customise(openApi);

        Parameter newFilterParam = operation.getParameters().stream()
                .filter(p -> "filter".equals(p.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(newFilterParam);
        assertEquals("query", newFilterParam.getIn());
        assertFalse(newFilterParam.getRequired());
        assertNotNull(newFilterParam.getSchema());
        assertEquals("#/components/schemas/ComponentSearchDTO", newFilterParam.getSchema().get$ref());
    }

    @ParameterizedTest
    @ValueSource(strings = { "sort" })
    void customizerRemovesParameter(String paramName) {
        OpenAPI openApi = createOpenAPIWithPath("/products", false);
        Operation operation = openApi.getPaths().get("/products").getGet();

        Parameter param = new Parameter().name(paramName).in("query");
        operation.addParametersItem(param);

        customizer.customise(openApi);

        assertTrue(operation.getParameters().stream()
                .noneMatch(p -> paramName.equals(p.getName())));
    }

    @Test
    void customizerHandlesOperationsWithoutParameters() {
        OpenAPI openApi = createOpenAPIWithPath("/products/1", false);
        Operation operation = openApi.getPaths().get("/products/1").getGet();
        operation.setParameters(null);

        customizer.customise(openApi);

        assertNull(operation.getParameters());
    }

    @Test
    void customizerCollectsAndSetsTags() {
        OpenAPI openApi = new OpenAPI();
        Paths paths = new Paths();

        PathItem path1 = new PathItem();
        Operation op1 = new Operation();
        op1.addTagsItem("Products");
        path1.setGet(op1);
        paths.put("/products", path1);

        PathItem path2 = new PathItem();
        Operation op2 = new Operation();
        op2.addTagsItem("Orders");
        path2.setPost(op2);
        paths.put("/orders", path2);

        openApi.setPaths(paths);

        customizer.customise(openApi);

        assertNotNull(openApi.getTags());
        assertEquals(2, openApi.getTags().size());
        assertTrue(openApi.getTags().stream().anyMatch(t -> "Orders".equals(t.getName())));
        assertTrue(openApi.getTags().stream().anyMatch(t -> "Products".equals(t.getName())));
    }

    @Test
    void customizerSortsTags() {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();

        PathItem path1 = new PathItem();
        Operation op1 = new Operation();
        op1.addTagsItem("Zebra");
        path1.setGet(op1);
        paths.put("/zebra", path1);

        PathItem path2 = new PathItem();
        Operation op2 = new Operation();
        op2.addTagsItem("Apple");
        path2.setPost(op2);
        paths.put("/apple", path2);

        openAPI.setPaths(paths);

        customizer.customise(openAPI);

        assertNotNull(openAPI.getTags());
        assertEquals(2, openAPI.getTags().size());
        assertEquals("Apple", openAPI.getTags().get(0).getName());
        assertEquals("Zebra", openAPI.getTags().get(1).getName());
    }

    @Test
    void customizerHandlesMultipleOperationsOnSamePath() {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();

        PathItem path = new PathItem();
        Operation getOp = new Operation();
        getOp.addTagsItem("Products");
        path.setGet(getOp);

        Operation postOp = new Operation();
        postOp.addTagsItem("Products");
        path.setPost(postOp);

        paths.put("/products", path);
        openAPI.setPaths(paths);

        customizer.customise(openAPI);

        assertNotNull(openAPI.getTags());
        assertEquals(1, openAPI.getTags().size());
    }

    @Test
    void customizerHandlesAllPublicEndpoints() {
        for (String endpoint : PUBLIC_ENDPOINTS) {
            OpenAPI openAPI = createOpenAPIWithPath(endpoint, true);
            customizer.customise(openAPI);

            Operation operation = openAPI.getPaths().get(endpoint).getPost();
            ApiResponses responses = operation.getResponses();
            assertNull(responses.get("403"), "403 should be removed from " + endpoint);
        }
    }

    private OpenAPI createOpenAPIWithPath(String path, boolean isPublic) {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();

        PathItem pathItem = new PathItem();
        Operation operation = new Operation();

        operation.addTagsItem("Test");

        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("200", new ApiResponse().description("Success"));
        if (!isPublic) {
            responses.addApiResponse("403", new ApiResponse().description("Forbidden"));
        }
        operation.setResponses(responses);

        if (isPublic) {
            pathItem.setPost(operation);
        } else {
            pathItem.setGet(operation);
        }

        paths.put(path, pathItem);
        openAPI.setPaths(paths);

        return openAPI;
    }
}
