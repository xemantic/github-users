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

package com.xemantic.quote;

import com.xemantic.ankh.shared.event.Sink;
import com.xemantic.ankh.shared.event.Trigger;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Map;

import static com.xemantic.ankh.shared.event.Trigger.fire;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Test of the {@link TariffEstimatePresenter}.
 *
 * @author morisil
 */
public class TariffEstimatePresenterTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

  @Mock
  private TariffEstimateView view;

  @Mock
  private Sink<QuoteEvent> quoteSink;

  @Mock
  private UsageValidator usageValidator;

  @Mock
  private Map<Integer, Integer> usageMap;

  @Mock
  private QuoteService quoteService;

  @Captor
  private ArgumentCaptor<QuoteEvent> quoteEventCaptor;

  private InOrder inOrder;

  @Test
  public void start_noInteraction_shouldOnlyBindToView() {
    // given
    given(view.personsSelection$()).willReturn(Observable.empty());
    given(view.usage$()).willReturn(Observable.empty());
    TariffEstimatePresenter presenter = new TariffEstimatePresenter(
        "10042",
        view,
        quoteSink,
        usageValidator,
        usageMap,
        quoteService
    );

    // when
    presenter.start();

    // then
    inOrder = inOrder(view, quoteSink, usageValidator, usageMap, quoteService);
    $(view).personsSelection$();
    $(view).usage$();
    verifyNoMoreInteractions(view);
    verifyZeroInteractions(quoteSink, usageValidator, usageMap, quoteService);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void personsSelected_1person_shouldDisplayUsageFor1PersonAndBindToSubmit() {
    // given
    PublishSubject<Integer> personSelection$ = PublishSubject.create();
    given(view.personsSelection$()).willReturn(personSelection$);
    given(view.usage$()).willReturn(Observable.empty());
    given(usageMap.get(1)).willReturn(1000);
    given(usageValidator.validate(anyInt())).willReturn(Single.just(""));
    given(view.submit$()).willReturn(Observable.empty());
    TariffEstimatePresenter presenter = new TariffEstimatePresenter(
        "10042",
        view,
        quoteSink,
        usageValidator,
        usageMap,
        quoteService
    );
    presenter.start();

    // when
    personSelection$.onNext(1);

    // then
    inOrder = inOrder(view, quoteSink, usageValidator, usageMap, quoteService);
    $(view).personsSelection$();
    $(view).usage$();
    $(usageMap).get(1);
    $(view).displayUsage(1000);
    $(usageValidator).validate(1000);
    $(view).submitEnabled(true);
    $(view).submit$();
    verifyNoMoreInteractions(view, usageValidator, usageMap);
    verifyZeroInteractions(quoteSink, quoteService);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void usageSupplied_validUsage_shouldBindToSubmit() {
    // given
    PublishSubject<Integer> usage$ = PublishSubject.create();
    given(view.personsSelection$()).willReturn(Observable.empty());
    given(view.usage$()).willReturn(usage$);
    given(usageValidator.validate(anyInt())).willReturn(Single.just(""));
    given(view.submit$()).willReturn(Observable.empty());
    TariffEstimatePresenter presenter = new TariffEstimatePresenter(
        "10042",
        view,
        quoteSink,
        usageValidator,
        usageMap,
        quoteService
    );
    presenter.start();

    // when
    usage$.onNext(1234);

    // then
    inOrder = inOrder(view, quoteSink, usageValidator, usageMap, quoteService);
    $(view).personsSelection$();
    $(view).usage$();
    $(usageValidator).validate(1234);
    $(view).submitEnabled(true);
    $(view).submit$();
    verifyNoMoreInteractions(view, usageValidator);
    verifyZeroInteractions(usageMap, quoteSink, quoteService);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void usageSupplied_invalidUsage_shouldDisplayUsageError() {
    // given
    PublishSubject<Integer> usage$ = PublishSubject.create();
    given(view.personsSelection$()).willReturn(Observable.empty());
    given(view.usage$()).willReturn(usage$);
    given(usageValidator.validate(anyInt())).willReturn(Single.just("wrong usage"));
    TariffEstimatePresenter presenter = new TariffEstimatePresenter(
        "10042",
        view,
        quoteSink,
        usageValidator,
        usageMap,
        quoteService
    );
    presenter.start();

    // when
    usage$.onNext(1234);

    // then
    inOrder = inOrder(view, quoteSink, usageValidator, usageMap, quoteService);
    $(view).personsSelection$();
    $(view).usage$();
    $(usageValidator).validate(1234);
    $(view).submitEnabled(false);
    $(view).displayUsageError("wrong usage");
    verifyNoMoreInteractions(view, usageValidator);
    verifyZeroInteractions(usageMap, quoteSink, quoteService);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void usageSupplied_subsequentUsage_shouldBindToSubmitTwice() {
    // given
    PublishSubject<Integer> usage$ = PublishSubject.create();
    given(view.personsSelection$()).willReturn(Observable.empty());
    given(view.usage$()).willReturn(usage$);
    given(usageValidator.validate(anyInt())).willReturn(Single.just(""));
    given(view.submit$()).willReturn(Observable.empty());
    TariffEstimatePresenter presenter = new TariffEstimatePresenter(
        "10042",
        view,
        quoteSink,
        usageValidator,
        usageMap,
        quoteService
    );
    presenter.start();
    usage$.onNext(1234);

    // when
    usage$.onNext(2345);

    // then
    inOrder = inOrder(view, quoteSink, usageValidator, usageMap, quoteService);
    $(view).personsSelection$();
    $(view).usage$();
    $(usageValidator).validate(1234);
    $(view).submitEnabled(true);
    $(view).submit$();
    $(usageValidator).validate(2345);
    $(view).submitEnabled(true);
    $(view).submit$();
    verifyNoMoreInteractions(view, usageValidator);
    verifyZeroInteractions(usageMap, quoteSink, quoteService);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void onSubmit_afterUsageSupplied_shouldRequestQuote() {
    // given
    PublishSubject<Trigger> submit$ = PublishSubject.create();
    given(view.personsSelection$()).willReturn(Observable.empty());
    given(view.usage$()).willReturn(Observable.just(1000));
    given(usageValidator.validate(anyInt())).willReturn(Single.just(""));
    given(view.submit$()).willReturn(submit$);
    given(quoteService.getQuote(anyString(), anyInt())).willReturn(Single.just(Collections.emptyMap()));
    TariffEstimatePresenter presenter = new TariffEstimatePresenter(
        "10042",
        view,
        quoteSink,
        usageValidator,
        usageMap,
        quoteService
    );
    presenter.start();

    // when
    fire(submit$);

    // then
    inOrder = inOrder(view, quoteSink, usageValidator, usageMap, quoteService);
    $(view).personsSelection$();
    $(view).usage$();
    $(usageValidator).validate(1000);
    $(view).submitEnabled(true);
    $(view).submit$();
    $(view).submitEnabled(false);
    $(view).requestingQuote(true);
    $(quoteService).getQuote("10042", 1000);
    $(view).requestingQuote(false);
    $(view).submitEnabled(true);
    $(quoteSink).publish(quoteEventCaptor.capture());
    verifyNoMoreInteractions(view, usageValidator, quoteService, quoteSink);
    verifyZeroInteractions(usageMap);
    inOrder.verifyNoMoreInteractions();
  }

  private <T> T $(T mock) {
    return inOrder.verify(mock);
  }

}
