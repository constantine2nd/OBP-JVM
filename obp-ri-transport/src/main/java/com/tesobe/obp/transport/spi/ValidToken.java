/*
 * Copyright (c) TESOBE Ltd.  2016. All rights reserved.
 *
 * Use of this source code is governed by a GNU AFFERO license that can be found in the LICENSE file.
 *
 */
package com.tesobe.obp.transport.spi;

import com.tesobe.obp.transport.Token;

import java.util.Optional;

/**
 * @since 2016.11
 */
@SuppressWarnings("WeakerAccess") public class ValidToken implements Token
{
  public ValidToken(String id)
  {
    this.id = id;
  }

  @Override public Optional<String> id()
  {
    return Optional.of(id);
  }

  @Override public Optional<String> error()
  {
    return Optional.empty();
  }

  @Override public boolean isValid()
  {
    return true;
  }

  final String id;
}
