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

import com.xemantic.ankh.shared.error.Errors;
import com.xemantic.ankh.shared.event.Sink;
import com.xemantic.ankh.shared.presenter.Presenter;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author morisil
 */
public class TariffEstimatePresenter extends Presenter {

  @Inject
  public TariffEstimatePresenter(
      String postCode,
      TariffEstimateView view,
      Sink<QuoteEvent> quoteSink,
      UsageValidator usageValidator,
      Map<Integer, Integer> usageMap,
      QuoteService quoteService
  ) {
    register(
        view.personsSelection$()
            .map(usageMap::get)
            .doOnNext(view::displayUsage)
            .mergeWith(view.usage$())
            .concatMap(usage ->
                usageValidator.validate(usage)
                  .doOnSuccess(error -> {
                    if (!error.isEmpty()) {
                      view.submitEnabled(false);
                      view.displayUsageError(error);
                    }
                  })
                  .filter(String::isEmpty)
                  .map(error -> usage)
                  .toObservable()
            )
            .doOnNext(usage -> view.submitEnabled(true))
            .switchMap(usage ->
                view.submit$()
                    .doOnNext(trigger -> {
                      view.submitEnabled(false);
                      view.requestingQuote(true);
                    })
                    .flatMapSingle(trigger -> quoteService.getQuote(postCode, usage))
                    .doOnNext(quote -> {
                      view.requestingQuote(false);
                      view.submitEnabled(true);
                    })
                    .doOnError(throwable -> {
                      Errors.onError(throwable);
                      view.requestingQuote(false);
                      view.submitEnabled(true);
                      view.displayError(throwable.getMessage());
                    })
            )
            .doOnNext(quote -> quoteSink.publish(new QuoteEvent(quote)))
    );
  }

}
