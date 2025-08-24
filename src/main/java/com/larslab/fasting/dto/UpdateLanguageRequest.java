package com.larslab.fasting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update user language")
public class UpdateLanguageRequest {
    
    @NotBlank(message = "Language is required")
    @Pattern(regexp = "en|de", message = "Language must be 'en' or 'de'")
    @Schema(description = "User interface language", example = "en", allowableValues = {"en", "de"}, required = true)
    private String language;
    
    public UpdateLanguageRequest() {}
    
    public UpdateLanguageRequest(String language) {
        this.language = language;
    }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
