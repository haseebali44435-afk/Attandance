package com.example.attandance.ui.teacher

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attandance.data.model.TeacherAssignment
import com.example.attandance.databinding.ItemTeacherClassBinding
import com.example.attandance.utils.Constants

class TeacherClassAdapter(
    private var list: List<TeacherAssignment> = emptyList(),
    private val onTakeAttendanceClick: (TeacherAssignment) -> Unit
) : RecyclerView.Adapter<TeacherClassAdapter.ViewHolder>() {

    fun submitList(newList: List<TeacherAssignment>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTeacherClassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemTeacherClassBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TeacherAssignment) {
            binding.tvClassSubjectName.text = item.subjectName
            binding.tvClassSubjectCode.text = "Code: ${item.subjectCode}"
            binding.tvClassMeta.text = "${item.departmentName} | Semester: ${item.semesterId}"

            binding.btnTakeAttendance.setOnClickListener {
                onTakeAttendanceClick(item)
            }
        }
    }
}