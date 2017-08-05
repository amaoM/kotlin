package thread

import java.io.FileWriter
import java.io.IOException
import java.util.Random

fun main(args: Array<String>) {
	val data = Data("data.txt", "empty")
	ChangerThread("ChangerThread", data).start()
	SaverThread("SaverThread", data).start()
}

class Data(filename: String, content: String) {
	private val filename: String
	private var content: String
	private var changed: Boolean
	
	init {
		this.filename = filename
		this.content = content
		this.changed = true
	}
	
	@Synchronized
	fun change(newContent: String) {
		this.content = newContent
		this.changed = true
	}
	
	@Synchronized
	fun save() {
		if (!this.changed) {
			return
		}
		doSave()
		this.changed = false
	}
	
	private fun doSave() {
		println("${Thread.currentThread().getName()} calls doSave, content ${this.content}")
		val writer = FileWriter(this.filename)
		writer.write(this.content)
		writer.close()
	}
}

class SaverThread(name: String, data: Data) : Thread(name) {
	private val data: Data
	
	init {
		this.data = data
	}
	
	override fun run() {
		try {
			while (true) {
				data.save()
				Thread.sleep(1000)
			}
		} catch (e: IOException) {
			e.printStackTrace()
		} catch (e: InterruptedException) {
			e.printStackTrace()
		}
	}
}

class ChangerThread(name: String, data: Data) : Thread(name) {
	private val data: Data
	private val random = Random()
	
	init {
		this.data = data
	}
	
	override fun run() {
		try {
			for (i in 0..1000) {
				data.change("No.$i")
				Thread.sleep(random.nextInt(1000).toLong())
				data.save()
			}
		} catch (e: IOException) {
			e.printStackTrace()
		} catch (e: InterruptedException) {
			e.printStackTrace()
		}
	}
}