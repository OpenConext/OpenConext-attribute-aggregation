package aa.model;

import java.util.List;

public class UserAttributes {

    private String serviceProviderEntityId;
    private List<UserAttribute> attributes;

    public UserAttributes() {
    }

    public UserAttributes(String serviceProviderEntityId, List<UserAttribute> attributes) {
        this.serviceProviderEntityId = serviceProviderEntityId;
        this.attributes = attributes;
    }

    public String getServiceProviderEntityId() {
        return serviceProviderEntityId;
    }

    public void setServiceProviderEntityId(String serviceProviderEntityId) {
        this.serviceProviderEntityId = serviceProviderEntityId;
    }

    public List<UserAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<UserAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "UserAttributes{" +
            "serviceProviderEntityId='" + getServiceProviderEntityId() + '\'' +
            ", attributes=" + getAttributes() +
            '}';
    }
}
