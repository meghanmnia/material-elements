/*
 * Copyright (C) 2020 ZeoFlow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zeoflow.material.elements.shadow;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

/**
 * Delegate interface that can be used to facilitate rendering of shadows pre-lollipop.
 */
public interface ShadowViewDelegate
{
  float getRadius();

  void setShadowPadding(int left, int top, int right, int bottom);

  void setBackgroundDrawable(@Nullable Drawable background);

  boolean isCompatPaddingEnabled();
}
