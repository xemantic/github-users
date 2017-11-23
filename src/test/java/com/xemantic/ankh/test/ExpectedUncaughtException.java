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

package com.xemantic.ankh.test;

import com.google.common.base.Preconditions;
import com.xemantic.ankh.shared.error.Errors;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JUnit rule checking non-handled exceptions caught by
 * {@link Thread.UncaughtExceptionHandler}.
 * <p>
 * Note: it will also temporally rewire
 * {@link RxJavaPlugins#setErrorHandler(Consumer)},
 * otherwise RxJava would print stack traces on {@code System.err}
 * polluting the output of our test cases.
 * </p>
 *
 * @author morisil
 */
public class ExpectedUncaughtException implements TestRule {

  private final AtomicReference<Throwable> throwableRef = new AtomicReference<>();

  private Class<? extends Throwable> expectedType = null;

  private String expectedMessage = null;

  public static ExpectedUncaughtException none() {
    return new ExpectedUncaughtException();
  }

  public void expect(Class<? extends Throwable> type) {
    Preconditions.checkState(
        expectedType == null,
        "Already expecting: " + expectedType
    );
    expectedType = Objects.requireNonNull(type);
  }

  public void expectMessage(String message) {
    Preconditions.checkState(
        expectedMessage == null,
        "Already expecting message: " + expectedMessage
    );
    expectedMessage = Objects.requireNonNull(message);
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        // we have to cache the current handler first before running the test
        Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
            throwableRef.set(throwable)
        );
        Consumer<? super Throwable> oldRxJavaHandler = RxJavaPlugins.getErrorHandler();
        RxJavaPlugins.setErrorHandler(Errors::onError);
        try {
          base.evaluate();
        } finally {
          // and restore cached handlers here
          Thread.setDefaultUncaughtExceptionHandler(oldHandler);
          RxJavaPlugins.setErrorHandler(oldRxJavaHandler);
          verify();
          // Some other exception might have happened on base.evaluate(), but if it is so,
          // it might be a side consequence of uncaught exception discovered by this
          // rule. For this reason we are are verifying in finally, if this rule will
          // report failure, then an exception thrown in base.evaluate() will be suppressed.
          // It will be reported eventually once the original problem with expected or
          // unexpected uncaught exception is resolved.
        }
      }
    };
  }

  private void verify() {
    Throwable throwable = throwableRef.get();
    if (throwable == null) {
      verifyUncaughtErrorNotExpected();
    } else {
      verifyUncaughtErrorMatchesExpectations(throwable);
    }
  }

  private void verifyUncaughtErrorNotExpected() {
    if (expectedType != null) {
      Assert.fail(
          "No uncaught exception occurred:\n" +
              "Expected: <" + expectedType.getName() + ">"
      );
    }
    if (expectedMessage != null) {
      Assert.fail(
          "No uncaught exception occurred, " +
              "but expected one with message: \n" +
              "Expected: \"" + expectedMessage +"\""
      );
    }
  }

  private void verifyUncaughtErrorMatchesExpectations(Throwable throwable) {
    if ((expectedType == null) && (expectedMessage == null)) {
      throw new AssertionError("Unexpected uncaught exception", throwable);
    } else if ((expectedType != null) && (expectedType != throwable.getClass())) {
      throw new AssertionError(
          "Uncaught exception occurred, but is different than expected:\n" +
              "Expected: <" + expectedType.getName() +">\n" +
              "     but: was <" + throwable.getClass().getName() + ">",
          throwable
      );
    } else if ((expectedMessage != null) && (!expectedMessage.equals(throwable.getMessage()))) {
      throw new AssertionError(
          "Expected uncaught exception " + expectedType.getName() + " occurred " +
              "but has unexpected message:\n" +
              "Expected: \"" + expectedMessage + "\"\n" +
              "     but: was \"" + throwable.getMessage() + "\"",
          throwable
      );
    }
  }

}
