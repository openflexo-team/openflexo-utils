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

import java.util.function.Supplier;
import java.util.logging.Logger;

import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.toolbox.StringUtils;

/**
 * Specification of the pretty-print of a child object
 * 
 * @author sylvain
 *
 * @param <T>
 *            Type of child object beeing handled in this {@link ChildContents}
 */
public class ChildContents<T> extends PrettyPrintableContents {

	private static final Logger logger = Logger.getLogger(ChildContents.class.getPackage().getName());

	private Supplier<T> childObjectSupplier;
	private P2PPNode<?, ?> parentNode;
	private P2PPNode<?, T> parsedChildNode;

	public ChildContents(String prelude, Supplier<T> childObjectSupplier, String postlude, Indentation indentation,
			P2PPNode<?, ?> parentNode) {
		super(prelude, postlude, indentation);
		this.childObjectSupplier = childObjectSupplier;
		this.parentNode = parentNode;
		T childObject = childObjectSupplier.get();
		parsedChildNode = parentNode.getObjectNode(childObject);
		if (parsedChildNode != null) {
			setFragment(parsedChildNode.getLastParsedFragment());
			parsedChildNode.setRegisteredForContents(this);
		}
	}

	public P2PPNode<?, T> getParsedChildNode() {
		return parsedChildNode;
	}

	@Override
	public String getNormalizedPrettyPrint(PrettyPrintContext context) {

		T childObject = childObjectSupplier.get();
		if (childObject != null) {
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				if (childNode != null) {
					parentNode.addToChildren(childNode);
				}
				else {
					logger.severe("Cannot create P2PPNode for " + childObject);
					return "";
				}
			}
			childNode.setRegisteredForContents(this);
			StringBuffer sb = new StringBuffer();
			String childPrettyPrint = childNode.getNormalizedTextualRepresentation(context.derive(getIndentation()));
			if (StringUtils.isNotEmpty(childPrettyPrint)) {
				if (StringUtils.isNotEmpty(getPrelude())) {
					sb.append(getPrelude());
				}
				sb.append(childPrettyPrint);
				if (StringUtils.isNotEmpty(getPostlude())) {
					sb.append(getPostlude());
				}
			}
			return sb.toString();
		}
		return null;
	}

	@Override
	public void updatePrettyPrint(DerivedRawSource derivedRawSource, PrettyPrintContext context) {

		T childObject = childObjectSupplier.get();
		if (childObject != null) {
			P2PPNode<?, T> childNode = parentNode.getObjectNode(childObject);
			if (childNode == null) {
				childNode = parentNode.makeObjectNode(childObject);
				parentNode.addToChildren(childNode);
			}
			childNode.setRegisteredForContents(this);
			PrettyPrintContext derivedContext = context.derive(getIndentation());
			if (parsedChildNode != null) {
				// replace existing by new
				if (childNode.getLastParsedFragment() != null) {
					// System.out.println("****** Replacing " + childNode.getLastParsedFragment() + " by ["
					// + childNode.computeTextualRepresentation(derivedContext).getStringRepresentation() + "]");
					derivedRawSource.replace(parsedChildNode.getLastParsedFragment(),
							childNode.computeTextualRepresentation(derivedContext));
				}
				else {
					// System.out.println("****** Replacing [" + parsedChildNode.getLastParsedFragment().getRawText() + "] by ["
					// + childNode.getNormalizedTextualRepresentation(derivedContext) + "]");
					derivedRawSource.replace(parsedChildNode.getLastParsedFragment(),
							childNode.getNormalizedTextualRepresentation(derivedContext));
				}
			}
			else {
				// System.out.println("****** Inserting at " + parentNode.getDefaultInsertionPoint() + " contents: "
				// + childNode.getTextualRepresentation(derivedContext));
				derivedRawSource.insert(parentNode.getDefaultInsertionPoint(), childNode.getTextualRepresentation(derivedContext));
			}
		}
		else {
			// new child is null
			if (parsedChildNode != null) {
				// object has been removed
				derivedRawSource.remove(getFragment());
			}
		}
	}

	@Override
	public void initializePrettyPrint(P2PPNode<?, ?> rootNode, PrettyPrintContext context) {
		if (parsedChildNode != null) {
			parsedChildNode.initializePrettyPrint(rootNode, context.derive(getIndentation()));
			parsedChildNode.setRegisteredForContents(this);
		}
	}

}
