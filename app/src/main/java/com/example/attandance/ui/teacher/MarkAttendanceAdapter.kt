package com.example.attandance.ui.teacher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attandance.data.model.AttendanceRecord
import com.example.attandance.databinding.ItemMarkStudentBinding

class MarkAttendanceAdapter(
    private var records: List<AttendanceRecord> = emptyList(),
    private val onStatusChanged: (Int, String) -> Unit
) : RecyclerView.Adapter<MarkAttendanceAdapter.ViewHolder>() {

    fun submitList(newList: List<AttendanceRecord>) {
        records = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMarkStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position], position)
    }

    override fun getItemCount(): Int = records.size

    inner class ViewHolder(private val binding: ItemMarkStudentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: AttendanceRecord, pos: Int) {
            binding.tvMarkStudentName.text = record.studentName
            binding.tvMarkRollNo.text = "Roll No: ${record.rollNumber}"

            when (record.status) {
                "PRESENT" -> binding.rbPresent.isChecked = true
                "ABSENT" -> binding.rbAbsent.isChecked = true
                "ON_LEAVE" -> binding.rbLeave.isChecked = true
                else -> binding.rbPresent.isChecked = true
            }

            binding.rgStatus.setOnCheckedChangeListener { _, checkedId ->
                val newStatus = when (checkedId) {
                    binding.rbPresent.id -> "PRESENT"
                    binding.rbAbsent.id -> "ABSENT"
                    binding.rbLeave.id -> "ON_LEAVE"
                    else -> "PRESENT"
                }
                onStatusChanged(pos, newStatus)
            }
        }
    }
}