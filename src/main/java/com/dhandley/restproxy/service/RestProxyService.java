package com.dhandley.restproxy.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class RestProxyService {

  private final HttpClient httpClient;

  private String proxiedHost = "https://clm-staging.sonatype.com";


  public RestProxyService() {
    httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .build();
  }

  public String getMessage() {
    return "Hello world";
  }

  public HttpServletResponse proxyRequest(HttpRequest request) {
    HttpClient client = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .build();


//    HttpResponse<String> response =
//      client.send(request, HttpResponse.BodyHandlers.ofString());

    return null;

//    HttpRequest myRequest = HttpRequest.newBuilder()
//      .uri(URI.create("http://openjdk.java.net/"))
//      .timeout(Duration.ofMinutes(1))
//      .header("Content-Type", "application/json")
//      .POST(BodyPublishers.ofFile(Paths.get("file.json")))
//      .build();






//    return "Hello world";
  }


  public ResponseEntity<String> proxyRequest(RequestEntity<String> request) {

    URI proxiedURI = getProxiedUri(request.getUrl());

    HttpRequest.BodyPublisher bodyPublisher = (request.getBody() == null) ?
      HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(request.getBody());

    HttpRequest.Builder builder = HttpRequest.newBuilder()
      .uri(proxiedURI)
      .method(request.getMethod().toString(), bodyPublisher);

    // transfer headers
    List<String> restrictedHeaders = List.of("host", "connection", "content-length", "accept-encoding");
    for (String key : request.getHeaders().keySet()) {
      if (!restrictedHeaders.contains(key)) {
        request.getHeaders().get(key).forEach(val -> builder.header(key, val));
      }
    }

    HttpRequest proxyRequest = builder.build();


    try {
      HttpResponse<String> proxiedResponse = httpClient.send(proxyRequest, HttpResponse.BodyHandlers.ofString());

      System.out.println(proxiedResponse.body());

      // convert proxied headers to returned headers
      HttpHeaders responseHeaders = new HttpHeaders();
      proxiedResponse.headers().map().entrySet().stream()
        .forEach(entry -> {
          entry.getValue().forEach(value -> responseHeaders.add(entry.getKey(), value));
        });

      return new ResponseEntity(
        proxiedResponse.body(),
        responseHeaders ,
        HttpStatus.valueOf(proxiedResponse.statusCode())
      );

    } catch (IOException | InterruptedException excp) {
      throw new RuntimeException(excp);
    }


  }

  private URI getProxiedUri(URI uri) {
    try {
      String url = uri.getRawPath() + "?" + uri.getRawQuery();
      URI proxiedUri = new URI(proxiedHost + url);
      return proxiedUri;
    } catch (URISyntaxException excp) {
      throw new RuntimeException(excp);
    }
  }



  private HttpRequest httpServletRequestToHttpRequest(HttpServletRequest request) {

    // convert headers to varchar
    String [] headers = {};
    // String [] headers = request.getHeaderNames();
//    for (String headerName : request.getHeaderNames().) {
//
//    }

    // headers, body, contenttype, url, ..
    HttpRequest httpRequest = HttpRequest.newBuilder()
      .headers(headers)
//      .uri(URI.create("http://openjdk.java.net/"))
//      .timeout(Duration.ofMinutes(1))
//      .header("Content-Type", "application/json")
//      .POST(BodyPublishers.ofFile(Paths.get("file.json")))
      .build();


    return httpRequest;


  }
}
