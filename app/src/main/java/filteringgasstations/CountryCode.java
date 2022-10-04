package filteringgasstations;

public enum CountryCode {
    GER ("Germany"),
    AUT("Austria"),
    BEL ("Belgium"),
    CZE ("Czechia"),
    DNK ("Denmark"),
    FRA ("France"),
    LUX ("Luxembourg"),
    NLD ("Netherlands"),
    POL ("Poland"),
    CHE ("Switzerland");
    private final String name;

    public String getName() {
        return name;
    }
    private CountryCode(String name) {
        this.name = name;
    }
}
