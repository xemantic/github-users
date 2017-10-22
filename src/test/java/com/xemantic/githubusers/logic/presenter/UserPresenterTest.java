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

import com.xemantic.githubusers.logic.event.Trigger;
import com.xemantic.githubusers.logic.event.UserSelectedEvent;
import com.xemantic.githubusers.logic.model.User;
import com.xemantic.githubusers.logic.view.UserView;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Test of the {@link UserPresenter}.
 *
 * @author morisil
 */
public class UserPresenterTest {

  @Test
  public void start_user_shouldDisplayUser() {
    // given
    PublishSubject<UserSelectedEvent> userSelectedBus = PublishSubject.create();

    User user = mock(User.class);
    given(user.getLogin()).willReturn("foo");

    UserView view = mock(UserView.class);
    given(view.observeSelection()).willReturn(Observable.empty());

    UserPresenter presenter = new UserPresenter(userSelectedBus::onNext);

    // when
    presenter.start(user, view);

    // then
    then(view).should().displayUser(user);
    then(view).should().observeSelection();
    then(view).shouldHaveNoMoreInteractions();
  }

  @Test
  public void onUserSelected_view_shouldPostUserSelectedEvent() {
    // given
    PublishSubject<UserSelectedEvent> userSelectedBus = PublishSubject.create();
    TestObserver<UserSelectedEvent> tracker = new TestObserver<>();
    userSelectedBus.subscribe(tracker);

    User user = mock(User.class);
    given(user.getLogin()).willReturn("foo");

    UserView view = mock(UserView.class);
    PublishSubject<Trigger> selectionTrigger = PublishSubject.create();
    given(view.observeSelection()).willReturn(selectionTrigger);

    UserPresenter presenter = new UserPresenter(userSelectedBus::onNext);
    presenter.start(user, view);

    // when
    selectionTrigger.onNext(Trigger.INSTANCE);

    // then
    tracker.assertValueCount(1);
    assertThat(tracker.values().get(0).getUser().getLogin(), is("foo"));
  }

}
