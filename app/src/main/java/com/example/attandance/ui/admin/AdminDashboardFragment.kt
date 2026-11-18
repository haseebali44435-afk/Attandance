package com.example.attandance.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.attandance.R
import com.example.attandance.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStatCards()
        binding.swipeRefreshAdmin.setOnRefreshListener {
            viewModel.loadDashboardData()
            binding.swipeRefreshAdmin.isRefreshing = false
        }
    }

    private fun setupStatCards() {
        binding.cardDepts.tvStatTitle.text = "Departments"
        binding.cardDepts.tvStatValue.text = "0"
        binding.cardDepts.ivStatIcon.setImageResource(android.R.drawable.ic_menu_sort_by_size)

        binding.cardTeachers.tvStatTitle.text = "Teachers"
        binding.cardTeachers.tvStatValue.text = "0"
        binding.cardTeachers.ivStatIcon.setImageResource(android.R.drawable.ic_menu_myplaces)

        binding.cardStudents.tvStatTitle.text = "Students"
        binding.cardStudents.tvStatValue.text = "0"
        binding.cardStudents.ivStatIcon.setImageResource(android.R.drawable.ic_menu_agenda)

        binding.cardSessions.tvStatTitle.text = "Today Sessions"
        binding.cardSessions.tvStatValue.text = "0"
        binding.cardSessions.ivStatIcon.setImageResource(android.R.drawable.ic_menu_recent_history)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}