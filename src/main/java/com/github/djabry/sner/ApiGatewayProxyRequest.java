package com.github.djabry.sner;

import com.amazonaws.services.lambda.runtime.Context;
import lombok.Data;

import java.util.Map;

@Data
public class ApiGatewayProxyRequest {
    private String resource;
    private String path;
    private String httpMethod;
    private Map<String, String> headers;
    private Map<String, String> queryStringParameters;
    private Map<String, String> pathParameters;
    private Map<String, String> stageVariables;
    private Context context;
    private String body;
    private Boolean isBase64Encoded;
}
