package happy.blumental.maxim.testproject.mvc

import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.checkedChanges
import happy.blumental.maxim.testproject.R
import happy.blumental.maxim.testproject.bind
import happy.blumental.maxim.testproject.data.Event
import happy.blumental.maxim.testproject.data.TodoItem
import happy.blumental.maxim.testproject.mainThread
import rx.lang.kotlin.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

class TodoListView(val layout: View, model: TodoListModel) {

    private val addButton = layout.findViewById(R.id.button_add) as Button
    private val clearCheckedButton = layout.findViewById(R.id.button_clear_checked)
    private val itemsView = layout.findViewById(R.id.items) as ViewGroup
    private val context = layout.context
    private val inflater = LayoutInflater.from(context)
    private val itemToView = HashMap<TodoItem, View>()

    val itemClicksSubject = PublishSubject<TodoItem>()
    val removeCheckedSubject = PublishSubject<Any>()
    val addNewItemSubject = PublishSubject<String>()

    init {
        model.uiUpdates()
                .mainThread()
                .bind(layout)
                .subscribe {
                    updateUI(it)
                }
        addButtonClicks()
                .debounce(200, TimeUnit.MILLISECONDS)
                .mainThread()
                .subscribe {
                    showCreateTaskDialog()
                }
        clearCheckedButtonClicks()
                .debounce(200, TimeUnit.MILLISECONDS)
                .mainThread()
                .subscribe {
                    showRemoveItemsDialog()
                }
    }

    private fun updateUI(event: Event) {
        // check delete
        if (event.status == Event.Status.DELETE) {
            itemsView.removeView(itemToView.get(event.todoItem))
            itemToView.remove(event.todoItem)
            return
        }

        // create or update
        val presentItem = itemToView.getOrPut(event.todoItem, {
            val itemView = createItemView(event.todoItem)
            itemsView.addView(itemView)
            itemView
        })

        when (event.status) {
            Event.Status.PROGRESS -> {
                presentItem.isEnabled = false
            }
            Event.Status.COMPLETED -> {
                presentItem.isEnabled = true
            }
            Event.Status.FAILED -> {
                Toast.makeText(context, "Failed to make an update", Toast.LENGTH_LONG)
                presentItem.isEnabled = true
            }
        }
    }

    private fun createItemView(item: TodoItem): View {
        val itemView = inflater.inflate(R.layout.entry, itemsView, false)
        val checkBox = itemView.findViewById(R.id.checkbox) as CheckBox
        checkBox.text = item.title
        checkBox.isChecked = item.checked
        checkBox.checkedChanges().skip(1)
                .mainThread()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe {
            item.checked = it
            itemClicksSubject.onNext(item)
        }
        return itemView
    }

    fun addButtonClicks() = addButton.clicks()
    fun clearCheckedButtonClicks() = clearCheckedButton.clicks()

    private fun showRemoveItemsDialog() {

        val alertDialog = AlertDialog.Builder(this.context)

        alertDialog.setMessage("Do you really want to delete checked items?")
                .setTitle("Remove completed tasks")
                .setCancelable(true)
                .setPositiveButton("Yes", { dialog, which -> removeCheckedSubject.onNext(null) })
                .setNegativeButton("Cancel", { dialog, which -> dialog.cancel() })
                .setOnCancelListener { dialog -> dialog.cancel() }

        alertDialog.create().show()
    }

    private fun showCreateTaskDialog() {

        val alertDialog = AlertDialog.Builder(this.context)
        val customView = inflater.inflate(R.layout.create_item_dialog, null)
        val editText = customView.findViewById(R.id.dialogEditText) as EditText

        alertDialog.setTitle("Add a new task")
                .setView(customView)
                .setCancelable(true)
                .setPositiveButton("Add", { dialog, which -> addNewItemSubject.onNext(editText.text.toString()) })
                .setNegativeButton("Cancel", { dialog, which -> dialog.cancel() })
                .setOnCancelListener { dialog -> dialog.cancel() }

        alertDialog.create().show()
    }

}