package com.neungi.moyeo.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
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
import java.time.LocalDate

/*** ImageView***/
@BindingAdapter("app:urlPhotoImage")
fun ImageView.bindUrlPhotoImage(url: String?) {
    if (url != null) {
        load(url)
    } else {
        this.setImageResource(R.drawable.bnv_setting)
    }
    scaleType = ImageView.ScaleType.CENTER_CROP
}

@BindingAdapter("app:uriPhotoImage")
fun ImageView.bindUriPhotoImage(uri: Uri?) {
    if (uri != null) {
        load(uri)
    } else {
        this.setImageResource(R.drawable.bnv_setting)
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

/*** TextInputLayout ***/
@BindingAdapter("app:validateEmail")
fun TextInputLayout.bindValidateEmail(authUiState: AuthUiState) {
    when (authUiState.loginEmailValidState) {
        InputValidState.VALID -> {
            helperText = ""
            error = ""
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
    when (authUiState.loginPasswordValidState) {
        InputValidState.VALID -> {
            helperText = ""
            error = ""
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

