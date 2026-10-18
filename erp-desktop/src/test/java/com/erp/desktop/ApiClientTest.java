package com.erp.desktop;

import com.erp.desktop.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiClientTest {
    @Test void realTransportUsesJsonMediaTypeAndTheSameQueryForListAndExport() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        List<String> mediaTypes = Collections.synchronizedList(new ArrayList<>());
        AtomicReference<String> query = new AtomicReference<>();
        AtomicReference<String> location = new AtomicReference<>();
        server.createContext("/", exchange -> {
            String method = exchange.getRequestMethod();
            if (method.equals("POST") || method.equals("PUT")) mediaTypes.add(exchange.getRequestHeaders().getFirst("Content-Type"));
            query.set(exchange.getRequestURI().getRawQuery());
            location.set(exchange.getRequestURI().getPath());
            String response = exchange.getRequestURI().getPath().endsWith("login") ? "{\"token\":\"test-token\"}"
                    : exchange.getRequestURI().getPath().equals("/precision") ? "{\"amount\":123456789012345.1234}" : "{}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json"); exchange.sendResponseHeaders(200, bytes.length);
            try (var stream = exchange.getResponseBody()) { stream.write(bytes); }
        });
        server.start();
        try {
            ApiClient api = new ApiClient("http://127.0.0.1:" + server.getAddress().getPort());
            api.login("reviewer", "password");
            var body = new ObjectMapper().createObjectNode().put("name", "Customer");
            api.create("/records", body); api.update("/records", "1", body);
            assertEquals(List.of("application/json", "application/json", "application/json"), mediaTypes);
            api.list("/records", "A & B", 1, 50, "name", false, Map.of("status", "DRAFT"));
            String listQuery = query.get();
            assertTrue(listQuery.contains("q=A+%26+B")); assertTrue(listQuery.contains("f.status=DRAFT"));
            api.exportCsv("/records", "A & B", "name", false, Map.of("status", "DRAFT"));
            assertTrue(query.get().contains("f.status=DRAFT")); assertTrue(query.get().contains("desc=false")); assertTrue(query.get().contains("sort=name"));
            api.importCsv("/records", "code,name\nC1,Customer\n");
            assertEquals("/records/import", location.get()); assertEquals("text/csv", mediaTypes.getLast());
            api.businessAction("/api/sales/actions/invoices/1/post");
            assertEquals("/api/sales/actions/invoices/1/post", location.get());
            var precision = api.get("/precision");
            assertEquals(new java.math.BigDecimal("123456789012345.1234"), precision.path("amount").decimalValue());
            assertEquals("123456789012345.1234", Values.asText(precision, "amount"));
        } finally { server.stop(0); }
    }
    @Test void rejectsIntegerOverflowAndUnexpectedBooleanValues() {
        var json = new ObjectMapper().createObjectNode();
        var count = new EntityMeta.Field("lineNo", "Line number", EntityMeta.Kind.INTEGER, false, List.of()); count.javaType = "Integer";
        assertThrows(IllegalArgumentException.class, () -> Values.put(json, count, "2147483648"));
        var enabled = new EntityMeta.Field("enabled", "Enabled", EntityMeta.Kind.BOOLEAN, false, List.of());
        assertThrows(IllegalArgumentException.class, () -> Values.put(json, enabled, "perhaps"));
    }
    @Test void monetaryDisplayPreservesFourDecimalPlacesAndKeepsIdsPlain() {
        var row = new ObjectMapper().createObjectNode().put("amount", new java.math.BigDecimal("1493.1234")).put("id", 12345);
        assertEquals("1,493.1234", Values.display(row, "amount"));
        assertEquals("12345", Values.display(row, "id"));
    }
}
