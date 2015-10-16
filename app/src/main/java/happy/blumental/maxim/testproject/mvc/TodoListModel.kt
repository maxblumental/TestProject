package happy.blumental.maxim.testproject.mvc

import happy.blumental.maxim.testproject.data.Event
import happy.blumental.maxim.testproject.data.PTodoItem
import happy.blumental.maxim.testproject.data.TodoItem
import happy.blumental.maxim.testproject.mainThread
import happy.blumental.maxim.testproject.plusAssign
import happy.blumental.maxim.testproject.sync
import rx.Observable
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.toObservable
import rx.lang.kotlin.toSingletonObservable
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit

interface TodoListModel {
    fun uiUpdates(): Observable<Event>
}

class TodoListModelImpl() : TodoListModel {
    private val subscription = CompositeSubscription()
    private val updateUISubject = PublishSubject<Event>()

    private val items = LinkedHashMap<TodoItem, Event.Status>()
    private val parseItems = HashMap<String, PTodoItem>()

    init {
        updateUISubject.filter { it.status == Event.Status.PROGRESS }
                .flatMap {
                    it.toSingletonObservable().delay(3000, TimeUnit.MILLISECONDS)
                }
                .mainThread()
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
                .mainThread()
                .map {
                    val parseItem = PTodoItem(it, false)
                    val item = TodoItem(parseItem.sync().objectId, it, false)
                    parseItems.put(parseItem.objectId, parseItem)
                    Event(item, Event.Status.COMPLETED)
                }
                .subscribe {
                    items[it.todoItem] = it.status
                    updateUISubject.onNext(it)
                }

        subscription += view.itemClicksSubject
                .mainThread()
                .debounce(200, TimeUnit.MILLISECONDS)
                .map {
                    val parseItem : PTodoItem? = parseItems.get(it.id)
                    parseItem?.isDone = it.checked
                    parseItem?.sync()
                    Event(it, Event.Status.COMPLETED)
                }
                .subscribe {
                    updateUISubject.onNext(it)
                }

        subscription += view.removeCheckedSubject
                .mainThread()
                .flatMap {
                    items.filter { it.key.checked }
                            .map { Event(it.key, Event.Status.DELETE) }
                            .toObservable()
                }
                .subscribe {
                    items.remove(it.todoItem)
                    parseItems.get(it.todoItem.id)?.delete()
                    updateUISubject.onNext(it)
                }
    }

    fun detachView() = subscription.clear()

    override fun uiUpdates() = updateUISubject
}
