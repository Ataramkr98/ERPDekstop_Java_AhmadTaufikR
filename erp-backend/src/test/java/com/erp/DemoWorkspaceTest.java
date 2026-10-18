package com.erp;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.Map;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.erp.platform.Company;
import com.erp.sales.SalCustomer;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Portfolio demo workspace: the flows a reviewer actually touches.
 *
 * <p>These tests are the isolation evidence. They assert that the demo path
 *  - is completely absent unless it was explicitly enabled,
 *  - gets its own tenant, distinct from the company the seeded administrator belongs to,
 *  - cannot read, change or delete a single row belonging to another tenant,
 *  - restores the starting dataset exactly, and
 *  - refuses reset to anyone who is not holding a demo session.
 */
@QuarkusTest
@QuarkusTestResource(BackendTestResource.class)
@io.quarkus.test.junit.TestProfile(DemoProfile.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemoWorkspaceTest extends AbstractHttpTest {

    @jakarta.inject.Inject
    jakarta.persistence.EntityManager em;
    @jakarta.inject.Inject com.erp.platform.PasswordHasher hasher;

    private static String demoToken;
    private static Long demoCompanyId;
    private static String adminToken;
    private static Long adminCompanyId;

    private static String login(String username, String password) {
        return given().contentType(ContentType.JSON).body(Map.of("username", username, "password", password))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
    }

    @Test
    @Order(1)
    void demoStatusAdvertisesTheWorkspaceOnlyWhenEnabled() {
        // This class runs with erp.demo.enabled=true, so the route must exist and the
        // sign-in screen must be told it can offer the button.
        given().get("/api/demo/status").then().statusCode(200)
                .body("enabled", equalTo(true))
                .body("username", equalTo("demo"));
    }

    @Test
    @Order(2)
    void demoSessionNeedsNoCredentialAndIsFlaggedAsDemo() {
        // No username, no password: the point of the button is that a reviewer never
        // has to know a credential.
        given().contentType(ContentType.JSON)
                .post("/api/demo/session").then().statusCode(200)
                .body("demo", equalTo(true))
                .body("username", equalTo("demo"))
                .body("roles", hasItem("ADMIN"));

        demoToken = given().contentType(ContentType.JSON).post("/api/demo/session")
                .then().statusCode(200).extract().path("token");
        demoCompanyId = Long.valueOf(given().header("Authorization", "Bearer " + demoToken)
                .get("/api/auth/me").then().statusCode(200).extract().path("companyId").toString());

        // Regression guard for the demo flag: the flag is a custom JWT claim, which
        // SmallRye hands back wrapped in a JSON-P value. Reading it as a plain
        // java.lang.Boolean used to return false, so /api/demo/reset answered 403 to a
        // genuine demo session. The claim must survive the token round-trip as a boolean.
        given().header("Authorization", "Bearer " + demoToken)
                .get("/api/auth/me").then().statusCode(200)
                .body("demo", equalTo(true))
                .body("fullName", equalTo("Demo Reviewer"));

        adminToken = login("admin", "admin");
        adminCompanyId = Long.valueOf(given().header("Authorization", "Bearer " + adminToken)
                .get("/api/auth/me").then().statusCode(200).extract().path("companyId").toString());

        // And a normal session must not be mistaken for a demo one.
        given().header("Authorization", "Bearer " + adminToken)
                .get("/api/auth/me").then().statusCode(200).body("demo", equalTo(false));

        // The whole isolation story in one assertion: demo data lives in its own tenant.
        org.junit.jupiter.api.Assertions.assertNotEquals(adminCompanyId, demoCompanyId,
                "the demo session must not share a tenant with a real company");
    }

    @Test
    @Order(3)
    void openingTheDemoSessionTwiceDoesNotDuplicateAnything() {
        String before = given().header("Authorization", "Bearer " + demoToken)
                .get("/api/sales/customers").then().statusCode(200).extract().path("total").toString();
        given().contentType(ContentType.JSON).post("/api/demo/session").then().statusCode(200);
        given().contentType(ContentType.JSON).post("/api/demo/session").then().statusCode(200);
        String after = given().header("Authorization", "Bearer " + demoToken)
                .get("/api/sales/customers").then().statusCode(200).extract().path("total").toString();
        org.junit.jupiter.api.Assertions.assertEquals(before, after,
                "the demo dataset must be keyed on natural codes so re-opening never duplicates rows");
    }

    @Test
    @Order(4)
    void theDemoDatasetCoversEveryModuleAContributorWouldClick() {
        // Enough variety to try tables, filters, detail panes and posting.
        given().header("Authorization", "Bearer " + demoToken)
                .get("/api/sales/customers").then().statusCode(200).body("total", greaterThanOrEqualTo(6));
        given().header("Authorization", "Bearer " + demoToken)
                .get("/api/sales/orders").then().statusCode(200).body("total", greaterThanOrEqualTo(6));
        given().header("Authorization", "Bearer " + demoToken)
                .get("/api/inventory/items").then().statusCode(200).body("total", greaterThanOrEqualTo(8));
        given().header("Authorization", "Bearer " + demoToken)
                .get("/api/finance/accounts").then().statusCode(200).body("total", greaterThanOrEqualTo(13));

        // Several document statuses so filtering has something to bite on.
        String invoices = given().header("Authorization", "Bearer " + demoToken)
                .get("/api/sales/invoices").then().statusCode(200).extract().asString();
        org.junit.jupiter.api.Assertions.assertTrue(invoices.contains("POSTED"), "demo needs posted documents");
        org.junit.jupiter.api.Assertions.assertTrue(invoices.contains("DRAFT"), "demo needs draft documents");
        org.junit.jupiter.api.Assertions.assertTrue(invoices.contains("OVERDUE"), "demo needs an overdue document");
    }

    @Test
    @Order(5)
    void demoCrudWorksEndToEnd() {
        String token = demoToken;
        String customers = "/api/sales/customers";

        Map<String, Object> created = given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("code", "REVIEW-01", "name", "Reviewer test customer", "countryCode", "NL"))
                .post(customers).then().statusCode(201).extract().as(Map.class);
        String id = created.get("id").toString();

        given().header("Authorization", "Bearer " + token)
                .get(customers + "?q=REVIEW-01").then().statusCode(200).body("total", equalTo(1));

        created.put("name", "Reviewer test customer (edited)");
        given().header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON).body(created)
                .put(customers + "/" + id).then().statusCode(200).body("name", equalTo("Reviewer test customer (edited)"));

        given().header("Authorization", "Bearer " + token)
                .delete(customers + "/" + id).then().statusCode(204);
        given().header("Authorization", "Bearer " + token)
                .get(customers + "/" + id).then().statusCode(404);
    }

    @Test
    @Order(6)
    void aDemoSessionCannotTouchAnotherTenant() {
        long[] foreignId = new long[1];
        io.quarkus.narayana.jta.QuarkusTransaction.requiringNew().run(() -> {
            Long foreignCompany = em.createQuery(
                            "select c.id from Company c where c.id <> :demo order by c.id", Long.class)
                    .setParameter("demo", demoCompanyId).setMaxResults(1).getSingleResult();
            foreignId[0] = em.createQuery(
                            "select k.id from SalCustomer k where k.companyId = :c and k.active = true order by k.id", Long.class)
                    .setParameter("c", foreignCompany).setMaxResults(1).getSingleResult();
        });

        String customers = "/api/sales/customers";
        given().header("Authorization", "Bearer " + demoToken)
                .get(customers + "/" + foreignId[0]).then().statusCode(404);
        given().header("Authorization", "Bearer " + demoToken)
                .delete(customers + "/" + foreignId[0]).then().statusCode(404);
        given().header("Authorization", "Bearer " + demoToken)
                .contentType(ContentType.JSON)
                .body(Map.of("code", "STOLEN", "name", "Stolen", "version", 0))
                .put(customers + "/" + foreignId[0]).then().statusCode(404);
        // And the foreign row is still there for its own tenant.
        given().header("Authorization", "Bearer " + adminToken)
                .get(customers + "/" + foreignId[0]).then().statusCode(200);
    }

    @Test
    @Order(7)
    void resetOnlyAcceptsADemoSessionAndRestoresTheStartingDataset() {
        // A normal administrator session is not a demo session.
        given().header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON).post("/api/demo/reset").then().statusCode(403);
        given().header("Authorization", "Bearer " + adminToken)
                .delete("/api/demo/session").then().statusCode(403);

        // Create something in the demo tenant, then reset and prove it is gone.
        String customers = "/api/sales/customers";
        given().header("Authorization", "Bearer " + demoToken)
                .contentType(ContentType.JSON)
                .body(Map.of("code", "TEMP-DEMO", "name", "Should not survive reset"))
                .post(customers).then().statusCode(201);

        given().header("Authorization", "Bearer " + demoToken)
                .get(customers + "?q=TEMP-DEMO").then().statusCode(200).body("total", equalTo(1));

        given().header("Authorization", "Bearer " + demoToken)
                .contentType(ContentType.JSON).post("/api/demo/reset").then().log().ifValidationFails().statusCode(200)
                .body("customers", greaterThanOrEqualTo(6));

        // The demo tenant was rebuilt, so re-open the session the way the client does.
        demoToken = given().contentType(ContentType.JSON).post("/api/demo/session")
                .then().statusCode(200).extract().path("token");

        given().header("Authorization", "Bearer " + demoToken)
                .get(customers + "?q=TEMP-DEMO").then().statusCode(200).body("total", equalTo(0));
        // The seeded dataset is back exactly as documented.
        given().header("Authorization", "Bearer " + demoToken)
                .get(customers + "?q=C-1001").then().statusCode(200).body("total", equalTo(1));
        given().header("Authorization", "Bearer " + demoToken)
                .get("/api/sales/invoices").then().statusCode(200).body("total", greaterThanOrEqualTo(5));
    }

    @Test
    @Order(8)
    void resetNeverTouchesTheOtherTenant() {
        String before = given().header("Authorization", "Bearer " + adminToken)
                .get("/api/sales/customers").then().statusCode(200).extract().path("total").toString();

        given().header("Authorization", "Bearer " + demoToken)
                .contentType(ContentType.JSON).post("/api/demo/reset").then().statusCode(200);

        String after = given().header("Authorization", "Bearer " + adminToken)
                .get("/api/sales/customers").then().statusCode(200).extract().path("total").toString();
        org.junit.jupiter.api.Assertions.assertEquals(before, after,
                "reset must delete only rows carrying the demo company id");

        demoToken = given().contentType(ContentType.JSON).post("/api/demo/session")
                .then().statusCode(200).extract().path("token");
    }

    @Test
    @Order(9)
    void leavingTheDemoIsAcknowledgedAndRefusedForANormalSession() {
        // A demo session may close itself. The client drops the token locally either way,
        // so the point of the endpoint is that the exit is explicit and auditable.
        given().header("Authorization", "Bearer " + demoToken)
                .delete("/api/demo/session").then().statusCode(204);
        // A normal administrator session is still not a demo session.
        given().header("Authorization", "Bearer " + adminToken)
                .delete("/api/demo/session").then().statusCode(403);
    }

    @Test
    @Order(10)
    void seededLinesAreScopedRepairableAndSupportRealBusinessActions() {
        var session = given().contentType(ContentType.JSON).post("/api/demo/session").then().statusCode(200).extract();
        String token = session.path("token");
        long company = Long.parseLong(session.path("companyId").toString());
        given().auth().oauth2(token).get("/api/platform/companies").then().statusCode(200).body("total", equalTo(1));
        given().auth().oauth2(token).get("/api/platform/companies/meta").then().statusCode(200).body("readOnly", equalTo(true));
        given().auth().oauth2(token).contentType(ContentType.JSON).body(Map.of("code", "BYPASS", "name", "Forbidden company"))
                .post("/api/platform/companies").then().statusCode(403);
        given().auth().oauth2(token).contentType(ContentType.JSON).body(Map.of("username", "BYPASS", "password", "long-password", "fullName", "Forbidden account"))
                .post("/api/platform/app_users").then().statusCode(403);
        given().auth().oauth2(adminToken).contentType(ContentType.JSON)
                .body(Map.of("username", "demo", "password", "normal-demo-password", "fullName", "Normal account named demo"))
                .post("/api/platform/app_users").then().statusCode(201);
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("currentPassword", "normal-demo-password", "newPassword", "forbidden-new-password"))
                .post("/api/auth/password").then().statusCode(403);
        String normalDemoToken = given().contentType(ContentType.JSON).body(Map.of("username", "demo", "password", "normal-demo-password"))
                .post("/api/auth/login").then().statusCode(200).extract().path("token");
        given().auth().oauth2(normalDemoToken).contentType(ContentType.JSON)
                .body(Map.of("currentPassword", "normal-demo-password", "newPassword", "normal-changed-password"))
                .post("/api/auth/password").then().statusCode(204);
        given().contentType(ContentType.JSON).body(Map.of("username", "demo", "password", "normal-changed-password"))
                .post("/api/auth/login").then().statusCode(200);
        io.quarkus.narayana.jta.QuarkusTransaction.requiringNew().run(() -> {
            com.erp.platform.AppUser legacy = new com.erp.platform.AppUser(); legacy.companyId = company;
            legacy.defaultCompanyId = adminCompanyId; legacy.username = "LEGACY-DEMO"; legacy.fullName = "Legacy demo account";
            legacy.status = com.erp.platform.UserStatus.ACTIVE; legacy.passwordHash = hasher.hash("legacy-demo-password"); em.persist(legacy);
        });
        given().contentType(ContentType.JSON).body(Map.of("username", "LEGACY-DEMO", "password", "legacy-demo-password"))
                .post("/api/auth/login").then().statusCode(403);
        given().auth().oauth2(token).get("/api/platform/companies/" + adminCompanyId).then().statusCode(404);
        int globalCurrency = given().auth().oauth2(adminToken).contentType(ContentType.JSON)
                .body(Map.of("code", "TST", "name", "Shared reference currency"))
                .post("/api/platform/currencies").then().statusCode(201).extract().path("id");
        given().auth().oauth2(token).get("/api/platform/currencies/" + globalCurrency).then().statusCode(404);
        given().auth().oauth2(token).delete("/api/platform/currencies/" + globalCurrency).then().statusCode(404);
        given().auth().oauth2(token).get("/api/platform/currencies?q=TST").then().statusCode(200).body("total", equalTo(0));
        for (String path : new String[]{"/api/sales/invoice_lines", "/api/sales/delivery_lines",
                "/api/purchasing/goods_receipt_lines", "/api/purchasing/invoice_lines"})
            given().auth().oauth2(token).get(path).then().statusCode(200).body("total", greaterThan(0)).body("items.companyId", everyItem(equalTo((int)company)));

        int legacy = given().auth().oauth2(token).get("/api/sales/invoice_lines").then().extract().path("items[0].id");
        io.quarkus.narayana.jta.QuarkusTransaction.requiringNew().run(() ->
                em.createQuery("update SalInvoiceLine l set l.companyId = null where l.id = :id").setParameter("id", (long)legacy).executeUpdate());
        given().contentType(ContentType.JSON).post("/api/demo/session").then().statusCode(200);
        given().auth().oauth2(token).get("/api/sales/invoice_lines/" + legacy).then().statusCode(200).body("companyId", equalTo((int)company));

        int invoice = given().auth().oauth2(token).get("/api/sales/invoices?f.status=DRAFT").then().extract().path("items[0].id");
        given().auth().oauth2(token).post("/api/sales/actions/invoices/" + invoice + "/post").then().statusCode(200).body("status", equalTo("POSTED"));
        var receipt = given().auth().oauth2(token).get("/api/purchasing/goods-receipts?f.status=DRAFT").then().extract();
        int receiptId = receipt.path("items[0].id");
        given().auth().oauth2(token).post("/api/purchasing/actions/goods-receipts/" + receiptId + "/post").then().statusCode(200).body("status", equalTo("POSTED"));
        int vendorInvoice = given().auth().oauth2(token).get("/api/purchasing/invoices?f.status=DRAFT").then().extract().path("items[0].id");
        given().auth().oauth2(token).post("/api/purchasing/actions/invoices/" + vendorInvoice + "/match").then().statusCode(200);
        given().auth().oauth2(token).post("/api/purchasing/actions/invoices/" + vendorInvoice + "/post").then().statusCode(200).body("status", equalTo("POSTED"));

        int item = given().auth().oauth2(token).get("/api/purchasing/goods_receipt_lines?f.receiptId=" + receiptId).then().extract().path("items[0].itemId");
        int warehouse = receipt.path("items[0].warehouseId");
        int customer = given().auth().oauth2(token).get("/api/sales/customers").then().extract().path("items[0].id");
        int delivery = given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("deliveryNumber", "ACCEPTANCE-SHIP", "customerId", customer, "warehouseId", warehouse,
                        "deliveryDate", java.time.LocalDate.now().toString(), "status", "PENDING"))
                .post("/api/sales/deliveries").then().statusCode(201).extract().path("id");
        given().auth().oauth2(token).contentType(ContentType.JSON)
                .body(Map.of("deliveryId", delivery, "lineNo", 10, "itemId", item, "warehouseId", warehouse, "quantity", "1", "uom", "EA"))
                .post("/api/sales/delivery_lines").then().statusCode(201);
        given().auth().oauth2(token).post("/api/sales/actions/deliveries/" + delivery + "/ship").then().statusCode(200).body("status", equalTo("SHIPPED"));
        given().auth().oauth2(token).get("/api/platform/audit_logs?f.entityId=" + delivery + "&f.entityType=SalDelivery&f.action=SHIP").then().statusCode(200).body("total", equalTo(1));
        given().auth().oauth2(token).contentType(ContentType.JSON).post("/api/demo/reset").then().statusCode(200);
        org.junit.jupiter.api.Assertions.assertEquals(0L, em.createQuery("select count(l) from SalInvoiceLine l where l.companyId = :c", Long.class).setParameter("c", company).getSingleResult());
    }
}
