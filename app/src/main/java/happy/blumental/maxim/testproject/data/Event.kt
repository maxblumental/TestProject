package happy.blumental.maxim.testproject.data

class Event(val todoItem: TodoItem, var status: Event.Status) {

    enum class Status {
        COMPLETED, PROGRESS, FAILED, DELETE
    }
}