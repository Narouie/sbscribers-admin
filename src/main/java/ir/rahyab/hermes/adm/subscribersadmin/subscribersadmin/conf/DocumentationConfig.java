package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.conf;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tahbaz
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class DocumentationConfig {

    @Value("${api.info.title}")
    private String title;

    @Value("${api.info.description}")
    private String description;

    @Value("${api.info.version}")
    private String version;

    @Value("${api.info.contact.name}")
    private String contactName;

    @Value("${api.info.contact.email}")
    private String contactEmail;

    @Value("${api.info.contact.url}")
    private String contactUrl;

    @Value("${api.info.license.name}")
    private String licenseName;

    @Value("${api.info.license.url}")
    private String licenseUrl;

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(info());
    }

    private Info info() {
        return new Info()
                .title(title)
                .description(description)
                .version(version)
                .contact(new Contact().name(contactName).email(contactEmail).url(contactUrl))
                .license(new License().name(licenseName).url(licenseUrl));
    }


}
