package com.br.infnet.pb_barpesujo;

import com.br.infnet.pb_barpesujo.shared.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PbBarPeSujoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void corsPermiteFrontendEmLocalhostNumerico() {
        TestCorsRegistry registry = new TestCorsRegistry();

        new CorsConfig().addCorsMappings(registry);

        CorsConfiguration corsConfiguration = registry.configurations().get("/api/**");
        assertThat(corsConfiguration.checkOrigin("http://127.0.0.1:5173"))
                .isEqualTo("http://127.0.0.1:5173");
    }

    private static class TestCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }
}
