package aa.aggregators;

import aa.model.UserAttribute;

import java.util.List;
import java.util.Optional;

public interface AttributeAggregator {

  String getAttributeAuthorityId();

  List<UserAttribute> aggregate(List<UserAttribute> input);

  Optional<String> cacheKey(List<UserAttribute> input);

}
