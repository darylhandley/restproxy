package com.dhandley.restproxy.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class QuickHttpTest {


  private String proxiedHost = "https://clm-staging.sonatype.com";




  public static void main(String[] args) throws Exception {
    HttpClient httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .build();

    URI proxiedURI = new URI("https://clm-staging.sonatype.com/rest/license");

    HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();

    HttpRequest.Builder builder = HttpRequest.newBuilder()
      .uri(proxiedURI)
      .method("GET", bodyPublisher);

    HttpRequest proxyRequest = builder.build();
    HttpResponse<String> proxiedResponse = httpClient.send(proxyRequest, HttpResponse.BodyHandlers.ofString());

    System.out.println(proxiedResponse.body());
  }
}
