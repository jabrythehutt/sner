package com.github.djabry.ner;

import lombok.*;

@Data
@Builder
public class EntityInfo
{
    private String name;
    private int count;
}
