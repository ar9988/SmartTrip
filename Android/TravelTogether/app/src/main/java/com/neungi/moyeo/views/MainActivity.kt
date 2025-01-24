package com.neungi.moyeo.views

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseActivity
import com.neungi.moyeo.databinding.ActivityMainBinding
import com.neungi.moyeo.views.aiplanning.AIPlanningViewModel
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import com.neungi.moyeo.views.setting.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModel: MainViewModel by viewModels()
    private val aiPlanningViewModel: AIPlanningViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val tripViewModel: TripViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val albumViewModel: AlbumViewModel by viewModels()
    private val settingViewModel: SettingViewModel by viewModels()
    private lateinit var navController: NavController
    private val navHostFragment: NavHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.fcv_main) as NavHostFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.vm = viewModel
        binding.lifecycleOwner = this

        setBottomNavigationBar()
    }

    private fun setBottomNavigationBar() {
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph_main)
        with(binding.bnvMain) {
            setupWithNavController(navController)
            background = null
            menu.getItem(1).isEnabled = false
        }
        binding.fabOrder.setOnClickListener {

        }
    }
}