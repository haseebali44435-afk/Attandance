package com.example.attandance.ui.student

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.attandance.data.model.Subject
import com.example.attandance.databinding.ItemStudentSubjectBinding

class StudentSubjectAdapter(
    private var list: List<Subject> = emptyList(),
    private val onItemClick: (Subject) -> Unit
) : RecyclerView.Adapter<StudentSubjectAdapter.ViewHolder>() {

    fun submitList(newList: List<Subject>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemStudentSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subject: Subject) {
            binding.tvStudentCardSubject.text = subject.name
            binding.tvStudentCardPercent.text = "0.0%"
            binding.tvStudentCardAttended.text = "Attended 0 of ${subject.totalClassesHeld} classes"
            binding.progressStudentCard.progress = 0

            binding.root.setOnClickListener { onItemClick(subject) }
        }
    }
}