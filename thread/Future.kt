package thread.future

import java.util.concurrent.locks.ReentrantLock

fun main(args: Array<String>) {
	println("main BEGIN")
	
	val host = Host()
	
	val data1 = host.request(10, 'A')
	val data2 = host.request(20, 'B')
	val data3 = host.request(30, 'C')
	
	println("main otherJob BEGIN")
	
	try {
		Thread.sleep(2000)
	} catch (e: InterruptedException) {
	}
	
	println("main otherJob END")
	
	println("data1 = ${data1.getContent()}")
	println("data2 = ${data2.getContent()}")
	println("data3 = ${data3.getContent()}")
	println("main END")
}

class Host {
	fun request(count: Int, c: Char): Data {
		println("    request($count, $c) BEGIN")
		
		val future = FutureData()
		
		object : Thread() {
			override fun run() {
				val realdata = RealData(count, c)
				future.setRealData(realdata)
			}
		}.start()
		
		println("    request($count, $c) END")
		
		return future
	}
}

interface Data {
	fun getContent(): String?
}

class FutureData : Data {
	private var realdata: RealData? = null
	private var ready = false
	private val lock = java.lang.Object()
	
	fun setRealData(realdata: RealData) {
		synchronized(lock) {
			if (this.ready) {
				return
			}
			this.realdata = realdata
			this.ready = true
			this.lock.notifyAll()
		}
	}
	
	override fun getContent(): String? {
		synchronized(lock) {
			while (!this.ready) {
			try {
				this.lock.wait()
			} catch (e: InterruptedException) {
			}
		}
		return this.realdata?.getContent()
		}
	}
}

class RealData(count: Int, c: Char) : Data {
	private val content: String
	
	init {
		println("        making RealData($count, $c) BEGIN")
		var buffer = CharArray(count)
		for (i in 0 until count) {
			buffer[i] = c
			try {
				Thread.sleep(100)
			} catch (e: InterruptedException) {
			}
		}
		println("        making RealData($count, $c) END")
		this.content = String(buffer)
	}
	
	override fun getContent(): String {
		return this.content
	}
}