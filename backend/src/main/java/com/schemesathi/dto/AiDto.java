package com.schemesathi.dto;

import java.util.List;
import java.util.Map;

public class AiDto {

    public static class ChatRequest {
        private String message;
        private String conversationId;

        public ChatRequest() {}
        public ChatRequest(String message, String conversationId) {
            this.message = message;
            this.conversationId = conversationId;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    }

    public static class ChatResponse {
        private String message;
        private String conversationId;
        private String agentType;
        private Map<String, Object> structuredData;
        private List<String> sourceReferences;
        private List<ActionSuggestion> suggestedActions;

        public ChatResponse() {}

        public ChatResponse(String message, String conversationId, String agentType, Map<String, Object> structuredData, List<String> sourceReferences, List<ActionSuggestion> suggestedActions) {
            this.message = message;
            this.conversationId = conversationId;
            this.agentType = agentType;
            this.structuredData = structuredData;
            this.sourceReferences = sourceReferences;
            this.suggestedActions = suggestedActions;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String message;
            private String conversationId;
            private String agentType;
            private Map<String, Object> structuredData;
            private List<String> sourceReferences;
            private List<ActionSuggestion> suggestedActions;

            public Builder message(String message) { this.message = message; return this; }
            public Builder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
            public Builder agentType(String agentType) { this.agentType = agentType; return this; }
            public Builder structuredData(Map<String, Object> structuredData) { this.structuredData = structuredData; return this; }
            public Builder sourceReferences(List<String> sourceReferences) { this.sourceReferences = sourceReferences; return this; }
            public Builder suggestedActions(List<ActionSuggestion> suggestedActions) { this.suggestedActions = suggestedActions; return this; }

            public ChatResponse build() {
                return new ChatResponse(message, conversationId, agentType, structuredData, sourceReferences, suggestedActions);
            }
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getConversationId() { return conversationId; }
        public void setConversationId(String conversationId) { this.conversationId = conversationId; }
        public String getAgentType() { return agentType; }
        public void setAgentType(String agentType) { this.agentType = agentType; }
        public Map<String, Object> getStructuredData() { return structuredData; }
        public void setStructuredData(Map<String, Object> structuredData) { this.structuredData = structuredData; }
        public List<String> getSourceReferences() { return sourceReferences; }
        public void setSourceReferences(List<String> sourceReferences) { this.sourceReferences = sourceReferences; }
        public List<ActionSuggestion> getSuggestedActions() { return suggestedActions; }
        public void setSuggestedActions(List<ActionSuggestion> suggestedActions) { this.suggestedActions = suggestedActions; }
    }

    public static class ActionSuggestion {
        private String label;
        private String action;
        private Map<String, String> params;

        public ActionSuggestion() {}

        public ActionSuggestion(String label, String action, Map<String, String> params) {
            this.label = label;
            this.action = action;
            this.params = params;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String label;
            private String action;
            private Map<String, String> params;

            public Builder label(String label) { this.label = label; return this; }
            public Builder action(String action) { this.action = action; return this; }
            public Builder params(Map<String, String> params) { this.params = params; return this; }

            public ActionSuggestion build() {
                return new ActionSuggestion(label, action, params);
            }
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public Map<String, String> getParams() { return params; }
        public void setParams(Map<String, String> params) { this.params = params; }
    }

    public static class ProfileExtractionResult {
        private Map<String, Object> extractedFields;
        private List<String> fieldsFound;
        private String confirmationMessage;

        public ProfileExtractionResult() {}

        public ProfileExtractionResult(Map<String, Object> extractedFields, List<String> fieldsFound, String confirmationMessage) {
            this.extractedFields = extractedFields;
            this.fieldsFound = fieldsFound;
            this.confirmationMessage = confirmationMessage;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Map<String, Object> extractedFields;
            private List<String> fieldsFound;
            private String confirmationMessage;

            public Builder extractedFields(Map<String, Object> extractedFields) { this.extractedFields = extractedFields; return this; }
            public Builder fieldsFound(List<String> fieldsFound) { this.fieldsFound = fieldsFound; return this; }
            public Builder confirmationMessage(String confirmationMessage) { this.confirmationMessage = confirmationMessage; return this; }

            public ProfileExtractionResult build() {
                return new ProfileExtractionResult(extractedFields, fieldsFound, confirmationMessage);
            }
        }

        public Map<String, Object> getExtractedFields() { return extractedFields; }
        public void setExtractedFields(Map<String, Object> extractedFields) { this.extractedFields = extractedFields; }
        public List<String> getFieldsFound() { return fieldsFound; }
        public void setFieldsFound(List<String> fieldsFound) { this.fieldsFound = fieldsFound; }
        public String getConfirmationMessage() { return confirmationMessage; }
        public void setConfirmationMessage(String confirmationMessage) { this.confirmationMessage = confirmationMessage; }
    }
}
