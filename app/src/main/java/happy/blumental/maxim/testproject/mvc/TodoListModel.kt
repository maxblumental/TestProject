package happy.blumental.maxim.testproject.mvc

import happy.blumental.maxim.testproject.bind
import happy.blumental.maxim.testproject.data.Event
import happy.blumental.maxim.testproject.mvc.TodoListView
import happy.blumental.maxim.testproject.data.TodoItem
import happy.blumental.maxim.testproject.mainThread
import happy.blumental.maxim.testproject.plusAssign
import rx.Observable
import rx.lang.kotlin.BehaviourSubject
import rx.lang.kotlin.PublishSubject
import rx.lang.kotlin.toObservable
import rx.lang.kotlin.toSingletonObservable
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit
import happy.blumental.maxim.testproject.mvc.*

interface TodoListModel {
    fun uiUpdates(): Observable<Event>
}

class TodoListModelImpl() : TodoListModel {
    private val subscription = CompositeSubscription()
    private val updateUISubject = PublishSubject<Event>()

    private val items = LinkedHashMap<TodoItem, Event.Status>()

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
                .flatMap {
                    val item = TodoItem(UUID.randomUUID().toString(), it, false)
                    Event(item, Event.Status.COMPLETED).toSingletonObservable()
                }
                .subscribe {
                    items[it.todoItem] = it.status
                    updateUISubject.onNext(it)
                }

        subscription += view.itemClicksSubject
                .mainThread()
                .debounce(200, TimeUnit.MILLISECONDS)
                .map { Event(it, Event.Status.PROGRESS) }
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
                    updateUISubject.onNext(it)
                }
    }

    fun detachView() = subscription.clear()

    override fun uiUpdates() = updateUISubject
}
