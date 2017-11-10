/*
 * github-users - lists GitHub users. Minimal app demonstrating
 * cross-platform app development (Web, Android, iOS) where core
 * logic is shared and transpiled from Java to JavaScript and
 * Objective-C. This project delivers core application logic.
 *
 * Copyright (C) 2017  Kazimierz Pogoda
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xemantic.githubusers.logic.test;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.concurrent.atomic.AtomicReference;

/**
 * JUnit rule checking non-handled exceptions coming from RxJava calls.
 *
 * @author morisil
 */
public class ExpectedRxJavaError implements TestRule {

  private final AtomicReference<Throwable> throwableRef = new AtomicReference<>();

  public static ExpectedRxJavaError none() {
    return new ExpectedRxJavaError();
  }

  public void expect(Class<? extends Throwable> type) {
    Throwable throwable = throwableRef.get();
    if (throwable == null) {
      Assert.fail("No exception was thrown");
    }
    Assert.assertThat(throwable, CoreMatchers.instanceOf(type));
  }

  public void expectMessage(String message) {
    Throwable throwable = throwableRef.get();
    if (throwable == null) {
      Assert.fail("No exception was thrown");
    }
    Assert.assertThat(throwable.getMessage(), CoreMatchers.is(message));
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        // we have to cache the current handler first before running the test
        Consumer<? super Throwable> handler = RxJavaPlugins.getErrorHandler();
        try {
          RxJavaPlugins.setErrorHandler(throwableRef::set);
          base.evaluate();
        } finally {
          // and restore it here
          RxJavaPlugins.setErrorHandler(handler);
        }
      }
    };
  }

}
