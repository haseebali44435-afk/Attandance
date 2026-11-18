package com.example.attandance.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.attandance.R
import com.example.attandance.data.model.AttendanceSession
import com.example.attandance.databinding.ItemStudentHistoryBinding
import com.example.attandance.utils.DateUtils

class StudentHistoryAdapter(private var list: List<AttendanceSession> = emptyList()) :
    RecyclerView.Adapter<StudentHistoryAdapter.ViewHolder>() {

    fun submitList(newList: List<AttendanceSession>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    class ViewHolder(private val binding: ItemStudentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(session: AttendanceSession) {
            binding.tvHistorySubject.text = session.subjectName
            binding.tvHistoryDate.text = DateUtils.formatDateTime(session.timestamp)

            val context = binding.root.context
            val isPresent = session.records.firstOrNull()?.isPresent ?: true
            if (isPresent) {
                binding.tvHistoryStatusBadge.text = "PRESENT"
                binding.tvHistoryStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_present)
                binding.tvHistoryStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.present_green))
            } else {
                binding.tvHistoryStatusBadge.text = "ABSENT"
                binding.tvHistoryStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_absent)
                binding.tvHistoryStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.absent_red))
            }
        }
    }
}