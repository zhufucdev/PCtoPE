package com.zhufuc.pctope.Activities

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar

import com.zhufuc.pctope.Adapters.FileChooserAdapter
import com.zhufuc.pctope.Adapters.TextureItems
import com.zhufuc.pctope.Adapters.Textures
import com.zhufuc.pctope.Collectors.ActivityCollector
import com.zhufuc.pctope.Interf.DeletingCallback
import com.zhufuc.pctope.Interf.SpacesItemDecoration
import com.zhufuc.pctope.Utils.GetPathFromUri4kitkat
import com.zhufuc.pctope.Utils.*
import com.zhufuc.pctope.R

import java.io.File


class MainActivity : BaseActivity() {

    fun MakeErrorDialog(errorString: String) {
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
                if (chooser_root!!.visibility == View.VISIBLE)
                    Handler().postDelayed({ Choose() }, 1000)
            }
        } else if (requestCode == 2) {
            if (data == null)
                return
            if (data.getBooleanExtra("isDataChanged", false)) {
                loadList()
            }

        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                val uri = data!!.data
                val realPath = GetPathFromUri4kitkat.getPath(this@MainActivity, uri)
                val intent = Intent(this@MainActivity, ConversionActivity::class.java)
                intent.putExtra("filePath", realPath)
                startActivityForResult(intent, 0)
            }
        }
    }

    fun initToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeAsUpIndicator(R.drawable.menu)

    }

    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.READ_EXTERNAL_STORAGE)

    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    private var fab: FloatingActionButton? = null
    private var recyclerView: RecyclerView? = null
    private var android_nothing_card: LinearLayout? = null
    private var chooser_root: FrameLayout? = null
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressbar_in_main) as ProgressBar

        fab = findViewById(R.id.fab) as FloatingActionButton
        level_up = findViewById(R.id.fab_level_up) as FloatingActionButton
        level_up!!.visibility = View.INVISIBLE
        level_up!!.rotation = -90f

        recyclerView = findViewById(R.id.recycle_view) as RecyclerView
        android_nothing_card = findViewById(R.id.android_nothing) as LinearLayout
        chooser_root = findViewById(R.id.chooser_in_main) as FrameLayout

        chooser_root!!.visibility = View.INVISIBLE

        val intent = intent
        isGranted = intent.getBooleanExtra("isGranted", true)

        mLog.d("status", isGranted.toString() + "")

        //file choosing
        fab!!.setOnClickListener { Choose() }

        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState > 0) {
                    fab!!.hide()
                } else {
                    fab!!.show()
                }
            }
        })


        if (isGranted) {
            initActivity()
        } else {
            Snackbar.make(fab!!, R.string.permissions_request, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
                        Thread(Runnable {
                            while (ContextCompat.checkSelfPermission(this@MainActivity, permissions[0]) == PackageManager.PERMISSION_DENIED) {
                            }
                            isGranted = true
                            runOnUiThread { initActivity() }
                        }).start()
                    }.show()
        }


        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.menu)
        }

        navigationView = findViewById(R.id.nav_view) as NavigationView
        drawerLayout = findViewById(R.id.drawer_main) as DrawerLayout

        navigationView!!.setNavigationItemSelectedListener { item ->
            drawerLayout!!.closeDrawer(GravityCompat.START)
            when (item.itemId) {
            //case R.id.nav_packer:
            //    ActivityCollector.finishOther(MainActivity.this);
            //    final Intent packer = new Intent(MainActivity.this,CompressionActivity.class);
            //    startActivity(packer, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
            //overridePendingTransition(0,0);
            //   break;
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
        //throw new RuntimeException("TEST");
        initToolbar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> drawerLayout!!.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    private val items = TextureItems()

    private fun initActivity() {
        //init for textures list
        recyclerView = findViewById(R.id.recycle_view) as RecyclerView
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
            }
        }
        firstLoad().execute()

        recyclerView!!.addItemDecoration(SpacesItemDecoration(16))
        val swipe = DefaultItemAnimator()
        recyclerView!!.itemAnimator = swipe
        recyclerView!!.setHasFixedSize(true)

        val mCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deleting = items.getItem(position)
                val test = File(deleting.path.toString())
                val oldTemp = items
                items.remove(position)

                if (items.itemCount == 0) {
                    recyclerView!!.visibility = View.GONE
                    android_nothing_card!!.visibility = View.VISIBLE
                    val show = AnimationUtils.loadAnimation(this@MainActivity, R.anim.cards_show)
                    android_nothing_card!!.startAnimation(show)
                }

                val callback = DiffUtilCallback(oldTemp, items)
                val diffResult = DiffUtil.calculateDiff(callback)
                diffResult.dispatchUpdatesTo(items)
                items.notifyItemRemoved(position)

                setLayoutManager()

                Snackbar.make(fab!!, R.string.deleted_completed, Snackbar.LENGTH_LONG)
                        .setCallback(DeletingCallback(test))
                        .setAction(R.string.undo) {
                            if (test.exists()) {
                                val oldTemp1 = items
                                items.addItem(position, deleting)
                                val callback1 = DiffUtilCallback(oldTemp1, items)
                                val diffResult1 = DiffUtil.calculateDiff(callback1)
                                recyclerView!!.visibility = View.VISIBLE
                                android_nothing_card!!.visibility = View.GONE
                                diffResult.dispatchUpdatesTo(items)
                                items.notifyItemInserted(position)

                                setLayoutManager()
                            } else {
                                Snackbar.make(fab!!, R.string.failed, Snackbar.LENGTH_SHORT).show()
                            }
                        }.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(mCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        //for swipe refresh layout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh) as SwipeRefreshLayout
        swipeRefreshLayout!!.setColorSchemeColors(resources.getColor(R.color.colorAccent), resources.getColor(R.color.google_blue), resources.getColor(R.color.google_red), resources.getColor(R.color.google_green))
        swipeRefreshLayout!!.setOnRefreshListener {
            Thread(Runnable {
                try {
                    Thread.sleep(500)
                } catch (e : Throwable) {
                    e.printStackTrace()
                }

                runOnUiThread {
                    loadList()
                    swipeRefreshLayout!!.isRefreshing = false
                }
            }).start()
        }

        android_nothing_card!!.setOnClickListener {
            val show = AnimationUtils.loadAnimation(this@MainActivity, R.anim.cards_show)
            android_nothing_card!!.startAnimation(show)
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


    override fun onStart() {
        super.onStart()
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else
            ActivityCollector.finishAll()
    }

    private fun loadList() {

        //TextureItems oldTemp = items;
        items.clear()

        val packsListDir = File(Environment.getExternalStorageDirectory().toString() + "/games/com.mojang/resource_packs/")
        var packsList: Array<File>
        var make: Boolean? = true

        if (!packsListDir.exists()) make = packsListDir.mkdirs()

        if (make!!) {
            packsList = packsListDir.listFiles()

            for (aPacksList in packsList) {
                if (aPacksList.exists())
                    if (aPacksList.isDirectory) {
                        val texture = Textures(aPacksList)
                        if (texture.IfIsResourcePack("ALL")!!) {
                            items.addItem(texture)
                        }
                    }
            }

            //DiffUtil.Callback callback = new DiffUtilCallback(oldTemp,items);
            //DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
            //diffResult.dispatchUpdatesTo(items);

            items.setOnItemClickListener(object : TextureItems.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    if (!items.getIfIsAlertIconShown(view)) {
                        val intent = Intent(this@MainActivity, DetailsActivity::class.java)

                        val temp = items.getItem(position)
                        intent.putExtra("texture_name", temp.name)
                        intent.putExtra("texture_description", temp.description)
                        intent.putExtra("texture_icon", temp.icon)
                        intent.putExtra("texture_version", temp.getVersion())
                        intent.putExtra("texture_path", temp.path)

                        val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity, view.findViewById(R.id.card_texture_icon), getString(R.string.pack_icon_transition))
                        ActivityCompat.startActivityForResult(this@MainActivity, intent, 2, options.toBundle())

                    }
                }
            })

            recyclerView!!.adapter = items

            runOnUiThread({
                items.notifyDataSetChanged()
                setLayoutManager()
            })
        } else
            MakeErrorDialog("Failed to make textures root directory.")
    }

    private var adapter: FileChooserAdapter? = null
    var level_up : FloatingActionButton? = null
    private fun Choose() {
        if (chooser_root!!.visibility == View.INVISIBLE) {
            level_up!!.show()
            toolbar!!.setTitle(R.string.choosing_alert)
            toolbar!!.subtitle = adapter!!.getPath()

            val animator = ViewAnimationUtils.createCircularReveal(chooser_root, fab!!.x.toInt() + fab!!.width / 2, fab!!.y.toInt() - fab!!.height / 2, 0f, Math.hypot(chooser_root!!.width.toDouble(), chooser_root!!.height.toDouble()).toFloat())
            animator.duration = 300

            chooser_root!!.visibility = View.VISIBLE
            animator.start()

            val first = RotateAnimation(0.0f, 45.0f * 4 + 15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            first.duration = 200
            fab!!.startAnimation(first)
            first.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    fab!!.rotation = 60.0f
                    val second = RotateAnimation(15.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    second.duration = 100
                    fab!!.startAnimation(second)
                    second.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {

                        }

                        override fun onAnimationEnd(animation: Animation) {
                            fab!!.rotation = 45.0f
                        }

                        override fun onAnimationRepeat(animation: Animation) {

                        }
                    })
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        } else {
            level_up!!.hide()
            toolbar!!.setTitle(getString(R.string.app_name))
            toolbar!!.subtitle = ""

            val animator = ViewAnimationUtils.createCircularReveal(chooser_root, fab!!.x.toInt() + fab!!.width / 2,
                    fab!!.y.toInt() - fab!!.height / 2, Math.hypot(chooser_root!!.width.toDouble(), chooser_root!!.height.toDouble()).toFloat(), 0f)
            animator.duration = 400
            animator.start()

            fab!!.rotation = 45.0f
            val first = RotateAnimation(0.0f, -45.0f * 4 + 15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            first.duration = 200
            first.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    fab!!.rotation = -15.0f
                    val second = RotateAnimation(0.0f, 15.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                    second.duration = 100
                    fab!!.startAnimation(second)
                    second.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {

                        }

                        override fun onAnimationEnd(animation: Animation) {
                            fab!!.rotation = 0.0f
                        }

                        override fun onAnimationRepeat(animation: Animation) {

                        }
                    })
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            fab!!.startAnimation(first)

            Handler().postDelayed({ chooser_root!!.visibility = View.INVISIBLE }, 400)
        }
    }
    var chooser : RecyclerView? = null
    private fun loadFileChooser() {
        chooser = findViewById(R.id.file_chooser_view) as RecyclerView
        chooser!!.layoutManager = StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL)

        adapter = FileChooserAdapter(Environment.getExternalStorageDirectory().path, mutableListOf("zip"))

        adapter?.setOnItemClickListener(object : FileChooserAdapter.OnItemClickListener{
            override fun onClick(view: View, data: Intent) {
                val path = data.getStringExtra("path")
                if (File(path).isFile){
                    val intent = Intent(this@MainActivity, ConversionActivity::class.java)
                    intent.putExtra("filePath",path)
                    startActivityForResult(intent, 0)
                }
                else{
                    toolbar!!.subtitle = path
                }
            }

        })
        level_up!!.setOnClickListener({
            if(adapter!!.upLevel(Environment.getExternalStorageDirectory().path))
                toolbar!!.subtitle = adapter!!.getPath()
            else
                Snackbar.make(fab as View,R.string.non_upper_level,Snackbar.LENGTH_SHORT).show()
        })

        chooser!!.adapter = adapter
    }

    fun setLayoutManager() {
        if (items.itemCount == 0) {
            recyclerView!!.visibility = View.GONE
            android_nothing_card!!.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            android_nothing_card!!.visibility = View.GONE
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

        recyclerView!!.layoutManager = layoutManager
    }
}
