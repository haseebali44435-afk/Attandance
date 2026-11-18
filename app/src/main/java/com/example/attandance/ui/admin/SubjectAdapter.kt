package com.example.attandance.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.attandance.data.model.Subject
import com.example.attandance.databinding.ItemSubjectBinding

class SubjectAdapter(
    private var list: List<Subject> = emptyList(),
    private val onEditClick: ((Subject) -> Unit)? = null,
    private val onDeleteClick: ((Subject) -> Unit)? = null
) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {

    fun submitList(newList: List<Subject>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemSubjectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(subject: Subject) {
            binding.tvSubjectName.text = subject.name
            binding.tvSubjectMeta.text = "Code: ${subject.code} | Total Classes: ${subject.totalClassesHeld}"

            binding.root.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add("Edit Subject")
                popup.menu.add("Delete Subject")
                popup.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.title == "Edit Subject") {
                        onEditClick?.invoke(subject)
                    } else if (menuItem.title == "Delete Subject") {
                        onDeleteClick?.invoke(subject)
                    }
                    true
                }
                popup.show()
            }
        }
    }
}