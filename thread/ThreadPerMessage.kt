package thread.thread.per.message

fun main(args: Array<String>) {
	println("main BEGIN")
	val host = Host()
	host.request(10, 'A')
	host.request(20, 'B')
	host.request(30, 'C')
	println("main END")
}

class Host {
	private val helper = Helper()
	
	fun request(count: Int, c: Char) {
		println("    request($count, $c) BEGIN")
		object : Thread() {
			override fun run() {
				helper.handle(count, c)
			}
		}.start()
		println("    request($count, $c) END")
	}
}

class Helper() {
	fun handle(count: Int, c: Char) {
		println("        handle($count, $c) BEGIN")
		for (i in 0..count - 1) {
			slowly()
			print(c)
		}
		println("")
		println("        handle($count, $c) END")
	}
	
	private fun slowly() {
		try {
			Thread.sleep(100)
		} catch (e: InterruptedException) {
		}
	}
}