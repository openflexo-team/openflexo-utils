/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

package org.openflexo.p2pp;

import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.toolbox.StringUtils;

/**
 * Specification of a part of pretty-print for a pretty-printable object
 * 
 * @author sylvain
 *
 * @param <N>
 *            Type of AST node
 * @param <T>
 *            General type of pretty-printable object
 */
public abstract class PrettyPrintableContents<N, T> {

	private final P2PPNode<N, T> node;

	private final String prelude;
	private final String postlude;
	private Indentation indentation;

	private RawSourceFragment fragment = null;

	public PrettyPrintableContents(P2PPNode<N, T> node, String prelude, String postlude, Indentation indentation) {
		super();
		this.node = node;
		this.prelude = prelude;
		this.postlude = postlude;
		this.indentation = indentation;
	}

	public PrettyPrintableContents(P2PPNode<N, T> node, String prelude, String postlude) {
		super();
		this.node = node;
		this.prelude = prelude;
		this.postlude = postlude;
	}

	public PrettyPrintableContents(P2PPNode<N, T> node, Indentation indentation) {
		super();
		this.node = node;
		this.prelude = null;
		this.postlude = null;
		this.indentation = indentation;
	}

	public PrettyPrintableContents(P2PPNode<N, T> node) {
		super();
		this.node = node;
		this.prelude = null;
		this.postlude = null;
	}

	/**
	 * Return {@link P2PPNode} where this {@link PrettyPrintableContents} was defined as contents of {@link P2PPNode}
	 * 
	 * @return
	 */
	public P2PPNode<N, T> getNode() {
		return node;
	}

	public String getPrelude() {
		return prelude;
	}

	public String getPostlude() {
		return postlude;
	}

	public Indentation getIndentation() {
		return indentation;
	}

	public PrettyPrintableContents<N, T> indent() {
		indentation = Indentation.Indent;
		return this;
	}

	public RawSourceFragment getFragment() {
		return fragment;
	}

	/**
	 * Called when a {@link DynamicContents} is registered to the place determined with supplied fragment
	 * 
	 * @param fragment
	 */
	public void setFragment(RawSourceFragment fragment) {
		this.fragment = fragment;
	}

	public abstract String getNormalizedPrettyPrint(PrettyPrintContext context);

	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {
		preludeFragment = null;
		postludeFragment = null;
	}

	public abstract void initializePrettyPrint(P2PPNode<?, ?> rootNode, PrettyPrintContext context);

	private RawSourceFragment preludeFragment;
	private RawSourceFragment postludeFragment;

	public RawSourceFragment getPreludeFragment() {
		if (preludeFragment == null && getFragment() != null && StringUtils.isNotEmpty(getPrelude())) {
			preludeFragment = findUnmappedSegmentBackwardFrom(getPrelude(), getFragment().getStartPosition());
		}
		return preludeFragment;
	}

	public RawSourceFragment getPostludeFragment() {
		if (postludeFragment == null && getFragment() != null && StringUtils.isNotEmpty(getPostlude())) {
			postludeFragment = findUnmappedSegmentForwardFrom(getPostlude(), getFragment().getEndPosition());
		}
		return postludeFragment;
	}

	private RawSourceFragment findUnmappedSegmentBackwardFrom(String expected, RawSourcePosition position) {
		int length = expected.length();
		int i = 0;
		// System.out.println("Backward looking for [" + expected + "] from " + position);
		boolean positionStillValid = true;
		while (positionStillValid) {
			try {
				RawSourcePosition start = position.decrement(length + i);
				RawSourcePosition end = position.decrement(i);
				RawSourceFragment f = position.getOuterType().makeFragment(start, end);
				// System.out.println("Test backward fragment " + f + " [" + f.getRawText() + "]");
				if (node.isFragmentMappedInPPContents(f)) {
					// This fragment intersects another mapped fragment, abort
					return null;
				}
				if (f.getRawText().equals(expected)) {
					return f;
				}
				i++;
				if (start.decrement().isBefore(node.getStartPosition())) {
					positionStillValid = false;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				positionStillValid = false;
			}
		}
		return null;
	}

	private RawSourceFragment findUnmappedSegmentForwardFrom(String expected, RawSourcePosition position) {
		int length = expected.length();
		int i = 0;
		// System.out.println("Forward looking for [" + expected + "] from " + position + " in fragment " + node.getLastParsedFragment());
		boolean positionStillValid = true;
		while (positionStillValid) {
			try {
				RawSourcePosition start = position.increment(i);
				RawSourcePosition end = position.increment(length + i);
				RawSourceFragment f = position.getOuterType().makeFragment(start, end);
				if (node.isFragmentMappedInPPContents(f)) {
					// This fragment intersects another mapped fragment, abort
					return null;
				}
				if (f.getRawText().equals(expected)) {
					return f;
				}
				i++;
				if (end.increment().isAfter(node.getEndPosition())) {
					positionStillValid = false;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				positionStillValid = false;
			}
		}
		return null;
	}

}
