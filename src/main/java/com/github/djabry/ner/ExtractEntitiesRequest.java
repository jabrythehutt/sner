package com.github.djabry.ner;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExtractEntitiesRequest {
    public String text;
}
