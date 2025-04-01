package org.mcezario.kafkastreams.serde;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TenantJson {
    private Long id;
    private String name;

    public TenantJson(@JsonProperty( "id" ) Long id,
                      @JsonProperty( "name" ) String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}