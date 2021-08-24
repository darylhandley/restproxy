package com.dhandley.restproxy.service;


import com.dhandley.restproxy.RestProxyApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RestProxyService .class})
public class RestProxyServiceTest {

  @Autowired
  private RestProxyService restProxyService;

  @Test
  void testHttpServletRequestToHttpRequest() {

    // restProxyService.httpServletRequestToHttpRequest()
    assertThat(restProxyService, is(notNullValue()));
  }
}
