package com.erp.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.time.*;
import com.erp.config.RuntimeConfigSource;
import com.erp.platform.AppUser;
import com.erp.sales.SalCustomer;

class SharedRegressionTest {
    @Test void signingResourcesLoadFromDirectoriesContainingSpaces(@org.junit.jupiter.api.io.TempDir java.nio.file.Path temporary) throws Exception {
        var directory = java.nio.file.Files.createDirectories(temporary.resolve("signing keys with spaces"));
        var publicKey = directory.resolve("public.pem");
        java.nio.file.Files.writeString(publicKey, "public-key-resource-fixture");
        String location = RuntimeConfigSource.keyLocation(publicKey);
        assertFalse(location.contains("%20"));
        assertEquals("public-key-resource-fixture", io.smallrye.jwt.util.ResourceUtils.readResource(location));
    }
    @Test void neonUriPreservesTlsAndBindingWithoutCredentialsInJdbcUrl() {
        var settings = RuntimeConfigSource.databaseProperties("postgresql://owner:p%40ss%2Bword@ep-test-pooler.aws.neon.tech/neondb?sslmode=require&channel_binding=require");
        assertEquals("p@ss+word", settings.get("quarkus.datasource.password"));
        assertEquals("owner", settings.get("quarkus.datasource.username"));
        assertEquals("jdbc:postgresql://ep-test-pooler.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require", settings.get("quarkus.datasource.jdbc.url"));
    }
    @Test void malformedUriDoesNotLeakInput() {
        Exception error=assertThrows(IllegalArgumentException.class, () -> RuntimeConfigSource.databaseProperties("postgresql://secret-password"));
        assertFalse(error.getMessage().contains("secret-password"));
    }
    @Test void csvRoundTripsBomCommasQuotesAndNewlines() {
        String csv=CsvSupport.render(List.of("name","notes"),List.of(List.of("A, B", "First\n\"Second\"")));
        assertEquals(List.of(List.of("name","notes"),List.of("A, B","First\n\"Second\"")), CsvSupport.parse(csv));
    }
    @Test void rejectsUnterminatedCsv() { assertThrows(ApiException.class,()->CsvSupport.parse("name\n\"unfinished")); }
    @Test void spreadsheetFormulasAreNeutralizedButNumbersAreNot() {
        String csv=CsvSupport.render(List.of("label","amount"),List.of(List.of("=1+1",-42)));
        assertTrue(csv.contains("'=1+1,-42"));
    }
    @Test void invalidTypedValuesAreNotSilentlyAccepted() {
        assertNull(CrudService.coerce("perhaps",Boolean.class));
        assertNull(CrudService.coerce("4.2",Long.class));
        assertEquals(Instant.parse("2026-09-14T12:00:00Z"),CrudService.coerce("2026-09-14T12:00:00Z",Instant.class));
    }
    @Test void passwordHashIsAbsentFromMetadataExportAndAudit() {
        AppUser user=new AppUser(); user.username="example"; user.passwordHash="private-hash";
        assertFalse(CsvSupport.exportFields(AppUser.class).stream().anyMatch(f->f.getName().equals("passwordHash")));
        assertFalse(com.erp.platform.AuditService.Snapshot.of(user).contains("private-hash"));
    }
    @Test void requiredSchemaFieldsHaveUsableValidation() {
        SalCustomer customer=new SalCustomer(); customer.code="C1";
        assertTrue(assertThrows(ApiException.class,()->EntityValidation.validate(customer)).getMessage().contains("name"));
        customer.name="Valid customer"; assertDoesNotThrow(()->EntityValidation.validate(customer));
    }
    @Test void tenantOwnershipAndGeneratedRecordGuards() {
        SalCustomer customer=new SalCustomer(); customer.companyId=5L;
        assertDoesNotThrow(()->RecordPolicy.owned(customer,5L));
        assertThrows(ApiException.class,()->RecordPolicy.owned(customer,6L));
        assertThrows(ApiException.class,()->RecordPolicy.owned(customer,null));
        assertTrue(RecordPolicy.readOnly(com.erp.platform.AuditLog.class));
    }
    @Test void demoFallbackNeverActivatesOutsideTheDemoProfile() {
        // Direct PostgreSQL property assembly stays untouched: the local H2 fallback
        // only applies at runtime when the demo profile runs without any URL.
        var settings = RuntimeConfigSource.databaseProperties("postgresql://owner:p%40ss@ep-test.us-east-2.aws.neon.tech/neondb?sslmode=require");
        assertTrue(settings.get("quarkus.datasource.jdbc.url").startsWith("jdbc:postgresql://"));
        assertFalse(settings.containsKey("quarkus.datasource.db-kind"));
    }
    @Test void calculatedBalancesAndReversedDocumentsCannotBeOverwritten() {
        SalCustomer existing=new SalCustomer(); existing.currentBalance=new java.math.BigDecimal("596");
        SalCustomer supplied=new SalCustomer(); supplied.currentBalance=java.math.BigDecimal.ZERO;
        RecordPolicy.preserveCalculated(supplied,existing); assertEquals(existing.currentBalance,supplied.currentBalance);
        RecordPolicy.preserveCalculated(supplied,null); assertEquals(java.math.BigDecimal.ZERO,supplied.currentBalance);
        var invoice=new com.erp.sales.SalInvoice(); invoice.status=com.erp.sales.InvoiceStatus.VOID;
        assertThrows(ApiException.class,()->RecordPolicy.editable(invoice));
        invoice.status=com.erp.sales.InvoiceStatus.DRAFT; invoice.journalEntryId=42L;
        assertThrows(ApiException.class,()->RecordPolicy.editable(invoice));
    }
}
