package com.neungi.moyeo.util

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.textfield.TextInputLayout
import com.neungi.moyeo.R
import com.neungi.moyeo.util.CommonUtils.adjustTextColorFromBackgroundUri
import com.neungi.moyeo.views.auth.viewmodel.AuthUiState
import com.neungi.moyeo.views.setting.viewmodel.SettingUiState
import java.time.LocalDate

/*** ImageView***/
@BindingAdapter("app:urlPhotoImage")
fun ImageView.bindUrlPhotoImage(url: String?) {
    if (url != null) {
        load(url) {
            error(R.drawable.ic_theme_white)
        }
    } else {
        this.setImageResource(R.drawable.ic_theme_white)
    }
    scaleType = ImageView.ScaleType.CENTER_CROP
}

@BindingAdapter("app:uriPhotoImage")
fun ImageView.bindUriPhotoImage(uri: Uri?) {
    if (uri != null) {
        load(uri) {
            error(R.drawable.ic_theme_white)
        }
    } else {
        this.setImageResource(R.drawable.ic_theme_white)
    }
    scaleType = ImageView.ScaleType.CENTER_CROP
}

@BindingAdapter("app:uriProfileImage")
fun ImageView.bindUriProfileImage(uri: Uri?) {
    if (uri != null) {
        load(uri) {
            error(R.drawable.ic_profile_empty)
            transformations(CircleCropTransformation())
        }
    } else {
        this.setImageResource(R.drawable.ic_profile_empty)
    }
    scaleType = ImageView.ScaleType.CENTER_CROP
}

@BindingAdapter("app:urlCircleImage")
fun ImageView.bindUrlCircleImage(url: String?) {
    elevation = 10F
    load(url) {
        placeholder(R.drawable.ic_theme_white)
        error(R.drawable.ic_theme_white)
        transformations(CircleCropTransformation())
    }
    scaleType = ImageView.ScaleType.CENTER_CROP
}

/*** ConstraintLayout ***/
@BindingAdapter("app:backgroundAlbumImage")
fun ConstraintLayout.bindBackgroundAlbumImage(url: String) {
    Glide.with(this)
        .asBitmap()
        .placeholder(R.drawable.ic_theme_white)
        .error(R.drawable.ic_theme_white)
        .load(url)
        .centerCrop()
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                background = BitmapDrawable(resources, resource)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                background = errorDrawable
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                background = placeholder
            }
        })
}

/*** TextView ***/
@BindingAdapter("app:changeTextColor")
fun TextView.bindChangeTextColor(uri: String) {
    adjustTextColorFromBackgroundUri(context, uri.toUri(), this)
}

/*** EditText ***/
@BindingAdapter("app:normalEditText")
fun EditText.bindEditTextCustomTheme(text: String) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    highlightColor = resources.getColor(R.color.colorPrimary, context.theme)
    textCursorDrawable = ContextCompat.getDrawable(context, R.drawable.shape_edit_text_cursor)
    val customTextSelectHandle = ContextCompat.getDrawable(context, R.drawable.shape_edit_text_handle)
    val customTextSelectHandleLeft = ContextCompat.getDrawable(context, R.drawable.shape_edit_text_handle_left)
    val customTextSelectHandleRight = ContextCompat.getDrawable(context, R.drawable.shape_edit_text_handle_right)
    if (customTextSelectHandle != null) {
        setTextSelectHandle(customTextSelectHandle)
    }
    if (customTextSelectHandleLeft != null) {
        setTextSelectHandleLeft(customTextSelectHandleLeft)
    }
    if (customTextSelectHandleRight != null) {
        setTextSelectHandleRight(customTextSelectHandleRight)
    }
}

/*** TextInputLayout ***/
@BindingAdapter("app:validateEmail")
fun TextInputLayout.bindValidateEmail(authUiState: AuthUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
    when (authUiState.loginEmailValidState) {
        InputValidState.VALID -> {
            helperText = ""
            error = ""
            defaultHintTextColor = color
        }

        InputValidState.BLANK -> {
            error = "이메일을 입력해주세요."
        }

        InputValidState.NONE -> {
            error = "올바른 이메일 형식이 아닙니다."
        }

        else -> {}
    }
}

@BindingAdapter("app:validatePassword")
fun TextInputLayout.bindValidatePassword(authUiState: AuthUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
    when (authUiState.loginPasswordValidState) {
        InputValidState.VALID -> {
            helperText = ""
            error = ""
            defaultHintTextColor = color
        }

        InputValidState.BLANK -> {
            error = "비밀번호를 입력해주세요."
        }

        InputValidState.NONE -> {
            error = "영문, 숫자, 특수문자 포함 8~16자로 입력해주세요."
        }

        else -> {}
    }
}

@BindingAdapter("app:validateJoinEmail")
fun TextInputLayout.bindValidateJoinEmail(authUiState: AuthUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
    when (authUiState.joinEmailValidState) {
        InputValidState.VALID -> {
            helperText = "올바른 이메일 형식입니다."
            error = ""
            defaultHintTextColor = color
            setHelperTextColor(color)
        }

        InputValidState.BLANK -> {
            error = "이메일을 입력해주세요."
        }

        InputValidState.NONE -> {
            error = "올바른 이메일 형식이 아닙니다."
        }

        else -> {}
    }
}

@BindingAdapter("app:validateJoinName")
fun TextInputLayout.bindValidateJoinName(authUiState: AuthUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
    when (authUiState.joinNameValidState) {
        InputValidState.VALID -> {
            helperText = "올바른 닉네임 형식입니다."
            error = ""
            defaultHintTextColor = color
            setHelperTextColor(color)
        }

        InputValidState.BLANK -> {
            error = "닉네임을 입력해주세요."
        }

        InputValidState.NONE -> {
            error = "올바른 닉네임 형식이 아닙니다."
        }

        else -> {}
    }
}

@BindingAdapter("app:validateJoinPhoneNumber")
fun TextInputLayout.bindValidateJoinPhoneNumber(authUiState: AuthUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
    when (authUiState.joinPhoneNumberValidState) {
        InputValidState.VALID -> {
            helperText = "올바른 전화번호 형식입니다."
            error = ""
            defaultHintTextColor = color
            setHelperTextColor(color)
        }

        InputValidState.BLANK -> {
            error = "전화번호를 입력해주세요."
        }

        InputValidState.NONE -> {
            error = "올바른 전화번호 형식이 아닙니다."
        }

        else -> {}
    }
}

@BindingAdapter("app:validateJoinPassword")
fun TextInputLayout.bindValidateJoinPassword(authUiState: AuthUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
    when (authUiState.joinPasswordValidState) {
        InputValidState.VALID -> {
            helperText = "올바른 비밀번호입니다."
            error = ""
            defaultHintTextColor = color
            setHelperTextColor(color)
        }

        InputValidState.BLANK -> {
            error = "비밀번호를 입력해주세요."
        }

        InputValidState.NONE -> {
            error = "영문, 숫자, 특수문자 포함 8~16자로 입력해주세요."
        }

        else -> {}
    }
}

@BindingAdapter("app:validateJoinPasswordAgain")
fun TextInputLayout.bindValidateJoinPasswordAgain(authUiState: AuthUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
    when (authUiState.joinPasswordAgainValidState) {
        InputValidState.VALID -> {
            helperText = "비밀번호가 일치합니다."
            error = ""
            defaultHintTextColor = color
            setHelperTextColor(color)
        }

        InputValidState.BLANK -> {
            error = "비밀번호를 입력해주세요."
        }

        InputValidState.NONE -> {
            error = "비밀번호가 다릅니다."
        }

        else -> {}
    }
}

@BindingAdapter("app:validateJoinProfileMessage")
fun TextInputLayout.bindValidateJoinProfileMessage(authUiState: AuthUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
}

@BindingAdapter("app:validateUpdateName")
fun TextInputLayout.bindValidateUpdateName(settingUiState: SettingUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
    when (settingUiState.updateUserNameValidState) {
        InputValidState.VALID -> {
            helperText = "올바른 닉네임 형식입니다."
            error = ""
            defaultHintTextColor = color
            setHelperTextColor(color)
        }

        InputValidState.BLANK -> {
            error = "닉네임을 입력해주세요."
        }

        InputValidState.NONE -> {
            error = "올바른 닉네임 형식이 아닙니다."
        }

        else -> {}
    }
}

@BindingAdapter("app:validateUpdateProfileMessage")
fun TextInputLayout.bindValidateUpdateProfileMessage(settingUiState: SettingUiState) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
    val color = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
    cursorColor = color
}

/*** CardView ***/
@BindingAdapter("app:cardViewAlbumImage")
fun CardView.bindCardViewAlbumImage(url: String) {

}

/*** RecyclerView ***/
@BindingAdapter("adapter")
fun RecyclerView.bindAdapter(adapter: RecyclerView.Adapter<*>?) {
    this.adapter = adapter
}

@BindingAdapter("submitData")
fun <T> RecyclerView.submitData(items: List<T>?) {
    val adapter = this.adapter as? ListAdapter<T, *> ?: return
    adapter.submitList(items ?: emptyList())
}

/*** CustomCalendarVIew ***/
@BindingAdapter("app:startDate")
fun setStartDate(view: CustomCalendarView, date: LocalDate?) {
    if (view.selectedStartDate != date) {
        view.selectedStartDate = date
    }
}

@BindingAdapter("app:endDate")
fun setEndDate(view: CustomCalendarView, date: LocalDate?) {
    if (view.selectedEndDate != date) {
        view.selectedEndDate = date
    }
}
@BindingAdapter("app:periodLength")
fun setPeriod(view:CustomCalendarView, value : Int){
    view.maxDateRange = value
}

@BindingAdapter("android:text")
fun setIntAsString(view: TextView, value: Int) {
    view.text = value.toString()
}

