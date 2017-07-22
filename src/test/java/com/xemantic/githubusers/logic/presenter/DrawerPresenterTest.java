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

import com.xemantic.githubusers.logic.driver.UrlOpener;
import com.xemantic.githubusers.logic.event.SnackbarMessageEvent;
import com.xemantic.githubusers.logic.event.Trigger;
import com.xemantic.githubusers.logic.view.DrawerView;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test of the {@link DrawerView}.
 *
 * @author morisil
 */
public class DrawerPresenterTest {

  @Test
  public void start_noInteraction_shouldObserveAllTheIntentsAndDoNothingWithView() {
    // given
    PublishSubject<SnackbarMessageEvent> snackbarMessageBus = PublishSubject.create();

    UrlOpener urlOpener = mock(UrlOpener.class);

    DrawerView view = mock(DrawerView.class);
    given(view.observeOpenDrawerIntent()).willReturn(Observable.empty());
    given(view.observeReadAboutIntent()).willReturn(Observable.empty());
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(Observable.empty());
    given(view.observeSelectLanguageIntent()).willReturn(Observable.empty());
    DrawerPresenter presenter = new DrawerPresenter("http://foo.com", snackbarMessageBus::onNext, urlOpener);

    // when
    presenter.start(view);

    // then
    verify(view).observeOpenDrawerIntent();
    verify(view).observeReadAboutIntent();
    verify(view).observeOpenProjectOnGitHubIntent();
    verify(view).observeSelectLanguageIntent();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
  }

  @Test
  public void start_openDrawerIntent_shouldOpenTheDrawer() {
    // given
    PublishSubject<SnackbarMessageEvent> snackbarMessageBus = PublishSubject.create();

    UrlOpener urlOpener = mock(UrlOpener.class);

    DrawerView view = mock(DrawerView.class);
    PublishSubject<Trigger> openTrigger = PublishSubject.create();
    given(view.observeOpenDrawerIntent()).willReturn(openTrigger);
    given(view.observeReadAboutIntent()).willReturn(Observable.empty());
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(Observable.empty());
    given(view.observeSelectLanguageIntent()).willReturn(Observable.empty());
    DrawerPresenter presenter = new DrawerPresenter("http://foo.com", snackbarMessageBus::onNext, urlOpener);
    presenter.start(view);

    // when
    openTrigger.onNext(Trigger.INSTANCE);

    // then
    verify(view).observeOpenDrawerIntent();
    verify(view).observeReadAboutIntent();
    verify(view).observeOpenProjectOnGitHubIntent();
    verify(view).observeSelectLanguageIntent();
    verify(view).openDrawer(true);
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
  }

  @Test
  public void start_readAboutIntent_shouldPostToSnackbar() {
    // given
    PublishSubject<SnackbarMessageEvent> snackbarMessageBus = PublishSubject.create();
    TestSubscriber<SnackbarMessageEvent> tracker = new TestSubscriber<>();
    snackbarMessageBus.subscribe(tracker);

    UrlOpener urlOpener = mock(UrlOpener.class);

    DrawerView view = mock(DrawerView.class);
    given(view.observeOpenDrawerIntent()).willReturn(Observable.empty());
    PublishSubject<Trigger> aboutTrigger = PublishSubject.create();
    given(view.observeReadAboutIntent()).willReturn(aboutTrigger);
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(Observable.empty());
    given(view.observeSelectLanguageIntent()).willReturn(Observable.empty());
    DrawerPresenter presenter = new DrawerPresenter("http://foo.com", snackbarMessageBus::onNext, urlOpener);
    presenter.start(view);

    // when
    aboutTrigger.onNext(Trigger.INSTANCE);

    // then
    verify(view).observeOpenDrawerIntent();
    verify(view).observeReadAboutIntent();
    verify(view).observeOpenProjectOnGitHubIntent();
    verify(view).observeSelectLanguageIntent();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
    assertThat(tracker.getOnNextEvents(), not(empty()));
    assertThat(tracker.getOnNextEvents().get(0).getMessage(), is("To be implemented soon"));
  }

  @Test
  public void start_openProjectOnGitHubIntent_shouldOpenProjectUrl() {
    // given
    PublishSubject<SnackbarMessageEvent> snackbarMessageBus = PublishSubject.create();

    UrlOpener urlOpener = mock(UrlOpener.class);

    DrawerView view = mock(DrawerView.class);
    given(view.observeOpenDrawerIntent()).willReturn(Observable.empty());
    given(view.observeReadAboutIntent()).willReturn(Observable.empty());
    PublishSubject<Trigger> openProjectTrigger = PublishSubject.create();
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(openProjectTrigger);
    given(view.observeSelectLanguageIntent()).willReturn(Observable.empty());
    DrawerPresenter presenter = new DrawerPresenter("http://foo.com", snackbarMessageBus::onNext, urlOpener);
    presenter.start(view);

    // when
    openProjectTrigger.onNext(Trigger.INSTANCE);

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
    PublishSubject<SnackbarMessageEvent> snackbarMessageBus = PublishSubject.create();
    TestSubscriber<SnackbarMessageEvent> tracker = new TestSubscriber<>();
    snackbarMessageBus.subscribe(tracker);

    UrlOpener urlOpener = mock(UrlOpener.class);

    DrawerView view = mock(DrawerView.class);
    given(view.observeOpenDrawerIntent()).willReturn(Observable.empty());
    given(view.observeReadAboutIntent()).willReturn(Observable.empty());
    given(view.observeOpenProjectOnGitHubIntent()).willReturn(Observable.empty());
    PublishSubject<Trigger> selectLanguageTrigger = PublishSubject.create();
    given(view.observeSelectLanguageIntent()).willReturn(selectLanguageTrigger);
    DrawerPresenter presenter = new DrawerPresenter("http://foo.com", snackbarMessageBus::onNext, urlOpener);
    presenter.start(view);

    // when
    selectLanguageTrigger.onNext(Trigger.INSTANCE);

    // then
    verify(view).observeOpenDrawerIntent();
    verify(view).observeReadAboutIntent();
    verify(view).observeOpenProjectOnGitHubIntent();
    verify(view).observeSelectLanguageIntent();
    then(view).shouldHaveNoMoreInteractions();
    then(urlOpener).shouldHaveZeroInteractions();
    assertThat(tracker.getOnNextEvents(), not(empty()));
    assertThat(tracker.getOnNextEvents().get(0).getMessage(), is("To be implemented soon"));
  }

}
