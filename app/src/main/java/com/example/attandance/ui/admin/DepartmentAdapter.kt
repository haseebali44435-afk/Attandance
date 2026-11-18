package com.example.attandance.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.attandance.data.model.Department
import com.example.attandance.databinding.ItemDepartmentBinding

class DepartmentAdapter(
    private var list: List<Department> = emptyList(),
    private val onEditClick: ((Department) -> Unit)? = null,
    private val onDeleteClick: ((Department) -> Unit)? = null
) : RecyclerView.Adapter<DepartmentAdapter.ViewHolder>() {

    fun submitList(newList: List<Department>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDepartmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemDepartmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dept: Department) {
            binding.tvDeptName.text = dept.name
            binding.tvDeptCode.text = "Code: ${dept.code}"

            binding.root.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menu.add("Edit Department")
                popup.menu.add("Delete Department")
                popup.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.title == "Edit Department") {
                        onEditClick?.invoke(dept)
                    } else if (menuItem.title == "Delete Department") {
                        onDeleteClick?.invoke(dept)
                    }
                    true
                }
                popup.show()
            }
        }
    }
}