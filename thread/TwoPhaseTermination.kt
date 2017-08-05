package thread

fun main(args: Array<String>) {
	println("main: BEGIN")
	try {
		val t = CountupThread()
		t.start()
		
		Thread.sleep(10000)
		
		println("main: shutdownRequest")
		t.shutdownRequest()
		
		println("main: join")
		
		t.join()
	} catch (e: InterruptedException) {
		e.printStackTrace()
	}
	println("main: END")
}

class CountupThread : Thread() {
	private var counter: Long = 0
	
	@Volatile private var shutdownRequested = false
	
	fun shutdownRequest() {
		this.shutdownRequested = true
		interrupt()
	}
	
	override fun run() {
		try {
			while (!this.shutdownRequested) {
				doWork()
			}
		} catch (e: InterruptedException) {
		} finally {
			doShutdown()
		}
	}
	
	private fun doWork() {
		this.counter++
		println("doWork: counter = ${this.counter}")
		Thread.sleep(500)
	}
	
	private fun doShutdown() {
		println("doShutdown: counter = ${this.counter}")
	}
	
}