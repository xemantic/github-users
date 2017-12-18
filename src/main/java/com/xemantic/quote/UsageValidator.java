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

import io.reactivex.Single;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author morisil
 */
public class UsageValidator {

  private final int minUsageKwh;

  private final int maxUsage;

  // TODO translation service
  @Inject
  public UsageValidator(
      @Named("minUsageKwh") int minUsageKwh,
      @Named("maxUsageKwh") int maxUsage
  ) {
    this.minUsageKwh = minUsageKwh;
    this.maxUsage = maxUsage;
  }

  public Single<String> validate(int usage) {
    return Single.just(doValidate(usage));
  }

  private String doValidate(int usage) {
    if (usage < minUsageKwh) {
      return "quote.error.usageTooLow";
    }
    if (usage > maxUsage) {
      return "quote.error.usageTooHigh";
    }
    return "";
  }

}
