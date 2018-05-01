package com.github.djabry.ner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiHandler implements RequestHandler<ApiGatewayProxyRequest, ApiGatewayResponse> {

    private static final Logger LOG = Logger.getLogger(ApiHandler.class);
    private EntityExtractor entityExtractor;
    public ApiHandler() {
        this.entityExtractor = new EntityExtractor();
    }

    @Override
    public ApiGatewayResponse handleRequest(ApiGatewayProxyRequest input, Context context) {
        LOG.info("received: " + input);
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        String dataContentType = "application/json";
        headers.put("Content-Type", dataContentType);
        String errorContentType = "text/html";
        try {

            Map<String, String> queryStringParameters = input.getQueryStringParameters();
            ExtractEntitiesRequest extractEntitiesRequest;
            if(queryStringParameters != null && queryStringParameters.containsKey("text")) {
                extractEntitiesRequest = ExtractEntitiesRequest.builder()
                        .text(queryStringParameters.get("text")).build();
            } else {
                Gson gson = new Gson();
                String body = input.getBody();
                extractEntitiesRequest = gson.fromJson(body, ExtractEntitiesRequest.class);
            }
            Map<String, List<EntityInfo>> entities = this.entityExtractor
                    .extractEntities(extractEntitiesRequest);
            LOG.info("Found entities: " + entities);
            return ApiGatewayResponse.builder()
                    .setStatusCode(200)
                    .setObjectBody(entities)
                    .setHeaders(headers)
                    .build();
        } catch (IOException e) {
            LOG.error(e);
            headers.put("Content-Type", errorContentType);
            return ApiGatewayResponse.builder()
                    .setStatusCode(500)
                    .setObjectBody("Failed to read model file")
                    .setHeaders(headers)
                    .build();
        }

    }
}