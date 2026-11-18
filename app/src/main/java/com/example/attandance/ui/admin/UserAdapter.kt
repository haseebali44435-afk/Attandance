package com.example.attandance.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.attandance.R
import com.example.attandance.data.model.Role
import com.example.attandance.data.model.User
import com.example.attandance.databinding.ItemUserBinding

class UserAdapter(
    private var users: List<User> = emptyList(),
    private val onEditClick: ((User) -> Unit)? = null,
    private val onDeleteClick: ((User) -> Unit)? = null,
    private val onApproveClick: ((User) -> Unit)? = null,
    private val onRejectClick: ((User) -> Unit)? = null
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun submitList(newList: List<User>) {
        users = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email

            if (user.status == "PENDING") {
                binding.tvUserRoleBadge.text = "PENDING TEACHER"
                val context = binding.root.context
                binding.tvUserRoleBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_warning)
                binding.tvUserRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.warning_orange))
                binding.tvUserDetail.text = "Dept: ${user.departmentName} (Awaiting Approval)"
            } else {
                binding.tvUserRoleBadge.text = user.role.name
                binding.tvUserDetail.text = if (user.role == Role.STUDENT) "Roll: ${user.rollNumber}" else user.departmentName

                val context = binding.root.context
                when (user.role) {
                    Role.ADMIN -> {
                        binding.tvUserRoleBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_role_admin)
                        binding.tvUserRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.role_admin))
                    }
                    Role.TEACHER -> {
                        binding.tvUserRoleBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_role_teacher)
                        binding.tvUserRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.role_teacher))
                    }
                    Role.STUDENT -> {
                        binding.tvUserRoleBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_role_student)
                        binding.tvUserRoleBadge.setTextColor(ContextCompat.getColor(context, R.color.role_student))
                    }
                }
            }

            binding.root.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                if (user.status == "PENDING") {
                    popup.menu.add("Approve Teacher Account")
                    popup.menu.add("Reject Teacher Registration")
                } else {
                    popup.menu.add("Edit User")
                    popup.menu.add("Delete User")
                }

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Approve Teacher Account" -> onApproveClick?.invoke(user)
                        "Reject Teacher Registration" -> onRejectClick?.invoke(user)
                        "Edit User" -> onEditClick?.invoke(user)
                        "Delete User" -> onDeleteClick?.invoke(user)
                    }
                    true
                }
                popup.show()
            }
        }
    }
}