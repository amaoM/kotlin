package thread

fun main(args: Array<String>) {
	println("main BEGIN")
	
	val host = MyHost()
	
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

class MyHost {
	fun request(count: Int, c: Char): IData {
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

interface IData {
	fun getContent(): String?
}

class FutureData : IData {
	private var realdata: RealData? = null
	private var ready = false
	
	@Synchronized
	fun setRealData(realdata: RealData) {
		if (this.ready) {
			return
		}
		this.realdata = realdata
		this.ready = true
		(this as java.lang.Object).notifyAll()
	}
	
	@Synchronized
	override fun getContent(): String? {
		while (!this.ready) {
			try {
				(this as java.lang.Object).wait()
			} catch (e: InterruptedException) {
			}
		}
		return this.realdata?.getContent()
	}
}

class RealData(count: Int, c: Char) : IData {
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