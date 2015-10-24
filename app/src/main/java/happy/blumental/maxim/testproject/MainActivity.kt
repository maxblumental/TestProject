package happy.blumental.maxim.testproject

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import happy.blumental.maxim.testproject.mvc.TodoListModelImpl
import happy.blumental.maxim.testproject.mvc.TodoListView
import rx.subjects.PublishSubject

public class MainActivity : RxAppCompatActivity() {

    lateinit var todoListView: TodoListView

    val menuClicks = PublishSubject.create<Int>()

    companion object {
        val model = TodoListModelImpl()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        todoListView = TodoListView(findViewById(R.id.content), model, menuClicks.filter { it == R.id.action_refresh })

        if (savedInstanceState == null) {
            // load data from parse
            model.refresh()
        } else {
            // load data from bundle
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        return when (id) {
            R.id.action_refresh -> {
                menuClicks.onNext(id)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        model.attachView(todoListView)
    }

    override fun onPause() {
        super.onPause()
        model.detachView()
    }
}
