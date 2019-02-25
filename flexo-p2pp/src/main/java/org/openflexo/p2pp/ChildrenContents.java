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

	private final String preludeForFirstItem;
	private final String postludeForLastItem;

	private P2PPNode<?, ?> parentNode;
	// Unused private Class<T> objectType;
	private Supplier<List<? extends T>> childrenObjectsSupplier;

	private ArrayList<P2PPNode<?, T>> lastParsedNodes;
	private List<P2PPNode<?, T>> childrenNodes;

	private RawSourcePosition defaultInsertionPoint;

	public ChildrenContents(String prelude, Supplier<List<? extends T>> childrenObjects, String postlude, int relativeIndentation,
			P2PPNode<?, ?> parentNode, Class<T> objectType) {
		this(null, prelude, childrenObjects, postlude, null, relativeIndentation, parentNode, objectType);
	}

	public ChildrenContents(String preludeForFirstItem, String prelude, Supplier<List<? extends T>> childrenObjects, String postlude,
			String postludeForLastItem, int relativeIndentation, P2PPNode<?, ?> parentNode, Class<T> objectType) {
		super(prelude, postlude, relativeIndentation);
		this.preludeForFirstItem = preludeForFirstItem;
		this.postludeForLastItem = postludeForLastItem;
		// System.out.println("ChildrenContents for " + objectType);
		// System.out.println("prelude=[" + getPrelude() + "]");
		// System.out.println("postlude=[" + getPostlude() + "]");
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
			// System.out.println("Hop: " + lastParsedNodes.get(0) + " of " + lastParsedNodes.get(0).getClass());
			// System.out.println("Fragment: " + lastParsedNodes.get(0).getLastParsedFragment());
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

		List<? extends T> allObjects = childrenObjectsSupplier.get();
		for (int i = 0; i < allObjects.size(); i++) {
			T childObject = allObjects.get(i);
			String applicablePrelude = getPrelude();
			if (i == 0 && preludeForFirstItem != null) {
				applicablePrelude = preludeForFirstItem;
			}
			String applicablePostlude = getPostlude();
			if (i == allObjects.size() - 1 && postludeForLastItem != null) {
				applicablePostlude = postludeForLastItem;
			}
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				parentNode.addToChildren(childNode);
			}
			String childPrettyPrint = childNode.getNormalizedTextualRepresentation(context.derive(getRelativeIndentation()));
			if (StringUtils.isNotEmpty(childPrettyPrint)) {
				if (StringUtils.isNotEmpty(applicablePrelude)) {
					sb.append(applicablePrelude);
				}
				sb.append(childPrettyPrint);
				if (StringUtils.isNotEmpty(applicablePostlude)) {
					sb.append(applicablePostlude);
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
		RawSourcePosition insertionPointAfterPostlude = defaultInsertionPoint;

		// System.out.println("Insertion point pour commencer: " + insertionPoint);

		PrettyPrintContext derivedContext = context.derive(getRelativeIndentation());

		List<? extends T> childrenObjectsList = childrenObjectsSupplier.get();

		// System.out.println("Handling children: " + childrenObjectsList);

		for (T childObject : childrenObjectsList) {
			// System.out.println("*** Handling " + childObject);
			// System.out.println("insertionPoint=" + insertionPoint);
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				parentNode.addToChildren(childNode);
			}

			if (!lastParsedNodes.contains(childNode)) {
				// In this case manage insertion
				if (postludeForLastItem != null && childObject == childrenObjectsList.get(childrenObjectsList.size() - 1)) {
					// We have a special postlude for last item and this is the last item
					String insertThis = (getPostlude() != null ? getPostlude() : "") + (getPrelude() != null ? getPrelude() : "")
							+ childNode.getTextualRepresentation(derivedContext);
					System.out.println("Et donc j'insere en " + insertionPoint + " value=[" + insertThis + "]");
					derivedRawSource.insert(insertionPoint, insertThis);
				}
				else {
					String insertThis = (getPrelude() != null ? getPrelude() : "") + childNode.getTextualRepresentation(derivedContext)
							+ (getPostlude() != null ? getPostlude() : "");
					System.out.println("Et donc j'insere en " + insertionPoint + " value=[" + insertThis + "]");
					derivedRawSource.insert(insertionPointAfterPostlude, insertThis);
				}

				/*if (postludeForLastItem == null) { // TODO tester si dernier element
					String insertThis = (getPrelude() != null ? getPrelude() : "") + childNode.getTextualRepresentation(derivedContext)
					+ (getPostlude() != null ? getPostlude() : "");
					System.out.println("Et donc j'insere en " + insertionPoint + " value=[" + insertThis + "]");
					derivedRawSource.insert(insertionPointAfterPostlude, insertThis);
				}
				else {
					// There is a special postlude for the last element
				}*/
			}
			else {
				// OK, this is an update
				derivedRawSource.replace(childNode.getLastParsedFragment(), childNode.getTextualRepresentation(context));
				insertionPoint = childNode.getLastParsedFragment().getEndPosition();
				insertionPointAfterPostlude = childNode.getLastParsedFragment().getEndPosition();
				System.out.println("Hop, pour " + childObject + " insertionPoint =" + insertionPoint);
				if (childNode.getPostlude() != null) {
					insertionPointAfterPostlude = childNode.getPostlude().getEndPosition();
					System.out.println("Hop, pour " + childObject + " je decale en =" + insertionPoint);
				}
			}

			/*if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				parentNode.addToChildren(childNode);
				// System.out.println("Nouveau childNode for " + childObject);
				// System.out.println("ASTNode " + childNode.getASTNode());
				// System.out.println("FML= " + childNode.getFMLRepresentation(context));
				if (postludeForLastItem == null) { // TODO tester si dernier element
					String insertThis = (getPrelude() != null ? getPrelude() : "") + childNode.getTextualRepresentation(derivedContext)
							+ (getPostlude() != null ? getPostlude() : "");
					System.out.println("Et donc j'insere en " + insertionPoint + " value=[" + insertThis + "]");
					derivedRawSource.insert(insertionPointAfterPostlude, insertThis);
				}
				else {
					// There is a special postlude for the last element
					String insertThis = (getPostlude() != null ? getPostlude() : "") + (getPrelude() != null ? getPrelude() : "")
							+ childNode.getTextualRepresentation(derivedContext);
					System.out.println("Et donc j'insere en " + insertionPoint + " value=[" + insertThis + "]");
					derivedRawSource.insert(insertionPoint, insertThis);
				}
			}
			else {
				if (lastParsedNodes.contains(childNode)) {
					// OK, this is an update
					derivedRawSource.replace(childNode.getLastParsedFragment(), childNode.getTextualRepresentation(context));
					insertionPoint = childNode.getLastParsedFragment().getEndPosition();
					insertionPointAfterPostlude = childNode.getLastParsedFragment().getEndPosition();
					System.out.println("Hop, pour " + childObject + " insertionPoint =" + insertionPoint);
					if (childNode.getPostlude() != null) {
						insertionPointAfterPostlude = childNode.getPostlude().getEndPosition();
						System.out.println("Hop, pour " + childObject + " je decale en =" + insertionPoint);
					}
				}
				else {
					if (postludeForLastItem == null) {
						String insertThis = (getPrelude() != null ? getPrelude() : "") + childNode.getTextualRepresentation(derivedContext)
								+ (getPostlude() != null ? getPostlude() : "");
						System.out.println("Et donc j'insere en " + insertionPoint + " value=[" + insertThis + "]");
						derivedRawSource.insert(insertionPointAfterPostlude, insertThis);
					}
					else {
						// There is a special postlude for the last element
						String insertThis = (getPostlude() != null ? getPostlude() : "") + (getPrelude() != null ? getPrelude() : "")
								+ childNode.getTextualRepresentation(derivedContext);
						System.out.println("Et donc j'insere en " + insertionPoint + " value=[" + insertThis + "]");
						derivedRawSource.insert(insertionPoint, insertThis);
					}
				}
			}*/
			nodesToBeRemoved.remove(childNode);
		}

		for (P2PPNode<?, T> removedNode : nodesToBeRemoved) {
			boolean wasFirst = (lastParsedNodes.get(0) == removedNode);
			boolean wasLast = (lastParsedNodes.get(lastParsedNodes.size() - 1) == removedNode);
			// boolean isFirst = (childrenObjectsList.get(0) == removedNode.getModelObject());
			// boolean isLast = (childrenObjectsList.get(childrenObjectsList.size() - 1) == removedNode.getModelObject());
			RawSourcePosition startPosition = removedNode.getLastParsedFragment().getStartPosition();
			RawSourcePosition endPosition = removedNode.getLastParsedFragment().getEndPosition();
			if (wasFirst && wasLast) {
				if (removedNode.getPrelude() != null) {
					startPosition = removedNode.getPrelude().getStartPosition();
				}
				if (removedNode.getPostlude() != null) {
					endPosition = removedNode.getPostlude().getEndPosition();
				}
				derivedRawSource.remove(startPosition.getOuterType().makeFragment(startPosition, endPosition));
			}
			else if (wasFirst) {
				if (StringUtils.isNotEmpty(preludeForFirstItem)) {
					derivedRawSource.remove(startPosition.getOuterType().makeFragment(startPosition, endPosition));
					P2PPNode<?, T> newFirstNode = parentNode.getObjectNode(childrenObjectsList.get(0));
					derivedRawSource.remove(newFirstNode.getPrelude());
				}
				else {
					if (removedNode.getPrelude() != null) {
						startPosition = removedNode.getPrelude().getStartPosition();
					}
					if (removedNode.getPostlude() != null) {
						endPosition = removedNode.getPostlude().getEndPosition();
					}
					derivedRawSource.remove(startPosition.getOuterType().makeFragment(startPosition, endPosition));
				}
			}
			else if (wasLast) {
				if (StringUtils.isNotEmpty(postludeForLastItem)) {
					derivedRawSource.remove(startPosition.getOuterType().makeFragment(startPosition, endPosition));
					P2PPNode<?, T> newLastNode = parentNode.getObjectNode(childrenObjectsList.get(childrenObjectsList.size() - 1));
					derivedRawSource.remove(newLastNode.getPostlude());
				}
				else {
					if (removedNode.getPrelude() != null) {
						startPosition = removedNode.getPrelude().getStartPosition();
					}
					if (removedNode.getPostlude() != null) {
						endPosition = removedNode.getPostlude().getEndPosition();
					}
					derivedRawSource.remove(startPosition.getOuterType().makeFragment(startPosition, endPosition));
				}
			}
			else {
				if (removedNode.getPrelude() != null) {
					startPosition = removedNode.getPrelude().getStartPosition();
				}
				if (removedNode.getPostlude() != null) {
					endPosition = removedNode.getPostlude().getEndPosition();
				}
				derivedRawSource.remove(startPosition.getOuterType().makeFragment(startPosition, endPosition));
			}

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
	public void initializePrettyPrint(P2PPNode<?, ?> rootNode, PrettyPrintContext context) {
		handlePreludeAndPoslude(rootNode, context);
		List<? extends T> allObjects = childrenObjectsSupplier.get();
		for (int i = 0; i < allObjects.size(); i++) {
			T childObject = allObjects.get(i);
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			childNode.initializePrettyPrint(rootNode, context.derive(getRelativeIndentation()));
		}
	}

	private void handlePreludeAndPoslude(P2PPNode<?, ?> rootNode, PrettyPrintContext context) {
		/*for (T childObject : childrenObjectsSupplier.get()) {
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			// System.out.println("Handle prelude and postlude extension " + childNode + " was: " + childNode.getLastParsedFragment());
			// handlePreludeExtension(childNode);
			// handlePostludeExtension(childNode);
			// System.out.println("Handle prelude and postlude extension " + childNode + " now: " + childNode.getLastParsedFragment());
		}*/

		List<? extends T> allObjects = childrenObjectsSupplier.get();
		for (int i = 0; i < allObjects.size(); i++) {
			T childObject = allObjects.get(i);
			String applicablePrelude = getPrelude();
			if (i == 0 && StringUtils.isNotEmpty(preludeForFirstItem)) {
				applicablePrelude = preludeForFirstItem;
			}
			if (StringUtils.isEmpty(applicablePrelude) && context.getIndentation() > 0) {
				applicablePrelude = context.getResultingIndentation();
			}
			String applicablePostlude = getPostlude();
			if (i == allObjects.size() - 1 && StringUtils.isNotEmpty(postludeForLastItem)) {
				applicablePostlude = postludeForLastItem;
			}
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			if (StringUtils.isNotEmpty(applicablePrelude)) {
				childNode.tryToIdentifyPrelude(applicablePrelude, rootNode);
			}
			if (StringUtils.isNotEmpty(applicablePostlude)) {
				childNode.tryToIdentifyPostlude(applicablePostlude, rootNode);
			}
		}

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

	// TODO
	// Provide better implementation by researching in forward direction first occurence of postlude
	// while text is not associated by any semantics
	/*private void handlePostludeExtension(P2PPNode<?, T> node) {
	
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
	}*/

}
