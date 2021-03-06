/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Flexoutils, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.search;

import java.util.Vector;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class TextQueryResult {

	public class Result {
		public int startOffset;
		public int endOffset;

		public Result(int startOffset, int endOffset) {
			this.startOffset = startOffset;
			this.endOffset = endOffset;
		}

		public int getLength() {
			return endOffset - startOffset;
		}

		@Override
		public String toString() {
			return "Start: " + startOffset + " End: " + endOffset;
		}
	}

	public enum Direction {
		FORWARD, BACKWARD
	}

	public class ResultNavigator {
		private int currentIndex = -1;
		private Direction direction = Direction.FORWARD;
		private boolean wrapSearch = true;
		private boolean limitToSelectedText = false;
		private int selectionStart;
		private int selectionEnd;

		public ResultNavigator() {
		}

		public Result getCurrentResult() {
			Vector<Result> res = getFilteredResults();
			if (currentIndex > -1 && currentIndex < res.size()) {
				return res.get(currentIndex);
			}
			else {
				return null;
			}
		}

		public void replaceBy(String replacement) {
			if (document == null) {
				throw new IllegalStateException("No document provided! Cannot edit the text");
			}
			Result currentResult = getCurrentResult();
			if (currentResult == null) {
				throw new IllegalStateException("No text is selected!");
			}
			try {
				document.remove(currentResult.startOffset, currentResult.getLength());
				document.insertString(currentResult.startOffset, replacement, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		public void replaceAllBy(String replacement) {
			Vector<Result> results = getFilteredResults();
			for (int i = results.size(); i > 0; i--) {
				setCurrentIndex(i - 1);
				replaceBy(replacement);
			}
		}

		public Result getNextResult() throws EndOfDocumentHasBeenReachedException, ResultNotFoundException {

			Vector<Result> filteredResults = getFilteredResults();
			if (filteredResults.size() == 0) {
				throw new ResultNotFoundException();
			}

			switch (direction) {
				case FORWARD:
					if (currentIndex + 1 == filteredResults.size()) {
						if (wrapSearch) {
							currentIndex = 0;
						}
						else {
							throw new EndOfDocumentHasBeenReachedException();
						}
					}
					else {
						currentIndex++;
					}
					return filteredResults.get(currentIndex);
				case BACKWARD:
					if (currentIndex - 1 < 0) {
						if (wrapSearch) {
							currentIndex = filteredResults.size() - 1;
						}
						else {
							throw new EndOfDocumentHasBeenReachedException();
						}
					}
					else {
						currentIndex--;
					}
					return filteredResults.get(currentIndex);
				default:
					break;
			}
			return null;
		}

		private Vector<Result> getFilteredResults() {
			if (limitToSelectedText) {
				Vector<Result> r = new Vector<>();
				for (Result result : getResults()) {
					if (result.startOffset >= selectionStart && result.endOffset <= selectionEnd) {
						r.add(result);
					}
				}
				return r;
			}
			else {
				return getResults();
			}
		}

		public void setCurrentCaretPosition(int caretPosition) {
			if (caretPosition < 0 || caretPosition >= text.length()) {
				return;
			}
			int index = 0;
			for (Result res : getFilteredResults()) {
				if (res.startOffset >= caretPosition) {
					setCurrentIndex(index - 1);
					return;
				}
				index++;
			}
			setCurrentIndex(index - 1);
		}

		public int getCurrentIndex() {
			return currentIndex;
		}

		public void setCurrentIndex(int currentIndex) {
			this.currentIndex = currentIndex;
		}

		public Direction getDirection() {
			return direction;
		}

		public void setDirection(Direction direction) {
			this.direction = direction;
		}

		public boolean isWrapSearch() {
			return wrapSearch;
		}

		public void setWrapSearch(boolean wrapSearch) {
			this.wrapSearch = wrapSearch;
		}

		public boolean isLimitToSelectedText() {
			return limitToSelectedText;
		}

		public void setLimitToSelectedText(boolean limitToSelectedText) {
			if (this.limitToSelectedText != limitToSelectedText) {
				currentIndex = -1;
			}
			this.limitToSelectedText = limitToSelectedText;
		}

		public int getSelectionStart() {
			return selectionStart;
		}

		public void setSelectionStart(int selectionStart) {
			this.selectionStart = selectionStart;
		}

		public int getSelectionEnd() {
			return selectionEnd;
		}

		public void setSelectionEnd(int selectionEnd) {
			this.selectionEnd = selectionEnd;
		}
	}

	private TextQuery query;

	private Vector<Result> results;

	String text;

	protected Document document;

	public TextQueryResult(TextQuery query) {
		this.query = query;
		this.results = new Vector<>();
	}

	public TextQueryResult(TextQuery query, String text) {
		this.query = query;
		this.text = text;
		this.results = new Vector<>();
	}

	public TextQueryResult(TextQuery query, Document document) {
		this.query = query;
		this.document = document;
		try {
			this.text = document.getText(0, document.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();// Should never happen
		}
		this.results = new Vector<>();
	}

	public void addToResults(Result res) {
		results.add(res);
	}

	public TextQuery getQuery() {
		return query;
	}

	public Vector<Result> getResults() {
		return results;
	}

}
