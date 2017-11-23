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
import com.xemantic.ankh.shared.snackbar.SnackbarView;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standardized exception handler. It should be set globally with
 * {@link Thread#setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)}.
 * The handler will log the exception, check if there is a user friendly message
 * for the exception, and if so it will display it on the {@link SnackbarView}.
 * The logger can be configured to log thrown exception remotely.
 *
 * @author morisil
 */
@Singleton
public class AnkhExceptionHandler implements Thread.UncaughtExceptionHandler {

  private final Logger logger;

  private final ErrorMessageProvider errorMessageProvider;

  private final Sink<SnackbarMessageEvent> snackbarMessageSink;

  @Inject
  public AnkhExceptionHandler(
      Logger logger,
      ErrorMessageProvider errorMessageProvider,
      Sink<SnackbarMessageEvent> snackbarMessageSink
  ) {
    this.logger = logger;
    this.snackbarMessageSink = snackbarMessageSink;
    this.errorMessageProvider = errorMessageProvider;
  }

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    logger.log(Level.SEVERE, "Uncaught Exception", throwable);
    errorMessageProvider.getMessage(throwable)
        .map(SnackbarMessageEvent::new)
        .subscribe(snackbarMessageSink::publish);
  }

}
