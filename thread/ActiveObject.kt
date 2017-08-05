package thread.active.objectimport

import kotlin.jvm.Synchronized

fun main(arts: Array<String>) {
	val activeObject = ActiveObjectFactory.createActiveObject()
	MakerClientThread("Alice", activeObject).start()
	MakerClientThread("Bobby", activeObject).start()
	DisplayClientThread("Chris", activeObject).start()
}

class MakerClientThread(name: String, activeObject: ActiveObject) : Thread(name) {
	private val activeObject: ActiveObject
	private val fillchar: Char
	
	init {
		this.activeObject = activeObject
		this.fillchar = name.elementAt(0)
	}
	
	override fun run() {
		try {
			for (i in 0 until 1000) {
				val result = activeObject.makeString(i, this.fillchar)
				Thread.sleep(10)
				val value = result.getResultValue()
				println("${Thread.currentThread().getName()}: value = $value")
			}
		} catch (e: InterruptedException) {
		}
	}
}

class DisplayClientThread(name: String, activeObject: ActiveObject) : Thread(name) {
	private val activeObject: ActiveObject
	
	init {
		this.activeObject = activeObject
	}
	
	override fun run() {
		try {
			for (i in 0 until 1000) {
				val string = "${Thread.currentThread().getName()} $i"
				activeObject.displayString(string)
				Thread.sleep(200)
			}
		} catch (e: InterruptedException) {
		}
	}
}

interface ActiveObject {
	fun makeString(count: Int, fillchar: Char): Result<String>
	fun displayString(string: String)
}

class ActiveObjectFactory {
	companion object {
		fun createActiveObject(): ActiveObject {
			val servant = Servant()
			val queue = ActivationQueue()
			val scheduler = SchedulerThread(queue)
			val proxy = Proxy(scheduler, servant)
			scheduler.start()
			return proxy
		}
	}
}

class Proxy(scheduler: SchedulerThread, servant: Servant) : ActiveObject {
	private val scheduler: SchedulerThread
	private val servant: Servant
	
	init {
		this.scheduler = scheduler
		this.servant = servant
	}
	
	override fun makeString(count: Int, fillchar: Char): Result<String> {
		val future = FutureResult<String>()
		scheduler.invoke(MakeStringRequest(servant, future, count, fillchar))
		return future
	}
	
	override fun displayString(string: String) {
		scheduler.invoke(DisplayStringRequest(this.servant, string))
	}
}

class SchedulerThread(queue: ActivationQueue) : Thread() {
	private val queue: ActivationQueue
	
	init {
		this.queue = queue
	}
	
	fun invoke(request: MethodRequest<String>) {
		queue.putRequest(request)
	}
	
	override fun run() {
		while (true) {
			val request = queue.takeRequest()
			request.execute()
		}
	}
}

class ActivationQueue() {
	companion object {
		private val MAX_METHOD_REQUEST = 100
	}
	
	private var requestQueue = ArrayList<MethodRequest<String>>()
	private var tail: Int
	private var head: Int
	private var count: Int
	private val lock = java.lang.Object()
	
	init {
		this.head = 0
		this.tail = 0
		this.count = 0
	}
	
	fun putRequest(request: MethodRequest<String>) {
		synchronized(lock) {
			while (count >= MAX_METHOD_REQUEST) {
				try {
					lock.wait()
				} catch (e: InterruptedException) {
				}
			}
			this.requestQueue.add(request)
			this.tail = (this.tail + 1) % MAX_METHOD_REQUEST
			this.count++
			lock.notifyAll()
		}
	}
	
	fun takeRequest(): MethodRequest<String> {
		synchronized(lock) {
			while (this.count <= 0) {
				try {
					lock.wait()
				} catch (e: InterruptedException) {
				}
			}
			val request = this.requestQueue[this.head]
			this.head = (this.head + 1) % MAX_METHOD_REQUEST
			this.count--
			lock.notifyAll()
			return request
		}
	}
}

abstract class MethodRequest<T>(servant: Servant, future: FutureResult<T>?) {
	protected val servant: Servant
	protected val future: FutureResult<T>?
	
	init {
		this.servant = servant
		this.future = future
	}
	
	abstract fun execute()
}

class MakeStringRequest(
		servant: Servant,
		future: FutureResult<String>,
		count: Int,
		fillchar: Char
) : MethodRequest<String>(servant, future) {
	private var count: Int
	private val fillchar: Char
	
	init {
		this.count = count
		this.fillchar = fillchar
	}
	
	override fun execute() {
		val result = servant.makeString(this.count, this.fillchar)
		this.future?.setResult(result)
	}
}

class DisplayStringRequest(servant: Servant, string: String) : MethodRequest<String>(servant, null) {
	private val string: String
	
	init {
		this.string = string
	}
	
	override fun execute() {
		servant.displayString(string)
	}
}

abstract class Result<T> {
	abstract fun getResultValue(): T?
}

class FutureResult<T> : Result<T>() {
	private var result: Result<T>? = null
	private var ready = false
	private val lock = java.lang.Object()
	
	fun setResult(result: Result<T>) {
		synchronized(lock) {
			this.result = result
			this.ready = true
			lock.notifyAll()
		}
	}
	
	override fun getResultValue(): T? {
		synchronized(lock) {
			while (!this.ready) {
				try {
					lock.wait()
				} catch (e: InterruptedException) {
				}
			}
			return this.result?.getResultValue()
		}
	}
}

class RealResult<T>(resultValue: T) : Result<T>() {
	private var resultValue: T
	
	init {
		this.resultValue = resultValue
	}
	
	override fun getResultValue(): T {
		return this.resultValue
	}
}

class Servant() : ActiveObject {
	override fun makeString(count: Int, fillchar: Char): Result<String>{
		val buffer = CharArray(count)
		for (i in 0 until count) {
			buffer[i] = fillchar
			try {
				Thread.sleep(100)
			} catch (e: InterruptedException) {
			}
		}
		return RealResult<String>(String(buffer))
	}
	
	override fun displayString(string: String) {
		try {
			println("displayString: $string")
			Thread.sleep(10)
		} catch (e: InterruptedException) {
		}
	}
}