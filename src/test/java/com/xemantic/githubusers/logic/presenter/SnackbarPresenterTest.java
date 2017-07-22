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

package com.xemantic.githubusers.logic.presenter;

import com.xemantic.githubusers.logic.event.SnackbarMessageEvent;
import com.xemantic.githubusers.logic.view.SnackbarView;
import org.junit.Test;
import rx.subjects.PublishSubject;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Test of the {@link SnackbarPresenter}.
 *
 * @author morisil
 */
public class SnackbarPresenterTest {

  @Test
  public void start_noEventPosted_shouldDoNothingWithView() {
    // given
    PublishSubject<SnackbarMessageEvent> snackbarMessageBus = PublishSubject.create();
    SnackbarView view = mock(SnackbarView.class);
    SnackbarPresenter presenter = new SnackbarPresenter(snackbarMessageBus);

    // when
    presenter.start(view);

    // then
    then(view).shouldHaveZeroInteractions();
  }

  @Test
  public void start_eventPosted_shouldShowTheMessage() {
    // given
    SnackbarMessageEvent event = new SnackbarMessageEvent("foo");
    PublishSubject<SnackbarMessageEvent> snackbarMessageBus = PublishSubject.create();
    SnackbarView view = mock(SnackbarView.class);

    SnackbarPresenter presenter = new SnackbarPresenter(snackbarMessageBus);
    presenter.start(view);

    // when
    snackbarMessageBus.onNext(event);

    // then
    then(view).should().show("foo");
    then(view).shouldHaveNoMoreInteractions();
  }

}
