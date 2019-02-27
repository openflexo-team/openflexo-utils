/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of flexo-p2pp, a component of the software infrastructure 
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.toolbox.StringUtils;

/**
 * Parser/PrettyPrinter node<br>
 * 
 * Maintains consistency between the model (a pretty-printable object) and source code represented in a textual language
 * 
 * Manage normalized textual representation
 * 
 * Manage a String representing textual representation as the merge of:
 * <ul>
 * <li>Last parsed version (as it was in the original source file)</li>
 * <li>Eventual model modifications</li>
 * </ul>
 * 
 * @author sylvain
 * 
 * @param <N>
 *            Type of AST node
 * @param <T>
 *            General type of pretty-printable object
 */
public abstract class P2PPNode<N, T> {

	private static final Logger logger = Logger.getLogger(P2PPNode.class.getPackage().getName());

	private N astNode;
	protected T modelObject;

	private P2PPNode<?, ?> parent;
	private List<P2PPNode<?, ?>> children = new ArrayList<>();

	protected RawSourcePosition startPosition;
	protected RawSourcePosition endPosition;
	protected RawSourceFragment parsedFragment;
	protected RawSourceFragment prelude;
	protected RawSourceFragment postlude;

	private List<PrettyPrintableContents> ppContents = new ArrayList<>();

	public static final String SPACE = " ";
	public static final String LINE_SEPARATOR = "\n";

	public P2PPNode(T aModelObject, N astNode, FragmentRetriever<?> fragmentRetriever) {
		this.astNode = astNode;
		this.modelObject = aModelObject;

		if (astNode != null) {
			RawSourceFragment fragment = ((FragmentRetriever) fragmentRetriever).retrieveFragment(astNode);
			setStartPosition(fragment.getStartPosition());
			setEndPosition(fragment.getEndPosition());
		}
	}

	protected void addToChildren(P2PPNode<?, ?> child, int index) {
		child.parent = this;
		children.add(index, child);
	}

	protected void addToChildren(P2PPNode<?, ?> child) {
		child.parent = this;
		children.add(child);
	}

	public N getASTNode() {
		return astNode;
	}

	public P2PPNode<?, ?> getParent() {
		return parent;
	}

	public List<P2PPNode<?, ?>> getChildren() {
		return children;
	}

	public abstract T buildModelObjectFromAST(N astNode);

	public abstract P2PPNode<N, T> deserialize();

	public final void finalizeDeserialization() {
		// Override when required
	}

	/**
	 * Return underlying model object: object being represented by referenced AST node through this node
	 * 
	 * @return
	 */
	public T getModelObject() {
		return modelObject;
	}

	/**
	 * Return original version of last serialized raw source, FOR THE ENTIRE compilation unit
	 * 
	 * @return
	 */
	public abstract RawSource getRawSource();

	/**
	 * Return starting position of RawSource, where underlying model object is textually serialized, inclusive
	 * 
	 * @return
	 */
	public RawSourcePosition getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(RawSourcePosition startPosition) {
		this.startPosition = startPosition;
		parsedFragment = null;
	}

	/**
	 * Return end position of RawSource, where underlying model object is textually serialized, inclusive
	 * 
	 * @return
	 */
	public RawSourcePosition getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(RawSourcePosition endPosition) {
		this.endPosition = endPosition;
		parsedFragment = null;
	}

	/**
	 * Return fragment representing underlying model object as a String in textual language, as it was last parsed
	 * 
	 * @return
	 */
	public RawSourceFragment getLastParsedFragment() {
		if (parsedFragment == null && getStartPosition() != null && getEndPosition() != null) {
			parsedFragment = getRawSource().makeFragment(getStartPosition(), getEndPosition());
		}
		return parsedFragment;
	}

	public RawSourceFragment getPrelude() {
		return prelude;
	}

	public RawSourceFragment getPostlude() {
		return postlude;
	}

	/**
	 * Build and return a new pretty-print context
	 * 
	 * @return
	 */
	public PrettyPrintContext makePrettyPrintContext() {
		return new DefaultPrettyPrintContext(0);
	}

	public final void initializePrettyPrint(P2PPNode<?, ?> rootNode, PrettyPrintContext context) {
		preparePrettyPrint(getASTNode() != null);
		// System.out.println("On regarde si pour ce noeud " + this + " il faudrait pas etendre le fragment " + getLastParsedFragment());

		for (PrettyPrintableContents prettyPrintableContents : ppContents) {
			prettyPrintableContents.initializePrettyPrint(rootNode, context.derive(prettyPrintableContents.getRelativeIndentation()));
		}

	}

	protected void preparePrettyPrint(boolean hasParsedVersion) {
		defaultInsertionPoint = getStartPosition();
	}

	// protected abstract void prepareNormalizedPrettyPrint();

	public final String getNormalizedTextualRepresentation(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();
		for (PrettyPrintableContents child : ppContents) {
			String normalizedPP = child.getNormalizedPrettyPrint(context);
			if (normalizedPP != null) {
				sb.append(normalizedPP);
			}
		}
		// System.out.println("On indente pour indentation=[" + context.getResultingIndentation() + "]");
		// System.out.println("Ce qu'on indente: " + sb.toString());
		// System.out.println("On retourne: " + context.indent(sb.toString()));
		return context.indent(sb.toString());
	}

	private RawSourcePosition defaultInsertionPoint;

	public RawSourcePosition getDefaultInsertionPoint() {
		return defaultInsertionPoint;
	}

	/**
	 * Append {@link StaticContents}, whose value is intended to replace text determined with supplied fragment
	 * 
	 * @param staticContents
	 *            value to append
	 * @param fragment
	 */
	public void appendStaticContents(String staticContents, RawSourceFragment fragment) {
		StaticContents newContents = new StaticContents(null, staticContents, null, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link StaticContents}, whose value is intended to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param staticContents
	 *            value to append
	 */
	public void appendStaticContents(String staticContents) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		StaticContents newContents = new StaticContents(null, staticContents, null, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link StaticContents}, whose value is intended to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param prelude
	 *            prelude to add if normalized pretty-print is to be applied
	 * @param staticContents
	 *            value to append
	 */
	public void appendStaticContents(String prelude, String staticContents) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		StaticContents newContents = new StaticContents(prelude, staticContents, null, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link StaticContents}, whose value is intended to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param prelude
	 *            prelude to add if normalized pretty-print is to be applied
	 * @param staticContents
	 *            value to append
	 * @param postlude
	 *            postlude to add if normalized pretty-print is to be applied
	 */
	public void appendStaticContents(String prelude, String staticContents, String postlude) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		StaticContents newContents = new StaticContents(prelude, staticContents, postlude, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link StaticContents}, whose value is intended to replace text determined with supplied fragment
	 * 
	 * @param prelude
	 *            prelude to add if normalized pretty-print is to be applied
	 * @param staticContents
	 *            value to append
	 * @param fragment
	 */
	public void appendStaticContents(String prelude, String staticContents, RawSourceFragment fragment) {
		StaticContents newContents = new StaticContents(prelude, staticContents, null, fragment);
		ppContents.add(newContents);
		if (fragment != null) {
			defaultInsertionPoint = fragment.getEndPosition();
		}
	}

	/**
	 * Append {@link StaticContents}, whose value is intended to replace text determined with supplied fragment
	 * 
	 * @param prelude
	 *            prelude to add if normalized pretty-print is to be applied
	 * @param staticContents
	 *            value to append
	 * @param postlude
	 *            postlude to add if normalized pretty-print is to be applied
	 * @param fragment
	 */
	public void appendStaticContents(String prelude, String staticContents, String postlude, RawSourceFragment fragment) {
		StaticContents newContents = new StaticContents(prelude, staticContents, postlude, fragment);
		ppContents.add(newContents);
		if (fragment != null) {
			defaultInsertionPoint = fragment.getEndPosition();
		}
	}

	/**
	 * Append {@link DynamicContents}, whose value is intended to replace text determined with supplied fragment
	 * 
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param fragment
	 */
	public void appendDynamicContents(Supplier<String> stringRepresentationSupplier, RawSourceFragment fragment) {
		DynamicContents newContents = new DynamicContents(null, stringRepresentationSupplier, null, fragment);
		ppContents.add(newContents);
		if (fragment != null) {
			defaultInsertionPoint = fragment.getEndPosition();
		}
	}

	/**
	 * Append {@link DynamicContents}, whose value is intended to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 */
	public void appendDynamicContents(Supplier<String> stringRepresentationSupplier) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		DynamicContents newContents = new DynamicContents(null, stringRepresentationSupplier, null, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link DynamicContents}, whose value is intended to replace text determined with supplied fragment
	 * 
	 * @param prelude
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param fragment
	 */
	public void appendDynamicContents(String prelude, Supplier<String> stringRepresentationSupplier, RawSourceFragment fragment) {
		DynamicContents newContents = new DynamicContents(prelude, stringRepresentationSupplier, null, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link DynamicContents}, whose value is intended to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param prelude
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 */
	public void addDynamicContents(String prelude, Supplier<String> stringRepresentationSupplier) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		DynamicContents newContents = new DynamicContents(prelude, stringRepresentationSupplier, null, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link DynamicContents}, whose value is intended to replace text determined with supplied fragment
	 * 
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param postlude
	 * @param fragment
	 */
	public void appendDynamicContents(Supplier<String> stringRepresentationSupplier, String postlude, RawSourceFragment fragment) {
		DynamicContents newContents = new DynamicContents(null, stringRepresentationSupplier, postlude, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link DynamicContents}, whose value is intended to be inserted at current location (no current contents was parsed in initial
	 * raw source)
	 * 
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param postlude
	 */
	public void appendDynamicContents(Supplier<String> stringRepresentationSupplier, String postlude) {
		RawSourceFragment insertionPointFragment = defaultInsertionPoint != null
				? defaultInsertionPoint.getOuterType().makeFragment(defaultInsertionPoint, defaultInsertionPoint) : null;
		DynamicContents newContents = new DynamicContents(null, stringRepresentationSupplier, postlude, insertionPointFragment);
		ppContents.add(newContents);
	}

	/**
	 * Append {@link ChildContents} managing pretty-print for supplied childObject<br>
	 * Either this object is already serialized, or should be created
	 * 
	 * @param prelude
	 * @param childObject
	 * @param postude
	 * @param relativeIndentation
	 *            <ul>
	 *            <li>When relativeIndentation is zero, keep current indentation</li>
	 *            <li>When relativeIndentation is positive, increment current indentation with that value</li>
	 *            <li>When relativeIndentation is negative (-1), discard current indentation</li>
	 *            </ul>
	 */
	protected <C> ChildContents<C> appendToChildPrettyPrintContents(String prelude, Supplier<C> childObjectSupplier, String postude,
			int relativeIndentation) {

		ChildContents<C> newChildContents = new ChildContents<>(prelude, childObjectSupplier, postude, relativeIndentation, this);
		ppContents.add(newChildContents);
		return newChildContents;
	}

	/**
	 * Called to indicate that supplied childObject must be serialized at this pretty-print level<br>
	 * Either this object is already serialized, or should be created
	 * 
	 * This method is used for example to render a vertical layout in pretty-printed text
	 * 
	 * @param prelude
	 * @param childrenObjects
	 * @param postude
	 * @param relativeIndentation
	 *            <ul>
	 *            <li>When relativeIndentation is zero, keep current indentation</li>
	 *            <li>When relativeIndentation is positive, increment current indentation with that value</li>
	 *            <li>When relativeIndentation is negative (-1), discard current indentation</li>
	 *            </ul>
	 * @param childrenType
	 * @return
	 */
	protected <C> ChildrenContents<C> appendToChildrenPrettyPrintContents(String prelude, Supplier<List<? extends C>> childrenObjects,
			String postude, int relativeIndentation, Class<C> childrenType) {

		ChildrenContents<C> newChildrenContents = new ChildrenContents<>(prelude, childrenObjects, postude, relativeIndentation, this,
				childrenType);
		ppContents.add(newChildrenContents);
		return newChildrenContents;
	}

	/**
	 * Called to indicate that supplied childObject must be serialized at this pretty-print level<br>
	 * Either this object is already serialized, or should be created
	 * 
	 * This method is used for example to render an horizontal layout in pretty-printed text (a comma-separated list for example)
	 * 
	 * @param preludeForFirstItem
	 * @param prelude
	 * @param childrenObjects
	 * @param postude
	 * @param postludeForLastItem
	 * @param relativeIndentation
	 *            <ul>
	 *            <li>When relativeIndentation is zero, keep current indentation</li>
	 *            <li>When relativeIndentation is positive, increment current indentation with that value</li>
	 *            <li>When relativeIndentation is negative (-1), discard current indentation</li>
	 *            </ul>
	 * @param childrenType
	 * @return
	 */
	protected <C> ChildrenContents<C> appendToChildrenPrettyPrintContents(String preludeForFirstItem, String prelude,
			Supplier<List<? extends C>> childrenObjects, String postude, String postludeForLastItem, int relativeIndentation,
			Class<C> childrenType) {

		ChildrenContents<C> newChildrenContents = new ChildrenContents<>(preludeForFirstItem, prelude, childrenObjects, postude,
				postludeForLastItem, relativeIndentation, this, childrenType);
		ppContents.add(newChildrenContents);
		return newChildrenContents;
	}

	/**
	 * Called to indicate that supplied childObject must be serialized at this pretty-print level<br>
	 * Either this object is already serialized, or should be created
	 * 
	 * This method is used for example to render an horizontal layout in pretty-printed text (a comma-separated list for example)
	 * 
	 * @param preludeForFirstItem
	 * @param prelude
	 * @param childrenObjects
	 * @param postude
	 * @param postludeForLastItem
	 * @param childrenType
	 * @return
	 */
	protected <C> ChildrenContents<C> appendToChildrenPrettyPrintContents(String preludeForFirstItem, String prelude,
			Supplier<List<? extends C>> childrenObjects, String postude, String postludeForLastItem, Class<C> childrenType) {

		return appendToChildrenPrettyPrintContents(preludeForFirstItem, prelude, childrenObjects, postude, postludeForLastItem, -1,
				childrenType);
	}

	/**
	 * Return textual representation for underlying object
	 * 
	 * @param context
	 * @return
	 */
	public String getTextualRepresentation(PrettyPrintContext context) {
		// TODO: implement a cache !!!!

		if (getASTNode() == null) {
			return getNormalizedTextualRepresentation(context);
		}

		DerivedRawSource derivedRawSource = computeTextualRepresentation(context);
		return derivedRawSource.getStringRepresentation();
	}

	/**
	 * Computes and return a {@link DerivedRawSource} representing textual representation as the merge of:
	 * <ul>
	 * <li>Last parsed version (as it was in the original source file)</li>
	 * <li>Eventual model modifications</li>
	 * </ul>
	 * 
	 * @return
	 */
	protected DerivedRawSource computeTextualRepresentation(PrettyPrintContext context) {

		DerivedRawSource derivedRawSource = new DerivedRawSource(getLastParsedFragment());

		if (getModelObject() == null) {
			logger.warning("Unexpected null model object in " + this);
			return derivedRawSource;
		}

		for (PrettyPrintableContents prettyPrintableContents : ppContents) {
			prettyPrintableContents.updatePrettyPrint(derivedRawSource, context);
		}

		return derivedRawSource;

	}

	/**
	 * Build new {@link P2PPNode} representing supplied pretty-printable object
	 * 
	 * @param object
	 *            Pretty-printable object
	 * @return
	 */
	public abstract <C> P2PPNode<?, C> makeObjectNode(C object);

	/**
	 * Return {@link P2PPNode} representing supplied pretty-printable object, when it exists
	 * 
	 * @param object
	 *            Pretty-printable object
	 * @return
	 */
	public <C> P2PPNode<?, C> getObjectNode(C object) {
		for (P2PPNode<?, ?> objectNode : getChildren()) {
			if (objectNode.getModelObject() == object) {
				return (P2PPNode<?, C>) objectNode;
			}
		}
		return null;
	}

	protected RawSourceFragment tryToIdentifyPrelude(String expectedPrelude, P2PPNode<?, ?> rootNode) {
		if (getLastParsedFragment() == null) {
			return null;
		}
		if (StringUtils.isEmpty(expectedPrelude)) {
			return null;
		}
		// System.out.println("On " + getClass().getSimpleName() + " trying to identify prelude [" + expectedPrelude + "]");
		prelude = findUnmappedSegmentBackwardFrom(expectedPrelude, getLastParsedFragment().getStartPosition(), rootNode);
		if (prelude == null) {
			// Try to find after trimming
			// System.out.println("cannot find, looking for [" + expectedPrelude.trim() + "]");
			prelude = findUnmappedSegmentBackwardFrom(expectedPrelude.trim(), getLastParsedFragment().getStartPosition(), rootNode);
		}
		if (prelude != null && prelude.getStartPosition().isBefore(getParent().getStartPosition())) {
			getParent().setStartPosition(prelude.getStartPosition());
		}
		// System.out.println("Finally found " + prelude);
		// if (prelude != null) {
		// System.out.println("RawText: [" + prelude.getRawText() + "]");
		// }
		return prelude;
	}

	protected RawSourceFragment tryToIdentifyPostlude(String expectedPostlude, P2PPNode<?, ?> rootNode) {
		if (getLastParsedFragment() == null) {
			return null;
		}
		if (StringUtils.isEmpty(expectedPostlude)) {
			return null;
		}
		// System.out.println("On " + getClass().getSimpleName() + " trying to identify postlude [" + expectedPostlude + "]");
		postlude = findUnmappedSegmentForwardFrom(expectedPostlude, getLastParsedFragment().getEndPosition(), rootNode);
		if (postlude == null) {
			// Try to find after trimming
			// System.out.println("cannot find, looking for [" + expectedPrelude.trim() + "]");
			postlude = findUnmappedSegmentForwardFrom(expectedPostlude.trim(), getLastParsedFragment().getEndPosition(), rootNode);
		}
		if (postlude != null && postlude.getEndPosition().isAfter(getParent().getEndPosition())) {
			getParent().setEndPosition(postlude.getEndPosition());
		}
		// System.out.println("Finally found " + postlude);
		// if (postlude != null) {
		// System.out.println("RawText: [" + postlude.getRawText() + "]");
		// }
		return postlude;
	}

	private RawSourceFragment findUnmappedSegmentBackwardFrom(String expected, RawSourcePosition position, P2PPNode<?, ?> rootNode) {
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
				if (rootNode.isFragmentMapped(f)) {
					// This fragment intersects another mapped fragment, abort
					return null;
				}
				if (f.getRawText().equals(expected)) {
					return f;
				}
				i++;
			} catch (ArrayIndexOutOfBoundsException e) {
				positionStillValid = false;
			}
		}
		return null;
	}

	private RawSourceFragment findUnmappedSegmentForwardFrom(String expected, RawSourcePosition position, P2PPNode<?, ?> rootNode) {
		int length = expected.length();
		int i = 0;
		// System.out.println("Forward looking for [" + expected + "] from " + position);
		boolean positionStillValid = true;
		while (positionStillValid) {
			try {
				RawSourcePosition start = position.increment(i);
				RawSourcePosition end = position.increment(length + i);
				RawSourceFragment f = position.getOuterType().makeFragment(start, end);
				// System.out.println("Test forward fragment " + f + " [" + f.getRawText() + "]");
				if (rootNode.isFragmentMapped(f)) {
					// This fragment intersects another mapped fragment, abort
					return null;
				}
				if (f.getRawText().equals(expected)) {
					return f;
				}
				i++;
			} catch (ArrayIndexOutOfBoundsException e) {
				positionStillValid = false;
			}
		}
		return null;
	}

	/**
	 * Search if supplied fragment is entirely or partially mapped in any of this node's subtree, either by the lastParsedFragment, or the
	 * prelude, or the postlude
	 * 
	 * @param fragment
	 * @return
	 */
	public boolean isFragmentMapped(RawSourceFragment fragment) {
		if (getChildren().size() == 0) {
			// This is a leaf node
			if (fragment.intersects(getLastParsedFragment())) {
				return true;
			}
			if (getPrelude() != null && fragment.intersects(getPrelude())) {
				return true;
			}
			if (getPostlude() != null && fragment.intersects(getPostlude())) {
				return true;
			}
		}
		else {
			// Otherwise, look in children
			for (P2PPNode<?, ?> child : getChildren()) {
				if (child.isFragmentMapped(fragment)) {
					return true;
				}
			}
		}
		return false;
	}

	// TODO
	// Provide better implementation by researching in backward direction first occurence of prelude
	// while text is not associated by any semantics
	/*private void handlePreludeExtension(P2PPNode<?, T> node) {
		if (node.getLastParsedFragment() == null) {
			return;
		}
		if (getPrelude() == null || getPrelude().equals("")) {
			// Nothing to do
		}
		else if (getPrelude().equals(P2PPNode.LINE_SEPARATOR)) {
			// We go to previous line, when possible
			if (node.getStartPosition().canDecrement()) {
				node.setStartPosition(node.getStartPosition().decrement());
			}
		}
		else if (getPrelude().equals(",")) {
			System.out.println("Tiens c'est bon, j'ai ma virgule");
			if (node.getStartPosition().canDecrement()) {
				node.setStartPosition(node.getStartPosition().decrement());
			}
		}
	
		// Workaround to handle indentation: please do better here !!!
		if (getRelativeIndentation() == 1) {
			if (node.getStartPosition().canDecrement()) {
				node.setStartPosition(node.getStartPosition().decrement());
			}
		}
	}*/

}
