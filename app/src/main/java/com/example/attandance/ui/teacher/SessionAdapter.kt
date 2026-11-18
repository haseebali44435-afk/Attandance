package com.example.attandance.ui.teacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attandance.data.model.AttendanceSession
import com.example.attandance.databinding.ItemAttendanceSessionBinding
import com.example.attandance.utils.DateUtils

class SessionAdapter(
    private var list: List<AttendanceSession> = emptyList(),
    private val onItemClick: (AttendanceSession) -> Unit
) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    fun submitList(newList: List<AttendanceSession>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemAttendanceSessionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(session: AttendanceSession) {
            binding.tvSessionSubject.text = session.subjectName
            binding.tvSessionDate.text = DateUtils.formatDateTime(session.timestamp)
            binding.tvSessionTeacher.text = "By: ${session.teacherName}"
            binding.tvSessionCounts.text = "P: ${session.presentCount} | A: ${session.absentCount}"

            if (session.editCount > 0) {
                binding.tvSessionEditBadge.visibility = View.VISIBLE
                binding.tvSessionEditBadge.text = "Edited (${session.editCount}/1)"
            } else {
                binding.tvSessionEditBadge.visibility = View.GONE
            }

            binding.root.setOnClickListener { onItemClick(session) }
        }
    }
}