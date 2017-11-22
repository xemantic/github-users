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

package com.xemantic.ankh.shared.snackbar;

import com.xemantic.ankh.shared.event.SnackbarMessageEvent;
import io.reactivex.subjects.PublishSubject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test of the {@link SnackbarPresenter}.
 *
 * @author morisil
 */
public class SnackbarPresenterTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Mock
  private SnackbarView view;

  @Test
  public void start_noEventPosted_shouldDoNothingWithView() {
    // given
    PublishSubject<SnackbarMessageEvent> snackbarMessage$ = PublishSubject.create();
    SnackbarPresenter presenter = new SnackbarPresenter(snackbarMessage$);

    // when
    presenter.start(view);

    // then
    verifyZeroInteractions(view);
  }

  @Test
  public void start_eventPosted_shouldShowTheMessage() {
    // given
    SnackbarMessageEvent event = new SnackbarMessageEvent("foo");
    PublishSubject<SnackbarMessageEvent> snackbarMessage$ = PublishSubject.create();
    SnackbarPresenter presenter = new SnackbarPresenter(snackbarMessage$);
    presenter.start(view);

    // when
    snackbarMessage$.onNext(event);

    // then
    verify(view).show("foo");
    verifyNoMoreInteractions(view);
  }

}
