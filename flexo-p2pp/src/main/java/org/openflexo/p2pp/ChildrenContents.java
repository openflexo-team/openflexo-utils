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

import org.openflexo.connie.type.TypeUtils;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
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

	private static final Logger logger = Logger.getLogger(ChildrenContents.class.getPackage().getName());

	private final String preludeForFirstItem;
	private final String postludeForLastItem;

	private P2PPNode<?, ?> parentNode;
	// Unused private Class<T> objectType;
	private Supplier<List<? extends T>> childrenObjectsSupplier;

	private ArrayList<P2PPNode<?, T>> lastParsedNodes;
	private List<P2PPNode<?, T>> childrenNodes;

	private RawSourcePosition defaultInsertionPoint;

	public ChildrenContents(String prelude, Supplier<List<? extends T>> childrenObjects, String postlude, Indentation indentation,
			P2PPNode<?, ?> parentNode, Class<T> objectType) {
		this(null, prelude, childrenObjects, postlude, null, indentation, parentNode, objectType);
	}

	public ChildrenContents(String preludeForFirstItem, String prelude, Supplier<List<? extends T>> childrenObjects, String postlude,
			String postludeForLastItem, Indentation indentation, P2PPNode<?, ?> parentNode, Class<T> objectType) {
		super(prelude, postlude, indentation);
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

		// Find a new default insertion point
		updateDefaultInsertionPoint();

	}

	public List<P2PPNode<?, T>> getChildrenNodes() {
		return childrenNodes;
	}

	@Override
	public String getNormalizedPrettyPrint(PrettyPrintContext context) {
		StringBuffer sb = new StringBuffer();

		List<? extends T> allObjects = childrenObjectsSupplier.get();

		if (allObjects == null) {
			return "";
		}

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
				if (childNode == null) {
					logger.severe("Cannot make node for " + childObject);
					Thread.dumpStack();
				}
				else {
					parentNode.addToChildren(childNode);
				}
			}
			if (childNode != null) {
				childNode.setRegisteredForContents(this);
				String childPrettyPrint = childNode.getNormalizedTextualRepresentation(context.derive(getIndentation()));
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
		}

		return sb.toString();
	}

	private void updateDefaultInsertionPoint() {
		if (lastParsedNodes.size() > 0) {
			// System.out.println("Hop: " + lastParsedNodes.get(0) + " of " + lastParsedNodes.get(0).getClass());
			// System.out.println("Fragment: " + lastParsedNodes.get(0).getLastParsedFragment());
			defaultInsertionPoint = lastParsedNodes.get(0).getLastParsedFragment().getStartPosition();
		}
		else {
			defaultInsertionPoint = parentNode.getDefaultInsertionPoint();
		}
	}

	@Override
	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {

		// System.out.println("Children: " + parentNode.getChildren());
		// System.out.println("Type: " + objectType);
		List<P2PPNode<?, T>> nodesToBeRemoved = new ArrayList<>();
		for (P2PPNode<?, T> lastParsedNode : lastParsedNodes) {
			if (lastParsedNode.getRegisteredForContents() == this) {
				nodesToBeRemoved.add(lastParsedNode);
			}
		}

		// Find a new default insertion point
		updateDefaultInsertionPoint();

		RawSourcePosition insertionPoint = defaultInsertionPoint;
		RawSourcePosition insertionPointAfterPostlude = defaultInsertionPoint;

		PrettyPrintContext derivedContext = context.derive(getIndentation());

		List<? extends T> childrenObjectsList = childrenObjectsSupplier.get();

		// System.out.println("Handling children: " + childrenObjectsList);
		// System.out.println("insertionPoint=" + insertionPoint);
		// System.out.println("PARENT insertionPoint=" + parentNode.getDefaultInsertionPoint());

		for (int i = 0; i < childrenObjectsList.size(); i++) {
			T childObject = childrenObjectsList.get(i);
			boolean isFirst = (i == 0);
			boolean isLast = (i == childrenObjectsList.size() - 1);
			// System.out.println("*** Handling " + childObject);
			// System.out.println("insertionPoint=" + insertionPoint);
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			boolean childNodeWasRebuild = false;
			if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				parentNode.addToChildren(childNode);
				childNodeWasRebuild = true;
			}
			childNode.setRegisteredForContents(this);
			if (!lastParsedNodes.contains(childNode)) {
				// In this case manage insertion

				boolean handleSpecificPrelude = false;
				boolean handleSpecificPostlude = false;
				String applicablePrelude = getPrelude();
				if (isFirst && preludeForFirstItem != null) {
					applicablePrelude = preludeForFirstItem;
					handleSpecificPrelude = true;
				}
				String applicablePostlude = getPostlude();
				if (isLast && postludeForLastItem != null) {
					applicablePostlude = postludeForLastItem;
					handleSpecificPostlude = true;
				}

				if (handleSpecificPrelude) {
					if (handleSpecificPostlude) {
						String insertThis = applicablePrelude + childNode.getTextualRepresentation(derivedContext) + applicablePostlude;
						System.out.println("Case 1: Inserting in " + insertionPoint + " value=[" + insertThis + "]");
						derivedRawSource.insert(insertionPoint, insertThis);
					}
					else {
						String insertThis = applicablePrelude + childNode.getTextualRepresentation(derivedContext) + applicablePostlude;
						System.out.println("Case 2: Inserting in " + insertionPoint + " value=[" + insertThis + "]");
						derivedRawSource.insert(insertionPoint, insertThis);
					}
				}
				else {
					if (handleSpecificPostlude) {
						// We have a special postlude for last item and this is the last item
						// System.out.println("****** For node " + childNode);
						// System.out.println("ASTNode=" + childNode.getASTNode());
						// System.out.println("childNodeWasRebuild=" + childNodeWasRebuild);
						String insertThis;
						if (!childNodeWasRebuild) {
							// This is the last item, and previous item didn't serialize any postude, do it now
							insertThis = (getPostlude() != null ? getPostlude() : "") + applicablePrelude
									+ childNode.getTextualRepresentation(derivedContext)
									+ (lastParsedNodes.size() > 0 ? "" : applicablePostlude);
							System.out.println("Case 3.1: Inserting in " + insertionPoint + " value=[" + insertThis + "]");
						}
						else {
							insertThis = applicablePrelude + childNode.getTextualRepresentation(derivedContext)
									+ (lastParsedNodes.size() > 0 ? "" : applicablePostlude);
							System.out.println("Case 3.2: Inserting in " + insertionPoint + " value=[" + insertThis + "]");
						}
						derivedRawSource.insert(insertionPoint, insertThis);
					}
					else {
						String insertThis = applicablePrelude + childNode.getTextualRepresentation(derivedContext) + applicablePostlude;
						System.out.println("Case 4: Inserting in " + insertionPoint + " value=[" + insertThis + "]");
						derivedRawSource.insert(insertionPointAfterPostlude, insertThis);
					}
				}

			}
			else {
				// OK, this is an update
				// try {
				derivedRawSource.replace(childNode.getLastParsedFragment(), childNode.getTextualRepresentation(context));
				/*} catch (StringIndexOutOfBoundsException e) {
					System.out.println("Ca foire quand on traite: " + childNode.getLastParsedFragment());
					System.out.println("soit: [" + childNode.getLastParsedFragment().getRawText() + "]");
				}*/
				insertionPoint = childNode.getLastParsedFragment().getEndPosition();
				insertionPointAfterPostlude = childNode.getLastParsedFragment().getEndPosition();
				// System.out.println("Updating " + childObject + " insertionPoint =" + insertionPoint);
				if (childNode.getPostlude() != null) {
					insertionPointAfterPostlude = childNode.getPostlude().getEndPosition();
				}
			}

			// Marks the node beeing handled
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
				if (StringUtils.isNotEmpty(preludeForFirstItem) && childrenObjectsList.size() > 0) {
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
				if (StringUtils.isNotEmpty(postludeForLastItem) && childrenObjectsList.size() > 0) {
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

		// Update parent default insertion point
		parentNode.setDefaultInsertionPoint(insertionPointAfterPostlude);
	}

	@Override
	public void initializePrettyPrint(P2PPNode<?, ?> rootNode, PrettyPrintContext context) {
		handlePreludeAndPoslude(rootNode, context);
		List<? extends T> allObjects = childrenObjectsSupplier.get();
		for (int i = 0; i < allObjects.size(); i++) {
			T childObject = allObjects.get(i);
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			childNode.initializePrettyPrint(rootNode, context.derive(getIndentation()));
			childNode.setRegisteredForContents(this);
		}
	}

	private void handlePreludeAndPoslude(P2PPNode<?, ?> rootNode, PrettyPrintContext context) {
		List<? extends T> allObjects = childrenObjectsSupplier.get();
		for (int i = 0; i < allObjects.size(); i++) {
			T childObject = allObjects.get(i);
			String applicablePrelude = getPrelude();
			if (i == 0 && StringUtils.isNotEmpty(preludeForFirstItem)) {
				applicablePrelude = preludeForFirstItem;
			}
			if (StringUtils.isEmpty(applicablePrelude) && context.getIndentation() != Indentation.DoNotIndent) {
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

}
