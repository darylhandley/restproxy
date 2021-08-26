package com.dhandley.restproxy.controllers;

import java.util.Map;

import com.dhandley.restproxy.requestlogger.RequestLogger;
import com.dhandley.restproxy.service.RestProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

  @Autowired
  private RestProxyService restProxyService;

  @Autowired
  private RequestLogger requestLogger;

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @RequestMapping(value = "/ping")
  public Map<String, String> ping() {
    return Map.of("response" , "pong");
  }

}
