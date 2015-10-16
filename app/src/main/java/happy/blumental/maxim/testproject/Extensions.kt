package happy.blumental.maxim.testproject

import android.view.View
import android.view.ViewGroup
import com.parse.ParseObject
import com.trello.rxlifecycle.RxLifecycle
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import happy.blumental.maxim.testproject.data.PTodoItem
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

fun CompositeSubscription.plusAssign(s: Subscription) = add(s)

fun ViewGroup.getChildren(): List<View> {
    val children = arrayListOf<View>()
    for (i in 0..childCount-1)
        children += getChildAt(i)
    return children
}

fun ParseObject.sync() : ParseObject {
    this.save()
    this.fetch<PTodoItem>()
    return this
}

fun <T> Observable<T>.bind(activity: RxAppCompatActivity) = compose(activity.bindToLifecycle<T>())

fun <T> Observable<T>.bind(view: View) = compose(RxLifecycle.bindView<T>(view))

fun <T> Observable<T>.mainThread() = observeOn(AndroidSchedulers.mainThread())