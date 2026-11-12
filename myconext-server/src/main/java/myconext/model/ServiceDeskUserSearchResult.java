package myconext.model;

import java.util.List;

public record ServiceDeskUserSearchResult(List<ServiceDeskUser> users,
                                          long total,
                                          int page,
                                          int pageSize) {
}
