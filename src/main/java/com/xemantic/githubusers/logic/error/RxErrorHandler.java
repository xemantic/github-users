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

package com.xemantic.githubusers.logic.error;

import com.xemantic.githubusers.logic.event.Sink;
import com.xemantic.githubusers.logic.event.SnackbarMessageEvent;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * The default global error handler to be supplied
 * to {@link RxJavaPlugins#setErrorHandler(Consumer)}.
 *
 * @author morisil
 */
@Singleton
public class RxErrorHandler implements Consumer<Throwable> {

  private final Thread.UncaughtExceptionHandler exceptionHandler;

  private final ErrorMessageProvider errorMessageProvider;

  private final Sink<SnackbarMessageEvent> snackbarMessageSink;

  @Inject
  public RxErrorHandler(
      Thread.UncaughtExceptionHandler exceptionHandler,
      ErrorMessageProvider errorMessageProvider,
      Sink<SnackbarMessageEvent> snackbarMessageSink
  ) {
    this.exceptionHandler = exceptionHandler;
    this.snackbarMessageSink = snackbarMessageSink;
    this.errorMessageProvider = errorMessageProvider;
  }

  @Override
  public void accept(Throwable throwable) throws Exception {
    if (throwable instanceof OnErrorNotImplementedException) {
      throwable = throwable.getCause();
    }
    exceptionHandler.uncaughtException(Thread.currentThread(), throwable);
    errorMessageProvider.getMessage(throwable)
        .map(SnackbarMessageEvent::new)
        .subscribe(snackbarMessageSink::publish);
  }

}
