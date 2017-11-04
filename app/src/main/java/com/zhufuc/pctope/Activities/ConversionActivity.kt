package com.zhufuc.pctope.Activities

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.SwitchCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import com.zhufuc.pctope.Utils.mLog
import android.view.animation.AnimationUtils
import android.widget.*
import com.zhufuc.pctope.Adapters.TutorialActivity
import com.zhufuc.pctope.Collectors.ActivityCollector

import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.*

import java.io.File

import com.zhufuc.pctope.Utils.TextureCompat.brokenPC

val layoutList = arrayOf(R.layout.tutorial_conversion_welcome,R.layout.tutorial_conversion_step1,R.layout.tutorial_conversion_step2)

class ConversionActivity : TutorialActivity(layoutList) {
    internal val finishIntent = Intent()

    private fun MakeErrorDialog(errorString: String) {
        //make up a error dialog
        val error_dialog = AlertDialog.Builder(this@ConversionActivity)
        error_dialog.setTitle(R.string.error)
        error_dialog.setMessage(this@ConversionActivity.getString(R.string.error_dialog) + errorString)
        error_dialog.setIcon(R.drawable.alert_octagram)
        error_dialog.setCancelable(false)
        error_dialog.setPositiveButton(R.string.close) { dialogInterface, i ->
            finishIntent.putExtra("Status_return", false)
            finish()
        }
        error_dialog.setNegativeButton(R.string.copy) { dialogInterface, i ->
            val copy = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            copy.text = errorString
            finish()
        }.show()
    }

    internal var skipUnzip: Boolean = false
    internal var isPreFinished: Boolean = false
    private lateinit var name: EditText
    private lateinit var description: EditText
    private var packname: String = ""
    private var packdescription: String = ""
    private var file: String = ""

    private var hasStep2BeenShown : Boolean = false
    private var hasIconBeenOverwritten : Boolean = false
    private var areAdvencedOptionsShown : Boolean = false
    private var isDoingConverting : Boolean = false

    private lateinit var conversion: TextureConversionUtils

    override fun onPageSwitched(){
        when (showingPostition){
            0 -> {
                //Views
                val fab_next = findViewById<FloatingActionButton>(R.id.tutorial_next)
                val loading = findViewById<LinearLayout>(R.id.tutorial_welcome_loading)
                getIntentInfo()

                if (!isPreFinished)
                    conversion = TextureConversionUtils(file,this)

                conversion.setOnUncompressListener(object : TextureConversionUtils.OnUncompressListener{
                    override fun onPreUncompress() {
                        fab_next.visibility = View.INVISIBLE
                        loading.visibility = View.VISIBLE
                    }

                    override fun inUncompressing() {}

                    override fun onPostUncompress(result: Boolean, version: String?) {
                        if (result) {
                            fab_next.show()
                            fab_next.setOnClickListener({
                                next()
                            })
                            isPreFinished = true
                        }
                        else{
                            val error = findViewById<LinearLayout>(R.id.tutorial_welcome_error_layout)
                            error.visibility = View.VISIBLE
                            doOnFail()
                        }
                        loading.visibility = View.INVISIBLE

                    }

                })

                conversion.setOnCrashListener(object : TextureConversionUtils.OnCrashListener{
                    override fun onCrash(errorContent: String) {
                        MakeErrorDialog(errorContent)
                    }
                })


                if (File(conversion.path).exists() && !(skipUnzip || isPreFinished)) {
                    val dialog = AlertDialog.Builder(this@ConversionActivity)
                    dialog.setTitle(R.string.overwrite_title)
                    dialog.setMessage(R.string.overwrite_content)
                    dialog.setCancelable(false)
                    dialog.setNegativeButton(R.string.skip) { dialog, which ->
                        skipUnzip = true
                        conversion.skipUnzip = skipUnzip
                        conversion.UncompressPack()
                    }
                    dialog.setPositiveButton(R.string.overwrite) { dialog, which ->
                        skipUnzip = false
                        conversion.skipUnzip = skipUnzip
                        conversion.UncompressPack()
                    }
                    dialog.show()
                } else if (!isPreFinished)
                    conversion.UncompressPack()
            }

            1 -> {
                //Init and add Watchers
                name = findViewById(R.id.conversion_name)
                description = findViewById(R.id.conversion_description)
                val next = findViewById<Button>(R.id.tutorial_next)
                name.addTextChangedListener(object : TextWatcher{
                    override fun afterTextChanged(s: Editable?) {

                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        packname = doDestFixing(s.toString())

                    }

                })
                description.addTextChangedListener(object : TextWatcher{
                    override fun afterTextChanged(s: Editable?) {

                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        packdescription = s.toString()
                    }

                })

                next.setOnClickListener({next()})
            }

            2 -> {
                val icon = findViewById<ImageView>(R.id.tutorial_icon_shower)
                val mcpackSwitcher = findViewById<SwitchCompat>(R.id.tutorial_mcpack_switcher)
                val blocker = findViewById<LinearLayout>(R.id.tutorial_loading_blocker)
                blocker.visibility = View.INVISIBLE
                loadIcon()
                val show = AnimationUtils.loadAnimation(this,R.anim.cards_show)
                if (!hasStep2BeenShown)
                    icon.startAnimation(show)
                hasStep2BeenShown = true

                icon.setOnClickListener({
                    val intent = Intent(ConversionActivity@this,FileChooserActivity::class.java)
                    intent.putStringArrayListExtra("format", arrayListOf("png","jpg"))
                    startActivityForResult(intent,0)
                })

                val next = findViewById<Button>(R.id.tutorial_next)
                next.setOnClickListener({

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        val animate = ViewAnimationUtils.createCircularReveal(blocker,blocker.width/2,blocker.height/2,0f,Math.hypot(blocker.width.toDouble(), blocker.height.toDouble()).toFloat())
                        animate.duration = 200
                        blocker.visibility = View.VISIBLE
                        animate.start()
                    } else {
                        val animate = AnimationUtils.loadAnimation(this,R.anim.cards_show)
                        blocker.startAnimation(animate)
                        blocker.visibility = View.VISIBLE
                    }


                    val loadingText = findViewById<TextView>(R.id.tutorial_loading_msg)
                    conversion.setConversionChangeListener(object : TextureConversionUtils.ConversionChangeListener{
                        override fun inDoingVersionDecisions() {
                            runOnUiThread({
                                loadingText.setText(R.string.please_wait)
                            })
                        }

                        override fun inDoingImageCompressions(whatsBeingCompressing: String) {
                            runOnUiThread({
                                loadingText.text = "${getString(R.string.resources_compression)}\n $whatsBeingCompressing"
                            })
                        }

                        override fun inDoingJSONWriting() {
                            runOnUiThread({
                                loadingText.setText(R.string.progress_writing_json)
                            })
                        }

                        override fun inDoingMcpackCompressing(file: String) {
                            runOnUiThread({
                                loadingText.text = "${getString(R.string.compressing_mcpack)}\n $file"
                            })
                        }

                        override fun onDone() {
                            finishIntent.putExtra("Status_return",true)
                            runOnUiThread({
                                loadingText.setText(R.string.done)
                            })
                            Looper.prepare()
                            Handler().postDelayed({ finish() },500)
                            Looper.loop()
                        }

                    })
                    if (packname=="") packname = getString(R.string.project_unnamed)

                    val mcpackPath : String = if(mcpackSwitcher.isChecked) "${Environment.getExternalStorageDirectory().path}/games/com.mojang/mcpacks/$packname.mcpack" else ""
                    conversion.compressFinalSize = compressFinalSize
                    Thread(Runnable { conversion.doConverting(packname,packdescription,mcpackPath) }).start()
                    isDoingConverting = true
                })

                val show_advenced = findViewById<Button>(R.id.tutorial_advenced_options_button)
                val advenced_layout = findViewById<LinearLayout>(R.id.tutorial_advenced_options_layout)
                val resource_compress = findViewById<LinearLayout>(R.id.tutorial_resource_compress)
                show_advenced.setOnClickListener({
                    if (areAdvencedOptionsShown){
                        advenced_layout.visibility = View.INVISIBLE
                        areAdvencedOptionsShown = false
                    }
                    else{
                        advenced_layout.visibility = View.VISIBLE
                        areAdvencedOptionsShown = true
                    }
                })
                resource_compress.setOnClickListener({
                    val dialog = BottomSheetDialog(this@ConversionActivity)
                    val dialogView = layoutInflater.inflate(R.layout.compression_dialog, null)

                    dialog.setContentView(dialogView)


                    val optionsBitmap = BitmapFactory.Options()
                    optionsBitmap.inSampleSize = 1

                    val bitmap = BitmapFactory.decodeFile(getIconOFResourceCompression(), optionsBitmap)
                    val confirm = dialogView.findViewById<Button>(R.id.compression_button_confirm) as Button

                    loadDialogLayout(dialogView, bitmap)

                    val spinner = dialogView.findViewById<Spinner>(R.id.compression_spinner)
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                            val options = resources.getStringArray(R.array.compression_options)

                            when (options[i]) {
                                "8x" -> compressSize = 8
                                "16x" -> compressSize = 16
                                "32x" -> compressSize = 32
                                "64x" -> compressSize = 64
                                "128x" -> compressSize = 128
                                "256x" -> compressSize = 256
                                "512x" -> compressSize = 512
                                else -> compressSize = 0
                            }
                            if (compressSize != 0) {
                                loadDialogLayout(dialogView, CompressImage.getBitmap(bitmap, compressSize, compressSize))
                            } else
                                loadDialogLayout(dialogView, bitmap)

                            if (compressSize > bitmap.width || compressSize > bitmap.height) {
                                confirm.isEnabled = false
                                confirm.setTextColor(resources.getColor(R.color.grey_primary))
                                Toast.makeText(this@ConversionActivity, R.string.compression_alert, Toast.LENGTH_SHORT).show()
                            } else {
                                confirm.isEnabled = true
                                confirm.setTextColor(resources.getColor(R.color.colorAccent))
                            }
                        }

                        override fun onNothingSelected(adapterView: AdapterView<*>) {
                            compressSize = 0
                        }
                    }

                    dialog.setOnCancelListener { compressSize = compressFinalSize }

                    dialog.show()

                    confirm.setOnClickListener {
                        compressFinalSize = compressSize
                        dialog.dismiss()
                    }
                })

            }
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (showingPostition > 0 && !isDoingConverting)
            back()
        else
            super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //Preload
        super.onCreate(savedInstanceState)
        ActivityCollector.addActivity(this)

        show(0)
        setResult(Activity.RESULT_OK, finishIntent)//We don't need to use it
        finishIntent.putExtra("Status_return",false)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityCollector.removeActivity(this)
    }

    private fun getIntentInfo() {
        val intent = intent
        skipUnzip = intent.getBooleanExtra("willSkipUnzipping", false)
        file = intent.getStringExtra("filePath")
    }

    fun loadIcon() {
        val icon = findViewById<ImageView>(R.id.tutorial_icon_shower)
        val bitmap = conversion.icon
        if (bitmap != null) {
            icon.setImageBitmap(bitmap)
            //Make a JOKE
            val text = findViewById<TextView>(R.id.conversion_icon_text)
            if (hasIconBeenOverwritten) text.text = "${text.text}?"
        } else {
            val finishBottom = findViewById<FloatingActionButton>(R.id.finishBottom)
            Snackbar.make(finishBottom, R.string.pack_icon_not_found, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        val choose = Intent(ConversionActivity@this,FileChooserActivity::class.java)
                        startActivityForResult(choose, 0)
                    }
                    .show()
        }

    }

    private var compressSize: Int = 0
    private var compressFinalSize: Int = 0

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(myContextWrapper(newBase).wrap())
    }

    fun getIconOFResourceCompression() : String{
        //Set Compression
        var image: File? = null
        var baseFrom: String? = null
        if (conversion.VerStr == TextureCompat.fullPC || conversion.VerStr == brokenPC)
            baseFrom = conversion.path + "/assets/minecraft/textures"
        else
            baseFrom = conversion.path + "/textures"
        //grass >> sword >> never mind
        image = FindFile.withKeywordOnce("grass_side.png", baseFrom)
        if (image == null) {
            image = FindFile.withKeywordOnce("iron_sword.png", baseFrom)
            if (image == null)
                image = FindFile.withKeywordOnce(".png", baseFrom)
        }
        return image!!.path
    }


    private fun loadDialogLayout(dialogView: View, bitmap: Bitmap?) {

        val spinner = dialogView.findViewById<Spinner>(R.id.compression_spinner)
        if (compressSize != 0) {
            when (compressSize) {
                8 -> spinner.setSelection(1)
                16 -> spinner.setSelection(2)
                32 -> spinner.setSelection(3)
                64 -> spinner.setSelection(4)
                128 -> spinner.setSelection(5)
                256 -> spinner.setSelection(6)
                512 -> spinner.setSelection(7)
                else -> spinner.setSelection(0)
            }
        } else
            spinner.setSelection(0, true)


        //set view
        val preview = dialogView.findViewById<ImageView>(R.id.compression_image)

        preview.setImageBitmap(bitmap)


        //set text
        val width_text = dialogView.findViewById<TextView>(R.id.compression_width_text)
        val height_text = dialogView.findViewById<TextView>(R.id.compression_height_text)
        width_text.text = bitmap!!.width.toString()
        height_text.text = bitmap.height.toString()
    }

    //on Result
    internal var iconMap: Bitmap? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == 0) {
                val fileLocation = data.getStringExtra("path")

                iconMap = BitmapFactory.decodeFile(fileLocation)
                if (CompressImage.testBitmap(512, 512, iconMap!!)) {
                    val builder = AlertDialog.Builder(this@ConversionActivity)
                    builder.setTitle(R.string.icon_edit_high_res_title)
                    builder.setMessage(R.string.icon_edit_high_res_subtitle)
                    builder.setPositiveButton(R.string.confirm) { dialogInterface, i ->
                        var scale = 1f
                        val scaleHeight = 512f / iconMap!!.height
                        val scaleWidth = 512f / iconMap!!.width
                        if (scaleHeight <= scaleWidth)
                            scale = scaleHeight
                        else
                            scale = scaleWidth

                        iconMap = CompressImage.getBitmap(iconMap!!, scale)

                        conversion.icon = iconMap
                        loadIcon()
                    }
                    builder.setNegativeButton(R.string.thanks) { dialogInterface, i ->
                        conversion.icon = iconMap
                        loadIcon()
                    }
                    builder.show()

                    hasIconBeenOverwritten = true

                } else {
                    conversion.icon = iconMap
                    loadIcon()
                }
            }
    }


    fun doOnFail() {
        //Delete not pack
        val text = findViewById<LinearLayout>(R.id.tutorial_welcome_error_layout)
        val notpack = File(conversion.path)
        mLog.i("PackConversion", "Deleting " + notpack.toString())
        class deleteTask : AsyncTask<Void, Int, Boolean>() {
            override fun doInBackground(vararg voids: Void): Boolean? {
                Snackbar.make(text, R.string.deleting, Snackbar.LENGTH_LONG).show()
                var r: Boolean? = false
                if (notpack.exists())
                    r = DeleteFolder.Delete(notpack.toString())
                else
                    r = true

                val callback : Snackbar.Callback = object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        finish()
                    }
                }

                if (r)
                    Snackbar.make(text, R.string.deleted_completed, Snackbar.LENGTH_SHORT)
                            .setCallback(callback)
                            .show()
                else
                    Snackbar.make(text, R.string.deteted_failed, Snackbar.LENGTH_SHORT)
                            .setCallback(callback)
                            .show()


                finishIntent.putExtra("Status_return", false)
                return r
            }
        }
        deleteTask().execute()
    }

    //Set BACK Icon
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finishIntent.putExtra("Status_return", false)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doDestFixing(old: String?): String {
        var dest = File(Environment.getExternalStorageDirectory().toString() + "/games/com.mojang/resource_packs/" + old)
        var plus = 0
        var str = old
        while (dest.exists()) {
            plus++
            str = old!! + plus
            dest = File(Environment.getExternalStorageDirectory().toString() + "/games/com.mojang/resource_packs/" + str)
        }
        return str!!
    }
}
