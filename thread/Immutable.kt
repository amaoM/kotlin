package thread

fun main(args: Array<String>) {
  val alice = Person("Alice", "Alaska")
  PrintPersonThread(alice).start()
  PrintPersonThread(alice).start()
  PrintPersonThread(alice).start()
}

class Person(name: String, address: String) {
  private val name: String
  private val address: String

  init {
    this.name = name
    this.address = address
  }

  fun toMessage(): String = "[ Person: name = ${this.name}, address = ${this.address} ]"
}

class PrintPersonThread(person: Person) : Thread() {
  private val person: Person

  init {
    this.person = person
  }

  override fun run() {
    while (true) {
      println("${Thread.currentThread().getName()} prints ${this.person}")
    }
  }
}
