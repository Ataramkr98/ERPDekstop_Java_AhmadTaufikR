package com.erp;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import com.erp.platform.ModuleRegistry;
import java.util.*;

@QuarkusTest @QuarkusTestResource(BackendTestResource.class)
@io.quarkus.test.junit.TestProfile(BackendProfile.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BackendWorkflowTest extends AbstractHttpTest {
    @jakarta.inject.Inject jakarta.persistence.EntityManager em;
    private String login(String username,String password) {
        return given().contentType("application/json").body(Map.of("username",username,"password",password)).post("/api/auth/login").then().statusCode(200).extract().path("token");
    }
    @Test @Order(1) void everyCatalogEndpointHasMetadataAndWorkingList() {
        String token=login("admin","admin");
        for(var entry:ModuleRegistry.ENTRIES) {
            given().auth().oauth2(token).get(entry.path()+"/meta").then().statusCode(200).body("entity",equalTo(entry.entity())).body("fields.size()",greaterThan(1));
            given().auth().oauth2(token).get(entry.path()+"?size=1").then().statusCode(200).body("items",notNullValue());
        }
    }
    @Test @Order(2) void fieldMetadataRequiresASession() {
        // The meta descriptor is a full schema map for every entity, so it must not be
        // readable anonymously.
        given().get("/api/sales/customers/meta").then().statusCode(401);
    }
    @Test @Order(3) void crudValidationOptimisticLockAuditAndSoftDelete() {
        String token=login("admin","admin"); String path="/api/sales/customers";
        given().auth().oauth2(token).contentType("application/json").body(Map.of("code","INVALID")).post(path).then().statusCode(400);
        Map<String,Object> record=given().auth().oauth2(token).contentType("application/json").body(Map.of("code","TEST-CUSTOMER","name","Regression customer","currentBalance",999)).post(path).then().statusCode(201).extract().as(Map.class);
        assertEquals(0,new java.math.BigDecimal(record.get("currentBalance").toString()).signum());
        String id=record.get("id").toString(); Object version=record.get("version");
        record.put("name","Updated customer");
        record.put("currentBalance",777);
        given().auth().oauth2(token).contentType("application/json").body(record).put(path+"/"+id).then().statusCode(200).body("name",equalTo("Updated customer"));
        record.put("version",version);
        given().auth().oauth2(token).contentType("application/json").body(record).put(path+"/"+id).then().statusCode(409);
        given().auth().oauth2(token).get("/api/platform/audit_logs?f.entityId="+id+"&f.entityType=SalCustomer").then().statusCode(200).body("total",greaterThanOrEqualTo(2));
        given().auth().oauth2(token).delete(path+"/"+id).then().statusCode(204);
        given().auth().oauth2(token).get(path+"/"+id).then().statusCode(404);
        given().auth().oauth2(token).get(path+"?q=TEST-CUSTOMER&includeInactive=true").then().statusCode(200).body("total",equalTo(1));
    }
    @Test @Order(4) void csvOver200RowsAndAtomicFailure() {
        String token=login("admin","admin"),path="/api/sales/customers";
        StringBuilder csv=new StringBuilder("code,name\n");
        for(int i=0;i<205;i++)csv.append("BULK-").append(i).append(",Customer ").append(i).append('\n');
        given().auth().oauth2(token).contentType("text/csv").body(csv.toString()).post(path+"/import").then().statusCode(200).body("created",equalTo(205));
        String exported=given().auth().oauth2(token).get(path+"/export.csv?q=BULK-").then().statusCode(200).extract().asString();
        assertEquals(206,com.erp.shared.CsvSupport.parse(exported).size());
        given().auth().oauth2(token).contentType("text/csv").body("code,name\nATOMIC-1,Valid\nATOMIC-2,\n").post(path+"/import").then().statusCode(400);
        given().auth().oauth2(token).get(path+"?q=ATOMIC-").then().body("total",equalTo(0));
    }
    @Test @Order(5) void authenticationDoesNotFallBackAndSecretsStayPrivate() {
        given().auth().preemptive().basic("admin","admin").get("/api/sales/customers").then().statusCode(401);
        String token=login("admin","admin");
        String users=given().auth().oauth2(token).get("/api/platform/app_users").then().statusCode(200).extract().asString();
        assertFalse(users.contains("passwordHash"));
        given().auth().oauth2(token).get("/api/platform/app_users/meta").then().statusCode(200).body("importable", equalTo(false));
        given().auth().oauth2(token).contentType("text/csv").body("username,fullName\nCSV-USER,CSV account\n")
                .post("/api/platform/app_users/import").then().statusCode(400).body("message", containsString("account form"));
        given().auth().oauth2(token).contentType("application/json").body(Map.of("username","testuser","password","long-password","fullName","Test user")).post("/api/platform/app_users").then().statusCode(201);
        String userToken=login("testuser","long-password");
        given().auth().oauth2(userToken).get("/api/platform/app_users").then().statusCode(403);
        given().auth().oauth2(userToken).get("/api/platform/app_users/meta").then().statusCode(403);
        given().auth().oauth2(userToken).get("/api/platform/audit_logs/meta").then().statusCode(403);
        given().auth().oauth2(userToken).get("/api/platform/companies/meta").then().statusCode(403);
        given().auth().oauth2(userToken).get("/api/platform/companies").then().statusCode(403);
        given().auth().oauth2(userToken).get("/api/platform/currencies/meta").then().statusCode(200).body("readOnly", equalTo(true));
        given().auth().oauth2(userToken).contentType("application/json").body(Map.of("code", "TST", "name", "Forbidden currency"))
                .post("/api/platform/currencies").then().statusCode(403);
        given().contentType("application/json").body(Map.of("username","testuser","password","long-password","companyId",99999)).post("/api/auth/login").then().statusCode(403);
        for(int i=0;i<5;i++)given().contentType("application/json").body(Map.of("username","testuser","password","wrong")).post("/api/auth/login").then().statusCode(400);
        given().contentType("application/json").body(Map.of("username","testuser","password","long-password")).post("/api/auth/login").then().statusCode(403);
    }

    /**
     * Builds the smallest fixture the posting workflow needs: a chart of accounts, a
     * warehouse, an item and a posted-capable invoice.
     *
     * <p>The fixture lives in the caller's own tenant. It used to come from
     * {@code POST /api/demo/seed}, which wrote sample rows into whichever company was
     * signed in - that is exactly the behaviour demo isolation had to remove, so the
     * regression test now creates what it needs.
     */
    private Map<String,Object> postableInvoice(String token) {
        String path="/api/sales/customers";
        Map<String,Object> customer=given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("code","WF-CUST-"+System.nanoTime(),"name","Workflow customer","currency","USD")).post(path)
            .then().statusCode(201).extract().as(Map.class);
        Map<String,Object> item=given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("code","WF-ITEM-"+System.nanoTime(),"name","Workflow item","uom","EA","itemType","STOCK",
                         "valuationMethod","STANDARD","standardCost","80.00","unitPrice","149.00",
                         "isStockable",true,"isPurchasable",true,"isSaleable",true))
            .post("/api/inventory/items").then().statusCode(201).extract().as(Map.class);
        Map<String,Object> warehouse=given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("code","WF-WH-"+System.nanoTime(),"name","Workflow warehouse")).post("/api/inventory/warehouses")
            .then().statusCode(201).extract().as(Map.class);

        for(String[] account : new String[][]{{"1200","Accounts receivable","ASSET"},{"4000","Sales revenue","REVENUE"}}){
            int existing = given().auth().oauth2(token).get("/api/finance/accounts?f.code=" + account[0]).then().statusCode(200).extract().path("total");
            if (existing > 0) continue;
            given().auth().oauth2(token).contentType("application/json")
                .body(Map.of("code",account[0],"name",account[1],"accountType",account[2],"currency","USD","isGroup",false))
                .post("/api/finance/accounts").then().statusCode(201);
        }

        Map<String,Object> invoice=given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("invoiceNumber","WF-INV-"+System.nanoTime(),"customerId",customer.get("id"),
                         "invoiceDate",java.time.LocalDate.now().toString(),
                         "dueDate",java.time.LocalDate.now().plusDays(30).toString(),
                         "currency","USD","status","DRAFT",
                         // The header carries the document totals and posting checks them
                         // against the line sum, so the fixture has to state them: 2 x 149.00.
                         "subtotal","298.00","taxAmount","0.00","totalAmount","298.00"))
            .post("/api/sales/invoices")
            .then().statusCode(201).extract().as(Map.class);
        given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("invoiceId",invoice.get("id"),"lineNo",10,"itemId",item.get("id"),
                         "description","Workflow line","quantity","2","uom","EA","unitPrice","149.00",
                         "discountPercent","0","lineTotal","298.00"))
            .post("/api/sales/invoice_lines").then().statusCode(201);
        return Map.of("invoice",invoice.get("id"),"item",item.get("id"),"warehouse",warehouse.get("id"));
    }

    @Test @Order(6) void invoicePostingAndSingleReversal() {
        String token=login("admin","admin");
        Map<String,Object> fixture=postableInvoice(token);
        int id=Integer.parseInt(fixture.get("invoice").toString());
        given().auth().oauth2(token).contentType("application/json").get("/api/sales/invoices/"+id).then().statusCode(200).body("status",equalTo("DRAFT"));

        int journal=given().auth().oauth2(token).post("/api/sales/actions/invoices/"+id+"/post").then().statusCode(200).body("status",equalTo("POSTED")).extract().path("journalEntryId");
        // Posting twice must be refused rather than double-posted.
        given().auth().oauth2(token).post("/api/sales/actions/invoices/"+id+"/post").then().statusCode(409);
        int reversal=given().auth().oauth2(token).post("/api/finance/actions/journals/"+journal+"/reverse").then().statusCode(200).extract().path("id");
        // Reversing the reversal is not allowed.
        given().auth().oauth2(token).post("/api/finance/actions/journals/"+reversal+"/reverse").then().statusCode(400);
        given().auth().oauth2(token).get("/api/sales/invoices/"+id).then().statusCode(200).body("status",equalTo("VOID")).body("balanceAmount",equalTo(0.0f));
        given().auth().oauth2(token).contentType("application/json").body(Map.of("status","DRAFT","version",0)).put("/api/sales/invoices/"+id).then().statusCode(409);
        given().auth().oauth2(token).post("/api/finance/actions/journals/"+journal+"/reverse").then().statusCode(400);
        given().auth().oauth2(token).delete("/api/finance/journal-entries/"+journal).then().statusCode(403);
        given().auth().oauth2(token).get("/api/overview").then().statusCode(200).body("customers",greaterThan(0));
    }

    @Test @Order(7) void foreignCompanyRecordsCannotBeReadUpdatedOrDeleted() {
        long[] id=new long[1];
        io.quarkus.narayana.jta.QuarkusTransaction.requiringNew().run(()->{
            com.erp.platform.Company company=new com.erp.platform.Company(); company.code="SECOND"; company.name="Second tenant"; em.persist(company); company.companyId=company.id;
            com.erp.sales.SalCustomer customer=new com.erp.sales.SalCustomer(); customer.companyId=company.id; customer.code="PRIVATE"; customer.name="Private record"; em.persist(customer); id[0]=customer.id;
        });
        String token=login("admin","admin"),path="/api/sales/customers/"+id[0];
        given().auth().oauth2(token).get(path).then().statusCode(404);
        given().auth().oauth2(token).delete(path).then().statusCode(404);
        given().auth().oauth2(token).contentType("application/json").body(Map.of("code","EDIT","name","Denied","version",0)).put(path).then().statusCode(404);
    }

    @Test @Order(8) void receiptPostingCreatesStockExactlyOnceAndProtectsItsLines() {
        String token=login("admin","admin");
        String vendorCode="WF-VEND-"+System.nanoTime();
        Map<String,Object> vendor=given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("code",vendorCode,"name","Workflow vendor","vendorType","SUPPLIER","currency","USD"))
            .post("/api/purchasing/vendors").then().statusCode(201).extract().as(Map.class);
        Map<String,Object> item=given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("code","WF-RITEM-"+System.nanoTime(),"name","Receipt item","uom","EA","itemType","STOCK",
                         "valuationMethod","STANDARD","standardCost","80.00","unitPrice","120.00",
                         "isStockable",true,"isPurchasable",true,"isSaleable",true))
            .post("/api/inventory/items").then().statusCode(201).extract().as(Map.class);
        Map<String,Object> warehouse=given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("code","WF-RWH-"+System.nanoTime(),"name","Receipt warehouse")).post("/api/inventory/warehouses")
            .then().statusCode(201).extract().as(Map.class);
        for(String[] account : new String[][]{{"1300","Inventory","ASSET"},{"1400","Goods received not invoiced","ASSET"}}){
            given().auth().oauth2(token).contentType("application/json")
                .body(Map.of("code",account[0],"name",account[1],"accountType",account[2],"currency","USD","isGroup",false))
                .post("/api/finance/accounts").then().statusCode(201);
        }
        Map<String,Object> receipt=given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("receiptNumber","WF-GR-"+System.nanoTime(),"vendorId",vendor.get("id"),
                         "receiptDate",java.time.LocalDate.now().toString(),"status","DRAFT",
                         "warehouseId",warehouse.get("id")))
            .post("/api/purchasing/goods-receipts").then().statusCode(201).extract().as(Map.class);
        given().auth().oauth2(token).contentType("application/json")
            .body(Map.of("receiptId",receipt.get("id"),"lineNo",10,"itemId",item.get("id"),
                         "quantity","20","unitCost","80.00","uom","EA","lineTotal","1600.00"))
            .post("/api/purchasing/goods_receipt_lines").then().statusCode(201);

        int id=Integer.parseInt(receipt.get("id").toString());
        given().auth().oauth2(token).post("/api/purchasing/actions/goods-receipts/"+id+"/post").then().statusCode(200).body("status",equalTo("POSTED"));
        // Reposting would duplicate the stock movement, so it must be refused.
        given().auth().oauth2(token).post("/api/purchasing/actions/goods-receipts/"+id+"/post").then().statusCode(409);

        int line=given().auth().oauth2(token).get("/api/purchasing/goods_receipt_lines?f.receiptId="+id).then().statusCode(200).extract().path("items[0].id");
        // A posted receipt is a stock document: its lines are frozen.
        given().auth().oauth2(token).delete("/api/purchasing/goods_receipt_lines/"+line).then().statusCode(409);
    }

    @Test @Order(9) void demoRoutesAreAbsentUnlessDemoModeIsSwitchedOn() {
        // This profile leaves erp.demo.enabled at its default of false, which is what a
        // real deployment looks like. The credential-free demo sign-in must not exist
        // there: 404, not 401 or 403, so there is no route to probe at all.
        given().get("/api/demo/status").then().statusCode(404);
        given().contentType("application/json").post("/api/demo/session").then().statusCode(404);
        given().auth().oauth2(login("admin","admin")).contentType("application/json").post("/api/demo/reset").then().statusCode(404);
    }

    @Test @Order(10) void metadataPublishesTenantScopedReferenceTargets() {
        String token = login("admin", "admin");
        given().auth().oauth2(token).get("/api/sales/invoices/meta").then().statusCode(200)
                .body("fields.find { it.name == 'customerId' }.referencePath", equalTo("/api/sales/customers"))
                .body("fields.find { it.name == 'customerId' }.referenceLabel", equalTo("Customers"));
        given().auth().oauth2(token).get("/api/purchasing/goods_receipt_lines/meta").then().statusCode(200)
                .body("fields.find { it.name == 'receiptId' }.referencePath", equalTo("/api/purchasing/goods-receipts"));
    }

    @Test @Order(11) void overviewDerivesOverdueFromUnpaidBalanceAndDueDate() {
        String token = login("admin", "admin");
        int previous = given().auth().oauth2(token).get("/api/overview").then().statusCode(200).extract().path("overdueInvoices");
        var fixture = postableInvoice(token);
        String id = fixture.get("invoice").toString();
        Map<String,Object> invoice = given().auth().oauth2(token).get("/api/sales/invoices/" + id)
                .then().statusCode(200).extract().as(Map.class);
        invoice.put("dueDate", java.time.LocalDate.now().minusDays(1).toString());
        given().auth().oauth2(token).contentType("application/json").body(invoice).put("/api/sales/invoices/" + id).then().statusCode(200);
        given().auth().oauth2(token).post("/api/sales/actions/invoices/" + id + "/post").then().statusCode(200);
        given().auth().oauth2(token).get("/api/overview").then().statusCode(200).body("overdueInvoices", equalTo(previous + 1));
    }
}
