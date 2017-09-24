package com.zhufuc.pctope.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import com.zhufuc.pctope.Adapters.Textures
import com.zhufuc.pctope.R
import com.zhufuc.pctope.Utils.*

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigDecimal
import java.nio.channels.FileChannel

class DetailsActivity : BaseActivity() {

    private var name: String? = null
    private var description: String? = null
    private var version: String? = null
    private var icon: String? = null
    private var path: String? = null
    private var textures: Textures? = null
    private var textureEditor: Textures.Edit? = null
    private var size: BigDecimal? = null

    private var cards: NestedScrollView? = null
    private var fab: FloatingActionButton? = null

    private val fullPE = "Found:full PE pack."
    private var compressSize = 0
    private var compressFinalSize = 0

    private var isDataChanged = false

    /*
        Some Utils
     */
    fun getFolderTotalSize(path: String): Long {
        val files = File(path).listFiles()
        var size: Long = 0
        for (f in files)
            if (f.exists()) {
                if (f.isFile) {
                    var fc: FileChannel? = null
                    var inputStream: FileInputStream? = null
                    try {
                        inputStream = FileInputStream(f)
                        fc = inputStream.channel
                        size += fc!!.size()
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                } else
                    size += getFolderTotalSize(f.path)
            }

        return size
    }

    private fun MakeErrorDialog(errorString: String) {
        //make up a error dialog
        val error_dialog = AlertDialog.Builder(this@DetailsActivity)
        error_dialog.setTitle(R.string.error)
        error_dialog.setMessage(this@DetailsActivity.getString(R.string.error_dialog) + errorString)
        error_dialog.setIcon(R.drawable.alert_octagram)
        error_dialog.setCancelable(false)
        error_dialog.setPositiveButton(R.string.close, null)
        error_dialog.setNegativeButton(R.string.copy) { dialogInterface, i ->
            val copy = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            copy.primaryClip = ClipData.newPlainText("PathCopied",path)
        }.show()
    }

    internal var FabListener: View.OnClickListener = View.OnClickListener {
        val dialogView = LayoutInflater.from(this@DetailsActivity).inflate(R.layout.details_texture_basic_info_editor, null)
        val dialog = AlertDialog.Builder(this@DetailsActivity)

        dialog.setTitle(R.string.project_icon_edit)
        dialog.setView(dialogView)

        val editName = dialogView.findViewById<View>(R.id.details_edit_name) as EditText
        val editDescription = dialogView.findViewById<View>(R.id.details_edit_description) as EditText

        editName.setText(name)
        editDescription.setText(description)

        dialog.setPositiveButton(R.string.confirm) { dialogInterface, i ->
            val setName = editName.text.toString()
            val setDescription = editDescription.text.toString()
            if (setName != name || setDescription != description) {
                mLog.i("Edit","Change name and description of $name to $setName and $setDescription")
                textureEditor!!.changeNameAndDescription(setName, setDescription)
                isDataChanged = true
                loadDetailedInfo()
            }
        }

        dialog.setNeutralButton(R.string.icon_edit) { dialogInterface, i ->
            val intent = Intent(DetailsActivity@this,FileChooserActivity::class.java)
            startActivityForResult(intent, 0)
        }

        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        progress = findViewById(R.id.details_loading_progress) as ProgressBar
        fab = findViewById(R.id.details_fab) as FloatingActionButton
        fab!!.setOnClickListener(FabListener)

        val intent = intent
        icon = intent.getStringExtra("texture_icon")
        version = intent.getStringExtra("texture_version")
        name = intent.getStringExtra("texture_name")
        description = intent.getStringExtra("texture_description")
        path = intent.getStringExtra("texture_path")

        initBasicTitles()

        initToolbar()

        loadDetailedInfo()

    }

    fun updateInformation() {
        if (textures != null)
            textures = null
        if (textureEditor != null)
            textureEditor = null
        textures = Textures(File(path!!))
        textureEditor = Textures.Edit(path!!)
        //on Crash
        textureEditor!!.setOnCrashListener (object : Textures.Edit.OnCrashListener{
            override fun onCrash(e: String) {
                MakeErrorDialog(e)
            }

        })

        version = textures!!.getVersion()
        name = textures!!.name
        description = textures!!.description

        val totalSize = BigDecimal(getFolderTotalSize(path!!))
        val BtoMB = BigDecimal(1024 * 1024)
        size = totalSize.divide(BtoMB, 5, BigDecimal.ROUND_HALF_UP)
    }

    fun loadDetailedInfo() {

        cards = findViewById(R.id.details_info_layout) as NestedScrollView

        class LoadingTask : AsyncTask<Void, Int, Boolean>() {
            public override fun onPreExecute() {
                showLoading()
                fab!!.isEnabled = false
                cards!!.visibility = View.INVISIBLE
            }

            public override fun doInBackground(vararg params: Void): Boolean? {
                updateInformation()
                if (description == null) {
                    description = ""
                }

                try {
                    Thread.sleep(600)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                return true

            }

            public override fun onPostExecute(result: Boolean?) {
                initBasicTitles()
                loadViews()
                hideLoading()
                initOperationalCards()
            }
        }
        LoadingTask().execute()
    }

    fun loadViews() {
        //-----FloatingActionButton-----
        fab!!.isEnabled = true

        //-----CARD------
        val size = findViewById(R.id.details_texture_size) as TextView
        size.text = (getString(R.string.details_card_basic_info_size) + ": " + this.size + "MB").toString()

        val location = findViewById(R.id.details_texture_location) as TextView
        location.text = (getString(R.string.details_card_basic_info_location) + ": " + path).toString()
        location.setOnClickListener { view ->
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText("Path", path)
            clipboardManager.primaryClip = data
            Snackbar.make(view, R.string.copied, Snackbar.LENGTH_SHORT).show()
        }

        val anim = ViewAnimationUtils.createCircularReveal(cards, cards!!.width / 2, 0, 0f, Math.hypot(cards!!.width.toDouble(), cards!!.height.toDouble()).toFloat())
        cards!!.visibility = View.VISIBLE
        anim.duration = 500
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()
    }

    fun initBasicTitles() {

        val iconView = findViewById(R.id.details_icon) as ImageView
        if (icon != null)
            iconView.setImageBitmap(BitmapFactory.decodeFile(icon))
        else
            iconView.setImageResource(R.drawable.bug_pack_icon)

        val packdescription = findViewById(R.id.details_description) as TextView
        val toolbarLayout = findViewById(R.id.details_toolbar_layout) as CollapsingToolbarLayout


        toolbarLayout.expandedTitleMarginStart = 235
        if (name != null)
            toolbarLayout.title = name
        else
            toolbarLayout.title = getString(R.string.unable_to_get_name)

        description = if (description == null) "" else description
        if (description != "") {
            packdescription.visibility = View.VISIBLE
            packdescription.text = description
            toolbarLayout.expandedTitleMarginBottom = 140
        } else {
            packdescription.visibility = View.INVISIBLE
            toolbarLayout.expandedTitleMarginBottom = 100
        }
    }

    fun initToolbar() {
        val toolbar = findViewById(R.id.details_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun initOperationalCards() {
        val VerStr = textures!!.getVersion()

        //Set Compression
        var image: File? = null
        var baseFrom: String? = null
        if (VerStr.equals(TextureCompat.fullPC) || VerStr.equals(TextureCompat.brokenPC))
            baseFrom = path!! + "/assets/minecraft/textures"
        else
            baseFrom = path!! + "/textures"
        //grass >> sword >> never mind
        image = FindFile.withKeywordOnce("grass_side.png", baseFrom)
        if (image == null) {
            image = FindFile.withKeywordOnce("iron_sword.png", baseFrom)
            if (image == null)
                image = FindFile.withKeywordOnce(".png", baseFrom)
        }
        val imageLocation = image!!.path


        //set listener
        val compress = findViewById(R.id.compression_card) as CardView


        compress.setOnClickListener(View.OnClickListener {
            val dialog = BottomSheetDialog(this@DetailsActivity)


            val dialogView = layoutInflater.inflate(R.layout.compression_dialog, null)

            dialog.setContentView(dialogView)

            val bitmap = BitmapFactory.decodeFile(imageLocation)
            val confirm = dialogView.findViewById<View>(R.id.compression_button_confirm) as Button

            loadDialogLayout(dialogView, bitmap)

            val spinner = dialogView.findViewById<View>(R.id.compression_spinner) as Spinner
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
                        Toast.makeText(this@DetailsActivity, R.string.compression_alert, Toast.LENGTH_SHORT).show()
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


            confirm.setOnClickListener(View.OnClickListener {
                compressFinalSize = compressSize
                dialog.dismiss()
                if (compressFinalSize == 0) {
                    return@OnClickListener
                }

                val progressDialog = ProgressDialog(this@DetailsActivity)
                progressDialog.setTitle(R.string.progress_compressing_title)
                progressDialog.setMessage(getString(R.string.please_wait))
                progressDialog.show()


                textureEditor!!.setOnCompressionProgressChangeListener(object: Textures.Edit.CompressionProgressChangeListener{
                    override fun OnProgressChangeListener(whatsBeingCompressed: String?, isDone: Boolean) {
                        if (!isDone) {
                            runOnUiThread {
                                progressDialog.setTitle(R.string.progress_compressing_title)
                                progressDialog.setMessage(whatsBeingCompressed)
                            }
                        } else {
                            runOnUiThread {
                                progressDialog.dismiss()
                                loadDetailedInfo()
                            }

                        }
                    }

                })



                Thread(Runnable { textureEditor!!.compressImages(compressFinalSize) }).start()
            })
        })
    }

    private fun loadDialogLayout(dialogView: View, bitmap: Bitmap?) {

        val spinner = dialogView.findViewById<View>(R.id.compression_spinner) as Spinner
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
        val preview = dialogView.findViewById<View>(R.id.compression_image) as ImageView

        preview.setImageBitmap(bitmap)


        //set text
        val width_text = dialogView.findViewById<View>(R.id.compression_width_text) as TextView
        val height_text = dialogView.findViewById<View>(R.id.compression_height_text) as TextView
        width_text.text = bitmap!!.width.toString()
        height_text.text = bitmap.height.toString()
    }

    private var progress: ProgressBar? = null
    fun showLoading() {
        progress!!.visibility = View.VISIBLE
    }

    fun hideLoading() {
        progress!!.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> onBackPressed()
            else -> {
            }
        }
        return true
    }

    override fun onBackPressed() {
        fab!!.visibility = View.INVISIBLE

        val intent = Intent()
        intent.putExtra("isDataChanged", isDataChanged)
        setResult(Activity.RESULT_OK, intent)

        super.onBackPressed()
    }

    //Activity Result
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == 0) {
                val iconMap = BitmapFactory.decodeFile(data.getStringExtra("path"))
                if (CompressImage.testBitmap(512, 512, iconMap)) {
                    val builder = AlertDialog.Builder(this@DetailsActivity)
                    builder.setTitle(R.string.icon_edit_high_res_title)
                    builder.setMessage(R.string.icon_edit_high_res_subtitle)
                    builder.setPositiveButton(R.string.confirm) { dialogInterface, i ->
                        try {
                            var scale = 1f
                            val scaleHeight = 512f / iconMap.height
                            val scaleWidth = 512f / iconMap.width
                            if (scaleHeight <= scaleWidth)
                                scale = scaleHeight
                            else
                                scale = scaleWidth
                            textureEditor!!.iconEdit(CompressImage.getBitmap(iconMap, scale))
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        loadDetailedInfo()
                    }
                    builder.setNegativeButton(R.string.thanks) { dialogInterface, i ->
                        try {
                            textureEditor!!.iconEdit(iconMap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        loadDetailedInfo()
                    }
                    builder.show()
                } else {
                    try {
                        textureEditor!!.iconEdit(iconMap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    loadDetailedInfo()
                }

                isDataChanged = true
            }
    }
}
