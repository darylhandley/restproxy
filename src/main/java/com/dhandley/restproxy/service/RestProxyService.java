package com.dhandley.restproxy.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.dhandley.restproxy.util.ProxyUrlUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RestProxyService {

  private final HttpClient httpClient;

  public static String PROXIED_HOST = "https://clm-staging.sonatype.com";


  public RestProxyService() {
    httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .build();
  }


  public ResponseEntity<String> proxyRequest(RequestEntity<String> request) {
    try {
      HttpRequest proxyRequest = requestEntityToHttpRequest(request);
      HttpResponse<String> proxiedResponse = httpClient.send(proxyRequest, HttpResponse.BodyHandlers.ofString());
      return httpResponseToResponseEntity(proxiedResponse);
    } catch (IOException | InterruptedException excp) {
      throw new RuntimeException(excp);
    }
  }

  private HttpRequest requestEntityToHttpRequest(RequestEntity<String> request) {
    URI proxiedURI = ProxyUrlUtil.proxyIt(request.getUrl(), PROXIED_HOST);

    HttpRequest.BodyPublisher bodyPublisher = (request.getBody() == null) ?
      HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(request.getBody());

    HttpRequest.Builder builder = HttpRequest.newBuilder()
      .uri(proxiedURI)
      .method(request.getMethod().toString(), bodyPublisher);

    // transfer headers
    // note that accept-encoding was removed so we don't have to handle unzipping, but we
    // might want to add it back later
    List<String> restrictedHeaders = List.of("host", "connection", "content-length", "accept-encoding");
    for (String key : request.getHeaders().keySet()) {
      if (!restrictedHeaders.contains(key)) {
        request.getHeaders().get(key).forEach(val -> builder.header(key, val));
      }
    }

    return builder.build();

  }


  private ResponseEntity<String> httpResponseToResponseEntity(HttpResponse httpResponse) {
    // System.out.println(httpResponse.body());

    // convert proxied headers to returned headers
    HttpHeaders responseHeaders = new HttpHeaders();
    httpResponse.headers().map().entrySet().stream()
      .forEach(entry -> {
        entry.getValue().forEach(value -> responseHeaders.add(entry.getKey(), value));
      });

    return new ResponseEntity(
      httpResponse.body(),
      responseHeaders ,
      HttpStatus.valueOf(httpResponse.statusCode())
    );
  }


}
