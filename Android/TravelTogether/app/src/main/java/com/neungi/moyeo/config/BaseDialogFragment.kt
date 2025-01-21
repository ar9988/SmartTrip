package com.neungi.moyeo.config

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

abstract class BaseDialogFragment<T : ViewDataBinding>(private val layoutId: Int) : DialogFragment() {

    private var _binding: T? = null
    protected val binding
        get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }

        _binding = DataBindingUtil.inflate<T>(inflater, layoutId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        requireContext().dialogFragmentResize(this, 0.9F, 0.7F)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun Context.dialogFragmentResize(
        dialogFragment: DialogFragment,
        width: Float,
        height: Float
    ) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val window = dialogFragment.dialog?.window
        if (Build.VERSION.SDK_INT < 30) {
            val displayWidth = resources.displayMetrics.widthPixels
            val displayHeight = resources.displayMetrics.heightPixels

            val x = (displayWidth * width).toInt()
            val y = (displayHeight * height).toInt()

            window?.setLayout(x, y)
        } else {
            val rect = windowManager.currentWindowMetrics.bounds

            val x = (rect.width() * width).toInt()
            val y = (rect.height() * height).toInt()

            window?.setLayout(x, y)
        }
    }

    protected fun showKeyboard(editText: EditText) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    protected fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun NavController.navigateSafely(
        @IdRes actionId: Int,
        args: Bundle? = null,
        navOptions: NavOptions? = null,
        navExtras: Navigator.Extras? = null
    ) {
        val action = currentDestination?.getAction(actionId) ?: graph.getAction(actionId)

        if ((action != null) && (currentDestination?.id != action.destinationId)) {
            navigate(actionId, args, navOptions, navExtras)
        }
    }
}