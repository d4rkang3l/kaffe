/*
 * Java core library component.
 *
 * A shared pipe like UNIX..
 * Implements a circular buffer in this class, used by PipedOutputStream
 * to store
 *
 * Copyright (c) 1997, 1998
 *      Transvirtual Technologies, Inc.  All rights reserved.
 *
 * See the file "license.terms" for information on usage and redistribution
 * of this file.
 */

package java.io;

public class PipedInputStream
  extends InputStream
{
	PipedOutputStream src = null;
	final protected static int PIPE_SIZE = 512;
	protected byte[] buffer = new byte[PIPE_SIZE];
	protected int out = 0;
	protected int in = 0;
	private boolean closed = true;
        private boolean finished = true;

public PipedInputStream () {
}

/* Public interface */
public PipedInputStream (PipedOutputStream src) throws IOException {
	connect(src);
}

public int available() throws IOException {
	return(in >= out ? in-out : PIPE_SIZE+out-in);
}
    
public void close() throws IOException {
	out = 0;
	in = 0;
	closed = true;
}

public void connect(PipedOutputStream src) throws IOException {
	if (this.src != null) {
		throw new IOException("already connected");
	}
	this.src = src;
	if (src.sink == null) {
		src.connect(this);
	}
	closed = false;
	finished = false;
}

public synchronized int read() throws IOException {

	if (closed) {
		throw new IOException("stream closed");
	}

	if (finished && (out == in)) {
	        return (-1);
	}

	while (out == in) {
		try {
			this.wait();
		}
		catch (InterruptedException e) {
		}
		if (finished && (out == in)) {
		        return (-1);
		}
	}

	/* Should be Ok now */
	byte result = buffer[out];
	out = (out + 1) % PIPE_SIZE;

	this.notifyAll();

	return ((int)result) & 0xFF;
}

public synchronized int read(byte b[], int off, int len) throws IOException {
	return super.read(b, off, len);
}

protected synchronized void receive(int b) throws IOException {
	while (out == (in+1) % PIPE_SIZE) {
		try {
			this.wait();
		}
		catch (InterruptedException e) {}
	}
	buffer[in] = (byte)b;
	in = (in + 1) % PIPE_SIZE;

	this.notifyAll();
}

//Used in java.io.PipedOutputStream
void receivedLast() {
    	finished = true;
}
}
