package com.dhandley.restproxy.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.dhandley.restproxy.util.ProxyUrlUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RestProxyService {

  private final HttpClient httpClient;

  private final OkHttpClient client = new OkHttpClient();

  public static final String PROXIED_HOST = "https://clm-staging.sonatype.com";


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


  public ResponseEntity<String> proxyRequestOkHttp(RequestEntity<String> request) {
    try {
      Request proxyRequest = requestEntityToOkHttpRequest(request);
      try (Response proxiedResponse = client.newCall(proxyRequest).execute()) {
        return okHttpResponseToResponseEntity(proxiedResponse);
      }
    } catch (IOException excp) {
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

  private Request requestEntityToOkHttpRequest(RequestEntity<String> request) {
    try {
      URI proxiedURI = ProxyUrlUtil.proxyIt(request.getUrl(), PROXIED_HOST);


      Request.Builder builder = new Request.Builder();
      builder.url(proxiedURI.toURL());
      // MediaType mediaType = MediaType.get(request.getType().toString());
      MediaType mediaType = null; // MediaType.get(request.getType().toString());
      if (request.getBody() != null) {
        RequestBody body = RequestBody.create(request.getBody(), mediaType);
        builder.method(request.getMethod().toString(), body);
      } else {
        builder.method(request.getMethod().toString(), null);
      }



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
    } catch (MalformedURLException excp) {
      throw new RuntimeException(excp);
    }

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


  private ResponseEntity<String> okHttpResponseToResponseEntity(Response httpResponse) {
    // System.out.println(httpResponse.body());

    try {

      // convert proxied headers to returned headers
      HttpHeaders responseHeaders = new HttpHeaders();
      httpResponse.headers().toMultimap().entrySet().stream()
        .forEach(entry -> {
          entry.getValue().forEach(value -> responseHeaders.add(entry.getKey(), value));
        });

//    String body = httpResponse.body().string();

      return new ResponseEntity(
        httpResponse.body().string(),
        responseHeaders,
        HttpStatus.valueOf(httpResponse.code())
      );
    } catch (IOException excp) {
      throw new RuntimeException(excp);
    }
  }


}
