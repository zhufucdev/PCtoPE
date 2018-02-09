package com.zhufuc.pctope.Utils

import com.zhufuc.pctope.Adapters.JSONEditingAdapter

/**
 * Created by zhufu on 17-8-28.
 */

object TextureCompat {
    const val fullPC = "Found:full PC pack."
    const val brokenPE = "Found:broken PE pack."
    const val brokenPC = "Found:broken PC pack."
    const val fullPE = "Found:full PE pack."

    const val JSON_MANIFEST = 1
    const val JSON_BLOCKS = 2

    object MANIFEST{
        const val name = 3
        const val description = 4
        const val uuid = 5
        const val version = 6
        val NeededItems = listOf(Textures.JSONInfo(name,"Name","","path:header/name"), Textures.JSONInfo(description,"Description","","path:header/description")
                                            , Textures.JSONInfo(uuid,"UUID","","path:header/uuid"), Textures.JSONInfo(version,"Version","","path:header/version"))
    }
}
