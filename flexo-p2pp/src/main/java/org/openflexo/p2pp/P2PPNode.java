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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.openflexo.connie.expr.parser.node.Token;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

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
	protected T fmlObject;

	private P2PPNode<?, ?> parent;
	private ArrayList<P2PPNode<?, ?>> children = new ArrayList<>();

	protected RawSourcePosition startPosition;
	protected RawSourcePosition endPosition;
	protected RawSourceFragment parsedFragment;

	private List<PrettyPrintableContents> ppContents = new ArrayList<>();

	public static final String SPACE = " ";
	public static final String LINE_SEPARATOR = "\n";

	public P2PPNode(T aFMLObject, N astNode) {
		this.astNode = astNode;
		this.fmlObject = aFMLObject;
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

	public abstract T buildFMLObjectFromAST(N astNode);

	public abstract P2PPNode<N, T> deserialize();

	public final void finalizeDeserialization() {
		// Override when required
	}

	public T getFMLObject() {
		return fmlObject;
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
	 * Return fragment representing underlying FMLObject as a String in FML language, as it was last parsed
	 * 
	 * @return
	 */
	public RawSourceFragment getLastParsedFragment() {
		if (parsedFragment == null && getStartPosition() != null && getEndPosition() != null) {
			parsedFragment = getRawSource().makeFragment(getStartPosition(), getEndPosition());
		}
		return parsedFragment;
	}

	/**
	 * Build and return a new pretty-print context
	 * 
	 * @return
	 */
	public PrettyPrintContext makePrettyPrintContext() {
		return new DefaultPrettyPrintContext(0);
	}

	public final void initializePrettyPrint() {
		preparePrettyPrint();
		System.out.println("On regarde si pour ce noeud " + this + " il faudrait pas etendre le fragment " + getLastParsedFragment());

		for (PrettyPrintableContents prettyPrintableContents : ppContents) {
			prettyPrintableContents.handlePreludeAndPosludeExtensions();
		}

	}

	protected void preparePrettyPrint() {
		defaultInsertionPoint = getStartPosition();
	}

	protected abstract void prepareNormalizedPrettyPrint();

	public final String getNormalizedTextualRepresentation(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();
		for (PrettyPrintableContents child : ppContents) {
			sb.append(child.getNormalizedPrettyPrint(context));
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
	 * Append {@link StaticContents}, whose value is intented to replace text determined with supplied fragment
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
	 * Append {@link StaticContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
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
	 * Append {@link StaticContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
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
	 * Append {@link StaticContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
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
	 * Append {@link StaticContents}, whose value is intented to replace text determined with supplied fragment
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
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link StaticContents}, whose value is intented to replace text determined with supplied fragment
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
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link DynamicContents}, whose value is intented to replace text determined with supplied fragment
	 * 
	 * @param stringRepresentationSupplier
	 *            gives dynamic value of that contents
	 * @param fragment
	 */
	public void appendDynamicContents(Supplier<String> stringRepresentationSupplier, RawSourceFragment fragment) {
		DynamicContents newContents = new DynamicContents(null, stringRepresentationSupplier, null, fragment);
		ppContents.add(newContents);
		defaultInsertionPoint = fragment.getEndPosition();
	}

	/**
	 * Append {@link DynamicContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
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
	 * Append {@link DynamicContents}, whose value is intented to replace text determined with supplied fragment
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
	 * Append {@link DynamicContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
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
	 * Append {@link DynamicContents}, whose value is intented to replace text determined with supplied fragment
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
	 * Append {@link DynamicContents}, whose value is intented to be inserted at current location (no current contents was parsed in initial
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
	 * @param childObject
	 */
	protected <C> void appendToChildPrettyPrintContents(String prelude, C childObject, String postude, int indentationLevel) {

		P2PPNode<?, C> childNode = getObjectNode(childObject);
		if (childNode == null) {
			childNode = makeObjectNode(childObject);
			addToChildren(childNode);
		}
		ChildContents<?> newChildContents = new ChildContents<>(prelude, childNode, postude, indentationLevel);
		ppContents.add(newChildContents);
	}

	/**
	 * Called to indicate that supplied childObject must be serialized at this pretty-print level<br>
	 * Either this object is already serialized, or should be created
	 * 
	 * @param childObject
	 */
	protected <C> void appendToChildrenPrettyPrintContents(String prelude, Supplier<List<? extends C>> childrenObjects, String postude,
			int indentationLevel, Class<C> childrenType) {

		ChildrenContents<C> newChildrenContents = new ChildrenContents<>(prelude, childrenObjects, postude, indentationLevel, this,
				childrenType);
		ppContents.add(newChildrenContents);
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

		if (getFMLObject() == null) {
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
	 * Return {@link P2PPNode} representing supplied pretty-printable object, when existant
	 * 
	 * @param object
	 *            Pretty-printable object
	 * @return
	 */
	public <C> P2PPNode<?, C> getObjectNode(C object) {
		for (P2PPNode<?, ?> objectNode : getChildren()) {
			if (objectNode.getFMLObject() == object) {
				return (P2PPNode<?, C>) objectNode;
			}
		}
		return null;
	}

	/**
	 * Return position as a cursor BEFORE the targetted character
	 * 
	 * @param line
	 * @param pos
	 * @return
	 */
	public RawSourcePosition getPositionBefore(Token token) {
		return getRawSource().makePositionBeforeChar(token.getLine(), token.getPos() - 1);
	}

	/**
	 * Return position as a cursor AFTER the targetted character
	 * 
	 * @param line
	 * @param pos
	 * @return
	 */
	public RawSourcePosition getPositionAfter(Token token) {
		return getRawSource().makePositionAfterChar(token.getLine(), token.getPos());
	}

	/**
	 * Return fragment matching supplied node in AST
	 * 
	 * @param token
	 * @return
	 */
	// public abstract RawSourceFragment getFragment(N node);

	/**
	 * Return fragment matching supplied nodes in AST
	 * 
	 * @param token
	 * @return
	 */
	// public abstract RawSourceFragment getFragment(N node, List<? extends N> otherNodes);

}
