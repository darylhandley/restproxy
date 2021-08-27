package com.dhandley.restproxy.requestlogger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dhandley.restproxy.service.RestProxyService;
import com.dhandley.restproxy.util.ProxyUrlUtil;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RequestLogger {

  private final File requestLogFile = new File("/Users/darylhandley/dev/projects/restproxy/requestLogFile.log");

  private final Logger log = LoggerFactory.getLogger(getClass());

  private List<Map> items = new ArrayList<>();

  private static String startDateString = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm").format(LocalDateTime.now());
  private static String postmanId = UUID.randomUUID().toString().substring(0,5);
  private final File swaggerFile = new File("/Users/darylhandley/dev/projects/restproxy/hdsSwagger_" + startDateString + ".json");


  public void logRequestInfoToFile(RequestEntity<String> request) {
    // skip noise (things that get auto uploaded)
    List<String> skipPatterns = ImmutableList.of(
        "user-telemetry"
    );
    boolean skip = skipPatterns.stream()
      .anyMatch(pattern -> request.getUrl().toString().contains(pattern));
    if (skip) {
      return;
    }

    URI proxiedUri =  ProxyUrlUtil.proxyIt(request.getUrl(), RestProxyService.PROXIED_HOST);
    Map<String, List<String>> headers = getHeadersFromRequest(request);
    String body = request.getBody(); // == null ? null : formatJsonWithGSON(request.getBody());
    String method = request.getMethod().toString();

    // create the swagggerItem and export to swagger file
    Map item = createItem(method, body, request, proxiedUri);
    items.add(item);
    writeSwaggerFile(items);




    StringBuilder sb = new StringBuilder();
    try {
      sb.append("-------------\n");
      sb.append("URI\n");
      sb.append("-------------\n");
      sb.append(method + " " + proxiedUri.toString() + "\n");

      sb.append("-------------\n");
      sb.append("Headers\n");
      sb.append("-------------\n");
      headers.entrySet().stream()
          .forEach(h -> {
            String paddedKey = Strings.padEnd(h.getKey(), 25, ' ');
            sb.append(paddedKey + " : " + h.getValue() + "\n");
          });

      sb.append("-------------\n");
      sb.append("Request Body\n");
      sb.append("-------------\n");
      if (body != null) {
        String formattedBody = formatJsonWithGSON(body);
        sb.append(formattedBody + "\n");
      } else {
        sb.append("No Body\n");
      }
      sb.append("====================================================================================\n");

    } catch (Exception excp) {
      sb.append("-------------\n");
      sb.append("error\n");
      sb.append("-------------\n");
      sb.append(excp.getMessage());
      log.error("An error occured", excp);
      sb.append("\n");

    }

    try {
      synchronized (requestLogFile) {
        Files.asCharSink(requestLogFile, Charsets.UTF_8).write(sb.toString());
      }
    } catch (Exception excp) {
      log.error("Unexpected exception when writing to file");
    }


  }

  private Map createItem(String method, String body, RequestEntity<String> request, URI proxiedUri) {

    String[] hostList = proxiedUri.getHost().split("\\.");
    String[] pathList = proxiedUri.getPath().split("\\/");
    String scheme = proxiedUri.getScheme();
    String port;
    if (proxiedUri.getPort() == -1) {
      port = scheme.equals("http") ? "80" : "443";
    } else {
      port = Integer.toString(proxiedUri.getPort());
    }

    String name = method + " " + proxiedUri.getPath();

    List<NameValuePair> queryParams = parseQueryParamsToNameValuePairs(proxiedUri);

    List<Map<String, String>> params = queryParams.stream()
      .filter(qp -> qp.value !=null) // temp need to possibly fix this
      .map(qp -> keyValueToMap(qp.name, qp.value))
      .collect(Collectors.toList());


    List<Map> event = ImmutableList.of(
        Map.of(
          "listen", "prerequest",
          "script", Map.of(
              "type" , "text/javascript",
              "exec" , ImmutableList.of("")

            )
        ),
        Map.of(
            "listen", "test",
            "script", Map.of(
                "type" , "text/javascript",
                "exec" , ImmutableList.of("")
            )
        )
    );

    /**
     * 	"event": [
     *                                {
     * 			"listen": "prerequest",
     * 			"script": {
     * 				"type": "text/javascript",
     * 				"exec": [
     * 					""
     * 				]
     *                        }
     *                },
     *                {
     * 			"listen": "test",
     * 			"script": {
     * 				"type": "text/javascript",
     * 				"exec": [
     * 					""
     * 				]
     *                        }
     *                }
     */


    /**
     * "url": {
     * 					"raw": "http://localhost:7213/rest/ci/componentDetails?componentIdentifier=%7B%22format%22%3A%22maven%22%2C%22coordinates%22%3A%7B%22artifactId%22%3A%22netty-codec-http%22%2C%22classifier%22%3A%22%22%2C%22extension%22%3A%22jar%22%2C%22groupId%22%3A%22io.netty%22%2C%22version%22%3A%224.1.43.Final%22%7D%7D&hash=07320f9db621dc51b798",
     * 					"protocol": "http",
     * 					"host": [
     * 						"localhost"
     * 					],
     * 					"port": "7213",
     * 					"path": [
     * 						"rest",
     * 						"ci",
     * 						"componentDetails"
     * 					],
     * 					"query": [
     *                        {
     * 							"key": "componentIdentifier",
     * 							"value": "%7B%22format%22%3A%22maven%22%2C%22coordinates%22%3A%7B%22artifactId%22%3A%22netty-codec-http%22%2C%22classifier%22%3A%22%22%2C%22extension%22%3A%22jar%22%2C%22groupId%22%3A%22io.netty%22%2C%22version%22%3A%224.1.43.Final%22%7D%7D"
     *            },
     *            {
     * 							"key": "hash",
     * 							"value": "07320f9db621dc51b798"
     *            }
     * 					]
     * 				}
     */

    /**
     * "header": [
     *                    {
     * 						"key": "firstHeader",
     * 						"value": "first",
     * 						"type": "text"
     *          },
     *          {
     * 						"key": "secondHeader",
     * 						"value": "second",
     * 						"type": "text"
     *          }
     * 				],
     */
    List<NameValuePair> headers = getHeadersFromRequestAsNameValuePair(request);
    List<String> headersToSkip = List.of("host");
    List<Map> headersJson = headers.stream()
      .filter(h -> !headersToSkip.contains(h.name))
      .map(h -> keyValueTypeToMap(h.name, h.value, "text"))
      .collect(Collectors.toList());


    Map jsonRequest  = Map.of(
        "method", method,
        "header", headersJson,
        "url", Map.of(
            "raw", proxiedUri.toString(),
            "protocol", scheme,
            "host", hostList,
            "port", port,
            "path", pathList,
            "query", params
        )
    );

    if (body != null) {
      jsonRequest =
          new ImmutableMap.Builder()
              .putAll(jsonRequest)
              .put("body",
                  Map.of(
                    "mode" , "raw",
                    "raw", body
                  )
              )
              .build();
    }

    return
        Map.of(
            "name", name,
            "request",jsonRequest
        );

  }


  private List<NameValuePair> parseQueryParamsToNameValuePairs(URI uri) {
    MultiValueMap<String, String> params =
      UriComponentsBuilder.fromUri(uri).build().getQueryParams();

    List<NameValuePair> nameValuePairs = new ArrayList<>();

    params.entrySet().forEach(param -> {
      param.getValue().forEach(valueInList -> {
        nameValuePairs.add(new NameValuePair(param.getKey(), valueInList));
      });
    });

    return nameValuePairs;

  }

  private Map<String, String> keyValueToMap(String key, String value) {
    return Map.of(
      "key", key,
      "value", value
    );

  }

  private Map<String, String> keyValueTypeToMap(String key, String value, String type) {
    return Map.of(
      "key", key,
      "value", value,
      "type", type
    );
  }


  private synchronized void writeSwaggerFile(List<Map> items) {

    // create map containing our data
    Map swaggerData = Map.of(
      "info", Map.of(
        "_postman_id", postmanId,
        "name", "HDS Daryl " + startDateString,
        "schema",  "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
      ),
      "item", items,
      "auth", Map.of(
        "type", Map.of(
          "apikey", ImmutableList.of(
            Map.of(
              "key", "value",
              "value", "17a838094e43cb6a64e073c43279c60382b437aa",
              "type",  "string"
            ),
            Map.of(
              "key", "key",
              "value", "X-CLM-Token",
              "type",  "string"
            )
          )
        )
      )
    );

    // pretty it up and send to file
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      String jsonSwagger = gson.toJson(swaggerData);
      Files.asCharSink(swaggerFile, Charsets.UTF_8).write(jsonSwagger);
    } catch (IOException excp) {
      throw new RuntimeException(excp);
    }

  }

  private String formatJsonWithGSON(String uglyJson) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    JsonElement je = JsonParser.parseString(uglyJson);
    return gson.toJson(je);
  }


  private Map<String, List<String>> getHeadersFromRequest(RequestEntity<String> request) {
    Map<String, List<String>> result = new HashMap<>();
    request.getHeaders().keySet().forEach(key ->
      result.put(key, request.getHeaders().get(key))
    );
    return result;
  }

  private List<NameValuePair> getHeadersFromRequestAsNameValuePair(RequestEntity<String> request) {

    List<NameValuePair> nameValuePairs = new ArrayList<>();

    request.getHeaders().entrySet().forEach(header -> {
      header.getValue().forEach(valueInList -> {
        nameValuePairs.add(new NameValuePair(header.getKey(), valueInList));
      });
    });

    return nameValuePairs;
  }



  private class NameValuePair {
    NameValuePair(String name, String value) {
      this.name = name;
      this.value = value;
    }
    private String name;
    private String value;
  }

}
