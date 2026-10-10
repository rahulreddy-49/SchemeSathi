package com.schemesathi.service;

import com.schemesathi.dto.AiDto;
import com.schemesathi.dto.CitizenProfileDto;
import com.schemesathi.dto.EligibilityResultDto;
import com.schemesathi.dto.SchemeDto;
import com.schemesathi.entity.CitizenProfile;
import com.schemesathi.entity.Scheme;
import com.schemesathi.entity.User;
import com.schemesathi.repository.SchemeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Multi-Agent AI Orchestrator.
 *
 * Coordinates conceptual agents:
 * 1. Citizen Profile Agent - extracts profile info from natural language
 * 2. Scheme Discovery Agent - finds relevant schemes
 * 3. Eligibility Verification Agent - wraps deterministic engine
 * 4. Document Verification Agent - checks document readiness
 * 5. Explanation Agent - generates clear explanations
 * 6. Application Guidance Agent - provides step-by-step guidance
 * 7. Update Monitoring Agent - monitors scheme changes
 *
 * This implementation uses deterministic service calls around structured data.
 * When an external LLM is configured, NLU calls are delegated to it.
 * When no LLM is available, pattern-matching extracts intent and entities.
 */
@Service
public class AiAgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AiAgentOrchestrator.class);

    private final CitizenProfileService profileService;
    private final SchemeService schemeService;
    private final EligibilityEngine eligibilityEngine;
    private final SchemeRepository schemeRepository;

    public AiAgentOrchestrator(CitizenProfileService profileService,
                               SchemeService schemeService,
                               EligibilityEngine eligibilityEngine,
                               SchemeRepository schemeRepository) {
        this.profileService = profileService;
        this.schemeService = schemeService;
        this.eligibilityEngine = eligibilityEngine;
        this.schemeRepository = schemeRepository;
    }

    // Simple conversation memory (in production, use a proper store)
    private final Map<String, List<ConversationTurn>> conversations = new ConcurrentHashMap<>();

    record ConversationTurn(String role, String content) {}

    /**
     * Process a user message through the agent pipeline.
     */
    public AiDto.ChatResponse processMessage(User user, String message, String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        // Store conversation context
        conversations.computeIfAbsent(conversationId, k -> new ArrayList<>())
                .add(new ConversationTurn("user", message));

        // 1. Intent Detection (Profile Agent)
        String intent = detectIntent(message);
        log.debug("Detected intent: {} for message: {}", intent, message.substring(0, Math.min(50, message.length())));

        CitizenProfile profile = profileService.getProfileEntity(user);
        AiDto.ChatResponse response;

        // 2. Route to appropriate agent
        response = switch (intent) {
            case "PROFILE_UPDATE" -> handleProfileUpdate(user, message, conversationId, profile);
            case "SCHEME_DISCOVERY" -> handleSchemeDiscovery(user, message, conversationId, profile);
            case "ELIGIBILITY_CHECK" -> handleEligibilityCheck(user, message, conversationId, profile);
            case "DOCUMENT_CHECK" -> handleDocumentCheck(user, message, conversationId, profile);
            case "APPLICATION_GUIDANCE" -> handleApplicationGuidance(user, message, conversationId, profile);
            case "SCHEME_COMPARISON" -> handleSchemeComparison(user, message, conversationId, profile);
            case "GREETING" -> handleGreeting(user, conversationId);
            case "HELP" -> handleHelp(user, conversationId);
            default -> handleGeneral(user, message, conversationId, profile);
        };

        // Store response
        conversations.get(conversationId).add(new ConversationTurn("assistant", response.getMessage()));

        return response;
    }

    // ==================== INTENT DETECTION (Profile Agent) ====================

    private String detectIntent(String message) {
        String lower = message.toLowerCase().trim();

        // Greeting patterns
        if (lower.matches("^(hi|hello|hey|namaste|good\\s*(morning|afternoon|evening)).*"))
            return "GREETING";

        // Help patterns
        if (lower.contains("help") || lower.contains("what can you do") || lower.contains("how does"))
            return "HELP";

        // Profile update patterns
        if (lower.matches(".*(i am|i'm|my\\s+(age|income|name|state|occupation|education|family)|" +
                "i\\s+(live|work|study|earn)|years?\\s+old|lakh|salary|student|farmer|employed|" +
                "update.*profile|from\\s+\\w+).*"))
            return "PROFILE_UPDATE";

        // Eligibility check patterns
        if (lower.matches(".*(am i eligible|eligib|qualify|can i (get|apply|avail)|" +
                "do i meet|check.*eligib|why.*not eligible|why.*rejected).*"))
            return "ELIGIBILITY_CHECK";

        // Document check patterns
        if (lower.matches(".*(document|missing.*doc|what.*need|aadhaar|certificate|" +
                "papers|proof|upload|required.*doc).*"))
            return "DOCUMENT_CHECK";

        // Application guidance patterns
        if (lower.matches(".*(how.*apply|application|apply.*for|steps.*apply|" +
                "process.*apply|where.*apply|submit|registration).*"))
            return "APPLICATION_GUIDANCE";

        // Comparison patterns
        if (lower.matches(".*(compare|comparison|difference|better|which.*scheme|" +
                "vs|versus|or).*") && lower.contains("scheme"))
            return "SCHEME_COMPARISON";

        // Scheme discovery patterns
        if (lower.matches(".*(scheme|yojana|programme|program|benefit|subsidy|" +
                "scholarship|pension|insurance|loan|grant|find|discover|" +
                "search|recommend|suggest|show|available|what.*for me|list).*"))
            return "SCHEME_DISCOVERY";

        return "GENERAL";
    }

    // ==================== PROFILE AGENT ====================

    private AiDto.ChatResponse handleProfileUpdate(User user, String message, String conversationId, CitizenProfile profile) {
        Map<String, Object> extracted = extractProfileFields(message);

        if (extracted.isEmpty()) {
            return buildResponse("I couldn't extract any profile information from your message. " +
                "Could you tell me specific details like your age, state, occupation, income, or education level?",
                conversationId, "ProfileAgent", null, null);
        }

        // Update profile with extracted fields
        CitizenProfileDto dto = mapToDto(extracted);
        CitizenProfileDto updated = profileService.updateProfileFields(user, dto);

        StringBuilder confirmation = new StringBuilder("I've updated your profile with the following information:\n\n");
        extracted.forEach((key, value) ->
            confirmation.append("• **").append(formatFieldName(key)).append("**: ").append(value).append("\n"));
        confirmation.append("\nYour profile is now **").append(updated.getCompletenessPercentage()).append("% complete**.");

        if (updated.getCompletenessPercentage() < 60) {
            confirmation.append("\n\nTo get better scheme recommendations, consider providing: ");
            List<String> missing = getMissingFields(updated);
            confirmation.append(String.join(", ", missing.subList(0, Math.min(3, missing.size()))));
        }

        List<AiDto.ActionSuggestion> actions = List.of(
            AiDto.ActionSuggestion.builder().label("Find schemes for me").action("navigate").params(Map.of("path", "/schemes")).build(),
            AiDto.ActionSuggestion.builder().label("Check my eligibility").action("checkEligibility").params(Map.of()).build()
        );

        return buildResponse(confirmation.toString(), conversationId, "ProfileAgent",
                Map.of("extractedFields", extracted, "completeness", updated.getCompletenessPercentage()), actions);
    }

    /**
     * Extracts structured profile fields from natural language.
     * Uses pattern matching; with LLM integration, this would use NLU.
     */
    private Map<String, Object> extractProfileFields(String message) {
        Map<String, Object> fields = new LinkedHashMap<>();
        String lower = message.toLowerCase();

        // Age extraction
        var ageMatcher = java.util.regex.Pattern.compile("(\\d{1,3})\\s*(years?\\s*old|yr|age)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(message);
        if (ageMatcher.find()) {
            int age = Integer.parseInt(ageMatcher.group(1));
            if (age > 0 && age < 150) fields.put("age", age);
        } else {
            var ageMatch2 = java.util.regex.Pattern.compile("age\\s*(?:is)?\\s*(\\d{1,3})", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(message);
            if (ageMatch2.find()) {
                int age = Integer.parseInt(ageMatch2.group(1));
                if (age > 0 && age < 150) fields.put("age", age);
            }
        }

        // Income extraction
        var incomeMatcher = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(lakh|lac|lakhs?)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(message);
        if (incomeMatcher.find()) {
            double income = Double.parseDouble(incomeMatcher.group(1)) * 100000;
            fields.put("annualIncome", income);
        } else {
            var incMatch2 = java.util.regex.Pattern.compile("income\\s*(?:is)?\\s*(?:around)?\\s*(?:₹|rs\\.?)?\\s*(\\d+(?:,\\d+)*)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(message);
            if (incMatch2.find()) {
                double income = Double.parseDouble(incMatch2.group(1).replace(",", ""));
                fields.put("annualIncome", income);
            }
        }

        // State extraction
        List<String> indianStates = List.of("Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar",
            "Chhattisgarh", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
            "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya",
            "Mizoram", "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim",
            "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Delhi", "Jammu and Kashmir", "Ladakh", "Puducherry", "Chandigarh");
        for (String state : indianStates) {
            if (lower.contains(state.toLowerCase())) {
                fields.put("state", state);
                break;
            }
        }

        // Occupation / Employment
        if (lower.contains("student")) {
            fields.put("occupation", "Student");
            fields.put("employmentStatus", "Student");
        } else if (lower.contains("farmer") || lower.contains("agriculture") || lower.contains("farming")) {
            fields.put("occupation", "Farmer");
            fields.put("employmentStatus", "SelfEmployed");
        } else if (lower.contains("unemployed") || lower.contains("jobless") || lower.contains("without job")) {
            fields.put("employmentStatus", "Unemployed");
        } else if (lower.contains("self employed") || lower.contains("self-employed") || lower.contains("business")) {
            fields.put("employmentStatus", "SelfEmployed");
        } else if (lower.contains("retired")) {
            fields.put("employmentStatus", "Retired");
        } else if (lower.contains("employed") || lower.contains("working") || lower.contains("job")) {
            fields.put("employmentStatus", "Employed");
        }

        // Education
        if (lower.contains("10th") || lower.contains("class 10") || lower.contains("matriculation")) {
            fields.put("educationLevel", "10th");
        } else if (lower.contains("12th") || lower.contains("class 12") || lower.contains("intermediate")) {
            fields.put("educationLevel", "12th");
        } else if (lower.contains("graduate") || lower.contains("degree") || lower.contains("b.tech") || lower.contains("b.sc") || lower.contains("b.a") || lower.contains("b.com")) {
            fields.put("educationLevel", "Graduate");
        } else if (lower.contains("post graduate") || lower.contains("postgraduate") || lower.contains("masters") || lower.contains("m.tech") || lower.contains("mba")) {
            fields.put("educationLevel", "PostGraduate");
        } else if (lower.contains("phd") || lower.contains("doctorate")) {
            fields.put("educationLevel", "PhD");
        } else if (lower.contains("illiterate") || lower.contains("no education") || lower.contains("uneducated")) {
            fields.put("educationLevel", "NoFormalEducation");
        }

        // Category
        for (var entry : Map.of("obc", "OBC", "sc", "SC", "st", "ST", "ews", "EWS", "general", "General").entrySet()) {
            if (lower.matches(".*\\b" + entry.getKey() + "\\b.*")) {
                fields.put("category", entry.getValue());
                break;
            }
        }

        // Gender
        if (lower.contains("female") || lower.contains("woman") || lower.contains("girl")) {
            fields.put("gender", "Female");
        } else if (lower.contains("male") || lower.contains("man") || lower.contains("boy")) {
            fields.put("gender", "Male");
        }

        // Disability
        if (lower.contains("disabled") || lower.contains("disability") || lower.contains("handicap") || lower.contains("divyang")) {
            fields.put("disabilityStatus", true);
        }

        // BPL
        if (lower.contains("bpl") || lower.contains("below poverty")) {
            fields.put("bplStatus", true);
        }

        // Residence
        if (lower.contains("rural") || lower.contains("village")) {
            fields.put("residenceType", "Rural");
        } else if (lower.contains("urban") || lower.contains("city")) {
            fields.put("residenceType", "Urban");
        }

        return fields;
    }

    // ==================== SCHEME DISCOVERY AGENT ====================

    private AiDto.ChatResponse handleSchemeDiscovery(User user, String message, String conversationId, CitizenProfile profile) {
        List<SchemeDto> schemes;
        String context = "";

        // Try to find schemes based on profile + query
        if (profile != null && profile.getState() != null) {
            schemes = schemeService.getApplicableSchemes(profile.getState(), user.getId());
            context = " based on your state (" + profile.getState() + ")";
        } else {
            schemes = schemeService.getAllSchemes(user.getId());
        }

        // Filter by category if mentioned
        String category = detectCategory(message);
        if (category != null) {
            schemes = schemes.stream()
                    .filter(s -> category.equalsIgnoreCase(s.getBenefitCategory()))
                    .collect(Collectors.toList());
            context += " in " + category;
        }

        // Also apply keyword search
        String searchQuery = extractSearchQuery(message);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            List<SchemeDto> searchResults = schemeService.searchSchemes(searchQuery, user.getId());
            if (!searchResults.isEmpty()) {
                schemes = searchResults;
                context = " matching '" + searchQuery + "'";
            }
        }

        if (schemes.isEmpty()) {
            return buildResponse("I couldn't find any schemes" + context + " at the moment. " +
                    "Try broadening your search or updating your profile with more details.",
                    conversationId, "DiscoveryAgent", null,
                    List.of(AiDto.ActionSuggestion.builder().label("View all schemes").action("navigate").params(Map.of("path", "/schemes")).build()));
        }

        // Build personalized response with eligibility hints
        StringBuilder sb = new StringBuilder();
        sb.append("I found **").append(schemes.size()).append(" schemes**").append(context).append(":\n\n");

        int limit = Math.min(5, schemes.size());
        for (int i = 0; i < limit; i++) {
            SchemeDto scheme = schemes.get(i);
            sb.append("**").append(i + 1).append(". ").append(scheme.getName()).append("**\n");
            if (scheme.getDescription() != null) {
                String desc = scheme.getDescription().length() > 150
                    ? scheme.getDescription().substring(0, 150) + "..."
                    : scheme.getDescription();
                sb.append(desc).append("\n");
            }
            sb.append("• Category: ").append(scheme.getBenefitCategory()).append(" | Scope: ").append(scheme.getScope()).append("\n\n");
        }

        if (schemes.size() > 5) {
            sb.append("_...and ").append(schemes.size() - 5).append(" more schemes._\n\n");
        }

        sb.append("Would you like to check your eligibility for any of these schemes?");

        Map<String, Object> data = new HashMap<>();
        data.put("schemes", schemes.stream().map(s -> Map.of("id", s.getId(), "name", s.getName(), "category", s.getBenefitCategory())).collect(Collectors.toList()));
        data.put("totalCount", schemes.size());

        return buildResponse(sb.toString(), conversationId, "DiscoveryAgent", data,
                List.of(
                    AiDto.ActionSuggestion.builder().label("View all schemes").action("navigate").params(Map.of("path", "/schemes")).build(),
                    AiDto.ActionSuggestion.builder().label("Check eligibility").action("checkEligibility").params(Map.of()).build()
                ));
    }

    // ==================== ELIGIBILITY VERIFICATION AGENT ====================

    private AiDto.ChatResponse handleEligibilityCheck(User user, String message, String conversationId, CitizenProfile profile) {
        if (profile == null) {
            return buildResponse("I need your profile information to check eligibility. " +
                "Please tell me about yourself — your age, state, income, occupation, education, and category.",
                conversationId, "EligibilityAgent", null,
                List.of(AiDto.ActionSuggestion.builder().label("Create profile").action("navigate").params(Map.of("path", "/profile")).build()));
        }

        // Try to identify a specific scheme
        Long schemeId = extractSchemeId(message);
        if (schemeId != null) {
            Scheme scheme = schemeRepository.findById(schemeId).orElse(null);
            if (scheme != null) {
                EligibilityResultDto result = eligibilityEngine.evaluate(scheme, profile);
                return formatEligibilityResponse(result, conversationId);
            }
        }

        // Evaluate all applicable schemes
        List<EligibilityResultDto> results = eligibilityEngine.evaluateAll(profile);

        if (results.isEmpty()) {
            return buildResponse("No schemes could be evaluated. Please ensure your profile has at least your state information.",
                    conversationId, "EligibilityAgent", null, null);
        }

        StringBuilder sb = new StringBuilder("Here's your eligibility overview:\n\n");

        long eligible = results.stream().filter(r -> r.getOverallStatus() == EligibilityResultDto.EligibilityStatus.ELIGIBLE).count();
        long probably = results.stream().filter(r -> r.getOverallStatus() == EligibilityResultDto.EligibilityStatus.PROBABLY_ELIGIBLE).count();
        long notEligible = results.stream().filter(r -> r.getOverallStatus() == EligibilityResultDto.EligibilityStatus.NOT_ELIGIBLE).count();
        long insufficient = results.stream().filter(r -> r.getOverallStatus() == EligibilityResultDto.EligibilityStatus.INSUFFICIENT_INFORMATION).count();

        sb.append("✅ **Eligible**: ").append(eligible).append(" schemes\n");
        sb.append("🟡 **Probably Eligible**: ").append(probably).append(" schemes\n");
        sb.append("❌ **Not Eligible**: ").append(notEligible).append(" schemes\n");
        if (insufficient > 0) sb.append("⚠️ **Needs More Info**: ").append(insufficient).append(" schemes\n");
        sb.append("\n");

        // Show top eligible ones
        List<EligibilityResultDto> top = results.stream()
                .filter(r -> r.getOverallStatus() == EligibilityResultDto.EligibilityStatus.ELIGIBLE ||
                             r.getOverallStatus() == EligibilityResultDto.EligibilityStatus.PROBABLY_ELIGIBLE)
                .limit(5)
                .collect(Collectors.toList());

        if (!top.isEmpty()) {
            sb.append("**Your best matches:**\n\n");
            for (EligibilityResultDto r : top) {
                String icon = r.getOverallStatus() == EligibilityResultDto.EligibilityStatus.ELIGIBLE ? "✅" : "🟡";
                sb.append(icon).append(" **").append(r.getSchemeName()).append("** — ")
                  .append(r.getOverallStatus().name().replace("_", " "))
                  .append(" (Score: ").append((int) r.getMatchScore()).append("%)\n");
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("eligibilityResults", results);
        data.put("summary", Map.of("eligible", eligible, "probablyEligible", probably, "notEligible", notEligible, "insufficientInfo", insufficient));

        return buildResponse(sb.toString(), conversationId, "EligibilityAgent", data,
                List.of(AiDto.ActionSuggestion.builder().label("View detailed results").action("navigate").params(Map.of("path", "/eligibility")).build()));
    }

    private AiDto.ChatResponse formatEligibilityResponse(EligibilityResultDto result, String conversationId) {
        StringBuilder sb = new StringBuilder();
        String icon = switch (result.getOverallStatus()) {
            case ELIGIBLE -> "✅";
            case PROBABLY_ELIGIBLE -> "🟡";
            case NOT_ELIGIBLE -> "❌";
            case INSUFFICIENT_INFORMATION -> "⚠️";
            case REQUIRES_OFFICIAL_VERIFICATION -> "🔍";
        };

        sb.append(icon).append(" **").append(result.getSchemeName()).append("** — ")
          .append(result.getOverallStatus().name().replace("_", " ")).append("\n\n");
        sb.append(result.getExplanation()).append("\n\n");

        if (result.getCriteriaResults() != null && !result.getCriteriaResults().isEmpty()) {
            sb.append("**Criteria Assessment:**\n\n");
            for (var cr : result.getCriteriaResults()) {
                String crIcon = switch (cr.getStatus()) {
                    case PASS -> "✅";
                    case FAIL -> "❌";
                    case MISSING -> "⚠️";
                    case NEEDS_VERIFICATION -> "🔍";
                };
                sb.append(crIcon).append(" ").append(cr.getDisplayLabel())
                  .append(": Your value: ").append(cr.getCitizenValue())
                  .append(" | Required: ").append(cr.getRequiredValue()).append("\n");
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("eligibilityResult", result);

        return buildResponse(sb.toString(), conversationId, "EligibilityAgent", data, null);
    }

    // ==================== DOCUMENT AGENT ====================

    private AiDto.ChatResponse handleDocumentCheck(User user, String message, String conversationId, CitizenProfile profile) {
        // Get all schemes and their document requirements
        List<SchemeDto> schemes;
        if (profile != null && profile.getState() != null) {
            schemes = schemeService.getApplicableSchemes(profile.getState(), user.getId());
        } else {
            schemes = schemeService.getAllSchemes(user.getId());
        }

        // Collect all unique documents
        Map<String, List<String>> docToSchemes = new LinkedHashMap<>();
        for (SchemeDto scheme : schemes) {
            if (scheme.getRequiredDocuments() != null) {
                for (var doc : scheme.getRequiredDocuments()) {
                    docToSchemes.computeIfAbsent(doc.getDocumentName(), k -> new ArrayList<>())
                            .add(scheme.getName());
                }
            }
        }

        if (docToSchemes.isEmpty()) {
            return buildResponse("No document requirements found for your applicable schemes.",
                    conversationId, "DocumentAgent", null, null);
        }

        StringBuilder sb = new StringBuilder("Here are the documents commonly required across your applicable schemes:\n\n");
        int count = 1;
        for (var entry : docToSchemes.entrySet()) {
            sb.append("**").append(count++).append(". ").append(entry.getKey()).append("**\n");
            sb.append("   Required for: ").append(String.join(", ", entry.getValue().subList(0, Math.min(3, entry.getValue().size()))));
            if (entry.getValue().size() > 3) sb.append(" and ").append(entry.getValue().size() - 3).append(" more");
            sb.append("\n\n");
        }

        sb.append("**Tip:** Keep digital copies of all these documents ready. Most scheme applications now accept online document uploads.\n\n");
        sb.append("⚠️ *Document verification is for guidance only. Official verification is required during the actual application process.*");

        Map<String, Object> data = Map.of("documents", docToSchemes);

        return buildResponse(sb.toString(), conversationId, "DocumentAgent", data,
                List.of(AiDto.ActionSuggestion.builder().label("View document readiness").action("navigate").params(Map.of("path", "/documents")).build()));
    }

    // ==================== APPLICATION GUIDANCE AGENT ====================

    private AiDto.ChatResponse handleApplicationGuidance(User user, String message, String conversationId, CitizenProfile profile) {
        Long schemeId = extractSchemeId(message);
        if (schemeId != null) {
            SchemeDto scheme = schemeService.getSchemeById(schemeId, user.getId());
            return formatApplicationGuidance(scheme, profile, conversationId);
        }

        // General guidance
        return buildResponse("I can guide you through the application process for any specific scheme. " +
                "Which scheme would you like to apply for? You can:\n\n" +
                "• Browse schemes and click 'How to Apply'\n" +
                "• Ask me about a specific scheme by name\n" +
                "• Check your eligibility first to find the best schemes for you",
                conversationId, "ApplicationAgent", null,
                List.of(
                    AiDto.ActionSuggestion.builder().label("Browse schemes").action("navigate").params(Map.of("path", "/schemes")).build(),
                    AiDto.ActionSuggestion.builder().label("Check eligibility").action("checkEligibility").params(Map.of()).build()
                ));
    }

    private AiDto.ChatResponse formatApplicationGuidance(SchemeDto scheme, CitizenProfile profile, String conversationId) {
        StringBuilder sb = new StringBuilder();
        sb.append("## How to Apply for ").append(scheme.getName()).append("\n\n");

        if (scheme.getApplicationProcess() != null) {
            sb.append("### Application Process\n");
            sb.append(scheme.getApplicationProcess()).append("\n\n");
        }

        if (scheme.getRequiredDocuments() != null && !scheme.getRequiredDocuments().isEmpty()) {
            sb.append("### Required Documents\n");
            for (var doc : scheme.getRequiredDocuments()) {
                sb.append("• ").append(doc.getDocumentName());
                if (Boolean.FALSE.equals(doc.getMandatory())) sb.append(" _(optional)_");
                sb.append("\n");
            }
            sb.append("\n");
        }

        if (scheme.getApplicationUrl() != null) {
            sb.append("### Official Application Link\n");
            sb.append("🔗 [Apply on official portal](").append(scheme.getApplicationUrl()).append(")\n\n");
        }

        if (scheme.getImportantWarnings() != null) {
            sb.append("### ⚠️ Important Warnings\n");
            sb.append(scheme.getImportantWarnings()).append("\n\n");
        }

        if (scheme.getCommonRejectionReasons() != null) {
            sb.append("### Common Rejection Reasons\n");
            sb.append(scheme.getCommonRejectionReasons()).append("\n\n");
        }

        sb.append("---\n*SchemeSathi provides guidance only. You must apply through the official government portal. " +
                "Always verify the latest requirements on the official website before applying.*");

        return buildResponse(sb.toString(), conversationId, "ApplicationAgent",
                Map.of("schemeId", scheme.getId(), "schemeName", scheme.getName()),
                List.of(AiDto.ActionSuggestion.builder().label("View scheme details").action("navigate")
                    .params(Map.of("path", "/schemes/" + scheme.getId())).build()));
    }

    // ==================== SCHEME COMPARISON AGENT ====================

    private AiDto.ChatResponse handleSchemeComparison(User user, String message, String conversationId, CitizenProfile profile) {
        return buildResponse("To compare schemes, please visit the Schemes page and select schemes to compare. " +
                "You can compare schemes based on benefits, eligibility criteria, required documents, and application process.\n\n" +
                "I can also help you compare specific schemes if you tell me their names.",
                conversationId, "ComparisonAgent", null,
                List.of(AiDto.ActionSuggestion.builder().label("Compare schemes").action("navigate").params(Map.of("path", "/schemes?compare=true")).build()));
    }

    // ==================== GENERAL HANDLERS ====================

    private AiDto.ChatResponse handleGreeting(User user, String conversationId) {
        String name = user.getFullName().split(" ")[0];
        return buildResponse("Namaste, " + name + "! 🙏\n\nI'm SchemeSathi, your AI assistant for government schemes. " +
                "I can help you:\n\n" +
                "• **Discover** government schemes you may be eligible for\n" +
                "• **Check eligibility** based on your profile\n" +
                "• **Understand** required documents\n" +
                "• **Guide** you through the application process\n\n" +
                "Tell me about yourself or ask about any government scheme!",
                conversationId, "Orchestrator", null,
                List.of(
                    AiDto.ActionSuggestion.builder().label("Find schemes for me").action("navigate").params(Map.of("path", "/schemes")).build(),
                    AiDto.ActionSuggestion.builder().label("Complete my profile").action("navigate").params(Map.of("path", "/profile")).build()
                ));
    }

    private AiDto.ChatResponse handleHelp(User user, String conversationId) {
        return buildResponse("Here's what I can do:\n\n" +
                "**🔍 Discover Schemes**\nAsk me: \"What schemes can I apply for?\" or \"Show me education schemes\"\n\n" +
                "**✅ Check Eligibility**\nAsk me: \"Am I eligible for PM Kisan?\" or \"Check my eligibility\"\n\n" +
                "**📋 Profile Updates**\nTell me: \"I am a 25 year old student from Telangana\" and I'll update your profile\n\n" +
                "**📄 Document Guidance**\nAsk me: \"What documents do I need?\" or \"Am I missing any documents?\"\n\n" +
                "**📝 Application Help**\nAsk me: \"How do I apply for this scheme?\" or \"Guide me through the application\"\n\n" +
                "**📊 Compare Schemes**\nAsk me: \"Compare these schemes\" on the schemes page",
                conversationId, "Orchestrator", null, null);
    }

    private AiDto.ChatResponse handleGeneral(User user, String message, String conversationId, CitizenProfile profile) {
        // Try to match against scheme names
        List<SchemeDto> matchedSchemes = schemeService.searchSchemes(message, user.getId());
        if (!matchedSchemes.isEmpty() && matchedSchemes.size() <= 5) {
            return handleSchemeDiscovery(user, message, conversationId, profile);
        }

        return buildResponse("I understand you're asking about: \"" + message + "\"\n\n" +
                "I'm best at helping with government scheme discovery, eligibility checking, and application guidance. " +
                "Could you rephrase your question or try one of these:\n\n" +
                "• \"What schemes can I apply for?\"\n" +
                "• \"Am I eligible for [scheme name]?\"\n" +
                "• \"What documents do I need?\"\n" +
                "• \"How do I apply for [scheme name]?\"",
                conversationId, "Orchestrator", null, null);
    }

    // ==================== UTILITY METHODS ====================

    private AiDto.ChatResponse buildResponse(String message, String conversationId, String agentType,
                                             Map<String, Object> data, List<AiDto.ActionSuggestion> actions) {
        return AiDto.ChatResponse.builder()
                .message(message)
                .conversationId(conversationId)
                .agentType(agentType)
                .structuredData(data)
                .suggestedActions(actions)
                .sourceReferences(List.of("Source: SchemeSathi Knowledge Base"))
                .build();
    }

    private String detectCategory(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("education") || lower.contains("scholarship") || lower.contains("study")) return "Education";
        if (lower.contains("health") || lower.contains("medical") || lower.contains("hospital")) return "Health";
        if (lower.contains("agriculture") || lower.contains("farm") || lower.contains("kisan")) return "Agriculture";
        if (lower.contains("housing") || lower.contains("house") || lower.contains("awas")) return "Housing";
        if (lower.contains("employment") || lower.contains("job") || lower.contains("skill")) return "Employment";
        if (lower.contains("pension") || lower.contains("social security") || lower.contains("old age")) return "Social Security";
        if (lower.contains("women") || lower.contains("girl") || lower.contains("mahila")) return "Women Empowerment";
        if (lower.contains("insurance")) return "Insurance";
        return null;
    }

    private String extractSearchQuery(String message) {
        // Remove common intent words
        String cleaned = message.replaceAll("(?i)(show|find|search|schemes?|for|me|available|what|are|the|any|list|discover|recommend|suggest|my|can|i|apply|get)", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private Long extractSchemeId(String message) {
        var matcher = java.util.regex.Pattern.compile("scheme[\\s#]*(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(message);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    private CitizenProfileDto mapToDto(Map<String, Object> fields) {
        CitizenProfileDto dto = new CitizenProfileDto();
        fields.forEach((key, value) -> {
            switch (key) {
                case "age" -> dto.setAge((Integer) value);
                case "gender" -> dto.setGender((String) value);
                case "state" -> dto.setState((String) value);
                case "district" -> dto.setDistrict((String) value);
                case "occupation" -> dto.setOccupation((String) value);
                case "annualIncome" -> dto.setAnnualIncome(((Number) value).doubleValue());
                case "educationLevel" -> dto.setEducationLevel((String) value);
                case "category" -> dto.setCategory((String) value);
                case "disabilityStatus" -> dto.setDisabilityStatus((Boolean) value);
                case "landOwnership" -> dto.setLandOwnership((Boolean) value);
                case "employmentStatus" -> dto.setEmploymentStatus((String) value);
                case "residenceType" -> dto.setResidenceType((String) value);
                case "maritalStatus" -> dto.setMaritalStatus((String) value);
                case "bplStatus" -> dto.setBplStatus((Boolean) value);
                case "minorityStatus" -> dto.setMinorityStatus((Boolean) value);
                case "exServiceman" -> dto.setExServiceman((Boolean) value);
            }
        });
        return dto;
    }

    private String formatFieldName(String field) {
        StringBuilder sb = new StringBuilder();
        for (char c : field.toCharArray()) {
            if (Character.isUpperCase(c) && !sb.isEmpty()) sb.append(' ');
            sb.append(sb.isEmpty() ? Character.toUpperCase(c) : c);
        }
        return sb.toString();
    }

    private List<String> getMissingFields(CitizenProfileDto profile) {
        List<String> missing = new ArrayList<>();
        if (profile.getAge() == null) missing.add("age");
        if (profile.getState() == null) missing.add("state");
        if (profile.getAnnualIncome() == null) missing.add("annual income");
        if (profile.getOccupation() == null) missing.add("occupation");
        if (profile.getEducationLevel() == null) missing.add("education level");
        if (profile.getCategory() == null) missing.add("category (General/OBC/SC/ST/EWS)");
        if (profile.getEmploymentStatus() == null) missing.add("employment status");
        if (profile.getGender() == null) missing.add("gender");
        return missing;
    }
}
