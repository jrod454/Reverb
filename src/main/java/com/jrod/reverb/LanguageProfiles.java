package com.jrod.reverb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LanguageProfiles {
    public List<LangProfile> profiles;

    private String US_NAME = "English (US)";
    private String US_CODE = "en-US";
    private String[] US_WAVENETS = {
            "en-US-Wavenet-A",
            "en-US-Wavenet-B",
            "en-US-Wavenet-C",
            "en-US-Wavenet-D",
            "en-US-Wavenet-E",
            "en-US-Wavenet-F"
    };
    private String IN_NAME = "English (India)";
    private String IN_CODE = "en-IN";
    private String[] IN_WAVENETS = {
            "en-IN-Wavenet-A",
            "en-IN-Wavenet-B",
            "en-IN-Wavenet-C",
            "en-IN-Wavenet-D"
    };
    private String GB_NAME = "English (Great Britain)";
    private String GB_CODE = "en-GB";
    private String[] GB_WAVENETS = {
            "en-GB-Wavenet-A",
            "en-GB-Wavenet-B",
            "en-GB-Wavenet-C",
            "en-GB-Wavenet-D",
            "en-GB-Wavenet-F"
    };
    private String AU_NAME = "English (Australia)";
    private String AU_CODE = "en-AU";
    private String[] AU_WAVENETS = {
            "en-AU-Wavenet-A",
            "en-AU-Wavenet-B",
            "en-AU-Wavenet-C",
            "en-AU-Wavenet-D"
    };
    private String FR_NAME = "French (France)";
    private String FR_CODE = "fr-FR";
    private String[] FR_WAVENETS = {
            "fr-FR-Wavenet-A",
            "fr-FR-Wavenet-B",
            "fr-FR-Wavenet-C",
            "fr-FR-Wavenet-D",
            "fr-FR-Wavenet-E"
    };
    private String RU_NAME = "Russian (Russia)";
    private String RU_CODE = "ru-RU";
    private String[] RU_WAVENETS = {
            "ru-RU-Wavenet-A",
            "ru-RU-Wavenet-B",
            "ru-RU-Wavenet-C",
            "ru-RU-Wavenet-D",
            "ru-RU-Wavenet-E"
    };
    private String JP_NAME = "Japanese (Japan)";
    private String JP_CODE = "ja-JP";
    private String[] JP_WAVENETS = {
            "ja-JP-Wavenet-A",
            "ja-JP-Wavenet-B",
            "ja-JP-Wavenet-C",
            "ja-JP-Wavenet-D"
    };
    private String KR_NAME = "Korean (South Korea)";
    private String KR_CODE = "ko-KR";
    private String[] KR_WAVENETS = {
            "ko-KR-Wavenet-A",
            "ko-KR-Wavenet-B",
            "ko-KR-Wavenet-C",
            "ko-KR-Wavenet-D"
    };

    public LanguageProfiles() {
        this.profiles = new ArrayList<>();
        this.profiles.add(new LangProfile(US_NAME, US_CODE, Arrays.asList(US_WAVENETS)));
        this.profiles.add(new LangProfile(IN_NAME, IN_CODE, Arrays.asList(IN_WAVENETS)));
        this.profiles.add(new LangProfile(GB_NAME, GB_CODE, Arrays.asList(GB_WAVENETS)));
        this.profiles.add(new LangProfile(AU_NAME, AU_CODE, Arrays.asList(AU_WAVENETS)));
        this.profiles.add(new LangProfile(FR_NAME, FR_CODE, Arrays.asList(FR_WAVENETS)));
        this.profiles.add(new LangProfile(RU_NAME, RU_CODE, Arrays.asList(RU_WAVENETS)));
        this.profiles.add(new LangProfile(JP_NAME, JP_CODE, Arrays.asList(JP_WAVENETS)));
        this.profiles.add(new LangProfile(KR_NAME, KR_CODE, Arrays.asList(KR_WAVENETS)));
    }

    public List<String> getNames() {
        return profiles.stream().map(LangProfile::getName).collect(Collectors.toList());
    }

    public List<String> getWavenetsForName(String name) {
        return profiles.stream().filter(langProfile -> langProfile.name.equals(name)).findFirst().get().wavenets;
    }

    public String getCodeForName(String name) {
        return profiles.stream().filter(langProfile -> langProfile.name.equals(name)).findFirst().get().code;
    }
}
