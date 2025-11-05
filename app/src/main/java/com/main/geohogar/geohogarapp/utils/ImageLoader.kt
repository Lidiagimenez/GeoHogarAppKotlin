package com.main.geohogar.geohogarapp.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.geohogar.app.R

object ImageLoader {

    /**
     * Carga una imagen desde una URL relativa o absoluta
     * @param imageView: ImageView donde se cargará la imagen
     * @param imageUrl: URL de la imagen (puede ser relativa "/uploads/..." o absoluta)
     */
    fun loadImage(imageView: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground)
            return
        }

        // Construir URL completa si es relativa
        val fullUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            "${Constants.IMAGE_BASE_URL}$imageUrl"
        }

        Glide.with(imageView.context)
            .load(fullUrl)
            .placeholder(R.drawable.ic_launcher_foreground) // Imagen mientras carga
            .error(R.drawable.ic_launcher_foreground) // Imagen si falla
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cachear en disco
            .into(imageView)
    }

    /**
     * Carga una imagen con tamaño específico
     */
    fun loadImageWithSize(imageView: ImageView, imageUrl: String?, width: Int, height: Int) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground)
            return
        }

        val fullUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            "${Constants.IMAGE_BASE_URL}$imageUrl"
        }

        Glide.with(imageView.context)
            .load(fullUrl)
            .apply(RequestOptions()
                .override(width, height)
                .centerCrop()
            )
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    /**
     * Carga una imagen circular (para avatares, perfiles, etc.)
     */
    fun loadCircularImage(imageView: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground)
            return
        }

        val fullUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            "${Constants.IMAGE_BASE_URL}$imageUrl"
        }

        Glide.with(imageView.context)
            .load(fullUrl)
            .apply(RequestOptions()
                .circleCrop()
            )
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    /**
     * Pre-cargar imágenes en caché
     */
    fun preloadImage(imageView: ImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) return

        val fullUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            "${Constants.IMAGE_BASE_URL}$imageUrl"
        }

        Glide.with(imageView.context)
            .load(fullUrl)
            .preload()
    }
}
