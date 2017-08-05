package thread.thread.specific.storage

import java.io.File
import java.io.PrintWriter
import java.io.IOException

fun main(args: Array<String>) {
	ClientThread("Alice").start()
	ClientThread("Bobby").start()
	ClientThread("Chris").start()
}

class TSLog(filename: String) {
	private var writer: PrintWriter? = null
	
	init {
		try {
			this.writer = File(filename).printWriter()
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}
	
	fun println(s: String) {
		this.writer?.println(s)
	}
	
	fun close() {
		this.writer?.println("==== End of log ====")
		writer?.close()
	}
}

class Log {
	companion object {
		private val tsLogCollection = ThreadLocal<TSLog>()
		
		fun println(s: String) {
			getTSLog().println(s)
		}
		
		fun close() {
			getTSLog().close()
		}
		
		private fun getTSLog(): TSLog {
			var tsLog = tsLogCollection.get()
			
			if (tsLog == null) {
				tsLog = TSLog("${Thread.currentThread().getName()}-log.txt")
				tsLogCollection.set(tsLog)
			}
			
			return tsLog
		}
	}
}

class ClientThread(name: String) : Thread(name) {
	
	override fun run() {
		println("${getName()} BEGIN")
		for (i in 0 until 10) {
			Log.println("i = $i")
			try {
				Thread.sleep(100)
			} catch (e: InterruptedException) {
			}
		}
		Log.close()
		println("${getName()} END")
	}
}