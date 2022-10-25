package filteringgasstations;

public enum CountryCode {
    GER ("Germany", "DE"),
    AUT("Austria", "AT"),
    BEL ("Belgium", "BE"),
    CZE ("Czechia", "CZ"),
    DNK ("Denmark", "DK"),
    FRA ("France", "FR"),
    LUX ("Luxembourg", "LU"),
    NLD ("Netherlands", "NL"),
    POL ("Poland", "PL"),
    CHE ("Switzerland", "CH");
    private final String name;
    private final String code;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    private CountryCode(String name, String code) {
        this.name = name; this.code = code;
    }
}
