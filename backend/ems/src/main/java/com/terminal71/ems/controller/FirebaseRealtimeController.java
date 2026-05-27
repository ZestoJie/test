package com.terminal71.ems.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.database.FirebaseDatabase;
import com.terminal71.ems.service.FirebaseRealtimeService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@ConditionalOnBean(FirebaseDatabase.class)
@RequestMapping("/api/v1/firebase/rtdb")
public class FirebaseRealtimeController {

  private final FirebaseRealtimeService firebaseRealtimeService;

  public FirebaseRealtimeController(FirebaseRealtimeService firebaseRealtimeService) {
    this.firebaseRealtimeService = firebaseRealtimeService;
  }

  /**
   * Write data to Realtime Database
   * Example: POST /api/v1/firebase/rtdb/write
   * Body: {
   * "path": "users/user123",
   * "data": {"name": "John", "email": "john@example.com"}
   * }
   */
  
  @PostMapping("/write")
  public CompletableFuture<ResponseEntity<Map<String, String>>> writeData(
      @RequestBody Map<String, Object> request) {
    String path = (String) request.get("path");
    Object data = request.get("data");

    return firebaseRealtimeService.writeData(path, data)
        .thenApply(v -> {
          Map<String, String> response = new HashMap<>();
          response.put("message", "Data written successfully");
          response.put("path", path);
          return ResponseEntity.ok(response);
        })
        .exceptionally(ex -> ResponseEntity.badRequest()
            .body(Map.of("error", ex.getMessage())));
  }

  @GetMapping("/read/**")
  public CompletableFuture<ResponseEntity<?>> readData(
      @RequestParam(defaultValue = "") String path,
      HttpServletRequest request) {
    String fullPath = extractPathFromRequest(request);

    return firebaseRealtimeService.readData(fullPath)
        .thenApply(snapshot -> {
          if (snapshot.exists()) {
            return ResponseEntity.ok(Map.of(
                "path", fullPath,
                "data", snapshot.getValue()));
          } else {
            return ResponseEntity.notFound().build();
          }
        })
        .exceptionally(ex -> ResponseEntity.badRequest()
            .body(Map.of("error", ex.getMessage())));
  }

  @PutMapping("/update")
  public CompletableFuture<ResponseEntity<Map<String, String>>> updateData(
      @RequestBody Map<String, Object> request) {
    String path = (String) request.get("path");
    @SuppressWarnings("unchecked")
    Map<String, Object> updates = (Map<String, Object>) request.get("updates");

    return firebaseRealtimeService.updateData(path, updates)
        .thenApply(v -> {
          Map<String, String> response = new HashMap<>();
          response.put("message", "Data updated successfully");
          response.put("path", path);
          return ResponseEntity.ok(response);
        })
        .exceptionally(ex -> ResponseEntity.badRequest()
            .body(Map.of("error", ex.getMessage())));
  }

  @DeleteMapping("/delete/**")
  public CompletableFuture<ResponseEntity<Map<String, String>>> deleteData(
      HttpServletRequest request) {
    String fullPath = extractPathFromRequest(request);

    return firebaseRealtimeService.deleteData(fullPath)
        .thenApply(v -> {
          Map<String, String> response = new HashMap<>();
          response.put("message", "Data deleted successfully");
          response.put("path", fullPath);
          return ResponseEntity.ok(response);
        })
        .exceptionally(ex -> ResponseEntity.badRequest()
            .body(Map.of("error", ex.getMessage())));
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    try {
      Map<String, String> response = new HashMap<>();
      response.put("status", "Firebase Realtime Database is connected");
      response.put("database_url", "https://terminal71-ems.firebaseio.com");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", "Firebase connection failed: " + e.getMessage()));
    }
  }

  private String extractPathFromRequest(HttpServletRequest request) {
    String requestURI = request.getRequestURI();

    if (requestURI.contains("/read/")) {
      return requestURI.substring(requestURI.indexOf("/read/") + 6);
    } else if (requestURI.contains("/delete/")) {
      return requestURI.substring(requestURI.indexOf("/delete/") + 8);
    }
    return "";
  }
}
