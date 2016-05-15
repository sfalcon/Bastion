package org.kpull.apitestsuites.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.kpull.apitestsuites.core.*;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:mail@kylepullicino.com">Kyle</a>
 */
public class ApiCallExecutor {

    private ApiEnvironment environment;
    private ApiCall apiCallToExecute;
    private ObjectMapper objectMapper;

    private ExecutionContext context;

    public ApiCallExecutor(ApiEnvironment environment, ApiCall apiCallToExecute, ObjectMapper objectMapper) {
        Objects.requireNonNull(environment);
        Objects.requireNonNull(apiCallToExecute);
        this.environment = environment;
        this.apiCallToExecute = apiCallToExecute;
        this.objectMapper = objectMapper;
    }

    public ApiEnvironment getEnvironment() {
        return environment;
    }

    public ApiCall getApiCallToExecute() {
        return apiCallToExecute;
    }

    private void initialiseContext() {
        context = new ExecutionContext();
        context.setEnvironment(environment);
        context.setCall(apiCallToExecute);
        context.setRequest(apiCallToExecute.getRequest());
    }

    public void execute() {
        try {
            initialiseContext();
            ApiRequest request = apiCallToExecute.getRequest();
            HttpRequest httpRequest = createHttpRequest(request);
            if (httpRequest instanceof HttpRequestWithBody) {
                ((HttpRequestWithBody) httpRequest).body(environment.process(request.getBody()));
            }
            request.getHeaders().forEach(header -> {
                httpRequest.header(header.getName(), environment.process(header.getValue()));
            });
            request.getQueryParams().forEach(queryParam -> {
                httpRequest.queryString(queryParam.getName(), environment.process(queryParam.getValue()));
            });
            HttpResponse<JsonNode> httpResponse = httpRequest.asJson();
            context.setHttpResponse(httpResponse);
            ApiResponse response = createAndSaveResponse(httpResponse);
            Object model = parseAndSaveModel(httpResponse);
            performAssertions(model);
            executePostCallScript();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private void executePostCallScript() {
        if (!Strings.isNullOrEmpty(apiCallToExecute.getPostCallScript())) {
            Binding binding = new Binding();
            binding.setVariable("context", context);
            binding.setVariable("apiCall", context.getCall());
            binding.setVariable("apiRequest", context.getRequest());
            binding.setVariable("httpRequest", context.getHttpRequest());
            binding.setVariable("apiResponse", context.getCall().getResponse());
            binding.setVariable("httpResponse", context.getHttpResponse());
            binding.setVariable("model", context.getResponseModel());
            binding.setVariable("environment", environment);
            GroovyShell groovy = new GroovyShell(binding);
            groovy.evaluate(apiCallToExecute.getPostCallScript());
        }
    }

    private void performAssertions(Object model) {
        apiCallToExecute.getAssertions().ifPresent(assertions -> {
            if (model == null) {
                throw new AssertionError("A null object was parsed from the API response");
            }
            assertions.assertions(model, context);
        });
    }

    private Object parseAndSaveModel(HttpResponse<JsonNode> httpResponse) {
        Object model = apiCallToExecute.getResponseModel().map(modelClass -> {
            try {
                Objects.requireNonNull(objectMapper, "Object Mapper must be set before we can deserialize to a model object");
                return objectMapper.reader(modelClass).readValue(httpResponse.getRawBody());
            } catch (IOException e) {
                throw new IllegalStateException("Could not deserialize JSON", e);
            }
        }).orElse(null);
        context.setResponseModel(model);
        return model;
    }

    private ApiResponse createAndSaveResponse(HttpResponse<JsonNode> httpResponse) {
        // TODO: Refine the next statement
        ApiResponse response = new ApiResponse(httpResponse.getHeaders().entrySet().stream().flatMap(header -> header.getValue().stream().map(value -> new ApiHeader(header.getKey(), value))).collect(Collectors.toList()),
                httpResponse.getStatus(), "application/json", httpResponse.getBody().toString());
        apiCallToExecute.setResponse(response);
        context.setResponse(response);
        return response;
    }

    private HttpRequest createHttpRequest(ApiRequest request) {
        HttpRequest httpRequest = null;
        switch (request.getMethod()) {
            case "POST":
                httpRequest = Unirest.post(environment.process(request.getUrl()));
                break;
            case "GET":
                httpRequest = Unirest.get(environment.process(request.getUrl()));
                break;
            case "PUT":
                httpRequest = Unirest.put(environment.process(request.getUrl()));
                break;
            case "DELETE":
                httpRequest = Unirest.delete(environment.process(request.getUrl()));
                break;
            case "OPTIONS":
                httpRequest = Unirest.options(environment.process(request.getUrl()));
                break;
            case "HEAD":
                httpRequest = Unirest.head(environment.process(request.getUrl()));
                break;
            case "PATCH":
                httpRequest = Unirest.patch(environment.process(request.getUrl()));
                break;
        }
        context.setHttpRequest(httpRequest);
        return httpRequest;
    }
}