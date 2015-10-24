package happy.blumental.maxim.testproject.data

import com.parse.ParseClassName
import com.parse.ParseObject

@ParseClassName("TodoItem")
class PTodoItem() : ParseObject() {

    var title: String
        get() = if (has("title")) getString("title") else "Untitled"
        set(title: String) = put("title", title)

    var isDone: Boolean
        get() = if (has("isDone")) getBoolean("isDone") else false
        set(isDone: Boolean) = put("isDone", isDone)

    fun set(title: String, isDone: Boolean) {
        this.title = title
        this.isDone = isDone
    }
}