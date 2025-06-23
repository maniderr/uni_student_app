package com.lock.utils.enums;

import java.util.HashMap;
import java.util.Map;

public enum Faculty {
    AUTOMATICA("Automatica si Calculatoare"),
    ELECTRONICA("Electronica si Telecomunicatii");

    private final String displayName;
    private static final Map<Faculty, String[]> sections = new HashMap<>();

    static {
        sections.put(AUTOMATICA, new String[]{"IS", "CTI", "CTIeng"});
        sections.put(ELECTRONICA, new String[]{"ETCro", "ETCen"});
    }

    Faculty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getSections() {
        return sections.get(this);
    }
}
