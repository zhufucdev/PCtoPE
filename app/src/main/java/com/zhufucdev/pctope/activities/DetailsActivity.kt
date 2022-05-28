package com.zhufucdev.pctope.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.widget.Toolbar
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.cardview.widget.CardView
import com.bm.library.PhotoView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

import com.zhufucdev.pctope.utils.Textures
import com.zhufucdev.pctope.R
import com.zhufucdev.pctope.utils.*

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigDecimal
import java.nio.channels.FileChannel
import kotlin.math.roundToInt

class DetailsActivity : BaseActivity(), ViewTreeObserver.OnPreDrawListener {

    private var name: String? = null
    private var description: String? = null
    private lateinit var version: String
    private var icon: String? = null
    private lateinit var path: String
    private lateinit var textures: Textures
    private lateinit var textureEditor: Textures.Edit
    private lateinit var size: BigDecimal

    private lateinit var cards: NestedScrollView
    private lateinit var fab: FloatingActionButton

    private var compressSize = 0
    private var compressFinalSize = 0

    private var isDataChanged = false

    /*
        Some Utils
     */
    fun getFolderTotalSize(path: String): Long {
        val files = File(path).listFiles()
        var size: Long = 0
        for (f in files!!)
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
            copy.setPrimaryClip(ClipData.newPlainText("PathCopied", path))
        }.show()
    }

    private var fabListener: View.OnClickListener = View.OnClickListener {
        val dialogView = LayoutInflater.from(this@DetailsActivity)
            .inflate(R.layout.details_texture_basic_info_editor, null)
        val dialog = AlertDialog.Builder(this@DetailsActivity)

        dialog.setTitle(R.string.project_icon_edit)
        dialog.setView(dialogView)

        val editName = dialogView.findViewById<EditText>(R.id.details_edit_name)
        val editDescription = dialogView.findViewById<EditText>(R.id.details_edit_description)

        editName.setText(name)
        editDescription.setText(description)

        dialog.setPositiveButton(R.string.confirm) { dialogInterface, i ->
            val setName = editName.text.toString()
            val setDescription = editDescription.text.toString()
            if (setName != name || setDescription != description) {
                mLog.i(
                    "Edit",
                    "Change name and description of $name to $setName and $setDescription"
                )
                textureEditor.changeNameAndDescription(setName, setDescription)
                isDataChanged = true
                loadDetailedInfo()
            }
        }

        dialog.setNeutralButton(R.string.icon_edit) { _, _ ->
            val intent = Intent(this, FileChooserActivity::class.java)
            startActivityForResult(intent, 0)
        }

        dialog.show()
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_details)

        progress = findViewById(R.id.details_loading_progress)
        fab = findViewById(R.id.details_fab)
        fab.setOnClickListener(fabListener)

        val intent = intent
        icon = intent.getStringExtra("texture_icon")!!
        version = intent.getStringExtra("texture_version")!!
        name = intent.getStringExtra("texture_name")!!
        description = intent.getStringExtra("texture_description")!!
        path = intent.getStringExtra("texture_path")!!

        initBasicTitles()

        initToolbar()

        loadDetailedInfo()

    }

    fun updateInformation() {
        textures = Textures(File(path))
        textureEditor = Textures.Edit(path)
        //on Crash
        textureEditor.setOnCrashListener(object : Textures.Edit.OnCrashListener {
            override fun onCrash(e: String) {
                MakeErrorDialog(e)
            }

        })

        version = textures.getVersion()
        name = textures.name
        description = textures.description

        val totalSize = BigDecimal(getFolderTotalSize(path))
        val BtoMB = BigDecimal(1024 * 1024)
        size = totalSize.divide(BtoMB, 5, BigDecimal.ROUND_HALF_UP)
    }

    fun loadDetailedInfo() {

        cards = findViewById(R.id.details_info_layout)

        class LoadingTask : AsyncTask<Void, Int, Boolean>() {
            public override fun onPreExecute() {
                showLoading()
                fab.isEnabled = false
                cards.visibility = View.INVISIBLE
            }

            public override fun doInBackground(vararg params: Void): Boolean {
                updateInformation()
                if (description == null) {
                    description = ""
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
        fab.isEnabled = true

        //-----CARD------
        val size = findViewById<TextView>(R.id.details_texture_size)
        size.text = "${getString(R.string.details_card_basic_info_size)} : ${this.size} MB"

        val location = findViewById<TextView>(R.id.details_texture_location)
        location.text =
            (getString(R.string.details_card_basic_info_location) + ": " + path).toString()
        location.setOnClickListener { view ->
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText("Path", path)
            clipboardManager.setPrimaryClip(data)
            Snackbar.make(view, R.string.copied, Snackbar.LENGTH_SHORT).show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val anim = ViewAnimationUtils.createCircularReveal(
                cards, cards.width / 2, 0, 0f, Math.hypot(
                    cards.width.toDouble(), cards.height.toDouble()
                ).toFloat()
            )
            cards.visibility = View.VISIBLE
            anim.duration = 500
            anim.interpolator = AccelerateDecelerateInterpolator()
            anim.start()
        } else {
            cards.visibility = View.VISIBLE
        }
    }

    lateinit var iconView: ImageView
    lateinit var iconFullScreenView: PhotoView
    lateinit var iconLayout: FrameLayout
    lateinit var toolbarLayout: CollapsingToolbarLayout
    var isFullScreenShown = false

    fun setFullScreen(shown: Boolean) {
        isFullScreenShown = shown
        val mInfo = PhotoView.getImageViewInfo(iconView)
        if (shown) {
            iconView.visibility = View.INVISIBLE
            iconLayout.visibility = View.VISIBLE
            iconLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cards_show))
            iconFullScreenView.animaFrom(mInfo)

            fab.visibility = View.INVISIBLE
        } else {
            iconFullScreenView.animaTo(mInfo) {
                iconLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cards_hide))
                iconLayout.visibility = View.GONE
                iconView.visibility = View.VISIBLE
                fab.visibility = View.VISIBLE
            }
        }
    }

    fun initBasicTitles() {

        iconView = findViewById(R.id.details_icon)
        iconFullScreenView = findViewById(R.id.photo_view)
        iconLayout = findViewById(R.id.full_screen_image_view_layout)

        val packdescription = findViewById<TextView>(R.id.details_description)
        toolbarLayout = findViewById(R.id.details_toolbar_layout)

        val vto = iconView.viewTreeObserver
        vto.addOnPreDrawListener(this)

        if (icon != null) {
            val icon = BitmapFactory.decodeFile(icon)
            iconView.setImageBitmap(icon)
            iconFullScreenView.setImageBitmap(icon)
        } else
            iconView.setImageResource(R.drawable.bug_pack_icon)

        iconFullScreenView.enable()
        iconFullScreenView.maxScale = 8f
        iconView.setOnClickListener {
            setFullScreen(true)
        }
        iconFullScreenView.setOnClickListener {
            setFullScreen(false)
        }

        if (!name.isNullOrEmpty())
            toolbarLayout.title = name
        else
            toolbarLayout.title = getString(R.string.unable_to_get_name)

        val smallestWith = resources.configuration.smallestScreenWidthDp
        mLog.d("Smallest Screen Width", "$smallestWith")
        if (!description.isNullOrEmpty()) {
            packdescription.visibility = View.VISIBLE
            packdescription.text = description
            toolbarLayout.expandedTitleMarginBottom = (3 / 7.0 * smallestWith).roundToInt()
        } else {
            packdescription.visibility = View.INVISIBLE
            toolbarLayout.expandedTitleMarginBottom = (25 / 79.0 * smallestWith).roundToInt()
        }
    }

    override fun onPreDraw(): Boolean {
        toolbarLayout.expandedTitleMarginStart = (iconView.measuredWidth * 1.3).roundToInt()
        mLog.d("Titles", "Margin set.")
        iconView.viewTreeObserver.removeOnPreDrawListener(this)
        return true
    }

    fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.details_toolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun initOperationalCards() {
        val verStr = textures.getVersion()

        //Set Compression

        val baseFrom = if (verStr == (TextureCompat.fullPC) || verStr == (TextureCompat.brokenPC))
            "$path/assets/minecraft/textures"
        else
            "$path/textures"

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


        compress.setOnClickListener(View.OnClickListener {
            val dialog = BottomSheetDialog(this@DetailsActivity)


            val dialogView = layoutInflater.inflate(R.layout.compression_dialog, null)

            dialog.setContentView(dialogView)

            val bitmap = BitmapFactory.decodeFile(imageLocation)
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

                    compressSize = when (options[i]) {
                        "8x" -> 8
                        "16x" -> 16
                        "32x" -> 32
                        "64x" -> 64
                        "128x" -> 128
                        "256x" -> 256
                        "512x" -> 512
                        else -> 0
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
                            this@DetailsActivity,
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


                textureEditor.setOnCompressionProgressChangeListener(object :
                    Textures.Edit.CompressionProgressChangeListener {
                    override fun progressChangeListener(
                        whatsBeingCompressed: String?,
                        isDone: Boolean
                    ) {
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



                Thread(Runnable { textureEditor.compressImages(compressFinalSize) }).start()
            })
        })

        //For mcpack compress card
        val mcpackCard = findViewById<CardView>(R.id.card_mcpack_compress)
        val mcpackSubtitle = findViewById<TextView>(R.id.card_mcpack_compress_subtitle)
        val mcpackChe = findViewById<ImageView>(R.id.card_mcpack_compress_chevron)
        val mcpackPath =
            File("${Environment.getExternalStorageDirectory()}/games/com.mojang/mcpacks/${name}.mcpack")
        val isMcpackExisted = mcpackPath.exists() && mcpackPath.isFile


        if (!isMcpackExisted) {
            mcpackSubtitle.text = "${getString(R.string.mcpack_compress_subtitle)} $mcpackPath"
            mcpackChe.setImageResource(R.drawable.chevron_right_overlay)
            mcpackChe.setOnClickListener(null)
            mcpackChe.background = null
        } else {
            mcpackSubtitle.text = "${getString(R.string.mcpack_exists)} $mcpackPath"
            mcpackChe.setImageResource(R.drawable.ic_delete_black_24dp)
            mcpackChe.setOnClickListener {
                if (isMcpackExisted) {
                    val alertDialog = AlertDialog.Builder(this@DetailsActivity)
                        .setMessage(R.string.mcpack_delete)
                        .setPositiveButton(R.string.confirm) { _, _ ->
                            mcpackPath.delete()
                            initOperationalCards()
                        }
                        .setNegativeButton(R.string.no, null)
                        .create()
                    alertDialog.show()
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mcpackChe.background = getDrawable(R.drawable.nomaskripple)
            }
        }

        mcpackCard.setOnClickListener {
            if (!isMcpackExisted) {
                class compressingTask : AsyncTask<Void, Void, Int>() {
                    lateinit var progressDialog: ProgressDialog
                    override fun onPreExecute() {
                        progressDialog = ProgressDialog(this@DetailsActivity)
                        progressDialog.setTitle(R.string.compressing_mcpack)
                        progressDialog.setMessage(mcpackPath.path)
                        progressDialog.show()
                        super.onPreExecute()
                    }

                    override fun doInBackground(vararg p0: Void?): Int {
                        textureEditor!!.setMcpack(mcpackPath.path)
                        return 0
                    }

                    override fun onPostExecute(result: Int?) {
                        progressDialog.hide()
                        initOperationalCards()
                        super.onPostExecute(result)
                    }
                }
                compressingTask().execute()
            } else {
                val intent = Intent(Intent.ACTION_SEND)
                val uri = Uri.parse(mcpackPath.path)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.type = "*/*"
                startActivity(Intent.createChooser(intent, getString(R.string.share)))
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
        }
        return true
    }

    override fun onBackPressed() {
        if (!isFullScreenShown) {
            fab.visibility = View.INVISIBLE

            val intent = Intent()
            intent.putExtra("isDataChanged", isDataChanged)
            setResult(Activity.RESULT_OK, intent)

            super.onBackPressed()
        } else {
            setFullScreen(false)
        }
    }

    //Activity Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == 0) {
                val iconMap = BitmapFactory.decodeFile(data!!.getStringExtra("path"))
                if (CompressImage.testBitmap(512, 512, iconMap)) {
                    val builder = AlertDialog.Builder(this@DetailsActivity)
                    builder.setTitle(R.string.icon_edit_high_res_title)
                    builder.setMessage(R.string.icon_edit_high_res_subtitle)
                    builder.setPositiveButton(R.string.confirm) { dialogInterface, i ->
                        try {
                            var scale = 1f
                            val scaleHeight = 512f / iconMap.height
                            val scaleWidth = 512f / iconMap.width
                            scale = if (scaleHeight <= scaleWidth)
                                scaleHeight
                            else
                                scaleWidth
                            textureEditor.iconEdit(CompressImage.getBitmap(iconMap, scale))
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        loadDetailedInfo()
                    }
                    builder.setNegativeButton(R.string.thanks) { _, _ ->
                        try {
                            textureEditor.iconEdit(iconMap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        loadDetailedInfo()
                    }
                    builder.show()
                } else {
                    try {
                        textureEditor.iconEdit(iconMap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    loadDetailedInfo()
                }

                isDataChanged = true
            }
    }
}