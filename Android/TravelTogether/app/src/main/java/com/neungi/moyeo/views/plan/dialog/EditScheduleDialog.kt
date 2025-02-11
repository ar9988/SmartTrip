package com.neungi.moyeo.views.plan.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.neungi.domain.model.ScheduleData
import com.neungi.moyeo.R

class EditScheduleDialog(
    private val context: Context,
    private val scheduleData: ScheduleData,
    private val onEdit: (ScheduleData) -> Unit,
    private val onDelete: (Int) -> Unit
) {
    private lateinit var dialog: AlertDialog

    fun show() {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_schedule, null)

        setupDialogViews(dialogView)
        createAndShowDialog(dialogView)
    }

    private fun setupDialogViews(dialogView: View) {
        with(dialogView) {
            val titleTextView = findViewById<EditText>(R.id.et_dialog_title)
            val durationEditText = findViewById<EditText>(R.id.et_duration)
            val closeBtn = findViewById<ImageButton>(R.id.button_dialog_edit_close)
            val editBtn = findViewById<Button>(R.id.button_confirm)
            val deleteBtn = findViewById<Button>(R.id.button_delete)

            // Initialize views
            deleteBtn.visibility = View.VISIBLE
            editBtn.text = "수정"
            titleTextView.setText(scheduleData.placeName)
            durationEditText.setText(scheduleData.duration.toString())

            // Setup click listeners
            closeBtn.setOnClickListener { dialog.dismiss() }

            deleteBtn.setOnClickListener {
                onDelete(scheduleData.scheduleId)
                dialog.dismiss()
            }

            editBtn.setOnClickListener {
                val newDuration = durationEditText.text.toString().toIntOrNull() ?: 0
                val newPlaceName = titleTextView.text.toString()

                val editedData = scheduleData.copy(
                    duration = newDuration,
                    placeName = newPlaceName
                )

                onEdit(editedData)
                dialog.dismiss()
            }
        }
    }

    private fun createAndShowDialog(dialogView: View) {
        dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.show()
    }
}