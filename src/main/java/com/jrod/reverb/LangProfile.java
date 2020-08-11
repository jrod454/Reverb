package com.jrod.reverb;

import java.util.List;

public class LangProfile {
    String name;
    String code;
    List<String> wavenets;

    public LangProfile(String name, String code, List<String> wavenets) {
        this.name = name;
        this.code = code;
        this.wavenets = wavenets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getWavenets() {
        return wavenets;
    }

    public void setWavenets(List<String> wavenets) {
        this.wavenets = wavenets;
    }
}
