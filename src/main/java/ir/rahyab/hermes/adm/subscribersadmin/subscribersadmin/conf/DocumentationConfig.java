package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.conf;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The {@code DocumentationConfig} class provides configuration for open api
 *
 * @author tahbaz
 */

@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = "basicAuth"))
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)

@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class DocumentationConfig {

    @Value("${api.info.title}")
    private String title;

    @Value("${api.info.description}")
    private String description;

    @Value("${api.info.version}")
    private String version;

    @Value("${api.info.license.name}")
    private String licenseName;

    @Value("${api.info.license.url}")
    private String licenseUrl;

    @Value("${subscribersAdmin.server.url}")
    private String serverUrl;

    @Bean
    public OpenAPI api() {
        return new OpenAPI().addServersItem(new Server().url(serverUrl)).info(info());

    }

    private Info info() {
        return new Info()
                .title(title)
                .description(description)
                .version(version)
                .license(new License().name(licenseName).url(licenseUrl));
    }


}
