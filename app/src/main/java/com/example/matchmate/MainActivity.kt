package com.example.matchmate

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.matchmate.data.api.RetrofitClient
import com.example.matchmate.data.local.AppDatabase
import com.example.matchmate.data.repository.UserRepository
import com.example.matchmate.databinding.ActivityMainBinding
import com.example.matchmate.ui.adapter.MatchAdapter
import com.example.matchmate.ui.main.MainViewModel
import com.example.matchmate.ui.main.MainViewModelFactory
import com.example.matchmate.ui.main.MatchFilter
import com.example.matchmate.utils.ConnectivityObserver

private const val PAGINATION_THRESHOLD = 3

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var connectivityObserver: ConnectivityObserver

    private val viewModel: MainViewModel by lazy {
        val userDao = AppDatabase.getInstance(applicationContext).userDao()
        val repository = UserRepository.getInstance(userDao, RetrofitClient.apiService)
        ViewModelProvider(this, MainViewModelFactory(repository))[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(getColor(R.color.primary_burgundy))
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
            val statusBarInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = statusBarInset)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = navBarInset)
            insets
        }

        val adapter = MatchAdapter(
            onAccept = { email -> viewModel.onAcceptClicked(email) },
            onDecline = { email -> viewModel.onDeclineClicked(email) }
        )
        binding.rvMatches.layoutManager = LinearLayoutManager(this)
        binding.rvMatches.adapter = adapter
        binding.rvMatches.setHasFixedSize(true)


        binding.rvMatches.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition >= totalItemCount - PAGINATION_THRESHOLD) {
                    viewModel.loadNextPage()
                }
            }
        })

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipPending -> MatchFilter.PENDING
                R.id.chipAccepted -> MatchFilter.ACCEPTED
                R.id.chipDeclined -> MatchFilter.DECLINED
                else -> MatchFilter.ALL
            }
            viewModel.onFilterSelected(filter)
        }

        viewModel.filteredUsers.observe(this) { users -> adapter.submitList(users) }

        connectivityObserver = ConnectivityObserver(applicationContext)
        connectivityObserver.start { viewModel.onNetworkRestored() }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityObserver.stop()
    }
}
