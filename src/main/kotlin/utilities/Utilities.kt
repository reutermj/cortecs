package utilities

fun <T>List<T>.replace(index: Int, element: T) = this.toMutableList().apply { this[index] = element }
fun <T, K, V>Map<K, V>.fold(init: T, f: (T, K, V) -> T): T {
    var acc = init
    for((key, value) in this) acc = f(acc, key, value)
    return acc
}