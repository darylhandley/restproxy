package com.dhandley.restproxy.controllers;

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.UUID;

import com.dhandley.restproxy.requestlogger.RequestLogger;
import com.dhandley.restproxy.service.RestProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  @Autowired
  private RequestLogger requestLogger;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @RequestMapping(value = "/**")
  public ResponseEntity<String> proxy(RequestEntity<String> request) {
    String requestData = UUID.randomUUID().toString() + " " + request.getMethod() + " " + request.getUrl().toString();
    log.info("Received Request " + requestData);
    ResponseEntity<String> response = restProxyService.proxyRequest(request);
    requestLogger.logRequestInfoToFile(request);
    log.info("Completed Request " + requestData);
    return response;
  }

}
