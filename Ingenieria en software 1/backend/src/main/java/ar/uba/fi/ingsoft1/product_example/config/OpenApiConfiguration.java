package ar.uba.fi.ingsoft1.product_example.config;

import static ar.uba.fi.ingsoft1.product_example.config.ConfigConstants.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

import static ar.uba.fi.ingsoft1.product_example.config.security.SecurityConfig.PUBLIC_ENDPOINTS;

@OpenAPIDefinition(info = @Info(title = "Simple Product App Backend"))
@SecurityScheme(name = BEARER_AUTH_SCHEME_KEY, type = SecuritySchemeType.HTTP, scheme = BEARER_SCHEME, bearerFormat = JWT_BEARER_FORMAT)
@Component
public class OpenApiConfiguration {


    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return openApi -> {
            var tags = new HashSet<String>();

            // Iterate over what spring calls controllers (OpenAPI paths) and paths (OpenAPI
            // operations)
            for (var entry : openApi.getPaths().entrySet()) {
                for (var operation : entry.getValue().readOperations()) {
                    tags.addAll(operation.getTags());
                    if (Arrays.asList(PUBLIC_ENDPOINTS).contains(entry.getKey())) {
                        operation.getResponses().remove(RESPONSE_FORBIDDEN);
                    } else {
                        operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME_KEY));
                    }

                    if (operation.getParameters() != null) {
                        boolean hadFilter = operation.getParameters()
                                .removeIf(param -> param.getName() != null && param.getName().equals(QUERY_PARAM_FILTER));

                        operation.getParameters()
                                .removeIf(param -> param.getName() != null && param.getName().equals(QUERY_PARAM_SORT));

                        if (hadFilter) {
                            var filterParameter = new Parameter()
                                    .name(QUERY_PARAM_FILTER)
                                    .in(QUERY_PARAM_IN)
                                    .required(false)
                                    .schema(new Schema<>().$ref("#/components/schemas/ComponentSearchDTO"));
                            operation.addParametersItem(filterParameter);
                        }
                    }
                }
            }

            openApi.setTags(tags.stream()
                    .sorted()
                    .map(name -> new Tag().name(name))
                    .toList());
        };
    }
}