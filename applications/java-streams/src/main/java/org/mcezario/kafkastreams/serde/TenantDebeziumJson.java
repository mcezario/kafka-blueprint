package org.mcezario.kafkastreams.serde;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties( ignoreUnknown = true )
public class TenantDebeziumJson {
    private TenantJson after;
    private TenantJson before;
    private String op;

    public TenantDebeziumJson(@JsonProperty( "after" ) TenantJson after,
                              @JsonProperty( "before" ) TenantJson before,
                              @JsonProperty( "op" ) String op) {
        this.after = after;
        this.before = before;
        this.op = op;
    }

    public TenantJson getAfter() {
        return after;
    }

    public TenantJson getBefore() {
        return before;
    }

    public String getOp() {
        return op;
    }
}