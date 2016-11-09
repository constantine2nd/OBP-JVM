/*
 * Copyright (c) TESOBE Ltd.  2016. All rights reserved.
 *
 * Use of this source code is governed by a GNU AFFERO license that can be found in the LICENSE file.
 *
 */

package com.tesobe.obp.transport.spi;

import com.tesobe.obp.transport.Account;
import com.tesobe.obp.transport.Bank;
import com.tesobe.obp.transport.Connector;
import com.tesobe.obp.transport.Message;
import com.tesobe.obp.transport.Sender;
import com.tesobe.obp.transport.Transaction;
import com.tesobe.obp.transport.Transport;
import com.tesobe.obp.transport.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

import static com.tesobe.obp.util.MethodMatcher.optionallyReturns;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class ConnectorSep2016Test
{
  @Before public void defaultConnector()
  {
    Transport.Factory factory = Transport.defaultFactory();
    Receiver receiver = new MockReceiver(factory.codecs());
    final BlockingQueue<String> in = new SynchronousQueue<>();
    final BlockingQueue<Message> out = new SynchronousQueue<>();
    final Sender sender = request ->
    {
      out.put(request);

      return in.take();
    };

    // north: sender
    connector = factory.connector(sender);

    // south: receiver in a background thread
    service.submit(new Callable<Void>()
    {
      @Override @SuppressWarnings({"InfiniteLoopStatement"}) public Void call()
        throws InterruptedException
      {
        for(; ; )
        {
          in.put(receiver.respond(out.take()));
        }
      }
    });
  }


  @After public void shutdown()
  {
    service.shutdown();
  }

  @Test public void getAccount() throws Exception
  {
    String accountId = "account-x";
    String bankId = "bank-x";
    String userId = "user-x";

    Optional<Account> anonymous;
    Optional<Account> owned;

    anonymous = connector.getAccount(bankId, accountId);
    owned = connector.getAccount(bankId, accountId, userId);

    assertThat(anonymous, optionallyReturns("id", "account-x"));
    assertThat(owned, optionallyReturns("id", "account-x"));
  }

  @Test public void getAccounts() throws Exception
  {
    String bankId = "bank-x";
    String userId = "user-x";
    Iterable<? extends Account> anonymous;
    Iterable<? extends Account> owned;

    anonymous = connector.getAccounts(bankId);
    owned = connector.getAccounts(bankId, userId);

    assertThat(anonymous, notNullValue());
    assertThat(owned, notNullValue());

    anonymous.forEach(account ->
    {
      assertThat(account.bankId(), is(bankId));
      assertThat(account.id(), anyOf(is("id-1"), is("id-2")));
    });

    owned.forEach(account ->
    {
      assertThat(account.bankId(), is(bankId));
      assertThat(account.id(), anyOf(is("id-1"), is("id-2")));
    });
  }

  @Test public void getBank() throws Exception
  {
    String bankId = "bank-x";
    String userId = "user-x";

    Optional<Bank> anonymous;
    Optional<Bank> owned;

    try
    {
      anonymous = connector.getBank(bankId);
      owned = connector.getBank(bankId, userId);

      assertThat(anonymous, optionallyReturns("id", "bank-x"));
      assertThat(owned, optionallyReturns("id", "bank-x"));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  @Test public void getBanks() throws Exception
  {
    String userId = "user-x";

    Iterable<? extends Bank> anonymous;
    Iterable<? extends Bank> owned;

    anonymous = connector.getBanks();
    owned = connector.getBanks(userId);

    assertThat(anonymous, notNullValue());
    assertThat(owned, notNullValue());

    anonymous.forEach(bank ->
    {
      assertThat(bank.id(), anyOf(is("id-1"), is("id-2")));
    });

    owned.forEach(bank ->
    {
      assertThat(bank.id(), anyOf(is("id-1"), is("id-2")));
    });
  }

  @Test public void getTransaction() throws Exception
  {
    String accountId = "account-x";
    String bankId = "bank-x";
    String tid = "transaction-x";
    String userId = "user-x";

    Optional<Transaction> anonymous;
    Optional<Transaction> owned;

    anonymous = connector.getTransaction(bankId, accountId, tid);
    owned = connector.getTransaction(bankId, accountId, tid, userId);

    assertThat(anonymous, optionallyReturns("id", "transaction-x"));
    assertThat(owned, optionallyReturns("id", "transaction-x"));
  }

  @Test public void getTransactions() throws Exception
  {
    String accountId = "account-x";
    String bankId = "bank-x";
    String userId = "user-x";

    Iterable<? extends Transaction> anonymous;
    Iterable<? extends Transaction> owned;

    anonymous = connector.getTransactions(bankId, accountId);
    owned = connector.getTransactions(bankId, accountId, userId);

    assertThat(anonymous.iterator().next().id(), is("id-0"));
    assertThat(owned.iterator().next().id(), is("id-0"));
  }

//  @Test public void pageTransactions() throws Exception
//  {
//    String accountId = "account-x";
//    String bankId = "bank-x";
//    String userId = "user-x";
//    Connector.Pager pager = connector.pager(0, 3, null, null, null, null);
//
//    List<? extends Transaction> owned;
//
//    owned = pager.getTransactions(bankId, accountId, userId);
//
//    assertThat(pager.hasMorePages(), is(true));
//    assertThat(owned.size(), is(3));
//
//    pager = pager.nextPage();
//    owned = pager.getTransactions(bankId, accountId, userId);
//
//    assertThat(pager.hasMorePages(), is(false));
//    assertThat(owned.size(), is(1));
//  }

  @Test public void getUser() throws Exception
  {
    String userId = "user-x@example.org";

    Optional<User> user = connector.getUser(userId);

    assertThat(user, optionallyReturns("email", userId));
  }

  private Connector connector;
  private ExecutorService service = Executors.newCachedThreadPool();
}