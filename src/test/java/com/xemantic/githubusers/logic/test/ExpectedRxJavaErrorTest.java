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
  public void applyAndEvaluate_noExceptionThrown_shouldFail() throws Throwable {
    // given
    Statement statement = mock(Statement.class);
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();
    thrown.expect(AssertionError.class);
    thrown.expectMessage("No exception was thrown");

    // when
    expectedError.apply(statement, description).evaluate();

    // then
    verify(statement).evaluate();
    verifyNoMoreInteractions(statement, description);
    expectedError.expect(Exception.class);
  }

  @Test
  public void applyAndEvaluate_exceptionThrown_shouldMatchExpectedException() throws Throwable {
    // given
    Statement statement = new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Single.just("bar")
            .map(s -> { throw new Exception("foo"); })
            .subscribe();
      }
    };
    Description description = mock(Description.class);
    ExpectedRxJavaError expectedError = ExpectedRxJavaError.none();

    // when
    expectedError.apply(statement, description).evaluate();

    // then
    verifyNoMoreInteractions(description);
    expectedError.expect(OnErrorNotImplementedException.class);
    expectedError.expectMessage("foo");
  }

}
