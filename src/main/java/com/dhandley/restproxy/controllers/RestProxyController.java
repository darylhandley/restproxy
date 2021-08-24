package com.dhandley.restproxy.controllers;

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import com.dhandley.restproxy.service.RestProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestProxyController {

  @Autowired
  private RestProxyService restProxyService;

  // @GetMapping("/**")
  @RequestMapping(value = "/**")
  public ResponseEntity<String> proxy(RequestEntity<String> request) {
    ResponseEntity<String> response = restProxyService.proxyRequest(request);
    return response;
  }

}
