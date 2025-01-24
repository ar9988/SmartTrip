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
import com.neungi.moyeo.R

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
