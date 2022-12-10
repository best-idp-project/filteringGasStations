package filteringgasstations.geolocation;

/**
 * Enum of all country code to simplify the reading / writing of stations
 */
public enum CountryCode {
    GER("Germany", "DE"),
    AUT("Austria", "AT"),
    BEL("Belgium", "BE"),
    CZE("Czechia", "CZ"),
    DNK("Denmark", "DK"),
    FRA("France", "FR"),
    LUX("Luxembourg", "LU"),
    NLD("Netherlands", "NL"),
    POL("Poland", "PL");
    private final String name;
    private final String code;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    CountryCode(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static CountryCode findByCode(String code) {
        for (CountryCode v : values()) {
            if (v.code.equals(code)) {
                return v;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code;
    }
}
