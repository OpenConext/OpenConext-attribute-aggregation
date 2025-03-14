package aa.service;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * ServiceProviders in the SURFconext federation have an entity-id that by convention contains
 * a ':'. For example: https://sp.service.nl
 * <p/>
 * The corresponding OAuth2 client_id can not contain ':' because it is used in the Basic Authentication scheme
 * for requesting tokens.
 * <p/>
 * Therefore the client_id must be translated to a SP entity-id and vica-versa on order to fetch Aggregations.
 */
@Service
public class ServiceProviderTranslationService {

    public String translateServiceProviderEntityId(String entityId) {
        Assert.notNull(entityId, "EntityId is required");
        return entityId.replace("@", "@@").replaceAll(":", "@");
    }

    public String translateClientId(String clientId) {
        Assert.notNull(clientId, "ClientId is required");
        return clientId.replaceAll("(?<!@)@(?!@)", ":").replaceAll("@@", "@");
    }

}
