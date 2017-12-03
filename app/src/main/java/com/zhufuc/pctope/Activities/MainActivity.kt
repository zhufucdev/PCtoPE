package com.zhufuc.pctope.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.*
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.percent.PercentRelativeLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.ActionMenuView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.transition.TransitionInflater
import android.util.Log
import android.view.*
import android.view.animation.*
import android.widget.*
import com.tencent.bugly.crashreport.CrashReport

import com.zhufuc.pctope.Adapters.FileChooserAdapter
import com.zhufuc.pctope.Adapters.TextureItems
import com.zhufuc.pctope.Utils.Textures
import com.zhufuc.pctope.Collectors.ActivityCollector
import com.zhufuc.pctope.Interf.DeletingCallback
import com.zhufuc.pctope.Interf.SpacesItemDecoration
import com.zhufuc.pctope.Utils.*
import com.zhufuc.pctope.R

import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : BaseActivity() {

    private fun MakeErrorDialog(errorString: String) {
        //make up a error dialog
        val error_dialog = AlertDialog.Builder(this@MainActivity)
        error_dialog.setTitle(R.string.error)
        error_dialog.setMessage(this@MainActivity.getString(R.string.error_dialog) + errorString)
        error_dialog.setIcon(R.drawable.alert_octagram)
        error_dialog.setCancelable(false)
        error_dialog.setPositiveButton(R.string.close) { dialogInterface, i -> finish() }
        error_dialog.setNegativeButton(R.string.copy) { dialogInterface, i ->
            val copy = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            copy.text = errorString
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
        if (requestCode == 0) {
            val result = data!!.getBooleanExtra("Status_return", false)
            if (result) {
                loadList()
                updatePinnedShortcut()
                initShortcuts()
                if (chooser_root.visibility == View.VISIBLE)
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

    fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.menu)

        toobarFore = findViewById(R.id.toolbar_fore)
        toobarFore.title = getString(R.string.mulit_select)

        menuView = findViewById(R.id.menu_item_view)
        menuView.menu.clear()
        menuInflater.inflate(R.menu.toolbar_menu,menuView.menu)

        foreLayout = findViewById(R.id.fore_toolbar_layout)
        foreLayout.visibility = View.GONE
    }

    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.READ_EXTERNAL_STORAGE)

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private lateinit var fab: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var android_nothing_card: LinearLayout
    private lateinit var chooser_root: FrameLayout
    private lateinit var toolbar: Toolbar
    private lateinit var toobarFore : Toolbar
    private lateinit var foreLayout : PercentRelativeLayout
    private lateinit var menuView : ActionMenuView
    private var isFromShortcut : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressbar_in_main)

        fab = findViewById(R.id.fab)
        level_up = findViewById(R.id.fab_level_up)
        level_up!!.visibility = View.INVISIBLE
        level_up!!.rotation = -90f

        recyclerView = findViewById(R.id.recycle_view)
        android_nothing_card = findViewById(R.id.android_nothing)
        chooser_root = findViewById(R.id.chooser_in_main)

        chooser_root.visibility = View.INVISIBLE

        val intent = intent
        isGranted = intent.getBooleanExtra("isGranted", true)
        isFromShortcut = intent.getBooleanExtra("isFromShortcut",true)

        mLog.d("Permissions", isGranted.toString())

        initToolbar()

        //file choosing
        fab.setOnClickListener { Choose() }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
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
                        ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
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

        super.onCreate(savedInstanceState)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1){
            isGranted = ContextCompat.checkSelfPermission(this@MainActivity, permissions[0]) == PackageManager.PERMISSION_GRANTED
            if (isGranted)
                initActivity()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (!items.isInSelectMode)
                    drawerLayout!!.openDrawer(GravityCompat.START)
                else{
                    inSelectMode(false,false)

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initShortcuts(){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N_MR1)
            return
        val manager = getSystemService(ShortcutManager::class.java)
        val max = manager.maxShortcutCountPerActivity-1
        val infos = ArrayList<ShortcutInfo>()
        var i = 0
        while(i < max && i < mTextures.size){

            val temp = mTextures[i]
            if (!temp.IfIsResourcePack("PE")!!) {
                i--
                continue
            }

            val intent = Intent(ACTION_VIEW,null,this@MainActivity,DetailsActivity::class.java)
            intent.putExtra("texture_name", temp.name)
            intent.putExtra("texture_description", temp.description)
            intent.putExtra("texture_icon", temp.icon)
            intent.putExtra("texture_version", temp.getVersion())
            intent.putExtra("texture_path", temp.path)
            val info = ShortcutInfo.Builder(this,"pack $i")
                    .setShortLabel(temp.name)
                    .setIcon(Icon.createWithBitmap(BitmapFactory.decodeFile(temp.icon)))
                    .setIntent(intent)
                    .build()
            infos.add(info)

            i++
        }
        manager.dynamicShortcuts = infos
    }

    private fun updatePinnedShortcut(){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N_MR1)
            return
        val manager = getSystemService(ShortcutManager::class.java)
        val pinned = manager.pinnedShortcuts
        for (i in 0 until pinned.size){
            var isFound = false
            if(pinned[i].id=="add") continue
            for (j in 0 until mTextures.size){
                if (pinned[i].intent.getStringExtra("texture_path")==mTextures[j].path){
                    isFound = true
                    break
                }
            }
            if (isFound==false){
                manager.disableShortcuts(Arrays.asList(pinned[i].id))
            }
        }
    }

    private fun initActivity() {
        //init for textures list
        recyclerView = findViewById(R.id.recycle_view)
        class firstLoad : AsyncTask<Void, Int, Boolean>() {
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
        firstLoad().execute()

        recyclerView.addItemDecoration(SpacesItemDecoration(16))
        recyclerView.setHasFixedSize(true)


        menuView.setOnMenuItemClickListener { item ->
            when(item!!.itemId){
                R.id.delete -> {
                    val paths = ArrayList<Textures>()
                    var hasBeenDeleted = 0
                    for(it in items.selectedItems) {
                        val position = it-hasBeenDeleted
                        val path = items.getItem(position)
                        paths.add(path)
                        mTextures.remove(path)
                        items.notifyItemRemoved(position)
                        hasBeenDeleted++
                    }

                    setLayoutManager()
                    initShortcuts()
                    updatePinnedShortcut()
                    Snackbar.make(fab, R.string.deleted_completed, Snackbar.LENGTH_LONG)
                            .setCallback(DeletingCallback(paths.toList()))
                            .setAction(R.string.undo) {
                                paths.forEach {
                                    if (File(it.path).exists()) {

                                        mTextures.add(it.position,it)
                                        recyclerView.visibility = View.VISIBLE
                                        android_nothing_card.visibility = View.GONE
                                        loadList()
                                        setLayoutManager()
                                        initShortcuts()
                                        updatePinnedShortcut()
                                    } else {
                                        Snackbar.make(fab, R.string.failed, Snackbar.LENGTH_SHORT).show()
                                    }
                                }

                            }.show()
                    inSelectMode(false,false)
                }
            }
            true
        }

        //for swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh)
        swipeRefreshLayout!!.setColorSchemeColors(resources.getColor(R.color.colorAccent), resources.getColor(R.color.google_blue), resources.getColor(R.color.google_red), resources.getColor(R.color.google_green))
        swipeRefreshLayout!!.setOnRefreshListener {
            Thread(Runnable {
                try {
                    Thread.sleep(500)
                } catch (e : Throwable) {
                    e.printStackTrace()
                }

                runOnUiThread {
                    inSelectMode(false,true)
                    swipeRefreshLayout!!.isRefreshing = false
                }
            }).start()
        }

        android_nothing_card.setOnClickListener {
            val show = AnimationUtils.loadAnimation(this@MainActivity, R.anim.cards_show)
            android_nothing_card.startAnimation(show)
            show.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    loadList()
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }
    }

    var lastTime : Long = 0
    var count = 0
    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else if (!items.isInSelectMode) {
            if (count == 0) {
                lastTime = System.currentTimeMillis()
                Snackbar.make(fab,R.string.double_back_exit,Snackbar.LENGTH_LONG)
                        .setCallback(object :Snackbar.Callback(){
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                count = 0
                                super.onDismissed(transientBottomBar, event)
                            }
                        })
                        .show()
            }
            else{
                ActivityCollector.finishAll()
            }
            Log.d("DoubleBack","Count = $count , Delay = ${System.currentTimeMillis() - lastTime}")
            count ++
        }
        else{
            inSelectMode(false,false)
        }
    }

    var mTextures : ArrayList<Textures> = ArrayList()
    lateinit var items : TextureItems

    private fun loadList() {
        mTextures = ArrayList()

        val packsListDir = File(Environment.getExternalStorageDirectory().toString() + "/games/com.mojang/resource_packs/")
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
                        if (texture.IfIsResourcePack("ALL")!!) {
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
                                val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, view.findViewById(R.id.card_texture_icon), getString(R.string.pack_icon_transition))
                                ActivityCompat.startActivityForResult(this@MainActivity, intent, 2, options.toBundle())
                            } else {
                                startActivityForResult(intent, 2)
                            }
                        }
                    }
                }
            })

            runOnUiThread({
                setLayoutManager()
                items.notifyDataSetChanged()
            })
        } else
            MakeErrorDialog("Failed to make textures root directory.")
    }

    fun inSelectMode(withLoadingList: Boolean){
        if (items.isInSelectMode){
            val animation = AnimationUtils.loadAnimation(this,R.anim.cards_show)
            foreLayout.visibility = View.VISIBLE
            foreLayout.startAnimation(animation)
            setSupportActionBar(toobarFore)
        }
        else{
            if (withLoadingList)
                loadList()
            items.deselectAll()
            val animation = AnimationUtils.loadAnimation(this,R.anim.cards_hide)
            foreLayout.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    foreLayout.visibility = View.GONE
                    setSupportActionBar(toolbar)

                }

                override fun onAnimationStart(animation: Animation?) {}

            })
        }
    }

    fun inSelectMode(inSelect : Boolean,withLoadingList: Boolean){
        items.isInSelectMode = inSelect
        inSelectMode(withLoadingList)
    }

    private var adapter: FileChooserAdapter? = null
    var level_up : FloatingActionButton? = null
    private fun Choose() {
        if (chooser_root.visibility == View.INVISIBLE) {
            fab.isEnabled = false
            level_up!!.show()
            toolbar.setTitle(R.string.choosing_alert)
            toolbar.subtitle = adapter!!.getPath()

            chooser_root.visibility = View.VISIBLE
            if (!isFromShortcut) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val animator = ViewAnimationUtils.createCircularReveal(chooser_root, fab.x.toInt() + fab.width / 2, fab.y.toInt() - fab.height / 2, 0f, Math.hypot(chooser_root.width.toDouble(), chooser_root.height.toDouble()).toFloat())
                    animator.duration = 300
                    animator.start()
                }
            }
            val first = RotateAnimation(0.0f, 45.0f * 4 + 15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            first.duration = 200
            fab.startAnimation(first)
            first.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    fab.rotation = 60.0f
                    val second = RotateAnimation(15.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
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
            level_up!!.hide()
            toolbar.setTitle(getString(R.string.app_name))
            toolbar.subtitle = ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val animator = ViewAnimationUtils.createCircularReveal(chooser_root, fab.x.toInt() + fab.width / 2,
                        fab.y.toInt() - fab.height / 2, Math.hypot(chooser_root.width.toDouble(), chooser_root.height.toDouble()).toFloat(), 0f)
                animator.duration = 400
                animator.start()
            }
            else chooser_root.visibility = View.INVISIBLE

            fab.rotation = 45.0f
            val first = RotateAnimation(0.0f, -45.0f * 4 + 15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            first.duration = 200
            first.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    fab.rotation = -15.0f
                    val second = RotateAnimation(0.0f, 15.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
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

            Handler().postDelayed({ chooser_root.visibility = View.INVISIBLE }, 390)
        }
        isFromShortcut = false
    }
    var chooser : RecyclerView? = null
    private fun loadFileChooser() {
        chooser = findViewById(R.id.file_chooser_view)
        chooser!!.layoutManager = StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL)

        adapter = FileChooserAdapter(Environment.getExternalStorageDirectory().path, mutableListOf("zip"))

        adapter?.setOnItemClickListener(object : FileChooserAdapter.OnItemClickListener{
            override fun onClick(view: View, data: Intent) {
                val path = data.getStringExtra("path")
                if (File(path).isFile){
                    val pref = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    val intent = if(pref.getString("pref_conversion_style","new") == "new") Intent(this@MainActivity, ConversionActivity::class.java)
                                else Intent(this@MainActivity,ConversionActivityOld::class.java)
                    intent.putExtra("filePath",path)
                    startActivityForResult(intent, 0)
                }
                else{
                    toolbar.subtitle = path
                }
            }

        })
        level_up!!.setOnClickListener({
            if(adapter!!.upLevel(Environment.getExternalStorageDirectory().path))
                toolbar.subtitle = adapter!!.getPath()
            else
                Snackbar.make(fab as View,R.string.non_upper_level,Snackbar.LENGTH_SHORT).show()
        })

        runOnUiThread { chooser!!.adapter = adapter }
    }

    fun setLayoutManager() {
        if (items.itemCount == 0) {
            recyclerView.visibility = View.GONE
            android_nothing_card.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            android_nothing_card.visibility = View.GONE
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
                mLog.i("Layout Manager", "Layout manager set by Grid Layout Manager. Line count is " + lineCount)
                layoutManager = GridLayoutManager(this, lineCount)
            }
        }

        recyclerView.layoutManager = layoutManager
    }
}