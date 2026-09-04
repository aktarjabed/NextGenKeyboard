package com.aktarjabed.nxtgenkeyboard.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.aktarjabed.nxtgenkeyboard.R
import com.aktarjabed.nxtgenkeyboard.clipboard.AppDatabase
import com.aktarjabed.nxtgenkeyboard.clipboard.ClipboardEntity
import com.aktarjabed.nxtgenkeyboard.clipboard.ClipboardRepository
import com.aktarjabed.nxtgenkeyboard.util.ClipboardInsertBus
import kotlinx.coroutines.launch

class ClipboardHistoryActivity : AppCompatActivity() {

    private lateinit var repo: ClipboardRepository
    private lateinit var adapter: ArrayAdapter<String>
    private val items = mutableListOf<ClipboardEntity>()
    private var launchedFromIme = false
    private var sessionToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        launchedFromIme = intent?.getBooleanExtra(EXTRA_FROM_IME, false) == true
        sessionToken = intent?.getStringExtra(EXTRA_SESSION_TOKEN)
        repo = ClipboardRepository(AppDatabase.getInstance(this).clipboardDao())

        val dp = { value: Int -> (value * resources.displayMetrics.density).toInt() }

        val rootView = android.widget.FrameLayout(this)
        val listView = ListView(this)
        val emptyView = android.widget.TextView(this).apply {
            text = getString(R.string.clipboard_empty)
            gravity = android.view.Gravity.CENTER
            setPadding(dp(32), dp(32), dp(32), dp(32))
        }

        rootView.addView(listView, android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        ))
        rootView.addView(emptyView, android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT
        ))

        listView.emptyView = emptyView

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter
        setContentView(rootView)

        lifecycleScope.launch {
            repo.history.collect { list ->
                items.clear()
                items.addAll(list)
                adapter.clear()
                adapter.addAll(list.map { if (it.pinned) "★ ${it.text}" else it.text })
                adapter.notifyDataSetChanged()
            }
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val item = items.getOrNull(position) ?: return@setOnItemClickListener
            if (launchedFromIme && sessionToken != null) {
                // Hand the text to the keyboard service; it commits on return.
                ClipboardInsertBus.post(sessionToken!!, item.text)
                finish()
            } else {
                copyToSystemClipboard(item.text)
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val item = items.getOrNull(position) ?: return@setOnItemLongClickListener true
            showItemMenu(item)
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, R.string.clear_all).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            confirmClearAll()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showItemMenu(item: ClipboardEntity) {
        val options = listOf(
            if (item.pinned) getString(R.string.unpin) else getString(R.string.pin),
            getString(R.string.copy),
            getString(R.string.delete),
            getString(R.string.clear_unpinned)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.clipboard_item)
            .setItems(options.toTypedArray()) { _, which ->
                when (which) {
                    0 -> lifecycleScope.launch { repo.togglePin(item) }
                    1 -> copyToSystemClipboard(item.text)
                    2 -> lifecycleScope.launch { repo.delete(item) }
                    3 -> lifecycleScope.launch { repo.clearUnpinned() }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmClearAll() {
        AlertDialog.Builder(this)
            .setTitle(R.string.clear_all)
            .setMessage(R.string.clear_all_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch { repo.clearAll() }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun copyToSystemClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText("NxtGenClip", text))
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_FROM_IME = "extra_from_ime"
        const val EXTRA_SESSION_TOKEN = "extra_session_token"
    }
}