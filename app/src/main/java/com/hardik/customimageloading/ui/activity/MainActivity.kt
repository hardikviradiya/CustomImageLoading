package com.hardik.customimageloading.ui.activity

import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hardik.customimageloading.databinding.ActivityMainBinding
import com.hardik.customimageloading.ui.adapters.ImageAdapter
import com.hardik.customimageloading.ui.viewmodels.MainViewModel
import com.hardik.customimageloading.utils.Util

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ImageAdapter
    private val viewModel: MainViewModel by viewModels()
    private var currentPage = 1
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressBar.visibility = VISIBLE
        binding.recyclerView.visibility = GONE

        setupRecyclerView()

        observeViewModel()

        loadInitialData()
    }

    private fun setupRecyclerView() {
        adapter = ImageAdapter(Util().getScreenWidth())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val lastVisibleItemPosition =
                    layoutManager.findLastVisibleItemPositions(null).maxOrNull() ?: 0
                val totalItemCount = layoutManager.itemCount

                // Load more data when scrolled to the end and not already loading
                if (!isLoading && totalItemCount - lastVisibleItemPosition <= 20) {
                    loadMoreData()
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.photosLiveData.observe(this) { photos ->
            if(binding.recyclerView.visibility == GONE){
                binding.progressBar.visibility = GONE
                binding.recyclerView.visibility = VISIBLE
            }
            adapter.addPhotos(photos)
            isLoading = false
        }
    }

    fun loadInitialData() {
        isLoading = true
        viewModel.loadPhotos(currentPage)
    }

    fun loadMoreData() {
        isLoading = true
        currentPage++
        viewModel.loadPhotos(currentPage)
    }
}