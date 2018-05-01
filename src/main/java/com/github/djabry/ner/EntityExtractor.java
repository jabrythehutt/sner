package com.github.djabry.ner;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.NERClassifierCombiner;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import one.util.streamex.StreamEx;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class EntityExtractor {
    private static final Logger LOG = Logger.getLogger(EntityExtractor.class);
    private AbstractSequenceClassifier<CoreLabel> classifier;
    private Properties properties;
    public EntityExtractor() {

    }

    private Properties getProperties() throws IOException {
        if(this.properties == null) {
            InputStream is = this.getClass().getResourceAsStream("/ner.properties");
            this.properties = new Properties();
            this.properties.load(is);
        }
        return this.properties;

    }

    private String getNerPrefix() throws IOException {
        Properties props = this.getProperties();
        return props.getProperty("ner.prefix");
    }

    private List<String> getClassifierNames() throws IOException {
        Properties props = this.getProperties();
        return props.stringPropertyNames()
                .stream()
                .filter(propertyName -> propertyName.startsWith("ner.model"))
                .map(props::getProperty)
                .collect(Collectors.toList());
    }

    private String getClassifierPath(String prefix, String classifierName) {
        return prefix + classifierName + ".crf.ser.gz";
    }


    private List<List<CoreLabel>> extractAnnotations(String text) throws IOException {
        return this.getClassifier().classify(text);
    }

    private boolean siblings(CoreLabel label1, CoreLabel label2) {
        int label1Position = Integer.parseInt(label1.getString(CoreAnnotations.PositionAnnotation.class));
        int label2Position = Integer.parseInt(label2.getString(CoreAnnotations.PositionAnnotation.class));
        return label1Position == label2Position - 1 || label1Position == label1Position + 1;
    }

    private String toName(List<CoreLabel> labels) {
        return labels.stream()
                .map(label -> label.getString(CoreAnnotations.ValueAnnotation.class))
                .collect(Collectors.joining(" "));
    }

    private AbstractSequenceClassifier<CoreLabel> getClassifier() throws IOException {

        if(this.classifier == null) {
            List<String> classifierNames = this.getClassifierNames();
            String prefix = this.getNerPrefix();
            String[] classifierPaths = classifierNames.stream()
                    .map(classifierName -> this.getClassifierPath(prefix, classifierName))
                    .toArray(String[]::new);
            this.classifier = new NERClassifierCombiner(true, false, false, classifierPaths);
        }
        return this.classifier;
    }

    Map<String, List<EntityInfo>> extractEntities(ExtractEntitiesRequest request) throws IOException {

        List<List<CoreLabel>> annotations = extractAnnotations(request.text);
        Map<String, Map<String, EntityInfo>> entitiesMap  = new HashMap<>();
        annotations.forEach(annotationList -> {
            annotationList.stream()
                    .filter(annotation ->
                            !annotation.getString(CoreAnnotations.AnswerAnnotation.class).equals("O"))
                    .collect(Collectors.groupingBy(annotation -> annotation
                            .getString(CoreAnnotations.AnswerAnnotation.class)))
                    .forEach((entityType, labels) -> StreamEx.of(labels).groupRuns(this::siblings).toList()
                            .forEach(entityList -> {
                                if (!entitiesMap.containsKey(entityType)) {
                                    entitiesMap.put(entityType, new HashMap<String, EntityInfo>());
                                }
                                String entityName = toName(entityList);
                                Map<String, EntityInfo> entityInfoMap = entitiesMap.get(entityType);
                                if (!entityInfoMap.containsKey(entityName)) {
                                    EntityInfo entityInfo = EntityInfo.builder()
                                            .name(entityName)
                                            .count(0)
                                            .build();
                                    entityInfoMap.put(entityName, entityInfo);
                                }
                                EntityInfo entityInfo = entityInfoMap.get(entityName);
                                entityInfo.setCount(entityInfo.getCount() + 1);

                            }));

        });

        return entitiesMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> new ArrayList<>(entry.getValue().values())));



    }



}
