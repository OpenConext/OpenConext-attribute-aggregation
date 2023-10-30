package aa.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cache {

    private Boolean enabled;

    private String endpoint;

    private String refreshCron;

    private String requestMethod;

    private List<Header> headers;

    private String rootListName;

    private List<CacheFilter> filters;

}
