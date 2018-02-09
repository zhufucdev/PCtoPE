package com.zhufuc.pctope.Utils

import com.zhufuc.pctope.Env.EnvironmentCalculations
import org.json.JSONObject

/**
 * Created by zhufu on 1/26/18.
 */
class Block(val name: String,val textureInfo: BlockTextureInfo?,val carriedTextureInfo: BlockTextureInfo?,val isotropic : String = "",val sound: String = "",val blockShape : String = ""){
    val isEmpty : Boolean
        get() = (textureInfo == null || textureInfo.isEmpty) && (carriedTextureInfo == null || carriedTextureInfo.isEmpty) && isotropic.isEmpty() && sound.isEmpty() && blockShape.isEmpty()
    class BlockTextureInfo(val BlockJSON : String,val TerrainBlockJSON: String){
        /**
         * @BlockJSON  part of 'block.json' file content
         */
        val blockJSON = mJSON(BlockJSON)
        val terrainBlockJSON = mJSON(mJSON(TerrainBlockJSON).get("texture_data"))

        val isOriginTexture: Boolean
        get() =  blockJSON.rootType == mJSON.TYPE_VALUE

        var up : BlockTextureValue
        var down : BlockTextureValue
        var side : BlockTextureValue
        var origin : BlockTextureValue? = null
        init {
            val label_up = blockJSON.get("up")
            up = BlockTextureValue(label_up, terrainBlockJSON.get(label_up))

            val label_down = blockJSON.get("down")
            down = BlockTextureValue(label_down,terrainBlockJSON.get(label_down))

            val label_side = blockJSON.get("side")
            side = BlockTextureValue(label_side,terrainBlockJSON.get(label_side))

            if (isOriginTexture)
                origin = BlockTextureValue(BlockJSON,terrainBlockJSON.get(BlockJSON))
        }


        class BlockTextureValue(val label: String,val value: String){
            val isEmpty: Boolean
            get() = label.isEmpty() || noRepeatPaths.isEmpty()

            private var noRepeatPaths : List<String> = listOf()
            init {
                val json = mJSON(mJSON(value).get("textures"))
                if (json.toString() != mJSON.DATA_NOT_FOUND_MSG) {
                    when {
                        json.rootType == mJSON.TYPE_VALUE -> {
                            noRepeatPaths = listOf(json.get(""))
                        }
                        json.rootType == mJSON.TYPE_ARRAY -> {
                            val paths = ArrayList<String>()
                            json.list().forEach {
                                val type = json.getType(it)
                                if (type == mJSON.TYPE_VALUE) {
                                    paths.add(json.get(it))
                                } else if (type == mJSON.TYPE_OBJECT) {
                                    paths.add(json.get("$it/path"))
                                }
                            }
                            noRepeatPaths = EnvironmentCalculations.getNoRepeat(paths)
                        }
                        else -> noRepeatPaths = listOf(json.get("path"))
                    }
                }
            }

            fun getNoRepeatPaths() : List<String> = noRepeatPaths
        }

        val isEmpty: Boolean
        get() = (!isOriginTexture && (up.isEmpty && side.isEmpty && down.isEmpty)) || (isOriginTexture && origin!!.isEmpty)
    }

    fun toJSONObject() : JSONObject {
        val result = JSONObject()
        if (!(textureInfo == null || textureInfo.isEmpty)) {
            if (textureInfo.isOriginTexture) {
                result.put("textures", textureInfo.origin!!.label)
            }
            else {
                val textures = JSONObject()
                textures.put("up",textureInfo.up.label)
                textures.put("down",textureInfo.down.label)
                textures.put("side",textureInfo.side.label)
                result.put("textures",textures)
            }
        }

        if (!(carriedTextureInfo == null || carriedTextureInfo.isEmpty)){
            if (carriedTextureInfo.isOriginTexture)
                result.put("carried_textures", carriedTextureInfo.toString())
            else {
                val textures = JSONObject()
                textures.put("up",carriedTextureInfo.up)
                textures.put("down",carriedTextureInfo.down)
                textures.put("side",carriedTextureInfo.side)
                result.put("carried_textures",textures)
            }
        }

        if (!isotropic.isEmpty()){
            result.put("isotropic",isotropic)
        }

        if (!sound.isEmpty()){
            result.put("sound",sound)
        }

        if (!blockShape.isEmpty()){
            result.put("blockshape",blockShape)
        }
        return result
    }

    companion object {
        fun getBlockByJSON(nameSet: String,BlockJSON: String,TerrainBlockJSON: String) : Block{
            val block = mJSON(BlockJSON)
            //temp vars
            val textureInfo = BlockTextureInfo(block.get("$nameSet/textures"),TerrainBlockJSON)
            val carriedTextureInfo = BlockTextureInfo(block.get("$nameSet/carried_textures"),TerrainBlockJSON)
            val isotropic: String = block.get("$nameSet/isotropic")
            val sound: String = block.get("$nameSet/sound")
            val blockShape: String = block.get("$nameSet/blockshape")
            //result
            return Block(nameSet,textureInfo, carriedTextureInfo, isotropic, sound, blockShape)
        }
    }

    object BlockShapeInfo{
        private val shapeList = listOf("invisible","cross_texture","tree","bed","rail","piston","block_half","torch"
                ,"stairs","chest","red_dust","rows","door","ladder","lever","top_snow","cactus","fence","repeater"
                ,"iron_fence","double_side_fence","stem","vince","fence_gate","lilypad","brewing_stand"
                ,"cauldron","portal_frame","end_rod","cocoa","beacon","tripwire_hook","tripwire","wall"
                ,"flower_pot","anvil","comparator","structure_void","hopper","chorus_plant","chorus_flower"
                ,"slime_block","dragon_egg","shulker_box","command_block","terracotta","frame")

        const val BLOCK_SHAPE_INVISIBLE = 0
        const val BLOCK_SHAPE_CROSS_TEXTURE = 1
        const val BLOCK_SHAPE_TREE = 2
        const val BLOCK_SHAPE_BED = 3
        const val BLOCK_SHAPE_RAILWAY = 4
        const val BLOCK_SHAPE_PISTON = 5
        const val BLOCK_SHAPE_HALF = 6
        const val BLOCK_SHAPE_TORCH = 7
        const val BLOCK_SHAPE_STAIRS = 8
        const val BLOCK_SHAPE_CHEST = 9
        const val BLOCK_SHAPE_RED_DUST = 10
        const val BLOCK_SHAPE_ROWS = 11
        const val BLOCK_SHAPE_DOOR = 12
        const val BLOCK_SHAPE_LADDER = 13
        const val BLOCK_SHAPE_LEVER = 14
        const val BLOCK_SHAPE_TOP_SNOW = 15
        const val BLOCK_SHAPE_CACTUS = 16
        const val BLOCK_SHAPE_FENCE = 17
        const val BLOCK_SHAPE_REPEATER = 18
        const val BLOCK_SHAPE_IRON_FENCE = 19
        const val BLOCK_SHAPE_DOUBLE_SIDE_FENCE = 20
        const val BLOCK_SHAPE_STEM = 21
        const val BLOCK_SHAPE_VINE = 22
        const val BLOCK_SHAPE_FENCE_GATE = 23
        const val BLOCK_SHAPE_LILYPAD = 24
        const val BLOCK_SHAPE_BREWING_STAND = 25
        const val BLOCK_SHAPE_CAULDRON = 26
        const val BLOCK_SHAPE_PORTAL_FRAME = 27
        const val BLOCK_SHAPE_END_ROD = 28
        const val BLOCK_SHAPE_COCOA = 29
        const val BLOCK_SHAPE_BEACON = 30
        const val BLOCK_SHAPE_TRIPWIRE_HOOK = 31
        const val BLOCK_SHAPE_TRIPWIRE = 32
        const val BLOCK_SHAPE_WALL = 33
        const val BLOCK_SHAPE_FLOWER_POT = 34
        const val BLOCK_SHAPE_ANVIL = 35
        const val BLOCK_SHAPE_COMPARATOR = 36
        const val BLOCK_SHAPE_STRUCTURE_VOID = 37
        const val BLOCK_SHAPE_HOPPER = 38
        const val BLOCK_SHAPE_CHORUS_PLANT = 39
        const val BLOCK_SHAPE_CHORUS_FLOWER = 40
        const val BLOCK_SHAPE_SLIME_BLOCK = 41
        const val BLOCK_SHAPE_DRAGON_EGG = 42
        const val BLOCK_SHAPE_DOUBLE_PLANT_POLY = 43
        const val BLOCK_SHAPE_FACING_BLOCK = 44
        const val BLOCK_SHAPE_FIRE = 45
        const val BLOCK_SHAPE_SHULKER_BOX = 46
        const val BLOCK_SHAPE_COMMAND_BLOCK = 47
        const val BLOCK_SHAPE_TERRACOTTA = 48
        const val BLOCK_SHAPE_FRAME = 49

        fun getValueByID(id : Int) : String = shapeList[id]
        fun getIDByValue(value : String) : Int = shapeList.indexOf(value)
    }
}