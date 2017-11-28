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
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
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

  private final AtomicReference<Throwable> uncaughtRef = new AtomicReference<>();

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
            uncaughtRef.set(throwable)
        );
        Consumer<? super Throwable> oldRxJavaHandler = RxJavaPlugins.getErrorHandler();
        RxJavaPlugins.setErrorHandler(Errors::onError);
        Throwable error = null;
        try {
          base.evaluate();
        } catch (Throwable e) {
          error = e;
        } finally {
          // and restore cached handlers here
          Thread.setDefaultUncaughtExceptionHandler(oldHandler);
          RxJavaPlugins.setErrorHandler(oldRxJavaHandler);
        }
        verify(error);
      }
    };
  }

  private void verify(Throwable error) throws Throwable {
    String message = verifyUncaught();
    if (message != null) {
      error = refineError(error);
      if (error != null) {
        throw new AssertionError(message, error);
      }
      throw new AssertionError(message);
    }
    if (error != null) {
      throw error;
    }
  }

  private String verifyUncaught() {
    Throwable throwable = uncaughtRef.get();
    String message;
    if (throwable == null) {
      message = verifyUncaughtErrorNotExpected();
    } else {
      message = verifyUncaughtErrorMatchesExpectations();
    }
    return message;
  }

  private String verifyUncaughtErrorNotExpected() {
    if (expectedType != null) {
      return "No uncaught exception occurred:\n" +
          "Expected: <" + expectedType.getName() + ">";
    }
    if (expectedMessage != null) {
      return "No uncaught exception occurred, " +
          "but expected one with message: \n" +
          "Expected: \"" + expectedMessage +"\"";
    }
    return null;
  }

  private String verifyUncaughtErrorMatchesExpectations() {
    Throwable uncaught = uncaughtRef.get();
    if ((expectedType == null) && (expectedMessage == null)) {
      return
          "Unexpected uncaught exception";
    } else if ((expectedType != null) && (expectedType != uncaught.getClass())) {
      return
          "Uncaught exception occurred, but is different than expected:\n" +
              "Expected: <" + expectedType.getName() +">\n" +
              "     but: was <" + uncaught.getClass().getName() + ">";
    } else if ((expectedMessage != null) && (!expectedMessage.equals(uncaught.getMessage()))) {
      return
          "Expected uncaught exception " + expectedType.getName() + " occurred " +
              "but has unexpected message:\n" +
              "Expected: \"" + expectedMessage + "\"\n" +
              "     but: was \"" + uncaught.getMessage() + "\"";
    }
    return null;
  }

  private Throwable refineError(Throwable caught) {
    Throwable uncaught = uncaughtRef.get();
    if ((caught == null) && (uncaught == null)) {
      return null;
    }
    if ((caught != null) && (uncaught != null)) {
      return new CompositeException(uncaught, caught);
    }
    if (caught != null) {
      return caught;
    }
    return uncaught;
  }

}
