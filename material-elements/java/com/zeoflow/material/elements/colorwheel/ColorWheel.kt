package com.zeoflow.material.elements.colorwheel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.zeoflow.R
import com.zeoflow.material.elements.color.ColorEnvelope
import com.zeoflow.material.elements.colorwheel.extensions.readBooleanCompat
import com.zeoflow.material.elements.colorwheel.extensions.writeBooleanCompat
import com.zeoflow.material.elements.colorwheel.flag.FlagView
import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawable
import com.zeoflow.material.elements.colorwheel.thumb.ThumbDrawableState
import com.zeoflow.material.elements.colorwheel.thumb.readThumbState
import com.zeoflow.material.elements.colorwheel.thumb.writeThumbState
import com.zeoflow.material.elements.colorwheel.utils.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

open class ColorWheel @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  private val hueGradient = GradientDrawable().apply {
    gradientType = GradientDrawable.SWEEP_GRADIENT
    shape = GradientDrawable.OVAL
    colors = HUE_COLORS
  }

  private val saturationGradient = GradientDrawable().apply {
    gradientType = GradientDrawable.RADIAL_GRADIENT
    shape = GradientDrawable.OVAL
    colors = SATURATION_COLORS
  }

  private val viewConfig = ViewConfiguration.get(context)
  private val thumbDrawable = ThumbDrawable()
  private val hsvColor = HsvColor(value = 1f)

  private var wheelCenterX = 0
  private var wheelCenterY = 0
  private var wheelRadius = 0
  private var downX = 0f
  private var downY = 0f

  var rgb
    get() = hsvColor.rgb
    set(rgb) {
      hsvColor.rgb = rgb
      hsvColor.set(value = 1f)
      fireColorListener()
      invalidate()
    }

  var thumbRadius
    get() = thumbDrawable.radius
    set(value) {
      thumbDrawable.radius = value
      invalidate()
    }

  var thumbColor
    get() = thumbDrawable.thumbColor
    set(value) {
      thumbDrawable.thumbColor = value
      invalidate()
    }

  var thumbStrokeColor
    get() = thumbDrawable.strokeColor
    set(value) {
      thumbDrawable.strokeColor = value
      invalidate()
    }

  var thumbColorCircleScale
    get() = thumbDrawable.colorCircleScale
    set(value) {
      thumbDrawable.colorCircleScale = value
      invalidate()
    }

  private var colorChangeListener: ((Int) -> Unit)? = null

  var interceptTouchEvent = true

  init {
    parseAttributes(context, attrs)
  }

  private fun parseAttributes(context: Context, attrs: AttributeSet?) {
    context.obtainStyledAttributes(attrs, R.styleable.ColorWheel, 0, R.style.ColorWheelDefaultStyle).apply {
      thumbRadius = getDimensionPixelSize(R.styleable.ColorWheel_cw_thumbRadius, 0)
      thumbColor = getColor(R.styleable.ColorWheel_cw_thumbColor, 0)
      thumbStrokeColor = getColor(R.styleable.ColorWheel_cw_thumbStrokeColor, 0)
      thumbColorCircleScale = getFloat(R.styleable.ColorWheel_cw_thumbColorCircleScale, 0f)
      recycle()
    }
  }

  fun setRgb(r: Int, g: Int, b: Int) {
    rgb = Color.rgb(r, g, b)
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val minDimension = minOf(
      MeasureSpec.getSize(widthMeasureSpec),
      MeasureSpec.getSize(heightMeasureSpec)
    )

    setMeasuredDimension(
      resolveSize(minDimension, widthMeasureSpec),
      resolveSize(minDimension, heightMeasureSpec)
    )
  }

  override fun onDraw(canvas: Canvas) {
    drawColorWheel(canvas)
    drawThumb(canvas)
  }

  private fun drawColorWheel(canvas: Canvas) {
    val hSpace = width - paddingLeft - paddingRight
    val vSpace = height - paddingTop - paddingBottom

    wheelCenterX = paddingLeft + hSpace / 2
    wheelCenterY = paddingTop + vSpace / 2
    wheelRadius = (minOf(hSpace, vSpace) / 2).takeIf { it > 0 } ?: 0

    val left = wheelCenterX - wheelRadius
    val top = wheelCenterY - wheelRadius
    val right = wheelCenterX + wheelRadius
    val bottom = wheelCenterY + wheelRadius

    hueGradient.setBounds(left, top, right, bottom)
    saturationGradient.setBounds(left, top, right, bottom)
    saturationGradient.gradientRadius = wheelRadius.toFloat()

    hueGradient.draw(canvas)
    saturationGradient.draw(canvas)
  }

  private fun drawThumb(canvas: Canvas) {
    val r = hsvColor.saturation * wheelRadius
    val hueRadians = toRadians(hsvColor.hue)
    val x = cos(hueRadians) * r + wheelCenterX
    val y = sin(hueRadians) * r + wheelCenterY

    thumbDrawable.indicatorColor = hsvColor.rgb
    thumbDrawable.setCoordinates(x, y)
    thumbDrawable.draw(canvas)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> onActionDown(event)
      MotionEvent.ACTION_MOVE -> updateColorOnMotionEvent(event)
      MotionEvent.ACTION_UP -> {
        updateColorOnMotionEvent(event)
        if (isTap(event, downX, downY, viewConfig)) performClick()
      }
    }
    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        if(getFlagView() != null) {
          getFlagView()?.receiveOnTouchEvent(event)
        }
      }
      MotionEvent.ACTION_MOVE -> {
        if(getFlagView() != null) {
          getFlagView()?.receiveOnTouchEvent(event)
        }
      }
      MotionEvent.ACTION_UP -> {
        if(getFlagView() != null) {
          getFlagView()?.receiveOnTouchEvent(event)
        }
      }
    }

    return true
  }

  private fun onActionDown(event: MotionEvent) {
    parent.requestDisallowInterceptTouchEvent(interceptTouchEvent)
    updateColorOnMotionEvent(event)
    downX = event.x
    downY = event.y
  }

  private fun updateColorOnMotionEvent(event: MotionEvent) {
    calculateColor(event)
    fireColorListener()
    invalidate()
  }

  private fun calculateColor(event: MotionEvent) {
    val legX = event.x - wheelCenterX
    val legY = event.y - wheelCenterY
    val hypot = minOf(hypot(legX, legY), wheelRadius.toFloat())
    val hue = (toDegrees(atan2(legY, legX)) + 360) % 360
    val saturation = hypot / wheelRadius
    hsvColor.set(hue, saturation, 1f)
  }

  private var flagView: FlagView? = null

  /**
   * gets a [FlagView].
   *
   * @return [FlagView].
   */
  open fun getFlagView(): FlagView? {
    return flagView
  }

  /**
   * sets a [FlagView].
   *
   * @param flagView [FlagView].
   */
  open fun setFlagView(flagView: FlagView) {
    flagView.gone()
    this.flagView = flagView
  }

  open fun getColorEnvelope(): ColorEnvelope? {
    return ColorEnvelope(rgb)
  }

  private fun fireColorListener() {
    if (flagView != null) {
      flagView!!.onRefresh(getColorEnvelope())
      println("hereWeAre -> onRefresh")
    }
    colorChangeListener?.invoke(hsvColor.rgb)
  }

  override fun onSaveInstanceState(): Parcelable {
    val superState = super.onSaveInstanceState()
    val thumbState = thumbDrawable.saveState()
    return ColorWheelState(superState, this, thumbState)
  }

  override fun onRestoreInstanceState(state: Parcelable) {
    if (state is ColorWheelState) {
      super.onRestoreInstanceState(state.superState)
      readColorWheelState(state)
    } else {
      super.onRestoreInstanceState(state)
    }
  }

  private fun readColorWheelState(state: ColorWheelState) {
    thumbDrawable.restoreState(state.thumbState)
    interceptTouchEvent = state.interceptTouchEvent
    rgb = state.rgb
  }
}

private class ColorWheelState : View.BaseSavedState {

  val thumbState: ThumbDrawableState
  val interceptTouchEvent: Boolean
  val rgb: Int

  constructor(superState: Parcelable?, view: ColorWheel, thumbState: ThumbDrawableState) : super(superState) {
    this.thumbState = thumbState
    interceptTouchEvent = view.interceptTouchEvent
    rgb = view.rgb
  }

  constructor(source: Parcel) : super(source) {
    thumbState = source.readThumbState()
    interceptTouchEvent = source.readBooleanCompat()
    rgb = source.readInt()
  }

  override fun writeToParcel(out: Parcel, flags: Int) {
    super.writeToParcel(out, flags)
    out.writeThumbState(thumbState, flags)
    out.writeBooleanCompat(interceptTouchEvent)
    out.writeInt(rgb)
  }

  companion object CREATOR : Parcelable.Creator<ColorWheelState> {

    override fun createFromParcel(source: Parcel) = ColorWheelState(source)

    override fun newArray(size: Int) = arrayOfNulls<ColorWheelState>(size)
  }
}