package com.github.djabry.ner;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityExtractorTest {

    @Test
    void extractEntities() throws IOException {
        EntityExtractor entityExtractor = new EntityExtractor();
        String name1 = "Andrew Smith";
        String company1 = "Apple";
        ExtractEntitiesRequest request = ExtractEntitiesRequest.builder()
                .text(name1 + "'s shiny new " + company1 + " headphones").build();
        Map<String, List<EntityInfo>> entities = entityExtractor.extractEntities(request);

        Map<String, List<EntityInfo>> expectedEntities = new HashMap<>();
        expectedEntities.put("ORGANIZATION",
                Collections.singletonList(EntityInfo.builder().name(company1).count(1).build()));
        expectedEntities.put("PERSON",
                Collections.singletonList(EntityInfo.builder().name(name1).count(1).build()));

        assertEquals(entities.entrySet().size(), 2, "Should have found 2 types of entities");
        MapDifference<String, List<EntityInfo>> differences = Maps.difference(expectedEntities, entities);
        assertTrue(differences.areEqual(), "Should have produced the expected entities");
    }
}
