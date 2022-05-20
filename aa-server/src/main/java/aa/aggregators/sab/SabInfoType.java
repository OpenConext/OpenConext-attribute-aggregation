package aa.aggregators.sab;

public enum SabInfoType {

    ROLE(
        "urn:mace:surfnet.nl:surfnet.nl:sab:role:",
        "urn:oid:1.3.6.1.4.1.5923.1.1.1.7"),

    ORGANIZATION(
        "urn:mace:surfnet.nl:surfnet.nl:sab:organizationCode:",
        "urn:oid:1.3.6.1.4.1.1076.20.100.10.50.1"),

    GUID(
        "urn:mace:surfnet.nl:surfnet.nl:sab:organizationGUID:",
        "urn:oid:1.3.6.1.4.1.1076.20.100.10.50.2"),

    MOBILE(
        "urn:mace:surfnet.nl:surfnet.nl:sab:mobile:",
        "urn:oid:1.3.6.1.4.1.1076.20.100.10.50.4");

    private final String prefix;
    private final String urn;

    SabInfoType(String prefix, String urn) {
        this.prefix = prefix;
        this.urn = urn;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUrn() {
        return urn;
    }
}


