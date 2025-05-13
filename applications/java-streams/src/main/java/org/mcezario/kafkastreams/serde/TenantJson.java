package org.mcezario.kafkastreams.serde;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties( ignoreUnknown = true )
public class TenantJson {
    private Long id;
    private String name;
    private Instant createdAt;

    public TenantJson(@JsonProperty( "id" ) Long id,
                      @JsonProperty( "name" ) String name,
                      @JsonProperty( "create_at" ) Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}