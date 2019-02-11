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

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.toolbox.StringUtils;

/**
 * Specification of the pretty-print of a collection of children objects
 * 
 * @author sylvain
 *
 * @param <T>
 */
public class ChildrenContents<T> extends PrettyPrintableContents {

	private P2PPNode<?, ?> parentNode;
	// Unused private Class<T> objectType;
	private Supplier<List<? extends T>> childrenObjectsSupplier;

	private List<P2PPNode<?, T>> lastParsedNodes;
	private List<P2PPNode<?, T>> childrenNodes;

	private RawSourcePosition defaultInsertionPoint;

	public ChildrenContents(String prelude, Supplier<List<? extends T>> childrenObjects, String postlude, int identationLevel,
			P2PPNode<?, ?> parentNode, Class<T> objectType) {
		super(prelude, postlude, identationLevel);
		this.parentNode = parentNode;
		// Unused this.objectType = objectType;
		// setFragment(childNode.getLastParsedFragment());

		childrenObjectsSupplier = childrenObjects;

		// System.out.println("Tous les children: " + parentNode.getChildren());
		// System.out.println("Type: " + objectType);
		lastParsedNodes = new ArrayList<>();
		for (P2PPNode<?, ?> objectNode : parentNode.getChildren()) {
			// TODO: Y a t'il vraiment besoin de se lier à connie ici ?
			if (TypeUtils.isOfType(objectNode.getModelObject(), objectType)) {
				lastParsedNodes.add((P2PPNode<?, T>) objectNode);
			}
		}
		// System.out.println("Tous les nodes qu'on considere: " + lastParsedNodes);

		// RawSourceFragment fragment = null;

		/*for (FMLObjectNode<?, T> objectNode : lastParsedNodes) {
			System.out.println("> fragment " + objectNode.getLastParsedFragment());
		}*/

		if (lastParsedNodes.size() > 0) {
			defaultInsertionPoint = lastParsedNodes.get(0).getLastParsedFragment().getStartPosition();
		}
		else {
			defaultInsertionPoint = parentNode.getDefaultInsertionPoint();
		}

	}

	public List<P2PPNode<?, T>> getChildrenNodes() {
		return childrenNodes;
	}

	@Override
	public String getNormalizedPrettyPrint(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();

		for (T childObject : childrenObjectsSupplier.get()) {
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				parentNode.addToChildren(childNode);
			}
			String childPrettyPrint = childNode.getNormalizedTextualRepresentation(context.derive(getRelativeIndentation()));
			if (StringUtils.isNotEmpty(childPrettyPrint)) {
				if (StringUtils.isNotEmpty(getPrelude())) {
					sb.append(getPrelude());
				}
				sb.append(childPrettyPrint);
				if (StringUtils.isNotEmpty(getPostlude())) {
					sb.append(getPostlude());
				}
			}
		}

		return sb.toString();
	}

	@Override
	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {

		// System.out.println("Tous les children: " + parentNode.getChildren());
		// System.out.println("Type: " + objectType);
		List<P2PPNode<?, T>> nodesToBeRemoved = new ArrayList<>();
		nodesToBeRemoved.addAll(lastParsedNodes);

		// System.out.println("Tous les nodes qu'on considere: " + nodesToBeRemoved);

		RawSourcePosition insertionPoint = defaultInsertionPoint;

		// System.out.println("Insertion point pour commencer: " + insertionPoint);

		PrettyPrintContext derivedContext = context.derive(getRelativeIndentation());

		for (T childObject : childrenObjectsSupplier.get()) {
			// System.out.println("*** Je m'occupe de " + childObject);
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				parentNode.addToChildren(childNode);
				// System.out.println("Nouveau childNode for " + childObject);
				// System.out.println("ASTNode " + childNode.getASTNode());
				// System.out.println("FML= " + childNode.getFMLRepresentation(context));
				String insertThis = (getPrelude() != null ? getPrelude() : "") + childNode.getTextualRepresentation(derivedContext)
						+ (getPostlude() != null ? getPostlude() : "");
				derivedRawSource.insert(insertionPoint, insertThis);
			}
			else {
				if (lastParsedNodes.contains(childNode)) {
					// OK, this is an update
					derivedRawSource.replace(childNode.getLastParsedFragment(), childNode.getTextualRepresentation(context));
					insertionPoint = childNode.getLastParsedFragment().getEndPosition();
					/*for (int i = 0; i < getPostlude().length(); i++) {
						insertionPoint = insertionPoint.increment();
					}*/
				}
				else {
					String insertThis = (getPrelude() != null ? getPrelude() : "") + childNode.getTextualRepresentation(derivedContext)
							+ (getPostlude() != null ? getPostlude() : "");
					derivedRawSource.insert(insertionPoint, insertThis);
				}
			}
			nodesToBeRemoved.remove(childNode);
		}

		for (P2PPNode<?, T> removedNode : nodesToBeRemoved) {
			RawSourcePosition startPosition = removedNode.getLastParsedFragment().getStartPosition();
			/*for (int i = 0; i < getPrelude().length(); i++) {
				startPosition = startPosition.decrement();
			}*/
			RawSourcePosition endPosition = removedNode.getLastParsedFragment().getEndPosition();
			/*for (int i = 0; i < getPostlude().length(); i++) {
				endPosition = endPosition.increment();
			}*/
			derivedRawSource.remove(startPosition.getOuterType().makeFragment(startPosition, endPosition));
		}

		/*FMLObjectNode<?, ?> childNode = getObjectNode(childObject);
		if (childNode == null) {
			childNode = makeObjectNode(childObject);
			addToChildren(childNode);
		}*/

		/*System.out.println("> Pour ChildContents " + childNode.getFMLObject() + " c'est plus complique");
		System.out.println("Et on calcule la nouvelle valeur:");
		derivedRawSource.replace(getFragment(), childNode.computeFMLRepresentation(context));*/
	}

	@Override
	public void handlePreludeAndPosludeExtensions() {
		for (T childObject : childrenObjectsSupplier.get()) {
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			System.out
					.println("Faudrait gerer l'extension eventuelle du noeud " + childNode + " was: " + childNode.getLastParsedFragment());
			handlePreludeExtension(childNode);
			handlePostludeExtension(childNode);
			System.out.println("On a gere l'extension eventuelle du noeud " + childNode + " now: " + childNode.getLastParsedFragment());
		}
	}

	private void handlePreludeExtension(P2PPNode<?, T> node) {
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

		// Workaround to handle indentation: please do better here !!!
		if (getRelativeIndentation() == 1) {
			if (node.getStartPosition().canDecrement()) {
				node.setStartPosition(node.getStartPosition().decrement());
			}
		}
	}

	private void handlePostludeExtension(P2PPNode<?, T> node) {

		if (node.getLastParsedFragment() == null) {
			return;
		}
		if (getPostlude() == null || getPostlude().equals("")) {
			// Nothing to do
		}
		else if (getPostlude().equals(P2PPNode.LINE_SEPARATOR)) {
			// We go to the next line, when possible
			// TODO: handle the case of this position is in another fragment
			RawSourcePosition p = node.getLastParsedFragment().getEndPosition();
			if (p.getLine() <= p.getOuterType().size() - 1) {
				RawSourcePosition newP = p.getOuterType().makePositionBeforeChar(p.getLine() + 1, 1);
				node.setEndPosition(newP);
			}
		}
	}

}
