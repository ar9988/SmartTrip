package com.neungi.moyeo.util

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.textfield.TextInputLayout
import com.neungi.moyeo.R
import com.neungi.moyeo.views.auth.viewmodel.AuthUiState

/*** ImageView***/
@BindingAdapter("app:urlPhotoImage")
fun ImageView.bindUrlPhotoImage(url: String?) {
    if (url != null) {
        load(url) {
            error(R.drawable.ic_profile)
        }
    } else {
        this.setImageResource(R.drawable.ic_profile)
    }
    scaleType = ImageView.ScaleType.CENTER_CROP
}

@BindingAdapter("app:uriPhotoImage")
fun ImageView.bindUriPhotoImage(uri: Uri?) {
    if (uri != null) {
        load(uri) {
            error(R.drawable.ic_profile)
        }
    } else {
        this.setImageResource(R.drawable.ic_profile)
    }
    scaleType = ImageView.ScaleType.CENTER_CROP
}

/*** ConstraintLayout ***/
@BindingAdapter("app:backgroundAlbumImage")
fun ConstraintLayout.bindBackgroundAlbumImage(url: String) {
    Glide.with(this)
        .asBitmap()
        .load(url)
        .centerCrop()
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                background = BitmapDrawable(resources, resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })
}

/*** EditText ***/
@RequiresApi(Build.VERSION_CODES.Q)
@BindingAdapter("app:normalEditText")
fun EditText.bindEditTextCustomTheme(authUiState: AuthUiState) {
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
@RequiresApi(Build.VERSION_CODES.Q)
@BindingAdapter("app:validateEmail")
fun TextInputLayout.bindValidateEmail(authUiState: AuthUiState) {
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

@RequiresApi(Build.VERSION_CODES.Q)
@BindingAdapter("app:validatePassword")
fun TextInputLayout.bindValidatePassword(authUiState: AuthUiState) {
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

@RequiresApi(Build.VERSION_CODES.Q)
@BindingAdapter("app:validateJoinEmail")
fun TextInputLayout.bindValidateJoinEmail(authUiState: AuthUiState) {
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

@RequiresApi(Build.VERSION_CODES.Q)
@BindingAdapter("app:validateJoinName")
fun TextInputLayout.bindValidateJoinName(authUiState: AuthUiState) {
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

@RequiresApi(Build.VERSION_CODES.Q)
@BindingAdapter("app:validateJoinPhoneNumber")
fun TextInputLayout.bindValidateJoinPhoneNumber(authUiState: AuthUiState) {
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

@RequiresApi(Build.VERSION_CODES.Q)
@BindingAdapter("app:validateJoinPassword")
fun TextInputLayout.bindValidateJoinPassword(authUiState: AuthUiState) {
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

@RequiresApi(Build.VERSION_CODES.Q)
@BindingAdapter("app:validateJoinPasswordAgain")
fun TextInputLayout.bindValidateJoinPasswordAgain(authUiState: AuthUiState) {
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
