package happy.blumental.maxim.testproject.data

data class TodoItem(val id: String, val title: String, var checked: Boolean) {
    override fun hashCode() : Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is TodoItem -> id == other.id
            else -> false
        }
    }
}