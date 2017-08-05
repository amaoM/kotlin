package thread.single.threaded.execution

import kotlin.concurrent.withLock

fun main(args: Array<String>) {
  println("Testing Gate")
  val gate = Gate()
  UserThread(gate, "Alice", "Alaska").start()
  UserThread(gate, "Bobby", "Brazil").start()
  UserThread(gate, "Chris", "Canada").start()
}

class Gate {
  private var counter = 0
  private var name = "Nobody"
  private var address = "Nowhere"

  @Synchronized
  fun pass(name: String, address: String) {
    this.counter++
    this.name = name
    this.address = address
    check()
  }

  @Synchronized
  override fun toString(): String {
    return "No.${this.counter}: ${this.name}, ${this.address}"
  }

  fun check() {
    if (this.name.elementAt(0) != this.address.elementAt(0)) {
      println("***** Broken ***** ${toString()}")
    }
  }
}

class UserThread(gate: Gate, name: String, address: String) : Thread(){
  private var gate: Gate
  private var myname: String
  private var myaddress: String

  init {
    this.gate = gate
    this.myname = name
    this.myaddress = address
  }

  override fun run() {
    println(this.myname + " BEGIN")
    while (true) {
      gate.pass(this.myname, this.myaddress)
    }
  }
}
