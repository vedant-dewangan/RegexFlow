package com.regexflow.backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Health Check
 * No @RequestMapping, so endpoints are at root level
 */
@RestController
public class HealthController {

    /**
     * ENDPOINT: GET /
     * @GetMapping("/") = This handles GET requests to the root path
     * 
     * Example request:
     * GET http://localhost:8080/
     * 
     * Returns: {"status": "UP", "message": "API is working"}
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "API is working");
        return ResponseEntity.ok(response);
    }
}
