package com.hardik.customimageloading.ui.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hardik.customimageloading.databinding.ItemImageBinding
import com.hardik.customimageloading.imageLoading.ImageLoaderInitializer
import com.hardik.customimageloading.models.UnsplashImage

class ImageAdapter(private var screenWidth: Int) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val photos: MutableList<UnsplashImage> = mutableListOf()

    fun addPhotos(newPhotos: List<UnsplashImage>) {
        photos.addAll(newPhotos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding =
            ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val photo = photos[position]
        val targetWidth = screenWidth * 0.25 // 80% of screen width
        val targetHeight = calculateHeight(photo.width!!, photo.height!!, targetWidth)
        ImageLoaderInitializer.get().load(photo.urls?.thumb)
            .resize(targetWidth.toInt(), targetHeight.toInt())
            .placeholder(ColorDrawable(Color.parseColor(photo.color)))
            .into(holder.binding.imageView)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

    private fun calculateHeight(originalWidth: Int, originalHeight: Int, targetWidth: Double): Double {
        return (originalHeight.toFloat() / originalWidth.toFloat()) * targetWidth
    }
}
