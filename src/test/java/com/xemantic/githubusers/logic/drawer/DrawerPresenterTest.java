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
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import static com.xemantic.ankh.test.TestEvents.noEvents;
import static com.xemantic.ankh.test.TestEvents.trigger;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;

/**
 * Test of the {@link DrawerView}.
 *
 * @author morisil
 */
public class DrawerPresenterTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Mock
  private UrlOpener urlOpener;

  @Mock
  private DrawerView view;

  @Test
  public void start_noInteraction_shouldObserveAllTheIntentsAndDoNothingWithView() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = TestObserver.create();
    given(view.observeOpenDrawerIntent()).willReturn(noEvents());
    given(view.observeReadAboutIntent()).willReturn(noEvents());
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(noEvents());
    given(view.observeSelectLanguageIntent()).willReturn(noEvents());

    DrawerPresenter presenter = new DrawerPresenter(
        "http://foo.com",
        Sink.of(snackbarMessage$),
        urlOpener
    );

    // when
    presenter.start(view);

    // then
    verify(view).observeOpenDrawerIntent();
    verify(view).observeReadAboutIntent();
    verify(view).observeOpenProjectOnGitHubIntent();
    verify(view).observeSelectLanguageIntent();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
    snackbarMessage$.assertNoValues();
  }

  @Test
  public void start_openDrawerIntent_shouldOpenTheDrawer() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = new TestObserver<>();
    PublishSubject<Trigger> openDrawerIntent = PublishSubject.create();
    given(view.observeOpenDrawerIntent()).willReturn(openDrawerIntent);
    given(view.observeReadAboutIntent()).willReturn(noEvents());
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(noEvents());
    given(view.observeSelectLanguageIntent()).willReturn(noEvents());
    DrawerPresenter presenter = new DrawerPresenter(
        "http://foo.com", Sink.of(snackbarMessage$), urlOpener
    );
    presenter.start(view);

    // when
    trigger(openDrawerIntent);

    // then
    verify(view).observeOpenDrawerIntent();
    verify(view).observeReadAboutIntent();
    verify(view).observeOpenProjectOnGitHubIntent();
    verify(view).observeSelectLanguageIntent();
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
    given(view.observeOpenDrawerIntent()).willReturn(noEvents());
    given(view.observeReadAboutIntent()).willReturn(readAboutIntent);
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(noEvents());
    given(view.observeSelectLanguageIntent()).willReturn(noEvents());
    DrawerPresenter presenter = new DrawerPresenter(
        "http://foo.com",
        Sink.of(snackbarMessage$),
        urlOpener
    );
    presenter.start(view);

    // when
    trigger(readAboutIntent);

    // then
    verify(view).observeOpenDrawerIntent();
    verify(view).observeReadAboutIntent();
    verify(view).observeOpenProjectOnGitHubIntent();
    verify(view).observeSelectLanguageIntent();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
    snackbarMessage$.assertValueCount(1);
    SnackbarMessageEvent event = snackbarMessage$.values().get(0);
    assertThat(event.getMessage(), is("To be implemented soon"));
  }

  @Test
  public void start_openProjectOnGitHubIntent_shouldOpenProjectUrl() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = TestObserver.create();
    PublishSubject<Trigger> openProjectIntent = PublishSubject.create();
    given(view.observeOpenDrawerIntent()).willReturn(noEvents());
    given(view.observeReadAboutIntent()).willReturn(noEvents());
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(openProjectIntent);
    given(view.observeSelectLanguageIntent()).willReturn(noEvents());
    DrawerPresenter presenter = new DrawerPresenter(
        "http://foo.com",
        Sink.of(snackbarMessage$),
        urlOpener
    );
    presenter.start(view);

    // when
    trigger(openProjectIntent);

    // then
    verify(view).observeOpenDrawerIntent();
    verify(view).observeReadAboutIntent();
    verify(view).observeOpenProjectOnGitHubIntent();
    verify(view).observeSelectLanguageIntent();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).should().openUrl("http://foo.com");
    then(urlOpener).shouldHaveNoMoreInteractions();
  }

  @Test
  public void start_selectLanguageIntent_shouldPostToSnackbar() {
    // given
    TestObserver<SnackbarMessageEvent> snackbarMessage$ = new TestObserver<>();
    PublishSubject<Trigger> selectLanguageIntent = PublishSubject.create();
    given(view.observeOpenDrawerIntent()).willReturn(noEvents());
    given(view.observeReadAboutIntent()).willReturn(noEvents());
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(noEvents());
    given(view.observeSelectLanguageIntent()).willReturn(selectLanguageIntent);
    DrawerPresenter presenter = new DrawerPresenter(
        "http://foo.com",
        Sink.of(snackbarMessage$),
        urlOpener
    );
    presenter.start(view);

    // when
    trigger(selectLanguageIntent);

    // then
    verify(view).observeOpenDrawerIntent();
    verify(view).observeReadAboutIntent();
    verify(view).observeOpenProjectOnGitHubIntent();
    verify(view).observeSelectLanguageIntent();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
    snackbarMessage$.assertValueCount(1);
    SnackbarMessageEvent event = snackbarMessage$.values().get(0);
    assertThat(event.getMessage(), is("To be implemented soon"));
  }

}
