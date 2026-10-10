package com.schemesathi.controller;

import com.schemesathi.dto.AiDto;
import com.schemesathi.dto.ApiResponse;
import com.schemesathi.entity.User;
import com.schemesathi.service.AiAgentOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAgentOrchestrator orchestrator;

    public AiController(AiAgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiDto.ChatResponse>> chat(
            @AuthenticationPrincipal User user,
            @RequestBody AiDto.ChatRequest request) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Authentication required"));
        }

        try {
            AiDto.ChatResponse response = orchestrator.processMessage(
                    user, request.getMessage(), request.getConversationId());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("AI service temporarily unavailable. Please try again."));
        }
    }
}
