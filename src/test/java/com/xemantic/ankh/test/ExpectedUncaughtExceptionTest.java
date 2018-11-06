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

import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test of the {@link ExpectedUncaughtException}.
 *
 * @author morisil
 */
public class ExpectedUncaughtExceptionTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private Description description;

  @Test
  public void evaluate_noExpectedErrorsAndNothingThrown_shouldOnlyEvaluateOriginalStatement() throws Throwable {
    // given
    Statement originalStatement = mock(Statement.class);
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    Statement statement = uncaughtThrown.apply(originalStatement, description);

    // when
    statement.evaluate();

    // then
    verify(originalStatement).evaluate();
    verifyNoMoreInteractions(originalStatement, description);
  }

  @Test
  public void expect_noExceptionThrownButOneExpected_shouldFail() throws Throwable {
    // given
    Statement originalStatement = mock(Statement.class);
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    Statement statement = uncaughtThrown.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("No uncaught exception occurred:\n" +
        "Expected: <java.lang.Exception>");

    // when
    uncaughtThrown.expect(Exception.class);
    statement.evaluate();

    // then should fail
  }

  @Test
  public void expectMessage_noExceptionThrownButMessageExpected_shouldFail() throws Throwable {
    // given
    Statement originalStatement = mock(Statement.class);
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    Statement statement = uncaughtThrown.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage(
        "No uncaught exception occurred, but expected one with message: \n" +
            "Expected: \"foo\""
    );

    // when
    uncaughtThrown.expectMessage("foo");
    statement.evaluate();

    // then should fail
  }

  @Test
  public void expectExceptionAndMessage_noExceptionThrown_shouldFail() throws Throwable {
    // given
    Statement originalStatement = mock(Statement.class);
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    Statement statement = uncaughtThrown.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("No uncaught exception occurred:\n" +
        "Expected: <java.lang.Exception>");

    // when
    uncaughtThrown.expect(Exception.class);
    uncaughtThrown.expectMessage("foo");
    statement.evaluate();

    // then should fail
  }

  @Test
  public void expect_calledTwice_shouldFail() throws Throwable {
    // given
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    uncaughtThrown.expect(Exception.class);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Already expecting: class java.lang.Exception");

    // when
    uncaughtThrown.expect(RuntimeException.class);

    // then should fail
  }

  @Test
  public void expect_calledWithNull_shouldFail() throws Throwable {
    // given
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    thrown.expect(NullPointerException.class);

    // when
    uncaughtThrown.expect(null);

    // then should fail
  }

  @Test
  public void expectMessage_calledTwice_shouldFail() throws Throwable {
    // given
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    uncaughtThrown.expectMessage("foo");
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Already expecting message: foo");

    // when
    uncaughtThrown.expectMessage("bar");

    // then should fail
  }

  @Test
  public void expectMessage_calledWithNull_shouldFail() throws Throwable {
    // given
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    thrown.expect(NullPointerException.class);

    // when
    uncaughtThrown.expectMessage(null);

    // then should fail
  }

  @Test
  public void expect_exceptionThrownAndExpected_shouldMatchExpectedException() throws Throwable {
    // given
    Statement originalStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Single.just("bar")
            .map(s -> { throw new Exception("foo"); })
            .subscribe();
      }
    };
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    uncaughtThrown.expect(OnErrorNotImplementedException.class);
    uncaughtThrown.expectMessage("foo");
    Statement statement = uncaughtThrown.apply(originalStatement, description);

    // when
    statement.evaluate();

    // then
    verifyNoMoreInteractions(description);
  }

  @Test
  public void expect_exceptionThrownButExpectingDifferentError_shouldFail() throws Throwable {
    // given
    Statement originalStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Single.just("bar")
            .map(s -> { throw new Exception("foo"); })
            .subscribe();
      }
    };
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    uncaughtThrown.expect(Exception.class);
    uncaughtThrown.expectMessage("foo");
    Statement statement = uncaughtThrown.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("Uncaught exception occurred, " +
        "but is different than expected:\n" +
        "Expected: <java.lang.Exception>\n" +
        "     but: was <io.reactivex.exceptions.OnErrorNotImplementedException>");

    // when
    statement.evaluate();

    // then should fail
  }

  @Test
  public void expect_exceptionThrownMatchesExpectedButExpectingDifferentMessage_shouldFail() throws Throwable {
    // given
    Statement originalStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Single.just("bar")
            .map(s -> { throw new Exception("foo"); })
            .subscribe();
      }
    };
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    uncaughtThrown.expect(OnErrorNotImplementedException.class);
    uncaughtThrown.expectMessage("bar");
    Statement statement = uncaughtThrown.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("Expected uncaught exception " +
        "io.reactivex.exceptions.OnErrorNotImplementedException " +
        "occurred but has unexpected message:\n" +
        "Expected: \"bar\"\n" +
        "     but: was \"foo\"");

    // when
    statement.evaluate();

    // then should fail
  }

  @Test
  public void noneExpected_butUncaughtErrorOccurred_shouldFail() throws Throwable {
    // given
    Statement originalStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Single.just("bar")
            .map(s -> { throw new Exception("foo"); })
            .subscribe();
      }
    };
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    Statement statement = uncaughtThrown.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("Unexpected uncaught exception");

    // when
    statement.evaluate();

    // then should fail
  }

  @Test
  public void noneExpected_butUncaughtErrorOccurredAndStatementThrowsException_shouldFail() throws Throwable {
    // given
    Statement originalStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Single.just("bar")
            .map(s -> { throw new Exception("foo"); })
            .subscribe();
        throw new Exception("buzz");
      }
    };
    ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();
    Statement statement = uncaughtThrown.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("Unexpected uncaught exception");
    thrown.expectCause(instanceOf(CompositeException.class));

    // when
    statement.evaluate();

    // then should fail
  }

}
