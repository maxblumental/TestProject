package happy.blumental.maxim.testproject

import com.parse.*
import rx.Observable
import java.util.concurrent.TimeUnit

public object RxParse {

    public fun login(username: String, password: String): Observable<ParseUser> {
        return Observable.create { subscriber ->
            try {
                val user = ParseUser.logIn(username, password)
                subscriber.onNext(user)
                subscriber.onCompleted()
            } catch (ex: ParseException) {
                subscriber.onError(ex)
            }
        }
    }

    public fun register(username: String, password: String, firstName: String, lastName: String, beforeSave: (ParseUser) -> Unit = {}): Observable<ParseUser> {
        return Observable.create { subscriber ->
            try {
                val user = ParseUser()
                user.username = username
                user.setPassword(password)
                user.email = username
                user.put("firstName", firstName)
                user.put("lastName", lastName)
                beforeSave.invoke(user)
                user.signUp()
                subscriber.onNext(user)
                subscriber.onCompleted()
            } catch (ex: ParseException) {
                subscriber.onError(ex)
            }
        }
    }

    public fun <T : ParseObject> save(toSave: T): Observable<T> {
        return Observable.create { subscriber ->
            try {
                toSave.save()
                subscriber.onNext(toSave)
                subscriber.onCompleted()
            } catch (ex: ParseException) {
                subscriber.onError(ex)
            }
        }
    }

    public fun <T : ParseObject> saveAndRetry(toSave: T, delay: Long): Observable<T> {
        return save(toSave).onErrorResumeNext {
            Observable.just(toSave)
                    .delay(delay, TimeUnit.MILLISECONDS)
                    .flatMap { saveAndRetry(toSave, delay) }
        }
    }

    public fun <T : ParseObject> find(query: ParseQuery<T>): Observable<List<T>> {
        val findQuery = query
        return Observable.create { subscriber ->
            try {
                val result = findQuery.find()
                subscriber.onNext(result)
                subscriber.onCompleted()
            } catch (ex: ParseException) {
                subscriber.onError(ex)
            }

        }
    }

    public fun <T : ParseObject> pin(toPin: T, name: String = "_default"): Observable<T> {
        return Observable.create { subscriber ->
            try {
                toPin.pin(name)
                subscriber.onNext(toPin)
                subscriber.onCompleted()
            } catch (ex: ParseException) {
                subscriber.onError(ex)
            }
        }
    }

    public fun <T : ParseObject> pin(toPin: List<T>, name: String = "_default"): Observable<List<T>> {
        return Observable.create { subscriber ->
            try {
                ParseObject.pinAll(name, toPin)
                subscriber.onNext(toPin)
                subscriber.onCompleted()
            } catch (ex: ParseException) {
                subscriber.onError(ex)
            }
        }
    }

    public fun <T> callFunction(name: String, params: Map<String, *>): Observable<T> {
        return Observable.create { subscriber ->
            try {
                val result = ParseCloud.callFunction<T>(name, params)
                subscriber.onNext(result)
                subscriber.onCompleted()
            } catch (ex: ParseException) {
                subscriber.onError(ex)
            }
        }
    }

}

fun <T: ParseObject> ParseQuery<T>.rxFind() = RxParse.find(this)