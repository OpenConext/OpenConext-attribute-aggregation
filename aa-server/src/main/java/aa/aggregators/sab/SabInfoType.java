package aa.aggregators.sab;

public enum SabInfoType {

    ROLE("urn:mace:surfnet.nl:surfnet.nl:sab:role:"),
    ORGANIZATION("urn:mace:surfnet.nl:surfnet.nl:sab:organization:"),
    GUID("urn:mace:surfnet.nl:surfnet.nl:sab:guid:");

    private final String prefix;

    SabInfoType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
