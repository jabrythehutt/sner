package com.github.djabry.sner;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtractEntitiesRequest {
    public String text;
}
