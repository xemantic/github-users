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

package com.xemantic.githubusers.logic.drawer;

import com.xemantic.ankh.shared.driver.UrlOpener;
import com.xemantic.ankh.shared.event.Sink;
import com.xemantic.ankh.shared.event.SnackbarMessageEvent;
import com.xemantic.ankh.shared.event.Trigger;
import com.xemantic.ankh.test.ExpectedUncaughtException;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.util.concurrent.atomic.AtomicInteger;

import static com.xemantic.ankh.shared.event.Trigger.fire;
import static com.xemantic.ankh.shared.event.Trigger.noTriggers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test of the {@link DrawerView}.
 *
 * @author morisil
 */
public class DrawerPresenterTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Rule
  public ExpectedUncaughtException uncaughtThrown = ExpectedUncaughtException.none();

  @Mock
  private UrlOpener urlOpener;

  @Mock
  private DrawerView view;

  @Test
  public void start_noInteraction_shouldObserveAllTheIntentsAndDoNothingWithView() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = TestObserver.create();
    given(view.openDrawerIntent$()).willReturn(noTriggers());
    given(view.readAboutIntent$()).willReturn(noTriggers());
    given(view.openProjectOnGitHubIntent$()).willReturn(noTriggers());
    given(view.selectLanguageIntent$()).willReturn(noTriggers());

    DrawerPresenter presenter = new DrawerPresenter(
        view, "http://foo.com", Sink.of(snackbarMessage$), urlOpener
    );

    // when
    presenter.start();

    // then
    verify(view).openDrawerIntent$();
    verify(view).readAboutIntent$();
    verify(view).openProjectOnGitHubIntent$();
    verify(view).selectLanguageIntent$();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
    snackbarMessage$.assertNoValues();
  }

  @Test
  public void start_openDrawerIntent_shouldOpenTheDrawer() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = new TestObserver<>();
    PublishSubject<Trigger> openDrawerIntent = PublishSubject.create();
    given(view.openDrawerIntent$()).willReturn(openDrawerIntent);
    given(view.readAboutIntent$()).willReturn(noTriggers());
    given(view.openProjectOnGitHubIntent$()).willReturn(noTriggers());
    given(view.selectLanguageIntent$()).willReturn(noTriggers());
    DrawerPresenter presenter = new DrawerPresenter(
        view, "http://foo.com", Sink.of(snackbarMessage$), urlOpener
    );
    presenter.start();

    // when
    fire(openDrawerIntent);

    // then
    verify(view).openDrawerIntent$();
    verify(view).readAboutIntent$();
    verify(view).openProjectOnGitHubIntent$();
    verify(view).selectLanguageIntent$();
    verify(view).openDrawer(true);
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
    snackbarMessage$.assertNoValues();
  }

  @Test
  public void start_readAboutIntent_shouldPostToSnackbar() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = new TestObserver<>();
    PublishSubject<Trigger> readAboutIntent = PublishSubject.create();
    given(view.openDrawerIntent$()).willReturn(noTriggers());
    given(view.readAboutIntent$()).willReturn(readAboutIntent);
    given(view.openProjectOnGitHubIntent$()).willReturn(noTriggers());
    given(view.selectLanguageIntent$()).willReturn(noTriggers());
    DrawerPresenter presenter = new DrawerPresenter(
        view, "http://foo.com", Sink.of(snackbarMessage$), urlOpener
    );
    presenter.start();

    // when
    fire(readAboutIntent);

    // then
    verify(view).openDrawerIntent$();
    verify(view).readAboutIntent$();
    verify(view).openProjectOnGitHubIntent$();
    verify(view).selectLanguageIntent$();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
    snackbarMessage$.assertValueCount(1);
    SnackbarMessageEvent event = snackbarMessage$.values().get(0);
    assertThat(event.getMessage()).isEqualTo("To be implemented soon");
  }

  @Test
  public void start_openProjectOnGitHubIntent_shouldOpenProjectUrl() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = TestObserver.create();
    PublishSubject<Trigger> openProjectIntent = PublishSubject.create();
    given(view.openDrawerIntent$()).willReturn(noTriggers());
    given(view.readAboutIntent$()).willReturn(noTriggers());
    given(view.openProjectOnGitHubIntent$()).willReturn(openProjectIntent);
    given(view.selectLanguageIntent$()).willReturn(noTriggers());
    DrawerPresenter presenter = new DrawerPresenter(
        view,"http://foo.com", Sink.of(snackbarMessage$), urlOpener
    );
    presenter.start();

    // when
    fire(openProjectIntent);

    // then
    verify(view).openDrawerIntent$();
    verify(view).readAboutIntent$();
    verify(view).openProjectOnGitHubIntent$();
    verify(view).selectLanguageIntent$();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).should().openUrl("http://foo.com");
    then(urlOpener).shouldHaveNoMoreInteractions();
  }

  @Test
  public void start_selectLanguageIntent_shouldPostToSnackbar() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = new TestObserver<>();
    PublishSubject<Trigger> selectLanguageIntent = PublishSubject.create();
    given(view.openDrawerIntent$()).willReturn(noTriggers());
    given(view.readAboutIntent$()).willReturn(noTriggers());
    given(view.openProjectOnGitHubIntent$()).willReturn(noTriggers());
    given(view.selectLanguageIntent$()).willReturn(selectLanguageIntent);
    DrawerPresenter presenter = new DrawerPresenter(
        view,"http://foo.com", Sink.of(snackbarMessage$), urlOpener
    );
    presenter.start();

    // when
    fire(selectLanguageIntent);

    // then
    verify(view).openDrawerIntent$();
    verify(view).readAboutIntent$();
    verify(view).openProjectOnGitHubIntent$();
    verify(view).selectLanguageIntent$();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
    snackbarMessage$.assertValueCount(1);
    SnackbarMessageEvent event = snackbarMessage$.values().get(0);
    assertThat(event.getMessage()).isEqualTo("To be implemented soon");
  }

  /**
   * This test case is not really necessary here, as resubscription tests are
   * already covered by the {@code com.xemantic.ankh.shared.presenter.PresenterTest}.
   * Still it's an interesting example with use case which is actually quite likely
   * to fail.
   */
  @Test
  public void start_unexpectedExceptionWhenOpeningProjectOnGitHubForThe1StTime_shouldResubscribe() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = new TestObserver<>();
    PublishSubject<Trigger> openProjectIntents = PublishSubject.create();
    AtomicInteger openIntentCounter = new AtomicInteger(0);
    doAnswer(invocation -> {
      int attempt = openIntentCounter.incrementAndGet();
      if (attempt == 1) {
        throw new RuntimeException("foo");
      }
      return null;
    }).when(urlOpener).openUrl(anyString());
    given(view.openDrawerIntent$()).willReturn(noTriggers());
    given(view.readAboutIntent$()).willReturn(noTriggers());
    given(view.openProjectOnGitHubIntent$()).willReturn(openProjectIntents);
    given(view.selectLanguageIntent$()).willReturn(noTriggers());
    DrawerPresenter presenter = new DrawerPresenter(
        view, "http://foo.com", Sink.of(snackbarMessage$), urlOpener
    );
    presenter.start();

    // when
    fire(openProjectIntents); // first one throws exception
    fire(openProjectIntents);

    // then
    uncaughtThrown.expect(RuntimeException.class);
    uncaughtThrown.expectMessage("foo");
    verify(view).openDrawerIntent$();
    verify(view).readAboutIntent$();
    verify(view).openProjectOnGitHubIntent$();
    verify(view).selectLanguageIntent$();
    then(view).shouldHaveNoMoreInteractions();
    //snackbarMessage$.assertValueCount(0);
    verify(urlOpener, times(2)).openUrl("http://foo.com"); // 1st time should throw exception
    then(urlOpener).shouldHaveNoMoreInteractions();
  }

}
