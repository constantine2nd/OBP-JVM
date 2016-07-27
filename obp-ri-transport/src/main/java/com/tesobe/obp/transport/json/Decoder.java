/*
 * Copyright (c) TESOBE Ltd. 2016. All rights reserved.
 *
 * Use of this source code is governed by a GNU AFFERO license
 * that can be found in the LICENSE file.
 */
package com.tesobe.obp.transport.json;

import com.tesobe.obp.transport.Connector;
import com.tesobe.obp.transport.Transport;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

/**
 * Internal JSON decoder. Only called by trusted code.
 *
 * @since 2016.0
 */
@SuppressWarnings("WeakerAccess") public class Decoder
  implements com.tesobe.obp.transport.spi.Decoder
{
  public Decoder(Transport.Version v)
  {
    version = v;
  }

  @Override public Request request(String request)
  {
    return new Request()
    {
      @Override public boolean hasArguments()
      {
        return arguments != null;
      }

      @Override public String userId()
      {
        assert arguments != null;

        return arguments.optString("username", null);
      }

      /**
       * @return null if absent or without value
       */
      @Override public String name()
      {
        return name;
      }

      @Override public String raw()
      {
        return request;
      }

      JSONObject json = new JSONObject(request);
      JSONObject arguments;
      String name;

      {
        Iterator<String> keys = json.keys(); // Legacy api: only one key

        name = keys.hasNext() ? keys.next() : null;
        arguments = json.opt(name) instanceof JSONObject ? json
          .getJSONObject(name) : null;
      }
    };
  }

  @Override public Optional<Connector.Account> account(String response)
  {
    if(nonNull(response))
    {
      JSONObject a = new JSONObject(response);

      // @formatter:off
      return Optional.of(new Connector.Account(
        a.optString("id"),
        a.optString("bank"),
        a.optString("label"),
        a.optString("number"),
        a.optString("type"),
        a.optString("currency"),
        a.optString("amount"),
        a.optString("iban")));
      // @formatter:on
    }

    return Optional.empty();
  }

  @Override public Iterable<Connector.Bank> banks(String response)
  {
    List<Connector.Bank> result = new ArrayList<>();

    if(nonNull(response))
    {
      JSONArray array = new JSONArray(response);

      for(Object bank : array)
      {
        if(bank instanceof JSONObject)
        {
          JSONObject b = (JSONObject)bank;

          // @formatter:off
          result.add(new Connector.Bank(
            b.optString("id", null),
            b.optString("short_name", null),
            b.optString("full_name", null),
            b.optString("logo", null),
            b.optString("website", null)));
          // @formatter:on
        }
      }
    }

    return result;
  }

  /**
   * @return en empty iterable
   */
  @Override public Iterable<Connector.Bank> banks()
  {
    return Collections.emptyList();
  }

  @Override public String toString()
  {
    return getClass().getTypeName() + "-" + version;
  }

  final Transport.Version version;
  static final Logger log = LoggerFactory.getLogger(Encoder.class);
}
