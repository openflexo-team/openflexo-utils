package org.openflexo.toolbox;

import java.io.IOException;
import java.io.Reader;

/**
 * A filter that escape Java comments (slashed-star comments) in a given input {@link Reader}
 * 
 * @author sylvain
 *
 */
public final class StripJavaComments extends BaseFilterReader {

	/**
	 * The read-ahead character, used for effectively pushing a single character back. A value of -1 indicates that no character is in the
	 * buffer.
	 */
	private int readAheadCh = -1;

	/**
	 * Whether or not the parser is currently in the middle of a string literal.
	 */
	private boolean inString = false;

	/**
	 * Whether or not the last char has been a backslash.
	 */
	private boolean quoted = false;

	/**
	 * Creates a new filtered reader.
	 *
	 * @param in
	 *            A Reader object providing the underlying stream. Must not be <code>null</code>.
	 */
	public StripJavaComments(final Reader in) {
		super(in);
	}

	/**
	 * Returns the next character in the filtered stream, not including Java comments.
	 *
	 * @return the next character in the resulting stream, or -1 if the end of the resulting stream has been reached
	 *
	 * @exception IOException
	 *                if the underlying stream throws an IOException during reading
	 */
	@Override
	public final int read() throws IOException {
		int ch = -1;
		if (readAheadCh != -1) {
			ch = readAheadCh;
			readAheadCh = -1;
		}
		else {
			ch = in.read();
			System.out.println("ch: " + ch);
			if (ch == '"' && !quoted) {
				inString = !inString;
				quoted = false;
			}
			else if (ch == '\\') {
				quoted = !quoted;
			}
			else {
				quoted = false;
				if (!inString) {
					if (ch == '/') {
						ch = in.read();
						if (ch == '/') {
							while (ch != '\n' && ch != -1 && ch != '\r') {
								ch = in.read();
							}
						}
						else if (ch == '*') {
							while (ch != -1) {
								ch = in.read();
								if (ch == '*') {
									ch = in.read();
									while (ch == '*' && ch != -1) {
										ch = in.read();
									}

									if (ch == '/') {
										ch = read();
										break;
									}
								}
							}
						}
						else {
							readAheadCh = ch;
							ch = '/';
						}
					}
				}
			}
		}

		return ch;
	}

}
