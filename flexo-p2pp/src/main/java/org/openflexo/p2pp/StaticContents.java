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

import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.toolbox.StringUtils;

/**
 * A static contents (a keyword for example)
 * 
 * @author sylvain
 *
 * @param <T>
 */
public class StaticContents extends PrettyPrintableContents {

	private final String staticContents;
	private P2PPNode<?, ?> node;

	public StaticContents(String staticContents, RawSourceFragment fragment, P2PPNode<?, ?> node) {
		super();
		this.staticContents = staticContents;
		this.node = node;
	}

	public StaticContents(String prelude, String staticContents, RawSourceFragment fragment, P2PPNode<?, ?> node) {
		super(prelude, null);
		this.staticContents = staticContents;
		this.node = node;
	}

	public StaticContents(String prelude, String staticContents, String postlude, RawSourceFragment fragment, P2PPNode<?, ?> node) {
		super(prelude, postlude);
		this.staticContents = staticContents;
		this.node = node;
	}

	public String getStaticContents() {
		return staticContents;
	}

	@Override
	public String getNormalizedPrettyPrint(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isNotEmpty(getPrelude())) {
			sb.append(getPrelude());
		}
		if (StringUtils.isNotEmpty(getStaticContents())) {
			sb.append(getStaticContents());
		}
		if (StringUtils.isNotEmpty(getPostlude())) {
			sb.append(getPostlude());
		}
		return sb.toString();
	}

	@Override
	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {
		if (StringUtils.isNotEmpty(getPrelude()))
			preludeFragment = findUnmappedSegmentBackwardFrom(getPrelude(), getFragment().getStartPosition());
		if (StringUtils.isNotEmpty(getPostlude()))
			postludeFragment = findUnmappedSegmentForwardFrom(getPostlude(), getFragment().getEndPosition());

		/*if (preludeFragment != null && StringUtils.isNotEmpty(getPrelude())) {
			derivedRawSource.replace(preludeFragment, getPrelude());
		}*/

		String replacedString = getStaticContents();

		if (StringUtils.isNotEmpty(getPrelude())) {
			if (preludeFragment == null) {
				replacedString = getPrelude() + replacedString;
			}
		}
		if (StringUtils.isNotEmpty(getPostlude())) {
			if (postludeFragment == null) {
				replacedString = replacedString + getPostlude();
			}
		}

		/*if (staticContents.equals("concept")) {
			System.out.println("Prelude: [" + getPrelude() + "]");
			System.out.println("Postlude: [" + getPostlude() + "]");
			System.out.println("Found prelude: " + preludeFragment);
			System.out.println("Found postlude: " + postludeFragment);
			System.out.println("replacedString=[" + replacedString + "]");
		}*/

		derivedRawSource.replace(getFragment(), replacedString);

	}

	/*@Override
	public void setFragment(RawSourceFragment fragment) {
		if (staticContents.equals("abstract")) {
			System.out.println("*********** tiens on me donne le fragment " + fragment);
		}
		super.setFragment(fragment);
	}*/

	private RawSourceFragment preludeFragment;
	private RawSourceFragment postludeFragment;

	@Override
	public void initializePrettyPrint(P2PPNode<?, ?> rootNode, PrettyPrintContext context) {
		// Nothing to do
		// System.out.println("*********** tiens j'arrive la pour " + getStaticContents() + " fragment " + getFragment());
		// System.out.println("rootNode:" + rootNode);
		/*if (StringUtils.isNotEmpty(getPrelude()))
			preludeFragment = findUnmappedSegmentBackwardFrom(getPrelude(), getFragment().getStartPosition(), rootNode);
		if (StringUtils.isNotEmpty(getPostlude()))
			postludeFragment = findUnmappedSegmentForwardFrom(getPostlude(), getFragment().getEndPosition(), rootNode);
		System.out.println("Prelude: [" + getPrelude() + "]");
		System.out.println("Postlude: [" + getPostlude() + "]");
		System.out.println("Found prelude: " + preludeFragment);
		System.out.println("Found postlude: " + postludeFragment);*/

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
		System.out.println("Forward looking for [" + expected + "] from " + position + " in fragment " + node.getLastParsedFragment());
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
