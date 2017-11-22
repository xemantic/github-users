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

package com.xemantic.ankh.shared.error;

import com.xemantic.ankh.shared.event.Sink;
import com.xemantic.ankh.shared.event.SnackbarMessageEvent;
import io.reactivex.Maybe;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test of the {@link AnkhExceptionHandler}.
 *
 * @author morisil
 */
public class AnkhExceptionHandlerTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Mock
  private Logger logger;

  @Mock
  private ErrorMessageProvider errorMessageProvider;

  @Mock
  private Sink<SnackbarMessageEvent> snackbarMessageSink;

  @Captor
  private ArgumentCaptor<SnackbarMessageEvent> snackbarMessageEventCaptor;

  @Test
  public void accept_exceptionIsNotReportable_shouldHandleExceptionAndNotSendSnackbarMessage() throws Exception {
    // given
    Exception exception = new Exception();
    given(errorMessageProvider.getMessage(exception)).willReturn(Maybe.empty());
    AnkhExceptionHandler handler = new AnkhExceptionHandler(
        logger,
        errorMessageProvider,
        snackbarMessageSink
    );

    // when
    handler.uncaughtException(Thread.currentThread(), exception);

    // then
    verify(logger).log(Level.SEVERE, "Uncaught Exception", exception);
    verify(errorMessageProvider).getMessage(exception);
    assertThat(snackbarMessageEventCaptor.getAllValues()).isEmpty();
    verifyNoMoreInteractions(
        errorMessageProvider,
        snackbarMessageSink
    );
  }

  @Test
  public void accept_exceptionIsReportable_shouldHandleExceptionAndSendSnackbarMessage() throws Exception {
    // given
    Exception exception = new Exception();
    given(errorMessageProvider.getMessage(exception)).willReturn(Maybe.just("foo"));
    AnkhExceptionHandler handler = new AnkhExceptionHandler(
        logger,
        errorMessageProvider,
        snackbarMessageSink
    );

    // when
    handler.uncaughtException(Thread.currentThread(), exception);

    // then
    verify(logger).log(Level.SEVERE, "Uncaught Exception", exception);
    verify(errorMessageProvider).getMessage(exception);
    verify(snackbarMessageSink).publish(snackbarMessageEventCaptor.capture());
    assertThat(snackbarMessageEventCaptor.getValue().getMessage()).isEqualTo("foo");
    verifyNoMoreInteractions(
        errorMessageProvider,
        snackbarMessageSink
    );
  }

}
