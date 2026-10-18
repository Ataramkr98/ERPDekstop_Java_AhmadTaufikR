package com.erp;
import java.util.Map;
import io.quarkus.test.junit.QuarkusTestProfile;
/** Database kind is build-time configuration and must precede augmentation. */
public class BackendProfile implements QuarkusTestProfile {
    @Override public Map<String,String> getConfigOverrides() {
        return Map.of("quarkus.datasource.db-kind","h2", "quarkus.datasource.jdbc.url","jdbc:h2:mem:erp_tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE",
                "quarkus.datasource.username","sa", "quarkus.datasource.password","", "quarkus.datasource.devservices.enabled","false");
    }
}
