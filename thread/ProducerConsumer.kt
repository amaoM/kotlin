package thread

import java.util.Random

fun main(args: Array<String>) {
	val table = Table(3)
	MakerThread("MakerThread-1", table, 31415).start()
	MakerThread("MakerThread-2", table, 92653).start()
	MakerThread("MakerThread-3", table, 58979).start()
	EaterThread("EaterThread-1", table, 32384).start()
	EaterThread("EaterThread-2", table, 62643).start()
	EaterThread("EaterThread-3", table, 38327).start()
}

class MakerThread(name: String, table: Table, seed: Long) : Thread(name) {
	private val random: Random
	private val table: Table
	companion object {
		private var id = 0
		
		@Synchronized
		private fun nextId(): Int {
			return this.id++
		}
	}
	
	init {
		this.table = table
		this.random = Random(seed)
	}
	
	override fun run() {
		try {
			while (true) {
				Thread.sleep(random.nextInt(1000).toLong())
				val cake = "[ Cake No.${nextId()} by ${getName()} ]"
				this.table.put(cake)
			}
		} catch (e: InterruptedException) {
		}
	}
}

class EaterThread(name: String, table: Table, seed: Long) : Thread(name) {
	private val random: Random
	private val table: Table
	
	init {
		this.table = table
		this.random = Random(seed)
	}
	
	override fun run() {
		try {
			while (true) {
				val cake = this.table.take()
				Thread.sleep(random.nextInt(1000).toLong())
			}
		} catch (e: InterruptedException) {
		}
	}
}

class Table(count: Int) {
	private val buffer: Array<String?>
	private var tail: Int
	private var head: Int
	private var count: Int
	
	init {
		this.buffer = arrayOfNulls(count)
		this.head = 0
		this.tail = 0
		this.count = 0
	}
	
	@Synchronized
	fun put(cake: String) {
		println("${Thread.currentThread().getName()} puts $cake")
		while (this.count >= this.buffer.size) {
			(this as java.lang.Object).wait()
		}
		this.buffer[this.tail] = cake
		this.tail = (this.tail + 1) % this.buffer.size
		this.count++
		(this as java.lang.Object).notifyAll()
	}
	
	@Synchronized
	fun take(): String? {
		while (this.count <= 0) {
			(this as java.lang.Object).wait()
		}
		val cake = this.buffer[this.head]
		this.head = (this.head + 1) % this.buffer.size
		this.count--
		(this as java.lang.Object).notifyAll()
		println("${Thread.currentThread().getName()} takes $cake")
		return cake
	}
}