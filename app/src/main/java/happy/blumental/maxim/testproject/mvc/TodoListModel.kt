package happy.blumental.maxim.testproject.mvc

import android.util.Log
import android.widget.Toast
import com.parse.ParseACL
import com.parse.ParseQuery
import com.parse.ParseUser
import happy.blumental.maxim.testproject.data.Event
import happy.blumental.maxim.testproject.data.PTodoItem
import happy.blumental.maxim.testproject.data.TodoItem
import happy.blumental.maxim.testproject.plusAssign
import rx.Observable
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.onError
import rx.lang.kotlin.toObservable
import rx.lang.kotlin.toSingletonObservable
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

interface TodoListModel {
    fun uiUpdates(): Observable<Event>
    fun stateUpdates(): Observable<Boolean>
    fun refresh()
}

class TodoListModelImpl() : TodoListModel {
    companion object {

        private val SCHEDULER =
                Schedulers.from(ThreadPoolExecutor(1, 1, 2, TimeUnit.DAYS, LinkedBlockingQueue()))
    }

    private val subscription = CompositeSubscription()

    private val updateUISubject = PublishSubject<Event>()
    private val activeStateSubject = PublishSubject<Boolean>()
    private val items = LinkedHashMap<TodoItem, Event.Status>()

    private val parseItems = HashMap<String, PTodoItem>()
    init {
        updateUISubject.filter { it.status == Event.Status.PROGRESS }
                .flatMap {
                    it.toSingletonObservable()
                }
                .withScheduler()
                .map {
                    it.status = Event.Status.COMPLETED
                    it
                }
                .subscribe {
                    items[it.todoItem] = Event.Status.COMPLETED
                    updateUISubject.onNext(it)
                }
    }

    fun attachView(view: TodoListView) {
        items.map { Event(it.key, it.value) }
                .toObservable()
                .doOnCompleted { subscribeView(view) }
                .subscribe { updateUISubject.onNext(it) }

    }

    private fun subscribeView(view: TodoListView) {
        subscription += view.addNewItemSubject
                .withScheduler()
                .map {
                    val parseItem = PTodoItem()
                            .apply {
                                set(it, false)
                                acl = ParseACL(ParseUser.getCurrentUser())
                                save()
                            }
                    val item = TodoItem(parseItem.objectId, it, false)
                    parseItems.put(parseItem.objectId, parseItem)
                    Event(item, Event.Status.COMPLETED)
                }
                .subscribe {
                    items[it.todoItem] = it.status
                    updateUISubject.onNext(it)
                }

        subscription += view.removeCheckedSubject
                .withScheduler()
                .flatMap {
                    items.filter { it.key.checked }
                            .map { Event(it.key, Event.Status.DELETE) }
                            .toObservable()
                }
                .subscribe {
                    items.remove(it.todoItem)
                    parseItems.get(it.todoItem.id)?.delete()
                    parseItems.remove(it.todoItem.id)
                    updateUISubject.onNext(it)
                }

        subscription += view.itemClicksSubject
                .withScheduler()
                .map {
                    val parseItem = parseItems.get(it.id)
                    if (parseItem != null) {
                        parseItem.isDone = it.checked
                        parseItem.saveInBackground()
                        Event(it, Event.Status.PROGRESS)
                    } else {
                        throw IllegalStateException("Value not found for key: ${it.id}")
                    }
                }
                .onError {
                    throwable -> Log.e("itemClicksSubject", throwable.message)
                }
                .subscribe {
                    updateUISubject.onNext(it)
                }

        subscription += view.refreshes()
                .debounce(500, TimeUnit.MILLISECONDS)
                .withScheduler()
                .subscribe { refresh() }
    }

    fun detachView() = subscription.clear()

    override fun uiUpdates() = updateUISubject

    override fun stateUpdates() = activeStateSubject

    override fun refresh() {
        activeStateSubject.onNext(false)
        // clear
        items.clear()
        parseItems.clear()

        // load data from parse
        val query = ParseQuery.getQuery<PTodoItem>("TodoItem")
        val list = query.find()

        for (pItem in list) {
            val item = TodoItem(pItem.objectId, pItem.title, pItem.isDone)
            items.put(item, Event.Status.PROGRESS)
            parseItems.put(pItem.objectId, pItem)
            updateUISubject.onNext(
                    Event(TodoItem(pItem), Event.Status.PROGRESS))
        }

        // indicate progress
        activeStateSubject.onNext(true)
    }

    private fun <T> Observable<T>.withScheduler() =
            subscribeOn(SCHEDULER)
                    .observeOn(SCHEDULER)
}
