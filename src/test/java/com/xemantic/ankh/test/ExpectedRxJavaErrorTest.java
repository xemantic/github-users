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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test of the {@link ExpectedRxJavaError}.
 *
 * @author morisil
 */
public class ExpectedRxJavaErrorTest {

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
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    Statement statement = expectedError.apply(originalStatement, description);

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
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    Statement statement = expectedError.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("No throwable was handled by RxJava error handler, " +
        "but expected: java.lang.Exception");

    // when
    expectedError.expect(Exception.class);
    statement.evaluate();

    // then should fail
  }

  @Test
  public void expectMessage_noExceptionThrownButMessageExpected_shouldFail() throws Throwable {
    // given
    Statement originalStatement = mock(Statement.class);
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    Statement statement = expectedError.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage(
        "No throwable was handled by RxJava error handler, " +
            "but expected one with message: foo"
    );

    // when
    expectedError.expectMessage("foo");
    statement.evaluate();

    // then should fail
  }

  @Test
  public void expectExceptionAndMessage_noExceptionThrown_shouldFail() throws Throwable {
    // given
    Statement originalStatement = mock(Statement.class);
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    Statement statement = expectedError.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("No throwable was handled by RxJava error handler, " +
        "but expected: java.lang.Exception");

    // when
    expectedError.expect(Exception.class);
    expectedError.expectMessage("foo");
    statement.evaluate();

    // then should fail
  }

  @Test
  public void expect_calledTwice_shouldFail() throws Throwable {
    // given
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    expectedError.expect(Exception.class);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Already expecting: class java.lang.Exception");

    // when
    expectedError.expect(RuntimeException.class);

    // then should fail
  }

  @Test
  public void expect_calledWithNull_shouldFail() throws Throwable {
    // given
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    thrown.expect(NullPointerException.class);

    // when
    expectedError.expect(null);

    // then should fail
  }

  @Test
  public void expectMessage_calledTwice_shouldFail() throws Throwable {
    // given
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    expectedError.expectMessage("foo");
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Already expecting message: foo");

    // when
    expectedError.expectMessage("bar");

    // then should fail
  }

  @Test
  public void expectMessage_calledWithNull_shouldFail() throws Throwable {
    // given
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    thrown.expect(NullPointerException.class);

    // when
    expectedError.expectMessage(null);

    // then should fail
  }

  @Test
  public void evaluate_exceptionThrownAndExpected_shouldMatchExpectedException() throws Throwable {
    // given
    Statement originalStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Single.just("bar")
            .map(s -> { throw new Exception("foo"); })
            .subscribe();
      }
    };
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    expectedError.expect(OnErrorNotImplementedException.class);
    expectedError.expectMessage("foo");
    Statement statement = expectedError.apply(originalStatement, description);

    // when
    statement.evaluate();

    // then
    verifyNoMoreInteractions(description);
  }

  @Test
  public void evaluate_exceptionThrownButExpectingDifferentError_shouldFail() throws Throwable {
    // given
    Statement originalStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Single.just("bar")
            .map(s -> { throw new Exception("foo"); })
            .subscribe();
      }
    };
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    expectedError.expect(Exception.class);
    expectedError.expectMessage("foo");
    Statement statement = expectedError.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("Unexpected exception\n" +
        "Expected: <class java.lang.Exception>\n" +
        "     but: was <class io.reactivex.exceptions.OnErrorNotImplementedException>");

    // when
    statement.evaluate();

    // then should fail
  }

  @Test
  public void evaluate_exceptionThrownMatchesExpectedButExpectingDifferentMessage_shouldFail() throws Throwable {
    // given
    Statement originalStatement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Single.just("bar")
            .map(s -> { throw new Exception("foo"); })
            .subscribe();
      }
    };
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    expectedError.expect(OnErrorNotImplementedException.class);
    expectedError.expectMessage("bar");
    Statement statement = expectedError.apply(originalStatement, description);
    thrown.expect(AssertionError.class);
    thrown.expectMessage("Unexpected throwable message\n" +
        "Expected: \"bar\"\n" +
        "     but: was \"foo\"");

    // when
    statement.evaluate();

    // then should fail
  }

}
