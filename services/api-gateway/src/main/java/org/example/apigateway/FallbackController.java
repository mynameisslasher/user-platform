package org.example.apigateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/users")
    public ResponseEntity<Map<String, Object>> usersFallback(ServerWebExchange exchange) {
        return build(HttpStatus.SERVICE_UNAVAILABLE,
                "user-service временно недоступен; повторите позже",
                exchange);
    }

    @RequestMapping("/notifications")
    public ResponseEntity<Map<String, Object>> notificationsFallback(ServerWebExchange exchange) {
        String method = exchange.getRequest().getMethod().name();

        HttpStatus status = "POST".equals(method)
                ? HttpStatus.ACCEPTED
                : HttpStatus.SERVICE_UNAVAILABLE;

        String msg = "POST".equals(method)
                ? "notification-service недоступен; запрос принят шлюзом"
                : "notification-service недоступен; повторите позже";

        return build(status, msg, exchange);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status,
                                                      String message,
                                                      ServerWebExchange exchange) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "message", message,
                "method", exchange.getRequest().getMethod().name(),
                "path", exchange.getRequest().getPath().value(),
                "source", "gateway-fallback"
        ));
    }
}