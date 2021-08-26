package com.dhandley.restproxy.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Converts a URI to a different scheme, host and port
 */

public class ProxyUrlUtil {

  public static URI proxyIt(URI uri, String proxiedHost) {
    try {
      String url = uri.getRawPath()  + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
      return new URI(proxiedHost + url);
    } catch (URISyntaxException excp) {
      throw new RuntimeException(excp);
    }
  }
}
