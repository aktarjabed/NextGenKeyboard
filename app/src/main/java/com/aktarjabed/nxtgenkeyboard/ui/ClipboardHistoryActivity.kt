package com.aktarjabed.nxtgenkeyboard.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launchedFromIme = intent?.getBooleanExtra(EXTRA_FROM_IME, false) == true
        repo = ClipboardRepository(AppDatabase.getInstance(this).clipboardDao())

        val listView = ListView(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter
        setContentView(listView)

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
            if (launchedFromIme) {
                // Hand the text to the keyboard service; it commits on return.
                ClipboardInsertBus.post(item.text)
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

    private fun showItemMenu(item: ClipboardEntity) {
        val options = listOf(
            if (item.pinned) getString(R.string.unpin) else getString(R.string.pin),
            getString(R.string.copy),
            getString(R.string.delete),
            getString(R.string.clear_unpinned),
            getString(R.string.clear_all)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.clipboard_item)
            .setItems(options.toTypedArray()) { _, which ->
                when (which) {
                    0 -> lifecycleScope.launch { repo.togglePin(item) }
                    1 -> copyToSystemClipboard(item.text)
                    2 -> lifecycleScope.launch { repo.delete(item) }
                    3 -> lifecycleScope.launch { repo.clearUnpinned() }
                    4 -> confirmClearAll()
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
    }
}