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

package com.xemantic.githubusers.logic.user;

import com.xemantic.ankh.shared.event.Sink;
import com.xemantic.ankh.shared.event.Trigger;
import com.xemantic.githubusers.logic.event.UserSelectedEvent;
import com.xemantic.githubusers.logic.model.User;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import static com.xemantic.ankh.shared.event.Trigger.fire;
import static com.xemantic.ankh.shared.event.Trigger.noTriggers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Test of the {@link UserPresenter}.
 *
 * @author morisil
 */
public class UserPresenterTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Mock
  private UserView view;

  @Test
  public void start_user_shouldDisplayUser() {
    // given
    TestObserver<UserSelectedEvent> userSelected$ = TestObserver.create();
    User user = mock(User.class);
    given(view.userSelection$()).willReturn(noTriggers());
    UserPresenter presenter = new UserPresenter(Sink.of(userSelected$));

    // when
    presenter.start(user, view);

    // then
    then(view).should().displayUser(user);
    then(view).should().userSelection$();
    then(view).shouldHaveNoMoreInteractions();
    userSelected$.assertNoValues();
  }

  @Test
  public void onUserSelected_view_shouldPostUserSelectedEvent() {
    // given
    TestObserver<UserSelectedEvent> userSelected$ = TestObserver.create();
    User user = mock(User.class);
    given(user.getLogin()).willReturn("foo");
    PublishSubject<Trigger> userSelectionIntent = PublishSubject.create();
    given(view.userSelection$()).willReturn(userSelectionIntent);

    UserPresenter presenter = new UserPresenter(Sink.of(userSelected$));
    presenter.start(user, view);

    // when
    fire(userSelectionIntent);

    // then
    userSelected$.assertValueCount(1);
    UserSelectedEvent event = userSelected$.values().get(0);
    assertThat(event.getUser().getLogin()).isEqualTo("foo");
  }

}
