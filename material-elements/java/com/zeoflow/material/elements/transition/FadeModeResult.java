/*
 * Copyright 2020 ZeoFlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zeoflow.material.elements.transition;

class FadeModeResult
{
  final int startAlpha;
  final int endAlpha;
  final boolean endOnTop;

  private FadeModeResult(int startAlpha, int endAlpha, boolean endOnTop)
  {
    this.startAlpha = startAlpha;
    this.endAlpha = endAlpha;
    this.endOnTop = endOnTop;
  }

  static FadeModeResult startOnTop(int startAlpha, int endAlpha)
  {
    return new FadeModeResult(startAlpha, endAlpha, false);
  }

  static FadeModeResult endOnTop(int startAlpha, int endAlpha)
  {
    return new FadeModeResult(startAlpha, endAlpha, true);
  }
}
