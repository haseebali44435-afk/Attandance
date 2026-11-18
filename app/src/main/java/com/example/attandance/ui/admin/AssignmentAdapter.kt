package com.example.attandance.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.attandance.data.model.TeacherAssignment
import com.example.attandance.databinding.ItemAssignmentBinding

class AssignmentAdapter(
    private var list: List<TeacherAssignment> = emptyList(),
    private val onDeleteClick: ((TeacherAssignment) -> Unit)? = null
) : RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {

    fun submitList(newList: List<TeacherAssignment>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAssignmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemAssignmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TeacherAssignment) {
            binding.tvAssignTeacherName.text = item.teacherName
            binding.tvAssignSubjectName.text = "Subject: ${item.subjectName} (${item.subjectCode})"
            binding.tvAssignMeta.text = "${item.departmentName} | Semester: ${item.semesterId}"

            binding.root.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add("Remove Assignment")
                popup.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.title == "Remove Assignment") {
                        onDeleteClick?.invoke(item)
                    }
                    true
                }
                popup.show()
            }
        }
    }
}