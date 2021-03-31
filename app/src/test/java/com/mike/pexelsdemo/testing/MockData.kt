@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.mike.pexelsdemo.testing

import com.mike.pexelsdemo.model.Photo
import com.mike.pexelsdemo.model.PhotoSrc
import com.mike.pexelsdemo.model.PhotosResponse
import java.util.concurrent.ThreadLocalRandom

object MockData {
    fun randomInt(min: Int, max: Int): Int {
        return ThreadLocalRandom.current().nextInt(min, max + 1)
    }

    fun randomBoolean(): Boolean {
        return randomInt(0, 1) == 1
    }

    fun randomString(length: Int): String {
        val builder = StringBuilder()
        for (i in 0 until length) {
            builder.append((randomInt(97, 122)).toChar())
        }

        return builder.toString()
    }

    fun randomUrl(): String {
        return "https://${randomString(3)}.${randomString(3)}.com/${randomString(10)}"
    }

    fun randomPhoto(): Photo {
        return Photo(
            id = randomInt(1, 65536),
            width = randomInt(2000, 6000),
            height = randomInt(2000, 6000),
            url = randomUrl(),
            photographer = "${randomString(randomInt(5, 10))} ${randomString(randomInt(5, 10))}",
            photographerUrl = randomUrl(),
            photographerId = randomString(6),
            avgColor = "#${randomString(6)}",
            src = PhotoSrc(
                original = randomUrl(),
                large2x = randomUrl(),
                large = randomUrl(),
                medium = randomUrl(),
                small = randomUrl(),
                portrait = randomUrl(),
                landscape = randomUrl(),
                tiny = randomUrl(),
            ),
            liked = randomBoolean(),
        )
    }

    fun randomPhotosResponse(count: Int = 5, total: Int = count * 10, page: Int = 1, hasNext: Boolean = count > 0): PhotosResponse {
        val photos = mutableListOf<Photo>()
        for (i in 1 until count) {
            photos.add(randomPhoto())
        }

        return PhotosResponse(
            totalResults = total,
            page = page,
            perPage = 30,
            photos = photos,
            nextPage = if (hasNext) randomUrl() else null
        )
    }
}