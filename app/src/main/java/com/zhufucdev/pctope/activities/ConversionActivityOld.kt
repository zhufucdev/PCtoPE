package com.zhufucdev.pctope.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import com.zhufucdev.pctope.utils.mLog
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.github.rubensousa.floatingtoolbar.FloatingToolbar
import com.github.rubensousa.floatingtoolbar.FloatingToolbarMenuBuilder
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

import com.zhufucdev.pctope.R
import com.zhufucdev.pctope.utils.*

import java.io.File
import java.io.FileNotFoundException

import com.zhufucdev.pctope.utils.TextureCompat.brokenPC


class ConversionActivityOld : BaseActivity() {
    internal val finishIntent = Intent()

    private fun MakeErrorDialog(errorString: String) {
        //make up a error dialog
        val error_dialog = AlertDialog.Builder(this@ConversionActivityOld)
        error_dialog.setTitle(R.string.error)
        error_dialog.setMessage(this@ConversionActivityOld.getString(R.string.error_dialog) + errorString)
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

    internal var skipUnzip: Boolean? = false
    internal var isPreFinished: Boolean? = false
    private var name: TextInputEditText? = null
    private var description: TextInputEditText? = null
    private var packname: String? = null
    private var packdescription: String? = null
    private var file: String? = null
    private var unzipping_tip: LinearLayout? = null
    private var cards: LinearLayout? = null
    private var error_layout: LinearLayout? = null

    private lateinit var conversion: TextureConversionUtils

    override fun onCreate(bundle: Bundle?) {
        //Preload
        super.onCreate(bundle)
        setContentView(R.layout.activity_conversion)
        setResult(Activity.RESULT_OK, finishIntent)//I don't need to use it
        initViews()
        getIntentInfo()

        //Main
        try {
            conversion = TextureConversionUtils(file!!, this)
        } catch (e: FileNotFoundException) {
            MakeErrorDialog(e.toString())
            return
        }

        conversion.skipUnzip = skipUnzip!!
        conversion.setOnUncompressListener(object : TextureConversionUtils.OnUncompressListener {
            override fun onPreUncompress() {
                unzipping_tip!!.visibility = View.VISIBLE
                cards!!.visibility = View.GONE
                error_layout!!.visibility = View.GONE
            }

            override fun inUncompressing() {

            }

            override fun onPostUncompress(result: Boolean, version: String?) {
                unzipping_tip!!.visibility = View.GONE
                cards!!.visibility = View.VISIBLE
                error_layout!!.visibility = View.GONE
                loadIcon()
                isPreFinished = true
                if (result)
                    doOnSuccess()
                else
                    doOnFail()
            }
        })

        conversion.setOnCrashListener(object : TextureConversionUtils.OnCrashListener {
            override fun onCrash(errorContent: String) {
                runOnUiThread {
                    unzipping_tip!!.visibility = View.GONE
                    cards!!.visibility = View.GONE
                    error_layout!!.visibility = View.VISIBLE
                    MakeErrorDialog(errorContent)
                }
            }

        })

        conversion.setConversionChangeListener(object :
            TextureConversionUtils.ConversionChangeListener {

            var alertDialog: ProgressDialog? = null

            override fun inDoingVersionDecisions() {
                runOnUiThread {
                    alertDialog = ProgressDialog(this@ConversionActivityOld)
                    alertDialog!!.setTitle(getString(R.string.loading))
                    alertDialog!!.setMessage(getString(R.string.please_wait))
                    alertDialog!!.setCancelable(false)
                    alertDialog!!.show()
                }
            }

            override fun inDoingImageCompressions(whatsBeingCompressing: String) {
                runOnUiThread {
                    alertDialog!!.setTitle(getString(R.string.progress_compressing_title))
                    alertDialog!!.setMessage(whatsBeingCompressing)
                }

            }

            override fun inDoingJSONWriting() {
                runOnUiThread {
                    alertDialog!!.setMessage(getString(R.string.do_final_step))
                    alertDialog!!.setTitle(getString(R.string.progress_writing_json))
                    mLog.i("Information On UI", "Showing json writing dialog...")
                }

            }

            override fun inDoingMcpackCompressing(path: String) {
                runOnUiThread {
                    alertDialog!!.setTitle(getString(R.string.compressing_mcpack))
                    alertDialog!!.setMessage(path)
                    mLog.i("Information On UI", "Showing compressing dialog...")
                }
            }

            override fun onDone() {
                finishIntent.putExtra("Status_return", true)
                runOnUiThread { alertDialog!!.setMessage(resources.getString(R.string.completed)) }
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                finish()
            }
        })

        //Overwrite dialog
        if (File(conversion.path).exists() && (!skipUnzip!!)) {
            val dialog = AlertDialog.Builder(this@ConversionActivityOld)
            dialog.setTitle(R.string.overwrite_title)
            dialog.setMessage(R.string.overwrite_content)
            dialog.setCancelable(false)
            dialog.setNegativeButton(R.string.skip) { dialog, which ->
                skipUnzip = true
                conversion.skipUnzip = skipUnzip!!
                conversion.uncompressPack()
            }
            dialog.setPositiveButton(R.string.overwrite) { dialog, which ->
                skipUnzip = false
                conversion.skipUnzip = skipUnzip!!
                conversion.uncompressPack()
            }
            dialog.show()
        } else
            conversion.uncompressPack()

    }

    private fun getIntentInfo() {
        val intent = intent
        skipUnzip = intent.getBooleanExtra("willSkipUnzipping", false)
        file = intent.getStringExtra("filePath")
    }

    private var format = 0
    private fun initViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val collapsingbar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_bar)
        unzipping_tip = findViewById(R.id.unzipping_tip)
        cards = findViewById(R.id.cards_grid)
        error_layout = findViewById(R.id.error_layout)
        name = findViewById(R.id.pname)
        description = findViewById(R.id.pdescription)


        //set back icon
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        //set project name on changed listener
        packname = getString(R.string.project_unnamed)
        packname = doDestFixing(packname)
        collapsingbar.title = packname

        name!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString() != "") {
                    packname = charSequence.toString()
                    packname = doDestFixing(packname)
                    collapsingbar.title = packname
                } else {
                    packname = resources.getString(R.string.project_unnamed)
                    packname = doDestFixing(packname)
                    collapsingbar.title = packname
                }

            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

        //for Floating Toolbar
        val ftb = findViewById<FloatingToolbar>(R.id.ftb_conversion)
        ftb.menu = FloatingToolbarMenuBuilder(this)
            .addItem(0, R.drawable.folder_white, R.string.convert_into_resources_folder)
            .addItem(1, R.drawable.zip_box, R.string.convert_into_mcpack)
            .build()

        //for FAB
        val button_finish = findViewById<FloatingActionButton>(R.id.finishBottom)
        val appBarLayout = findViewById<AppBarLayout>(R.id.appBar)

        ftb.attachAppBarLayout(appBarLayout)
        ftb.attachFab(button_finish)


        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, _ -> if (ftb.isShowing) ftb.hide() }
        )

        ftb.setClickListener(object : FloatingToolbar.ItemClickListener {
            override fun onItemClick(item: MenuItem?) {
                format = item!!.itemId
                when (item.itemId) {
                    0 -> doFinishButtonDoes(button_finish)
                    1 -> doFinishButtonDoes(button_finish)
                }
            }

            override fun onItemLongClick(item: MenuItem?) {

            }
        })
    }

    private fun doFinishButtonDoes(v: View) {
        if (isPreFinished!!) {

            packdescription = description!!.text.toString()

            name!!.isEnabled = false
            description!!.isEnabled = false

            conversion.compressFinalSize = compressFinalSize

            var mcpackDest = ""
            if (format == 1) {
                val dest =
                    File("${Environment.getExternalStorageDirectory().path}/games/com.mojang/mcpacks")
                if (!dest.isDirectory) {
                    dest.delete()
                }
                if (!dest.exists()) {
                    dest.mkdir()
                }
                mcpackDest = "${dest.path}/${packname}.mcpack"
            }

            Thread(Runnable {
                conversion.doConverting(
                    packname!!,
                    packdescription!!,
                    mcpackDest
                )
            }).start()
        } else
            Snackbar.make(v, R.string.unclickable_unzipping, Snackbar.LENGTH_LONG).show()
    }

    fun loadIcon() {
        val icon = findViewById<ImageView>(R.id.img_card_icon)
        val bitmap = conversion.icon
        if (bitmap != null) {
            icon.setImageBitmap(bitmap)
        } else {
            val finishBottom = findViewById<FloatingActionButton>(R.id.finishBottom)
            Snackbar.make(finishBottom, R.string.pack_icon_not_found, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok) {
                    val choose = Intent(ConversionActivity@ this, FileChooserActivity::class.java)
                    startActivityForResult(choose, 0)
                }
                .show()
        }

    }

    private var compressSize: Int = 0
    private var compressFinalSize: Int = 0
    private fun doOnSuccess() {
        //Set layouts
        //==>define
        val unzipping_tip = findViewById<LinearLayout>(R.id.unzipping_tip)
        val cards = findViewById<LinearLayout>(R.id.cards_grid)
        val error_layout = findViewById<LinearLayout>(R.id.error_layout)
        val PDI_card = findViewById<CardView>(R.id.pdi_card)
        //settings
        cards.visibility = View.VISIBLE
        unzipping_tip.visibility = View.GONE
        error_layout.visibility = View.GONE

        isPreFinished = true

        val animation = AnimationUtils.loadAnimation(this@ConversionActivityOld, R.anim.cards_show)
        cards.startAnimation(animation)
        //Load icon
        loadIcon()
        //Set icon editor
        val edit = findViewById<ImageView>(R.id.card_icon_edit)
        edit.setOnClickListener {
            val choose = Intent(this, FileChooserActivity::class.java)
            startActivityForResult(choose, 0)
        }
        //Set PDI CardView layout
        val PackType = findViewById<TextView>(R.id.info_pack_type)
        val PackInMC = findViewById<TextView>(R.id.info_pack_in_mc_ver)
        var type: String? = resources.getString(R.string.info_pack_type)
        when (conversion.verStr) {
            TextureCompat.fullPE -> type += resources.getString(R.string.type_fullPE)
            TextureCompat.fullPC -> type += resources.getString(R.string.type_fullPC)
            TextureCompat.brokenPE -> type += resources.getString(R.string.type_brokenPE)
            brokenPC -> type += resources.getString(R.string.type_brokenPC)
            else -> type = null
        }
        PackType.text = type

        var ver = conversion.decisions!!.getInMinecraftVer(PackInMC)
        if (ver == null)
            ver = resources.getString(R.string.info_file_not_exists)

        PackInMC.text = "${resources.getString(R.string.info_pack_in_mc_ver)}$ver"

        val supportOrNot = findViewById<ImageView>(R.id.support_or_not_icon)
        if (ver == resources.getString(R.string.type_before_1_9)) {
            supportOrNot.setImageResource(R.drawable.close_circle)
        }

        //Set Compression
        val baseFrom = if (conversion.verStr == (TextureCompat.fullPC) || conversion.verStr == brokenPC)
            conversion.path + "/assets/minecraft/textures"
        else
            conversion.path + "/textures"

        var image: String = FindFile.withKeywordOnce("grass_side.png", baseFrom)!!
        //grass >> sword >> never mind
        if (image.isEmpty()) {
            image = FindFile.withKeywordOnce("iron_sword.png", baseFrom)!!
            if (image.isEmpty())
                image = FindFile.withKeywordOnce(".png", baseFrom)!!
        }
        val imageLocation = image

        //set listener
        val compress = findViewById<CardView>(R.id.compression_card)


        compress.setOnClickListener {
            val dialog = BottomSheetDialog(this@ConversionActivityOld)


            val dialogView = layoutInflater.inflate(R.layout.compression_dialog, null)

            dialog.setContentView(dialogView)


            val optionsBitmap = BitmapFactory.Options()
            optionsBitmap.inSampleSize = 1

            val bitmap = BitmapFactory.decodeFile(imageLocation, optionsBitmap)
            val confirm = dialogView.findViewById<Button>(R.id.compression_button_confirm)

            loadDialogLayout(dialogView, bitmap)

            val spinner = dialogView.findViewById<Spinner>(R.id.compression_spinner)
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    adapterView: AdapterView<*>,
                    view: View,
                    i: Int,
                    l: Long
                ) {
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
                        loadDialogLayout(
                            dialogView,
                            CompressImage.getBitmap(bitmap, compressSize, compressSize)
                        )
                    } else
                        loadDialogLayout(dialogView, bitmap)

                    if (compressSize > bitmap.width || compressSize > bitmap.height) {
                        confirm.isEnabled = false
                        confirm.setTextColor(resources.getColor(R.color.grey_primary))
                        Toast.makeText(
                            this@ConversionActivityOld,
                            R.string.compression_alert,
                            Toast.LENGTH_SHORT
                        ).show()
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
        }
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
    private lateinit var iconMap: Bitmap

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == 0) {
                val fileLocation = data!!.getStringExtra("path")

                iconMap = BitmapFactory.decodeFile(fileLocation)
                if (CompressImage.testBitmap(512, 512, iconMap)) {
                    val builder = AlertDialog.Builder(this@ConversionActivityOld)
                    builder.setTitle(R.string.icon_edit_high_res_title)
                    builder.setMessage(R.string.icon_edit_high_res_subtitle)
                    builder.setPositiveButton(R.string.confirm) { dialogInterface, i ->
                        var scale = 1f
                        val scaleHeight = 512f / iconMap.height
                        val scaleWidth = 512f / iconMap.width
                        if (scaleHeight <= scaleWidth)
                            scale = scaleHeight
                        else
                            scale = scaleWidth

                        iconMap = CompressImage.getBitmap(iconMap, scale)

                        conversion.icon = iconMap
                        loadIcon()
                    }
                    builder.setNegativeButton(R.string.thanks) { dialogInterface, i ->
                        conversion.icon = iconMap
                        loadIcon()
                    }
                    builder.show()
                } else {
                    conversion.icon = iconMap
                    loadIcon()
                }
            }
    }


    fun doOnFail() {
        //Delete not pack
        //==>define
        val unzipping_tip = findViewById<LinearLayout>(R.id.unzipping_tip)
        val cards = findViewById<LinearLayout>(R.id.cards_grid)
        val error_layout = findViewById<LinearLayout>(R.id.error_layout)
        //settings
        cards.visibility = View.GONE
        unzipping_tip.visibility = View.GONE
        error_layout.visibility = View.VISIBLE

        val text = findViewById<TextView>(R.id.error_layout_text)
        text.text = text.text.toString() + this@ConversionActivityOld.getString(R.string.not_pack)
        val notpack = File(conversion.path)
        mLog.i("PackConversion", "Deleting $notpack")
        class DeleteTask : AsyncTask<Void, Int, Boolean>() {
            override fun doInBackground(vararg voids: Void): Boolean? {
                Snackbar.make(text, R.string.deleting, Snackbar.LENGTH_LONG).show()
                var r: Boolean? = false
                if (notpack.exists())
                    r = DeleteFolder.delete(notpack.toString())
                else
                    r = true


                if (r)
                    Snackbar.make(text, R.string.deleted_completed, Snackbar.LENGTH_LONG).show()
                else
                    Snackbar.make(text, R.string.deteted_failed, Snackbar.LENGTH_LONG).show()
                try {
                    Thread.sleep(1400)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                finishIntent.putExtra("Status_return", false)
                finish()
                return r
            }
        }
        DeleteTask().execute()
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
        var dest = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/games/com.mojang/resource_packs/" + old
        )
        var plus = 0
        var str = old
        while (dest.exists()) {
            plus++
            str = old!! + plus
            dest = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/games/com.mojang/resource_packs/" + str
            )
        }
        return str!!
    }
}