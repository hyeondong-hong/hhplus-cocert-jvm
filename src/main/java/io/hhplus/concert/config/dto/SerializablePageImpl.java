package io.hhplus.concert.config.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable"})
public class SerializablePageImpl<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public SerializablePageImpl(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int pageNumber,
            @JsonProperty("size") int pageSize,
            @JsonProperty("totalElements") long total
    ) {
        super(content, PageRequest.of(pageNumber, Math.max(pageSize, 1)), total);
    }

    public SerializablePageImpl(Page<T> page) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
    }
}
