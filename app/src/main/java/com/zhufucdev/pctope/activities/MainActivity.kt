package com.zhufucdev.pctope.activities

import android.Manifest
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.*
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.animation.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.zhufucdev.pctope.adapters.FileChooserAdapter
import com.zhufucdev.pctope.adapters.TextureItems
import com.zhufucdev.pctope.collectors.ActivityCollector
import com.zhufucdev.pctope.interf.DeletingCallback
import com.zhufucdev.pctope.interf.SpacesItemDecoration
import com.zhufucdev.pctope.R
import com.zhufucdev.pctope.utils.*
import java.io.File
import java.util.*


class MainActivity : BaseActivity() {

    private fun makeErrorDialog(errorString: String) {
        //make up a error dialog
        val errorDialog = AlertDialog.Builder(this@MainActivity)
        errorDialog.setTitle(R.string.error)
        errorDialog.setMessage(this@MainActivity.getString(R.string.error_dialog) + errorString)
        errorDialog.setIcon(R.drawable.alert_octagram)
        errorDialog.setCancelable(false)
        errorDialog.setPositiveButton(R.string.close) { _, _ -> finish() }
        errorDialog.setNegativeButton(R.string.copy) { _, _ ->
            val copy = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            copy.setPrimaryClip(ClipData.newPlainText("Error", errorString))
            finish()
        }.show()
    }

    private var progressBar: ProgressBar? = null
    private fun showLoading() {
        progressBar!!.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar!!.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            val result = data!!.getBooleanExtra("Status_return", false)
            if (result) {
                loadList()
                updatePinnedShortcut()
                initShortcuts()
                if (chooserRoot.visibility == View.VISIBLE)
                    Handler().postDelayed({ Choose() }, 1000)
            }
        } else if (requestCode == 2) {
            if (data == null)
                return
            if (data.getBooleanExtra("isDataChanged", false)) {
                loadList()
                updatePinnedShortcut()
                initShortcuts()

            }

        }
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.menu)

        toolbarFore = findViewById(R.id.toolbar_fore)
        toolbarFore.title = getString(R.string.mulit_select)
        toolbarFore.visibility = View.GONE

        menuView = findViewById(R.id.menu_item_view)
        menuView.menu.clear()
        menuInflater.inflate(R.menu.toolbar_menu, menuView.menu)

    }

    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private lateinit var fab: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var chooserRoot: FrameLayout
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarFore: Toolbar
    private lateinit var menuView: ActionMenuView
    private var isFromShortcut: Boolean = false

    override fun onCreate(bundle: Bundle?) {
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressbar_in_main)

        fab = findViewById(R.id.fab)
        levelUp = findViewById(R.id.fab_level_up)
        levelUp!!.visibility = View.INVISIBLE
        levelUp!!.rotation = -90f

        recyclerView = findViewById(R.id.recycle_view)
        chooserRoot = findViewById(R.id.chooser_in_main)

        chooserRoot.visibility = View.INVISIBLE

        isFromShortcut = intent.getBooleanExtra("isFromShortcut", true)

        mLog.d("Permissions", isGranted.toString())

        initToolbar()

        //file choosing
        fab.setOnClickListener { Choose() }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState > 0) {
                    fab.hide()
                } else {
                    fab.show()
                }
            }
        })


        if (isGranted) {
            initActivity()
        } else {
            Snackbar.make(fab, R.string.permissions_request, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok) {
                    requirePermissions()
                }.show()
        }

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.menu)
        }
        navigationView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_main)

        navigationView!!.setNavigationItemSelectedListener { item ->
            drawerLayout!!.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.nav_settings -> {
                    val settings = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(settings)
                }
                R.id.nav_about -> {
                    val about = Intent(this@MainActivity, AboutActivity::class.java)
                    startActivity(about)
                }
                R.id.nav_log -> {
                    val log = Intent(this@MainActivity, ShowLogActivity::class.java)
                    startActivity(log)
                }
            }
            true
        }

        super.onCreate(bundle)

    }

    private fun requirePermissions() {
        ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            try {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        .addCategory("android.intent.category.DEFAULT")
                        .setData(Uri.parse("package:${applicationContext.packageName}")),
                    2
                )
            } catch (ignored: Exception) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),
                    2
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (isGranted)
                initActivity()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (!::items.isInitialized || !items.isInSelectMode)
                    drawerLayout!!.openDrawer(GravityCompat.START)
                else {
                    inSelectMode(inSelect = false, withLoadingList = false)

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initShortcuts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1)
            return
        val manager = getSystemService(ShortcutManager::class.java)
        val max = manager.maxShortcutCountPerActivity - 1
        val infos = ArrayList<ShortcutInfo>()
        var i = 0
        while (i < max && i < mTextures.size) {

            val temp = mTextures[i]
            if (!temp.ifIsResourcePack("PE")!!) {
                i--
                continue
            }

            val intent = Intent(ACTION_VIEW, null, this@MainActivity, DetailsActivity::class.java)
            intent.putExtra("texture_name", temp.name)
            intent.putExtra("texture_description", temp.description)
            intent.putExtra("texture_icon", temp.icon)
            intent.putExtra("texture_version", temp.getVersion())
            intent.putExtra("texture_path", temp.path)
            val info = ShortcutInfo.Builder(this, "pack $i")
                .setShortLabel(
                    if (temp.name.isNullOrEmpty()) {
                        getString(R.string.unable_to_get_name)
                    } else {
                        temp.name
                    }
                )
                .setIcon(Icon.createWithBitmap(BitmapFactory.decodeFile(temp.icon)))
                .setIntent(intent)
                .build()
            infos.add(info)

            i++
        }
        manager.dynamicShortcuts = infos
    }

    private fun updatePinnedShortcut() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1)
            return
        val manager = getSystemService(ShortcutManager::class.java)
        val pinned = manager.pinnedShortcuts
        for (i in 0 until pinned.size) {
            var isFound = false
            if (pinned[i].id == "add") continue
            for (j in 0 until mTextures.size) {
                if (pinned[i].intent?.getStringExtra("texture_path") == mTextures[j].path) {
                    isFound = true
                    break
                }
            }
            if (!isFound) {
                manager.disableShortcuts(listOf(pinned[i].id))
            }
        }
    }

    private fun initActivity() {
        //init for textures list
        recyclerView = findViewById(R.id.recycle_view)
        class FirstLoad : AsyncTask<Void, Int, Boolean>() {
            override fun onPreExecute() {
                showLoading()
            }

            override fun doInBackground(vararg voids: Void): Boolean? {
                loadList()
                loadFileChooser()
                return null
            }

            override fun onPostExecute(result: Boolean?) {
                hideLoading()
                if (isFromShortcut)
                    Choose()
                initShortcuts()
                updatePinnedShortcut()
            }
        }
        FirstLoad().execute()

        recyclerView.addItemDecoration(SpacesItemDecoration(16))
        recyclerView.setHasFixedSize(true)


        menuView.setOnMenuItemClickListener { item ->
            when (item!!.itemId) {
                R.id.delete -> {
                    val paths = ArrayList<Textures>()
                    for ((hasBeenDeleted, it) in items.selectedItems.withIndex()) {
                        val position = it - hasBeenDeleted
                        val path = items.getItem(position)
                        paths.add(path)
                        mTextures.remove(path)

                        items.notifyItemRemoved(position)
                    }

                    setLayoutManager()
                    initShortcuts()
                    updatePinnedShortcut()
                    Snackbar.make(fab, R.string.deleted_completed, Snackbar.LENGTH_LONG)
                        .addCallback(DeletingCallback(paths.toList()))
                        .setAction(R.string.undo) {
                            paths.forEach {
                                if (File(it.path).exists()) {

                                    mTextures.add(it.position, it)
                                    recyclerView.visibility = View.VISIBLE
                                    loadList()
                                    setLayoutManager()
                                    initShortcuts()
                                    updatePinnedShortcut()
                                } else {
                                    Snackbar.make(fab, R.string.failed, Snackbar.LENGTH_SHORT)
                                        .show()
                                }
                            }

                        }.show()
                    inSelectMode(inSelect = false, withLoadingList = false)
                }

                R.id.select_all -> {
                    if (!items.isSelectAllButtonActive)
                        items.selectAll()
                    else
                        items.deselectAll()
                }

                R.id.select_inverse -> {
                    items.selectInverse()
                }
            }
            true
        }

        //for swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh)
        swipeRefreshLayout!!.setColorSchemeColors(
            resources.getColor(R.color.colorAccent),
            resources.getColor(R.color.google_blue),
            resources.getColor(R.color.google_red),
            resources.getColor(R.color.google_green)
        )
        swipeRefreshLayout!!.setOnRefreshListener {
            Thread(Runnable {
                try {
                    Thread.sleep(500)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                runOnUiThread {
                    inSelectMode(false, true)
                    swipeRefreshLayout!!.isRefreshing = false
                }
            }).start()
        }
    }

    var lastTime: Long = 0
    var count = 0
    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else if (!items.isInSelectMode) {
            if (count == 0) {
                lastTime = System.currentTimeMillis()
                Snackbar.make(fab, R.string.double_back_exit, Snackbar.LENGTH_LONG)
                    .setCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            count = 0
                            super.onDismissed(transientBottomBar, event)
                        }
                    })
                    .show()
            } else {
                ActivityCollector.finishAll()
            }
            Log.d("DoubleBack", "Count = $count , Delay = ${System.currentTimeMillis() - lastTime}")
            count++
        } else {
            inSelectMode(false, false)
        }
    }

    var mTextures: ArrayList<Textures> = ArrayList()
    lateinit var items: TextureItems

    private fun loadList() {
        mTextures = ArrayList()

        val packsListDir = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/games/com.mojang/resource_packs/"
        )
        val packsList: Array<File>
        var make = true

        if (!packsListDir.exists()) make = packsListDir.mkdirs()

        if (make) {
            packsList = packsListDir.listFiles()
            items = TextureItems(mTextures)
            recyclerView.adapter = items

            for (aPacksList in packsList) {
                if (aPacksList.exists())
                    if (aPacksList.isDirectory) {
                        val texture = Textures(aPacksList)
                        if (texture.ifIsResourcePack("ALL")!!) {
                            mTextures.add(texture)
                        }
                    }
            }
            items.setOnItemClickListener(object : TextureItems.OnItemClickListener {
                override fun onLongPress(view: View, position: Int) {
                    inSelectMode(false)
                }

                override fun onItemClick(view: View, position: Int) {
                    if (!items.getIfIsAlertIconShown(view)) {
                        if (!items.isInSelectMode) {
                            val intent = Intent(this@MainActivity, DetailsActivity::class.java)

                            val temp = items.getItem(position)
                            intent.putExtra("texture_name", temp.name)
                            intent.putExtra("texture_description", temp.description)
                            intent.putExtra("texture_icon", temp.icon)
                            intent.putExtra("texture_version", temp.getVersion())
                            intent.putExtra("texture_path", temp.path)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                val options = ActivityOptions.makeSceneTransitionAnimation(
                                    this@MainActivity,
                                    view.findViewById(R.id.card_texture_icon),
                                    getString(R.string.pack_icon_transition)
                                )
                                ActivityCompat.startActivityForResult(
                                    this@MainActivity,
                                    intent,
                                    2,
                                    options.toBundle()
                                )
                            } else {
                                startActivityForResult(intent, 2)
                            }
                        }
                    }
                }
            })

            runOnUiThread {
                setLayoutManager()
                items.notifyDataSetChanged()
            }
        } else
            makeErrorDialog("Failed to make textures root directory.")
    }

    fun inSelectMode(withLoadingList: Boolean) {
        if (items.isInSelectMode) {
            val animation = AnimationUtils.loadAnimation(this, R.anim.cards_show)
            toolbarFore.visibility = View.VISIBLE
            toolbarFore.startAnimation(animation)
            setSupportActionBar(toolbarFore)
        } else {
            if (withLoadingList)
                loadList()
            items.deselectAll()
            val animation = AnimationUtils.loadAnimation(this, R.anim.cards_hide)
            toolbarFore.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    toolbarFore.visibility = View.GONE
                    setSupportActionBar(toolbar)

                }

                override fun onAnimationStart(animation: Animation?) {}

            })
        }
    }

    fun inSelectMode(inSelect: Boolean, withLoadingList: Boolean) {
        items.isInSelectMode = inSelect
        inSelectMode(withLoadingList)
    }

    private var adapter: FileChooserAdapter? = null
    private var levelUp: FloatingActionButton? = null
    private fun Choose() {
        if (chooserRoot.visibility == View.INVISIBLE) {
            fab.isEnabled = false
            levelUp!!.show()
            toolbar.setTitle(R.string.choosing_alert)
            toolbar.subtitle = adapter!!.getPath()

            chooserRoot.visibility = View.VISIBLE
            if (!isFromShortcut) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val animator = ViewAnimationUtils.createCircularReveal(
                        chooserRoot,
                        fab.x.toInt() + fab.width / 2,
                        fab.y.toInt() - fab.height / 2,
                        0f,
                        Math.hypot(chooserRoot.width.toDouble(), chooserRoot.height.toDouble())
                            .toFloat()
                    )
                    animator.duration = 300
                    animator.start()
                }
            }
            val first = RotateAnimation(
                0.0f,
                45.0f * 4 + 15,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            first.duration = 200
            fab.startAnimation(first)
            first.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    fab.rotation = 60.0f
                    val second = RotateAnimation(
                        15.0f,
                        0.0f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                    )
                    second.duration = 100
                    fab.startAnimation(second)
                    second.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {

                        }

                        override fun onAnimationEnd(animation: Animation) {
                            fab.rotation = 45.0f
                            fab.isEnabled = true
                        }

                        override fun onAnimationRepeat(animation: Animation) {

                        }
                    })
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        } else {
            fab.isEnabled = false
            levelUp!!.hide()
            toolbar.setTitle(getString(R.string.app_name))
            toolbar.subtitle = ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val animator = ViewAnimationUtils.createCircularReveal(
                    chooserRoot,
                    fab.x.toInt() + fab.width / 2,
                    fab.y.toInt() - fab.height / 2,
                    Math.hypot(chooserRoot.width.toDouble(), chooserRoot.height.toDouble())
                        .toFloat(),
                    0f
                )
                animator.duration = 400
                animator.start()
            } else chooserRoot.visibility = View.INVISIBLE

            fab.rotation = 45.0f
            val first = RotateAnimation(
                0.0f,
                -45.0f * 4 + 15,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            first.duration = 200
            first.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    fab.rotation = -15.0f
                    val second = RotateAnimation(
                        0.0f,
                        15.0f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                    )
                    second.duration = 100
                    fab.startAnimation(second)
                    second.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {

                        }

                        override fun onAnimationEnd(animation: Animation) {
                            fab.rotation = 0.0f
                            fab.isEnabled = true
                        }

                        override fun onAnimationRepeat(animation: Animation) {

                        }
                    })
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            fab.startAnimation(first)

            Handler().postDelayed({ chooserRoot.visibility = View.INVISIBLE }, 390)
        }
        isFromShortcut = false
    }

    var chooser: RecyclerView? = null
    private fun loadFileChooser() {
        chooser = findViewById(R.id.file_chooser_view)
        chooser!!.layoutManager = StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL)

        adapter =
            FileChooserAdapter(Environment.getExternalStorageDirectory().path, mutableListOf("zip"))

        adapter?.setOnItemClickListener(object : FileChooserAdapter.OnItemClickListener {
            override fun onClick(view: View, data: Intent) {
                val path = data.getStringExtra("path")
                if (File(path).isFile) {
                    val pref = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    val intent = if (pref.getString(
                            "pref_conversion_style",
                            "new"
                        ) == "new"
                    ) Intent(this@MainActivity, ConversionActivity::class.java)
                    else Intent(this@MainActivity, ConversionActivityOld::class.java)
                    intent.putExtra("filePath", path)
                    startActivityForResult(intent, 0)
                } else {
                    toolbar.subtitle = path
                }
            }

        })
        levelUp!!.setOnClickListener {
            if (adapter!!.upLevel(Environment.getExternalStorageDirectory().path))
                toolbar.subtitle = adapter!!.getPath()
            else
                Snackbar.make(fab as View, R.string.non_upper_level, Snackbar.LENGTH_SHORT).show()
        }

        runOnUiThread { chooser!!.adapter = adapter }
    }

    fun setLayoutManager() {
        if (items.itemCount == 0) {
            recyclerView.visibility = View.GONE
            (findViewById<LinearLayout>(R.id.android_nothing)).visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            (findViewById<LinearLayout>(R.id.android_nothing)).visibility = View.GONE
        }

        var layoutManager = LinearLayoutManager(this@MainActivity)
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        val itemCount = items.itemCount
        if (itemCount != 0) {
            if (width >= height) {
                var lineCount = Math.round(width / 512f)

                if (lineCount >= itemCount && itemCount != 0)
                    lineCount = items.itemCount
                mLog.i(
                    "Layout Manager",
                    "Layout manager set by Grid Layout Manager. Line count is $lineCount"
                )
                layoutManager = GridLayoutManager(this, lineCount)
            }
        }

        recyclerView.layoutManager = layoutManager
    }
}