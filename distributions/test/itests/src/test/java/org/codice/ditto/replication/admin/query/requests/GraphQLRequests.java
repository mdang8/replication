/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ditto.replication.admin.query.requests;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.fail;

import com.jayway.restassured.response.ExtractableResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.boon.Boon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to facilitate sending GraphQL requests. Takes advantage of GraphQL variables to support
 * graphql files that hold queries.
 */
public class GraphQLRequests {

  private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLRequests.class);

  public static final String USERNAME = "admin";

  @SuppressWarnings("squid:S2068" /* Password used internally */)
  public static final String PASSWORD = "admin";

  private Class resourceClass;

  private String queryResourcePath;

  private String mutationResourcePath;

  private String graphQlEndpoint;

  public GraphQLRequests(
      Class resourceClass,
      String queryResourcePath,
      String mutationResourcePath,
      String graphQlEndpoint) {
    this.resourceClass = resourceClass;
    this.queryResourcePath = queryResourcePath;
    this.mutationResourcePath = mutationResourcePath;
    this.graphQlEndpoint = graphQlEndpoint;
  }

  public GraphQLRequest createRequest() {
    return new GraphQLRequest(queryResourcePath, mutationResourcePath, graphQlEndpoint);
  }

  public static boolean responseHasErrors(ExtractableResponse response) {
    return response != null && response.jsonPath().get("errors") != null;
  }

  public static <T> T extractResponse(ExtractableResponse response, String jsonPath) {
    return response.jsonPath().get(jsonPath);
  }

  public class GraphQLRequest {
    private String queryResourcePath;

    private String mutationResourcePath;

    private String mutationFile;

    private String queryFile;

    private String graphQlEndpoint;

    private Map<String, Object> reqArgs;

    private ExtractableResponse response;

    public GraphQLRequest(
        String queryResourcePath, String mutationResourcePath, String graphQlEndpoint) {
      this.queryResourcePath = queryResourcePath;
      this.mutationResourcePath = mutationResourcePath;
      this.graphQlEndpoint = graphQlEndpoint;
      this.reqArgs = new HashMap<>();
    }

    public GraphQLRequest addArgument(String argName, Object argValue) {
      reqArgs.put(argName, argValue);
      return this;
    }

    public GraphQLRequest addArguments(Map<String, Object> args) {
      for (Map.Entry<String, Object> arg : args.entrySet()) {
        addArgument(arg.getKey(), arg.getValue());
      }
      return this;
    }

    public GraphQLRequest usingQuery(String queryFileName) {
      this.queryFile = queryFileName;
      return this;
    }

    public GraphQLRequest usingMutation(String mutationFileName) {
      this.mutationFile = mutationFileName;
      return this;
    }

    private String getResourceAsString(String filePath) {
      try {
        return IOUtils.toString(resourceClass.getResourceAsStream(filePath), "UTF-8");
      } catch (IOException e) {
        fail("Unable to retrieve resource: " + filePath);
      }

      return null;
    }

    public GraphQLRequest send() {
      Map<String, String> query = new HashMap<>();

      String queryBody = null;

      if (queryFile != null) {
        queryBody = getResourceAsString(queryResourcePath + queryFile);
      } else if (mutationFile != null) {
        queryBody = getResourceAsString(mutationResourcePath + mutationFile);
      } else {
        fail(
            "Failed to send GraphQLRequest. A query or mutation file must be specified before attempting to send the request.");
      }
      query.put("query", queryBody);

      if (!reqArgs.isEmpty()) {
        query.put("variables", Boon.toJson(reqArgs));
      }

      String queryStr = Boon.toPrettyJson(query);
      LOGGER.debug("\nSending request:\n{}", queryStr);

      response =
          given()
              .header("Content-Type", "application/json")
              .relaxedHTTPSValidation()
              .auth()
              .preemptive()
              .basic(USERNAME, PASSWORD)
              .header("X-Requested-With", "XMLHttpRequest")
              .body(queryStr)
              .post(graphQlEndpoint)
              .then()
              .extract();

      String responseBody = response.body().asString();
      LOGGER.debug("\nReplied with:\n{}", responseBody);

      return this;
    }

    public ExtractableResponse getResponse() {
      return response;
    }
  }
}
